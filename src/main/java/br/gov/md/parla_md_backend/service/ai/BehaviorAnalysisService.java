package br.gov.md.parla_md_backend.service.ai;

import br.gov.md.parla_md_backend.domain.TemaComportamento;
import br.gov.md.parla_md_backend.domain.Parlamentar;
import br.gov.md.parla_md_backend.repository.IParlamentarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BehaviorAnalysisService {
    private final IParlamentarRepository parlamentarianRepository;
    private final Logger logger = LoggerFactory.getLogger(BehaviorAnalysisService.class);

    @Autowired
    public BehaviorAnalysisService(IParlamentarRepository parlamentarianRepository) {
        this.parlamentarianRepository = parlamentarianRepository;
    }

    @Transactional
    public void registerBehavior(String nomeParlamentar, String tema, boolean votoAFavor) {
        Parlamentar parlamentar = parlamentarianRepository.findByNome(nomeParlamentar)
                .orElseGet(() -> createNewParlamentarian(nomeParlamentar));

        TemaComportamento behavior = parlamentar.getComportamento(tema);
        if (behavior == null) {
            behavior = new TemaComportamento();
            parlamentar.addComportamento(tema, behavior);
        }

        if (votoAFavor) {
            behavior.setVotosAFavor(behavior.getVotosAFavor() + 1);
        } else {
            behavior.setVotosContra(behavior.getVotosContra() + 1);
        }

        parlamentarianRepository.save(parlamentar);
    }

    public double behaviorAnalysis(String nomeParlamentar, String tema) {
        return parlamentarianRepository.findByNome(nomeParlamentar)
                .map(parlamentarian -> parlamentarian.getComportamento(tema))
                .map(TemaComportamento::calcularIndiceApoio)
                .orElse(-1.0); // Indica que não há dados suficientes
    }

    @Transactional
    public void registerPresentedProposition(Parlamentar parlamentar, String theme) {
        Parlamentar existingParlamentar = parlamentarianRepository.findByNome(parlamentar.getNome())
                .orElseGet(() -> createNewParlamentarian(parlamentar));

        TemaComportamento behavior = existingParlamentar.getComportamento(theme);
        if (behavior == null) {
            behavior = new TemaComportamento();
            existingParlamentar.addComportamento(theme, behavior);
        }

        behavior.setPropostasApresentadas(behavior.getPropostasApresentadas() + 1);
        parlamentarianRepository.save(existingParlamentar);
    }

    private Parlamentar createNewParlamentarian(String nomeParlamentar) {
        String id = UUID.randomUUID().toString();
        return new Parlamentar(id, nomeParlamentar, null, null);
    }

    private Parlamentar createNewParlamentarian(Parlamentar parlamentar) {
        return new Parlamentar(
                parlamentar.getId() != null ? parlamentar.getId() : UUID.randomUUID().toString(),
                parlamentar.getNome(),
                parlamentar.getPartido(),
                parlamentar.getEstado()
        );
    }

    private TemaComportamento createNewBehavior(Parlamentar parlamentar, String tema) {
        TemaComportamento behavior = new TemaComportamento();
        parlamentar.addComportamento(tema, behavior);
        return behavior;
    }

    private void updateBehavior(TemaComportamento behavior, boolean votoAFavor) {
        if (votoAFavor) {
            behavior.incrementVotosAFavor();
        } else {
            behavior.incrementVotosContra();
        }
    }
}


