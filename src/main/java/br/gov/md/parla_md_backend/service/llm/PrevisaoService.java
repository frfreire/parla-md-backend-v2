package br.gov.md.parla_md_backend.service.llm;

import br.gov.md.parla_md_backend.domain.legislativo.ItemLegislativo;
import br.gov.md.parla_md_backend.domain.enums.NivelConfianca;
import br.gov.md.parla_md_backend.domain.parlamentar.PerfilParlamentar;
import br.gov.md.parla_md_backend.domain.enums.TendenciaVoto;
import br.gov.md.parla_md_backend.domain.dto.AnalisePreditivaDTO;
import br.gov.md.parla_md_backend.domain.dto.PrevisaoVotoDTO;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IPerfilParlamentarRepository;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrevisaoService {

    private final LlamaService llama;
    private final IPerfilParlamentarRepository perfilRepository;
    private final IProposicaoRepository proposicaoRepository;
    private final ObjectMapper objectMapper;

    public AnalisePreditivaDTO preverAprovacao(String proposicaoId) {
        ItemLegislativo item = proposicaoRepository.findById(proposicaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Proposição não encontrada"));

        List<PerfilParlamentar> perfis = perfilRepository.findByCasa(item.getCasa().name());

        String prompt = construirPromptPredicao(item, perfis);
        String systemPrompt = """
                Você é um especialista em análise política e previsão de votações legislativas.
                Sua tarefa é analisar dados históricos de parlamentares e prever comportamentos de votação.
                Sempre forneça análises baseadas em dados, com níveis de confiança apropriados.
                Responda sempre em formato JSON estruturado.
                """;

        String resposta = llama.generate(prompt, systemPrompt);

        return parseRespostaPredicao(resposta, item, perfis);
    }

    public PrevisaoVotoDTO preverVotoParlamentar(String parlamentarId, String tema) {
        PerfilParlamentar perfil = perfilRepository.findByParlamentarId(parlamentarId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil parlamentar não encontrado"));

        String prompt = construirPromptPrevisaoIndividual(perfil, tema);
        String systemPrompt = """
                Você é um especialista em análise política e previsão de votações legislativas.
                Sua tarefa é analisar dados históricos de parlamentares e prever comportamentos de votação.
                Sempre forneça análises baseadas em dados, com níveis de confiança apropriados.
                Responda sempre em formato JSON estruturado.
                """;

        String resposta = llama.generate(prompt, systemPrompt);

        return parseRespostaPrevisaoIndividual(resposta, perfil, tema);
    }

    private String construirPromptPredicao(ItemLegislativo item, List<PerfilParlamentar> perfis) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analise a seguinte proposição legislativa e preveja sua aprovação:\n\n");
        prompt.append("PROPOSIÇÃO:\n");
        prompt.append("Tipo: ").append(item.getTipo()).append("\n");
        prompt.append("Número: ").append(item.getIdentificadorCompleto()).append("\n");
        prompt.append("Ementa: ").append(item.getEmenta()).append("\n");
        prompt.append("Tema: ").append(item.getTema()).append("\n\n");

        prompt.append("PERFIL DOS PARLAMENTARES:\n");
        prompt.append("Total de parlamentares analisados: ").append(perfis.size()).append("\n");

        long favoraveis = perfis.stream()
                .filter(p -> calcularTendencia(p, item.getTema()) == TendenciaVoto.FAVORAVEL)
                .count();

        prompt.append("Parlamentares com histórico favorável ao tema: ").append(favoraveis).append("\n\n");

        prompt.append("Com base nessas informações, forneça:\n");
        prompt.append("1. Probabilidade de aprovação (0-100%)\n");
        prompt.append("2. Nível de confiança da predição\n");
        prompt.append("3. Principais fatores que influenciam\n");
        prompt.append("4. Recomendação estratégica\n\n");
        prompt.append("Responda em formato JSON.");

        return prompt.toString();
    }

    private String construirPromptPrevisaoIndividual(PerfilParlamentar perfil, String tema) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analise o perfil do parlamentar e preveja como votaria:\n\n");
        prompt.append("PARLAMENTAR:\n");
        prompt.append("Nome: ").append(perfil.getNomeParlamentar()).append("\n");
        prompt.append("Partido: ").append(perfil.getPartido()).append("\n");
        prompt.append("Alinhamento: ").append(perfil.getAlinhamentoGoverno()).append("\n");
        prompt.append("Alinhamento com governo: ").append(perfil.getPercentualAlinhamentoGoverno()).append("%\n\n");

        prompt.append("TEMA DA VOTAÇÃO: ").append(tema).append("\n\n");

        var posicionamento = perfil.buscarPosicionamentoTema(tema);
        if (posicionamento != null) {
            prompt.append("HISTÓRICO NO TEMA:\n");
            prompt.append("Votações analisadas: ").append(posicionamento.getVotacoesAnalisadas()).append("\n");
            prompt.append("Favorável: ").append(posicionamento.getPercentualFavoravel()).append("%\n");
            prompt.append("Contrário: ").append(posicionamento.getPercentualContrario()).append("%\n\n");
        }

        prompt.append("Preveja:\n");
        prompt.append("1. Tendência de voto (FAVORAVEL, CONTRARIO, ABSTENCAO, INCERTO)\n");
        prompt.append("2. Probabilidades para cada opção\n");
        prompt.append("3. Justificativa\n");
        prompt.append("4. Fatores que influenciam\n\n");
        prompt.append("Responda em formato JSON.");

        return prompt.toString();
    }

    private AnalisePreditivaDTO parseRespostaPredicao(String resposta, ItemLegislativo item, List<PerfilParlamentar> perfis) {
        try {
            Map<String, Object> json = objectMapper.readValue(resposta, Map.class);

            double probabilidade = ((Number) json.get("probabilidadeAprovacao")).doubleValue();

            return new AnalisePreditivaDTO(
                    item.getId(),
                    item.getEmenta(),
                    item.getTema(),
                    probabilidade,
                    (String) json.get("nivelConfianca"),
                    new ArrayList<>(),
                    0, 0, 0,
                    (List<String>) json.get("fatores"),
                    (String) json.get("recomendacao"),
                    (String) json.get("resumo")
            );
        } catch (Exception e) {
            log.error("Erro ao parsear resposta de predição", e);
            throw new RuntimeException("Erro ao processar resposta LLM", e);
        }
    }

    private PrevisaoVotoDTO parseRespostaPrevisaoIndividual(String resposta, PerfilParlamentar perfil, String tema) {
        try {
            Map<String, Object> json = objectMapper.readValue(resposta, Map.class);

            TendenciaVoto tendencia = TendenciaVoto.valueOf((String) json.get("tendencia"));
            double probabilidadeFavoravel = ((Number) json.get("probabilidadeFavoravel")).doubleValue();

            return new PrevisaoVotoDTO(
                    perfil.getParlamentarId(),
                    perfil.getNomeParlamentar(),
                    perfil.getPartido(),
                    perfil.getUf(),
                    tema,
                    tendencia,
                    NivelConfianca.fromProbabilidade(probabilidadeFavoravel),
                    probabilidadeFavoravel,
                    ((Number) json.get("probabilidadeContrario")).doubleValue(),
                    ((Number) json.get("probabilidadeAbstencao")).doubleValue(),
                    (String) json.get("justificativa"),
                    (List<String>) json.get("fatores"),
                    (Integer) json.get("votacoesSimilares"),
                    (String) json.get("recomendacao")
            );
        } catch (Exception e) {
            log.error("Erro ao parsear resposta de previsão individual", e);
            throw new RuntimeException("Erro ao processar resposta LLM", e);
        }
    }

    private TendenciaVoto calcularTendencia(PerfilParlamentar perfil, String tema) {
        var posicionamento = perfil.buscarPosicionamentoTema(tema);
        if (posicionamento == null) {
            return TendenciaVoto.INCERTO;
        }
        return posicionamento.getTendenciaPredominante();
    }
}