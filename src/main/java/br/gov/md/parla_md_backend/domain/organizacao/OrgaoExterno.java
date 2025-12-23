package br.gov.md.parla_md_backend.domain.organizacao;

import br.gov.md.parla_md_backend.domain.enums.TipoOrgao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Órgão externo ao MD (Ministérios, Forças Armadas, etc)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orgaos_externos")
public class OrgaoExterno {

    @Id
    private String id;

    private String nome;

    private String sigla;

    private TipoOrgao tipo;

    private String descricao;

    private String emailOficial;

    private String telefone;

    private String endereco;

    private List<Representante> representantes = new ArrayList<>();

    private boolean ativo;

    private String observacoes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Representante {
        private String nome;
        private String cargo;
        private String email;
        private String telefone;
        private boolean principal;
    }
}