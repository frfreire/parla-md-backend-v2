package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.AreaImpacto;
import br.gov.md.parla_md_backend.repository.IAreaImpactoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AreaImpactoService {

    private final IAreaImpactoRepository areaImpactoRepository;

    @Autowired
    public AreaImpactoService(IAreaImpactoRepository areaImpactoRepository) {
        this.areaImpactoRepository = areaImpactoRepository;
    }

    public List<AreaImpacto> lisarTodasAreasImpacto() {
        return areaImpactoRepository.findAll();
    }

    public Optional<AreaImpacto> getImpactAreaById(String id) {
        return areaImpactoRepository.findById(id);
    }

    public AreaImpacto criarAreaImpacto(AreaImpacto areaImpacto) {
        return areaImpactoRepository.save(areaImpacto);
    }

    public Optional<AreaImpacto> updateAreaImpacto(String id, AreaImpacto areaImpactoDetails) {
        return areaImpactoRepository.findById(id)
                .map(existingArea -> {
                    existingArea.setNome(areaImpactoDetails.getNome());
                    existingArea.setKeywords(areaImpactoDetails.getKeywords());
                    return areaImpactoRepository.save(existingArea);
                });
    }

    public boolean AreaImpacto(String id) {
        return areaImpactoRepository.findById(id)
                .map(impactArea -> {
                    areaImpactoRepository.delete(impactArea);
                    return true;
                })
                .orElse(false);
    }

    public Map<String, Double> analyzeImpact(String text) {
        List<AreaImpacto> areaImpactos = areaImpactoRepository.findAll();
        Map<String, Double> impactos = new HashMap<>();

        for (AreaImpacto area : areaImpactos) {
            double impact = calcularImpacto(text, area.getKeywords());
            impactos.put(area.getNome(), impact);
        }

        return impactos;
    }

    private double calcularImpacto(String text, List<String> keywords) {
        long matchCount = keywords.stream()
                .filter(keyword -> text.toLowerCase().contains(keyword.toLowerCase()))
                .count();
        return (double) matchCount / keywords.size();
    }
}
