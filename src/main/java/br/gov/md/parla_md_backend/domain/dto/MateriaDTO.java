package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.Casa;
import br.gov.md.parla_md_backend.domain.enums.TipoMateria;
import br.gov.md.parla_md_backend.domain.Materia;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Representação de uma Matéria Legislativa do Senado Federal")
public class MateriaDTO {

    @Schema(description = "ID único da matéria no sistema", example = "507f1f77bcf86cd799439011")
    private String id;

    @Schema(description = "Casa legislativa de origem", example = "SENADO")
    private Casa casa;

    @Schema(description = "Código da matéria no Senado", example = "123456")
    private Long codigoMateria;

    @Schema(description = "Tipo da matéria legislativa", example = "PLS")
    private TipoMateria tipoMateria;

    @Schema(description = "Sigla do subtipo da matéria", example = "PLS")
    private String siglaSubtipoMateria;

    @Schema(description = "Descrição do subtipo da matéria", example = "Projeto de Lei do Senado")
    private String descricaoSubtipoMateria;

    @Schema(description = "Número da matéria", example = "1234")
    private String numero;

    @Schema(description = "Ano da matéria", example = "2024")
    private Integer ano;

    @Schema(description = "Identificação completa da matéria", example = "PLS 1234/2024")
    private String identificadorCompleto;

    @Schema(description = "Ementa da matéria")
    private String ementa;

    @Schema(description = "Ementa detalhada da matéria")
    private String ementaDetalhada;

    @Schema(description = "Palavras-chave para busca")
    private String keywords;

    @Schema(description = "Tema principal da matéria", example = "Defesa Nacional")
    private String tema;

    @Schema(description = "Data de apresentação da matéria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataApresentacao;

    @Schema(description = "Código do parlamentar autor", example = "5012")
    private Long codigoParlamentarAutor;

    @Schema(description = "Nome do parlamentar autor", example = "Senador João da Silva")
    private String nomeParlamentarAutor;

    @Schema(description = "Sigla do partido do autor", example = "PARTIDO")
    private String siglaPartidoParlamentar;

    @Schema(description = "UF do parlamentar autor", example = "DF")
    private String siglaUFParlamentar;

    @Schema(description = "Descrição da natureza da matéria")
    private String descricaoNatureza;

    @Schema(description = "Indicador se está tramitando", example = "Sim")
    private String indicadorTramitando;

    @Schema(description = "Status de triagem no sistema", example = "NAO_AVALIADO")
    private StatusTriagem statusTriagem;

    @Schema(description = "Situação atual da matéria")
    private String situacaoAtual;

    @Schema(description = "Sigla do órgão de origem", example = "SF")
    private String siglaOrgaoOrigem;

    @Schema(description = "Assunto específico da matéria")
    private String assuntoEspecifico;

    @Schema(description = "Assunto geral da matéria")
    private String assuntoGeral;

    @Schema(description = "Indexação da matéria")
    private String indexacao;

    @Schema(description = "URL do inteiro teor da matéria")
    private String urlInteiroTeor;

    @Schema(description = "Link para página da matéria no Senado")
    private String linkPaginaCasa;

    @Schema(description = "Indica se a matéria foi aprovada")
    private boolean aprovada;

    @Schema(description = "Data de captura no sistema")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataCaptura;

    @Schema(description = "Data da última atualização")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataUltimaAtualizacao;


    public static MateriaDTO fromEntity(Materia materia) {
        if (materia == null) {
            return null;
        }

        return MateriaDTO.builder()
                .id(materia.getId())
                .casa(materia.getCasa())
                .codigoMateria(materia.getCodigoMateria())
                .tipoMateria(materia.getTipoMateria())
                .siglaSubtipoMateria(materia.getSiglaSubtipoMateria())
                .descricaoSubtipoMateria(materia.getDescricaoSubtipoMateria())
                .numero(String.valueOf(materia.getNumero()))
                .ano(materia.getAno())
                .identificadorCompleto(materia.getIdentificadorCompleto())
                .ementa(materia.getEmenta())
                .ementaDetalhada(materia.getEmentaDetalhada())
                .keywords(materia.getKeywords())
                .tema(materia.getTema())
                .dataApresentacao(materia.getDataApresentacao())
                .codigoParlamentarAutor(materia.getCodigoParlamentarAutor())
                .nomeParlamentarAutor(materia.getNomeParlamentarAutor())
                .siglaPartidoParlamentar(materia.getSiglaPartidoParlamentar())
                .siglaUFParlamentar(materia.getSiglaUFParlamentar())
                .descricaoNatureza(materia.getDescricaoNatureza())
                .indicadorTramitando(materia.getIndicadorTramitando())
                .statusTriagem(materia.getStatusTriagem())
                .situacaoAtual(materia.getSituacaoAtual())
                .siglaOrgaoOrigem(materia.getSiglaOrgaoOrigem())
                .assuntoEspecifico(materia.getAssuntoEspecifico())
                .assuntoGeral(materia.getAssuntoGeral())
                .indexacao(materia.getIndexacao())
                .urlInteiroTeor(materia.getUrlInteiroTeor())
                .linkPaginaCasa(materia.getLinkPaginaCasa())
                .aprovada(materia.isAprovada())
                .dataCaptura(materia.getDataCaptura())
                .dataUltimaAtualizacao(materia.getDataUltimaAtualizacao())
                .build();
    }

    public Materia toEntity() {
        Materia materia = new Materia();
        materia.setId(this.id);
        materia.setCodigoMateria(this.codigoMateria);
        materia.setTipoMateria(this.tipoMateria);
        materia.setSiglaSubtipoMateria(this.siglaSubtipoMateria);
        materia.setDescricaoSubtipoMateria(this.descricaoSubtipoMateria);
        materia.setNumero(this.numero);
        materia.setAno(this.ano);
        materia.setEmenta(this.ementa);
        materia.setEmentaDetalhada(this.ementaDetalhada);
        materia.setKeywords(this.keywords);
        materia.setTema(this.tema);
        materia.setDataApresentacao(this.dataApresentacao);
        materia.setCodigoParlamentarAutor(this.codigoParlamentarAutor);
        materia.setNomeParlamentarAutor(this.nomeParlamentarAutor);
        materia.setSiglaPartidoParlamentar(this.siglaPartidoParlamentar);
        materia.setSiglaUFParlamentar(this.siglaUFParlamentar);
        materia.setDescricaoNatureza(this.descricaoNatureza);
        materia.setIndicadorTramitando(this.indicadorTramitando);
        materia.setStatusTriagem(this.statusTriagem);
        materia.setSituacaoAtual(this.situacaoAtual);
        materia.setSiglaOrgaoOrigem(this.siglaOrgaoOrigem);
        materia.setAssuntoEspecifico(this.assuntoEspecifico);
        materia.setAssuntoGeral(this.assuntoGeral);
        materia.setIndexacao(this.indexacao);
        materia.setUrlInteiroTeor(this.urlInteiroTeor);
        materia.setLinkPaginaCasa(this.linkPaginaCasa);
        materia.setAprovada(this.aprovada);
        materia.setDataCaptura(this.dataCaptura);
        materia.setDataUltimaAtualizacao(this.dataUltimaAtualizacao);
        return materia;
    }
}