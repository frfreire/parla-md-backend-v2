package br.gov.md.parla_md_backend.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
class ErroDetalhe {
    private LocalDateTime timestamp;
    private String mensagem;
    private String detalhes;

}
