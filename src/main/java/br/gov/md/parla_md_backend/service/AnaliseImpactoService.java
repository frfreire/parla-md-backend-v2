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
import lombok.extern.slf4j.Slf4j;
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
public class AnaliseImpactoService extends BaseIAService<AnaliseImpacto, AnaliseImpactoDTO, ResultadoAnaliseImpactoIA, IAnaliseImpactoRepository> {

    private final IAreaImpactoRepository areaRepository;
    private final IItemLegislativoRepository itemLegislativoRepository;

    public AnaliseImpactoService(
            LlamaService llamaService,
            IAnaliseImpactoRepository analiseRepository,
            IAreaImpactoRepository areaRepository,
            IItemLegislativoRepository itemLegislativoRepository) {
        super(llamaService, analiseRepository);
        this.areaRepository = areaRepository;
        this.itemLegislativoRepository = itemLegislativoRepository;
    }

    @Override
    protected String getNomeAnalise() {
        return "Análise de Impacto";
    }

    @Override
    protected String getNomeCacheEvict() {
        return "analises-impacto";
    }

    @Override
    protected Class<ResultadoAnaliseImpactoIA> getResultadoClass() {
        return ResultadoAnaliseImpactoIA.class;
    }

    @Override
    protected AnaliseImpactoDTO toDTO(AnaliseImpacto entidade) {
        return AnaliseImpactoDTO.from(entidade);
    }

    @Override
    protected String construirPrompt(Object... parametros) {
        ItemLegislativo item = (ItemLegislativo) parametros[0];
        AreaImpacto area = (AreaImpacto) parametros[1];

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

    @Override
    protected String construirPromptSistema() {
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

    @Override
    protected AnaliseImpacto construirEntidade(ResultadoAnaliseImpactoIA resultado, RespostaLlamaDTO resposta, long duracaoMs, Object... parametros) {
        ItemLegislativo item = (ItemLegislativo) parametros[0];
        AreaImpacto area = (AreaImpacto) parametros[1];
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
                .dataExpiracao(calcularDataExpiracao())
                .build();
    }

    @Override
    protected AnaliseImpacto construirEntidadeFalha(Exception erro, long duracaoMs, Object... parametros) {
        ItemLegislativo item = (ItemLegislativo) parametros[0];
        AreaImpacto area = (AreaImpacto) parametros[1];
        LocalDateTime agora = LocalDateTime.now();

        return AnaliseImpacto.builder()
                .itemLegislativo(item)
                .areaImpacto(area)
                .dataAnalise(agora)
                .modeloVersao(modeloVersao)
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(false)
                .mensagemErro(erro.getMessage())
                .dataExpiracao(calcularDataExpiracao())
                .build();
    }

    @Override
    protected Optional<AnaliseImpacto> buscarCacheRecente(Object... parametros) {
        ItemLegislativo item = (ItemLegislativo) parametros[0];
        AreaImpacto area = (AreaImpacto) parametros[1];

        return repository.findByItemLegislativo_IdAndAreaImpacto_Id(item.getId(), area.getId())
                .filter(this::isCacheValido);
    }

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
                AnaliseImpacto analise = processarComCache(
                        request.isForcarNovaAnalise(),
                        item,
                        area
                );
                analises.add(toDTO(analise));
            } catch (Exception e) {
                log.error("Erro ao analisar impacto na área {}: {}", area.getNome(), e.getMessage());
            }
        }

