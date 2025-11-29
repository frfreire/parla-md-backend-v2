package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "parladb.users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Usuario {
    @Id
    private String id;
    private String nome;
    private String email;
    private String senha;
    private String setorId;
    private String cargo;
    private boolean ativo;
    private List<String> interests;
}
