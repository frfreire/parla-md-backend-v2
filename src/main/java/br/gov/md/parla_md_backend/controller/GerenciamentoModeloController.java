package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.dto.ProposicaoDTO;
import br.gov.md.parla_md_backend.service.ai.PredictionService;
import org.nd4j.evaluation.classification.Evaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/model")
public class GerenciamentoModeloController {

    private final PredictionService predictionService;

    @Autowired
    public GerenciamentoModeloController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping("/train")
    public ResponseEntity<?> trainModel() {
        try {
            predictionService.onDemandTraining();
            return ResponseEntity.ok("Treinamento do modelo concluído e modelo salvo.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro durante o treinamento do modelo: " + e.getMessage());
        }
    }

    @GetMapping("/evaluate")
    public ResponseEntity<?> evaluateModel() {
        try {
            Evaluation eval = predictionService.onDemandEvaluation();
            return ResponseEntity.ok(eval.stats());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Modelo não treinado. Por favor, treine o modelo primeiro.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro durante a avaliação do modelo: " + e.getMessage());
        }
    }

    @PostMapping("/cross-validate")
    public ResponseEntity<?> performCrossValidation(@RequestParam(defaultValue = "5") int folds) {
        try {
            predictionService.onDemandCrossValidation(folds);
            return ResponseEntity.ok("Validação cruzada concluída. Verifique os logs para resultados detalhados.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro durante a validação cruzada: " + e.getMessage());
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveModel() {
        try {
            predictionService.saveModel();
            return ResponseEntity.ok("Modelo salvo com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao salvar o modelo: " + e.getMessage());
        }
    }

    @PostMapping("/load")
    public ResponseEntity<?> loadModel() {
        try {
            predictionService.loadModel();
            return ResponseEntity.ok("Modelo carregado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao carregar o modelo: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getModelStatus() {
        try {
            boolean isModelTrained = predictionService.isModelTrained();
            return ResponseEntity.ok("Status do modelo: " + (isModelTrained ? "Treinado" : "Não treinado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao verificar o status do modelo: " + e.getMessage());
        }
    }

    @PostMapping("/predict")
    public ResponseEntity<?> predictApproval(@RequestBody ProposicaoDTO proposicaoDTO) {
        try {
            double probability = predictionService.predictApprovalProbability(proposicaoDTO.toProposition());
            return ResponseEntity.ok("Probabilidade de aprovação: " + probability);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Modelo não treinado. Por favor, treine o modelo primeiro.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro durante a previsão: " + e.getMessage());
        }
    }
}