package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.AreaImpacto;
import br.gov.md.parla_md_backend.domain.dto.AreaImpactoDTO;
import br.gov.md.parla_md_backend.domain.dto.CriarAreaImpactoDTO;
import br.gov.md.parla_md_backend.domain.dto.AtualizarAreaImpactoDTO;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.exception.ValidacaoException;
import br.gov.md.parla_md_backend.repository.IAreaImpactoRepository;
import br.gov.md.parla_md_backend.repository.IAnaliseImpactoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AreaImpactoService {

    private final IAreaImpactoRepository areaImpactoRepository;
    private final IAnaliseImpactoRepository analiseImpactoRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "areasImpacto", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<AreaImpactoDTO> listarTodas(Pageable pageable) {
        log.debug("Listando áreas de impacto - página: {}, tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<AreaImpacto> areas = areaImpactoRepository.findAll(pageable);
        log.info("Encontradas {} áreas de impacto", areas.getTotalElements());

        return areas.map(AreaImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "areasImpactoAtivas")
    public List<AreaImpactoDTO> listarAtivas() {
        log.debug("Listando áreas de impacto ativas");

        List<AreaImpacto> areas = areaImpactoRepository.findByAtivaTrue();
        log.info("Encontradas {} áreas de impacto ativas", areas.size());

        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> listarInativas() {
        log.debug("Listando áreas de impacto inativas");

        List<AreaImpacto> areas = areaImpactoRepository.findByAtivaFalse();
        log.info("Encontradas {} áreas de impacto inativas", areas.size());

        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AreaImpactoDTO> listarAtivasPaginadas(Pageable pageable) {
        log.debug("Listando áreas de impacto ativas com paginação");

        Page<AreaImpacto> areas = areaImpactoRepository.findAllByAtivaTrue(pageable);
        return areas.map(AreaImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<AreaImpactoDTO> listarInativasPaginadas(Pageable pageable) {
        log.debug("Listando áreas de impacto inativas com paginação");

        Page<AreaImpacto> areas = areaImpactoRepository.findAllByAtivaFalse(pageable);
        return areas.map(AreaImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> listarPorStatusOrdenadas(Boolean ativa) {
        log.debug("Listando áreas de impacto por status: {} ordenadas", ativa);

        List<AreaImpacto> areas = areaImpactoRepository.findAllByAtivaOrderByOrdemAsc(ativa);
        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> listarTodasOrdenadas() {
        log.debug("Listando todas as áreas de impacto ordenadas");

        List<AreaImpacto> areas = areaImpactoRepository.findAllByOrderByOrdemAsc();
        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AreaImpactoDTO buscarPorId(String id) {
        log.debug("Buscando área de impacto por ID: {}", id);

        AreaImpacto area = buscarAreaOuLancarExcecao(id);
        return AreaImpactoDTO.from(area);
    }

    @Transactional(readOnly = true)
    public AreaImpactoDTO buscarPorNome(String nome) {
        log.debug("Buscando área de impacto por nome: {}", nome);

        AreaImpacto area = areaImpactoRepository.findByNome(nome)
                .orElseThrow(() -> {
                    log.error("Área de impacto não encontrada com nome: {}", nome);
                    return new RecursoNaoEncontradoException(
                            "Área de impacto não encontrada com nome: " + nome);
                });

        return AreaImpactoDTO.from(area);
    }

    @Transactional(readOnly = true)
    public AreaImpactoDTO buscarPorNomeIgnorandoCase(String nome) {
        log.debug("Buscando área de impacto por nome (case-insensitive): {}", nome);

        AreaImpacto area = areaImpactoRepository.findByNomeIgnoreCase(nome)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Área de impacto não encontrada com nome: " + nome));

        return AreaImpactoDTO.from(area);
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> buscarPorTermoNoNome(String termo) {
        log.debug("Buscando áreas de impacto contendo termo: {}", termo);

        List<AreaImpacto> areas = areaImpactoRepository.findByNomeContainingIgnoreCase(termo);
        log.info("Encontradas {} áreas contendo termo '{}'", areas.size(), termo);

        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> buscarPorCategoria(String categoria) {
        log.debug("Buscando áreas de impacto por categoria: {}", categoria);

        List<AreaImpacto> areas = areaImpactoRepository.findByCategoria(categoria);
        log.info("Encontradas {} áreas na categoria '{}'", areas.size(), categoria);

        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AreaImpactoDTO> buscarPorCategoriaPaginadas(String categoria, Pageable pageable) {
        log.debug("Buscando áreas de impacto por categoria: {} (paginado)", categoria);

        Page<AreaImpacto> areas = areaImpactoRepository.findAllByCategoria(categoria, pageable);
        return areas.map(AreaImpactoDTO::from);
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> buscarAtivasPorCategoria(String categoria) {
        log.debug("Buscando áreas ativas por categoria: {}", categoria);

        List<AreaImpacto> areas = areaImpactoRepository.findByCategoriaAndAtivaTrue(categoria);
        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> buscarPorCategoriaOrdenadas(String categoria) {
        log.debug("Buscando áreas por categoria: {} ordenadas", categoria);

        List<AreaImpacto> areas = areaImpactoRepository.findByCategoriaOrderByOrdemAsc(categoria);
        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> buscarPorKeywords(List<String> keywords) {
        log.debug("Buscando áreas de impacto por keywords: {}", keywords);

        List<AreaImpacto> areas = areaImpactoRepository.findByKeywordsContaining(keywords);
        log.info("Encontradas {} áreas com keywords: {}", areas.size(), keywords);

        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> buscarPorTodasKeywords(List<String> keywords) {
        log.debug("Buscando áreas que contêm TODAS as keywords: {}", keywords);

        List<AreaImpacto> areas = areaImpactoRepository.findByKeywordsContainingAll(keywords);
        log.info("Encontradas {} áreas com todas as keywords", areas.size());

        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> buscarPorGruposAfetados(List<String> grupos) {
        log.debug("Buscando áreas de impacto por grupos afetados: {}", grupos);

        List<AreaImpacto> areas = areaImpactoRepository.findByGruposAfetadosContaining(grupos);
        log.info("Encontradas {} áreas afetando grupos: {}", areas.size(), grupos);

        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> buscarPorTodosGruposAfetados(List<String> grupos) {
        log.debug("Buscando áreas que afetam TODOS os grupos: {}", grupos);

        List<AreaImpacto> areas = areaImpactoRepository.findByGruposAfetadosContainingAll(grupos);
        log.info("Encontradas {} áreas afetando todos os grupos", areas.size());

        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> buscarCriadasApos(LocalDateTime data) {
        log.debug("Buscando áreas criadas após: {}", data);

        List<AreaImpacto> areas = areaImpactoRepository.findByDataCriacaoAfter(data);
        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> buscarAtualizadasApos(LocalDateTime data) {
        log.debug("Buscando áreas atualizadas após: {}", data);

        List<AreaImpacto> areas = areaImpactoRepository.findByDataUltimaAtualizacaoAfter(data);
        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> buscarCriadasEntre(LocalDateTime inicio, LocalDateTime fim) {
        log.debug("Buscando áreas criadas entre {} e {}", inicio, fim);

        List<AreaImpacto> areas = areaImpactoRepository.findByDataCriacaoBetween(inicio, fim);
        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AreaImpactoDTO> buscarPorTextoLivre(String termo) {
        log.debug("Buscando áreas por texto livre: {}", termo);

        List<AreaImpacto> areas = areaImpactoRepository.buscarPorTextoLivre(termo);
        log.info("Encontradas {} áreas com termo '{}'", areas.size(), termo);

        return areas.stream()
                .map(AreaImpactoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AreaImpactoDTO> buscarAtivasPorTextoLivre(String termo, Pageable pageable) {
        log.debug("Buscando áreas ativas por texto livre: {}", termo);

        Page<AreaImpacto> areas = areaImpactoRepository.buscarAtivasPorTextoLivre(termo, pageable);
        return areas.map(AreaImpactoDTO::from);
    }

    @Transactional
    @CacheEvict(value = {"areasImpacto", "areasImpactoAtivas"}, allEntries = true)
    public AreaImpactoDTO criar(CriarAreaImpactoDTO dto) {
        log.info("Criando área de impacto: {}", dto.nome());

        validarNomeDuplicado(dto.nome(), null);

        AreaImpacto area = construirNovaArea(dto);
        area = areaImpactoRepository.save(area);

        log.info("Área de impacto criada com ID: {}", area.getId());
        return AreaImpactoDTO.from(area);
    }

    @Transactional
    @CacheEvict(value = {"areasImpacto", "areasImpactoAtivas"}, allEntries = true)
    public AreaImpactoDTO atualizar(String id, AtualizarAreaImpactoDTO dto) {
        log.info("Atualizando área de impacto: {}", id);

        AreaImpacto area = buscarAreaOuLancarExcecao(id);
        aplicarAtualizacoes(area, dto, id);

        area.setDataUltimaAtualizacao(LocalDateTime.now());
        area = areaImpactoRepository.save(area);

        log.info("Área de impacto atualizada: {}", id);
        return AreaImpactoDTO.from(area);
    }

    @Transactional
    @CacheEvict(value = {"areasImpacto", "areasImpactoAtivas"}, allEntries = true)
    public AreaImpactoDTO ativar(String id) {
        log.info("Ativando área de impacto: {}", id);

        AreaImpacto area = buscarAreaOuLancarExcecao(id);

        if (Boolean.TRUE.equals(area.getAtiva())) {
            log.warn("Área de impacto {} já está ativa", id);
        }

        area.setAtiva(true);
        area.setDataUltimaAtualizacao(LocalDateTime.now());
        area = areaImpactoRepository.save(area);

        log.info("Área de impacto ativada: {}", id);
        return AreaImpactoDTO.from(area);
    }

    @Transactional
    @CacheEvict(value = {"areasImpacto", "areasImpactoAtivas"}, allEntries = true)
    public AreaImpactoDTO desativar(String id) {
        log.info("Desativando área de impacto: {}", id);

        AreaImpacto area = buscarAreaOuLancarExcecao(id);

        if (Boolean.FALSE.equals(area.getAtiva())) {
            log.warn("Área de impacto {} já está inativa", id);
        }

        area.setAtiva(false);
        area.setDataUltimaAtualizacao(LocalDateTime.now());
        area = areaImpactoRepository.save(area);

        log.info("Área de impacto desativada: {}", id);
        return AreaImpactoDTO.from(area);
    }

    @Transactional
    @CacheEvict(value = {"areasImpacto", "areasImpactoAtivas"}, allEntries = true)
    public void deletar(String id) {
        log.warn("Deletando área de impacto: {}", id);

        AreaImpacto area = buscarAreaOuLancarExcecao(id);
        validarPossuiAnalises(id);

        areaImpactoRepository.delete(area);
        log.info("Área de impacto deletada: {}", id);
    }

    @Transactional(readOnly = true)
    public long contarAtivas() {
        long total = areaImpactoRepository.countByAtivaTrue();
        log.debug("Total de áreas ativas: {}", total);
        return total;
    }

    @Transactional(readOnly = true)
    public long contarInativas() {
        long total = areaImpactoRepository.countByAtivaFalse();
        log.debug("Total de áreas inativas: {}", total);
        return total;
    }

    @Transactional(readOnly = true)
    public long contarPorCategoria(String categoria) {
        long total = areaImpactoRepository.countByCategoria(categoria);
        log.debug("Total de áreas na categoria '{}': {}", categoria, total);
        return total;
    }

    @Transactional(readOnly = true)
    public boolean existePorNome(String nome) {
        return areaImpactoRepository.existsByNome(nome);
    }

    @Transactional(readOnly = true)
    public boolean existePorNomeIgnorandoCase(String nome) {
        return areaImpactoRepository.existsByNomeIgnoreCase(nome);
    }

    private AreaImpacto buscarAreaOuLancarExcecao(String id) {
        return areaImpactoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Área de impacto não encontrada: {}", id);
                    return new RecursoNaoEncontradoException(
                            "Área de impacto não encontrada: " + id);
                });
    }

    private void validarNomeDuplicado(String nome, String idExcluir) {
        boolean existe = idExcluir != null
                ? areaImpactoRepository.existsByNomeAndIdNot(nome, idExcluir)
                : areaImpactoRepository.existsByNome(nome);

        if (existe) {
            log.error("Já existe área de impacto com o nome: {}", nome);
            throw new ValidacaoException(
                    "Já existe uma área de impacto com o nome: " + nome);
        }
    }

    private void validarPossuiAnalises(String areaId) {
        long totalAnalises = analiseImpactoRepository.countByAreaImpacto_Id(areaId);

        if (totalAnalises > 0) {
            log.error("Área de impacto {} possui {} análises associadas", areaId, totalAnalises);
            throw new ValidacaoException(
                    String.format("Não é possível deletar área com %d análises associadas. " +
                            "Desative a área ao invés de deletá-la.", totalAnalises));
        }
    }

    private AreaImpacto construirNovaArea(CriarAreaImpactoDTO dto) {
        LocalDateTime agora = LocalDateTime.now();

        return AreaImpacto.builder()
                .nome(dto.nome())
                .descricao(dto.descricao())
                .keywords(dto.keywords())
                .gruposAfetados(dto.gruposAfetados())
                .categoria(dto.categoria())
                .ativa(dto.ativa() != null ? dto.ativa() : true)
                .ordem(dto.ordem() != null ? dto.ordem() : 0)
                .dataCriacao(agora)
                .dataUltimaAtualizacao(agora)
                .build();
    }

    private void aplicarAtualizacoes(AreaImpacto area, AtualizarAreaImpactoDTO dto, String id) {
        if (dto.nome() != null && !dto.nome().equals(area.getNome())) {
            validarNomeDuplicado(dto.nome(), id);
            area.setNome(dto.nome());
        }

        if (dto.descricao() != null) {
            area.setDescricao(dto.descricao());
        }

        if (dto.keywords() != null) {
            area.setKeywords(dto.keywords());
        }

        if (dto.gruposAfetados() != null) {
            area.setGruposAfetados(dto.gruposAfetados());
        }

        if (dto.categoria() != null) {
            area.setCategoria(dto.categoria());
        }

        if (dto.ativa() != null) {
            area.setAtiva(dto.ativa());
        }

        if (dto.ordem() != null) {
            area.setOrdem(dto.ordem());
        }
    }
}