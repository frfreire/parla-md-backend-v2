package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.InteracaoLlama;
import br.gov.md.parla_md_backend.domain.dto.RespostaLlamaDTO;
import br.gov.md.parla_md_backend.service.LlamaService;
import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/llama")
@RequiredArgsConstructor
@Tag(name = "Llama IA", description = "Integração com LLM Llama via Ollama")
@SecurityRequirement(name = "bearer-jwt")
public class LlamaController {

    private final LlamaService llamaService;

    @PostMapping("/prompt")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Enviar prompt simples ao Llama",
            description = "Envia um prompt de texto e recebe resposta do LLM"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resposta gerada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Prompt inválido", content = @Content),
            @ApiResponse(responseCode = "503", description = "Llama indisponível", content = @Content)
    })
    public ResponseEntity<RespostaLlamaDTO> enviarPrompt(
            @Valid @RequestBody PromptRequest request) {

        log.info("Recebendo prompt simples");

        RespostaLlamaDTO resposta = llamaService.enviarRequisicao(request.getPrompt());

        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/prompt-avancado")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Enviar prompt avançado ao Llama",
            description = "Envia prompt com contexto de sistema e opção de resposta JSON"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resposta gerada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content),
            @ApiResponse(responseCode = "503", description = "Llama indisponível", content = @Content)
    })
    public ResponseEntity<RespostaLlamaDTO> enviarPromptAvancado(
            @Valid @RequestBody PromptAvancadoRequest request) {

        log.info("Recebendo prompt avançado");

        RespostaLlamaDTO resposta = llamaService.enviarRequisicao(
                request.getPrompt(),
                request.getPromptSistema(),
                request.isRespostaJson()
        );

        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/extrair-json")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Extrair JSON estruturado da resposta",
            description = "Parseia resposta do Llama para objeto tipado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JSON extraído com sucesso"),
            @ApiResponse(responseCode = "400", description = "JSON inválido", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> extrairJson(
            @Valid @RequestBody ExtrairJsonRequest request) {

        log.info("Extraindo JSON de resposta Llama");

        RespostaLlamaDTO resposta = RespostaLlamaDTO.class.cast(request.getResposta());

        Map<String, Object> json = llamaService.extrairJson(
                resposta,
                new TypeReference<Map<String, Object>>() {}
        );

        return ResponseEntity.ok(json);
    }

    @GetMapping("/disponibilidade")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Verificar disponibilidade do Ollama",
            description = "Checa se o serviço Ollama está online e respondendo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status verificado")
    })
    public ResponseEntity<DisponibilidadeResponse> verificarDisponibilidade() {

        log.debug("Verificando disponibilidade do Ollama");

        boolean disponivel = llamaService.verificarDisponibilidade();

        DisponibilidadeResponse response = new DisponibilidadeResponse(
                disponivel,
                disponivel ? "Ollama online" : "Ollama offline"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/historico/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Buscar histórico de interações",
            description = "Retorna histórico de prompts de um usuário"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<List<InteracaoLlama>> buscarHistorico(
            @Parameter(description = "ID do usuário") @PathVariable String usuarioId) {

        log.debug("Buscando histórico de interações: {}", usuarioId);

        List<InteracaoLlama> historico = llamaService.buscarHistorico(usuarioId);

        return ResponseEntity.ok(historico);
    }

    @GetMapping("/falhas-recentes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Buscar falhas recentes",
            description = "Retorna falhas de comunicação com Llama nas últimas horas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Falhas retornadas"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<List<InteracaoLlama>> buscarFalhasRecentes(
            @Parameter(description = "Últimas N horas")
            @RequestParam(defaultValue = "24") int horas) {

        log.debug("Buscando falhas das últimas {} horas", horas);

        List<InteracaoLlama> falhas = llamaService.buscarFalhasRecentes(horas);

        return ResponseEntity.ok(falhas);
    }

    @DeleteMapping("/limpar-expirados")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Limpar registros expirados",
            description = "Remove interações antigas além do TTL configurado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Limpeza executada")
    })
    public ResponseEntity<Void> limparExpirados() {

        log.info("Executando limpeza de registros expirados");

        llamaService.limparExpirados();

        return ResponseEntity.noContent().build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptRequest {
        @NotBlank(message = "Prompt é obrigatório")
        private String prompt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptAvancadoRequest {
        @NotBlank(message = "Prompt é obrigatório")
        private String prompt;

        private String promptSistema;

        private boolean respostaJson = false;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtrairJsonRequest {
        @Valid
        private Object resposta;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisponibilidadeResponse {
        private boolean disponivel;
        private String mensagem;
    }
}