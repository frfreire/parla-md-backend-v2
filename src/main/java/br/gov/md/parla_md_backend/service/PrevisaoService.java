package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.ItemLegislativo;
import br.gov.md.parla_md_backend.domain.Previsao;
import br.gov.md.parla_md_backend.domain.dto.PrevisaoDTO;
import br.gov.md.parla_md_backend.domain.dto.RespostaLlamaDTO;
import br.gov.md.parla_md_backend.domain.dto.ResultadoPrevisaoIA;
import br.gov.md.parla_md_backend.domain.dto.SolicitarPrevisaoDTO;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IItemLegislativoRepository;
import br.gov.md.parla_md_backend.repository.IPrevisaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PrevisaoService extends BaseIAService<Previsao, PrevisaoDTO, ResultadoPrevisaoIA, IPrevisaoRepository> {

    private final IItemLegislativoRepository itemLegislativoRepository;

    public PrevisaoService(
            LlamaService llamaService,
            IPrevisaoRepository previsaoRepository,
            IItemLegislativoRepository itemLegislativoRepository) {
        super(llamaService, previsaoRepository);
        this.itemLegislativoRepository = itemLegislativoRepository;
    }

    @Override
    protected String getNomeAnalise() {
        return "Previsão de Aprovação";
    }

    @Override
    protected String getNomeCacheEvict() {
        return "previsoes";
    }

    @Override
    protected Class<ResultadoPrevisaoIA> getResultadoClass() {
        return ResultadoPrevisaoIA.class;
    }

    @Override
    protected PrevisaoDTO toDTO(Previsao entidade) {
        return PrevisaoDTO.from(entidade);
    }

    @Override
    protected String construirPrompt(Object... parametros) {
        ItemLegislativo item = (ItemLegislativo) parametros[0];
        String tipoPrevisao = (String) parametros[1];

        return String.format("""
            Analise a seguinte proposição legislativa e preveja a probabilidade de aprovação:
            
            TIPO: %s
            ANO: %d
            EMENTA: %s
            TEMA: %s
            
            Responda APENAS com um JSON no formato:
            {
                "probabilidade": <número entre 0 e 1>,
                "confianca": <número entre 0 e 1>,
                "justificativa": "<texto explicativo>",
                "fatoresPositivos": "<fatores que favorecem aprovação>",
                "fatoresNegativos": "<fatores que dificultam aprovação>"
            }
            """,
                item.getTipo(),
                item.getAno(),
                item.getEmenta(),
                item.getTema()
        );
    }

    @Override
    protected String construirPromptSistema() {
        return """
            Você é um especialista em análise legislativa do Congresso Nacional Brasileiro.
            Sua tarefa é prever a probabilidade de aprovação de proposições com base em:
            - Tema e conteúdo da proposição
            - Contexto político atual
            - Histórico de proposições similares
            - Complexidade e impacto
            
            Seja preciso, objetivo e baseie-se em análise racional.
            Sempre responda em formato JSON válido.
            """;
    }

    @Override
    protected Previsao construirEntidade(ResultadoPrevisaoIA resultado, RespostaLlamaDTO resposta, long duracaoMs, Object... parametros) {
        ItemLegislativo item = (ItemLegislativo) parametros[0];
        String tipoPrevisao = (String) parametros[1];
        LocalDateTime agora = LocalDateTime.now();

        return Previsao.builder()
                .itemLegislativo(item)
                .tipoPrevisao(tipoPrevisao)
                .probabilidadeAprovacao(resultado.probabilidade())
                .confianca(resultado.confianca())
                .justificativa(resultado.justificativa())
                .fatoresPositivos(resultado.fatoresPositivos())
                .fatoresNegativos(resultado.fatoresNegativos())
                .dataPrevisao(agora)
                .modeloVersao(modeloVersao)
                .promptUtilizado(resposta.getMessage().getContent())
                .respostaCompleta(resposta.getMessage().getContent())
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(true)
                .dataExpiracao(calcularDataExpiracao())
                .build();
    }

    @Override
    protected Previsao construirEntidadeFalha(Exception erro, long duracaoMs, Object... parametros) {
        ItemLegislativo item = (ItemLegislativo) parametros[0];
        String tipoPrevisao = (String) parametros[1];
        LocalDateTime agora = LocalDateTime.now();

        return Previsao.builder()
                .itemLegislativo(item)
                .tipoPrevisao(tipoPrevisao)
                .dataPrevisao(agora)
                .modeloVersao(modeloVersao)
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(false)
                .mensagemErro(erro.getMessage())
                .dataExpiracao(calcularDataExpiracao())
                .build();
    }

    @Override
    protected Optional<Previsao> buscarCacheRecente(Object... parametros) {
        ItemLegislativo item = (ItemLegislativo) parametros[0];

        return repository.findByItemLegislativo(item).stream()
                .filter(this::isCacheValido)
                .findFirst();
    }

    @Transactional
    public PrevisaoDTO prever(SolicitarPrevisaoDTO request) {
        ItemLegislativo item = buscarItemLegislativo(request.getItemLegislativoId());
        String tipoPrevisao = request.getTipoPrevisao() != null ? request.getTipoPrevisao() : "GERAL";

        Previsao previsao = processarComCache(request.isForcarNovaPrevisao(), item, tipoPrevisao);

        return toDTO(previsao);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "previsoes", key = "#itemId")
    public PrevisaoDTO buscarPorItem(String itemId) {
        ItemLegislativo item = buscarItemLegislativo(itemId);

        Previsao previsao = repository.findFirstByItemLegislativoOrderByDataPrevisaoDesc(item)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhuma previsão encontrada para item: " + itemId));

        return toDTO(previsao);
    }

    @Transactional(readOnly = true)
    public Page<PrevisaoDTO> buscarTodasPorItem(String itemId, Pageable pageable) {
        ItemLegislativo item = buscarItemLegislativo(itemId);
        Page<Previsao> previsoes = repository.findByItemLegislativo(item, pageable);
        return previsoes.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<PrevisaoDTO> buscarTodasPorItemLista(String itemId) {
        ItemLegislativo item = buscarItemLegislativo(itemId);
        List<Previsao> previsoes = repository.findByItemLegislativo(item);
        return previsoes.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrevisaoDTO> buscarPorTipo(String tipoPrevisao) {
        List<Previsao> previsoes = repository.findByTipoPrevisao(tipoPrevisao);
        return previsoes.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrevisaoDTO> buscarPorSucesso(Boolean sucesso) {
        List<Previsao> previsoes = repository.findBySucesso(sucesso);
        return previsoes.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrevisaoDTO> buscarAposData(LocalDateTime data) {
        List<Previsao> previsoes = repository.findByDataPrevisaoAfter(data);
        return previsoes.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PrevisaoDTO> buscarAposDataPaginado(LocalDateTime data, Pageable pageable) {
        Page<Previsao> previsoes = repository.findByDataPrevisaoAfter(data, pageable);
        return previsoes.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<PrevisaoDTO> buscarEntreDatas(LocalDateTime inicio, LocalDateTime fim) {
        List<Previsao> previsoes = repository.findByDataPrevisaoBetween(inicio, fim);
        return previsoes.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrevisaoDTO> buscarPorProbabilidadeMinima(Double probabilidadeMinima) {
        List<Previsao> previsoes = repository.buscarComProbabilidadeMinima(probabilidadeMinima);
        return previsoes.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrevisaoDTO> buscarPorFaixaProbabilidade(Double min, Double max) {
        List<Previsao> previsoes = repository.buscarPorFaixaProbabilidade(min, max);
        return previsoes.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrevisaoDTO> buscarPorConfiancaMinima(Double confiancaMinima) {
        List<Previsao> previsoes = repository.buscarComConfiancaMinima(confiancaMinima);
        return previsoes.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long contarPorTipo(String tipoPrevisao) {
        return repository.countByTipoPrevisao(tipoPrevisao);
    }

    @Transactional(readOnly = true)
    public long contarPorSucesso(Boolean sucesso) {
        return repository.countBySucesso(sucesso);
    }

    @Transactional(readOnly = true)
    public long contarAposData(LocalDateTime data) {
        return repository.countByDataPrevisaoAfter(data);
    }

    @Override
    @Transactional
    @CacheEvict(value = "previsoes", allEntries = true)
    public void limparExpiradas() {
        super.limparExpiradas();
    }

    private ItemLegislativo buscarItemLegislativo(String itemId) {
        return itemLegislativoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Item legislativo não encontrado: " + itemId));
    }

}