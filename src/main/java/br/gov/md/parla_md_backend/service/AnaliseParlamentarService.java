package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.AnaliseParlamentar;
import br.gov.md.parla_md_backend.domain.Parlamentar;
import br.gov.md.parla_md_backend.domain.Votacao;
import br.gov.md.parla_md_backend.domain.dto.*;
import br.gov.md.parla_md_backend.exception.AnaliseParlamentarException;
import br.gov.md.parla_md_backend.repository.IAnaliseParlamentarRepository;
import br.gov.md.parla_md_backend.repository.IParlamentarRepository;
import br.gov.md.parla_md_backend.repository.IVotacaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnaliseParlamentarService extends BaseIAService<AnaliseParlamentar, AnaliseParlamentarDTO, ResultadoAnaliseParlamentarIA, IAnaliseParlamentarRepository> {

    private final IParlamentarRepository parlamentarRepository;
    private final IVotacaoRepository votacaoRepository;

    @Value("${analise.parlamentar.minimo.votacoes:5}")
    private int minimoVotacoes;

    public AnaliseParlamentarService(
            LlamaService llamaService,
            IAnaliseParlamentarRepository analiseRepository,
            IParlamentarRepository parlamentarRepository,
            IVotacaoRepository votacaoRepository) {
        super(llamaService, analiseRepository);
        this.parlamentarRepository = parlamentarRepository;
        this.votacaoRepository = votacaoRepository;
    }

    @Override
    protected String getNomeAnalise() {
        return "Análise Parlamentar";
    }

    @Override
    protected String getNomeCacheEvict() {
        return "analises-parlamentares";
    }

    @Override
    protected Class<ResultadoAnaliseParlamentarIA> getResultadoClass() {
        return ResultadoAnaliseParlamentarIA.class;
    }

    @Override
    protected AnaliseParlamentarDTO toDTO(AnaliseParlamentar entidade) {
        return AnaliseParlamentarDTO.from(entidade);
    }

    @Override
    protected String construirPrompt(Object... parametros) {
        Parlamentar parlamentar = (Parlamentar) parametros[0];
        String tema = (String) parametros[1];
        @SuppressWarnings("unchecked")
        List<Votacao> votacoes = (List<Votacao>) parametros[2];
        SolicitarAnaliseParlamentarDTO request = (SolicitarAnaliseParlamentarDTO) parametros[3];

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
                    v.getDataHoraInicio() != null ? v.getDataHoraInicio().toLocalDate() : "Data não disponível",
                    v.getDescricao() != null ? v.getDescricao().substring(0, Math.min(100, v.getDescricao().length())) : "Sem descrição",
                    v.getVoto() != null ? v.getVoto() : "Não registrado"
            ));
        }

        prompt.append("\n\nResponda APENAS com um JSON no formato:\n");
        prompt.append("{\n");
        prompt.append("  \"posicionamento\": \"<PRO|CONTRA|NEUTRO>\",\n");
        prompt.append("  \"confiabilidade\": <número entre 0 e 1>,\n");
        prompt.append("  \"analiseDetalhada\": \"<análise em 3-5 frases>\",\n");

        if (request.isIncluirTendencias()) {
            prompt.append("  \"tendencia\": \"<descrição da tendência>\",\n");
            prompt.append("  \"padroesIdentificados\": [\"<padrão 1>\", \"<padrão 2>\"],\n");
        }

        if (request.isIncluirPrevisoes()) {
            prompt.append("  \"previsaoComportamento\": \"<previsão de comportamento futuro>\",\n");
        }

        prompt.append("  \"percentualCoerencia\": <número entre 0 e 1>,\n");
        prompt.append("  \"alinhamentoPolitico\": \"<descrição do alinhamento>\"\n");
        prompt.append("}");

        return prompt.toString();
    }

    @Override
    protected String construirPromptSistema() {
        return """
            Você é um especialista em análise de comportamento parlamentar brasileiro.
            Sua tarefa é analisar padrões de votação e posicionamento político.
            
            Diretrizes:
            - Seja objetivo e imparcial
            - Baseie-se em dados concretos de votação
            - Identifique padrões consistentes
            - Avalie coerência ideológica
            - Considere contexto político
            
            Sempre responda em formato JSON válido.
            """;
    }

    @Override
    protected AnaliseParlamentar construirEntidade(ResultadoAnaliseParlamentarIA resultado, RespostaLlamaDTO resposta, long duracaoMs, Object... parametros) {
        Parlamentar parlamentar = (Parlamentar) parametros[0];
        String tema = (String) parametros[1];
        @SuppressWarnings("unchecked")
        List<Votacao> votacoes = (List<Votacao>) parametros[2];
        LocalDateTime agora = LocalDateTime.now();

        Map<String, Object> estatisticas = calcularEstatisticasVotacoes(votacoes);

        return AnaliseParlamentar.builder()
                .parlamentar(parlamentar)
                .tema(tema)
                .posicionamento(resultado.posicionamento())
                .confiabilidade(resultado.confiabilidade())
                .analiseDetalhada(resultado.analiseDetalhada())
                .tendencia(resultado.tendencia())
                .padroesIdentificados(resultado.padroesIdentificados())
                .estatisticas(estatisticas)
                .totalVotacoes(votacoes.size())
                .votosAFavor((Integer) estatisticas.get("votosAFavor"))
                .votosContra((Integer) estatisticas.get("votosContra"))
                .abstencoes((Integer) estatisticas.get("abstencoes"))
                .percentualCoerencia(resultado.percentualCoerencia())
                .votacoesChave(extrairVotacoesChave(votacoes))
                .alinhamentoPolitico(resultado.alinhamentoPolitico())
                .previsaoComportamento(resultado.previsaoComportamento())
                .dataAnalise(agora)
                .modeloVersao(modeloVersao)
                .promptUtilizado(resposta.getMessage().getContent())
                .respostaCompleta(resposta.getMessage().getContent())
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(true)
                .dataExpiracao(calcularDataExpiracao())
                .build();
    }

    @Override
    protected AnaliseParlamentar construirEntidadeFalha(Exception erro, long duracaoMs, Object... parametros) {
        Parlamentar parlamentar = (Parlamentar) parametros[0];
        String tema = (String) parametros[1];
        LocalDateTime agora = LocalDateTime.now();

        return AnaliseParlamentar.builder()
                .parlamentar(parlamentar)
                .tema(tema)
                .dataAnalise(agora)
                .modeloVersao(modeloVersao)
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(false)
                .mensagemErro(erro.getMessage())
                .dataExpiracao(calcularDataExpiracao())
                .build();
    }

    @Override
    protected Optional<AnaliseParlamentar> buscarCacheRecente(Object... parametros) {
        Parlamentar parlamentar = (Parlamentar) parametros[0];
        String tema = (String) parametros[1];

        return repository.findByParlamentar_IdAndTema(parlamentar.getId(), tema)
                .filter(this::isCacheValido);
    }

    @Transactional
    public AnaliseParlamentarDTO analisar(SolicitarAnaliseParlamentarDTO request) {
        Parlamentar parlamentar = buscarParlamentar(request.getParlamentarId());
        List<Votacao> votacoes = buscarVotacoesPorTema(parlamentar, request.getTema());

        validarVotacoes(votacoes);

        AnaliseParlamentar analise = processarComCache(
                request.isForcarNovaAnalise(),
                parlamentar,
                request.getTema(),
                votacoes,
                request
        );

        return toDTO(analise);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "analises-parlamentares", key = "#parlamentarId + '-' + #tema")
    public AnaliseParlamentarDTO buscarPorParlamentarETema(String parlamentarId, String tema) {
        AnaliseParlamentar analise = repository.findByParlamentar_IdAndTema(parlamentarId, tema)
                .orElseThrow(() -> AnaliseParlamentarException.analiseNaoEncontrada(parlamentarId, tema));

        return toDTO(analise);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseParlamentarDTO> buscarPorParlamentar(String parlamentarId, Pageable pageable) {
        Page<AnaliseParlamentar> analises = repository.findAllByParlamentar_Id(parlamentarId, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseParlamentarDTO> buscarPorTema(String tema, Pageable pageable) {
        Page<AnaliseParlamentar> analises = repository.findAllByTema(tema, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseParlamentarDTO> buscarPorPosicionamento(String posicionamento, Pageable pageable) {
        Page<AnaliseParlamentar> analises = repository.findAllByPosicionamento(posicionamento, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseParlamentarDTO> buscarPorTendencia(String tendencia, Pageable pageable) {
        Page<AnaliseParlamentar> analises = repository.findAllByTendencia(tendencia, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseParlamentarDTO> buscarPorAlinhamento(String alinhamentoPolitico, Pageable pageable) {
        Page<AnaliseParlamentar> analises = repository.findAllByAlinhamentoPolitico(alinhamentoPolitico, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseParlamentarDTO> buscarPorConfiabilidade(Double min, Double max, Pageable pageable) {
        Page<AnaliseParlamentar> analises = repository.findAllByConfiabilidadeBetween(min, max, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseParlamentarDTO> buscarPorConfiabilidadeMinima(Double confiabilidade, Pageable pageable) {
        Page<AnaliseParlamentar> analises = repository.findAllByConfiabilidadeMinima(confiabilidade, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseParlamentarDTO> buscarEntreDatas(LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
        Page<AnaliseParlamentar> analises = repository.findAllByDataAnaliseBetween(inicio, fim, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ComportamentoParlamentarDTO obterComportamento(
            String parlamentarId,
            String tema,
            LocalDateTime inicio,
            LocalDateTime fim) {

        Parlamentar parlamentar = buscarParlamentar(parlamentarId);

        List<Votacao> votacoes;
        if (inicio != null && fim != null) {
            votacoes = votacaoRepository.buscarPorParlamentarEPeriodo(parlamentar.getId(), inicio, fim);
        } else {
            votacoes = votacaoRepository.findByParlamentarId(parlamentar.getId());
        }

        votacoes = filtrarPorTema(votacoes, tema);

        return construirComportamento(parlamentar, tema, votacoes, inicio, fim);
    }

    @Transactional(readOnly = true)
    public EstatisticasParlamentarDTO calcularEstatisticas(int dias) {
        LocalDateTime inicio = LocalDateTime.now().minusDays(dias);
        List<AnaliseParlamentar> analises = repository.findByDataAnaliseAfter(inicio);

        if (analises.isEmpty()) {
            return construirEstatisticasVazias(inicio, LocalDateTime.now());
        }

        return calcularEstatisticasDeAnalises(analises, inicio);
    }

    @Transactional(readOnly = true)
    public long contarPorParlamentar(String parlamentarId) {
        return repository.countByParlamentar_Id(parlamentarId);
    }

    @Transactional(readOnly = true)
    public long contarPorTema(String tema) {
        return repository.countByTema(tema);
    }

    @Transactional(readOnly = true)
    public long contarPorPosicionamento(String posicionamento) {
        return repository.countByPosicionamento(posicionamento);
    }

    @Override
    @Transactional
    @CacheEvict(value = "analises-parlamentares", allEntries = true)
    public void limparExpiradas() {
        super.limparExpiradas();
    }

    private Parlamentar buscarParlamentar(String parlamentarId) {
        return parlamentarRepository.findById(parlamentarId)
                .orElseThrow(() -> AnaliseParlamentarException.parlamentarNaoEncontrado(parlamentarId));
    }

    private List<Votacao> buscarVotacoesPorTema(Parlamentar parlamentar, String tema) {
        List<Votacao> todasVotacoes = votacaoRepository.findByParlamentarId(parlamentar.getId());
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

    private void validarVotacoes(List<Votacao> votacoes) {
        if (votacoes.size() < minimoVotacoes) {
            throw AnaliseParlamentarException.votacoesInsuficientes(minimoVotacoes, votacoes.size());
        }
    }

    private Map<String, Object> calcularEstatisticasVotacoes(List<Votacao> votacoes) {
        Map<String, Object> stats = new HashMap<>();

        int favor = 0;
        int contra = 0;
        int abstencoes = 0;

        for (Votacao v : votacoes) {
            if (v.getVoto() == null) continue;

            String voto = v.getVoto().toUpperCase();
            if (voto.contains("SIM") || voto.contains("FAVOR")) {
                favor++;
            } else if (voto.contains("NAO") || voto.contains("NÃO") || voto.contains("CONTRA")) {
                contra++;
            } else {
                abstencoes++;
            }
        }

        stats.put("votosAFavor", favor);
        stats.put("votosContra", contra);
        stats.put("abstencoes", abstencoes);
        stats.put("totalVotacoes", votacoes.size());

        return stats;
    }

    private List<String> extrairVotacoesChave(List<Votacao> votacoes) {
        return votacoes.stream()
                .limit(5)
                .map(v -> v.getDescricao() != null ?
                        v.getDescricao().substring(0, Math.min(100, v.getDescricao().length())) :
                        "Votação sem descrição")
                .collect(Collectors.toList());
    }

    private ComportamentoParlamentarDTO construirComportamento(
            Parlamentar parlamentar,
            String tema,
            List<Votacao> votacoes,
            LocalDateTime inicio,
            LocalDateTime fim) {

        Map<String, Object> stats = calcularEstatisticasVotacoes(votacoes);

        return ComportamentoParlamentarDTO.builder()
                .parlamentarId(parlamentar.getId())
                .parlamentarNome(parlamentar.getNome())
                .tema(tema)
                .totalVotacoes(votacoes.size())
                .votosAFavor((Integer) stats.get("votosAFavor"))
                .votosContra((Integer) stats.get("votosContra"))
                .abstencoes((Integer) stats.get("abstencoes"))
                .periodoInicio(inicio)
                .periodoFim(fim)
                .build();
    }

    private EstatisticasParlamentarDTO construirEstatisticasVazias(LocalDateTime inicio, LocalDateTime fim) {
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
                .filter(a -> Boolean.TRUE.equals(a.getSucesso()))
                .count();

        List<AnaliseParlamentar> sucessos = analises.stream()
                .filter(a -> Boolean.TRUE.equals(a.getSucesso()))
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

        return EstatisticasParlamentarDTO.builder()
                .totalAnalises(total)
                .analisesComSucesso(sucesso)
                .analisesFalhas(total - sucesso)
                .taxaSucesso(total > 0 ? (double) sucesso / total : 0.0)
                .distribuicaoPorPosicionamento(distribuicaoPosicionamento)
                .distribuicaoPorTema(distribuicaoTema)
                .confiabilidadeMedia(calcularMediaConfiabilidade(sucessos))
                .tempoMedioMs(calcularMediaTempo(analises))
                .periodoInicio(inicio)
                .periodoFim(LocalDateTime.now())
                .build();
    }

    private Double calcularMediaConfiabilidade(List<AnaliseParlamentar> analises) {
        return analises.stream()
                .filter(a -> a.getConfiabilidade() != null)
                .mapToDouble(AnaliseParlamentar::getConfiabilidade)
                .average()
                .orElse(0.0);
    }

    private Long calcularMediaTempo(List<AnaliseParlamentar> analises) {
        return (long) analises.stream()
                .filter(a -> a.getTempoProcessamentoMs() != null)
                .mapToLong(AnaliseParlamentar::getTempoProcessamentoMs)
                .average()
                .orElse(0.0);
    }
}