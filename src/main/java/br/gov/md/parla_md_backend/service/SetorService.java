package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Setor;
import br.gov.md.parla_md_backend.repository.ISetorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetorService {

    private ISetorRepository sectorRepository;

    @Autowired
    public SetorService(ISetorRepository sectorRepository) {
        this.sectorRepository = sectorRepository;
    }

    public Setor criarSetor(Setor setor) {
        return sectorRepository.save(setor);
    }

    public Setor atualizarSetor(String id, Setor setorAtualizado) {
        Setor setorExistente = sectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Setor não encontrado"));

        setorExistente.setNome(setorAtualizado.getNome());
        setorExistente.setSigla(setorAtualizado.getSigla());
        setorExistente.setDescricao(setorAtualizado.getDescricao());

        return sectorRepository.save(setorExistente);
    }

    public void deletarSetor(String id) {
        sectorRepository.deleteById(id);
    }

    public Setor buscarSetorPorId(String id) {
        return sectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Setor não encontrado"));
    }

    public List<Setor> listarTodosSetores() {
        return sectorRepository.findAll();
    }
}
