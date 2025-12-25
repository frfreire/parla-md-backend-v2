package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.DispositivoUsuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IDispositivoUsuarioRepository extends MongoRepository<DispositivoUsuario, String> {

    Optional<DispositivoUsuario> findByTokenFcm(String tokenFcm);

    List<DispositivoUsuario> findByUsuarioId(String usuarioId);

    List<DispositivoUsuario> findByUsuarioIdAndAtivoTrue(String usuarioId);

    Optional<DispositivoUsuario> findByUsuarioIdAndTokenFcm(String usuarioId, String tokenFcm);

    long countByUsuarioIdAndAtivoTrue(String usuarioId);

    void deleteByUsuarioId(String usuarioId);

    List<DispositivoUsuario> findByAtivoTrueAndUltimoAcessoBefore(LocalDateTime dataLimite);

    List<DispositivoUsuario> findByPlataformaAndAtivoTrue(String plataforma);
}