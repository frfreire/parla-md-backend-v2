package br.gov.md.parla_md_backend.domain.organizacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Setor interno do Ministério da Defesa
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "setores")
public class Setor {

    @Id
    private String id;

    private String nome;

    private String sigla;

    private String descricao;

    private String setorPaiId;

    private Integer nivel;

    private String responsavelId;

    private String responsavelNome;

    private List<String> competencias = new ArrayList<>();

    private List<String> areasAtuacao = new ArrayList<>();

    private String email;

    private String telefone;

    private boolean ativo;

    private boolean podeEmitirParecer;

    private boolean recebeTramitacoes;
}