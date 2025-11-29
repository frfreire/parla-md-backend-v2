package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Posicionamento;
import br.gov.md.parla_md_backend.domain.Setor;
import br.gov.md.parla_md_backend.domain.enums.TipoPosicionamento;
import br.gov.md.parla_md_backend.domain.enums.TipoSetor;
import br.gov.md.parla_md_backend.repository.IPosicionamentoRepository;
import br.gov.md.parla_md_backend.repository.ISetorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PosicionamentoService {

    private IPosicionamentoRepository positioningRepository;
    private ISetorRepository sectorRepository;

    @Autowired
    public PosicionamentoService(IPosicionamentoRepository positioningRepository, ISetorRepository sectorRepository) {
        this.positioningRepository = positioningRepository;
        this.sectorRepository = sectorRepository;
    }

    public Posicionamento solicitarPosicionamento(String propositionId, String setorId, String usuarioSolicitanteId) {
        Setor setor = sectorRepository.findById(setorId)
                .orElseThrow(() -> new RuntimeException("Setor não encontrado"));

        if (setor.getTipo() != TipoSetor.EXTERNO) {
            throw new IllegalArgumentException("Posicionamento só pode ser solicitado para setores externos");
        }

        Posicionamento posicionamento = new Posicionamento();
        posicionamento.setPropositionId(propositionId);
        posicionamento.setSetorId(setorId);
        posicionamento.setTipo(TipoPosicionamento.PENDENTE);
        posicionamento.setDataSolicitacao(LocalDateTime.now());
        posicionamento.setUsuarioSolicitanteId(usuarioSolicitanteId);

        return positioningRepository.save(posicionamento);
    }

    public Posicionamento responderPosicionamento(String posicionamentoId, TipoPosicionamento tipo, String justificativa, String usuarioRespondenteId) {
        Posicionamento posicionamento = positioningRepository.findById(posicionamentoId)
                .orElseThrow(() -> new RuntimeException("Posicionamento não encontrado"));

        posicionamento.setTipo(tipo);
        posicionamento.setJustificativa(justificativa);
        posicionamento.setDataResposta(LocalDateTime.now());
        posicionamento.setUsuarioRespondenteId(usuarioRespondenteId);

        return positioningRepository.save(posicionamento);
    }

    public List<Posicionamento> getPosicionamentosByProposition(String propositionId) {
        return positioningRepository.findByPropositionId(propositionId);
    }
}
