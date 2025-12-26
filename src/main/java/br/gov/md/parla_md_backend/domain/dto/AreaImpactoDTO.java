package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.AreaImpacto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaImpactoDTO {

    private String id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    private List<String> keywords;

    private List<String> gruposAfetados;

    private String categoria;

    private Boolean ativa;

    private Integer ordem;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataUltimaAtualizacao;

    public static AreaImpactoDTO from(AreaImpacto area) {
        return AreaImpactoDTO.builder()
                .id(area.getId())
                .nome(area.getNome())
                .descricao(area.getDescricao())
                .keywords(area.getKeywords())
                .gruposAfetados(area.getGruposAfetados())
                .categoria(area.getCategoria())
                .ativa(area.getAtiva())
                .ordem(area.getOrdem())
                .dataCriacao(area.getDataCriacao())
                .dataUltimaAtualizacao(area.getDataUltimaAtualizacao())
                .build();
    }

    public AreaImpacto toEntity() {
        return AreaImpacto.builder()
                .id(this.id)
                .nome(this.nome)
                .descricao(this.descricao)
                .keywords(this.keywords)
                .gruposAfetados(this.gruposAfetados)
                .categoria(this.categoria)
                .ativa(this.ativa != null ? this.ativa : true)
                .ordem(this.ordem)
                .dataCriacao(this.dataCriacao)
                .dataUltimaAtualizacao(LocalDateTime.now())
                .build();
    }
}