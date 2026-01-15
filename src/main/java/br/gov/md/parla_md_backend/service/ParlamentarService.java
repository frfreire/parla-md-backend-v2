package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.*;
import br.gov.md.parla_md_backend.domain.enums.Casa;
import br.gov.md.parla_md_backend.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParlamentarService {

    private static final Logger logger = LoggerFactory.getLogger(ParlamentarService.class);
    private RestTemplate restTemplate;
    private IParlamentarRepository parlamentarRepository;
    private IProposicaoRepository proposicaoRepository;
    private IVotacaoRepository votingRepository;
    private CamaraService camaraService;
    private SenadoService senadoService;
    private List<String> searchTerms;
    private final ISenadorRepository senadorRepository;
    private final IDeputadoRepository deputadoRepository;

    @Value("${search.terms.file:classpath:search_terms.txt}")
    private String specificTermsFile;

    @Value("${camara.api.base-url}")
    private String camaraApiBaseUrl;

    @Value("${senado.api.base-url}")
    private String senadoApiBaseUrl;

    @Autowired
    public ParlamentarService(IParlamentarRepository parlamentarRepository,
                              IProposicaoRepository proposicaoRepository,
                              IVotacaoRepository votacaoRepository,
                              RestTemplate restTemplate,
                              CamaraService camaraService,
                              SenadoService senadoService, ISenadorRepository senadorRepository, IDeputadoRepository deputadoRepository) {
        this.parlamentarRepository = parlamentarRepository;
        this.proposicaoRepository = proposicaoRepository;
        this.votingRepository = votacaoRepository;
        this.restTemplate = restTemplate;
        this.camaraService = camaraService;
        this.senadoService = senadoService;
        this.senadorRepository = senadorRepository;
        this.deputadoRepository = deputadoRepository;
    }

    @PostConstruct
    public void init() {
        try {
            loadSpecificTerms();
        } catch (Exception e) {
            logger.error("Erro durante a inicialização do ParlamentarianService", e);
        }
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledSyncParlamentarians() {
        try {
            logger.info("Iniciando sincronização agendada de parlamentares");
            syncDeputados();
            syncSenadores();
            logger.info("Sincronização de parlamentares concluída");
        } catch (Exception e) {
            logger.error("Erro durante a sincronização agendada de parlamentares", e);
        }
    }

    @Transactional
    public void syncDeputados() {
        logger.info("Iniciando sincronização de deputados");
        try {
            List<Parlamentar> deputados = fetchAndSaveDeputados();

            logger.info("Sincronização de deputados concluída: {} deputados", deputados.size());

        } catch (Exception e) {
            logger.error("Erro durante a sincronização de deputados", e);
        }
    }

    @Transactional
    public void syncSenadores() {
        logger.info("Iniciando sincronização de senadores");
        try {
            List<Parlamentar> senadores = fetchAndSaveSenadores();

            logger.info("Sincronização de senadores concluída: {} senadores", senadores.size());

        } catch (Exception e) {
            logger.error("Erro durante a sincronização de senadores", e);
        }
    }

    private void updateDeputadoIfNeeded(Parlamentar existing, Parlamentar updated) {
        boolean needsUpdate = !existing.getNome().equals(updated.getNome()) ||
                !existing.getSiglaPartido().equals(updated.getSiglaPartido()) ||
                !existing.getSiglaUF().equals(updated.getSiglaUF()) ||
                !existing.getUrlFoto().equals(updated.getUrlFoto());

        if (needsUpdate) {
            existing.setNome(updated.getNome());
            existing.setSiglaPartido(updated.getSiglaPartido());
            existing.setSiglaUF(updated.getSiglaUF());
            existing.setUrlFoto(updated.getUrlFoto());
            parlamentarRepository.save(existing);
            logger.info("Deputado atualizado: {}", existing.getId());
        }
    }

    private void updateSenadorIfNeeded(Parlamentar existing, Parlamentar updated) {
        boolean needsUpdate = !existing.getNome().equals(updated.getNome()) ||
                !existing.getSiglaPartido().equals(updated.getSiglaPartido()) ||
                !existing.getSiglaUF().equals(updated.getSiglaUF()) ||
                !existing.getUrlFoto().equals(updated.getUrlFoto());

        if (needsUpdate) {
            existing.setNome(updated.getNome());
            existing.setSiglaPartido(updated.getSiglaPartido());
            existing.setSiglaUF(updated.getSiglaUF());
            existing.setUrlFoto(updated.getUrlFoto());
            parlamentarRepository.save(existing);
            logger.info("Senador atualizado: {}", existing.getId());
        }
    }

    @Transactional
    public List<Parlamentar> fetchAndSaveDeputados() {
        logger.info("Buscando deputados da API da Câmara");

        String endpoint = camaraApiBaseUrl + "deputados?ordem=ASC&ordenarPor=nome";
        String response = restTemplate.getForObject(endpoint, String.class);

        JSONObject json = new JSONObject(response);
        JSONArray dados = json.getJSONArray("dados");

        List<Parlamentar> deputados = new ArrayList<>();

        for (int i = 0; i < dados.length(); i++) {
            try {
                JSONObject deputadoJson = dados.getJSONObject(i);
                Parlamentar deputado = mapJsonToDeputado(deputadoJson);

                parlamentarRepository.findById(deputado.getId())
                        .ifPresentOrElse(
                                existing -> updateDeputadoIfNeeded(existing, deputado),
                                () -> parlamentarRepository.save(deputado)
                        );

                deputados.add(deputado);

            } catch (Exception e) {
                logger.error("Erro ao processar deputado no índice {}: {}", i, e.getMessage());
            }
        }

        logger.info("Total de {} deputados processados", deputados.size());
        return deputados;
    }

    @Transactional
    public List<Parlamentar> fetchAndSaveSenadores() {
        logger.info("Buscando senadores da API do Senado");

        String endpoint = senadoApiBaseUrl + "senador/lista/atual";
        String response = restTemplate.getForObject(endpoint, String.class);

        List<Parlamentar> senadores = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(response)));

            NodeList parlamentarList = document.getElementsByTagName("Parlamentar");

            for (int i = 0; i < parlamentarList.getLength(); i++) {
                try {
                    Element parlamentarElement = (Element) parlamentarList.item(i);
                    Parlamentar senador = mapXmlToSenador(parlamentarElement);

                    parlamentarRepository.findById(senador.getId())
                            .ifPresentOrElse(
                                    existing -> updateSenadorIfNeeded(existing, senador),
                                    () -> parlamentarRepository.save(senador)
                            );

                    senadores.add(senador);

                } catch (Exception e) {
                    logger.error("Erro ao processar senador no índice {}: {}", i, e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Erro ao parsear XML de senadores", e);
        }

        logger.info("Total de {} senadores processados", senadores.size());
        return senadores;
    }

    private Parlamentar mapJsonToDeputado(JSONObject deputadoJson) {
        Parlamentar deputado = new Deputado();
        deputado.setId(String.valueOf(deputadoJson.getInt("id")));
        deputado.setNome(deputadoJson.getString("nome"));
        deputado.setSiglaPartido(deputadoJson.getString("siglaPartido"));
        deputado.setSiglaUF(deputadoJson.getString("siglaUf"));
        deputado.setUrlFoto(deputadoJson.getString("urlFoto"));
        deputado.setCasa(Casa.CAMARA);
        deputado.setEmExercicio(true);
        return deputado;
    }

    private Parlamentar mapXmlToSenador(Element parlamentarElement) {
        Parlamentar senador = new Senador();

        Element identificacao = (Element) parlamentarElement
                .getElementsByTagName("IdentificacaoParlamentar").item(0);

        senador.setId(getElementTextContent(identificacao, "CodigoParlamentar"));
        senador.setNome(getElementTextContent(identificacao, "NomeParlamentar"));
        senador.setSiglaPartido(getElementTextContent(identificacao, "SiglaPartidoParlamentar"));
        senador.setSiglaUF(getElementTextContent(identificacao, "UfParlamentar"));
        senador.setUrlFoto(getElementTextContent(identificacao, "UrlFotoParlamentar"));
        senador.setCasa(Casa.SENADO);
        senador.setEmExercicio(true);

        return senador;
    }

    private String getElementTextContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    @Cacheable("searchTerms")
    public List<String> getSearchTerms() {
        if (searchTerms == null) {
            loadSpecificTerms();
        }
        return searchTerms;
    }
    private void loadSpecificTerms() {
        try {
            List<String> termos = carregarTermosDeArquivo(specificTermsFile);

            if (termos.isEmpty()) {
                logger.warn("Nenhum termo específico carregado. Usando termos padrão.");
                termos = obterTermosPadrao();
            }

            this.searchTerms = termos;
            logger.info("Carregados {} termos específicos", searchTerms.size());

        } catch (Exception e) {
            logger.error("Erro ao carregar termos para pesquisa: {}", e.getMessage());
            this.searchTerms = obterTermosPadrao();
        }
    }

    private List<String> carregarTermosDeArquivo(String nomeArquivo) throws IOException {
        List<String> termos = new ArrayList<>();

        Path caminhoArquivo = Paths.get(nomeArquivo);

        if (Files.exists(caminhoArquivo)) {
            logger.debug("Carregando termos do arquivo: {}", caminhoArquivo);
            termos = Files.readAllLines(caminhoArquivo, StandardCharsets.UTF_8);
        } else {
            logger.debug("Arquivo não encontrado no filesystem. Tentando classpath: {}", nomeArquivo);
            termos = carregarTermosDoClasspath(nomeArquivo);
        }

        return termos.stream()
                .map(String::trim)
                .filter(termo -> !termo.isEmpty() && !termo.startsWith("#"))
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> carregarTermosDoClasspath(String nomeArquivo) throws IOException {
        List<String> termos = new ArrayList<>();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(nomeArquivo)) {
            if (is == null) {
                logger.warn("Arquivo não encontrado no classpath: {}", nomeArquivo);
                return termos;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    termos.add(linha);
                }
            }
        }

        return termos;
    }

    /**
     * Retorna termos padrão caso o arquivo não seja encontrado
     */
    private List<String> obterTermosPadrao() {
        return List.of(
                "defesa",
                "segurança",
                "militar",
                "forças armadas",
                "exército",
                "marinha",
                "aeronáutica",
                "armamento",
                "equipamento militar",
                "orçamento defesa",
                "pessoal militar",
                "previdência militar",
                "remuneração militar",
                "carreira militar",
                "defesa nacional",
                "soberania",
                "fronteiras",
                "segurança pública",
                "inteligência",
                "cibersegurança"
        );
    }

        @Cacheable("parlamentarianInfo")
    public Parlamentar getParlamentarianInfo(String id) {
        return parlamentarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parlamentar não encontrado"));
    }

    @Cacheable("propositionsByParlamentarian")
    public List<Proposicao> getPropositionsByParlamentarian(Long parlamentarId) {
        return proposicaoRepository.findByIdDeputadoAutor(parlamentarId);
    }

    public String getPositionAboutSpecificThemes(String parlamentarianId) {
        List<String> specificTermsRelatedPropositionIds = getThemeRelatedPropositionIds(parlamentarianId);
        List<Votacao> specificTermsRelatedVotings = votingRepository.findThemeRelatedVotingsByParlamentarId(
                parlamentarianId, specificTermsRelatedPropositionIds);

        int totalVotes = specificTermsRelatedVotings.size();
        if (totalVotes == 0) {
            return "INSUFICIENTE";
        }

        long favorableVotes = specificTermsRelatedVotings.stream()
                .filter(v -> "Sim".equals(v.getVoto()))
                .count();

        double favorablePercentage = (double) favorableVotes / totalVotes;

        if (favorablePercentage > 0.7) {
            return "PRO";
        } else if (favorablePercentage < 0.3) {
            return "CONTRA";
        } else {
            return "NEUTRO";
        }
    }

    private List<String> getThemeRelatedPropositionIds(String parlamentarianId) {
        List<Votacao> allVotings = votingRepository.findByParlamentarId(parlamentarianId);

        return allVotings.stream()
                .filter(voting -> "Sim".equals(voting.getVoto()))
                .map(Votacao::getProposicaoId)
                .distinct()
                .filter(this::isPropositionThemeRelated)
                .collect(Collectors.toList());
    }

    private boolean isPropositionThemeRelated(String propositionId) {
        return proposicaoRepository.findById(propositionId)
                .map(proposition -> {
                    String lowerCaseEmenta = proposition.getEmenta().toLowerCase();
                    return searchTerms.stream()
                            .anyMatch(term -> lowerCaseEmenta.contains(term.toLowerCase()));
                })
                .orElse(false);
    }

    @Cacheable("allParlamentarians")
    public Page<Parlamentar> getAllParlamentarians(Pageable pageable) {
        return parlamentarRepository.findAll(pageable);
    }

}