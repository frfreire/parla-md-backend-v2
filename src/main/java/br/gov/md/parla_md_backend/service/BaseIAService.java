package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.interfaces.AnaliseIAEntity;
import br.gov.md.parla_md_backend.domain.dto.RespostaLlamaDTO;
import br.gov.md.parla_md_backend.exception.IAException;
import br.gov.md.parla_md_backend.repository.IAnaliseIARepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class BaseIAService<E extends AnaliseIAEntity, D, R, REPO extends IAnaliseIARepository<E>> {

    protected final LlamaService llamaService;
    protected final REPO repository;

    @Value("${ai.cache.ttl:86400}")
    protected int cacheTtlSegundos;

    @Value("${ai.modelo.versao:1.0.0}")
    protected String modeloVersao;

    protected BaseIAService(LlamaService llamaService, REPO repository) {
        this.llamaService = llamaService;
        this.repository = repository;
    }

    protected abstract String getNomeAnalise();

    protected abstract String getNomeCacheEvict();

    protected abstract Class<R> getResultadoClass();

    protected abstract D toDTO(E entidade);

    protected abstract String construirPrompt(Object... parametros);

    protected abstract String construirPromptSistema();

    protected abstract E construirEntidade(R resultado, RespostaLlamaDTO resposta, long duracaoMs, Object... parametros);

    protected abstract E construirEntidadeFalha(Exception erro, long duracaoMs, Object... parametros);

    protected abstract Optional<E> buscarCacheRecente(Object... parametros);

    protected E processarAnaliseIA(Object... parametros) {
        long inicioMs = System.currentTimeMillis();

        try {
            String prompt = construirPrompt(parametros);
            String promptSistema = construirPromptSistema();

            log.debug("Enviando requisição ao Llama para {}", getNomeAnalise());

            RespostaLlamaDTO resposta = llamaService.enviarRequisicao(prompt, promptSistema, true);

            R resultado = parsearResposta(resposta);

            long duracaoMs = System.currentTimeMillis() - inicioMs;

            E entidade = construirEntidade(resultado, resposta, duracaoMs, parametros);

            log.info("{} processado com sucesso em {}ms", getNomeAnalise(), duracaoMs);

            return entidade;

        } catch (Exception e) {
            long duracaoMs = System.currentTimeMillis() - inicioMs;

            log.error("Erro ao processar {}: {}", getNomeAnalise(), e.getMessage(), e);

            E entidadeFalha = construirEntidadeFalha(e, duracaoMs, parametros);
            repository.save(entidadeFalha);

            throw criarExcecaoProcessamento(e);
        }
    }

    protected R parsearResposta(RespostaLlamaDTO resposta) {
        try {
            return llamaService.extrairJson(resposta, getResultadoClass());
        } catch (Exception e) {
            log.error("Erro ao parsear resposta do Llama para {}: {}", getNomeAnalise(), e.getMessage());
            throw new IAException("Resposta em formato inválido: " + e.getMessage());
        }
    }

    protected E processarComCache(boolean forcarNova, Object... parametros) {
        if (!forcarNova) {
            Optional<E> cache = buscarCacheRecente(parametros);
            if (cache.isPresent()) {
                log.info("Retornando {} do cache: {}", getNomeAnalise(), cache.get().getId());
                return cache.get();
            }
        }

        E entidade = processarAnaliseIA(parametros);
        return repository.save(entidade);
    }

    protected boolean isCacheValido(E entidade) {
        if (entidade == null) {
            return false;
        }

        LocalDateTime limite = LocalDateTime.now().minusSeconds(cacheTtlSegundos);

        return entidade.getDataAnalise() != null
                && entidade.getDataAnalise().isAfter(limite)
                && Boolean.TRUE.equals(entidade.getSucesso());
    }

    @Transactional(readOnly = true)
    public Page<D> buscarRecentes(Pageable pageable) {
        LocalDateTime limite = LocalDateTime.now().minusDays(30);
        Page<E> entidades = repository.findByDataAnaliseAfter(limite, pageable);
        return entidades.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<D> buscarRecentesPorPeriodo(int dias, Pageable pageable) {
        LocalDateTime limite = LocalDateTime.now().minusDays(dias);
        Page<E> entidades = repository.findByDataAnaliseAfter(limite, pageable);
        return entidades.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<D> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
        List<E> entidades = repository.findByDataAnaliseAfter(inicio);

        List<E> filtradas = entidades.stream()
                .filter(e -> e.getDataAnalise().isBefore(fim) || e.getDataAnalise().isEqual(fim))
                .toList();

        return converterParaPage(filtradas, pageable);
    }

    @Transactional(readOnly = true)
    public Page<D> buscarBemSucedidas(Pageable pageable) {
        Page<E> entidades = repository.findAllBySucessoTrue(pageable);
        return entidades.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<D> buscarFalhas(Pageable pageable) {
        Page<E> entidades = repository.findAllBySucessoFalse(pageable);
        return entidades.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<D> buscarPorModeloVersao(String versao, Pageable pageable) {
        Page<E> entidades = repository.findAllByModeloVersao(versao, pageable);
        return entidades.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public long contarBemSucedidas() {
        return repository.countBySucessoTrue();
    }

    @Transactional(readOnly = true)
    public long contarFalhas() {
        return repository.countBySucessoFalse();
    }

    @Transactional
    public void limparExpiradas() {
        LocalDateTime agora = LocalDateTime.now();

        List<E> expiradas = repository.findByDataExpiracaoBefore(agora);
        int total = expiradas.size();

        if (total > 0) {
            repository.deleteByDataExpiracaoBefore(agora);
            log.info("Removidas {} análises expiradas de {}", total, getNomeAnalise());
        }
    }

    protected LocalDateTime calcularDataExpiracao() {
        return LocalDateTime.now().plusSeconds(cacheTtlSegundos);
    }

    protected Page<D> converterParaPage(List<E> lista, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), lista.size());

        List<E> sublista = start < lista.size() ? lista.subList(start, end) : List.of();

        return new PageImpl<>(
                sublista.stream().map(this::toDTO).toList(),
                pageable,
                lista.size()
        );
    }

    protected IAException criarExcecaoProcessamento(Exception causa) {
        return new IAException(
                String.format("Erro ao processar %s: %s", getNomeAnalise(), causa.getMessage()),
                causa
        );
    }
}