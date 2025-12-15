package br.gov.md.parla_md_backend.util;

import br.gov.md.parla_md_backend.domain.legislativo.Proposicao;

public class PromptTemplates {

    public static final String SISTEMA_ANALISTA_LEGISLATIVO = """
            Você é um analista legislativo especializado do Congresso Nacional Brasileiro.
            Sua função é analisar proposições legislativas com precisão técnica e objetividade.
            
            IMPORTANTE:
            - Sempre responda em JSON válido
            - Base suas análises em dados fornecidos
            - Seja objetivo e técnico
            - Use terminologia legislativa apropriada
            """;

    public static String promptPrevisaoAprovacao(Proposicao proposicao) {
        return String.format("""
                Analise esta proposição legislativa e preveja sua probabilidade de aprovação:
                
                DADOS DA PROPOSIÇÃO:
                - Tipo: %s
                - Ementa: %s
                - Tema: %s
                - Partido do Autor: %s
                - Estado do Autor: %s
                - Casa: %s
                - Ano: %d
                
                TAREFA:
                Retorne um JSON com a análise seguindo EXATAMENTE este formato:
                
                {
                  "probabilidadeAprovacao": <número entre 0.0 e 1.0>,
                  "justificativa": "<explicação técnica em 2-3 frases>",
                  "confianca": "<ALTA|MEDIA|BAIXA>",
                  "fatoresInfluentes": [
                    {
                      "nome": "<nome do fator>",
                      "impacto": "<POSITIVO|NEGATIVO|NEUTRO>",
                      "peso": <número entre 0.0 e 1.0>,
                      "descricao": "<breve explicação>"
                    }
                  ]
                }
                
                IMPORTANTE:
                - Considere contexto político brasileiro
                - Analise partido do autor e tema
                - Avalie complexidade da proposição
                - Retorne APENAS o JSON, sem texto adicional
                """,
                proposicao.getTipoProposicao(),
                proposicao.getEmenta(),
                proposicao.getTema(),
                proposicao.getPartidoAutor(),
                proposicao.getEstadoAutor(),
                proposicao.getCasa(),
                proposicao.getAno()
        );
    }

    public static String promptSumarizacao(Proposicao proposicao) {
        return String.format("""
                Crie um sumário executivo desta proposição legislativa:
                
                PROPOSIÇÃO:
                %s
                
                EMENTA:
                %s
                
                TAREFA:
                Retorne um JSON com:
                
                {
                  "sumario": "<sumário em 3-5 frases, linguagem acessível>",
                  "pontosPrincipais": [
                    "<ponto 1>",
                    "<ponto 2>",
                    "<ponto 3>"
                  ],
                  "impactoEstimado": "<descrição breve do impacto esperado>"
                }
                
                Seja claro, objetivo e use linguagem acessível.
                """,
                proposicao.getTipoProposicao() + " " + proposicao.getNumero() + "/" + proposicao.getAno(),
                proposicao.getEmenta()
        );
    }

    public static String promptAnaliseImpacto(Proposicao proposicao, String area) {
        return String.format("""
                Analise o impacto desta proposição na área de %s:
                
                PROPOSIÇÃO:
                - Tipo: %s
                - Ementa: %s
                - Tema: %s
                
                TAREFA:
                Retorne um JSON:
                
                {
                  "area": "%s",
                  "nivelImpacto": "<ALTO|MEDIO|BAIXO|NENHUM>",
                  "tipoImpacto": "<POSITIVO|NEGATIVO|MISTO|NEUTRO>",
                  "analise": "<análise detalhada em 3-5 frases>",
                  "consequencias": [
                    "<consequência 1>",
                    "<consequência 2>"
                  ],
                  "gruposAfetados": [
                    "<grupo 1>",
                    "<grupo 2>"
                  ]
                }
                """,
                area,
                proposicao.getTipoProposicao(),
                proposicao.getEmenta(),
                proposicao.getTema(),
                area
        );
    }

    public static String promptComportamentoParlamentar(
            String nomeParlamentar,
            String tema,
            String historicoVotacoes) {
        return String.format("""
                Analise o comportamento de votação do parlamentar %s sobre o tema %s:
                
                HISTÓRICO DE VOTAÇÕES:
                %s
                
                TAREFA:
                Retorne um JSON:
                
                {
                  "parlamentar": "%s",
                  "tema": "%s",
                  "posicionamento": "<PRO|CONTRA|NEUTRO>",
                  "confiabilidade": <número entre 0.0 e 1.0>,
                  "analise": "<análise do padrão de votação>",
                  "tendencia": "<descrição da tendência identificada>"
                }
                """,
                nomeParlamentar,
                tema,
                historicoVotacoes,
                nomeParlamentar,
                tema
        );
    }
}