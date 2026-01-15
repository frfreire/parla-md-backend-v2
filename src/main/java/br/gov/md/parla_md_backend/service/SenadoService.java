package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Parlamentar;
import br.gov.md.parla_md_backend.domain.ProcedimentoMateria;
import br.gov.md.parla_md_backend.domain.dto.MateriaDTO;
import br.gov.md.parla_md_backend.domain.enums.TipoMateria;
import br.gov.md.parla_md_backend.domain.Materia;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import br.gov.md.parla_md_backend.exception.ApiExternaException;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.messaging.RabbitMQProducer;
import br.gov.md.parla_md_backend.repository.IMateriaRepository;
import br.gov.md.parla_md_backend.repository.IProcedimentoMateriaRepository;
import br.gov.md.parla_md_backend.repository.ISenadorRepository;
import br.gov.md.parla_md_backend.util.ApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SenadoService {

    private static final String MATERIA_EXCHANGE = "materia.exchange";
    private static final String MATERIA_ROUTING_KEY = "materia.nova";
    private static final String PROCEDIMENTO_EXCHANGE = "procedimento.exchange";
    private static final String PROCEDIMENTO_ROUTING_KEY = "procedimento.novo";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Value("${senado.api.base-url:https://legis.senado.leg.br/dadosabertos/}")
    private String senadoApiBaseUrl;

    @Value("${senado.api.default-year:#{T(java.time.Year).now().getValue()}}")
    private int anoDefault;

    @Value("${senado.api.default-items:100}")
    private int itensDefault;

    // ==================== DEPENDÊNCIAS ====================

    private final ApiClient apiClient;
    private final IMateriaRepository materiaRepository;
    private final IProcedimentoMateriaRepository procedimentoRepository;
    private final ISenadorRepository senadorRepository;
    private final RabbitMQProducer rabbitMQProducer;

    // ==================== ESTADO ====================

    private LocalDateTime ultimaAtualizacaoSucesso;

    // ==================== MÉTODOS PÚBLICOS - MATÉRIAS ====================

    /**
     * Busca matéria por ID.
     *
     * @param id ID da matéria
     * @return DTO da matéria encontrada
     * @throws RecursoNaoEncontradoException se não encontrar
     */
    @Cacheable(value = "materias", key = "#id")
    public MateriaDTO buscarPorId(String id) {
        log.debug("Buscando matéria por ID: {}", id);

        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Matéria não encontrada com ID: " + id));

        return MateriaDTO.fromEntity(materia);
    }

    /**
     * Busca matéria por código do Senado.
     *
     * @param codigoMateria Código da matéria no Senado
     * @return DTO da matéria encontrada
     * @throws RecursoNaoEncontradoException se não encontrar
     */
    @Cacheable(value = "materias", key = "'codigo-' + #codigoMateria")
    public MateriaDTO buscarPorCodigo(Long codigoMateria) {
        log.debug("Buscando matéria por código: {}", codigoMateria);

        Materia materia = materiaRepository.findByCodigoMateria(codigoMateria)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Matéria não encontrada com código: " + codigoMateria));

        return MateriaDTO.fromEntity(materia);
    }

    /**
     * Lista todas as matérias com paginação.
     *
     * @param pageable Configuração de paginação
     * @return Página de matérias
     */
    public Page<MateriaDTO> listarTodas(Pageable pageable) {
        log.debug("Listando matérias - página: {}, tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return materiaRepository.findAll(pageable)
                .map(MateriaDTO::fromEntity);
    }

    /**
     * Busca matérias por ano.
     *
     * @param ano Ano das matérias
     * @return Lista de DTOs das matérias
     */
    @Cacheable(value = "materias-ano", key = "#ano")
    public List<MateriaDTO> buscarPorAno(int ano) {
        log.debug("Buscando matérias do ano: {}", ano);

        return materiaRepository.findByAno(ano).stream()
                .map(MateriaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Busca matérias por tipo.
     *
     * @param tipoMateria Tipo da matéria
     * @param pageable Configuração de paginação
     * @return Página de matérias
     */
    public Page<MateriaDTO> buscarPorTipo(TipoMateria tipoMateria, Pageable pageable) {
        log.debug("Buscando matérias do tipo: {}", tipoMateria);

        return materiaRepository.findByTipoMateria(tipoMateria, pageable)
                .map(MateriaDTO::fromEntity);
    }

    /**
     * Busca matérias em tramitação.
     *
     * @param pageable Configuração de paginação
     * @return Página de matérias em tramitação
     */
    public Page<MateriaDTO> buscarEmTramitacao(Pageable pageable) {
        log.debug("Buscando matérias em tramitação");

        return materiaRepository.findByIndicadorTramitando("Sim", pageable)
                .map(MateriaDTO::fromEntity);
    }

    /**
     * Busca matérias por status de triagem.
     *
     * @param status Status de triagem
     * @param pageable Configuração de paginação
     * @return Página de matérias
     */
    public Page<MateriaDTO> buscarPorStatusTriagem(StatusTriagem status, Pageable pageable) {
        log.debug("Buscando matérias com status de triagem: {}", status);

        return materiaRepository.findByStatusTriagem(status, pageable)
                .map(MateriaDTO::fromEntity);
    }

    /**
     * Busca matérias apresentadas após uma data.
     *
     * @param data Data limite
     * @return Lista de DTOs das matérias
     */
    public List<MateriaDTO> buscarApresentadasApos(LocalDate data) {
        log.debug("Buscando matérias apresentadas após: {}", data);

        return materiaRepository.findByDataApresentacaoAfter(data).stream()
                .map(MateriaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS DE SINCRONIZAÇÃO ====================

    /**
     * Job agendado para sincronizar matérias diariamente às 2h.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void sincronizarMateriasAgendado() {
        log.info("Iniciando sincronização agendada de matérias do Senado");

        try {
            List<MateriaDTO> materias = buscarESalvarMaterias(anoDefault, itensDefault);
            log.info("Sincronização concluída. {} matérias processadas", materias.size());
            atualizarUltimaSincronizacao();
        } catch (Exception e) {
            log.error("Erro na sincronização agendada de matérias", e);
        }
    }

    /**
     * Busca matérias na API do Senado e salva no banco.
     *
     * @param ano Ano das matérias
     * @param itens Quantidade de itens
     * @return Lista de DTOs das matérias salvas
     */
    @Transactional
    @CacheEvict(value = {"materias", "materias-ano"}, allEntries = true)
    public List<MateriaDTO> buscarESalvarMaterias(int ano, int itens) {
        log.info("Buscando matérias do Senado - ano: {}, itens: {}", ano, itens);

        try {
            String xmlData = buscarMateriasXml(ano, itens);
            List<Materia> materias = parsearMateriasXml(xmlData);
            List<Materia> salvas = salvarMaterias(materias);

            // Publicar eventos para cada matéria nova
            salvas.forEach(this::publicarEventoMateria);

            log.info("{} matérias salvas com sucesso", salvas.size());
            return salvas.stream()
                    .map(MateriaDTO::fromEntity)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Erro ao buscar e salvar matérias do Senado", e);
            throw new ApiExternaException("Falha ao sincronizar matérias do Senado: " + e.getMessage(), e);
        }
    }

    /**
     * Busca e salva matérias com parâmetros default.
     *
     * @return Lista de DTOs das matérias salvas
     */
    @Transactional
    public List<MateriaDTO> buscarESalvarMaterias() {
        return buscarESalvarMaterias(anoDefault, itensDefault);
    }

    /**
     * Atualiza uma matéria existente.
     *
     * @param id ID da matéria
     * @param dto DTO com dados atualizados
     * @return DTO da matéria atualizada
     */
    @Transactional
    @CacheEvict(value = "materias", key = "#id")
    public MateriaDTO atualizar(String id, MateriaDTO dto) {
        log.info("Atualizando matéria: {}", id);

        Materia existente = materiaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Matéria não encontrada com ID: " + id));

        atualizarCamposMateria(existente, dto);
        existente.setDataUltimaAtualizacao(LocalDateTime.now());

        Materia salva = materiaRepository.save(existente);
        return MateriaDTO.fromEntity(salva);
    }

    /**
     * Remove uma matéria.
     *
     * @param id ID da matéria
     */
    @Transactional
    @CacheEvict(value = "materias", key = "#id")
    public void remover(String id) {
        log.info("Removendo matéria: {}", id);

        if (!materiaRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Matéria não encontrada com ID: " + id);
        }

        materiaRepository.deleteById(id);
    }

    // ==================== MÉTODOS DE PROCEDIMENTOS ====================

    /**
     * Busca procedimentos/tramitações de uma matéria.
     *
     * @param codigoMateria Código da matéria
     * @return Lista de procedimentos
     */
    public List<ProcedimentoMateria> buscarProcedimentos(Long codigoMateria) {
        log.debug("Buscando procedimentos da matéria: {}", codigoMateria);

        try {
            String xmlData = buscarProcedimentosXml(codigoMateria);
            List<ProcedimentoMateria> procedimentos = parsearProcedimentosXml(xmlData, codigoMateria);

            // Salvar e publicar
            procedimentos = procedimentoRepository.saveAll(procedimentos);
            procedimentos.forEach(this::publicarEventoProcedimento);

            return procedimentos;

        } catch (Exception e) {
            log.error("Erro ao buscar procedimentos da matéria {}", codigoMateria, e);
            throw new ApiExternaException(
                    "Falha ao buscar procedimentos da matéria: " + codigoMateria, e);
        }
    }

    // ==================== MÉTODOS DE SENADORES ====================

    /**
     * Busca todos os senadores cadastrados.
     *
     * @return Lista de senadores
     */
    @Cacheable(value = "senadores")
    public List<Parlamentar> buscarSenadores() {
        log.debug("Buscando senadores cadastrados");
        return senadorRepository.findAll();
    }

    /**
     * Busca senador por nome.
     *
     * @param nome Nome do senador
     * @return Senador encontrado ou null
     */
    public Parlamentar buscarSenadorPorNome(String nome) {
        log.debug("Buscando senador por nome: {}", nome);
        return senadorRepository.findByNome(nome);
    }

    // ==================== MÉTODOS DE STATUS ====================

    /**
     * Retorna o horário da última sincronização bem-sucedida.
     *
     * @return String com data/hora ou mensagem informativa
     */
    public String obterHorarioUltimaAtualizacao() {
        return ultimaAtualizacaoSucesso != null
                ? ultimaAtualizacaoSucesso.format(DATE_TIME_FORMATTER)
                : "Nenhuma sincronização realizada";
    }

    /**
     * Retorna o ano default configurado.
     *
     * @return Ano default
     */
    public int getAnoDefault() {
        return anoDefault;
    }

    /**
     * Retorna a quantidade de itens default.
     *
     * @return Quantidade de itens default
     */
    public int getItensDefault() {
        return itensDefault;
    }

    // ==================== MÉTODOS PRIVADOS - API ====================

    private String buscarMateriasXml(int ano, int itens) {
        String endpoint = String.format("%smateria/pesquisa/lista?ano=%d&itens=%d",
                senadoApiBaseUrl, ano, itens);

        log.debug("Chamando API do Senado: {}", endpoint);

        try {
            return apiClient.get(endpoint);
        } catch (Exception e) {
            throw new ApiExternaException("Erro ao chamar API do Senado: " + endpoint, e);
        }
    }

    private String buscarProcedimentosXml(Long codigoMateria) {
        String endpoint = String.format("%smateria/%d/tramitacoes",
                senadoApiBaseUrl, codigoMateria);

        log.debug("Buscando procedimentos: {}", endpoint);
        return apiClient.get(endpoint);
    }

    // ==================== MÉTODOS PRIVADOS - PARSING ====================

    private List<Materia> parsearMateriasXml(String xmlData) {
        List<Materia> materias = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Desabilitar DTD para segurança
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlData)));

            NodeList materiaNodes = doc.getElementsByTagName("Materia");
            log.debug("Encontradas {} matérias no XML", materiaNodes.getLength());

            for (int i = 0; i < materiaNodes.getLength(); i++) {
                Element materiaElement = (Element) materiaNodes.item(i);
                Materia materia = criarMateriaDeElemento(materiaElement);
                materias.add(materia);
            }

        } catch (Exception e) {
            log.error("Erro ao parsear XML de matérias", e);
            throw new ApiExternaException("Erro ao processar resposta do Senado", e);
        }

        return materias;
    }

    private Materia criarMateriaDeElemento(Element element) {
        Materia materia = new Materia();

        // Dados básicos
        String codigo = obterTextoElemento(element, "CodigoMateria");
        if (codigo != null && !codigo.isEmpty()) {
            materia.setCodigoMateria(Long.parseLong(codigo));
        }

        materia.setSiglaSubtipoMateria(obterTextoElemento(element, "SiglaSubtipoMateria"));
        materia.setDescricaoSubtipoMateria(obterTextoElemento(element, "DescricaoSubtipoMateria"));

        String numero = obterTextoElemento(element, "NumeroMateria");
        if (numero != null && !numero.isEmpty()) {
            materia.setNumero(numero);
        }

        String ano = obterTextoElemento(element, "AnoMateria");
        if (ano != null && !ano.isEmpty()) {
            materia.setAno(Integer.parseInt(ano));
        }

        materia.setEmenta(obterTextoElemento(element, "EmentaMateria"));
        materia.setDescricaoIdentificacaoMateria(obterTextoElemento(element, "DescricaoIdentificacaoMateria"));
        materia.setIndicadorTramitando(obterTextoElemento(element, "IndicadorTramitando"));

        // Autor
        materia.setNomeParlamentarAutor(obterTextoElemento(element, "NomeAutor"));
        String codAutor = obterTextoElemento(element, "CodigoParlamentarAutor");
        if (codAutor != null && !codAutor.isEmpty()) {
            materia.setCodigoParlamentarAutor(Long.parseLong(codAutor));
        }
        materia.setSiglaPartidoParlamentar(obterTextoElemento(element, "SiglaPartidoParlamentar"));
        materia.setSiglaUFParlamentar(obterTextoElemento(element, "SiglaUfParlamentar"));

        // Outros campos
        materia.setDescricaoNatureza(obterTextoElemento(element, "DescricaoNatureza"));
        materia.setSiglaOrgaoOrigem(obterTextoElemento(element, "SiglaOrgaoOrigem"));
        materia.setAssuntoEspecifico(obterTextoElemento(element, "AssuntoEspecifico"));
        materia.setAssuntoGeral(obterTextoElemento(element, "AssuntoGeral"));
        materia.setIndexacao(obterTextoElemento(element, "Indexacao"));

        // Data de apresentação
        String dataApres = obterTextoElemento(element, "DataApresentacao");
        if (dataApres != null && !dataApres.isEmpty()) {
            try {
                materia.setDataApresentacao(LocalDate.parse(dataApres, DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                log.warn("Erro ao parsear data de apresentação: {}", dataApres);
            }
        }

        // Metadados
        materia.setStatusTriagem(StatusTriagem.NAO_AVALIADO);
        materia.setDataCaptura(LocalDateTime.now());
        materia.setDataUltimaAtualizacao(LocalDateTime.now());

        // Tentar determinar tipo
        determinarTipoMateria(materia);

        return materia;
    }

    private List<ProcedimentoMateria> parsearProcedimentosXml(String xmlData, Long codigoMateria) {
        List<ProcedimentoMateria> procedimentos = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlData)));

            NodeList tramitacaoNodes = doc.getElementsByTagName("Tramitacao");

            for (int i = 0; i < tramitacaoNodes.getLength(); i++) {
                Element tramitacaoElement = (Element) tramitacaoNodes.item(i);
                ProcedimentoMateria procedimento = criarProcedimentoDeElemento(
                        tramitacaoElement, codigoMateria);
                procedimentos.add(procedimento);
            }

        } catch (Exception e) {
            log.error("Erro ao parsear XML de procedimentos", e);
            throw new ApiExternaException("Erro ao processar procedimentos", e);
        }

        return procedimentos;
    }

    private ProcedimentoMateria criarProcedimentoDeElemento(Element element, Long codigoMateria) {
        ProcedimentoMateria procedimento = new ProcedimentoMateria();

        procedimento.setCodigoMateria(codigoMateria);
        procedimento.setDescricaoTramitacao(obterTextoElemento(element, "DescricaoTramitacao"));
        procedimento.setSituacaoDescricao(obterTextoElemento(element, "SituacaoDescricaoSituacao"));
        procedimento.setLocalTramitacao(obterTextoElemento(element, "NomeLocal"));

        String dataTram = obterTextoElemento(element, "DataTramitacao");
        if (dataTram != null && !dataTram.isEmpty()) {
            try {
                procedimento.setDataTramitacao(LocalDateTime.parse(dataTram, DATE_TIME_FORMATTER));
            } catch (DateTimeParseException e) {
                try {
                    procedimento.setDataTramitacao(LocalDate.parse(dataTram, DATE_FORMATTER).atStartOfDay());
                } catch (DateTimeParseException ex) {
                    log.warn("Erro ao parsear data de tramitação: {}", dataTram);
                }
            }
        }

        procedimento.setDataCaptura(LocalDateTime.now());

        return procedimento;
    }

    private String obterTextoElemento(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent().trim();
        }
        return null;
    }

    private void determinarTipoMateria(Materia materia) {
        String sigla = materia.getSiglaSubtipoMateria();
        if (sigla == null) return;

        try {
            materia.setTipoMateria(TipoMateria.valueOf(sigla.toUpperCase()));
        } catch (IllegalArgumentException e) {
            log.debug("Tipo de matéria não mapeado: {}", sigla);
        }
    }

    // ==================== MÉTODOS PRIVADOS - PERSISTÊNCIA ====================

    private List<Materia> salvarMaterias(List<Materia> materias) {
        List<Materia> salvas = new ArrayList<>();

        for (Materia materia : materias) {
            try {
                // Verificar se já existe
                if (materia.getCodigoMateria() != null) {
                    materiaRepository.findByCodigoMateria(materia.getCodigoMateria())
                            .ifPresentOrElse(
                                    existente -> {
                                        // Atualizar existente
                                        atualizarCamposMateria(existente, MateriaDTO.fromEntity(materia));
                                        existente.setDataUltimaAtualizacao(LocalDateTime.now());
                                        salvas.add(materiaRepository.save(existente));
                                    },
                                    () -> {
                                        // Salvar nova
                                        salvas.add(materiaRepository.save(materia));
                                    }
                            );
                } else {
                    salvas.add(materiaRepository.save(materia));
                }
            } catch (Exception e) {
                log.error("Erro ao salvar matéria: {}", materia.getCodigoMateria(), e);
            }
        }

        return salvas;
    }

    private void atualizarCamposMateria(Materia existente, MateriaDTO dto) {
        if (dto.getEmenta() != null) {
            existente.setEmenta(dto.getEmenta());
        }
        if (dto.getIndicadorTramitando() != null) {
            existente.setIndicadorTramitando(dto.getIndicadorTramitando());
        }
        if (dto.getSituacaoAtual() != null) {
            existente.setSituacaoAtual(dto.getSituacaoAtual());
        }
        if (dto.getStatusTriagem() != null) {
            existente.setStatusTriagem(dto.getStatusTriagem());
        }
        if (dto.getTema() != null) {
            existente.setTema(dto.getTema());
        }
    }

    // ==================== MÉTODOS PRIVADOS - EVENTOS ====================

    private void publicarEventoMateria(Materia materia) {
        try {
            rabbitMQProducer.sendMessage(MATERIA_EXCHANGE, MATERIA_ROUTING_KEY, materia);
            log.debug("Evento publicado para matéria: {}", materia.getCodigoMateria());
        } catch (Exception e) {
            log.error("Erro ao publicar evento de matéria: {}", materia.getCodigoMateria(), e);
        }
    }

    private void publicarEventoProcedimento(ProcedimentoMateria procedimento) {
        try {
            rabbitMQProducer.sendMessage(PROCEDIMENTO_EXCHANGE, PROCEDIMENTO_ROUTING_KEY, procedimento);
            log.debug("Evento publicado para procedimento da matéria: {}", procedimento.getCodigoMateria());
        } catch (Exception e) {
            log.error("Erro ao publicar evento de procedimento", e);
        }
    }

    private void atualizarUltimaSincronizacao() {
        this.ultimaAtualizacaoSucesso = LocalDateTime.now();
    }
}