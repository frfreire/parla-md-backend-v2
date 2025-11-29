# Diretrizes Gerais

## Padrões de Nomenclatura

### Pacotes
- Todos os pacotes seguem o padrão `br.gov.md.parla_md_backend.[categoria]`
- Categorias incluem: `controller`, `service`, `domain`, `repository`, `config`, `exception`, etc.

### Classes
- Nomes em português, no singular
- Seguir convenção PascalCase (ex: `ServicoProposicao`, `ControladorParlamentar`)
- Sufixos padrão por categoria:
  - Controllers: `Controlador[Entidade]`
  - Services: `Servico[Entidade]`
  - Repositories: `Repositorio[Entidade]` ou `IRepositorio[Entidade]` (se interface)
  - Entities: Nome da entidade (ex: `Proposicao`, `Parlamentar`)
  - Exceptions: `Excecao[Tipo]` ou `[Contexto]Exception`
  - Configurations: `Configuracao[Componente]`

### Métodos
- Nomes em português
- Seguir convenção camelCase
- Prefixos padrão por operação:
  - Buscar: para obter dados
  - Salvar: para persistir dados
  - Atualizar: para modificar dados existentes
  - Remover/Excluir: para deletar dados
  - Validar: para validação

### Endpoints REST
- Base path: `/api/publico/` (para endpoints públicos)
- Base path: `/api/` (para endpoints protegidos)
- Recursos no plural (ex: `/proposicoes`, `/parlamentares`)
- Seguir convenções RESTful:
  - GET: buscar recursos
  - POST: criar recursos
  - PUT: atualizar recursos
  - DELETE: remover recursos

## Arquitetura e Padrões

### Camadas
1. **Controladores**: Manipulação de requisições HTTP, validação básica, roteamento
2. **Serviços**: Lógica de negócio, orquestração de operações
3. **Repositórios**: Acesso a dados
4. **Domínio**: Entidades e objetos de valor

### Tratamento de Exceções
- Usar exceções específicas por contexto de erro
- Hierarquia de exceções:
  - `ExcecaoDominio`: Base para exceções de domínio
  - `EntidadeNaoEncontradaException`: Quando entidade não é encontrada
  - `ExcecaoValidacao`: Para erros de validação

### Segurança
- Autenticação via JWT (OAuth2 com Keycloak)
- Autorização baseada em roles (`ROLE_ADMIN`, `ROLE_ANALISTA`, etc.)
- Dados sensíveis em variáveis de ambiente
- Validação de entrada em todas as APIs

### Documentação
- Usar OpenAPI/Swagger para documentação de API
- Documentar todos os endpoints com:
  - Descrição
  - Parâmetros
  - Respostas possíveis
  - Exemplos quando necessário

## Boas Práticas

### Código
- Métodos pequenos (máximo 30 linhas)
- Classes com responsabilidade única
- Injeção de dependências via construtor
- Imutabilidade quando possível
- Logging adequado para depuração e auditoria

### Banco de Dados
- Nomes de coleções MongoDB: snake_case (ex: `parladb.proposicoes`)
- Índices para campos frequentemente consultados
- Usar projeções para limitar dados retornados

### Mensageria
- Nomes de exchanges/queues: snake_case
- Padrão para routing keys: `[entidade].[acao]`
- Garantir idempotência para operações via mensageria

### Testes
- Cobertura mínima de 80% para código crítico
- Testes unitários para lógica de negócio
- Testes de integração para APIs e repositórios
- Testes de componente para fluxos completos