package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.dto.MateriaDTO;
import br.gov.md.parla_md_backend.domain.enums.TipoMateria;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import br.gov.md.parla_md_backend.service.SenadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para gerenciamento de Matérias Legislativas do Senado Federal.
 *
 * <p>Fornece endpoints para:</p>
 * <ul>
 *   <li>Consulta de matérias por diversos filtros</li>
 *   <li>Sincronização com a API do Senado</li>
 *   <li>Gerenciamento do ciclo de vida das matérias</li>
 *   <li>Status de atualização</li>
 * </ul>
 *
 * @author Parla-MD
 * @version 1.0
 * @since 2024-12
 */
@Slf4j
@RestController
@RequestMapping("/api/materias")
@RequiredArgsConstructor
@Tag(name = "Matérias Legislativas", description = "Endpoints para gerenciamento de matérias do Senado Federal")
@SecurityRequirement(name = "bearer-jwt")
public class MateriaController {

    private final SenadoService senadoService;

    // ==================== ENDPOINTS DE CONSULTA ====================

    /**
     * Lista todas as matérias com paginação.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(
            summary = "Listar matérias",
            description = "Lista todas as matérias legislativas do Senado com paginação"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de matérias retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso")
    })
    public ResponseEntity<Page<MateriaDTO>> listarMaterias(
            @PageableDefault(size = 20, sort = "dataApresentacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Listando matérias - página: {}", pageable.getPageNumber());
        Page<MateriaDTO> materias = senadoService.listarTodas(pageable);
        return ResponseEntity.ok(materias);
    }

    /**
     * Busca matéria por ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(
            summary = "Buscar matéria por ID",
            description = "Retorna uma matéria específica pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matéria encontrada"),
            @ApiResponse(responseCode = "404", description = "Matéria não encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<MateriaDTO> buscarPorId(
            @Parameter(description = "ID da matéria", required = true)
            @PathVariable String id) {

        log.debug("Buscando matéria por ID: {}", id);
        MateriaDTO materia = senadoService.buscarPorId(id);
        return ResponseEntity.ok(materia);
    }

    /**
     * Busca matéria por código do Senado.
     */
    @GetMapping("/codigo/{codigoMateria}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(
            summary = "Buscar matéria por código",
            description = "Retorna uma matéria pelo código oficial do Senado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matéria encontrada"),
            @ApiResponse(responseCode = "404", description = "Matéria não encontrada")
    })
    public ResponseEntity<MateriaDTO> buscarPorCodigo(
            @Parameter(description = "Código da matéria no Senado", required = true)
            @PathVariable Long codigoMateria) {

        log.debug("Buscando matéria por código: {}", codigoMateria);
        MateriaDTO materia = senadoService.buscarPorCodigo(codigoMateria);
        return ResponseEntity.ok(materia);
    }

    /**
     * Busca matérias por ano.
     */
    @GetMapping("/ano/{ano}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(
            summary = "Buscar matérias por ano",
            description = "Lista todas as matérias de um determinado ano"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de matérias do ano"),
            @ApiResponse(responseCode = "400", description = "Ano inválido")
    })
    public ResponseEntity<List<MateriaDTO>> buscarPorAno(
            @Parameter(description = "Ano das matérias", example = "2024", required = true)
            @PathVariable int ano) {

        log.debug("Buscando matérias do ano: {}", ano);

        // Validar ano
        int anoAtual = Year.now().getValue();
        if (ano < 1900 || ano > anoAtual + 1) {
            return ResponseEntity.badRequest().build();
        }

        List<MateriaDTO> materias = senadoService.buscarPorAno(ano);
        return ResponseEntity.ok(materias);
    }

    /**
     * Busca matérias por tipo.
     */
    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(
            summary = "Buscar matérias por tipo",
            description = "Lista matérias de um determinado tipo (PLS, PEC, MPV, etc.)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de matérias do tipo"),
            @ApiResponse(responseCode = "400", description = "Tipo inválido")
    })
    public ResponseEntity<Page<MateriaDTO>> buscarPorTipo(
            @Parameter(description = "Tipo da matéria", required = true)
            @PathVariable TipoMateria tipo,
            @PageableDefault(size = 20, sort = "dataApresentacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando matérias do tipo: {}", tipo);
        Page<MateriaDTO> materias = senadoService.buscarPorTipo(tipo, pageable);
        return ResponseEntity.ok(materias);
    }

    /**
     * Busca matérias em tramitação.
     */
    @GetMapping("/em-tramitacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(
            summary = "Buscar matérias em tramitação",
            description = "Lista todas as matérias que estão atualmente em tramitação"
    )
    public ResponseEntity<Page<MateriaDTO>> buscarEmTramitacao(
            @PageableDefault(size = 20, sort = "dataApresentacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando matérias em tramitação");
        Page<MateriaDTO> materias = senadoService.buscarEmTramitacao(pageable);
        return ResponseEntity.ok(materias);
    }

    /**
     * Busca matérias por status de triagem.
     */
    @GetMapping("/triagem/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(
            summary = "Buscar matérias por status de triagem",
            description = "Lista matérias filtradas pelo status de triagem interna"
    )
    public ResponseEntity<Page<MateriaDTO>> buscarPorStatusTriagem(
            @Parameter(description = "Status de triagem", required = true)
            @PathVariable StatusTriagem status,
            @PageableDefault(size = 20, sort = "dataApresentacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando matérias com status de triagem: {}", status);
        Page<MateriaDTO> materias = senadoService.buscarPorStatusTriagem(status, pageable);
        return ResponseEntity.ok(materias);
    }

    /**
     * Busca matérias apresentadas recentemente.
     */
    @GetMapping("/recentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(
            summary = "Buscar matérias recentes",
            description = "Lista matérias apresentadas nos últimos N dias (padrão: 30)"
    )
    public ResponseEntity<List<MateriaDTO>> buscarRecentes(
            @Parameter(description = "Número de dias", example = "30")
            @RequestParam(defaultValue = "30") int dias) {

        log.debug("Buscando matérias dos últimos {} dias", dias);
        LocalDate dataLimite = LocalDate.now().minusDays(dias);
        List<MateriaDTO> materias = senadoService.buscarApresentadasApos(dataLimite);
        return ResponseEntity.ok(materias);
    }

    // ==================== ENDPOINTS DE SINCRONIZAÇÃO ====================

    /**
     * Sincroniza matérias com a API do Senado.
     */
    @PostMapping("/sincronizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Sincronizar matérias",
            description = "Busca e salva matérias da API de Dados Abertos do Senado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sincronização concluída"),
            @ApiResponse(responseCode = "500", description = "Erro na sincronização"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<Map<String, Object>> sincronizarMaterias(
            @Parameter(description = "Ano das matérias (padrão: ano atual)")
            @RequestParam(required = false) Integer ano,
            @Parameter(description = "Quantidade de itens (padrão: 100)")
            @RequestParam(required = false) Integer itens) {

        int anoFinal = ano != null ? ano : senadoService.getAnoDefault();
        int itensFinal = itens != null ? itens : senadoService.getItensDefault();

        log.info("Iniciando sincronização manual - ano: {}, itens: {}", anoFinal, itensFinal);

        try {
            List<MateriaDTO> materias = senadoService.buscarESalvarMaterias(anoFinal, itensFinal);

            return ResponseEntity.ok(Map.of(
                    "status", "sucesso",
                    "mensagem", "Sincronização concluída",
                    "materiasProcessadas", materias.size(),
                    "ano", anoFinal,
                    "horario", LocalDateTime.now().toString()
            ));

        } catch (Exception e) {
            log.error("Erro na sincronização manual", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "erro",
                    "mensagem", "Falha na sincronização: " + e.getMessage(),
                    "horario", LocalDateTime.now().toString()
            ));
        }
    }

    /**
     * Retorna status da última sincronização.
     */
    @GetMapping("/sincronizacao/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(
            summary = "Status da sincronização",
            description = "Retorna informações sobre a última sincronização realizada"
    )
    public ResponseEntity<Map<String, Object>> statusSincronizacao() {

        return ResponseEntity.ok(Map.of(
                "ultimaAtualizacao", senadoService.obterHorarioUltimaAtualizacao(),
                "anoDefault", senadoService.getAnoDefault(),
                "itensDefault", senadoService.getItensDefault()
        ));
    }

    // ==================== ENDPOINTS DE ATUALIZAÇÃO ====================

    /**
     * Atualiza uma matéria existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Atualizar matéria",
            description = "Atualiza dados de uma matéria existente"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matéria atualizada"),
            @ApiResponse(responseCode = "404", description = "Matéria não encontrada"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<MateriaDTO> atualizarMateria(
            @PathVariable String id,
            @RequestBody MateriaDTO dto) {

        log.info("Atualizando matéria: {}", id);
        MateriaDTO atualizada = senadoService.atualizar(id, dto);
        return ResponseEntity.ok(atualizada);
    }

    /**
     * Atualiza status de triagem de uma matéria.
     */
    @PatchMapping("/{id}/triagem")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(
            summary = "Atualizar triagem",
            description = "Atualiza o status de triagem de uma matéria"
    )
    public ResponseEntity<MateriaDTO> atualizarTriagem(
            @PathVariable String id,
            @Parameter(description = "Novo status de triagem", required = true)
            @RequestParam StatusTriagem status,
            @Parameter(description = "Tema associado")
            @RequestParam(required = false) String tema) {

        log.info("Atualizando triagem da matéria {} para {}", id, status);

        MateriaDTO dto = MateriaDTO.builder()
                .statusTriagem(status)
                .tema(tema)
                .build();

        MateriaDTO atualizada = senadoService.atualizar(id, dto);
        return ResponseEntity.ok(atualizada);
    }

    /**
     * Remove uma matéria.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Remover matéria",
            description = "Remove uma matéria do sistema (apenas ADMIN)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Matéria removida"),
            @ApiResponse(responseCode = "404", description = "Matéria não encontrada"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<Void> removerMateria(@PathVariable String id) {

        log.info("Removendo matéria: {}", id);
        senadoService.remover(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ENDPOINTS PÚBLICOS ====================

    /**
     * Endpoint público para consulta de matérias (sem autenticação).
     */
    @GetMapping("/publico")
    @Operation(
            summary = "Consulta pública de matérias",
            description = "Lista matérias sem necessidade de autenticação (limitado)"
    )
    public ResponseEntity<Page<MateriaDTO>> consultaPublica(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) TipoMateria tipo,
            @PageableDefault(size = 10, sort = "dataApresentacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Consulta pública de matérias - ano: {}, tipo: {}", ano, tipo);

        Page<MateriaDTO> materias;

        if (tipo != null) {
            materias = senadoService.buscarPorTipo(tipo, pageable);
        } else {
            materias = senadoService.listarTodas(pageable);
        }

        return ResponseEntity.ok(materias);
    }
}