        return analises;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "analises-impacto", key = "#itemId + '-' + #areaId")
    public AnaliseImpactoDTO buscarPorItemEArea(String itemId, String areaId) {
        AnaliseImpacto analise = repository.findByItemLegislativo_IdAndAreaImpacto_Id(itemId, areaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Análise não encontrada para item " + itemId + " e área " + areaId));

        return toDTO(analise);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarPorItem(String itemId, Pageable pageable) {
        Page<AnaliseImpacto> analises = repository.findAllByItemLegislativo_Id(itemId, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarPorArea(String areaId, Pageable pageable) {
        Page<AnaliseImpacto> analises = repository.findAllByAreaImpacto_Id(areaId, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarPorNivelImpacto(String nivelImpacto, Pageable pageable) {
        Page<AnaliseImpacto> analises = repository.findAllByNivelImpacto(nivelImpacto, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarAltoImpacto(Pageable pageable) {
        Page<AnaliseImpacto> analises = repository.findAllByNivelImpactoAlto(pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarMedioImpacto(Pageable pageable) {
        Page<AnaliseImpacto> analises = repository.findAllByNivelImpactoMedio(pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarBaixoImpacto(Pageable pageable) {
        Page<AnaliseImpacto> analises = repository.findAllByNivelImpactoBaixo(pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarPorTipoImpacto(String tipoImpacto, Pageable pageable) {
        Page<AnaliseImpacto> analises = repository.findAllByTipoImpacto(tipoImpacto, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarImpactoNegativo(Pageable pageable) {
        Page<AnaliseImpacto> analises = repository.findAllByTipoImpactoNegativo(pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarImpactoPositivo(Pageable pageable) {
        Page<AnaliseImpacto> analises = repository.findAllByTipoImpactoPositivo(pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarPorNivelETipo(String nivelImpacto, String tipoImpacto, Pageable pageable) {
        Page<AnaliseImpacto> analises = repository.findAllByNivelImpactoAndTipoImpacto(nivelImpacto, tipoImpacto, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarAnalisesCriticas(Pageable pageable) {
        Page<AnaliseImpacto> analises = repository.findAnalisesCriticas(pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarAltoImpactoNegativo(Pageable pageable) {
        Page<AnaliseImpacto> analises = repository.findAllByAltoImpactoNegativo(pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<AnaliseImpactoDTO> buscarPorPercentualMinimo(Double percentualMinimo) {
        List<AnaliseImpacto> analises = repository.findByPercentualImpactoGreaterThanEqual(percentualMinimo);
        return analises.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarPorPercentualEntre(Double min, Double max, Pageable pageable) {
        Page<AnaliseImpacto> analises = repository.findAllByPercentualImpactoBetween(min, max, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarBemSucebidasPorArea(String areaId, Pageable pageable) {
        Page<AnaliseImpacto> analises = repository.findAllByAreaImpacto_IdAndSucessoTrue(areaId, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarRecentesBemSucedidas(Pageable pageable) {
        LocalDateTime limite = LocalDateTime.now().minusDays(30);
        Page<AnaliseImpacto> analises = repository.findAnalisesBemSucedsRecentes(limite, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnaliseImpactoDTO> buscarAltoImpactoRecentes(Pageable pageable) {
        LocalDateTime limite = LocalDateTime.now().minusDays(30);
        Page<AnaliseImpacto> analises = repository.findAnaliseAltoImpactoRecentes(limite, pageable);
        return analises.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<AnaliseImpactoDTO> buscarPorAreaNoPeriodo(String areaId, LocalDateTime inicio, LocalDateTime fim) {
        List<AnaliseImpacto> analises = repository.findByAreaNoPeriodo(areaId, inicio, fim);
        return analises.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EstatisticasImpactoDTO calcularEstatisticas(int dias) {
        LocalDateTime inicio = LocalDateTime.now().minusDays(dias);
        List<AnaliseImpacto> analises = repository.findByDataAnaliseAfter(inicio);

        if (analises.isEmpty()) {
            return construirEstatisticasVazias(inicio, LocalDateTime.now());
        }

        return calcularEstatisticasDeAnalises(analises, inicio);
    }

    @Transactional(readOnly = true)
    public long contarPorNivel(String nivelImpacto) {
        return repository.countByNivelImpacto(nivelImpacto);
    }

    @Transactional(readOnly = true)
    public long contarPorTipo(String tipoImpacto) {
        return repository.countByTipoImpacto(tipoImpacto);
    }

    @Transactional(readOnly = true)
    public long contarPorArea(String areaId) {
        return repository.countByAreaImpacto_Id(areaId);
    }

    @Transactional(readOnly = true)
    public long contarPorItem(String itemId) {
        return repository.countByItemLegislativo_Id(itemId);
    }

    @Transactional(readOnly = true)
    public long contarPorNivelETipo(String nivelImpacto, String tipoImpacto) {
        return repository.countByNivelImpactoAndTipoImpacto(nivelImpacto, tipoImpacto);
    }

    @Transactional(readOnly = true)
    public boolean existeAnalisePara(String itemId, String areaId) {
        return repository.existsByItemLegislativo_IdAndAreaImpacto_Id(itemId, areaId);
    }

    @Transactional(readOnly = true)
    public boolean existeAnaliseBemSucedidaPara(String itemId, String areaId) {
        return repository.existsByItemLegislativo_IdAndAreaImpacto_IdAndSucessoTrue(itemId, areaId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "analises-impacto", allEntries = true)
    public void limparExpiradas() {
        super.limparExpiradas();
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

}