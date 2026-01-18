package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.TendenciasIA;
import br.gov.md.parla_md_backend.domain.dto.RespostaLlamaDTO;
import br.gov.md.parla_md_backend.domain.dto.ResultadoTendenciasIA;
import br.gov.md.parla_md_backend.domain.dto.TendenciasIADTO;
import br.gov.md.parla_md_backend.repository.ITendenciasIARepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TendenciasIAService extends BaseIAService<TendenciasIA, TendenciasIADTO, ResultadoTendenciasIA, ITendenciasIARepository> {

    private static final DateTimeFormatter PERIODO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public TendenciasIAService(LlamaService llamaService, ITendenciasIARepository tendenciasRepository) {
        super(llamaService, tendenciasRepository);
    }

    @Override
    protected String getNomeAnalise() {
        return "Análise de Tendências";
    }

    @Override
    protected String getNomeCacheEvict() {
        return "tendencias-ia";
    }

    @Override
    protected Class<ResultadoTendenciasIA> getResultadoClass() {
        return ResultadoTendenciasIA.class;
    }

    @Override
    protected TendenciasIADTO toDTO(TendenciasIA entidade) {
        return TendenciasIADTO.from(entidade);
    }

    @Override
    protected String construirPrompt(Object... parametros) {
        Map<String, Object> dadosContexto = (Map<String, Object>) parametros[0];

        StringBuilder prompt = new StringBuilder();
        prompt.append("Analise os seguintes dados parlamentares e identifique tendências:\n\n");

        prompt.append("DADOS DO PERÍODO:\n");
        dadosContexto.forEach((chave, valor) -> {
            prompt.append(String.format("- %s: %s\n", formatarChave(chave), valor));
        });

        prompt.append("\n\nResponda APENAS com um JSON no formato:\n");
        prompt.append("{\n");
        prompt.append("  \"analiseGeral\": \"<análise geral das tendências em 3-5 frases>\",\n");
        prompt.append("  \"temasEmergentes\": [\"<tema 1>\", \"<tema 2>\", \"<tema 3>\"],\n");
        prompt.append("  \"alertas\": [\"<alerta 1>\", \"<alerta 2>\"],\n");
        prompt.append("  \"previsaoProximoMes\": \"<previsão para o próximo mês em 2-3 frases>\"\n");
        prompt.append("}");

        return prompt.toString();
    }

    @Override
    protected String construirPromptSistema() {
        return """
            Você é um especialista em análise de tendências parlamentares brasileiras.
            Sua tarefa é identificar padrões e tendências em dados legislativos.
            
            Diretrizes:
            - Seja objetivo e baseado em dados
            - Identifique padrões significativos
            - Destaque anomalias ou mudanças importantes
            - Forneça previsões fundamentadas
            - Aponte riscos e oportunidades
            
            Sempre responda em formato JSON válido.
            """;
    }

    @Override
    protected TendenciasIA construirEntidade(ResultadoTendenciasIA resultado, RespostaLlamaDTO resposta, long duracaoMs, Object... parametros) {
        Map<String, Object> dadosContexto = (Map<String, Object>) parametros[0];
        LocalDateTime agora = LocalDateTime.now();
        String periodoReferencia = agora.format(PERIODO_FORMATTER);

        Integer totalDocs = dadosContexto.containsKey("totalDocumentos") ?
                ((Number) dadosContexto.get("totalDocumentos")).intValue() : null;

        return TendenciasIA.builder()
                .analiseGeral(resultado.analiseGeral())
                .temasEmergentes(resultado.temasEmergentes())
                .alertas(resultado.alertas())
                .previsaoProximoMes(resultado.previsaoProximoMes())
                .dadosContexto(dadosContexto)
                .totalDocumentosAnalisados(totalDocs)
                .periodoReferencia(periodoReferencia)
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
    protected TendenciasIA construirEntidadeFalha(Exception erro, long duracaoMs, Object... parametros) {
        Map<String, Object> dadosContexto = (Map<String, Object>) parametros[0];
        LocalDateTime agora = LocalDateTime.now();
        String periodoReferencia = agora.format(PERIODO_FORMATTER);

        return TendenciasIA.builder()
                .dadosContexto(dadosContexto)
                .periodoReferencia(periodoReferencia)
                .dataAnalise(agora)
                .modeloVersao(modeloVersao)
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(false)
                .mensagemErro(erro.getMessage())
                .dataExpiracao(calcularDataExpiracao())
                .build();
    }

    @Override
    protected Optional<TendenciasIA> buscarCacheRecente(Object... parametros) {
        return repository.findFirstByOrderByDataAnaliseDesc()
                .filter(this::isCacheValido);
    }

    @Transactional
    @Cacheable(value = "tendencias-ia", key = "'atual'")
    public TendenciasIADTO analisarTendencias(Map<String, Object> dadosContexto) {
        return analisarTendencias(dadosContexto, false);
    }

    @Transactional
    public TendenciasIADTO analisarTendencias(Map<String, Object> dadosContexto, boolean forcarNova) {
        TendenciasIA tendencias = processarComCache(forcarNova, dadosContexto);
        return toDTO(tendencias);
    }

    @Transactional(readOnly = true)
    public TendenciasIADTO buscarUltima() {
        TendenciasIA tendencias = repository.findFirstByOrderByDataAnaliseDesc()
                .orElse(null);

        if (tendencias != null && isCacheValido(tendencias)) {
            return toDTO(tendencias);
        }

        return null;
    }

    @Transactional(readOnly = true)
    public List<TendenciasIADTO> buscarPorPeriodo(String periodoReferencia) {
        List<TendenciasIA> tendencias = repository.findByPeriodoReferencia(periodoReferencia);
        return tendencias.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TendenciasIADTO> buscarPorPeriodoPaginado(String periodoReferencia, Pageable pageable) {
        Page<TendenciasIA> tendencias = repository.findByPeriodoReferencia(periodoReferencia, pageable);
        return tendencias.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<TendenciasIADTO> buscarPorTemas(List<String> temas) {
        List<TendenciasIA> tendencias = repository.findByTemasEmergentesContaining(temas);
        return tendencias.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TendenciasIADTO> buscarComAlertas() {
        List<TendenciasIA> tendencias = repository.findComAlertas();
        return tendencias.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TendenciasIADTO> buscarComAlertasPaginado(Pageable pageable) {
        Page<TendenciasIA> tendencias = repository.findComAlertas(pageable);
        return tendencias.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public long contarPorPeriodo(String periodoReferencia) {
        return repository.countByPeriodoReferencia(periodoReferencia);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tendencias-ia", allEntries = true)
    public void limparExpiradas() {
        super.limparExpiradas();
    }

    private String formatarChave(String chave) {
        return chave.replaceAll("([A-Z])", " $1")
                .replaceAll("_", " ")
                .trim()
                .substring(0, 1).toUpperCase() +
                chave.replaceAll("([A-Z])", " $1")
                        .replaceAll("_", " ")
                        .trim()
                        .substring(1);
    }
}