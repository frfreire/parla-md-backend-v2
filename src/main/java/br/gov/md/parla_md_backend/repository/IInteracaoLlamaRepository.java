package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.InteracaoLlama;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IInteracaoLlamaRepository extends MongoRepository<InteracaoLlama, String> {

    List<InteracaoLlama> findByModelo(String modelo);

    Page<InteracaoLlama> findByModelo(String modelo, Pageable pageable);

    List<InteracaoLlama> findBySucesso(Boolean sucesso);

    List<InteracaoLlama> findByUsuarioId(String usuarioId);

    Page<InteracaoLlama> findByUsuarioId(String usuarioId, Pageable pageable);

    List<InteracaoLlama> findByContexto(String contexto);

    List<InteracaoLlama> findByDataHoraRequisicaoAfter(LocalDateTime dataHora);

    List<InteracaoLlama> findByDataHoraRequisicaoBetween(
            LocalDateTime inicio,
            LocalDateTime fim
    );

    @Query("{ 'sucesso': true, 'dataHoraRequisicao': { $gte: ?0 } }")
    List<InteracaoLlama> buscarSucessosRecentes(LocalDateTime desde);

    @Query("{ 'sucesso': false, 'dataHoraRequisicao': { $gte: ?0 } }")
    List<InteracaoLlama> buscarFalhasRecentes(LocalDateTime desde);

    @Query("{ 'promptUsuario': { $regex: ?0, $options: 'i' } }")
    List<InteracaoLlama> buscarPorPromptContendo(String texto);

    @Query("{ 'duracaoTotalMs': { $gte: ?0 } }")
    List<InteracaoLlama> buscarLentas(Long duracaoMinimaMs);

    long countBySucesso(Boolean sucesso);

    long countByModelo(String modelo);

    long countByDataHoraRequisicaoAfter(LocalDateTime dataHora);

    @Query(value = "{ 'modelo': ?0 }", count = true)
    long contarPorModelo(String modelo);

    @Query("{ 'dataExpiracao': { $lt: ?0 } }")
    List<InteracaoLlama> buscarExpiradas(LocalDateTime agora);

    void deleteByDataExpiracaoBefore(LocalDateTime dataLimite);
}