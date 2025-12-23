package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Parlamentar;
import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.Votacao;
import br.gov.md.parla_md_backend.domain.enums.TipoParlamentar;
import br.gov.md.parla_md_backend.repository.IParlamentarRepository;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import br.gov.md.parla_md_backend.repository.IVotacaoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.io.StringReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParlamentarService {

    private static final Logger logger = LoggerFactory.getLogger(ParlamentarService.class);
    private RestTemplate restTemplate;
    private IParlamentarRepository parlamentarianRepository;
    private IProposicaoRepository propositionRepository;
    private IVotacaoRepository votingRepository;
    private CamaraService camaraService;
    private SenadoService senadoService;
    private List<String> searchTerms;

    @Value("${search.terms.file:classpath:search_terms.txt}")
    private String specificTermsFile;

    @Value("${camara.api.base-url}")
    private String camaraApiBaseUrl;

    @Value("${senado.api.base-url}")
    private String senadoApiBaseUrl;

    @Autowired
    public ParlamentarService(IParlamentarRepository parlamentarianRepository,
                              IProposicaoRepository propositionRepository,
                              IVotacaoRepository votingRepository,
                              RestTemplate restTemplate,
                              CamaraService camaraService,
                              SenadoService senadoService) {
        this.parlamentarianRepository = parlamentarianRepository;
        this.propositionRepository = propositionRepository;
        this.votingRepository = votingRepository;
        this.restTemplate = restTemplate;
        this.camaraService = camaraService;
        this.senadoService = senadoService;
    }

    @PostConstruct
    public void init() {
        try {
            loadSpecificTerms();
        } catch (Exception e) {
            logger.error("Erro durante a inicialização do ParlamentarianService", e);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
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

    public void syncDeputados() {
        logger.info("Iniciando sincronização de deputados");
        try {
            List<Parlamentar> deputados = camaraService.fetchDeputados();
            for (Parlamentar deputado : deputados) {
                try {
                    parlamentarianRepository.findById(deputado.getId())
                            .ifPresentOrElse(
                                    existing -> updateDeputadoIfNeeded(existing, deputado),
                                    () -> parlamentarianRepository.save(deputado)
                            );
                } catch (Exception e) {
                    logger.error("Erro ao processar deputado: {}", deputado.getId(), e);
                }
            }
            logger.info("Sincronização de deputados concluída");
        } catch (Exception e) {
            logger.error("Erro durante a sincronização de deputados", e);
        }
    }

    public void syncSenadores() {
        logger.info("Iniciando sincronização de senadores");
        try {
            List<Parlamentar> senadores = senadoService.fetchSenadores();
            for (Parlamentar senador : senadores) {
                try {
                    parlamentarianRepository.findById(senador.getId())
                            .ifPresentOrElse(
                                    existing -> updateSenadorIfNeeded(existing, senador),
                                    () -> parlamentarianRepository.save(senador)
                            );
                } catch (Exception e) {
                    logger.error("Erro ao processar senador: {}", senador.getId(), e);
                }
            }
            logger.info("Sincronização de senadores concluída");
        } catch (Exception e) {
            logger.error("Erro durante a sincronização de senadores", e);
        }
    }

    private void updateDeputadoIfNeeded(Parlamentar existing, Parlamentar updated) {
        boolean needsUpdate = !existing.getNome().equals(updated.getNome()) ||
                !existing.getPartido().equals(updated.getPartido()) ||
                !existing.getEstado().equals(updated.getEstado()) ||
                !existing.getUrlFoto().equals(updated.getUrlFoto());

        if (needsUpdate) {
            existing.setNome(updated.getNome());
            existing.setPartido(updated.getPartido());
            existing.setEstado(updated.getEstado());
            existing.setUrlFoto(updated.getUrlFoto());
            parlamentarianRepository.save(existing);
            logger.info("Deputado atualizado: {}", existing.getId());
        }
    }

    private void updateSenadorIfNeeded(Parlamentar existing, Parlamentar updated) {
        boolean needsUpdate = !existing.getNome().equals(updated.getNome()) ||
                !existing.getPartido().equals(updated.getPartido()) ||
                !existing.getEstado().equals(updated.getEstado()) ||
                !existing.getUrlFoto().equals(updated.getUrlFoto());

        if (needsUpdate) {
            existing.setNome(updated.getNome());
            existing.setPartido(updated.getPartido());
            existing.setEstado(updated.getEstado());
            existing.setUrlFoto(updated.getUrlFoto());
            parlamentarianRepository.save(existing);
            logger.info("Senador atualizado: {}", existing.getId());
        }
    }

    public List<Parlamentar> fetchAndSaveDeputados() {
        String endpoint = camaraApiBaseUrl + "deputados?ordem=ASC&ordenarPor=nome";
        String response = restTemplate.getForObject(endpoint, String.class);
        JSONObject json = new JSONObject(response);
        JSONArray dados = json.getJSONArray("dados");

        List<Parlamentar> deputados = new ArrayList<>();
        for (int i = 0; i < dados.length(); i++) {
            JSONObject deputadoJson = dados.getJSONObject(i);
            Parlamentar deputado = mapJsonToDeputado(deputadoJson);
            deputados.add(deputado);
            parlamentarianRepository.save(deputado);
        }

        return deputados;
    }

    public List<Parlamentar> fetchAndSaveSenadores() {
        String endpoint = senadoApiBaseUrl + "senador/lista/atual";
        String response = restTemplate.getForObject(endpoint, String.class);

        List<Parlamentar> senadores = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(response)));

            NodeList parlamentarList = document.getElementsByTagName("Parlamentar");

            for (int i = 0; i < parlamentarList.getLength(); i++) {
                Element parlamentarElement = (Element) parlamentarList.item(i);
                Parlamentar senador = mapXmlToSenador(parlamentarElement);
                senadores.add(senador);
                parlamentarianRepository.save(senador);
            }
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
        }

        return senadores;
    }

    private Parlamentar mapJsonToDeputado(JSONObject deputadoJson) {
        Parlamentar deputado = new Parlamentar();
        deputado.setId(String.valueOf(deputadoJson.getInt("id"))); // Convertendo Integer para String
        deputado.setNome(deputadoJson.getString("nome"));
        deputado.setPartido(deputadoJson.getString("siglaPartido"));
        deputado.setEstado(deputadoJson.getString("siglaUf"));
        deputado.setUrlFoto(deputadoJson.getString("urlFoto"));
        deputado.setTipo(TipoParlamentar.DEPUTADO);
        return deputado;
    }
    private Parlamentar mapXmlToSenador(Element parlamentarElement) {
        Parlamentar senador = new Parlamentar();

        Element identificacaoParlamentar = (Element) parlamentarElement.getElementsByTagName("IdentificacaoParlamentar").item(0);

        senador.setId(getElementTextContent(identificacaoParlamentar, "CodigoParlamentar"));
        senador.setNome(getElementTextContent(identificacaoParlamentar, "NomeParlamentar"));
        senador.setPartido(getElementTextContent(identificacaoParlamentar, "SiglaPartidoParlamentar"));
        senador.setEstado(getElementTextContent(identificacaoParlamentar, "UfParlamentar"));
        senador.setUrlFoto(getElementTextContent(identificacaoParlamentar, "UrlFotoParlamentar"));
        senador.setTipo(TipoParlamentar.SENADOR);

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
            searchTerms = camaraService.loadSpecificTerms(specificTermsFile);
            logger.info("Carregados {} termos específicos", searchTerms.size());
        } catch (IOException e) {
            logger.error("Erro ao carregar termos para pesquisa: {}", e.getMessage());
            searchTerms = new ArrayList<>();
        }
    }

    @Cacheable("parlamentarianInfo")
    public Parlamentar getParlamentarianInfo(String id) {
        return parlamentarianRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parlamentar não encontrado"));
    }

    @Cacheable("propositionsByParlamentarian")
    public List<Proposicao> getPropositionsByParlamentarian(String parlamentarianId) {
        return propositionRepository.findByAutorId(parlamentarianId);
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
        return propositionRepository.findById(propositionId)
                .map(proposition -> {
                    String lowerCaseEmenta = proposition.getEmenta().toLowerCase();
                    return searchTerms.stream()
                            .anyMatch(term -> lowerCaseEmenta.contains(term.toLowerCase()));
                })
                .orElse(false);
    }

    @Cacheable("allParlamentarians")
    public Page<Parlamentar> getAllParlamentarians(Pageable pageable) {
        return parlamentarianRepository.findAll(pageable);
    }

}