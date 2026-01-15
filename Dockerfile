## ===============================================
## DOCKERFILE OTIMIZADO PARA PARLA-MD BACKEND
## ===============================================
#
## ===============================================
## STAGE 1: BUILD
## ===============================================
#FROM maven:3.9-eclipse-temurin-21-alpine AS builder
#
#WORKDIR /build
#
## Copiar POM e código fonte juntos
#COPY pom.xml ./
#COPY src ./src
#
## Build do projeto (Maven baixará dependências automaticamente)
## -U: força atualização de snapshots
## -B: modo batch (não-interativo)
## -DskipTests: pula testes
#RUN mvn clean package -DskipTests -B -U && \
#    mkdir -p target/dependency && \
#    cd target/dependency && \
#    jar -xf ../parla-md-backend-*.jar
#
## ===============================================
## STAGE 2: RUNTIME
## ===============================================
#FROM eclipse-temurin:21-jre-alpine
#
## Metadados
#LABEL maintainer="Ministério da Defesa"
#LABEL version="1.0.0"
#LABEL description="Parla-MD Backend - Sistema de Monitoramento Legislativo"
#
## Instalar dependências de runtime
#RUN apk add --no-cache \
#    curl \
#    tzdata \
#    bash \
#    dumb-init && \
#    rm -rf /var/cache/apk/*
#
## Configurar timezone
#ENV TZ=America/Sao_Paulo
#RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
#    echo $TZ > /etc/timezone
#
## Criar usuário não-root
#RUN addgroup -g 1001 -S appgroup && \
#    adduser -u 1001 -S appuser -G appgroup
#
#WORKDIR /app
#
## Copiar artefatos do build
#ARG DEPENDENCY=/build/target/dependency
#COPY --from=builder --chown=appuser:appgroup ${DEPENDENCY}/BOOT-INF/lib /app/lib
#COPY --from=builder --chown=appuser:appgroup ${DEPENDENCY}/META-INF /app/META-INF
#COPY --from=builder --chown=appuser:appgroup ${DEPENDENCY}/BOOT-INF/classes /app
#
## Criar diretórios necessários
#RUN mkdir -p /app/logs /tmp && \
#    chown -R appuser:appgroup /app/logs /tmp
#
## Mudar para usuário não-root
#USER appuser
#
## Variáveis de ambiente
#ENV SPRING_PROFILES_ACTIVE=docker
#ENV SERVER_PORT=8081
#ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
#
## JVM Options (será sobrescrito pelo docker-compose se necessário)
#ENV JAVA_OPTS="-Xms512m -Xmx1536m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseContainerSupport"
#
#EXPOSE 8081
#
## Healthcheck
#HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
#    CMD curl -f http://localhost:8081/actuator/health || exit 1
#
## Entrypoint
#ENTRYPOINT ["dumb-init", "--"]
#
## Comando padrão
#CMD ["sh", "-c", "exec java ${JAVA_OPTS} -cp /app:/app/lib/* br.gov.md.parla_md_backend.ParlaMdApplication"]
# ===============================================
# DOCKERFILE OTIMIZADO PARA PARLA-MD BACKEND
# VERSÃO: 1.1.0 (PRODUCTION-READY FOR DEV)
# JAVA VERSION: 21 (ECLIPSE TEMURIN)
# ===============================================

# ===============================================
# STAGE 1: BUILD (CACHE EFICIENTE)
# ===============================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 1. Copiar apenas arquivos de definição de dependência primeiro
# Isso permite que o Docker utilize o cache se o pom.xml não mudou
COPY pom.xml ./

# 2. Baixar dependências em modo offline (fail-fast)
RUN mvn dependency:go-offline -B

# 3. Copiar o código fonte restante
COPY src ./src

# 4. Build do pacote pulando testes (testes devem rodar em pipeline de CI separado)
# A extração do JAR é feita aqui para suportar o padrão "Layered JAR" do Spring Boot
RUN mvn clean package -DskipTests -B -U && \
    mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../parla-md-backend-*.jar

# ===============================================
# STAGE 2: RUNTIME (IMAGEM FINAL LEVE)
# ===============================================
FROM eclipse-temurin:21-jre-alpine

# Metadados de Governança
LABEL maintainer="Ministério da Defesa"
LABEL system="Parla-MD"
LABEL module="Backend API"
LABEL java.version="21"

# Instalação de pacotes de infraestrutura básicos
# dumb-init: Gerenciamento correto de PID 1
# curl: Necessário para o Healthcheck
RUN apk add --no-cache \
    curl \
    tzdata \
    bash \
    dumb-init && \
    rm -rf /var/cache/apk/*

# Configuração Regional (Mandatório para Logs e Auditoria)
ENV TZ=America/Sao_Paulo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Hardening de Segurança: Criação de usuário não-privilegiado
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Cópia dos artefatos em camadas (Layered JAR)
# Isso otimiza o deploy: se apenas classes mudarem, as libs não são copiadas novamente
ARG DEPENDENCY=/build/target/dependency
COPY --from=builder --chown=appuser:appgroup ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder --chown=appuser:appgroup ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder --chown=appuser:appgroup ${DEPENDENCY}/BOOT-INF/classes /app

# Criação de diretórios de logs com permissões corretas
RUN mkdir -p /app/logs /tmp && \
    chown -R appuser:appgroup /app/logs /tmp

# Switch para usuário seguro
USER appuser

# Variáveis de Ambiente Padrão (Podem ser sobrescritas no docker-compose)
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8081
# Configurações da JVM otimizadas para container (Awareness de CGroup)
ENV JAVA_OPTS="-Xms512m -Xmx1536m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseContainerSupport"

EXPOSE 8081

# Healthcheck Robusto
# Verifica a disponibilidade real da aplicação via Spring Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Inicialização via dumb-init para tratamento de sinais do OS
ENTRYPOINT ["dumb-init", "--"]

# Execução da aplicação explodida (cp ao invés de -jar para performance)
CMD ["sh", "-c", "exec java ${JAVA_OPTS} -cp /app:/app/lib/* br.gov.md.parla_md_backend.ParlaMdApplication"]