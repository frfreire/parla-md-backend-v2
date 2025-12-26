package br.gov.md.parla_md_backend.exception;

public class AnaliseImpactoException extends IAException {

    private static final long serialVersionUID = 1L;
    private static final String CODIGO_ERRO = "IMP001";

    public AnaliseImpactoException(String mensagem) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem));
    }

    public AnaliseImpactoException(String mensagem, Throwable causa) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem), causa);
    }

    public static AnaliseImpactoException areaNaoEncontrada(String areaId) {
        return new AnaliseImpactoException("Área de impacto não encontrada: " + areaId);
    }

    public static AnaliseImpactoException erroProcessamento(String detalhes, Throwable causa) {
        return new AnaliseImpactoException("Erro ao processar análise: " + detalhes, causa);
    }
}