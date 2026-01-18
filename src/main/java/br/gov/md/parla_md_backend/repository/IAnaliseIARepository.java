package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.interfaces.AnaliseIAEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.LocalDateTime;
import java.util.List;

@NoRepositoryBean
public interface IAnaliseIARepository <T extends AnaliseIAEntity> extends MongoRepository<T, String> {

    List<T> findByDataAnaliseAfter(LocalDateTime data);

    Page<T> findByDataAnaliseAfter(LocalDateTime data, Pageable pageable);

    Page<T> findAllBySucessoTrue(Pageable pageable);

    Page<T> findAllBySucessoFalse(Pageable pageable);

    Page<T> findAllByModeloVersao(String modeloVersao, Pageable pageable);

    @Query("{ 'dataExpiracao': { $lt: ?0 } }")
    List<T> findByDataExpiracaoBefore(LocalDateTime data);

    void deleteByDataExpiracaoBefore(LocalDateTime data);

    long countBySucessoTrue();

    long countBySucessoFalse();

}
