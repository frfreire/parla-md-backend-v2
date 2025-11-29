# Documentação da API

## Visão Geral da API

A API do ParlaLex segue os princípios REST e fornece endpoints para acessar e manipular todas as funcionalidades do sistema. A autenticação é realizada via JWT (OAuth2).

## Autenticação

- **Tipo**: Bearer Token (JWT)
- **Provedor**: Keycloak
- **Roles**: ADMIN, ANALISTA, GESTOR, EXTERNO

## Padrões de API

### Base URLs
- Base pública: `/api/publico/`
- Base protegida: `/api/`

### Formato de Dados
- Request/Response: JSON
- Codificação: UTF-8
- Content-Type: application/json

### Status HTTP
- 200: OK (sucesso)
- 201: Created (recurso criado)
- 204: No Content (sucesso sem corpo)
- 400: Bad Request (erro de validação)
- 401: Unauthorized (não autenticado)
- 403: Forbidden (não autorizado)
- 404: Not Found (recurso não encontrado)
- 500: Internal Server Error (erro interno)

### Paginação
GET /api/publico/proposicoes?page=0&size=20&sort=dataApresentacao,desc

Resposta:
```json
{
  "content": [...],
  "pageable": {...},
  "totalElements": 100,
  "totalPages": 5
}

Grupos de Endpoints
Proposições

GET /api/publico/dados-legislativos/proposicoes: Lista proposições
GET /api/publico/dados-legislativos/proposicoes/{id}: Busca proposição por ID
POST /api/publico/dados-legislativos/proposicoes: Cria proposição
PUT /api/publico/dados-legislativos/proposicoes/{id}: Atualiza proposição
DELETE /api/publico/dados-legislativos/proposicoes/{id}: Remove proposição

Parlamentares

GET /api/publico/parlamentares: Lista parlamentares
GET /api/publico/parlamentares/{id}: Busca parlamentar por ID
GET /api/publico/parlamentares/{id}/comportamento: Analisa comportamento
POST /api/publico/parlamentares/{nome}/registrar-voto: Registra voto

Triagem

GET /api/publico/triagem/nao-avaliadas: Lista proposições não avaliadas
GET /api/publico/triagem/interesse: Lista proposições de interesse
GET /api/publico/triagem/descartadas: Lista proposições descartadas
POST /api/publico/triagem/{id}/avaliar: Avalia proposição

Áreas de Impacto

GET /api/publico/impact-areas: Lista áreas de impacto
GET /api/publico/impact-areas/{id}: Busca área de impacto por ID
POST /api/publico/impact-areas: Cria área de impacto
PUT /api/publico/impact-areas/{id}: Atualiza área de impacto
DELETE /api/publico/impact-areas/{id}: Remove área de impacto

IA Generativa

POST /api/publico/ai/summary: Gera sumário
POST /api/publico/ai/impact-analysis: Gera análise de impacto

Gerenciamento de Modelo

POST /api/publico/model/train: Treina modelo
GET /api/publico/model/evaluate: Avalia modelo
POST /api/publico/model/predict: Prediz aprovação

Formato de Erros
json{
  "status": 404,
  "timestamp": "2025-05-16T14:30:00",
  "mensagem": "Proposição não encontrada com ID: 12345",
  "caminho": "/api/publico/proposicoes/12345"
}
Ferramentas

Documentação: Swagger UI deve ficar disponível em /swagger-ui.html
OpenAPI Spec: Disponível em /v3/api-docs
Insomnia Collection: Deve ficar disponível no repositório