package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.legislativo.ItemLegislativo;
import br.gov.md.parla_md_backend.domain.enums.Casa;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IItemLegislativoRepository extends MongoRepository<ItemLegislativo, String> {

    Page<ItemLegislativo> findByCasa(Casa casa, Pageable pageable);

    Page<ItemLegislativo> findByStatusTriagem(StatusTriagem status, Pageable pageable);

    Page<ItemLegislativo> findByCasaAndStatusTriagem(Casa casa, StatusTriagem status, Pageable pageable);

    Optional<ItemLegislativo> findByCasaAndNumeroAndAno(Casa casa, String numero, Integer ano);

    List<ItemLegislativo> findByTemaContaining(String tema);
}