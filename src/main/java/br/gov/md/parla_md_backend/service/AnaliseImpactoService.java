package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.AnaliseImpacto;
import br.gov.md.parla_md_backend.domain.AreaImpacto;
import br.gov.md.parla_md_backend.domain.ItemLegislativo;
import br.gov.md.parla_md_backend.domain.dto.*;
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
    private final AreaImpactoService areaImpactoService;

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
                AnaliseImpactoDTO analise = processarAnaliseParaArea(item, area, request.isForcarNovaAnalise());
                analises.add(analise);
            } catch (Exception e) {
                log.error("Erro ao analisar impacto na área {}: {}", area.getNome(), e.getMessage());
            }
        }

        return analises;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "analises-impacto", key = "#itemId + '-' + #areaId")
    public AnaliseImpactoDTO buscarPorItemEArea(String itemId, String areaId) {
        AnaliseImpacto analise = analiseRepository
                .findByItemLegislativo_IdAndAreaImpacto_Id(itemId, areaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Análise não encontrada para item " + itemId + " e área " + areaId));

        return AnaliseImpactoDTO.from(analise);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarPorItem(String itemId, Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository.findAllByItemLegislativo_Id(itemId, pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarPorArea(String areaId, Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository.findAllByAreaImpacto_Id(areaId, pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarPorNivelImpacto(String nivelImpacto, Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository.findAllByNivelImpacto(nivelImpacto, pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarAltoImpacto(Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository.findAllByNivelImpactoAlto(pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarMedioImpacto(Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository.findAllByNivelImpactoMedio(pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarBaixoImpacto(Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository.findAllByNivelImpactoBaixo(pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarPorTipoImpacto(String tipoImpacto, Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository.findAllByTipoImpacto(tipoImpacto, pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarImpactoNegativo(Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository.findAllByTipoImpactoNegativo(pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarImpactoPositivo(Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository.findAllByTipoImpactoPositivo(pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarPorNivelETipo(String nivelImpacto, String tipoImpacto, Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository
                .findAllByNivelImpactoAndTipoImpacto(nivelImpacto, tipoImpacto, pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarAnalisesCriticas(Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository.findAnalisesCriticas(pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarAltoImpactoNegativo(Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository.findAllByAltoImpactoNegativo(pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public List<AnaliseImpactoDTO> buscarPorPercentualMinimo(Double percentualMinimo) {
        List<AnaliseImpacto> analises = analiseRepository
                .findByPercentualImpactoGreaterThanEqual(percentualMinimo);
        return analises.stream()
                .map(AnaliseImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarPorPercentualEntre(Double min, Double max, Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository
                .findAllByPercentualImpactoBetween(min, max, pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarBemSucedidas(Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository.findAllBySucessoTrue(pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarFalhas(Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository.findAllBySucessoFalse(pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarBemSucebidasPorArea(String areaId, Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository
                .findAllByAreaImpacto_IdAndSucessoTrue(areaId, pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarPorModeloVersao(String modeloVersao, Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository.findAllByModeloVersao(modeloVersao, pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarRecentes(Pageable pageable) {
        LocalDateTime limite = LocalDateTime.now().minusDays(30);
        Page<AnaliseImpacto> analises = analiseRepository.findByDataAnaliseAfter(limite, pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarRecentesBemSucedidas(Pageable pageable) {
        LocalDateTime limite = LocalDateTime.now().minusDays(30);
        Page<AnaliseImpacto> analises = analiseRepository.findAnalisesBemSucedsRecentes(limite, pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarAltoImpactoRecentes(Pageable pageable) {
        LocalDateTime limite = LocalDateTime.now().minusDays(30);
        Page<AnaliseImpacto> analises = analiseRepository.findAnaliseAltoImpactoRecentes(limite, pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
        Page<AnaliseImpacto> analises = analiseRepository
                .findAllByDataAnaliseBetween(inicio, fim, pageable);
        return analises.map(AnaliseImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public List<AnaliseImpactoDTO> buscarPorAreaNoPeriodo(String areaId, LocalDateTime inicio, LocalDateTime fim) {
        List<AnaliseImpacto> analises = analiseRepository.findByAreaNoPeriodo(areaId, inicio, fim);
        return analises.stream()
                .map(AnaliseImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EstatisticasImpactoDTO calcularEstatisticas(int dias) {
        LocalDateTime inicio = LocalDateTime.now().minusDays(dias);
        List<AnaliseImpacto> analises = analiseRepository.findByDataAnaliseAfter(inicio);

        if (analises.isEmpty()) {
            return construirEstatisticasVazias(inicio, LocalDateTime.now());
        }

        return calcularEstatisticasDeAnalises(analises, inicio);
    }

    @Transactional
    @CacheEvict(value = "analises-impacto", allEntries = true)
    public void limparExpiradas() {
        LocalDateTime agora = LocalDateTime.now();
        List<AnaliseImpacto> expiradas = analiseRepository.findByDataExpiracaoBefore(agora);

        if (!expiradas.isEmpty()) {
            analiseRepository.deleteAll(expiradas);
            log.info("Removidas {} análises expiradas", expiradas.size());
        }
    }

    @Transactional(readOnly = true)
    public long contarPorNivel(String nivelImpacto) {
        return analiseRepository.countByNivelImpacto(nivelImpacto);
    }

    @Transactional(readOnly = true)
    public long contarPorTipo(String tipoImpacto) {
        return analiseRepository.countByTipoImpacto(tipoImpacto);
    }

    @Transactional(readOnly = true)
    public long contarBemSucedidas() {
        return analiseRepository.countBySucessoTrue();
    }

    @Transactional(readOnly = true)
    public long contarFalhas() {
        return analiseRepository.countBySucessoFalse();
    }

    @Transactional(readOnly = true)
    public long contarPorArea(String areaId) {
        return analiseRepository.countByAreaImpacto_Id(areaId);
    }

    @Transactional(readOnly = true)
    public long contarPorItem(String itemId) {
        return analiseRepository.countByItemLegislativo_Id(itemId);
    }

    @Transactional(readOnly = true)
    public long contarPorNivelETipo(String nivelImpacto, String tipoImpacto) {
        return analiseRepository.countByNivelImpactoAndTipoImpacto(nivelImpacto, tipoImpacto);
    }

    @Transactional(readOnly = true)
    public boolean existeAnalisePara(String itemId, String areaId) {
        return analiseRepository.existsByItemLegislativo_IdAndAreaImpacto_Id(itemId, areaId);
    }

    @Transactional(readOnly = true)
    public boolean existeAnaliseBemSucedidaPara(String itemId, String areaId) {
        return analiseRepository.existsByItemLegislativo_IdAndAreaImpacto_IdAndSucessoTrue(itemId, areaId);
    }

    private AnaliseImpactoDTO processarAnaliseParaArea(ItemLegislativo item, AreaImpacto area, boolean forcarNova) {
        if (!forcarNova) {
            AnaliseImpacto analiseCache = buscarAnaliseRecente(item, area);
            if (analiseCache != null) {
                log.info("Retornando análise do cache: {} - {}", item.getId(), area.getNome());
                return AnaliseImpactoDTO.from(analiseCache);
            }
        }

        return gerarNovaAnalise(item, area);
    }

    private AnaliseImpacto buscarAnaliseRecente(ItemLegislativo item, AreaImpacto area) {
        LocalDateTime limite = LocalDateTime.now().minusSeconds(cacheTtlSegundos);

        return analiseRepository
                .findByItemLegislativo_IdAndAreaImpacto_Id(item.getId(), area.getId())
                .filter(a -> a.getDataAnalise().isAfter(limite))
                .filter(a -> Boolean.TRUE.equals(a.getSucesso()))
                .orElse(null);
    }

    private AnaliseImpactoDTO gerarNovaAnalise(ItemLegislativo item, AreaImpacto area) {
        long inicioMs = System.currentTimeMillis();

        try {
            String prompt = construirPrompt(item, area);
            String promptSistema = construirPromptSistema();

            RespostaLlamaDTO resposta = llamaService.enviarRequisicao(prompt, promptSistema, true);
            ResultadoAnaliseImpactoIA resultado = parsearResposta(resposta);

            long duracaoMs = System.currentTimeMillis() - inicioMs;

            AnaliseImpacto analise = construirAnalise(item, area, resultado, resposta, duracaoMs);
            analise = analiseRepository.save(analise);

            log.info("Análise gerada: {} - {} - Nível: {}", item.getId(), area.getNome(), analise.getNivelImpacto());

            return AnaliseImpactoDTO.from(analise);

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
              "consequencias": ["<consequência 1>", "<consequência 2>", "<consequência 3>"],
              "gruposAfetados": ["<grupo 1>", "<grupo 2>"],
              "riscos": ["<risco 1>", "<risco 2>"],
              "oportunidades": ["<oportunidade 1>", "<oportunidade 2>"],
              "recomendacoes": "<recomendações em 2-3 frases>"
            }
            """,
                area.getNome(),
                item.getTipo(),
                item.getEmenta(),
                item.getTema(),
                area.getNome(),
                area.getDescricao() != null ? area.getDescricao() : "Não especificada",
                area.getGruposAfetados() != null ? String.join(", ", area.getGruposAfetados()) : "Não especificados"
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

    private AnaliseImpacto construirAnalise(ItemLegislativo item, AreaImpacto area,
                                            ResultadoAnaliseImpactoIA resultado,
                                            RespostaLlamaDTO resposta, long duracaoMs) {
        LocalDateTime agora = LocalDateTime.now();

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
                .dataAnalise(agora)
                .modeloVersao(modeloVersao)
                .promptUtilizado(resposta.getMessage().getContent())
                .respostaCompleta(resposta.getMessage().getContent())
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(true)
                .dataExpiracao(agora.plusSeconds(cacheTtlSegundos))
                .build();
    }

    private void registrarFalha(ItemLegislativo item, AreaImpacto area, Exception erro, long duracaoMs) {
        LocalDateTime agora = LocalDateTime.now();

        AnaliseImpacto analiseFalha = AnaliseImpacto.builder()
                .itemLegislativo(item)
                .areaImpacto(area)
                .dataAnalise(agora)
                .modeloVersao(modeloVersao)
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(false)
                .mensagemErro(erro.getMessage())
                .dataExpiracao(agora.plusSeconds(cacheTtlSegundos))
                .build();

        analiseRepository.save(analiseFalha);
    }

    private ItemLegislativo buscarItemLegislativo(String itemId) {
        return itemLegislativoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Item legislativo não encontrado: " + itemId));
    }

    private List<AreaImpacto> determinarAreas(SolicitarAnaliseImpactoDTO request) {
        if (request.isAnalisarTodasAreas()) {
            return areaRepository.findByAtivaTrue();
        }

        if (request.getAreaIds() != null && !request.getAreaIds().isEmpty()) {
            return request.getAreaIds().stream()
                    .map(id -> areaRepository.findById(id)
                            .orElseThrow(() -> AnaliseImpactoException.areaNaoEncontrada(id)))
                    .collect(Collectors.toList());
        }

        return areaRepository.findByAtivaTrue();
    }

    private EstatisticasImpactoDTO construirEstatisticasVazias(LocalDateTime inicio, LocalDateTime fim) {
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

    private EstatisticasImpactoDTO calcularEstatisticasDeAnalises(List<AnaliseImpacto> analises, LocalDateTime inicio) {
        long total = analises.size();
        long sucesso = analises.stream()
                .filter(a -> Boolean.TRUE.equals(a.getSucesso()))
                .count();
        long falhas = total - sucesso;

        List<AnaliseImpacto> sucessos = analises.stream()
                .filter(a -> Boolean.TRUE.equals(a.getSucesso()))
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
                        a -> a.getAreaImpacto() != null ? a.getAreaImpacto().getNome() : "INDEFINIDO",
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