package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.legislativo.Materia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SenadoAPIService {

    private final RestTemplate restTemplate;

    @Value("${senado.api.url:https://legis.senado.leg.br/dadosabertos}")
    private String apiUrl;

    public Materia buscarMateria(String codigoMateria) {
        try {
            String url = String.format("%s/materia/%s", apiUrl, codigoMateria);

            log.info("Buscando matéria {} na API do Senado", codigoMateria);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                return mapearMateria(response);
            }

            log.warn("Matéria {} não encontrada", codigoMateria);
            return null;

        } catch (Exception e) {
            log.error("Erro ao buscar matéria {} na API do Senado", codigoMateria, e);
            throw new RuntimeException("Erro ao buscar matéria do Senado", e);
        }
    }

    private Materia mapearMateria(Map<String, Object> dados) {
        Materia materia = new Materia();

        materia.setCodigoMateria(Long.valueOf((String) dados.get("CodigoMateria")));
        materia.setSiglaSubtipoMateria((String) dados.get("SiglaSubtipoMateria"));

        if (dados.get("NumeroMateria") != null) {
            materia.setNumero(String.valueOf(dados.get("NumeroMateria")));
        }

        if (dados.get("AnoMateria") != null) {
            materia.setAnoMateria(String.valueOf(((Number) dados.get("AnoMateria")).intValue()));
        }

        materia.setEmentaMateria((String) dados.get("EmentaMateria"));

        return materia;
    }

    public boolean verificarDisponibilidade() {
        try {
            String url = apiUrl + "/materia/pesquisa/lista?sigla=PL";
            restTemplate.getForObject(url, Map.class);
            return true;
        } catch (Exception e) {
            log.error("API do Senado indisponível", e);
            return false;
        }
    }
}
