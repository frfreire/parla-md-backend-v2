package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.MetricasDesempenho;
import br.gov.md.parla_md_backend.domain.PerfilParlamentar;
import br.gov.md.parla_md_backend.domain.dto.RankingParlamentarDTO;
import br.gov.md.parla_md_backend.repository.IPerfilParlamentarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricasDesempenhoService {

    private final IPerfilParlamentarRepository perfilRepository;

    @Transactional
    public void calcularMetricas() {
        log.info("Iniciando cálculo de métricas de desempenho");

        List<PerfilParlamentar> perfis = perfilRepository.findAll();

        calcularRankingGeral(perfis);
        calcularRankingsPorPartido(perfis);
        calcularRankingsPorEstado(perfis);

        perfilRepository.saveAll(perfis);

        log.info("Métricas calculadas para {} parlamentares", perfis.size());
    }

    public List<RankingParlamentarDTO> obterRankingGeral(int limite) {
        return perfilRepository.findAll(Pageable.ofSize(limite))
                .stream()
                .filter(p -> p.getMetricas() != null)
                .sorted(Comparator.comparingInt(p -> p.getMetricas().getRankingGeral()))
                .map(this::converterParaRankingDTO)
                .toList();
    }

    public List<RankingParlamentarDTO> obterRankingPorPartido(String partido) {
        return perfilRepository.findByPartidoOrderByRankingPartido(partido)
                .stream()
                .map(this::converterParaRankingDTO)
                .toList();
    }

    public List<RankingParlamentarDTO> obterRankingPorEstado(String uf) {
        return perfilRepository.findByUfOrderByRankingEstado(uf)
                .stream()
                .map(this::converterParaRankingDTO)
                .toList();
    }

    private void calcularRankingGeral(List<PerfilParlamentar> perfis) {
        List<PerfilParlamentar> comMetricas = perfis.stream()
                .filter(p -> p.getMetricas() != null)
                .sorted((p1, p2) -> Double.compare(
                        calcularScoreGeral(p2.getMetricas()),
                        calcularScoreGeral(p1.getMetricas())
                ))
                .toList();

        for (int i = 0; i < comMetricas.size(); i++) {
            comMetricas.get(i).getMetricas().setRankingGeral(i + 1);
        }
    }

    private void calcularRankingsPorPartido(List<PerfilParlamentar> perfis) {
        Map<String, List<PerfilParlamentar>> porPartido = perfis.stream()
                .filter(p -> p.getMetricas() != null)
                .collect(Collectors.groupingBy(PerfilParlamentar::getPartido));

        porPartido.forEach((partido, parlamentares) -> {
            List<PerfilParlamentar> ordenados = parlamentares.stream()
                    .sorted((p1, p2) -> Double.compare(
                            calcularScoreGeral(p2.getMetricas()),
                            calcularScoreGeral(p1.getMetricas())
                    ))
                    .toList();

            for (int i = 0; i < ordenados.size(); i++) {
                ordenados.get(i).getMetricas().setRankingPartido(i + 1);
            }
        });
    }

    private void calcularRankingsPorEstado(List<PerfilParlamentar> perfis) {
        Map<String, List<PerfilParlamentar>> porEstado = perfis.stream()
                .filter(p -> p.getMetricas() != null)
                .collect(Collectors.groupingBy(PerfilParlamentar::getUf));

        porEstado.forEach((uf, parlamentares) -> {
            List<PerfilParlamentar> ordenados = parlamentares.stream()
                    .sorted((p1, p2) -> Double.compare(
                            calcularScoreGeral(p2.getMetricas()),
                            calcularScoreGeral(p1.getMetricas())
                    ))
                    .toList();

            for (int i = 0; i < ordenados.size(); i++) {
                ordenados.get(i).getMetricas().setRankingEstado(i + 1);
            }
        });
    }

    private double calcularScoreGeral(MetricasDesempenho metricas) {
        double scorePresenca = metricas.getTaxaPresenca() * 0.2;
        double scoreAtividade = metricas.getIndiceAtividade() * 0.3;
        double scoreProdutividade = metricas.getIndiceProdutividade() * 0.5;

        return scorePresenca + scoreAtividade + scoreProdutividade;
    }

    private RankingParlamentarDTO converterParaRankingDTO(PerfilParlamentar perfil) {
        return new RankingParlamentarDTO(
                perfil.getMetricas().getRankingGeral(),
                perfil.getParlamentarId(),
                perfil.getNomeParlamentar(),
                perfil.getPartido(),
                perfil.getUf(),
                perfil.getCasa(),
                calcularScoreGeral(perfil.getMetricas()),
                "Geral",
                new br.gov.md.parla_md_backend.domain.dto.MetricasDesempenhoDTO(
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
                )
        );
    }
}