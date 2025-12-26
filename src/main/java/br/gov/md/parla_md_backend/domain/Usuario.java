package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String senha;
    private String nome;
    private String cpf;
    private String telefone;

    @Indexed
    private String setorId;
    private String setorNome;

    @Builder.Default
    private List<String> roles = new ArrayList<>();

    @Builder.Default
    private boolean ativo = true;

    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();
    private LocalDateTime dataAtualizacao;
    private LocalDateTime ultimoAcesso;

}
