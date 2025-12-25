package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.OrgaoExterno;
import br.gov.md.parla_md_backend.domain.dto.CriarOrgaoExternoDTO;
import br.gov.md.parla_md_backend.domain.dto.OrgaoExternoDTO;
import br.gov.md.parla_md_backend.domain.dto.AtualizarOrgaoExternoDTO;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IOrgaoExternoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrgaoExternoService {

    private final IOrgaoExternoRepository orgaoExternoRepository;

    @Transactional
    public OrgaoExternoDTO criar(CriarOrgaoExternoDTO dto) {
        log.info("Criando órgão externo: {}", dto.nome());

        validarSiglaUnica(dto.sigla());

        OrgaoExterno orgao = OrgaoExterno.builder()
                .nome(dto.nome())
                .sigla(dto.sigla())
                .tipo(dto.tipo())
                .descricao(dto.descricao())
                .emailOficial(dto.emailOficial())
                .telefone(dto.telefone())
                .endereco(dto.endereco())
                .representantes(dto.representantes())
                .ativo(true)
                .observacoes(dto.observacoes())
                .build();

        orgao = orgaoExternoRepository.save(orgao);

        log.info("Órgão externo {} criado com sucesso", orgao.getSigla());

        return converterParaDTO(orgao);
    }

    @Transactional
    public OrgaoExternoDTO atualizar(String id, AtualizarOrgaoExternoDTO dto) {
        log.info("Atualizando órgão externo: {}", id);

        OrgaoExterno orgao = orgaoExternoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Órgão externo não encontrado"));

        if (dto.nome() != null) {
            orgao.setNome(dto.nome());
        }

        if (dto.sigla() != null && !dto.sigla().equals(orgao.getSigla())) {
            validarSiglaUnica(dto.sigla());
            orgao.setSigla(dto.sigla());
        }

        if (dto.tipo() != null) {
            orgao.setTipo(dto.tipo());
        }

        if (dto.descricao() != null) {
            orgao.setDescricao(dto.descricao());
        }

        if (dto.emailOficial() != null) {
            orgao.setEmailOficial(dto.emailOficial());
        }

        if (dto.telefone() != null) {
            orgao.setTelefone(dto.telefone());
        }

        if (dto.endereco() != null) {
            orgao.setEndereco(dto.endereco());
        }

        if (dto.representantes() != null) {
            orgao.setRepresentantes(dto.representantes());
        }

        if (dto.observacoes() != null) {
            orgao.setObservacoes(dto.observacoes());
        }

        orgao = orgaoExternoRepository.save(orgao);

        log.info("Órgão externo {} atualizado com sucesso", orgao.getSigla());

        return converterParaDTO(orgao);
    }

    public OrgaoExternoDTO buscarPorId(String id) {
        log.debug("Buscando órgão externo por ID: {}", id);

        OrgaoExterno orgao = orgaoExternoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Órgão externo não encontrado"));

        return converterParaDTO(orgao);
    }

    public OrgaoExternoDTO buscarPorSigla(String sigla) {
        log.debug("Buscando órgão externo por sigla: {}", sigla);

        OrgaoExterno orgao = orgaoExternoRepository.findBySigla(sigla)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Órgão externo não encontrado"));

        return converterParaDTO(orgao);
    }

    public Page<OrgaoExternoDTO> listar(Pageable pageable) {
        log.debug("Listando órgãos externos - página: {}", pageable.getPageNumber());

        return orgaoExternoRepository.findAll(pageable)
                .map(this::converterParaDTO);
    }

    public List<OrgaoExternoDTO> listarAtivos() {
        log.debug("Listando órgãos externos ativos");

        return orgaoExternoRepository.findByAtivoTrue()
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public List<OrgaoExternoDTO> buscarPorNome(String nome) {
        log.debug("Buscando órgãos externos por nome: {}", nome);

        return orgaoExternoRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional
    public OrgaoExternoDTO ativar(String id) {
        log.info("Ativando órgão externo: {}", id);

        OrgaoExterno orgao = orgaoExternoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Órgão externo não encontrado"));

        orgao.setAtivo(true);
        orgao = orgaoExternoRepository.save(orgao);

        log.info("Órgão externo {} ativado", orgao.getSigla());

        return converterParaDTO(orgao);
    }

    @Transactional
    public OrgaoExternoDTO desativar(String id) {
        log.info("Desativando órgão externo: {}", id);

        OrgaoExterno orgao = orgaoExternoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Órgão externo não encontrado"));

        orgao.setAtivo(false);
        orgao = orgaoExternoRepository.save(orgao);

        log.info("Órgão externo {} desativado", orgao.getSigla());

        return converterParaDTO(orgao);
    }

    @Transactional
    public OrgaoExternoDTO adicionarRepresentante(String id, OrgaoExterno.Representante representante) {
        log.info("Adicionando representante ao órgão externo: {}", id);

        OrgaoExterno orgao = orgaoExternoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Órgão externo não encontrado"));

        if (representante.isPrincipal()) {
            orgao.getRepresentantes().forEach(r -> r.setPrincipal(false));
        }

        orgao.getRepresentantes().add(representante);
        orgao = orgaoExternoRepository.save(orgao);

        log.info("Representante {} adicionado ao órgão {}", representante.getNome(), orgao.getSigla());

        return converterParaDTO(orgao);
    }

    @Transactional
    public OrgaoExternoDTO removerRepresentante(String id, int index) {
        log.info("Removendo representante do órgão externo: {}", id);

        OrgaoExterno orgao = orgaoExternoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Órgão externo não encontrado"));

        if (index < 0 || index >= orgao.getRepresentantes().size()) {
            throw new IllegalArgumentException("Índice de representante inválido");
        }

        orgao.getRepresentantes().remove(index);
        orgao = orgaoExternoRepository.save(orgao);

        log.info("Representante removido do órgão {}", orgao.getSigla());

        return converterParaDTO(orgao);
    }

    @Transactional
    public void deletar(String id) {
        log.info("Deletando órgão externo: {}", id);

        OrgaoExterno orgao = orgaoExternoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Órgão externo não encontrado"));

        orgaoExternoRepository.delete(orgao);

        log.info("Órgão externo {} deletado", orgao.getSigla());
    }

    private void validarSiglaUnica(String sigla) {
        if (orgaoExternoRepository.existsBySigla(sigla)) {
            throw new IllegalArgumentException("Já existe órgão externo com esta sigla: " + sigla);
        }
    }

    private OrgaoExternoDTO converterParaDTO(OrgaoExterno orgao) {
        return new OrgaoExternoDTO(
                orgao.getId(),
                orgao.getNome(),
                orgao.getSigla(),
                orgao.getTipo(),
                orgao.getDescricao(),
                orgao.getEmailOficial(),
                orgao.getTelefone(),
                orgao.getEndereco(),
                orgao.getRepresentantes(),
                orgao.isAtivo(),
                orgao.getObservacoes()
        );
    }
}