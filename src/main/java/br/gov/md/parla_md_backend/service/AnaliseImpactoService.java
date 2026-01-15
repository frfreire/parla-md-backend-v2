package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.AreaImpacto;
import br.gov.md.parla_md_backend.domain.dto.*;
import br.gov.md.parla_md_backend.domain.AnaliseImpacto;
import br.gov.md.parla_md_backend.domain.ItemLegislativo;
import br.gov.md.parla_md_backend.exception.AnaliseImpactoException;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IAnaliseImpactoRepository;
import br.gov.md.parla_md_backend.repository.IAreaImpactoRepository;
import br.gov.md.parla_md_backend.repository.IItemLegislativoRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnaliseImpactoService {

    private final LlamaService llamaService;
    private final IAnaliseImpactoRepository analiseRepository;
    private final IAreaImpactoRepository areaRepository;
    private final IItemLegislativoRepository itemLegislativoRepository;

    @Value("${analise.cache.ttl:86400}")
    private int cacheTtlSegundos;

    @Value("${analise.modelo.versao:1.0.0}")
    private String modeloVersao;

    @Transactional
    public List<AnaliseImpactoDTO> analisar(SolicitarAnaliseImpactoDTO request) {
        ItemLegislativo item = buscarItemLegislativo(request.getItemLegislativoId());

        List<AreaImpacto> areasParaAnalisar = determinarAreas(request);

        if (areasParaAnalisar.isEmpty()) {
            throw new AnaliseImpactoException("Nenhuma área de impacto disponível para análise");
        }

        List<AnaliseImpactoDTO> analises = new ArrayList<>();

        for (AreaImpacto area : areasParaAnalisar) {
            try {
                if (!request.isForcarNovaAnalise()) {
                    AnaliseImpacto analiseCache = buscarAnaliseRecente(item, area);
                    if (analiseCache != null) {
                        log.info("Retornando análise do cache: {} - {}",
                                item.getId(), area.getNome());
                        analises.add(AnaliseImpactoDTO.from(analiseCache));
                        continue;
                    }
                }

                AnaliseImpactoDTO analise = gerarNovaAnalise(item, area);
                analises.add(analise);

            } catch (Exception e) {
                log.error("Erro ao analisar impacto na área {}: {}",
                        area.getNome(), e.getMessage());
            }
        }

        return analises;
    }

    @Cacheable(value = "analises-impacto", key = "#itemId + '-' + #areaId")
    public AnaliseImpactoDTO buscarAnalisePorItemEArea(String itemId, String areaId) {

        AnaliseImpacto analise = analiseRepository
                .findByItemLegislativo_IdAndAreaImpacto_Id(itemId, areaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Análise não encontrada para item " + itemId + " e área " + areaId));

        return AnaliseImpactoDTO.from(analise);
    }

    public List<AnaliseImpactoDTO> buscarAnalisesPorItem(String itemId) {

        return analiseRepository.findAllByItemLegislativo_Id(itemId, PageRequest.of(0, 20) ).stream()
                .map(AnaliseImpactoDTO::from)
                .collect(Collectors.toList());
    }

    public Page<AnaliseImpactoDTO> buscarAnalisesRecentes(Pageable pageable) {
        LocalDateTime limite = LocalDateTime.now().minusDays(30);

        return analiseRepository.findByDataAnaliseAfter(limite, pageable)
                .map(AnaliseImpactoDTO::from);
    }

    public EstatisticasImpactoDTO calcularEstatisticas(int dias) {
        LocalDateTime inicio = LocalDateTime.now().minusDays(dias);
        List<AnaliseImpacto> analises = analiseRepository.findByDataAnaliseAfter(inicio);

        if (analises.isEmpty()) {
            return estatisticasVazias(inicio, LocalDateTime.now());
        }

        return calcularEstatisticasDeAnalises(analises, inicio);
    }

    @Transactional
    @CacheEvict(value = "analises-impacto", allEntries = true)
    public void limparExpiradas() {
        LocalDateTime agora = LocalDateTime.now();
        List<AnaliseImpacto> expiradas = analiseRepository.buscarExpiradas(agora);

        if (!expiradas.isEmpty()) {
            analiseRepository.deleteAll(expiradas);
            log.info("Removidas {} análises expiradas", expiradas.size());
        }
    }

    @Transactional
    public AreaImpactoDTO criarAreaImpacto(AreaImpactoDTO dto) {
        if (areaRepository.existsByNome(dto.getNome())) {
            throw new AnaliseImpactoException("Já existe uma área com este nome: " + dto.getNome());
        }

        AreaImpacto area = dto.toEntity();
        area.setDataCriacao(LocalDateTime.now());
        area.setDataUltimaAtualizacao(LocalDateTime.now());

        AreaImpacto salva = areaRepository.save(area);

        log.info("Área de impacto criada: {}", salva.getNome());

        return AreaImpactoDTO.from(salva);
    }

    @Transactional
    public AreaImpactoDTO atualizarAreaImpacto(String id, AreaImpactoDTO dto) {
        AreaImpacto area = areaRepository.findById(id)
                .orElseThrow(() -> AnaliseImpactoException.areaNaoEncontrada(id));

        area.setNome(dto.getNome());
        area.setDescricao(dto.getDescricao());
        area.setKeywords(dto.getKeywords());
        area.setGruposAfetados(dto.getGruposAfetados());
        area.setCategoria(dto.getCategoria());
        area.setAtiva(dto.getAtiva());
        area.setOrdem(dto.getOrdem());
        area.setDataUltimaAtualizacao(LocalDateTime.now());

        AreaImpacto atualizada = areaRepository.save(area);

        log.info("Área de impacto atualizada: {}", atualizada.getNome());

        return AreaImpactoDTO.from(atualizada);
    }

    @Transactional
    public void deletarAreaImpacto(String id) {
        AreaImpacto area = areaRepository.findById(id)
                .orElseThrow(() -> AnaliseImpactoException.areaNaoEncontrada(id));

        areaRepository.delete(area);

        log.info("Área de impacto deletada: {}", area.getNome());
    }

    public List<AreaImpactoDTO> listarTodasAreas() {
        return areaRepository.findAll().stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    public List<AreaImpactoDTO> listarAreasAtivas() {
        return areaRepository.findByAtiva(true).stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    public AreaImpactoDTO buscarAreaPorId(String id) {
        AreaImpacto area = buscarAreaImpacto(id);
        return AreaImpactoDTO.from(area);
    }

    private ItemLegislativo buscarItemLegislativo(String itemId) {
        return itemLegislativoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Item legislativo não encontrado: " + itemId));
    }

    private AreaImpacto buscarAreaImpacto(String areaId) {
        return areaRepository.findById(areaId)
                .orElseThrow(() -> AnaliseImpactoException.areaNaoEncontrada(areaId));
    }

    private List<AreaImpacto> determinarAreas(SolicitarAnaliseImpactoDTO request) {
        if (request.isAnalisarTodasAreas()) {
            return areaRepository.findByAtiva(true);
        }

        if (request.getAreaIds() != null && !request.getAreaIds().isEmpty()) {
            return request.getAreaIds().stream()
                    .map(this::buscarAreaImpacto)
                    .collect(Collectors.toList());
        }

        return areaRepository.findByAtiva(true);
    }

    private AnaliseImpacto buscarAnaliseRecente(ItemLegislativo item, AreaImpacto area) {
        LocalDateTime limite = LocalDateTime.now().minusSeconds(cacheTtlSegundos);

        return analiseRepository
                .findByItemLegislativo_IdAndAreaImpacto_Id(item.getId(), area.getId())
                .filter(a -> a.getDataAnalise().isAfter(limite))
                .filter(a -> a.getSucesso() != null && a.getSucesso())
                .orElse(null);
    }

    private AnaliseImpactoDTO gerarNovaAnalise(ItemLegislativo item, AreaImpacto area) {
        long inicioMs = System.currentTimeMillis();

        try {
            String prompt = construirPrompt(item, area);
            String promptSistema = construirPromptSistema();

            RespostaLlamaDTO resposta = llamaService.enviarRequisicao(
                    prompt,
                    promptSistema,
                    true
            );

            ResultadoAnaliseImpactoIA resultado = parsearResposta(resposta);

            long duracaoMs = System.currentTimeMillis() - inicioMs;

            AnaliseImpacto analise = construirAnalise(
                    item,
                    area,
                    resultado,
                    resposta,
                    duracaoMs
            );

            AnaliseImpacto salva = analiseRepository.save(analise);

            log.info("Análise gerada: {} - {} - Nível: {}",
                    item.getId(),
                    area.getNome(),
                    salva.getNivelImpacto());

            return AnaliseImpactoDTO.from(salva);

        } catch (Exception e) {
            long duracaoMs = System.currentTimeMillis() - inicioMs;
            registrarFalha(item, area, e, duracaoMs);

            log.error("Erro ao gerar análise: {}", e.getMessage(), e);
            throw AnaliseImpactoException.erroProcessamento(e.getMessage(), e);
        }
    }

    private String construirPrompt(ItemLegislativo item, AreaImpacto area) {
        return String.format("""
            Analise o impacto desta proposição legislativa na área de %s:
            
            PROPOSIÇÃO:
            - Tipo: %s
            - Ementa: %s
            - Tema: %s
            
            ÁREA DE IMPACTO: %s
            - Descrição: %s
            - Grupos potencialmente afetados: %s
            
            TAREFA:
            Responda APENAS com um JSON no formato:
            
            {
              "nivelImpacto": "<ALTO|MEDIO|BAIXO|NENHUM>",
              "tipoImpacto": "<POSITIVO|NEGATIVO|MISTO|NEUTRO>",
              "percentualImpacto": <número entre 0.0 e 1.0>,
              "analiseDetalhada": "<análise em 3-5 frases>",
              "consequencias": [
                "<consequência 1>",
                "<consequência 2>",
                "<consequência 3>"
              ],
              "gruposAfetados": [
                "<grupo 1>",
                "<grupo 2>"
              ],
              "riscos": [
                "<risco 1>",
                "<risco 2>"
              ],
              "oportunidades": [
                "<oportunidade 1>",
                "<oportunidade 2>"
              ],
              "recomendacoes": "<recomendações em 2-3 frases>"
            }
            """,
                area.getNome(),
                item.getTipo(),
                item.getEmenta(),
                item.getTema(),
                area.getNome(),
                area.getDescricao() != null ? area.getDescricao() : "Não especificada",
                area.getGruposAfetados() != null ?
                        String.join(", ", area.getGruposAfetados()) : "Não especificados"
        );
    }

    private String construirPromptSistema() {
        return """
            Você é um especialista em análise de impacto legislativo brasileiro.
            Sua tarefa é avaliar como proposições legislativas afetam diferentes áreas.
            
            Diretrizes:
            - Seja preciso e técnico
            - Considere impactos diretos e indiretos
            - Avalie curto, médio e longo prazo
            - Identifique grupos específicos afetados
            - Aponte riscos e oportunidades
            - Seja objetivo nas recomendações
            
            Sempre responda em formato JSON válido.
            """;
    }

    private ResultadoAnaliseImpactoIA parsearResposta(RespostaLlamaDTO resposta) {
        try {
            return llamaService.extrairJson(resposta, ResultadoAnaliseImpactoIA.class);
        } catch (Exception e) {
            log.error("Erro ao parsear resposta do Llama: {}", e.getMessage());
            throw AnaliseImpactoException.erroProcessamento("Resposta em formato inválido", e);
        }
    }

    private AnaliseImpacto construirAnalise(
            ItemLegislativo item,
            AreaImpacto area,
            ResultadoAnaliseImpactoIA resultado,
            RespostaLlamaDTO resposta,
            long duracaoMs) {

        return AnaliseImpacto.builder()
                .itemLegislativo(item)
                .areaImpacto(area)
                .nivelImpacto(resultado.nivelImpacto())
                .tipoImpacto(resultado.tipoImpacto())
                .percentualImpacto(resultado.percentualImpacto())
                .analiseDetalhada(resultado.analiseDetalhada())
                .consequencias(resultado.consequencias())
                .gruposAfetados(resultado.gruposAfetados())
                .riscos(resultado.riscos())
                .oportunidades(resultado.oportunidades())
                .recomendacoes(resultado.recomendacoes())
                .dataAnalise(LocalDateTime.now())
                .modeloVersao(modeloVersao)
                .promptUtilizado(resposta.getMessage().getContent())
                .respostaCompleta(resposta.getMessage().getContent())
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(true)
                .dataExpiracao(LocalDateTime.now().plusSeconds(cacheTtlSegundos))
                .build();
    }

    private void registrarFalha(
            ItemLegislativo item,
            AreaImpacto area,
            Exception erro,
            long duracaoMs) {

        AnaliseImpacto analiseFalha = AnaliseImpacto.builder()
                .itemLegislativo(item)
                .areaImpacto(area)
                .dataAnalise(LocalDateTime.now())
                .modeloVersao(modeloVersao)
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(false)
                .mensagemErro(erro.getMessage())
                .dataExpiracao(LocalDateTime.now().plusSeconds(cacheTtlSegundos))
                .build();

        analiseRepository.save(analiseFalha);
    }

    private EstatisticasImpactoDTO estatisticasVazias(
            LocalDateTime inicio,
            LocalDateTime fim) {

        return EstatisticasImpactoDTO.builder()
                .totalAnalises(0L)
                .analisesComSucesso(0L)
                .analisesFalhas(0L)
                .taxaSucesso(0.0)
                .distribuicaoPorNivel(new HashMap<>())
                .distribuicaoPorTipo(new HashMap<>())
                .distribuicaoPorArea(new HashMap<>())
                .periodoInicio(inicio)
                .periodoFim(fim)
                .build();
    }

    private EstatisticasImpactoDTO calcularEstatisticasDeAnalises(
            List<AnaliseImpacto> analises,
            LocalDateTime inicio) {

        long total = analises.size();
        long sucesso = analises.stream()
                .filter(a -> a.getSucesso() != null && a.getSucesso())
                .count();
        long falhas = total - sucesso;

        List<AnaliseImpacto> sucessos = analises.stream()
                .filter(a -> a.getSucesso() != null && a.getSucesso())
                .toList();

        Map<String, Long> distribuicaoNivel = sucessos.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getNivelImpacto() != null ? a.getNivelImpacto() : "INDEFINIDO",
                        Collectors.counting()
                ));

        Map<String, Long> distribuicaoTipo = sucessos.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getTipoImpacto() != null ? a.getTipoImpacto() : "INDEFINIDO",
                        Collectors.counting()
                ));

        Map<String, Long> distribuicaoArea = sucessos.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAreaImpacto() != null ?
                                a.getAreaImpacto().getNome() : "INDEFINIDO",
                        Collectors.counting()
                ));

        return EstatisticasImpactoDTO.builder()
                .totalAnalises(total)
                .analisesComSucesso(sucesso)
                .analisesFalhas(falhas)
                .taxaSucesso(total > 0 ? (double) sucesso / total : 0.0)
                .distribuicaoPorNivel(distribuicaoNivel)
                .distribuicaoPorTipo(distribuicaoTipo)
                .distribuicaoPorArea(distribuicaoArea)
                .impactosAltos(distribuicaoNivel.getOrDefault("ALTO", 0L))
                .impactosMedios(distribuicaoNivel.getOrDefault("MEDIO", 0L))
                .impactosBaixos(distribuicaoNivel.getOrDefault("BAIXO", 0L))
                .impactosNegativos(distribuicaoTipo.getOrDefault("NEGATIVO", 0L))
                .impactosPositivos(distribuicaoTipo.getOrDefault("POSITIVO", 0L))
                .percentualImpactoMedio(calcularMediaPercentual(sucessos))
                .tempoMedioMs(calcularMediaTempo(analises))
                .periodoInicio(inicio)
                .periodoFim(LocalDateTime.now())
                .build();
    }

    private Double calcularMediaPercentual(List<AnaliseImpacto> analises) {
        return analises.stream()
                .filter(a -> a.getPercentualImpacto() != null)
                .mapToDouble(AnaliseImpacto::getPercentualImpacto)
                .average()
                .orElse(0.0);
    }

    private Long calcularMediaTempo(List<AnaliseImpacto> analises) {
        return (long) analises.stream()
                .filter(a -> a.getTempoProcessamentoMs() != null)
                .mapToLong(AnaliseImpacto::getTempoProcessamentoMs)
                .average()
                .orElse(0.0);
    }

    private record ResultadoAnaliseImpactoIA(
            String nivelImpacto,
            String tipoImpacto,
            Double percentualImpacto,
            String analiseDetalhada,
            List<String> consequencias,
            List<String> gruposAfetados,
            List<String> riscos,
            List<String> oportunidades,
            String recomendacoes
    ) {}
}