package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.enums.AlinhamentoPolitico;
import br.gov.md.parla_md_backend.domain.PerfilParlamentar;
import br.gov.md.parla_md_backend.domain.dto.AnaliseParlamentarDTO;
import br.gov.md.parla_md_backend.domain.dto.MetricasDesempenhoDTO;
import br.gov.md.parla_md_backend.domain.dto.PosicionamentoTematicoDTO;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IPerfilParlamentarRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnaliseParlamentarService {

    private final IPerfilParlamentarRepository perfilRepository;
    private final LlamaService llama;
    private final ObjectMapper objectMapper;

    public AnaliseParlamentarDTO buscarAnalise(String parlamentarId) {
        PerfilParlamentar perfil = perfilRepository.findByParlamentarId(parlamentarId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil parlamentar não encontrado"));

        return converterParaDTO(perfil);
    }

    public List<AnaliseParlamentarDTO> buscarPorAlinhamento(AlinhamentoPolitico alinhamento) {
        return perfilRepository.findByAlinhamentoGoverno(alinhamento)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public List<AnaliseParlamentarDTO> buscarPorTema(String tema) {
        return perfilRepository.findByTemaInteresse(tema)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public Page<AnaliseParlamentarDTO> buscarRankingPorCasa(String casa, Pageable pageable) {
        return perfilRepository.findByCasaOrderByRanking(casa, pageable)
                .map(this::converterParaDTO);
    }

    public List<AnaliseParlamentarDTO> buscarTopDefesa(int quantidade) {
        return perfilRepository.findTopParlamentaresDefesa(Pageable.ofSize(quantidade))
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private AnaliseParlamentarDTO converterParaDTO(PerfilParlamentar perfil) {
        List<PosicionamentoTematicoDTO> posicionamentos = perfil.getPosicionamentosTematicos()
                .stream()
                .map(p -> new PosicionamentoTematicoDTO(
                        p.getTema(),
                        p.getTendenciaPredominante(),
                        p.getVotacoesAnalisadas(),
                        p.getPercentualFavoravel(),
                        p.getPercentualContrario(),
                        p.getPercentualAbstencao(),
                        p.getNivelConfianca(),
                        p.getObservacoes()
                ))
                .toList();

        MetricasDesempenhoDTO metricas = perfil.getMetricas() != null ?
                new MetricasDesempenhoDTO(
                        perfil.getMetricas().getTaxaPresenca(),
                        perfil.getMetricas().getProposicoesApresentadas(),
                        perfil.getMetricas().getProposicoesAprovadas(),
                        perfil.getMetricas().getTaxaAprovacao(),
                        perfil.getMetricas().getDiscursosRealizados(),
                        perfil.getMetricas().getParticipacaoComissoes(),
                        perfil.getMetricas().getIndiceAtividade(),
                        perfil.getMetricas().getIndiceProdutividade(),
                        perfil.getMetricas().getRankingGeral(),
                        perfil.getMetricas().getRankingPartido(),
                        perfil.getMetricas().getRankingEstado()
                ) : null;

        return new AnaliseParlamentarDTO(
                perfil.getParlamentarId(),
                perfil.getNomeParlamentar(),
                perfil.getPartido(),
                perfil.getUf(),
                perfil.getCasa(),
                perfil.getAlinhamentoGoverno(),
                perfil.getPercentualAlinhamentoGoverno(),
                posicionamentos,
                metricas,
                perfil.getResumoIA(),
                perfil.getPontosFortes(),
                perfil.getAreasInteresse(),
                perfil.getEstrategiaAbordagem(),
                perfil.getTotalVotacoes(),
                perfil.getVotacoesDefesa(),
                perfil.getDataUltimaAnalise(),
                perfil.getVersaoModeloIA()
        );
    }
}