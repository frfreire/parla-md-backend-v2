package br.gov.md.parla_md_backend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utilitário com métodos auxiliares comuns para controllers.
 *
 * <p>Fornece funcionalidades para:</p>
 * <ul>
 *   <li>Extração de informações de UserDetails</li>
 *   <li>Validações comuns de dados</li>
 *   <li>Formatação e parsing de datas</li>
 *   <li>Construção de mensagens de erro padronizadas</li>
 * </ul>
 */
@Slf4j
@Component
public class ControllerUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // =========================================================================
    // MÉTODOS DE AUTENTICAÇÃO E AUTORIZAÇÃO
    // =========================================================================

    /**
     * Extrai o ID do usuário a partir do UserDetails.
     *
     * @param userDetails Detalhes do usuário autenticado
     * @return ID do usuário
     */
    public static String extrairUsuarioId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails não pode ser null");
        }
        return userDetails.getUsername();
    }

    /**
     * Extrai o nome do usuário a partir do UserDetails.
     *
     * @param userDetails Detalhes do usuário autenticado
     * @return Nome do usuário (username)
     */
    public static String extrairNomeUsuario(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails não pode ser null");
        }
        return userDetails.getUsername();
    }

    /**
     * Verifica se o usuário possui uma role específica.
     *
     * @param userDetails Detalhes do usuário
     * @param role Role a verificar (ex: "ROLE_ADMIN")
     * @return true se o usuário possui a role
     */
    public static boolean possuiRole(UserDetails userDetails, String role) {
        if (userDetails == null) {
            return false;
        }

        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
    }

    /**
     * Verifica se o usuário é administrador.
     *
     * @param userDetails Detalhes do usuário
     * @return true se o usuário é ADMIN
     */
    public static boolean isAdmin(UserDetails userDetails) {
        return possuiRole(userDetails, "ROLE_ADMIN");
    }

    // =========================================================================
    // MÉTODOS DE VALIDAÇÃO
    // =========================================================================

    /**
     * Valida se uma string não é nula nem vazia.
     *
     * @param valor Valor a validar
     * @param nomeCampo Nome do campo (para mensagem de erro)
     * @throws IllegalArgumentException se o valor for inválido
     */
    public static void validarCampoObrigatorio(String valor, String nomeCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(nomeCampo + " é obrigatório");
        }
    }

    /**
     * Valida se um ID não é nulo nem vazio.
     *
     * @param id ID a validar
     * @param nomeEntidade Nome da entidade (para mensagem de erro)
     * @throws IllegalArgumentException se o ID for inválido
     */
    public static void validarId(String id, String nomeEntidade) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID de " + nomeEntidade + " é obrigatório");
        }
    }

    /**
     * Valida se um intervalo de datas é válido.
     *
     * @param inicio Data inicial
     * @param fim Data final
     * @throws IllegalArgumentException se o intervalo for inválido
     */
    public static void validarIntervaloDatas(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }

        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final");
        }
    }

    /**
     * Valida se uma data não é futura.
     *
     * @param data Data a validar
     * @param nomeCampo Nome do campo (para mensagem de erro)
     * @throws IllegalArgumentException se a data for futura
     */
    public static void validarDataNaoFutura(LocalDate data, String nomeCampo) {
        if (data == null) {
            throw new IllegalArgumentException(nomeCampo + " é obrigatório");
        }

        if (data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(nomeCampo + " não pode ser futura");
        }
    }

    /**
     * Valida se um ano é válido.
     *
     * @param ano Ano a validar
     * @param anoMinimo Ano mínimo aceitável
     * @throws IllegalArgumentException se o ano for inválido
     */
    public static void validarAno(int ano, int anoMinimo) {
        int anoAtual = LocalDate.now().getYear();

        if (ano < anoMinimo) {
            throw new IllegalArgumentException("Ano não pode ser anterior a " + anoMinimo);
        }

        if (ano > anoAtual + 1) {
            throw new IllegalArgumentException("Ano não pode ser futuro");
        }
    }

    // =========================================================================
    // MÉTODOS DE FORMATAÇÃO E PARSING
    // =========================================================================

    /**
     * Converte string para LocalDate.
     *
     * @param dataStr String no formato yyyy-MM-dd
     * @return LocalDate ou null se a string for inválida
     */
    public static LocalDate parseData(String dataStr) {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dataStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Erro ao converter data: {}", dataStr);
            return null;
        }
    }

    /**
     * Converte string para LocalDateTime.
     *
     * @param dataHoraStr String no formato yyyy-MM-dd'T'HH:mm:ss
     * @return LocalDateTime ou null se a string for inválida
     */
    public static LocalDateTime parseDataHora(String dataHoraStr) {
        if (dataHoraStr == null || dataHoraStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dataHoraStr, DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Erro ao converter data/hora: {}", dataHoraStr);
            return null;
        }
    }

    /**
     * Formata LocalDate para string.
     *
     * @param data Data a formatar
     * @return String no formato yyyy-MM-dd
     */
    public static String formatarData(LocalDate data) {
        if (data == null) {
            return null;
        }
        return data.format(DATE_FORMATTER);
    }

    /**
     * Formata LocalDateTime para string.
     *
     * @param dataHora Data/hora a formatar
     * @return String no formato yyyy-MM-dd'T'HH:mm:ss
     */
    public static String formatarDataHora(LocalDateTime dataHora) {
        if (dataHora == null) {
            return null;
        }
        return dataHora.format(DATETIME_FORMATTER);
    }

    // =========================================================================
    // MÉTODOS DE CONSTRUÇÃO DE MENSAGENS
    // =========================================================================

    /**
     * Constrói mensagem de recurso não encontrado.
     *
     * @param nomeEntidade Nome da entidade
     * @param id ID da entidade
     * @return Mensagem formatada
     */
    public static String mensagemNaoEncontrado(String nomeEntidade, String id) {
        return String.format("%s não encontrado(a) com ID: %s", nomeEntidade, id);
    }

    /**
     * Constrói mensagem de recurso criado com sucesso.
     *
     * @param nomeEntidade Nome da entidade
     * @param id ID da entidade
     * @return Mensagem formatada
     */
    public static String mensagemCriado(String nomeEntidade, String id) {
        return String.format("%s criado(a) com sucesso - ID: %s", nomeEntidade, id);
    }

    /**
     * Constrói mensagem de recurso atualizado com sucesso.
     *
     * @param nomeEntidade Nome da entidade
     * @param id ID da entidade
     * @return Mensagem formatada
     */
    public static String mensagemAtualizado(String nomeEntidade, String id) {
        return String.format("%s atualizado(a) com sucesso - ID: %s", nomeEntidade, id);
    }

    /**
     * Constrói mensagem de recurso deletado com sucesso.
     *
     * @param nomeEntidade Nome da entidade
     * @param id ID da entidade
     * @return Mensagem formatada
     */
    public static String mensagemDeletado(String nomeEntidade, String id) {
        return String.format("%s deletado(a) com sucesso - ID: %s", nomeEntidade, id);
    }

    // =========================================================================
    // MÉTODOS DE SANITIZAÇÃO
    // =========================================================================

    /**
     * Sanitiza string para evitar XSS.
     *
     * @param input String a sanitizar
     * @return String sanitizada
     */
    public static String sanitizarString(String input) {
        if (input == null) {
            return null;
        }

        return input
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");
    }

    /**
     * Normaliza sigla para maiúsculas e remove espaços.
     *
     * @param sigla Sigla a normalizar
     * @return Sigla normalizada
     */
    public static String normalizarSigla(String sigla) {
        if (sigla == null) {
            return null;
        }
        return sigla.trim().toUpperCase();
    }

    // =========================================================================
    // MÉTODOS DE PAGINAÇÃO
    // =========================================================================

    /**
     * Valida parâmetros de paginação.
     *
     * @param page Número da página
     * @param size Tamanho da página
     * @throws IllegalArgumentException se os parâmetros forem inválidos
     */
    public static void validarParametrosPaginacao(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Número da página não pode ser negativo");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("Tamanho da página deve ser maior que zero");
        }

        if (size > 100) {
            throw new IllegalArgumentException("Tamanho máximo da página é 100");
        }
    }
}