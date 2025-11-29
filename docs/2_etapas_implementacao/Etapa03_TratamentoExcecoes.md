## 10. Arquivo: /docs/2_etapas_de_implementacao/Etapa03_TratamentoExcecoes.md

```markdown
# Etapa 03: Tratamento de Exceções

## Objetivo
Implementar um sistema centralizado de tratamento de exceções com nomenclatura em português, hierarquia clara e respostas padronizadas.

## Componentes Afetados
- Novas classes:
  - ExcecaoDominio.java
  - EntidadeNaoEncontradaException.java
  - ExcecaoValidacao.java
  - RespostaErro.java
  - ManipuladorGlobalExcecoes.java
  
## Tarefas a Realizar
1. Criar hierarquia de exceções de domínio
2. Implementar classe de resposta de erro padronizada
3. Criar manipulador global de exceções
4. Integrar com Spring Framework
5. Documentar com OpenAPI

## Código a Implementar

### Arquivo: ExcecaoDominio.java
```java
package br.gov.md.parla_md_backend.exception;

public class ExcecaoDominio extends RuntimeException {
    public ExcecaoDominio(String mensagem) {
        super(mensagem);
    }
    
    public ExcecaoDominio(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

package br.gov.md.parla_md_backend.exception;

public class EntidadeNaoEncontradaException extends ExcecaoDominio {
    public EntidadeNaoEncontradaException(String entidade, String id) {
        super(String.format("%s não encontrado(a) com ID: %s", entidade, id));
    }
}

package br.gov.md.parla_md_backend.exception;

public class ExcecaoValidacao extends ExcecaoDominio {
    public ExcecaoValidacao(String mensagem) {
        super(mensagem);
    }
}

package br.gov.md.parla_md_backend.exception;

import java.time.LocalDateTime;

public class RespostaErro {
    private int status;
    private LocalDateTime timestamp;
    private String mensagem;
    private String caminho;
    
    public RespostaErro(int status, LocalDateTime timestamp, String mensagem, String caminho) {
        this.status = status;
        this.timestamp = timestamp;
        this.mensagem = mensagem;
        this.caminho = caminho;
    }
    
    // Getters e setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getCaminho() {
        return caminho;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }
}

package br.gov.md.parla_md_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class ManipuladorGlobalExcecoes {

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<RespostaErro> manipularEntidadeNaoEncontrada(EntidadeNaoEncontradaException ex, WebRequest requisicao) {
        RespostaErro respostaErro = new RespostaErro(
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                requisicao.getDescription(false)
        );
        return new ResponseEntity<>(respostaErro, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ExcecaoValidacao.class)
    public ResponseEntity<RespostaErro> manipularExcecaoValidacao(ExcecaoValidacao ex, WebRequest requisicao) {
        RespostaErro respostaErro = new RespostaErro(
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                requisicao.getDescription(false)
        );
        return new ResponseEntity<>(respostaErro, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExcecaoDominio.class)
    public ResponseEntity<RespostaErro> manipularExcecaoDominio(ExcecaoDominio ex, WebRequest requisicao) {
        RespostaErro respostaErro = new RespostaErro(
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                requisicao.getDescription(false)
        );
        return new ResponseEntity<>(respostaErro, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErro> manipularExcecaoGlobal(Exception ex, WebRequest requisicao) {
        RespostaErro respostaErro = new RespostaErro(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now(),
                "Ocorreu um erro interno. Por favor, tente novamente mais tarde.",
                requisicao.getDescription(false)
        );
        return new ResponseEntity<>(respostaErro, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

## Testes

- Verificar tratamento de exceção de entidade não encontrada
- Verificar tratamento de exceção de validação
- Verificar tratamento de exceção de domínio genérica
- Verificar tratamento de exceção não tratada (Exception genérica)
- Validar formato das respostas de erro

## Dependências

* Nenhuma dependência de outras etapas

## Status

 [ ] Análise Concluída
 [ ] Implementação Iniciada
 [ ] Testes Realizados
 [ ] Revisão Concluída
 [ ] Implementação Concluída