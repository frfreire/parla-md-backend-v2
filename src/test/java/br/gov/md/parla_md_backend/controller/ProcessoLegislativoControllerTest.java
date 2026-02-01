package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.config.SecurityConfig;
import br.gov.md.parla_md_backend.domain.ProcessoLegislativo;
import br.gov.md.parla_md_backend.domain.dto.CriarProcessoDTO;
import br.gov.md.parla_md_backend.domain.dto.ProcessoLegislativoDTO;
import br.gov.md.parla_md_backend.domain.enums.PrioridadeProcesso;
import br.gov.md.parla_md_backend.domain.enums.StatusProcesso;
import br.gov.md.parla_md_backend.repository.IUsuarioRepository; // IMPORTANTE
import br.gov.md.parla_md_backend.service.ProcessoLegislativoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(ProcessoLegislativoController.class)
@Import(SecurityConfig.class)
class ProcessoLegislativoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProcessoLegislativoService processoService;

    @MockBean
    private IUsuarioRepository usuarioRepository;

    private static final String BASE_URL = "/api/processos";

    private static final LocalDateTime AGORA = LocalDateTime.now();

    private ProcessoLegislativoDTO processoDTO() {
        return ProcessoLegislativoDTO.builder()
                .id("proc-001")
                .numero("PL-2024-001")
                .status(StatusProcesso.INICIADO)
                .numeroPareceresPendentes(0)
                .numeroPosicionamentosPendentes(0)
                .build();
    }

    private ProcessoLegislativo entityProcesso() {
        return ProcessoLegislativo.builder()
                .id("proc-001")
                .numero("PL-2024-001")
                .titulo("Título do Processo")
                .status(StatusProcesso.INICIADO)
                .dataCriacao(AGORA)
                .build();
    }

    private CriarProcessoDTO criarProcessoDTO() {
        return CriarProcessoDTO.builder()
                .numeroProcesso("PL-2024-001")
                .titulo("Título do Processo")
                .setorResponsavel("setor-001")
                .prioridade(PrioridadeProcesso.ALTA)
                .build();
    }

    @Nested
    @DisplayName("Testes de Criação de Processo")
    class CriarProcesso {

        @Test
        @WithMockUser(username = "admin-user", roles = {"ADMIN"})
        void criar_quando_ADMIN_retorna201() throws Exception {
            when(processoService.criar(any(CriarProcessoDTO.class), anyString()))
                    .thenReturn(entityProcesso());

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criarProcessoDTO())))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"ANALISTA"})
        void criar_quando_ANALISTA_retorna403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criarProcessoDTO())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Testes de Busca")
    class BuscarPorId {

        @Test
        @WithMockUser(roles = {"ANALISTA"})
        void buscar_quando_existe_retorna200() throws Exception {
            when(processoService.buscarPorId("proc-001")).thenReturn(processoDTO());

            mockMvc.perform(get(BASE_URL + "/proc-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("proc-001"));
        }
    }

    @Nested
    @DisplayName("Testes de Dashboard por Setor")
    class DashboardSetor {

        @Test
        @WithMockUser(roles = {"GESTOR"})
        void buscar_quando_setorExiste_retornaContagens() throws Exception {
            Page<ProcessoLegislativoDTO> page = new PageImpl<>(List.of(processoDTO()));
            when(processoService.buscarPorSetor(eq("setor-001"), any())).thenReturn(page);

            mockMvc.perform(get(BASE_URL + "/dashboard/setor/setor-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.setorId").value("setor-001"));
        }

        @Test
        @WithMockUser(roles = {"ANALISTA"})
        void buscar_quando_ANALISTA_retorna403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/dashboard/setor/setor-001"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Testes de Meus Processos")
    class MeusProcessos {

        @Test
        @WithMockUser(username = "gestor-user", roles = {"GESTOR"})
        void buscar_quando_GESTOR_retorna200() throws Exception {
            when(processoService.buscarPorGestor("gestor-user"))
                    .thenReturn(List.of(processoDTO()));

            mockMvc.perform(get(BASE_URL + "/meus-processos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value("proc-001"));
        }
    }
}