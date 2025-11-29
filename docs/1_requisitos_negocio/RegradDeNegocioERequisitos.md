# Regras de Negócio e Requisitos

## Visão Geral do Sistema

O sistema ParlaLex é uma plataforma para acompanhamento legislativo e análise de proposições, oferecendo suporte à tomada de decisão com uso de IA generativa e machine learning.

## Proposições Legislativas

- Toda proposição deve ser classificada em um tipo (PL, PEC, MP, etc.)
- Proposições podem ser categorizadas por tema
- Proposições devem passar por processo de triagem (NÃO_AVALIADO → INTERESSE/DESCARTADO)
- Proposições de interesse devem passar por processo de análise e parecer
- Cada proposição deve ter sua probabilidade de aprovação calculada automaticamente

## Parlamentares e Votações

- O sistema deve manter cadastro atualizado de parlamentares da Câmara e Senado
- Deve ser possível analisar o comportamento de votação por tema
- Parlamentares devem ser classificados como PRO, CONTRA ou NEUTRO em relação a temas específicos
- Votações devem ser registradas e vinculadas às proposições correspondentes

## Fluxo de Trabalho

- **Triagem**: Classificação inicial de proposições como de interesse ou não
- **Parecer**: Elaboração de análise técnica sobre proposições de interesse
- **Posicionamento**: Registro da posição institucional sobre proposições
- **Encaminhamento**: Distribuição de proposições para setores competentes

## Inteligência Artificial

- O sistema deve gerar sumários automáticos de proposições usando NLP
- Deve realizar análise de impacto em áreas pré-definidas
- Deve prever probabilidade de aprovação usando machine learning
- Deve analisar comportamento parlamentar e identificar padrões de votação

## Segurança e Permissões

- Autenticação via OAuth2/JWT usando Keycloak
- Autorização baseada em papéis:
  - ADMIN: Acesso total
  - ANALISTA: Pode avaliar proposições, emitir pareceres
  - GESTOR: Pode visualizar relatórios, encaminhar para setores
  - EXTERNO: Pode responder solicitações de posicionamento

## Módulos Funcionais

| Módulo | Finalidade |
|--------|------------|
| Monitoramento de Proposições | Captura automática de novas proposições da Câmara e Senado |
| Triagem | Classificação de proposições como de interesse ou não |
| Análise de Impacto | Avaliação de efeitos potenciais em áreas estratégicas |
| Parlamentares | Análise de comportamento de votação e posicionamento temático |
| Pareceres | Elaboração de análises técnicas sobre proposições |
| Posicionamentos | Registro e acompanhamento de posições institucionais |
| Relatórios | Dashboards e relatórios sobre métricas legislativas |

## Observações Gerais

- O sistema deve ser responsivo e funcionar em dispositivos móveis e desktop
- Todas as operações críticas devem ser registradas em log para auditoria
- O processamento de dados massivos deve ser realizado de forma assíncrona
- A interface será desenvolvida em Angular 12 posteriormente