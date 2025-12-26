package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Deputado;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDeputadoRepository extends MongoRepository<Deputado, String> {

    Optional<Deputado> findByIdCamara(Long idCamara);

    List<Deputado> findByPartido(String partido);

    List<Deputado> findByUf(String uf);

    List<Deputado> findByAtivoTrue();

    boolean existsByIdCamara(Long idCamara);
}