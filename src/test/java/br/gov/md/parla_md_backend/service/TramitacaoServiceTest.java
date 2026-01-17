package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Tramitacao;
import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
import br.gov.md.parla_md_backend.exception.TramitacaoInvalidaException;
import br.gov.md.parla_md_backend.repository.ITramitacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TramitacaoServiceTest {

    @Mock
    private ITramitacaoRepository tramitacaoRepository;

    // Mocks adicionais que o Service exige, mesmo que não usados diretamente nestes testes específicos
    @Mock
    private br.gov.md.parla_md_backend.repository.IProcessoLegislativoRepository processoRepository;
    @Mock
    private br.gov.md.parla_md_backend.repository.IUsuarioRepository usuarioRepository;
    @Mock
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @InjectMocks
    private TramitacaoService tramitacaoService;

    private final String TRAMITACAO_ID = "123";
    private final String USUARIO_ID = "analista@defesa.gov.br";
    private final String OUTRO_USUARIO_ID = "user-intruso";

    // --- TESTES DE INICIAR ANÁLISE ---

    @Test
    @DisplayName("Deve iniciar análise com sucesso quando status for RECEBIDO")
    void deveIniciarAnaliseComSucesso() {
        // Cenário
        Tramitacao tramitacao = Tramitacao.builder()
                .id(TRAMITACAO_ID)
                .destinatarioId(USUARIO_ID)
                .status(StatusTramitacao.RECEBIDO)
                .build();

        when(tramitacaoRepository.findById(TRAMITACAO_ID)).thenReturn(Optional.of(tramitacao));
        when(tramitacaoRepository.save(any(Tramitacao.class))).thenAnswer(i -> i.getArguments()[0]);

        // Execução
        Tramitacao resultado = tramitacaoService.iniciarAnalise(TRAMITACAO_ID, USUARIO_ID);

        // Verificação
        assertEquals(StatusTramitacao.EM_ANALISE, resultado.getStatus());
        verify(tramitacaoRepository).save(tramitacao);
    }

    @Test
    @DisplayName("Não deve iniciar análise se status não for RECEBIDO")
    void naoDeveIniciarAnaliseSeStatusInvalido() {
        Tramitacao tramitacao = Tramitacao.builder()
                .id(TRAMITACAO_ID)
                .destinatarioId(USUARIO_ID)
                .status(StatusTramitacao.PENDENTE) // Errado, deveria ser RECEBIDO
                .build();

        when(tramitacaoRepository.findById(TRAMITACAO_ID)).thenReturn(Optional.of(tramitacao));

        assertThrows(TramitacaoInvalidaException.class, () ->
                tramitacaoService.iniciarAnalise(TRAMITACAO_ID, USUARIO_ID)
        );

        verify(tramitacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve permitir outro usuário alterar o status")
    void naoDevePermitirUsuarioNaoDestinatario() {
        Tramitacao tramitacao = Tramitacao.builder()
                .id(TRAMITACAO_ID)
                .destinatarioId(USUARIO_ID)
                .status(StatusTramitacao.RECEBIDO)
                .build();

        when(tramitacaoRepository.findById(TRAMITACAO_ID)).thenReturn(Optional.of(tramitacao));

        assertThrows(TramitacaoInvalidaException.class, () ->
                tramitacaoService.iniciarAnalise(TRAMITACAO_ID, OUTRO_USUARIO_ID)
        );
    }

    // --- TESTES DE SOLICITAR PARECER ---

    @Test
    @DisplayName("Deve transitar para AGUARDANDO_PARECER com sucesso")
    void deveSolicitarParecerComSucesso() {
        Tramitacao tramitacao = Tramitacao.builder()
                .id(TRAMITACAO_ID)
                .destinatarioId(USUARIO_ID)
                .status(StatusTramitacao.EM_ANALISE)
                .build();

        when(tramitacaoRepository.findById(TRAMITACAO_ID)).thenReturn(Optional.of(tramitacao));
        when(tramitacaoRepository.save(any(Tramitacao.class))).thenAnswer(i -> i.getArguments()[0]);

        Tramitacao resultado = tramitacaoService.solicitarParecer(TRAMITACAO_ID, USUARIO_ID);

        assertEquals(StatusTramitacao.AGUARDANDO_PARECER, resultado.getStatus());
    }

    // --- TESTES DE SUSPENDER ---

    @Test
    @DisplayName("Deve suspender tramitação (voltar para PENDENTE)")
    void deveSuspenderTramitacao() {
        Tramitacao tramitacao = Tramitacao.builder()
                .id(TRAMITACAO_ID)
                .destinatarioId(USUARIO_ID)
                .status(StatusTramitacao.EM_ANALISE)
                .build();

        when(tramitacaoRepository.findById(TRAMITACAO_ID)).thenReturn(Optional.of(tramitacao));
        when(tramitacaoRepository.save(any(Tramitacao.class))).thenAnswer(i -> i.getArguments()[0]);

        Tramitacao resultado = tramitacaoService.suspender(TRAMITACAO_ID, USUARIO_ID);

        assertEquals(StatusTramitacao.PENDENTE, resultado.getStatus());
    }

    // --- TESTES DE RETOMAR ---

    @Test
    @DisplayName("Deve retomar tramitação (PENDENTE -> EM_ANALISE)")
    void deveRetomarTramitacao() {
        Tramitacao tramitacao = Tramitacao.builder()
                .id(TRAMITACAO_ID)
                .destinatarioId(USUARIO_ID)
                .status(StatusTramitacao.PENDENTE)
                .build();

        when(tramitacaoRepository.findById(TRAMITACAO_ID)).thenReturn(Optional.of(tramitacao));
        when(tramitacaoRepository.save(any(Tramitacao.class))).thenAnswer(i -> i.getArguments()[0]);

        Tramitacao resultado = tramitacaoService.retomar(TRAMITACAO_ID, USUARIO_ID);

        assertEquals(StatusTramitacao.EM_ANALISE, resultado.getStatus());
    }
}
