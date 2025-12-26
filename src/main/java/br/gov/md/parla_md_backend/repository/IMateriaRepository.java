package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.enums.TipoMateria;
import br.gov.md.parla_md_backend.domain.Materia;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IMateriaRepository extends MongoRepository<Materia, String> {

    Optional<Materia> findByCodigoMateria(Long codigoMateria);

    Optional<Materia> findByNumeroAndAno(String numero, Integer ano);

    Optional<Materia> findByTipoMateriaAndNumeroAndAno(TipoMateria tipoMateria, String numero, Integer ano);

    boolean existsByCodigoMateria(Long codigoMateria);

    List<Materia> findByAno(Integer ano);

    Page<Materia> findByAno(Integer ano, Pageable pageable);

    Page<Materia> findByAnoBetween(Integer anoInicio, Integer anoFim, Pageable pageable);

    List<Materia> findByTipoMateria(TipoMateria tipoMateria);

    Page<Materia> findByTipoMateria(TipoMateria tipoMateria, Pageable pageable);

    Page<Materia> findBySiglaSubtipoMateria(String siglaSubtipoMateria, Pageable pageable);

    Page<Materia> findByTipoMateriaAndAno(TipoMateria tipoMateria, Integer ano, Pageable pageable);

    Page<Materia> findByIndicadorTramitando(String indicadorTramitando, Pageable pageable);

    Page<Materia> findByIndicadorTramitandoAndTipoMateria(
            String indicadorTramitando, TipoMateria tipoMateria, Pageable pageable);

    long countByIndicadorTramitando(String indicadorTramitando);

    Page<Materia> findByStatusTriagem(StatusTriagem statusTriagem, Pageable pageable);

    Page<Materia> findByStatusTriagemAndAno(StatusTriagem statusTriagem, Integer ano, Pageable pageable);

    long countByStatusTriagem(StatusTriagem statusTriagem);

    @Query("{ 'statusTriagem': { $in: ['NAO_AVALIADO', null] } }")
    Page<Materia> findMateriasNaoAvaliadas(Pageable pageable);

    List<Materia> findByDataApresentacaoAfter(LocalDateTime data);

    List<Materia> findByDataApresentacaoAfter(LocalDate data);

    Page<Materia> findByDataApresentacaoBetween(LocalDate dataInicio, LocalDate dataFim, Pageable pageable);

    Page<Materia> findByDataUltimaAtualizacaoAfter(LocalDateTime data, Pageable pageable);

    Page<Materia> findByCodigoParlamentarAutor(Long codigoParlamentarAutor, Pageable pageable);

    Page<Materia> findByNomeParlamentarAutorContainingIgnoreCase(String nomeParlamentarAutor, Pageable pageable);

    Page<Materia> findBySiglaPartidoParlamentar(String siglaPartidoParlamentar, Pageable pageable);

    Page<Materia> findBySiglaUFParlamentar(String siglaUFParlamentar, Pageable pageable);

    Page<Materia> findByTema(String tema, Pageable pageable);

    Page<Materia> findByTemaContainingIgnoreCase(String termo, Pageable pageable);

    Page<Materia> findByAssuntoGeral(String assuntoGeral, Pageable pageable);

    Page<Materia> findByAssuntoEspecifico(String assuntoEspecifico, Pageable pageable);

    Page<Materia> findByEmentaContainingIgnoreCase(String termo, Pageable pageable);

    Page<Materia> findByKeywordsContainingIgnoreCase(String termo, Pageable pageable);

    @Query("{ $or: [ " +
            "{ 'ementa': { $regex: ?0, $options: 'i' } }, " +
            "{ 'keywords': { $regex: ?0, $options: 'i' } }, " +
            "{ 'indexacao': { $regex: ?0, $options: 'i' } }, " +
            "{ 'assuntoGeral': { $regex: ?0, $options: 'i' } }, " +
            "{ 'assuntoEspecifico': { $regex: ?0, $options: 'i' } } " +
            "] }")
    Page<Materia> pesquisarPorTermo(String termo, Pageable pageable);

    Page<Materia> findBySiglaOrgaoOrigem(String siglaOrgaoOrigem, Pageable pageable);

    long countByAno(Integer ano);

    long countByTipoMateria(TipoMateria tipoMateria);

    long countByTipoMateriaAndAno(TipoMateria tipoMateria, Integer ano);

    long countByAprovada(boolean aprovada);

    @Query("{ $or: [ " +
            "{ 'ementa': { $regex: 'defesa|militar|forças armadas|exército|marinha|aeronáutica|segurança nacional', $options: 'i' } }, " +
            "{ 'tema': { $regex: 'defesa|militar|segurança', $options: 'i' } }, " +
            "{ 'assuntoGeral': { $regex: 'defesa|militar|segurança', $options: 'i' } } " +
            "] }")
    Page<Materia> findMateriasRelacionadasDefesa(Pageable pageable);

    @Query("{ 'statusTriagem': { $in: ['NAO_AVALIADO', null] }, 'indicadorTramitando': 'Sim' }")
    Page<Materia> findMateriasPrioritarias(Pageable pageable);

    Page<Materia> findByTipoMateriaIn(List<TipoMateria> tipos, Pageable pageable);

    Collection<Object> findByDataApresentacaoBetween(LocalDateTime inicio, LocalDateTime fim);
}