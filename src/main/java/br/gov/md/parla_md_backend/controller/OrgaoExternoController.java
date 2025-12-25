package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.OrgaoExterno;
import br.gov.md.parla_md_backend.domain.dto.AtualizarOrgaoExternoDTO;
import br.gov.md.parla_md_backend.domain.dto.CriarOrgaoExternoDTO;
import br.gov.md.parla_md_backend.domain.dto.OrgaoExternoDTO;
import br.gov.md.parla_md_backend.domain.dto.PosicionamentoDTO;
import br.gov.md.parla_md_backend.domain.enums.StatusPosicionamento;
import br.gov.md.parla_md_backend.service.OrgaoExternoService;
import br.gov.md.parla_md_backend.service.PosicionamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.List;

/**
 * Controller para gerenciamento de órgãos externos
 */
@Slf4j
@RestController
@RequestMapping("/api/orgaos-externos")
@RequiredArgsConstructor
@Tag(name = "Órgãos Externos", description = "Endpoints para gerenciamento de órgãos externos (Ministérios e Forças Armadas)")
@SecurityRequirement(name = "bearer-jwt")
public class OrgaoExternoController {

    private final OrgaoExternoService orgaoExternoService;
    private final PosicionamentoService posicionamentoService;

    /**
     * Cria novo órgão externo
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar órgão externo",
            description = "Cadastra novo órgão externo no sistema")
    public ResponseEntity<OrgaoExternoDTO> criar(
            @Valid @RequestBody CriarOrgaoExternoDTO dto) {

        log.info("Criando órgão externo: {}", dto.sigla());

        OrgaoExternoDTO orgao = orgaoExternoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(orgao);
    }

    /**
     * Atualiza órgão externo
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar órgão externo",
            description = "Atualiza dados de órgão externo")
    public ResponseEntity<OrgaoExternoDTO> atualizar(
            @PathVariable String id,
            @Valid @RequestBody AtualizarOrgaoExternoDTO dto) {

        log.info("Atualizando órgão externo: {}", id);

        OrgaoExternoDTO orgao = orgaoExternoService.atualizar(id, dto);
        return ResponseEntity.ok(orgao);
    }

    /**
     * Busca órgão externo por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(summary = "Buscar órgão externo por ID")
    public ResponseEntity<OrgaoExternoDTO> buscarPorId(@PathVariable String id) {
        log.debug("Buscando órgão externo: {}", id);

        OrgaoExternoDTO orgao = orgaoExternoService.buscarPorId(id);
        return ResponseEntity.ok(orgao);
    }

    /**
     * Busca órgão externo por sigla
     */
    @GetMapping("/sigla/{sigla}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(summary = "Buscar órgão externo por sigla")
    public ResponseEntity<OrgaoExternoDTO> buscarPorSigla(@PathVariable String sigla) {
        log.debug("Buscando órgão externo por sigla: {}", sigla);

        OrgaoExternoDTO orgao = orgaoExternoService.buscarPorSigla(sigla);
        return ResponseEntity.ok(orgao);
    }

    /**
     * Lista todos os órgãos externos com paginação
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(summary = "Listar todos os órgãos externos",
            description = "Lista todos os órgãos externos cadastrados com paginação")
    public ResponseEntity<Page<OrgaoExternoDTO>> listar(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.debug("Listando órgãos externos - página: {}", pageable.getPageNumber());

        Page<OrgaoExternoDTO> orgaos = orgaoExternoService.listar(pageable);
        return ResponseEntity.ok(orgaos);
    }

    /**
     * Lista apenas órgãos externos ativos
     */
    @GetMapping("/ativos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(summary = "Listar órgãos externos ativos",
            description = "Lista apenas os órgãos externos com status ativo")
    public ResponseEntity<List<OrgaoExternoDTO>> listarAtivos() {
        log.debug("Listando órgãos externos ativos");

        List<OrgaoExternoDTO> orgaos = orgaoExternoService.listarAtivos();
        return ResponseEntity.ok(orgaos);
    }

    /**
     * Busca órgãos externos por nome
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(summary = "Buscar órgãos por nome",
            description = "Busca órgãos externos por nome (parcial, case-insensitive)")
    public ResponseEntity<List<OrgaoExternoDTO>> buscarPorNome(
            @RequestParam String nome) {

        log.debug("Buscando órgãos externos por nome: {}", nome);

        List<OrgaoExternoDTO> orgaos = orgaoExternoService.buscarPorNome(nome);
        return ResponseEntity.ok(orgaos);
    }

    /**
     * Ativa órgão externo
     */
    @PutMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativar órgão externo",
            description = "Ativa órgão externo previamente desativado")
    public ResponseEntity<OrgaoExternoDTO> ativar(@PathVariable String id) {
        log.info("Ativando órgão externo: {}", id);

        OrgaoExternoDTO orgao = orgaoExternoService.ativar(id);
        return ResponseEntity.ok(orgao);
    }

