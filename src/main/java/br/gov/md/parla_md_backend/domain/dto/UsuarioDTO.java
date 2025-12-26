package br.gov.md.parla_md_backend.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioDTO(
        String id,
        String nome,
        String email,
        String cpf,
        String telefone,
        String cargo,
        String setorId,
        String setorNome,
        List<String> roles,
        boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao,
        LocalDateTime ultimoAcesso
) {}