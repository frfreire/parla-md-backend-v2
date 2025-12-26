package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Parlamentar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ISenadorRepository extends MongoRepository<Parlamentar, String> {

    @Query("{ 'casa': 'SENADO', 'codigoParlamentar': ?0 }")
    Optional<Parlamentar> findByCodigoParlamentar(Long codigoParlamentar);

    @Query("{ 'casa': 'SENADO', 'nome': ?0 }")
    Parlamentar findByNome(String nome);

    @Query("{ 'casa': 'SENADO', 'nomeCivil': ?0 }")
    Optional<Parlamentar> findByNomeCivil(String nomeCivil);

    @Query(value = "{ 'casa': 'SENADO', 'codigoParlamentar': ?0 }", exists = true)
    boolean existsByCodigoParlamentar(Long codigoParlamentar);

    @Query("{ 'casa': 'SENADO' }")
    List<Parlamentar> findAllSenadores();

    @Query("{ 'casa': 'SENADO' }")
    Page<Parlamentar> findAllSenadores(Pageable pageable);

    @Query("{ 'casa': 'SENADO', 'emExercicio': true }")
    Page<Parlamentar> findSenadoresEmExercicio(Pageable pageable);

    @Query("{ 'casa': 'SENADO', 'emExercicio': true }")
    List<Parlamentar> findAllSenadoresEmExercicio();

    @Query("{ 'casa': 'SENADO', 'siglaPartido': ?0 }")
    List<Parlamentar> findBySiglaPartido(String siglaPartido);

    @Query("{ 'casa': 'SENADO', 'siglaPartido': ?0 }")
    Page<Parlamentar> findBySiglaPartido(String siglaPartido, Pageable pageable);

    @Query("{ 'casa': 'SENADO', 'siglaPartido': ?0, 'emExercicio': true }")
    List<Parlamentar> findBySiglaPartidoEmExercicio(String siglaPartido);

    @Query("{ 'casa': 'SENADO', 'siglaUF': ?0 }")
    List<Parlamentar> findBySiglaUF(String siglaUF);

    @Query("{ 'casa': 'SENADO', 'siglaUF': ?0 }")
    Page<Parlamentar> findBySiglaUF(String siglaUF, Pageable pageable);

    @Query("{ 'casa': 'SENADO', 'siglaUF': ?0, 'emExercicio': true }")
    List<Parlamentar> findBySiglaUFEmExercicio(String siglaUF);

    @Query("{ 'casa': 'SENADO', 'siglaUF': ?0, 'siglaPartido': ?1 }")
    List<Parlamentar> findBySiglaUFAndSiglaPartido(String siglaUF, String siglaPartido);

    @Query("{ 'casa': 'SENADO', 'nome': { $regex: ?0, $options: 'i' } }")
    Page<Parlamentar> pesquisarPorNome(String nome, Pageable pageable);

    @Query("{ 'casa': 'SENADO', $or: [ " +
            "{ 'nome': { $regex: ?0, $options: 'i' } }, " +
            "{ 'nomeCivil': { $regex: ?0, $options: 'i' } } " +
            "] }")
    Page<Parlamentar> pesquisarPorNomeOuNomeCivil(String termo, Pageable pageable);

    @Query(value = "{ 'casa': 'SENADO' }", count = true)
    long countSenadores();

    @Query(value = "{ 'casa': 'SENADO', 'emExercicio': true }", count = true)
    long countSenadoresEmExercicio();

    @Query(value = "{ 'casa': 'SENADO', 'siglaPartido': ?0 }", count = true)
    long countBySiglaPartido(String siglaPartido);

    @Query(value = "{ 'casa': 'SENADO', 'siglaUF': ?0 }", count = true)
    long countBySiglaUF(String siglaUF);

    @Query("{ 'casa': 'SENADO', 'email': { $exists: true, $ne: null, $ne: '' } }")
    Page<Parlamentar> findSenadoresComEmail(Pageable pageable);

    @Query(value = "{ 'casa': 'SENADO', 'emExercicio': true }", fields = "{ 'siglaPartido': 1 }")
    List<Parlamentar> findPartidosDistintos();

    @Query(value = "{ 'casa': 'SENADO', 'emExercicio': true }", fields = "{ 'siglaUF': 1 }")
    List<Parlamentar> findUFsDistintas();
}