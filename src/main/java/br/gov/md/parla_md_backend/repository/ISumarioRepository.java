package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Sumario;
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
public interface ISumarioRepository extends MongoRepository<Sumario, String> {

    List<Sumario> findByItemLegislativo(ItemLegislativo item);

    Page<Sumario> findByItemLegislativo(ItemLegislativo item, Pageable pageable);

    Optional<Sumario> findFirstByItemLegislativoOrderByDataCriacaoDesc(ItemLegislativo item);

    List<Sumario> findByTipoSumario(String tipoSumario);

    List<Sumario> findBySucesso(Boolean sucesso);

    List<Sumario> findByDataCriacaoAfter(LocalDateTime dataCriacao);

    Page<Sumario> findByDataCriacaoAfter(LocalDateTime dataCriacao, Pageable pageable);

    List<Sumario> findByDataCriacaoBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("{ 'palavrasChave': { $in: ?0 } }")
    List<Sumario> buscarPorPalavrasChave(List<String> palavras);

    @Query("{ 'temasPrincipais': { $regex: ?0, $options: 'i' } }")
    List<Sumario> buscarPorTema(String tema);

    @Query("{ 'taxaCompressao': { $lt: ?0 } }")
    List<Sumario> buscarCompressaoEficiente(Double taxaMaxima);

    @Query("{ 'dataExpiracao': { $lt: ?0 } }")
    List<Sumario> buscarExpirados(LocalDateTime agora);

    long countByTipoSumario(String tipoSumario);

    long countBySucesso(Boolean sucesso);

    long countByDataCriacaoAfter(LocalDateTime dataCriacao);

    @Query(value = "{ 'sucesso': true }", count = true)
    long contarSucessos();

    void deleteByDataExpiracaoBefore(LocalDateTime dataLimite);
}