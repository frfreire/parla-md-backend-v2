package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Proposicao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CamaraAPIService {

    private final RestTemplate restTemplate;

    @Value("${camara.api.url:https://dadosabertos.camara.leg.br/api/v2}")
    private String apiUrl;

    public Proposicao buscarProposicao(Long id) {
        try {
            String url = String.format("%s/proposicoes/%d", apiUrl, id);

            log.info("Buscando proposição {} na API da Câmara", id);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("dados")) {
                Map<String, Object> dados = (Map<String, Object>) response.get("dados");
                return mapearProposicao(dados);
            }

            log.warn("Proposição {} não encontrada", id);
            return null;

        } catch (Exception e) {
            log.error("Erro ao buscar proposição {} na API da Câmara", id, e);
            throw new RuntimeException("Erro ao buscar proposição da Câmara", e);
        }
    }

    private Proposicao mapearProposicao(Map<String, Object> dados) {
        Proposicao proposicao = new Proposicao();

        if (dados.get("id") != null) {
            proposicao.setIdCamara(((Number) dados.get("id")).longValue());
        }

        proposicao.setSiglaTipo((String) dados.get("siglaTipo"));

        if (dados.get("numero") != null) {
            proposicao.setNumero(String.valueOf(((Number) dados.get("numero")).intValue()));
        }

        if (dados.get("ano") != null) {
            proposicao.setAno(((Number) dados.get("ano")).intValue());
        }

        proposicao.setEmenta((String) dados.get("ementa"));
        proposicao.setEmentaDetalhada((String) dados.get("keywords"));

        return proposicao;
    }

    public boolean verificarDisponibilidade() {
        try {
            String url = apiUrl + "/proposicoes?itens=1";
            restTemplate.getForObject(url, Map.class);
            return true;
        } catch (Exception e) {
            log.error("API da Câmara indisponível", e);
            return false;
        }
    }
}
