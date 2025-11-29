# Parla-MD Backend

Sistema de acompanhamento legislativo para analise de proposicoes do Congresso Nacional.

## Execucao Rapida

### Windows
```cmd
# 1. Abrir prompt de comando
# 2. Navegar para o projeto
cd C:\caminho\para\workspace\workspace\parla-md\parla-md-backend

# 3. Executar
scripts\run.bat
```

### Linux/Mac
```bash
chmod +x scripts/*.sh
./scripts/deploy.sh
```

### Docker Direto
```bash
docker-compose up --build -d
```

## URLs Apos Deploy

| Servico | URL | Credenciais |
|---------|-----|-------------|
| Backend API | http://localhost:8081 | - |
| Swagger UI | http://localhost:8081/swagger-ui.html | - |
| Health Check | http://localhost:8081/actuator/health | - |
| Keycloak | http://localhost:8080 | admin/admin123 |
| RabbitMQ | http://localhost:15672 | guest/guest |

## Pre-requisitos

- Docker Desktop (Windows/Mac) ou Docker + Docker Compose (Linux)
- Java 21 (apenas para desenvolvimento local)

## Scripts Disponiveis

### Windows
- `scripts\check.bat` - Verificar ambiente
- `scripts\deploy.bat` - Deploy dos containers
- `scripts\build.bat` - Build da aplicacao
- `scripts\run.bat` - Execucao completa

### Linux/Mac
- `scripts/deploy.sh` - Deploy completo
- `scripts/build.sh` - Build da aplicacao

## Comandos Uteis

### Gerenciar Containers
```bash
# Ver status
docker-compose ps

# Ver logs
docker-compose logs -f parlamd-backend

# Parar tudo
docker-compose down

# Limpar tudo
docker-compose down --volumes --remove-orphans
docker system prune -f
```

### Health Checks
```bash
# Status geral
curl http://localhost:8081/actuator/health

# MongoDB
curl http://localhost:8081/actuator/health/mongo

# RabbitMQ  
curl http://localhost:8081/actuator/health/rabbit
```

## Tecnologias

- Java 21 + Spring Boot 3.3.4
- MongoDB (banco principal)
- Keycloak (autenticacao)
- RabbitMQ (mensageria)
- Docker + Docker Compose

## Problemas Comuns

### Containers nao iniciam
```bash
docker-compose down --volumes --remove-orphans
docker system prune -f
scripts\run.bat  # Windows
./scripts/deploy.sh  # Linux/Mac
```

### Porta ocupada
```bash
# Windows
netstat -ano | findstr :8081
taskkill /PID [numero] /F

# Linux/Mac
sudo lsof -ti:8081 | xargs kill -9
```

### Aplicacao nao responde
```bash
docker-compose logs -f parlamd-backend
```

## Desenvolvimento Local

```bash
# Subir apenas dependencias
docker-compose up -d mongodb rabbitmq keycloak

# Executar aplicacao em modo dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Status do Projeto

- [x] Backend API funcionando
- [x] Autenticacao Keycloak integrada  
- [x] Banco MongoDB configurado
- [x] Sistema de mensageria RabbitMQ
- [x] Documentacao Swagger disponivel
- [x] Health checks implementados
- [x] Docker containers orquestrados

## Documentacao

- Swagger UI: http://localhost:8081/swagger-ui.html
- API Docs: http://localhost:8081/v3/api-docs
- Health: http://localhost:8081/actuator/health

**Objetivo**: Facilitar o acompanhamento legislativo brasileiro usando tecnologia e IA.
