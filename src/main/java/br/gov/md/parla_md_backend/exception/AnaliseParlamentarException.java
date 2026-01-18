package br.gov.md.parla_md_backend.exception;

public class AnaliseParlamentarException extends RuntimeException {

    public AnaliseParlamentarException(String mensagem) {
        super(mensagem);
    }

    public AnaliseParlamentarException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public static AnaliseParlamentarException parlamentarNaoEncontrado(String parlamentarId) {
        return new AnaliseParlamentarException(
                String.format("Parlamentar não encontrado: %s", parlamentarId));
    }

    public static AnaliseParlamentarException analiseNaoEncontrada(String parlamentarId, String tema) {
        return new AnaliseParlamentarException(
                String.format("Análise não encontrada para parlamentar %s sobre tema '%s'", parlamentarId, tema));
    }

    public static AnaliseParlamentarException votacoesInsuficientes(int minimo, int encontradas) {
        return new AnaliseParlamentarException(
                String.format("Votações insuficientes para análise. Mínimo: %d, encontradas: %d", minimo, encontradas));
    }

    public static AnaliseParlamentarException erroProcessamento(String mensagem) {
        return new AnaliseParlamentarException("Erro ao processar análise parlamentar: " + mensagem);
    }

    public static AnaliseParlamentarException erroProcessamento(String mensagem, Throwable causa) {
        return new AnaliseParlamentarException("Erro ao processar análise parlamentar: " + mensagem, causa);
    }
}