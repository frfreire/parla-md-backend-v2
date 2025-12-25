package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Parecer;
import br.gov.md.parla_md_backend.domain.dto.ParecerDTO;
import br.gov.md.parla_md_backend.domain.dto.SolicitarParecerDTO;
import br.gov.md.parla_md_backend.domain.dto.EmitirParecerDTO;
import br.gov.md.parla_md_backend.domain.dto.AprovarParecerDTO;
import br.gov.md.parla_md_backend.domain.enums.RecomendacaoParecer;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IParecerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static br.gov.md.parla_md_backend.config.WorkflowConfig.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParecerService {

    private final IParecerRepository parecerRepository;
    private final ProcessoLegislativoService processoService;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public ParecerDTO solicitarParecer(SolicitarParecerDTO dto, String solicitanteId) {
        log.info("Solicitando parecer do setor {} para processo {}",
                dto.setorEmissorId(), dto.processoId());

        processoService.buscarPorId(dto.processoId());

        verificarParecerDuplicado(dto.processoId(), dto.setorEmissorId());

        String numeroParecer = gerarNumeroParecer();

        Parecer parecer = Parecer.builder()
                .numero(numeroParecer)
                .processoId(dto.processoId())
                .setorEmissorId(dto.setorEmissorId())
                .setorEmissorNome(dto.setorEmissorNome())
                .tipo(dto.tipo())
                .assunto(dto.assunto())
                .dataSolicitacao(LocalDateTime.now())
                .prazo(dto.prazo())
                .observacoes(dto.observacoes())
                .build();

        parecer = parecerRepository.save(parecer);

        enviarParaFila(parecer, "PARECER_SOLICITADO");

        log.info("Parecer {} solicitado com sucesso", parecer.getNumero());

        return ParecerDTO.fromEntity(parecer);
    }

    @Transactional
    public ParecerDTO emitirParecer(EmitirParecerDTO dto, String analistaId, String analistaNome) {
        log.info("Emitindo parecer: {}", dto.parecerId());

        Parecer parecer = parecerRepository.findById(dto.parecerId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Parecer não encontrado"));

        if (parecer.getDataEmissao() != null) {
            throw new IllegalStateException("Parecer já foi emitido");
        }

        parecer.setAnalistaResponsavelId(analistaId);
        parecer.setAnalistaResponsavelNome(analistaNome);
        parecer.setTipo(dto.tipo());
        parecer.setContexto(dto.contexto());
        parecer.setAnalise(dto.analise());
        parecer.setRecomendacao(dto.recomendacao());
        parecer.setJustificativaRecomendacao(dto.justificativa());
        parecer.setFundamentacaoLegal(dto.fundamentacaoLegal());
        parecer.setImpactosIdentificados(dto.impactosIdentificados());
        parecer.setConclusao(dto.conclusao());
        parecer.setDataEmissao(LocalDateTime.now());
        parecer.setAtendidoPrazo(parecer.getPrazo() != null &&
                LocalDateTime.now().isBefore(parecer.getPrazo()));
        parecer.setDataAtualizacao(LocalDateTime.now());

        parecer = parecerRepository.save(parecer);

        enviarParaFila(parecer, "PARECER_EMITIDO");

        log.info("Parecer {} emitido com sucesso", parecer.getNumero());

        return ParecerDTO.fromEntity(parecer);
    }

    @Transactional
    public ParecerDTO aprovarParecer(AprovarParecerDTO dto, String aprovadorId, String aprovadorNome) {
        log.info("Processando aprovação do parecer: {}", dto.parecerId());

        Parecer parecer = parecerRepository.findById(dto.parecerId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Parecer não encontrado"));

        if (parecer.getDataEmissao() == null) {
            throw new IllegalStateException("Parecer ainda não foi emitido");
        }

        if (parecer.getDataAprovacao() != null) {
            throw new IllegalStateException("Parecer já foi aprovado");
        }

        if (Boolean.FALSE.equals(dto.aprovado())) {
            log.info("Parecer {} rejeitado por {}", parecer.getNumero(), aprovadorNome);

            parecer.setDataEmissao(null);
            parecer.setObservacoes(dto.observacoes());
            parecer.setDataAtualizacao(LocalDateTime.now());

            parecer = parecerRepository.save(parecer);

            enviarParaFila(parecer, "PARECER_REJEITADO");

            return ParecerDTO.fromEntity(parecer);
        }

        parecer.setAprovadoPorId(aprovadorId);
        parecer.setAprovadoPorNome(aprovadorNome);
        parecer.setDataAprovacao(LocalDateTime.now());
        parecer.setDataAtualizacao(LocalDateTime.now());

        parecer = parecerRepository.save(parecer);

        enviarParaFila(parecer, "PARECER_APROVADO");

        log.info("Parecer {} aprovado por {}", parecer.getNumero(), aprovadorNome);

        return ParecerDTO.fromEntity(parecer);
    }

    public List<ParecerDTO> buscarPorProcesso(String processoId) {
        log.debug("Buscando pareceres do processo: {}", processoId);

        return parecerRepository.findByProcessoId(processoId)
                .stream()
                .map(ParecerDTO::fromEntity)
                .toList();
    }

    public Page<ParecerDTO> buscarPendentesPorSetor(String setorId, Pageable pageable) {
        log.debug("Buscando pareceres pendentes do setor: {}", setorId);

        return parecerRepository.findBySetorEmissorIdAndDataEmissaoIsNull(setorId, pageable)
                .map(ParecerDTO::fromEntity);
    }

    public Page<ParecerDTO> buscarPendentesAprovacao(Pageable pageable) {
        return parecerRepository.findByDataEmissaoIsNotNullAndDataAprovacaoIsNull(pageable)
                .map(ParecerDTO::fromEntity);
    }

    public List<ParecerDTO> buscarComPrazoVencido() {
        LocalDateTime agora = LocalDateTime.now();

        return parecerRepository.findByPrazoBeforeAndDataEmissaoIsNull(agora)
                .stream()
                .map(ParecerDTO::fromEntity)
                .toList();
    }

    public ParecerDTO buscarPorId(String parecerId) {
        Parecer parecer = parecerRepository.findById(parecerId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Parecer não encontrado"));

        return ParecerDTO.fromEntity(parecer);
    }

    private void verificarParecerDuplicado(String processoId, String setorId) {
        boolean existe = parecerRepository.existsByProcessoIdAndSetorEmissorId(processoId, setorId);

        if (existe) {
            throw new IllegalStateException("Setor já possui parecer solicitado para este processo");
        }
    }

    private String gerarNumeroParecer() {
        int ano = LocalDateTime.now().getYear();
        long count = parecerRepository.countByNumeroStartingWith(String.valueOf(ano));
        return String.format("PARECER-%d/%05d", ano, count + 1);
    }

    private void enviarParaFila(Parecer parecer, String evento) {
        try {
            rabbitTemplate.convertAndSend(
                    NOTIFICACAO_EXCHANGE,
                    NOTIFICACAO_ROUTING_KEY,
                    Map.of(
                            "tipo", evento,
                            "parecerId", parecer.getId(),
                            "processoId", parecer.getProcessoId(),
                            "numero", parecer.getNumero()
                    )
            );
            log.debug("Evento {} enviado para fila RabbitMQ", evento);
        } catch (Exception e) {
            log.error("Erro ao enviar evento {} para fila", evento, e);
        }
    }
}