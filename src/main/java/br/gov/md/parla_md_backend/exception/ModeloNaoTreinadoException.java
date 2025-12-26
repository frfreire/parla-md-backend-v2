package br.gov.md.parla_md_backend.exception;

public class ModeloNaoTreinadoException extends IAException {

    private static final long serialVersionUID = 1L;
    private static final String CODIGO_ERRO = "ML001";

    public ModeloNaoTreinadoException(String mensagem) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem));
    }

    public static ModeloNaoTreinadoException naoInicializado() {
        return new ModeloNaoTreinadoException(
                "Modelo de previsão não está treinado. Execute o treinamento primeiro."
        );
    }
}