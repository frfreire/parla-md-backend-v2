package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Parlamentar;
import br.gov.md.parla_md_backend.domain.AnaliseParlamentar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IAnaliseParlamentarRepository extends MongoRepository<AnaliseParlamentar, String> {

    List<AnaliseParlamentar> findByParlamentar(Parlamentar parlamentar);

    Page<AnaliseParlamentar> findByParlamentar(Parlamentar parlamentar, Pageable pageable);

    Optional<AnaliseParlamentar> findByParlamentarAndTema(Parlamentar parlamentar, String tema);

    List<AnaliseParlamentar> findByTema(String tema);

    Page<AnaliseParlamentar> findByTema(String tema, Pageable pageable);

    List<AnaliseParlamentar> findByPosicionamento(String posicionamento);

    List<AnaliseParlamentar> findByDataAnaliseAfter(LocalDateTime data);

    Page<AnaliseParlamentar> findByDataAnaliseAfter(LocalDateTime data, Pageable pageable);

    List<AnaliseParlamentar> findByDataAnaliseBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("{ 'confiabilidade': { $gte: ?0 } }")
    List<AnaliseParlamentar> buscarComConfiabilidadeMinima(Double confiabilidadeMinima);

    @Query("{ 'dataExpiracao': { $lt: ?0 } }")
    List<AnaliseParlamentar> buscarExpiradas(LocalDateTime agora);

    long countByPosicionamento(String posicionamento);

    long countByTema(String tema);

    long countBySucesso(Boolean sucesso);

    void deleteByDataExpiracaoBefore(LocalDateTime dataLimite);
}