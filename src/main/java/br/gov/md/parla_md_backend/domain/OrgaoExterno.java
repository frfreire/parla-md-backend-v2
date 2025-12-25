package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orgaos_externos")
public class OrgaoExterno {

    @Id
    private String id;

    @Indexed
    private String nome;

    @Indexed(unique = true)
    private String sigla;

    private String tipo;

    private String descricao;

    private String emailOficial;

    private String telefone;

    private String endereco;

    @Builder.Default
    private List<Representante> representantes = new ArrayList<>();

    @Builder.Default
    private boolean ativo = true;

    private String observacoes;

    public Representante getRepresentantePrincipal() {
        return representantes.stream()
                .filter(Representante::isPrincipal)
                .findFirst()
                .orElse(null);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Representante {
        private String nome;
        private String cargo;
        private String email;
        private String telefone;

        @Builder.Default
        private boolean principal = false;
    }
}