    /**
     * Desativa órgão externo
     */
    @PutMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar órgão externo",
            description = "Desativa órgão externo (não poderá receber novos posicionamentos)")
    public ResponseEntity<OrgaoExternoDTO> desativar(@PathVariable String id) {
        log.info("Desativando órgão externo: {}", id);

        OrgaoExternoDTO orgao = orgaoExternoService.desativar(id);
        return ResponseEntity.ok(orgao);
    }

    /**
     * Adiciona representante ao órgão externo
     */
    @PostMapping("/{id}/representantes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Adicionar representante",
            description = "Adiciona novo representante ao órgão externo")
    public ResponseEntity<OrgaoExternoDTO> adicionarRepresentante(
            @PathVariable String id,
            @Valid @RequestBody RepresentanteDTO dto) {

        log.info("Adicionando representante ao órgão externo: {}", id);

        OrgaoExterno.Representante representante = OrgaoExterno.Representante.builder()
                .nome(dto.nome())
                .cargo(dto.cargo())
                .email(dto.email())
                .telefone(dto.telefone())
                .principal(dto.principal())
                .build();

        OrgaoExternoDTO orgao = orgaoExternoService.adicionarRepresentante(id, representante);
        return ResponseEntity.ok(orgao);
    }

    /**
     * Remove representante do órgão externo
     */
    @DeleteMapping("/{id}/representantes/{index}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover representante",
            description = "Remove representante do órgão externo pelo índice")
    public ResponseEntity<OrgaoExternoDTO> removerRepresentante(
            @PathVariable String id,
            @PathVariable int index) {

        log.info("Removendo representante {} do órgão externo: {}", index, id);

        OrgaoExternoDTO orgao = orgaoExternoService.removerRepresentante(id, index);
        return ResponseEntity.ok(orgao);
    }

    /**
     * Deleta órgão externo
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar órgão externo",
            description = "Remove permanentemente órgão externo do sistema")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        log.info("Deletando órgão externo: {}", id);

        orgaoExternoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Busca posicionamentos do órgão externo
     */
    @GetMapping("/{id}/posicionamentos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'EXTERNO')")
    @Operation(summary = "Listar posicionamentos do órgão",
            description = "Lista todos os posicionamentos solicitados ao órgão")
    public ResponseEntity<Page<PosicionamentoDTO>> buscarPosicionamentos(
            @PathVariable String id,
            @PageableDefault(size = 20, sort = "dataSolicitacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando posicionamentos do órgão: {}", id);

        Page<PosicionamentoDTO> posicionamentos = posicionamentoService.buscarPendentesPorOrgao(id, pageable);
        return ResponseEntity.ok(posicionamentos);
    }

    /**
     * Busca estatísticas do órgão externo
     */
    @GetMapping("/{id}/estatisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Estatísticas do órgão externo",
            description = "Retorna estatísticas de posicionamentos do órgão")
    public ResponseEntity<EstatisticasOrgaoDTO> buscarEstatisticas(
            @PathVariable String id) {

        log.debug("Buscando estatísticas do órgão externo: {}", id);

        OrgaoExternoDTO orgao = orgaoExternoService.buscarPorId(id);

        // Buscar todos os posicionamentos do órgão (sem paginação para cálculo)
        Page<PosicionamentoDTO> page = posicionamentoService.buscarPendentesPorOrgao(
                id,
                Pageable.unpaged()
        );

        List<PosicionamentoDTO> todosPosicionamentos = page.getContent();

        long totalPosicionamentos = todosPosicionamentos.size();
        long posicionamentosPendentes = todosPosicionamentos.stream()
                .filter(p -> p.getStatus() == StatusPosicionamento.PENDENTE)
                .count();
        long posicionamentosRecebidos = todosPosicionamentos.stream()
                .filter(p -> p.getStatus() == StatusPosicionamento.RECEBIDO)
                .count();
        long posicionamentosConsolidados = todosPosicionamentos.stream()
                .filter(p -> p.getStatus() == StatusPosicionamento.CONSOLIDADO)
                .count();

        EstatisticasOrgaoDTO estatisticas = new EstatisticasOrgaoDTO(
                orgao.id(),
                orgao.nome(),
                orgao.sigla(),
                orgao.representantes() != null ? orgao.representantes().size() : 0,
                orgao.ativo(),
                totalPosicionamentos,
                posicionamentosPendentes,
                posicionamentosRecebidos,
                posicionamentosConsolidados
        );

        return ResponseEntity.ok(estatisticas);
    }

    /**
     * Busca dashboard geral de órgãos externos
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Dashboard de órgãos externos",
            description = "Retorna visão geral de todos os órgãos externos")
    public ResponseEntity<DashboardOrgaosDTO> buscarDashboard() {
        log.debug("Buscando dashboard de órgãos externos");

        Page<OrgaoExternoDTO> page = orgaoExternoService.listar(Pageable.unpaged());
        List<OrgaoExternoDTO> todosOrgaos = page.getContent();

        long total = todosOrgaos.size();
        long ativos = todosOrgaos.stream().filter(OrgaoExternoDTO::ativo).count();
        long inativos = total - ativos;
        long comRepresentante = todosOrgaos.stream()
                .filter(o -> o.representantes() != null && !o.representantes().isEmpty())
                .count();
        long semRepresentante = total - comRepresentante;

        DashboardOrgaosDTO dashboard = new DashboardOrgaosDTO(
                total,
                ativos,
                inativos,
                comRepresentante,
                semRepresentante
        );

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Busca relatório detalhado de posicionamentos por órgão
     */
    @GetMapping("/{id}/relatorio")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Relatório detalhado do órgão",
            description = "Gera relatório completo com dados do órgão e histórico de posicionamentos")
    public ResponseEntity<RelatorioOrgaoDTO> gerarRelatorio(@PathVariable String id) {
        log.debug("Gerando relatório do órgão externo: {}", id);

        OrgaoExternoDTO orgao = orgaoExternoService.buscarPorId(id);

        Page<PosicionamentoDTO> page = posicionamentoService.buscarPendentesPorOrgao(
                id,
                Pageable.unpaged()
        );

        List<PosicionamentoDTO> posicionamentos = page.getContent();

        long totalPosicionamentos = posicionamentos.size();
        long pendentes = posicionamentos.stream()
                .filter(p -> p.getStatus() == StatusPosicionamento.PENDENTE)
                .count();
        long recebidos = posicionamentos.stream()
                .filter(p -> p.getStatus() == StatusPosicionamento.RECEBIDO)
                .count();
        long consolidados = posicionamentos.stream()
                .filter(p -> p.getStatus() == StatusPosicionamento.CONSOLIDADO)
                .count();
        long prazoVencido = posicionamentos.stream()
                .filter(p -> p.getPrazo() != null && p.getStatus() == StatusPosicionamento.PENDENTE)
                .filter(p -> java.time.LocalDateTime.now().isAfter(p.getPrazo()))
                .count();
        long atendidoPrazo = posicionamentos.stream()
                .filter(p -> p.getDataRecebimento() != null)
                .filter(PosicionamentoDTO::isAtendidoPrazo)
                .count();

        double taxaResposta = totalPosicionamentos > 0
                ? ((double) (recebidos + consolidados) / totalPosicionamentos) * 100
                : 0.0;

        double taxaCumprimentoPrazo = (recebidos + consolidados) > 0
                ? ((double) atendidoPrazo / (recebidos + consolidados)) * 100
                : 0.0;

        RelatorioOrgaoDTO relatorio = new RelatorioOrgaoDTO(
                orgao.id(),
                orgao.nome(),
                orgao.sigla(),
                orgao.tipo(),
                orgao.emailOficial(),
                orgao.telefone(),
                orgao.representantes() != null ? orgao.representantes().size() : 0,
                orgao.ativo(),
                totalPosicionamentos,
                pendentes,
                recebidos,
                consolidados,
                prazoVencido,
                atendidoPrazo,
                taxaResposta,
                taxaCumprimentoPrazo
        );

        return ResponseEntity.ok(relatorio);
    }

    /**
     * DTO para adicionar representante
     */
    public record RepresentanteDTO(
            @jakarta.validation.constraints.NotBlank(message = "Nome é obrigatório")
            String nome,

            @jakarta.validation.constraints.NotBlank(message = "Cargo é obrigatório")
            String cargo,

            @jakarta.validation.constraints.Email(message = "E-mail inválido")
            String email,

            String telefone,
            boolean principal
    ) {}

    /**
     * DTO para estatísticas do órgão externo
     */
    public record EstatisticasOrgaoDTO(
            String id,
            String nome,
            String sigla,
            int totalRepresentantes,
            boolean ativo,
            long totalPosicionamentos,
            long posicionamentosPendentes,
            long posicionamentosRecebidos,
            long posicionamentosConsolidados
    ) {}

    /**
     * DTO para dashboard de órgãos externos
     */
    public record DashboardOrgaosDTO(
            long total,
            long ativos,
            long inativos,
            long comRepresentante,
            long semRepresentante
    ) {}

    /**
     * DTO para relatório detalhado do órgão
     */
    public record RelatorioOrgaoDTO(
            String id,
            String nome,
            String sigla,
            String tipo,
            String emailOficial,
            String telefone,
            int totalRepresentantes,
            boolean ativo,
            long totalPosicionamentos,
            long posicionamentosPendentes,
            long posicionamentosRecebidos,
            long posicionamentosConsolidados,
            long posicionamentosPrazoVencido,
            long posicionamentosAtendidoPrazo,
            double taxaResposta,
            double taxaCumprimentoPrazo
    ) {}
}