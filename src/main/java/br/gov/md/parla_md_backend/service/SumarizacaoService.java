package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.dto.SumarizacaoDTO;
import br.gov.md.parla_md_backend.domain.ItemLegislativo;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SumarizacaoService {

    private final LlamaService llama;
    private final IProposicaoRepository proposicaoRepository;
    private final ObjectMapper objectMapper;

    public SumarizacaoDTO sumarizar(String proposicaoId) {
        ItemLegislativo item = proposicaoRepository.findById(proposicaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Proposição não encontrada"));

        String prompt = construirPromptSumarizacao(item);
        String resposta = llama.generate(prompt, getSystemPromptSumarizacao());

        return parseRespostaSumarizacao(resposta, item);
    }

    private String construirPromptSumarizacao(ItemLegislativo item) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Sumarize a seguinte proposição legislativa:\n\n");
        prompt.append("PROPOSIÇÃO:\n");
        prompt.append("Tipo: ").append(item.getTipo()).append("\n");
        prompt.append("Número: ").append(item.getIdentificadorCompleto()).append("\n");
        prompt.append("Ementa: ").append(item.getEmenta()).append("\n");

        if (item.getEmentaDetalhada() != null) {
            prompt.append("Ementa Detalhada: ").append(item.getEmentaDetalhada()).append("\n");
        }

        prompt.append("Tema: ").append(item.getTema()).append("\n\n");

        prompt.append("Forneça:\n");
        prompt.append("1. Resumo curto (2-3 frases)\n");
        prompt.append("2. Resumo detalhado (1 parágrafo)\n");
        prompt.append("3. Pontos-chave (lista)\n");
        prompt.append("4. Impactos identificados\n");
        prompt.append("5. Áreas afetadas\n");
        prompt.append("6. Análise de relevância para defesa nacional\n");
        prompt.append("7. Recomendação\n\n");
        prompt.append("Responda em formato JSON.");

        return prompt.toString();
    }

    private String getSystemPromptSumarizacao() {
        return """
                Você é um analista legislativo especializado em sumarização de proposições.
                Sua tarefa é criar resumos claros, objetivos e informativos.
                Foque em identificar impactos concretos e relevância para áreas estratégicas.
                Sempre analise a relevância específica para defesa nacional.
                Responda sempre em formato JSON estruturado.
                """;
    }

    private SumarizacaoDTO parseRespostaSumarizacao(String resposta, ItemLegislativo item) {
        try {
            Map<String, Object> json = objectMapper.readValue(resposta, Map.class);

            return new SumarizacaoDTO(
                    item.getId(),
                    item.getIdentificadorCompleto(),
                    item.getEmenta(),
                    (String) json.get("resumoCurto"),
                    (String) json.get("resumoDetalhado"),
                    (List<String>) json.get("pontosChave"),
                    (List<String>) json.get("impactos"),
                    (List<String>) json.get("areasAfetadas"),
                    (String) json.get("analiseRelevanciaDefesa"),
                    (String) json.get("recomendacao"),
                    "llama3.2:3b",
                    0.85
            );
        } catch (Exception e) {
            log.error("Erro ao parsear resposta de sumarização", e);
            throw new RuntimeException("Erro ao processar resposta LLM", e);
        }
    }
}