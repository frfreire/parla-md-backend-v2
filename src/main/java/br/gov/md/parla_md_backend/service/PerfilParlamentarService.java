package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.PerfilParlamentar;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IPerfilParlamentarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerfilParlamentarService {

    private final IPerfilParlamentarRepository perfilRepository;

    public PerfilParlamentar buscarPorParlamentarId(String parlamentarId) {
        return perfilRepository.findByParlamentarId(parlamentarId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil parlamentar não encontrado"));
    }

    public List<PerfilParlamentar> buscarPorCasa(String casa) {
        return perfilRepository.findByCasa(casa);
    }

    public List<PerfilParlamentar> buscarPorPartido(String partido) {
        return perfilRepository.findByPartido(partido);
    }

    public List<PerfilParlamentar> buscarPorUf(String uf) {
        return perfilRepository.findByUf(uf);
    }

    @Transactional
    public PerfilParlamentar criar(PerfilParlamentar perfil) {
        perfil.setDataUltimaAnalise(LocalDateTime.now());
        perfil.setDataProximaAtualizacao(LocalDateTime.now().plusDays(30));

        log.info("Criando perfil parlamentar para: {}", perfil.getNomeParlamentar());
        return perfilRepository.save(perfil);
    }

    @Transactional
    public PerfilParlamentar atualizar(PerfilParlamentar perfil) {
        if (!perfilRepository.existsById(perfil.getId())) {
            throw new RecursoNaoEncontradoException("Perfil parlamentar não encontrado");
        }

        perfil.setDataUltimaAnalise(LocalDateTime.now());
        perfil.setDataProximaAtualizacao(LocalDateTime.now().plusDays(30));

        log.info("Atualizando perfil parlamentar: {}", perfil.getNomeParlamentar());
        return perfilRepository.save(perfil);
    }

    public List<PerfilParlamentar> buscarPendentesAtualizacao() {
        return perfilRepository.findByDataProximaAtualizacaoBefore(LocalDateTime.now());
    }

    @Transactional
    public void deletar(String parlamentarId) {
        PerfilParlamentar perfil = buscarPorParlamentarId(parlamentarId);
        perfilRepository.delete(perfil);
        log.info("Perfil parlamentar deletado: {}", parlamentarId);
    }
}