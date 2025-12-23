package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.dto.AnaliseImpactoDTO;
import br.gov.md.parla_md_backend.domain.dto.ImpactoSetorialDTO;
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
public class AnaliseImpactoService {

    private final LlamaService llama;
    private final IProposicaoRepository proposicaoRepository;
    private final ObjectMapper objectMapper;

    public AnaliseImpactoDTO analisarImpacto(String proposicaoId) {
        ItemLegislativo item = proposicaoRepository.findById(proposicaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Proposição não encontrada"));

        String prompt = construirPromptAnaliseImpacto(item);
        String resposta = llama.generate(prompt, getSystemPromptAnaliseImpacto());

        return parseRespostaAnaliseImpacto(resposta, item);
    }

    private String construirPromptAnaliseImpacto(ItemLegislativo item) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analise os impactos da seguinte proposição:\n\n");
        prompt.append("PROPOSIÇÃO:\n");
        prompt.append("Tipo: ").append(item.getTipo()).append("\n");
        prompt.append("Número: ").append(item.getIdentificadorCompleto()).append("\n");
        prompt.append("Ementa: ").append(item.getEmenta()).append("\n");
        prompt.append("Tema: ").append(item.getTema()).append("\n\n");

        prompt.append("Analise:\n");
        prompt.append("1. Impacto geral e gravidade\n");
        prompt.append("2. Impactos setoriais (Forças Armadas, Indústria de Defesa, etc)\n");
        prompt.append("3. Riscos identificados\n");
        prompt.append("4. Oportunidades\n");
        prompt.append("5. Recomendação de posicionamento\n");
        prompt.append("6. Fundamentação\n");
        prompt.append("7. Score de prioridade (0-100)\n");
        prompt.append("8. Se requer análise detalhada\n\n");
        prompt.append("Responda em formato JSON.");

        return prompt.toString();
    }

    private String getSystemPromptAnaliseImpacto() {
        return """
                Você é um especialista em análise de impacto de políticas públicas e legislação.
                Foque em impactos estratégicos para defesa nacional e segurança.
                Identifique riscos e oportunidades de forma objetiva.
                Priorize análises práticas e acionáveis.
                Responda sempre em formato JSON estruturado.
                """;
    }

    private AnaliseImpactoDTO parseRespostaAnaliseImpacto(String resposta, ItemLegislativo item) {
        try {
            Map<String, Object> json = objectMapper.readValue(resposta, Map.class);

            List<Map<String, Object>> impactosJson = (List<Map<String, Object>>) json.get("impactosSetoriais");
            List<ImpactoSetorialDTO> impactos = impactosJson.stream()
                    .map(i -> new ImpactoSetorialDTO(
                            (String) i.get("setor"),
                            (String) i.get("descricao"),
                            (String) i.get("intensidade"),
                            (List<String>) i.get("detalhes")
                    ))
                    .toList();

            return new AnaliseImpactoDTO(
                    item.getId(),
                    item.getEmenta(),
                    (String) json.get("impactoGeral"),
                    (String) json.get("gravidade"),
                    impactos,
                    (Map<String, String>) json.get("impactosEspecificos"),
                    (List<String>) json.get("riscos"),
                    (List<String>) json.get("oportunidades"),
                    (String) json.get("recomendacaoPosicionamento"),
                    (String) json.get("fundamentacao"),
                    ((Number) json.get("scorePrioridade")).doubleValue(),
                    (Boolean) json.get("requerAnaliseDetalhada")
            );
        } catch (Exception e) {
            log.error("Erro ao parsear resposta de análise de impacto", e);
            throw new RuntimeException("Erro ao processar resposta LLM", e);
        }
    }
}