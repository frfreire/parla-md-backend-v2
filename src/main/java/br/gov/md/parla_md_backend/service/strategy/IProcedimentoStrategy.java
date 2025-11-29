package br.gov.md.parla_md_backend.service.strategy;

public interface IProcedimentoStrategy<T> {
    void buscarESalvarProcedimentos(T projeto);
    String getTipoProcedimento();
    boolean podeProcessar(Object projeto);
}