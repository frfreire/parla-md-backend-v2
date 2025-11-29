package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUsuarioRepository extends MongoRepository<Usuario, String> {

    Usuario findByEmail(String email);
    List<Usuario> findBySetorId(String setorId);
}
