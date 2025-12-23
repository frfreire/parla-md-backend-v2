package br.gov.md.parla_md_backend.domain.processo;

import br.gov.md.parla_md_backend.domain.enums.PrioridadeProcesso;
import br.gov.md.parla_md_backend.domain.enums.StatusProcesso;
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
@Document(collection = "processos_legislativos")
public class ProcessoLegislativo {

    @Id
    private String id;

    @Indexed(unique = true)
    private String numero;

    private String titulo;
    private String descricao;

    @Indexed
    private StatusProcesso status;

    @Indexed
    private PrioridadeProcesso prioridade;

    @Builder.Default
    private List<String> proposicaoIds = new ArrayList<>();

    @Builder.Default
    private List<String> materiaIds = new ArrayList<>();

    private String setorResponsavelId;
    private String setorResponsavelNome;

    private String gestorId;
    private String gestorNome;

    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();
    private LocalDateTime dataAtualizacao;
    private LocalDateTime dataConclusao;
}