package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.AreaImpacto;
import br.gov.md.parla_md_backend.service.AreaImpactoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/impact-areas")
public class AreaImpactoController {

    private final AreaImpactoService areaImpactoService;

    @Autowired
    public AreaImpactoController(AreaImpactoService areaImpactoService) {
        this.areaImpactoService = areaImpactoService;
    }

    @GetMapping
    public ResponseEntity<List<AreaImpacto>> getAllImpactAreas() {
        List<AreaImpacto> areaImpactos = areaImpactoService.lisarTodasAreasImpacto();
        return ResponseEntity.ok(areaImpactos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaImpacto> getImpactAreaById(@PathVariable String id) {
        return areaImpactoService.getImpactAreaById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AreaImpacto> createImpactArea(@RequestBody AreaImpacto areaImpacto) {
        AreaImpacto createdArea = areaImpactoService.criarAreaImpacto(areaImpacto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdArea);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AreaImpacto> updateImpactArea(@PathVariable String id, @RequestBody AreaImpacto areaImpactoDetails) {
        return areaImpactoService.updateAreaImpacto(id, areaImpactoDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImpactArea(@PathVariable String id) {
        if (areaImpactoService.AreaImpacto(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
