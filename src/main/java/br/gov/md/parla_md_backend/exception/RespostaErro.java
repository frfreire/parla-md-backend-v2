package br.gov.md.parla_md_backend.exception;

import java.time.LocalDateTime;

public class RespostaErro {

    private int status;
    private LocalDateTime timestamp;
    private String message;
    private String path;

    public RespostaErro(int status, LocalDateTime timestamp, String message, String path) {
        this.status = status;
        this.timestamp = timestamp;
        this.message = message;
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
