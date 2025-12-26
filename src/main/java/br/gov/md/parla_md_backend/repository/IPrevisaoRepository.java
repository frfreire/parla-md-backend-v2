package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Previsao;
import br.gov.md.parla_md_backend.domain.ItemLegislativo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPrevisaoRepository extends MongoRepository<Previsao, String> {

    List<Previsao> findByItemLegislativo(ItemLegislativo item);

    Page<Previsao> findByItemLegislativo(ItemLegislativo item, Pageable pageable);

    Optional<Previsao> findFirstByItemLegislativoOrderByDataPrevisaoDesc(ItemLegislativo item);

    List<Previsao> findByTipoPrevisao(String tipoPrevisao);

    List<Previsao> findBySucesso(Boolean sucesso);

    List<Previsao> findByDataPrevisaoAfter(LocalDateTime dataPrevisao);

    Page<Previsao> findByDataPrevisaoAfter(LocalDateTime dataPrevisao, Pageable pageable);

    List<Previsao> findByDataPrevisaoBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("{ 'probabilidadeAprovacao': { $gte: ?0 } }")
    List<Previsao> buscarComProbabilidadeMinima(Double probabilidadeMinima);

    @Query("{ 'probabilidadeAprovacao': { $gte: ?0, $lte: ?1 } }")
    List<Previsao> buscarPorFaixaProbabilidade(Double min, Double max);

    @Query("{ 'confianca': { $gte: ?0 } }")
    List<Previsao> buscarComConfiancaMinima(Double confiancaMinima);

    @Query("{ 'dataExpiracao': { $lt: ?0 } }")
    List<Previsao> buscarExpiradas(LocalDateTime agora);

    long countByTipoPrevisao(String tipoPrevisao);

    long countBySucesso(Boolean sucesso);

    long countByDataPrevisaoAfter(LocalDateTime dataPrevisao);

    void deleteByDataExpiracaoBefore(LocalDateTime dataLimite);
}