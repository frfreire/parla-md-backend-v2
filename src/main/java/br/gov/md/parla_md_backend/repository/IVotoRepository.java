package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Parlamentar;
import br.gov.md.parla_md_backend.domain.Votacao;
import br.gov.md.parla_md_backend.domain.Voto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IVotoRepository extends MongoRepository<Voto, String> {

    List<Voto> findByDeputado(Parlamentar parlamentar);

    Page<Voto> findByDeputado(Parlamentar parlamentar, Pageable pageable);

    List<Voto> findByVotacao(Votacao votacao);

    List<Voto> findByVoto(String voto);

    @Query("{ 'deputado': ?0, 'voto': ?1 }")
    List<Voto> buscarPorParlamentarEVoto(Parlamentar parlamentar, String voto);

    @Query("{ 'deputado': ?0, 'votacao.dataHoraInicio': { $gte: ?1, $lte: ?2 } }")
    List<Voto> buscarPorParlamentarEPeriodo(
            Parlamentar parlamentar,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    long countByDeputado(Parlamentar parlamentar);

    long countByDeputadoAndVoto(Parlamentar parlamentar, String voto);
}