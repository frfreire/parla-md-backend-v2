## 12. Arquivo: /docs/2_etapas_de_implementacao/Etapa05_MigracaoControladores.md

```markdown
# Etapa 05: Migração de Controladores

## Objetivo
Migrar os controladores para nomenclatura em português, aplicar documentação OpenAPI e melhorar o tratamento de erros.

## Componentes Afetados
- LegislativeDataController.java → ControladorDadosLegislativos.java
- TriagemController.java → ControladorTriagem.java
- ParlamentarianController.java → ControladorParlamentar.java
- ImpactAreaController.java → ControladorAreaImpacto.java

## Tarefas a Realizar
1. Renomear classes para português
2. Atualizar caminhos de URL para `/api/publico/`
3. Adicionar documentação OpenAPI
4. Implementar tratamento adequado de exceções
5. Usar DTOs para entrada/saída quando apropriado

## Código a Implementar

### Arquivo: ControladorDadosLegislativos.java
```java
package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.legislativo.Proposicao;
import br.gov.md.parla_md_backend.service.ServicoProposicao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publico/dados-legislativos")
@Tag(name = "Dados Legislativos", description = "Operações relacionadas a dados legislativos")
public class ControladorDadosLegislativos {

    private final ServicoProposicao servicoProposicao;
    // Outros serviços...

    @Autowired
    public ControladorDadosLegislativos(ServicoProposicao servicoProposicao /* Outros serviços... */) {
        this.servicoProposicao = servicoProposicao;
        // Inicialização de outros serviços...
    }

    @GetMapping("/proposicoes/{id}")
    @Operation(
            summary = "Buscar proposição por ID",
            description = "Retorna os detalhes de uma proposição específica com base no ID fornecido"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposição encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Proposicao.class))),
            @ApiResponse(responseCode = "404", description = "Proposição não encontrada", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    public ResponseEntity<Proposicao> buscarProposicaoPorId(
            @Parameter(description = "ID da proposição", required = true) @PathVariable String id) {
        Proposicao proposicao = servicoProposicao.buscarProposicaoPorId(id);
        return ResponseEntity.ok(proposicao);
    }

    @GetMapping("/proposicoes")
    @Operation(
            summary = "Listar proposições",
            description = "Retorna uma lista de todas as proposições"
    )
    public ResponseEntity<List<Proposicao>> listarProposicoes() {
        List<Proposicao> proposicoes = servicoProposicao.buscarTodasProposicoes();
        return ResponseEntity.ok(proposicoes);
    }

    // Outros métodos...
}
```

## Testes

1. Verificar se as URLs antigas redirecionam corretamente
2. Testar se as respostas estão no formato esperado
3. Verificar se a documentação OpenAPI está correta
4. Testar tratamento de erros nos endpoints

## Dependências

* Etapa 02: Implementação OpenAPI (para documentação)
* Etapa 03: Tratamento de Exceções (para uso das exceções)
* Etapa 04: Refatoração de Serviços (para uso dos serviços refatorados)

## Status

[ ] Análise Concluída
[ ] Implementação Iniciada
[ ] Testes Realizados
[ ] Revisão Concluída
[ ] Implementação Concluída