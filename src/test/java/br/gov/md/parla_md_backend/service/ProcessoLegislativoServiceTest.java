package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.ProcessoLegislativo;
import br.gov.md.parla_md_backend.domain.Setor;
import br.gov.md.parla_md_backend.domain.Usuario;
import br.gov.md.parla_md_backend.domain.dto.CriarProcessoDTO;
import br.gov.md.parla_md_backend.domain.dto.ProcessoLegislativoDTO;
import br.gov.md.parla_md_backend.domain.enums.PrioridadeProcesso;
import br.gov.md.parla_md_backend.domain.enums.StatusProcesso;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProcessoLegislativoServiceTest {

    @InjectMocks
    private ProcessoLegislativoService service;

    @Mock private IProcessoLegislativoRepository processoRepository;
    @Mock private IProposicaoRepository proposicaoRepository;
    @Mock private IMateriaRepository materiaRepository;
    @Mock private ISetorRepository setorRepository;
    @Mock private IUsuarioRepository usuarioRepository;

    private static final LocalDateTime AGORA = LocalDateTime.now();

    // =========================================================================
    // FÁBRICA DE DADOS REAIS (Blindagem contra NullPointerException)
    // =========================================================================

    private ProcessoLegislativo criarEntidadeReal() {
        return ProcessoLegislativo.builder()
                .id("proc-001")
                .numero("PL-2024-001")
                .titulo("Título do Processo Legislativo")
                .descricao("Descrição detalhada")
                .status(StatusProcesso.INICIADO)
                .prioridade(PrioridadeProcesso.ALTA)
                .temaPrincipal("Defesa")
                .setorResponsavelId("setor-001")
                .setorResponsavelNome("Setor Teste")
                .gestorId("user-01")
                .gestorNome("Gestor Teste")
                .proposicaoIds(new ArrayList<>())
                .materiaIds(new ArrayList<>())
                .numeroPareceresPendentes(0)
                .numeroPosicionamentosPendentes(0)
                .dataCriacao(AGORA)
                .dataAtualizacao(AGORA)
                .build();
    }

    private CriarProcessoDTO mockCriarDTO() {
        return CriarProcessoDTO.builder()
                .numeroProcesso("PL-2024-001")
                .titulo("Título do Processo Legislativo")
                .descricao("Descrição")
                .setorResponsavel("setor-001")
                .prioridade(PrioridadeProcesso.ALTA)
                .temaPrincipal("Defesa")
                .requerAnaliseJuridica(true)
                .requerAnaliseOrcamentaria(false)
                .requerConsultaExterna(false)
                .proposicaoIds(new ArrayList<>())
                .materiaIds(new ArrayList<>())
                .build();
    }

    // =========================================================================
    // 1. CRIAR
    // =========================================================================
    @Nested
    @DisplayName("Cenários de Criação")
    class Criar {
        @Test
        void criar_quandoDadosValidos_retornaComStatusIniciado() {
            Usuario user = Usuario.builder().id("user-01").nome("Teste").build();
            Setor setor = Setor.builder().id("setor-001").nome("Setor").build();

            when(processoRepository.existsByNumero(anyString())).thenReturn(false);
            when(usuarioRepository.findById("user-01")).thenReturn(Optional.of(user));
            when(setorRepository.findById("setor-001")).thenReturn(Optional.of(setor));
            when(processoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            ProcessoLegislativo res = service.criar(mockCriarDTO(), "user-01");

            assertEquals(StatusProcesso.INICIADO, res.getStatus());
            verify(processoRepository).save(any());
        }

        @Test
        void criar_quandoProposicaoEMateriaIds_chamaSaveComSucesso() {
            CriarProcessoDTO dto = mockCriarDTO();
            dto.setProposicaoIds(List.of("prop-1"));

            when(usuarioRepository.findById(anyString())).thenReturn(Optional.of(new Usuario()));
            when(setorRepository.findById(anyString())).thenReturn(Optional.of(new Setor()));
            when(processoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            service.criar(dto, "user-01");
            verify(processoRepository).save(any());
        }

        @Test
        void criar_quandoNumeroJaExiste_lancaIllegalArgumentException() {
            when(processoRepository.existsByNumero(anyString())).thenReturn(true);
            assertThrows(IllegalArgumentException.class, () -> service.criar(mockCriarDTO(), "user-01"));
        }

        @Test
        void criar_quandoUsuarioNaoEncontrado_lancaRecursoNaoEncontradoException() {
            when(usuarioRepository.findById(anyString())).thenReturn(Optional.empty());
            assertThrows(RecursoNaoEncontradoException.class, () -> service.criar(mockCriarDTO(), "inv"));
        }

        @Test
        void criar_quandoSetorNaoEncontrado_lancaRecursoNaoEncontradoException() {
            when(usuarioRepository.findById(anyString())).thenReturn(Optional.of(new Usuario()));
            when(setorRepository.findById(anyString())).thenReturn(Optional.empty());
            assertThrows(RecursoNaoEncontradoException.class, () -> service.criar(mockCriarDTO(), "any"));
        }
    }

    // =========================================================================
    // 2. BUSCA
    // =========================================================================
    @Nested
    @DisplayName("Cenários de Busca")
    class Busca {
        @Test
        void buscarPorId_quandoExiste_retornaDTO() {
            when(processoRepository.findById("proc-001")).thenReturn(Optional.of(criarEntidadeReal()));
            ProcessoLegislativoDTO res = service.buscarPorId("proc-001");
            assertEquals("proc-001", res.getId());
        }

        @Test
        void buscarPorId_quandoNaoExiste_lancaRecursoNaoEncontradoException() {
            when(processoRepository.findById(anyString())).thenReturn(Optional.empty());
            assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId("none"));
        }

        @Test
        void buscarPorNumero_quandoExiste_retornaDTO() {
            when(processoRepository.findByNumero("PL-01")).thenReturn(Optional.of(criarEntidadeReal()));
            ProcessoLegislativoDTO res = service.buscarPorNumero("PL-01");
            assertEquals("PL-2024-001", res.getNumero());
        }

        @Test
        void buscarPorNumero_quandoNaoExiste_lancaRecursoNaoEncontradoException() {
            when(processoRepository.findByNumero(anyString())).thenReturn(Optional.empty());
            assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorNumero("none"));
        }
    }

    // =========================================================================
    // 3. LISTAGENS
    // =========================================================================
    @Nested
    @DisplayName("Cenários de Listagem")
    class Listagens {
        @Test
        void listar_delegaParaRepositorio() {
            PageRequest p = PageRequest.of(0, 10);
            when(processoRepository.findAll(p)).thenReturn(new PageImpl<>(List.of(criarEntidadeReal())));
            Page<ProcessoLegislativoDTO> res = service.listar(p);
            assertFalse(res.isEmpty());
        }

        @Test
        void buscarPorStatus_delegaParaRepositorio() {
            when(processoRepository.findByStatus(any(), any())).thenReturn(new PageImpl<>(List.of(criarEntidadeReal())));
            Page<ProcessoLegislativoDTO> res = service.buscarPorStatus(StatusProcesso.INICIADO, PageRequest.of(0, 10));
            assertNotNull(res);
        }

        @Test
        void buscarPorSetor_delegaParaRepositorio() {
            when(processoRepository.findBySetorResponsavelId(anyString(), any())).thenReturn(new PageImpl<>(List.of(criarEntidadeReal())));
            Page<ProcessoLegislativoDTO> res = service.buscarPorSetor("setor-1", PageRequest.of(0, 10));
            assertNotNull(res);
        }

        @Test
        void buscarPorGestor_delegaParaRepositorio() {
            when(processoRepository.findByGestorId(anyString())).thenReturn(List.of(criarEntidadeReal()));
            List<ProcessoLegislativoDTO> res = service.buscarPorGestor("gestor-1");
            assertEquals(1, res.size());
        }
    }

    // =========================================================================
    // 4. STATUS
    // =========================================================================
    @Nested
    @DisplayName("Cenários de Status")
    class StatusTests {
        @Test
        void atualizarStatus_quandoNormal_naoSetaDataConclusao() {
            ProcessoLegislativo p = criarEntidadeReal();
            when(processoRepository.findById("proc-01")).thenReturn(Optional.of(p));
            when(processoRepository.save(any())).thenReturn(p);

            service.atualizarStatus("proc-01", StatusProcesso.EM_ANDAMENTO);
            assertEquals(StatusProcesso.EM_ANDAMENTO, p.getStatus());
            assertNull(p.getDataConclusao());
        }

        @ParameterizedTest
        @EnumSource(value = StatusProcesso.class, names = {"FINALIZADO", "ARQUIVADO"})
        void atualizarStatus_quandoFinal_setaDataConclusao(StatusProcesso status) {
            ProcessoLegislativo p = criarEntidadeReal();
            when(processoRepository.findById("proc-01")).thenReturn(Optional.of(p));
            when(processoRepository.save(any())).thenReturn(p);

            service.atualizarStatus("proc-01", status);
            assertNotNull(p.getDataConclusao());
        }

        @Test
        void atualizarStatus_quandoInexistente_lancaException() {
            when(processoRepository.findById(anyString())).thenReturn(Optional.empty());
            assertThrows(RecursoNaoEncontradoException.class, () -> service.atualizarStatus("id", StatusProcesso.FINALIZADO));
        }
    }

    // =========================================================================
    // 5. DOCUMENTOS (Matérias e Proposições)
    // =========================================================================
    @Nested
    @DisplayName("Cenários de Documentos")
    class DocumentosTests {
        @Test
        void adicionarProposicao_quandoExiste_salvaSucesso() {
            ProcessoLegislativo p = criarEntidadeReal();
            when(processoRepository.findById("proc-01")).thenReturn(Optional.of(p));
            when(proposicaoRepository.existsById("prop-01")).thenReturn(true);

            service.adicionarProposicao("proc-01", "prop-01");
            assertTrue(p.getProposicaoIds().contains("prop-01"));
            verify(processoRepository).save(p);
        }

        @Test
        void adicionarProposicao_quandoNaoExiste_lancaException() {
            when(processoRepository.findById(anyString())).thenReturn(Optional.of(criarEntidadeReal()));
            when(proposicaoRepository.existsById(anyString())).thenReturn(false);
            assertThrows(RecursoNaoEncontradoException.class, () -> service.adicionarProposicao("p", "inv"));
        }

        @Test
        void adicionarMateria_quandoExiste_salvaSucesso() {
            ProcessoLegislativo p = criarEntidadeReal();
            when(processoRepository.findById("proc-01")).thenReturn(Optional.of(p));
            when(materiaRepository.existsById("mat-01")).thenReturn(true);

            service.adicionarMateria("proc-01", "mat-01");
            assertTrue(p.getMateriaIds().contains("mat-01"));
            verify(processoRepository).save(p);
        }

        @Test
        void adicionarMateria_quandoNaoExiste_lancaException() {
            when(processoRepository.findById(anyString())).thenReturn(Optional.of(criarEntidadeReal()));
            when(materiaRepository.existsById(anyString())).thenReturn(false);
            assertThrows(RecursoNaoEncontradoException.class, () -> service.adicionarMateria("p", "inv"));
        }

        @Test
        void adicionarMateria_quandoProcessoInexistente_lancaException() {
            when(processoRepository.findById(anyString())).thenReturn(Optional.empty());
            assertThrows(RecursoNaoEncontradoException.class, () -> service.adicionarMateria("inv", "mat"));
        }

        @Test
        void adicionarProposicao_quandoProcessoInexistente_lancaException() {
            when(processoRepository.findById(anyString())).thenReturn(Optional.empty());
            assertThrows(RecursoNaoEncontradoException.class, () -> service.adicionarProposicao("inv", "prop"));
        }
    }
}