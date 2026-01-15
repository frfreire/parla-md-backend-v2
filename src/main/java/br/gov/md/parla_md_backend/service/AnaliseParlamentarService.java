package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Parlamentar;
import br.gov.md.parla_md_backend.domain.Votacao;
import br.gov.md.parla_md_backend.domain.dto.*;
import br.gov.md.parla_md_backend.domain.AnaliseParlamentar;
import br.gov.md.parla_md_backend.exception.AnaliseParlamentarException;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IAnaliseParlamentarRepository;
import br.gov.md.parla_md_backend.repository.IParlamentarRepository;
import br.gov.md.parla_md_backend.repository.IVotacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnaliseParlamentarService {

    private final LlamaService llamaService;
    private final IAnaliseParlamentarRepository analiseRepository;
    private final IParlamentarRepository parlamentarRepository;
    private final IVotacaoRepository votacaoRepository;

    @Value("${analise.parlamentar.cache.ttl:86400}")
    private int cacheTtlSegundos;

    @Value("${analise.parlamentar.modelo.versao:1.0.0}")
    private String modeloVersao;

    @Value("${analise.parlamentar.minimo.votacoes:5}")
    private int minimoVotacoes;

    @Transactional
    public AnaliseParlamentarDTO analisar(SolicitarAnaliseParlamentarDTO request) {
        long inicioMs = System.currentTimeMillis();

        Parlamentar parlamentar = buscarParlamentar(request.getParlamentarId());

        if (!request.isForcarNovaAnalise()) {
            AnaliseParlamentar analiseCache = buscarAnaliseRecente(parlamentar, request.getTema());
            if (analiseCache != null) {
                log.info("Retornando análise do cache: {} - {}",
                        parlamentar.getNome(), request.getTema());
                return AnaliseParlamentarDTO.from(analiseCache);
            }
        }

        List<Votacao> votacoes = buscarVotacoesPorTema(parlamentar, request.getTema());

        if (votacoes.size() < minimoVotacoes) {
            throw AnaliseParlamentarException.dadosInsuficientes(
                    parlamentar.getId(), request.getTema());
        }

        try {
            AnaliseParlamentar analise = gerarNovaAnalise(
                    parlamentar,
                    request.getTema(),
                    votacoes,
                    request,
                    inicioMs
            );

            AnaliseParlamentar salva = analiseRepository.save(analise);

            log.info("Análise parlamentar gerada: {} - {} - Posicionamento: {}",
                    parlamentar.getNome(),
                    request.getTema(),
                    salva.getPosicionamento());

            return AnaliseParlamentarDTO.from(salva);

        } catch (Exception e) {
            long duracaoMs = System.currentTimeMillis() - inicioMs;
            registrarFalha(parlamentar, request.getTema(), e, duracaoMs);

            log.error("Erro ao gerar análise parlamentar: {}", e.getMessage(), e);
            throw AnaliseParlamentarException.erroProcessamento(e.getMessage(), e);
        }
    }

    @Cacheable(value = "analises-parlamentares", key = "#parlamentarId + '-' + #tema")
    public AnaliseParlamentarDTO buscarAnalisePorParlamentarETema(
            String parlamentarId,
            String tema) {

        AnaliseParlamentar analise = analiseRepository
                .findByParlamentar_IdAndTema(parlamentarId, tema)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Análise não encontrada para parlamentar " + parlamentarId + " e tema " + tema));

        return AnaliseParlamentarDTO.from(analise);
    }

    public List<AnaliseParlamentarDTO> buscarAnalisesPorParlamentar(String parlamentarId) {

        return analiseRepository.findAllByParlamentar_Id(parlamentarId, PageRequest.of(0, 20)).stream()
                .map(AnaliseParlamentarDTO::from)
                .collect(Collectors.toList());
    }

    public Page<AnaliseParlamentarDTO> buscarAnalisesPorTema(String tema, Pageable pageable) {
        return analiseRepository.findByTema(tema, PageRequest.of(0,20))
                .map(AnaliseParlamentarDTO::from);
    }

    public ComportamentoParlamentarDTO obterComportamento(
            String parlamentarId,
            String tema,
            LocalDateTime inicio,
            LocalDateTime fim) {

        Parlamentar parlamentar = buscarParlamentar(parlamentarId);

        List<Votacao> votacoes;
        if (inicio != null && fim != null) {
            votacoes = votacaoRepository.buscarPorParlamentarEPeriodo(
                    parlamentar.getId(), inicio, fim);
        } else {
            votacoes = votacaoRepository.findByParlamentarId(parlamentar.getId());
        }

        votacoes = filtrarPorTema(votacoes, tema);

        return construirComportamento(parlamentar, tema, votacoes, inicio, fim);
    }

    public Page<AnaliseParlamentarDTO> buscarAnalisesRecentes(Pageable pageable) {
        LocalDateTime limite = LocalDateTime.now().minusDays(30);

        return analiseRepository.findByDataAnaliseAfter(limite, pageable)
                .map(AnaliseParlamentarDTO::from);
    }

    public EstatisticasParlamentarDTO calcularEstatisticas(int dias) {
        LocalDateTime inicio = LocalDateTime.now().minusDays(dias);
        List<AnaliseParlamentar> analises = analiseRepository.findByDataAnaliseAfter(inicio);

        if (analises.isEmpty()) {
            return estatisticasVazias(inicio, LocalDateTime.now());
        }

        return calcularEstatisticasDeAnalises(analises, inicio);
    }

    @Transactional
    @CacheEvict(value = "analises-parlamentares", allEntries = true)
    public void limparExpiradas() {
        LocalDateTime agora = LocalDateTime.now();
        List<AnaliseParlamentar> expiradas = analiseRepository.buscarExpiradas(agora);

        if (!expiradas.isEmpty()) {
            analiseRepository.deleteAll(expiradas);
            log.info("Removidas {} análises parlamentares expiradas", expiradas.size());
        }
    }

    private Parlamentar buscarParlamentar(String parlamentarId) {
        return parlamentarRepository.findById(parlamentarId)
                .orElseThrow(() -> AnaliseParlamentarException
                        .parlamentarNaoEncontrado(parlamentarId));
    }

    private List<Votacao> buscarVotacoesPorTema(Parlamentar parlamentar, String tema) {
        List<Votacao> todasVotacoes = votacaoRepository
                .findByParlamentarId(parlamentar.getId());

        return filtrarPorTema(todasVotacoes, tema);
    }

    private List<Votacao> filtrarPorTema(List<Votacao> votacoes, String tema) {
        if (tema == null || tema.isBlank()) {
            return votacoes;
        }

        return votacoes.stream()
                .filter(v -> votacaoRelacionadaAoTema(v, tema))
                .collect(Collectors.toList());
    }

    private boolean votacaoRelacionadaAoTema(Votacao votacao, String tema) {
        if (votacao.getDescricao() != null &&
                votacao.getDescricao().toLowerCase().contains(tema.toLowerCase())) {
            return true;
        }

        return false;
    }

    private AnaliseParlamentar buscarAnaliseRecente(Parlamentar parlamentar, String tema) {
        LocalDateTime limite = LocalDateTime.now().minusSeconds(cacheTtlSegundos);

        return analiseRepository
                .findByParlamentar_IdAndTema(parlamentar.getId(), tema)
                .filter(a -> a.getDataAnalise().isAfter(limite))
                .filter(a -> a.getSucesso() != null && a.getSucesso())
                .orElse(null);
    }

    private AnaliseParlamentar gerarNovaAnalise(
            Parlamentar parlamentar,
            String tema,
            List<Votacao> votacoes,
            SolicitarAnaliseParlamentarDTO request,
            long inicioMs) {

        String prompt = construirPrompt(parlamentar, tema, votacoes, request);
        String promptSistema = construirPromptSistema();

        RespostaLlamaDTO resposta = llamaService.enviarRequisicao(
                prompt,
                promptSistema,
                true
        );

        ResultadoAnaliseParlamentarIA resultado = parsearResposta(resposta);

        long duracaoMs = System.currentTimeMillis() - inicioMs;

        Map<String, Object> estatisticas = calcularEstatisticasVotacoes(votacoes);

        return construirAnalise(
                parlamentar,
                tema,
                votacoes,
                resultado,
                estatisticas,
                resposta,
                duracaoMs
        );
    }

    private String construirPrompt(
            Parlamentar parlamentar,
            String tema,
            List<Votacao> votacoes,
            SolicitarAnaliseParlamentarDTO request) {

        StringBuilder prompt = new StringBuilder();

        prompt.append(String.format("""
            Analise o comportamento de votação do parlamentar %s sobre o tema "%s":
            
            PARLAMENTAR:
            - Nome: %s
            - Partido: %s
            - Estado: %s
            
            HISTÓRICO DE VOTAÇÕES (%d votações analisadas):
            """,
                parlamentar.getNome(),
                tema,
                parlamentar.getNome(),
                parlamentar.getSiglaPartido(),
                parlamentar.getSiglaUF(),
                votacoes.size()
        ));

        int limite = Math.min(10, votacoes.size());
        for (int i = 0; i < limite; i++) {
            Votacao v = votacoes.get(i);
            prompt.append(String.format(
                    "- %s: %s (Voto: %s)\n",
                    v.getDataHoraInicio().toLocalDate(),
                    v.getDescricao() != null ? v.getDescricao() : "Sem descrição",
                    v.getVoto()
            ));
        }

        if (votacoes.size() > limite) {
            prompt.append(String.format("... e mais %d votações\n", votacoes.size() - limite));
        }

        prompt.append("\nTAREFA:\n");
        prompt.append("Responda APENAS com um JSON no formato:\n\n");
        prompt.append("{\n");
        prompt.append("  \"posicionamento\": \"<PRO|CONTRA|NEUTRO>\",\n");
        prompt.append("  \"confiabilidade\": <número entre 0.0 e 1.0>,\n");
        prompt.append("  \"analiseDetalhada\": \"<análise em 3-5 frases>\",\n");

        if (request.isIncluirTendencias()) {
            prompt.append("  \"tendencia\": \"<descrição da tendência identificada>\",\n");
        }

        prompt.append("  \"padroesIdentificados\": [\n");
        prompt.append("    \"<padrão 1>\",\n");
        prompt.append("    \"<padrão 2>\"\n");
        prompt.append("  ],\n");

        prompt.append("  \"alinhamentoPolitico\": \"<descrição do alinhamento>\",\n");

        if (request.isIncluirPrevisoes()) {
            prompt.append("  \"previsaoComportamento\": \"<previsão de comportamento futuro>\",\n");
        }

        prompt.append("  \"votacoesChave\": [\n");
        prompt.append("    \"<descrição votação chave 1>\",\n");
        prompt.append("    \"<descrição votação chave 2>\"\n");
        prompt.append("  ]\n");
        prompt.append("}");

        return prompt.toString();
    }

    private String construirPromptSistema() {
        return """
            Você é um especialista em análise política e comportamento parlamentar brasileiro.
            Sua tarefa é analisar padrões de votação e identificar tendências políticas.
            
            Diretrizes:
            - Seja objetivo e baseie-se em dados
            - Identifique padrões consistentes
            - Avalie coerência ideológica
            - Considere contexto político brasileiro
            - Aponte mudanças de posicionamento
            - Preveja comportamento futuro com base em histórico
            
            Sempre responda em formato JSON válido.
            """;
    }

    private ResultadoAnaliseParlamentarIA parsearResposta(RespostaLlamaDTO resposta) {
        try {
            return llamaService.extrairJson(resposta, ResultadoAnaliseParlamentarIA.class);
        } catch (Exception e) {
            log.error("Erro ao parsear resposta do Llama: {}", e.getMessage());
            throw AnaliseParlamentarException.erroProcessamento("Resposta em formato inválido", e);
        }
    }

    private Map<String, Object> calcularEstatisticasVotacoes(List<Votacao> votacoes) {
        Map<String, Object> stats = new HashMap<>();

        long favoraveis = votacoes.stream().filter(Votacao::isVotoFavoravel).count();
        long contrarios = votacoes.stream().filter(Votacao::isVotoContrario).count();
        long abstencoes = votacoes.stream().filter(Votacao::isAbstencao).count();

        stats.put("total", votacoes.size());
        stats.put("favoraveis", favoraveis);
        stats.put("contrarios", contrarios);
        stats.put("abstencoes", abstencoes);
        stats.put("percentualFavoravel", votacoes.isEmpty() ? 0.0 : (double) favoraveis / votacoes.size());
        stats.put("percentualContrario", votacoes.isEmpty() ? 0.0 : (double) contrarios / votacoes.size());
        stats.put("percentualAbstencao", votacoes.isEmpty() ? 0.0 : (double) abstencoes / votacoes.size());

        return stats;
    }

    private AnaliseParlamentar construirAnalise(
            Parlamentar parlamentar,
            String tema,
            List<Votacao> votacoes,
            ResultadoAnaliseParlamentarIA resultado,
            Map<String, Object> estatisticas,
            RespostaLlamaDTO resposta,
            long duracaoMs) {

        int total = votacoes.size();
        int favoraveis = (int) votacoes.stream().filter(Votacao::isVotoFavoravel).count();
        int contrarios = (int) votacoes.stream().filter(Votacao::isVotoContrario).count();
        int abstencoes = (int) votacoes.stream().filter(Votacao::isAbstencao).count();

        double coerencia = calcularCoerencia(votacoes, resultado.posicionamento());

        return AnaliseParlamentar.builder()
                .parlamentar(parlamentar)
                .tema(tema)
                .posicionamento(resultado.posicionamento())
                .confiabilidade(resultado.confiabilidade())
                .analiseDetalhada(resultado.analiseDetalhada())
                .tendencia(resultado.tendencia())
                .padroesIdentificados(resultado.padroesIdentificados())
                .estatisticas(estatisticas)
                .totalVotacoes(total)
                .votosAFavor(favoraveis)
                .votosContra(contrarios)
                .abstencoes(abstencoes)
                .percentualCoerencia(coerencia)
                .votacoesChave(resultado.votacoesChave())
                .alinhamentoPolitico(resultado.alinhamentoPolitico())
                .previsaoComportamento(resultado.previsaoComportamento())
                .dataAnalise(LocalDateTime.now())
                .modeloVersao(modeloVersao)
                .promptUtilizado(resposta.getMessage().getContent())
                .respostaCompleta(resposta.getMessage().getContent())
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(true)
                .dataExpiracao(LocalDateTime.now().plusSeconds(cacheTtlSegundos))
                .build();
    }

    private double calcularCoerencia(List<Votacao> votacoes, String posicionamento) {
        if (votacoes.isEmpty()) return 0.0;

        long votosCoerentes;
        if ("PRO".equalsIgnoreCase(posicionamento)) {
            votosCoerentes = votacoes.stream().filter(Votacao::isVotoFavoravel).count();
        } else if ("CONTRA".equalsIgnoreCase(posicionamento)) {
            votosCoerentes = votacoes.stream().filter(Votacao::isVotoContrario).count();
        } else {
            return 0.5;
        }

        return (double) votosCoerentes / votacoes.size();
    }

    private ComportamentoParlamentarDTO construirComportamento(
            Parlamentar parlamentar,
            String tema,
            List<Votacao> votacoes,
            LocalDateTime inicio,
            LocalDateTime fim) {

        int total = votacoes.size();
        int favoraveis = (int) votacoes.stream().filter(Votacao::isVotoFavoravel).count();
        int contrarios = (int) votacoes.stream().filter(Votacao::isVotoContrario).count();
        int abstencoes = (int) votacoes.stream().filter(Votacao::isAbstencao).count();

        double percFavoravel = total > 0 ? (double) favoraveis / total : 0.0;
        double percContrario = total > 0 ? (double) contrarios / total : 0.0;

        String posicionamento;
        if (percFavoravel > 0.7) {
            posicionamento = "PRO";
        } else if (percContrario > 0.7) {
            posicionamento = "CONTRA";
        } else {
            posicionamento = "NEUTRO";
        }

        List<VotacaoResumoDTO> resumos = votacoes.stream()
                .limit(10)
                .map(VotacaoResumoDTO::from)
                .collect(Collectors.toList());

        return ComportamentoParlamentarDTO.builder()
                .parlamentarId(parlamentar.getId())
                .parlamentarNome(parlamentar.getNome())
                .tema(tema)
                .totalVotacoes(total)
                .votosAFavor(favoraveis)
                .votosContra(contrarios)
                .abstencoes(abstencoes)
                .percentualAFavor(percFavoravel)
                .percentualContra(percContrario)
                .percentualAbstencao(total > 0 ? (double) abstencoes / total : 0.0)
                .posicionamento(posicionamento)
                .votacoesRecentes(resumos)
                .periodoInicio(inicio)
                .periodoFim(fim)
                .build();
    }

    private void registrarFalha(
            Parlamentar parlamentar,
            String tema,
            Exception erro,
            long duracaoMs) {

        AnaliseParlamentar analiseFalha = AnaliseParlamentar.builder()
                .parlamentar(parlamentar)
                .tema(tema)
                .dataAnalise(LocalDateTime.now())
                .modeloVersao(modeloVersao)
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(false)
                .mensagemErro(erro.getMessage())
                .dataExpiracao(LocalDateTime.now().plusSeconds(cacheTtlSegundos))
                .build();

        analiseRepository.save(analiseFalha);
    }

    private EstatisticasParlamentarDTO estatisticasVazias(
            LocalDateTime inicio,
            LocalDateTime fim) {

        return EstatisticasParlamentarDTO.builder()
                .totalAnalises(0L)
                .analisesComSucesso(0L)
                .analisesFalhas(0L)
                .taxaSucesso(0.0)
                .distribuicaoPorPosicionamento(new HashMap<>())
                .distribuicaoPorTema(new HashMap<>())
                .periodoInicio(inicio)
                .periodoFim(fim)
                .build();
    }

    private EstatisticasParlamentarDTO calcularEstatisticasDeAnalises(
            List<AnaliseParlamentar> analises,
            LocalDateTime inicio) {

        long total = analises.size();
        long sucesso = analises.stream()
                .filter(a -> a.getSucesso() != null && a.getSucesso())
                .count();
        long falhas = total - sucesso;

        List<AnaliseParlamentar> sucessos = analises.stream()
                .filter(a -> a.getSucesso() != null && a.getSucesso())
                .toList();

        Map<String, Long> distribuicaoPosicionamento = sucessos.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getPosicionamento() != null ? a.getPosicionamento() : "INDEFINIDO",
                        Collectors.counting()
                ));

        Map<String, Long> distribuicaoTema = sucessos.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getTema() != null ? a.getTema() : "INDEFINIDO",
                        Collectors.counting()
                ));

        double confiabilidadeMedia = sucessos.stream()
                .filter(a -> a.getConfiabilidade() != null)
                .mapToDouble(AnaliseParlamentar::getConfiabilidade)
                .average()
                .orElse(0.0);

        long tempoMedio = (long) analises.stream()
                .filter(a -> a.getTempoProcessamentoMs() != null)
                .mapToLong(AnaliseParlamentar::getTempoProcessamentoMs)
                .average()
                .orElse(0.0);

        return EstatisticasParlamentarDTO.builder()
                .totalAnalises(total)
                .analisesComSucesso(sucesso)
                .analisesFalhas(falhas)
                .taxaSucesso(total > 0 ? (double) sucesso / total : 0.0)
                .distribuicaoPorPosicionamento(distribuicaoPosicionamento)
                .distribuicaoPorTema(distribuicaoTema)
                .posicionamentosPro(distribuicaoPosicionamento.getOrDefault("PRO", 0L))
                .posicionamentosContra(distribuicaoPosicionamento.getOrDefault("CONTRA", 0L))
                .posicionamentosNeutros(distribuicaoPosicionamento.getOrDefault("NEUTRO", 0L))
                .confiabilidadeMedia(confiabilidadeMedia)
                .tempoMedioMs(tempoMedio)
                .periodoInicio(inicio)
                .periodoFim(LocalDateTime.now())
                .build();
    }

    private record ResultadoAnaliseParlamentarIA(
            String posicionamento,
            Double confiabilidade,
            String analiseDetalhada,
            String tendencia,
            List<String> padroesIdentificados,
            String alinhamentoPolitico,
            String previsaoComportamento,
            List<String> votacoesChave
    ) {}
}