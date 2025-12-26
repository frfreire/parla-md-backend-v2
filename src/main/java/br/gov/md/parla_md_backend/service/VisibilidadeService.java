package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.Parecer;
import br.gov.md.parla_md_backend.domain.Tramitacao;
import br.gov.md.parla_md_backend.domain.Usuario;
import br.gov.md.parla_md_backend.domain.ControleVisibilidade;
import br.gov.md.parla_md_backend.domain.enums.NivelVisibilidade;
import br.gov.md.parla_md_backend.domain.PermissaoAcesso;
import br.gov.md.parla_md_backend.domain.dto.ConcederPermissaoDTO;
import br.gov.md.parla_md_backend.domain.dto.ControleVisibilidadeDTO;
import br.gov.md.parla_md_backend.exception.AcessoNegadoException;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IParecerRepository;
import br.gov.md.parla_md_backend.repository.ITramitacaoRepository;
import br.gov.md.parla_md_backend.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisibilidadeService {

    private final IParecerRepository parecerRepository;
    private final ITramitacaoRepository tramitacaoRepository;
    private final IUsuarioRepository usuarioRepository;

    @Transactional
    public Parecer definirVisibilidadeParecer(String parecerId, ControleVisibilidadeDTO dto, String usuarioId) {
        Parecer parecer = parecerRepository.findById(parecerId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Parecer não encontrado"));

        validarAutoriaOuAdmin(parecer.getAnalista(), usuarioId);

        Usuario usuario = buscarUsuario(usuarioId);

        ControleVisibilidade controle = construirControleVisibilidade(dto, usuario);
        parecer.setControleVisibilidade(controle);

        log.info("Visibilidade definida para parecer {} - Nível: {}", parecerId, dto.nivelVisibilidade());
        return parecerRepository.save(parecer);
    }

    @Transactional
    public Tramitacao definirVisibilidadeTramitacao(String tramitacaoId, ControleVisibilidadeDTO dto, String usuarioId) {
        Tramitacao tramitacao = tramitacaoRepository.findById(tramitacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tramitação não encontrada"));

        validarAutoriaOuAdmin(tramitacao.getRemetenteId(), usuarioId);

        Usuario usuario = buscarUsuario(usuarioId);

        ControleVisibilidade controle = construirControleVisibilidade(dto, usuario);
        tramitacao.setControleVisibilidade(controle);

        log.info("Visibilidade definida para tramitação {} - Nível: {}", tramitacaoId, dto.nivelVisibilidade());
        return tramitacaoRepository.save(tramitacao);
    }

    @Transactional
    public Parecer concederPermissaoParecer(String parecerId, ConcederPermissaoDTO dto, String concedenteId) {
        Parecer parecer = parecerRepository.findById(parecerId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Parecer não encontrado"));

        validarAutoriaOuAdmin(parecer.getAnalista(), concedenteId);

        Usuario concedente = buscarUsuario(concedenteId);

        ControleVisibilidade controle = parecer.getControleVisibilidade();
        if (controle == null) {
            controle = criarControleVisibilidadePadrao(parecer.getAnalista());
            parecer.setControleVisibilidade(controle);
        }

        PermissaoAcesso permissao = PermissaoAcesso.builder()
                .usuarioId(dto.usuarioId())
                .usuarioNome(dto.usuarioNome())
                .tipoPermissao(dto.tipoPermissao())
                .dataConcessao(LocalDateTime.now())
                .concedidoPorId(concedente.getId())
                .concedidoPorNome(concedente.getNome())
                .dataExpiracao(dto.dataExpiracao())
                .justificativa(dto.justificativa())
                .ativa(true)
                .build();

        controle.adicionarPermissao(permissao);
        controle.setDataUltimaAlteracao(LocalDateTime.now());

        log.info("Permissão concedida no parecer {} para usuário {}", parecerId, dto.usuarioId());
        return parecerRepository.save(parecer);
    }

    @Transactional
    public Tramitacao concederPermissaoTramitacao(String tramitacaoId, ConcederPermissaoDTO dto, String concedenteId) {
        Tramitacao tramitacao = tramitacaoRepository.findById(tramitacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tramitação não encontrada"));

        validarAutoriaOuAdmin(tramitacao.getRemetenteId(), concedenteId);

        Usuario concedente = buscarUsuario(concedenteId);

        ControleVisibilidade controle = tramitacao.getControleVisibilidade();
        if (controle == null) {
            controle = criarControleVisibilidadePadrao(tramitacao.getRemetenteId());
            tramitacao.setControleVisibilidade(controle);
        }

        PermissaoAcesso permissao = PermissaoAcesso.builder()
                .usuarioId(dto.usuarioId())
                .usuarioNome(dto.usuarioNome())
                .tipoPermissao(dto.tipoPermissao())
                .dataConcessao(LocalDateTime.now())
                .concedidoPorId(concedente.getId())
                .concedidoPorNome(concedente.getNome())
                .dataExpiracao(dto.dataExpiracao())
                .justificativa(dto.justificativa())
                .ativa(true)
                .build();

        controle.adicionarPermissao(permissao);
        controle.setDataUltimaAlteracao(LocalDateTime.now());

        log.info("Permissão concedida na tramitação {} para usuário {}", tramitacaoId, dto.usuarioId());
        return tramitacaoRepository.save(tramitacao);
    }

    @Transactional
    public Parecer revogarPermissaoParecer(String parecerId, String usuarioId, String revogadorId) {
        Parecer parecer = parecerRepository.findById(parecerId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Parecer não encontrado"));

        validarAutoriaOuAdmin(parecer.getAnalista(), revogadorId);

        ControleVisibilidade controle = parecer.getControleVisibilidade();
        if (controle != null) {
            controle.removerPermissao(usuarioId);
            controle.setDataUltimaAlteracao(LocalDateTime.now());
        }

        log.info("Permissão revogada no parecer {} para usuário {}", parecerId, usuarioId);
        return parecerRepository.save(parecer);
    }

    @Transactional
    public Tramitacao revogarPermissaoTramitacao(String tramitacaoId, String usuarioId, String revogadorId) {
        Tramitacao tramitacao = tramitacaoRepository.findById(tramitacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tramitação não encontrada"));

        validarAutoriaOuAdmin(tramitacao.getRemetenteId(), revogadorId);

        ControleVisibilidade controle = tramitacao.getControleVisibilidade();
        if (controle != null) {
            controle.removerPermissao(usuarioId);
            controle.setDataUltimaAlteracao(LocalDateTime.now());
        }

        log.info("Permissão revogada na tramitação {} para usuário {}", tramitacaoId, usuarioId);
        return tramitacaoRepository.save(tramitacao);
    }

    public boolean verificarAcessoParecer(String parecerId, String usuarioId) {
        Parecer parecer = parecerRepository.findById(parecerId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Parecer não encontrado"));

        return verificarAcesso(parecer.getControleVisibilidade(), parecer.getAnalista(), usuarioId);
    }

    public boolean verificarAcessoTramitacao(String tramitacaoId, String usuarioId) {
        Tramitacao tramitacao = tramitacaoRepository.findById(tramitacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tramitação não encontrada"));

        return verificarAcesso(tramitacao.getControleVisibilidade(), tramitacao.getRemetenteId(), usuarioId);
    }

    public void validarAcessoParecer(String parecerId, String usuarioId) {
        if (!verificarAcessoParecer(parecerId, usuarioId)) {
            throw new AcessoNegadoException("Você não tem permissão para acessar este parecer");
        }
    }

    public void validarAcessoTramitacao(String tramitacaoId, String usuarioId) {
        if (!verificarAcessoTramitacao(tramitacaoId, usuarioId)) {
            throw new AcessoNegadoException("Você não tem permissão para acessar esta tramitação");
        }
    }

    public List<Parecer> filtrarPareceresComAcesso(List<Parecer> pareceres, String usuarioId) {
        return pareceres.stream()
                .filter(p -> verificarAcesso(p.getControleVisibilidade(), p.getAnalista(), usuarioId))
                .toList();
    }

    public List<Tramitacao> filtrarTramitacoesComAcesso(List<Tramitacao> tramitacoes, String usuarioId) {
        return tramitacoes.stream()
                .filter(t -> verificarAcesso(t.getControleVisibilidade(), t.getRemetenteId(), usuarioId))
                .toList();
    }

    private boolean verificarAcesso(ControleVisibilidade controle, String autorId, String usuarioId) {
        if (controle == null || controle.getNivelVisibilidade() == NivelVisibilidade.PUBLICO) {
            return true;
        }

        if (autorId.equals(usuarioId)) {
            return true;
        }

        if (controle.isExpirado()) {
            return true;
        }

        if (controle.getNivelVisibilidade() == NivelVisibilidade.PRIVADO) {
            return false;
        }

        Usuario usuario = buscarUsuario(usuarioId);

        if (controle.getNivelVisibilidade() == NivelVisibilidade.RESTRITO_SETOR) {
            if (controle.getSetoresAutorizados() == null || controle.getSetoresAutorizados().isEmpty()) {
                return false;
            }
            return controle.getSetoresAutorizados().contains(usuario.getSetorId());
        }

        if (controle.getNivelVisibilidade() == NivelVisibilidade.RESTRITO_INDIVIDUAL) {
            PermissaoAcesso permissao = controle.buscarPermissaoUsuario(usuarioId);
            return permissao != null && permissao.isValida();
        }

        return false;
    }

    private ControleVisibilidade construirControleVisibilidade(ControleVisibilidadeDTO dto, Usuario usuario) {
        ControleVisibilidade controle = ControleVisibilidade.builder()
                .nivelVisibilidade(dto.nivelVisibilidade())
                .autorId(usuario.getId())
                .autorNome(usuario.getNome())
                .dataDefinicao(LocalDateTime.now())
                .dataUltimaAlteracao(LocalDateTime.now())
                .justificativaRestricao(dto.justificativaRestricao())
                .permitirVisualizacaoSuperior(dto.permitirVisualizacaoSuperior())
                .dataExpiracao(dto.dataExpiracao())
                .build();

        if (dto.setoresAutorizados() != null) {
            dto.setoresAutorizados().forEach(controle::adicionarSetorAutorizado);
        }

        return controle;
    }

    private ControleVisibilidade criarControleVisibilidadePadrao(String autorId) {
        Usuario usuario = buscarUsuario(autorId);
        return ControleVisibilidade.builder()
                .nivelVisibilidade(NivelVisibilidade.PUBLICO)
                .autorId(usuario.getId())
                .autorNome(usuario.getNome())
                .dataDefinicao(LocalDateTime.now())
                .permitirVisualizacaoSuperior(true)
                .build();
    }

    private void validarAutoriaOuAdmin(String autorId, String usuarioId) {
        if (!autorId.equals(usuarioId)) {
            Usuario usuario = buscarUsuario(usuarioId);
            if (usuario.getRoles() == null || !usuario.getRoles().contains("ROLE_ADMIN")) {
                throw new AcessoNegadoException("Apenas o autor ou administrador pode alterar a visibilidade");
            }
        }
    }

    private Usuario buscarUsuario(String usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }
}