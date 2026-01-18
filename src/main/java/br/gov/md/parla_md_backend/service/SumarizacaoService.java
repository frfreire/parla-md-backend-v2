package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.ItemLegislativo;
import br.gov.md.parla_md_backend.domain.Sumario;
import br.gov.md.parla_md_backend.domain.dto.RespostaLlamaDTO;
import br.gov.md.parla_md_backend.domain.dto.ResultadoSumarizacaoIA;
import br.gov.md.parla_md_backend.domain.dto.SolicitarSumarioDTO;
import br.gov.md.parla_md_backend.domain.dto.SumarioDTO;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.exception.SumarizacaoException;
import br.gov.md.parla_md_backend.repository.IItemLegislativoRepository;
import br.gov.md.parla_md_backend.repository.ISumarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class SumarizacaoService extends BaseIAService<Sumario, SumarioDTO, ResultadoSumarizacaoIA, ISumarioRepository> {

    private final IItemLegislativoRepository itemLegislativoRepository;

    @Value("${sumario.texto.minimo:100}")
    private int textoMinimoCaracteres;

    public SumarizacaoService(
            LlamaService llamaService,
            ISumarioRepository sumarioRepository,
            IItemLegislativoRepository itemLegislativoRepository) {
        super(llamaService, sumarioRepository);
        this.itemLegislativoRepository = itemLegislativoRepository;
    }

    @Override
    protected String getNomeAnalise() {
        return "Sumarização";
    }

    @Override
    protected String getNomeCacheEvict() {
        return "sumarios";
    }

    @Override
    protected Class<ResultadoSumarizacaoIA> getResultadoClass() {
        return ResultadoSumarizacaoIA.class;
    }

    @Override
    protected SumarioDTO toDTO(Sumario entidade) {
        return SumarioDTO.from(entidade);
    }

    @Override
    protected String construirPrompt(Object... parametros) {
        String texto = (String) parametros[0];
        SolicitarSumarioDTO request = (SolicitarSumarioDTO) parametros[1];

        StringBuilder prompt = new StringBuilder();

        prompt.append("Crie um sumário executivo do seguinte texto legislativo:\n\n");
        prompt.append(texto);
        prompt.append("\n\nResponda APENAS com um JSON no formato:\n");
        prompt.append("{\n");
        prompt.append("  \"sumarioExecutivo\": \"<sumário em 3-5 frases>\",\n");
        prompt.append("  \"pontosPrincipais\": [\"<ponto 1>\", \"<ponto 2>\", \"<ponto 3>\"],\n");

        if (request.isIncluirPalavrasChave()) {
            prompt.append("  \"palavrasChave\": [\"<palavra 1>\", \"<palavra 2>\"],\n");
        }

        if (request.isIncluirEntidades()) {
            prompt.append("  \"entidadesRelevantes\": [\"<entidade 1>\", \"<entidade 2>\"],\n");
        }

        if (request.isIncluirSentimento()) {
            prompt.append("  \"sentimentoGeral\": \"<positivo|neutro|negativo>\",\n");
        }

        prompt.append("  \"temasPrincipais\": \"<tema principal em 1 frase>\",\n");
        prompt.append("  \"impactoEstimado\": \"<impacto esperado em 1 frase>\"\n");
        prompt.append("}");

        return prompt.toString();
    }

    @Override
    protected String construirPromptSistema() {
        return """
            Você é um especialista em sumarização de textos legislativos brasileiros.
            Sua tarefa é criar sumários claros, objetivos e informativos.
            
            Diretrizes:
            - Use linguagem acessível mas técnica
            - Mantenha precisão jurídica
            - Destaque pontos mais relevantes
            - Seja conciso sem perder informação essencial
            
            Sempre responda em formato JSON válido.
            """;
    }

    @Override
    protected Sumario construirEntidade(ResultadoSumarizacaoIA resultado, RespostaLlamaDTO resposta, long duracaoMs, Object... parametros) {
        ItemLegislativo item = (ItemLegislativo) parametros[2];
        String texto = (String) parametros[0];
        SolicitarSumarioDTO request = (SolicitarSumarioDTO) parametros[1];
        LocalDateTime agora = LocalDateTime.now();

        int tamanhoOriginal = texto.length();
        int tamanhoSumario = resultado.sumarioExecutivo() != null ? resultado.sumarioExecutivo().length() : 0;
        double taxaCompressao = tamanhoOriginal > 0 ? (double) tamanhoSumario / tamanhoOriginal : 0.0;

        return Sumario.builder()
                .itemLegislativo(item)
                .tipoSumario(request.getTipoSumario() != null ? request.getTipoSumario() : "EXECUTIVO")
                .sumarioExecutivo(resultado.sumarioExecutivo())
                .pontosPrincipais(resultado.pontosPrincipais())
                .entidadesRelevantes(resultado.entidadesRelevantes())
                .palavrasChave(resultado.palavrasChave())
                .temasPrincipais(resultado.temasPrincipais())
                .sentimentoGeral(resultado.sentimentoGeral())
                .impactoEstimado(resultado.impactoEstimado())
                .dataCriacao(agora)
                .modeloVersao(modeloVersao)
                .promptUtilizado(resposta.getMessage().getContent())
                .respostaCompleta(resposta.getMessage().getContent())
                .tempoProcessamentoMs(duracaoMs)
                .tamanhoTextoOriginal(tamanhoOriginal)
                .tamanhoSumario(tamanhoSumario)
                .taxaCompressao(taxaCompressao)
                .sucesso(true)
                .dataExpiracao(calcularDataExpiracao())
                .build();
    }

    @Override
    protected Sumario construirEntidadeFalha(Exception erro, long duracaoMs, Object... parametros) {
        ItemLegislativo item = (ItemLegislativo) parametros[2];
        String texto = (String) parametros[0];
        SolicitarSumarioDTO request = (SolicitarSumarioDTO) parametros[1];
        LocalDateTime agora = LocalDateTime.now();

        return Sumario.builder()
                .itemLegislativo(item)
                .tipoSumario(request.getTipoSumario() != null ? request.getTipoSumario() : "EXECUTIVO")
                .dataCriacao(agora)
                .modeloVersao(modeloVersao)
                .tempoProcessamentoMs(duracaoMs)
                .tamanhoTextoOriginal(texto.length())
                .sucesso(false)
                .mensagemErro(erro.getMessage())
                .dataExpiracao(calcularDataExpiracao())
                .build();
    }

    @Override
    protected Optional<Sumario> buscarCacheRecente(Object... parametros) {
        ItemLegislativo item = (ItemLegislativo) parametros[2];

        return repository.findByItemLegislativo(item).stream()
                .filter(this::isCacheValido)
                .findFirst();
    }

    @Transactional
    public SumarioDTO sumarizar(SolicitarSumarioDTO request) {
        ItemLegislativo item = buscarItemLegislativo(request.getItemLegislativoId());
        String textoParaSumarizar = determinarTexto(request, item);

        validarTexto(textoParaSumarizar);

        Sumario sumario = processarComCache(
                request.isForcarNovoSumario(),
                textoParaSumarizar,
                request,
                item
        );

        return toDTO(sumario);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "sumarios", key = "#itemId")
    public SumarioDTO buscarPorItem(String itemId) {
        ItemLegislativo item = buscarItemLegislativo(itemId);

        Sumario sumario = repository.findFirstByItemLegislativoOrderByDataCriacaoDesc(item)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhum sumário encontrado para item: " + itemId));

        return toDTO(sumario);
    }

    @Transactional(readOnly = true)
    public Page<SumarioDTO> buscarTodosPorItem(String itemId, Pageable pageable) {
        ItemLegislativo item = buscarItemLegislativo(itemId);
        Page<Sumario> sumarios = repository.findByItemLegislativo(item, pageable);
        return sumarios.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<SumarioDTO> buscarTodosPorItemLista(String itemId) {
        ItemLegislativo item = buscarItemLegislativo(itemId);
        List<Sumario> sumarios = repository.findByItemLegislativo(item);
        return sumarios.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SumarioDTO> buscarPorTipo(String tipoSumario) {
        List<Sumario> sumarios = repository.findByTipoSumario(tipoSumario);
        return sumarios.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SumarioDTO> buscarPorSucesso(Boolean sucesso) {
        List<Sumario> sumarios = repository.findBySucesso(sucesso);
        return sumarios.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SumarioDTO> buscarAposData(LocalDateTime data) {
        List<Sumario> sumarios = repository.findByDataCriacaoAfter(data);
        return sumarios.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<SumarioDTO> buscarAposDataPaginado(LocalDateTime data, Pageable pageable) {
        Page<Sumario> sumarios = repository.findByDataCriacaoAfter(data, pageable);
        return sumarios.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<SumarioDTO> buscarEntreDatas(LocalDateTime inicio, LocalDateTime fim) {
        List<Sumario> sumarios = repository.findByDataCriacaoBetween(inicio, fim);
        return sumarios.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SumarioDTO> buscarPorPalavrasChave(List<String> palavras) {
        List<Sumario> sumarios = repository.buscarPorPalavrasChave(palavras);
        return sumarios.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SumarioDTO> buscarPorTema(String tema) {
        List<Sumario> sumarios = repository.buscarPorTema(tema);
        return sumarios.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SumarioDTO> buscarCompressaoEficiente(Double taxaMaxima) {
        List<Sumario> sumarios = repository.buscarCompressaoEficiente(taxaMaxima);
        return sumarios.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long contarPorTipo(String tipoSumario) {
        return repository.countByTipoSumario(tipoSumario);
    }

    @Transactional(readOnly = true)
    public long contarPorSucesso(Boolean sucesso) {
        return repository.countBySucesso(sucesso);
    }

    @Transactional(readOnly = true)
    public long contarAposData(LocalDateTime data) {
        return repository.countByDataCriacaoAfter(data);
    }

    @Transactional(readOnly = true)
    public long contarSucessos() {
        return repository.contarSucessos();
    }

    @Override
    @Transactional
    @CacheEvict(value = "sumarios", allEntries = true)
    public void limparExpiradas() {
        super.limparExpiradas();
    }

    private ItemLegislativo buscarItemLegislativo(String itemId) {
        return itemLegislativoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Item legislativo não encontrado: " + itemId));
    }

    private String determinarTexto(SolicitarSumarioDTO request, ItemLegislativo item) {
        if (request.getTexto() != null && !request.getTexto().isBlank()) {
            return request.getTexto();
        }

        if (item.getEmenta() != null && !item.getEmenta().isBlank()) {
            return item.getEmenta();
        }

        throw new SumarizacaoException("Nenhum texto disponível para sumarização");
    }

    private void validarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new SumarizacaoException("Texto para sumarização não pode ser vazio");
        }

        if (texto.length() < textoMinimoCaracteres) {
            throw new SumarizacaoException(
                    String.format("Texto muito curto para sumarização. Mínimo: %d caracteres", textoMinimoCaracteres));
        }
    }

}