package br.gov.md.parla_md_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class ApiExternaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String apiOrigem;

    private final Integer statusCode;

    private final String url;

    public ApiExternaException(String mensagem) {
        super(mensagem);
        this.apiOrigem = null;
        this.statusCode = null;
        this.url = null;
    }

    public ApiExternaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
        this.apiOrigem = null;
        this.statusCode = null;
        this.url = null;
    }

    public ApiExternaException(String mensagem, String apiOrigem) {
        super(mensagem);
        this.apiOrigem = apiOrigem;
        this.statusCode = null;
        this.url = null;
    }

    public ApiExternaException(String mensagem, String apiOrigem, Integer statusCode, String url) {
        super(mensagem);
        this.apiOrigem = apiOrigem;
        this.statusCode = statusCode;
        this.url = url;
    }

    public ApiExternaException(String mensagem, String apiOrigem, Integer statusCode,
                               String url, Throwable causa) {
        super(mensagem, causa);
        this.apiOrigem = apiOrigem;
        this.statusCode = statusCode;
        this.url = url;
    }

    public String getApiOrigem() {
        return apiOrigem;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getUrl() {
        return url;
    }

    public String getDescricaoDetalhada() {
        StringBuilder sb = new StringBuilder();
        sb.append("Erro na API externa");

        if (apiOrigem != null) {
            sb.append(" [").append(apiOrigem).append("]");
        }

        sb.append(": ").append(getMessage());

        if (statusCode != null) {
            sb.append(" (HTTP ").append(statusCode).append(")");
        }

        if (url != null) {
            sb.append(" - URL: ").append(url);
        }

        return sb.toString();
    }

    public static ApiExternaException erroApiSenado(String mensagem) {
        return new ApiExternaException(mensagem, "API_SENADO");
    }

    public static ApiExternaException erroApiSenado(String mensagem, Throwable causa) {
        return new ApiExternaException(mensagem, "API_SENADO", null, null, causa);
    }

    public static ApiExternaException erroApiCamara(String mensagem) {
        return new ApiExternaException(mensagem, "API_CAMARA");
    }

    public static ApiExternaException erroApiCamara(String mensagem, Throwable causa) {
        return new ApiExternaException(mensagem, "API_CAMARA", null, null, causa);
    }

    public static ApiExternaException timeout(String apiOrigem, String url) {
        return new ApiExternaException(
                "Timeout na comunicação com API externa",
                apiOrigem, 504, url);
    }

    public static ApiExternaException erroConexao(String apiOrigem, Throwable causa) {
        return new ApiExternaException(
                "Não foi possível conectar à API externa: " + causa.getMessage(),
                apiOrigem, null, null, causa);
    }
}