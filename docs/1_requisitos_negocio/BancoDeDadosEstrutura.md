# Estrutura do Banco de Dados

## Visão Geral

O sistema utiliza MongoDB como banco de dados principal, organizado em coleções que representam as entidades do domínio.

## Coleções Principais

### parladb.proposition
Armazena proposições legislativas.

**Campos principais:**
- `_id`: String (ID único)
- `siglaTipo`: String (PL, PEC, MP, etc.)
- `codTipo`: int
- `numero`: int
- `ano`: int
- `ementa`: String
- `dataApresentacao`: LocalDateTime
- `autorId`: String
- `autorNome`: String
- `partidoAutor`: String
- `estadoAutor`: String
- `tema`: String
- `triagemStatus`: Enum (NÃO_AVALIADO, INTERESSE, DESCARTADO)
- `approvalProbability`: double
- `parecer`: String
- `statusParecer`: Enum (NÃO_INICIADO, EM_ELABORACAO, AGUARDANDO_RESPOSTA_SETOR, CONCLUIDO)

### parladb.parlamentarian
Armazena informações sobre parlamentares.

**Campos principais:**
- `_id`: String (ID único)
- `nome`: String
- `partido`: String
- `estado`: String
- `urlFoto`: String
- `tipo`: Enum (DEPUTADO, SENADOR)
- `behaviors`: Map<String, BehaviorTheme> (comportamento por tema)

### impact_areas
Áreas de impacto que podem ser afetadas por proposições.

**Campos principais:**
- `_id`: String (ID único)
- `name`: String
- `keywords`: List<String>

### parladb.positionings
Posicionamentos sobre proposições.

**Campos principais:**
- `_id`: String (ID único)
- `propositionId`: String
- `setorId`: String
- `tipo`: Enum (FAVORAVEL, NAO_FAVORAVEL, NEUTRO, PENDENTE)
- `justificativa`: String
- `dataSolicitacao`: LocalDateTime
- `dataResposta`: LocalDateTime
- `usuarioSolicitanteId`: String
- `usuarioRespondenteId`: String

### votacao
Registro de votações.

**Campos principais:**
- `_id`: String (ID único)
- `dataHoraInicio`: LocalDateTime
- `dataHoraFim`: LocalDateTime
- `siglaOrgao`: String
- `propositionId`: String
- `parlamentarianId`: String
- `vote`: String
- `voteDate`: LocalDateTime

### parladb.sectors
Setores internos e externos.

**Campos principais:**
- `_id`: String (ID único)
- `nome`: String
- `sigla`: String
- `descricao`: String
- `tipo`: Enum (INTERNO, EXTERNO)

## Relacionamentos

- **Proposição** → **Tramitações**: Uma proposição tem múltiplas tramitações
- **Proposição** → **Posicionamentos**: Uma proposição pode ter múltiplos posicionamentos
- **Parlamentar** → **Votações**: Um parlamentar participa de múltiplas votações
- **Setor** → **Posicionamentos**: Um setor pode emitir múltiplos posicionamentos

## Índices

### parladb.proposition
- `autorId`: Para busca de proposições por autor
- `tema`: Para busca de proposições por tema
- `triagemStatus`: Para filtragem por status de triagem

### parladb.parlamentarian
- `partido`: Para filtragem por partido
- `estado`: Para filtragem por estado

### votacao
- `parlamentarianId`: Para busca de votações por parlamentar
- `propositionId`: Para busca de votações por proposição

## Observações

- A modelagem utiliza documentos aninhados para representar relacionamentos simples
- `@DBRef` é usado para relacionamentos mais complexos
- Coleções especializadas são usadas para relacionamentos muitos-para-muitos
- Campos de auditoria (criação, modificação) são incluídos em todas as coleções