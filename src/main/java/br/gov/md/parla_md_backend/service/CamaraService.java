package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.dto.ProposicaoDTO;
import br.gov.md.parla_md_backend.domain.dto.ProposicaoResumoDTO;
import br.gov.md.parla_md_backend.domain.dto.ProcedimentoProposicaoDTO;
import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.ProcedimentoProposicao;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
import br.gov.md.parla_md_backend.exception.ApiExternaException;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import br.gov.md.parla_md_backend.repository.IProcedimentoProposicaoRepository;
import br.gov.md.parla_md_backend.util.ApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CamaraService {

    private static final int ITENS_PADRAO = 20;
    private static final int ANO_PADRAO = LocalDateTime.now().getYear();

    @Value("${camara.api.base-url}")
    private String camaraApiBaseUrl;

    private final ApiClient apiClient;
    private final IProposicaoRepository proposicaoRepository;
    private final IProcedimentoProposicaoRepository procedimentoRepository;

    // ==================== SINCRONIZAÇÃO COM API ====================

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    @CacheEvict(value = "proposicoes", allEntries = true)
    public void sincronizacaoAgendada() {
        log.info("Iniciando sincronização agendada de proposições da Câmara");
        sincronizarProposicoes();
    }

    @Transactional
    @CacheEvict(value = "proposicoes", allEntries = true)
    public List<ProposicaoDTO> sincronizarProposicoes() {
        return sincronizarProposicoes(ANO_PADRAO, ITENS_PADRAO);
    }

    @Transactional
    @CacheEvict(value = "proposicoes", allEntries = true)
    public List<ProposicaoDTO> sincronizarProposicoes(Integer ano, Integer itens) {
        try {
            String endpoint = construirEndpointProposicoes(ano, itens);
            log.info("Sincronizando proposições do endpoint: {}", endpoint);

            String jsonData = buscarDadosApi(endpoint);
            JSONObject json = new JSONObject(jsonData);
            JSONArray dados = json.getJSONArray("dados");

            log.info("Recebidas {} proposições da API", dados.length());

            List<ProposicaoDTO> proposicoesSalvas = new ArrayList<>();

            for (int i = 0; i < dados.length(); i++) {
                try {
                    JSONObject propJson = dados.getJSONObject(i);
                    Proposicao proposicao = parsearProposicao(propJson);

                    Proposicao salva = proposicaoRepository.save(proposicao);
                    proposicoesSalvas.add(ProposicaoDTO.fromEntity(salva));

                    log.debug("Proposição salva: {}", salva.getIdentificadorCompleto());

                } catch (Exception e) {
                    log.error("Erro ao processar proposição no índice {}: {}",
                            i, e.getMessage(), e);
                }
            }

            log.info("Total de proposições sincronizadas: {}", proposicoesSalvas.size());
            return proposicoesSalvas;

        } catch (Exception e) {
            log.error("Erro durante sincronização de proposições: {}", e.getMessage(), e);
            throw new ApiExternaException(
                    "Erro ao sincronizar proposições da Câmara: " + e.getMessage(), e);
        }
    }

    // ==================== BUSCA DE PROPOSIÇÕES ====================

    @Cacheable(value = "proposicoes", key = "'todas'")
    public List<ProposicaoDTO> buscarTodasProposicoes() {
        log.debug("Buscando todas as proposições");
        return proposicaoRepository.findAll().stream()
                .map(ProposicaoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "proposicoes", key = "'todas-resumo'")
    public List<ProposicaoResumoDTO> buscarTodasProposicoesResumo() {
        log.debug("Buscando resumo de todas as proposições");
        return proposicaoRepository.findAll().stream()
                .map(ProposicaoResumoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "proposicoes", key = "#id")
    public ProposicaoDTO buscarPorId(String id) {
        log.debug("Buscando proposição por ID: {}", id);

        Proposicao proposicao = proposicaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Proposição não encontrada com ID: " + id));

        return ProposicaoDTO.fromEntity(proposicao);
    }

    public List<ProposicaoDTO> buscarPorAno(Integer ano) {
        log.debug("Buscando proposições por ano: {}", ano);
        return proposicaoRepository.findByAno(ano).stream()
                .map(ProposicaoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<ProposicaoDTO> buscarPorAno(Integer ano, Pageable pageable) {
        log.debug("Buscando proposições por ano com paginação: {}", ano);
        return proposicaoRepository.findByAno(ano, pageable)
                .map(ProposicaoDTO::fromEntity);
    }

    public List<ProposicaoDTO> buscarPorSiglaTipo(String siglaTipo) {
        log.debug("Buscando proposições por tipo: {}", siglaTipo);
        return proposicaoRepository.findBySiglaTipo(siglaTipo).stream()
                .map(ProposicaoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProposicaoDTO> buscarPorAnoETipo(Integer ano, String siglaTipo) {
        log.debug("Buscando proposições por ano {} e tipo {}", ano, siglaTipo);
        return proposicaoRepository.findByAnoAndSiglaTipo(ano, siglaTipo).stream()
                .map(ProposicaoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProposicaoDTO> buscarPorAutor(String autorId) {
        log.debug("Buscando proposições por autor: {}", autorId);
        return proposicaoRepository.findByAutorId(autorId).stream()
                .map(ProposicaoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProposicaoDTO> buscarPorDeputado(Long idDeputado) {
        log.debug("Buscando proposições por deputado: {}", idDeputado);
        return proposicaoRepository.findByIdDeputadoAutor(idDeputado).stream()
                .map(ProposicaoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProposicaoDTO> buscarPorTema(String tema) {
        log.debug("Buscando proposições por tema: {}", tema);
        return proposicaoRepository.findByTema(tema).stream()
                .map(ProposicaoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<ProposicaoDTO> buscarPorStatusTriagem(
            StatusTriagem status,
            Pageable pageable) {
        log.debug("Buscando proposições por status de triagem: {}", status);
        return proposicaoRepository.findByTriagemStatus(status, pageable)
                .map(ProposicaoDTO::fromEntity);
    }

    @Transactional
    public List<ProcedimentoProposicaoDTO> sincronizarProcedimentos(Long idProposicao) {
        try {
            String endpoint = construirEndpointProcedimentos(idProposicao);
            log.info("Sincronizando procedimentos do endpoint: {}", endpoint);

            String jsonData = buscarDadosApi(endpoint);
            JSONObject json = new JSONObject(jsonData);
            JSONArray dados = json.getJSONArray("dados");

            List<ProcedimentoProposicaoDTO> procedimentosSalvos = new ArrayList<>();

            Proposicao proposicao = proposicaoRepository.findByIdCamara(idProposicao)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Proposição não encontrada com ID Câmara: " + idProposicao));

            for (int i = 0; i < dados.length(); i++) {
                try {
                    JSONObject procJson = dados.getJSONObject(i);

                    ProcedimentoProposicao procedimento = parsearProcedimento(procJson);
                    procedimento.setProposicao(proposicao);

                    ProcedimentoProposicao salvo = procedimentoRepository.save(procedimento);

                    ProcedimentoProposicaoDTO dto = ProcedimentoProposicaoDTO.from(salvo);
                    procedimentosSalvos.add(dto);

                    log.debug("Procedimento salvo - Seq: {}", salvo.getSequencia());

                } catch (Exception e) {
                    log.error("Erro ao processar procedimento no índice {}: {}",
                            i, e.getMessage(), e);
                }
            }

            log.info("Total de procedimentos sincronizados: {}", procedimentosSalvos.size());
            return procedimentosSalvos;

        } catch (Exception e) {
            log.error("Erro durante sincronização de procedimentos: {}", e.getMessage(), e);
            throw new ApiExternaException(
                    "Erro ao sincronizar procedimentos da proposição: " + e.getMessage(), e);
        }
    }

    public List<ProcedimentoProposicaoDTO> buscarProcedimentosPorProposicao(String proposicaoId) {
        log.debug("Buscando procedimentos da proposição: {}", proposicaoId);

        List<ProcedimentoProposicao> procedimentos = procedimentoRepository.findByPropositionId(proposicaoId);

        return procedimentos.stream()
                .map(ProcedimentoProposicaoDTO::from)
                .collect(Collectors.toList());
    }

    public long contarPorAno(Integer ano) {
        return proposicaoRepository.countByAno(ano);
    }

    public long contarPorTipo(String siglaTipo) {
        return proposicaoRepository.countBySiglaTipo(siglaTipo);
    }

    public long contarPorStatusTriagem(StatusTriagem status) {
        return proposicaoRepository.countByTriagemStatus(status);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private String construirEndpointProposicoes(Integer ano, Integer itens) {
        return String.format("%sproposicoes?ano=%d&itens=%d&ordem=ASC&ordenarPor=id",
                camaraApiBaseUrl, ano, itens);
    }

    private String construirEndpointProcedimentos(Long idProposicao) {
        return String.format("%sproposicoes/%d/tramitacoes",
                camaraApiBaseUrl, idProposicao);
    }

    private String buscarDadosApi(String endpoint) {
        try {
            return apiClient.get(endpoint);
        } catch (Exception e) {
            log.error("Erro ao buscar dados da API: {}", e.getMessage(), e);
            throw new ApiExternaException(
                    "Erro ao buscar dados da API da Câmara: " + e.getMessage(), e);
        }
    }

    private Proposicao parsearProposicao(JSONObject propJson) {
        Proposicao proposicao = new Proposicao();

        proposicao.setIdCamara(propJson.optLong("id", 0L));
        proposicao.setUriProposicao(propJson.optString("uri"));
        proposicao.setSiglaTipo(propJson.optString("siglaTipo"));

        // ✅ CORREÇÃO LINHA 292 - Converter int para String
        int numeroInt = propJson.optInt("numero", 0);
        proposicao.setNumero(String.valueOf(numeroInt));

        proposicao.setAno(propJson.optInt("ano", 0));
        proposicao.setEmenta(propJson.optString("ementa"));

        // ✅ CORREÇÃO LINHA 297 - Converter LocalDateTime para LocalDate
        String dataApresentacaoStr = propJson.optString("dataApresentacao");
        LocalDateTime dataHoraApresentacao = parsearDataHora(dataApresentacaoStr);
        if (dataHoraApresentacao != null) {
            proposicao.setDataApresentacao(dataHoraApresentacao.toLocalDate());
        }

        JSONObject statusProposicao = propJson.optJSONObject("statusProposicao");
        if (statusProposicao != null) {
            parsearStatusProposicao(proposicao, statusProposicao);
        }

        proposicao.setStatusTriagem(StatusTriagem.NAO_AVALIADO);
        proposicao.setStatusTramitacao(StatusTramitacao.EM_ANDAMENTO);
        proposicao.setDataCaptura(LocalDateTime.now());
        proposicao.setDataUltimaAtualizacao(LocalDateTime.now());

        return proposicao;
    }

    private void parsearStatusProposicao(Proposicao proposicao, JSONObject statusObj) {
        proposicao.setStatusProposicao(statusObj.optString("descricaoSituacao"));
        proposicao.setDescricaoTramitacao(statusObj.optString("descricaoTramitacao"));
        proposicao.setDespacho(statusObj.optString("despacho"));
        proposicao.setSiglaOrgao(statusObj.optString("siglaOrgao"));
        proposicao.setRegime(statusObj.optString("regime"));
        proposicao.setSituacaoAtual(statusObj.optString("descricaoSituacao"));
    }

    private ProcedimentoProposicao parsearProcedimento(JSONObject procJson) {
        ProcedimentoProposicao procedimento = new ProcedimentoProposicao();

        String dataHoraStr = procJson.getString("dataHora");
        procedimento.setDataHora(parsearDataHora(dataHoraStr));

        procedimento.setSequencia(procJson.optInt("sequencia", 0));
        procedimento.setSiglaOrgao(procJson.optString("siglaOrgao"));
        procedimento.setDescricaoTramitacao(procJson.optString("descricaoTramitacao"));
        procedimento.setDespacho(procJson.optString("despacho"));
        procedimento.setRegime(procJson.optString("regime"));
        procedimento.setIdTipoTramitacao(procJson.optString("idTipoTramitacao"));

        return procedimento;
    }

    /**
     * Parseia string de data/hora ISO para LocalDateTime
     */
    private LocalDateTime parsearDataHora(String dataStr) {
        if (dataStr == null || dataStr.isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dataStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            log.warn("Erro ao parsear data/hora '{}': {}", dataStr, e.getMessage());
            return null;
        }
    }

    /**
     * Parseia string de data ISO para LocalDate
     */
    private LocalDate parsearData(String dataStr) {
        if (dataStr == null || dataStr.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dataStr, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e1) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(dataStr, DateTimeFormatter.ISO_DATE_TIME);
                return dateTime.toLocalDate();
            } catch (DateTimeParseException e2) {
                log.warn("Erro ao parsear data '{}': {}", dataStr, e2.getMessage());
                return null;
            }
        }
    }
}