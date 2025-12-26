package br.gov.md.parla_md_backend.domain.old;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemaComportamento {
    private int propostasApresentadas;
    private int votosAFavor;
    private int votosContra;
    private int abstencoes;

    public void incrementVotosAFavor() {
        this.votosAFavor++;
    }

    public void incrementVotosContra() {
        this.votosContra++;
    }
    public double calcularIndiceApoio() {
        int totalVotos = votosAFavor + votosContra + abstencoes;
        return totalVotos > 0 ? (double) votosAFavor / totalVotos : 0;
    }
}
