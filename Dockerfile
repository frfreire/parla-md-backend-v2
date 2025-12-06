# ===============================================
# DOCKERFILE OTIMIZADO PARA PARLA-MD BACKEND
# ===============================================

# ===============================================
# STAGE 1: BUILD
# ===============================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copiar POM primeiro para cache de dependências
COPY pom.xml ./

# Baixar dependências (camada cacheável)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build do projeto
RUN mvn clean package -DskipTests -B && \
    mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../parla-md-backend-*.jar

# ===============================================
# STAGE 2: RUNTIME
# ===============================================
FROM eclipse-temurin:21-jre-alpine

# Metadados
LABEL maintainer="Ministério da Defesa"
LABEL version="1.0.0"
LABEL description="Parla-MD Backend - Sistema de Monitoramento Legislativo"

# Instalar dependências de runtime
RUN apk add --no-cache \
    curl \
    tzdata \
    bash \
    dumb-init && \
    rm -rf /var/cache/apk/*

# Configurar timezone
ENV TZ=America/Sao_Paulo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# Criar usuário não-root
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copiar artefatos do build
ARG DEPENDENCY=/build/target/dependency
COPY --from=builder --chown=appuser:appgroup ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder --chown=appuser:appgroup ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder --chown=appuser:appgroup ${DEPENDENCY}/BOOT-INF/classes /app

# Criar diretórios necessários
RUN mkdir -p /app/logs /tmp && \
    chown -R appuser:appgroup /app/logs /tmp

# Mudar para usuário não-root
USER appuser

# Variáveis de ambiente
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8081
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

# JVM Options (será sobrescrito pelo docker-compose se necessário)
ENV JAVA_OPTS="-Xms512m -Xmx1536m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseContainerSupport"

EXPOSE 8081

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Entrypoint
ENTRYPOINT ["dumb-init", "--"]

# Comando padrão
CMD ["sh", "-c", "exec java ${JAVA_OPTS} -cp /app:/app/lib/* br.gov.md.parla_md_backend.ParlaMdBackendApplication"]