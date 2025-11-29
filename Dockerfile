# ===============================================
# DOCKERFILE OTIMIZADO PARA PARLA-MD BACKEND
# ===============================================
# Usa multi-stage build para reduzir tamanho da imagem final

# ===============================================
# STAGE 1: BUILD
# ===============================================
FROM eclipse-temurin:21-jdk-alpine AS builder

# Instalar ferramentas necessárias para build
RUN apk add --no-cache \
    curl \
    bash

# Configurar diretório de trabalho
WORKDIR /build

# Copiar arquivos de configuração do Maven primeiro (melhor cache)
COPY pom.xml ./
COPY .mvn .mvn/
COPY mvnw ./
COPY mvnw.cmd ./

# Dar permissões ao Maven Wrapper
RUN chmod +x mvnw

# Download de dependências (layer cacheable)
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src src/

# Compilar e empacotar aplicação
RUN ./mvnw clean package -DskipTests -B && \
    mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../parla-md-backend-*.jar

# ===============================================
# STAGE 2: RUNTIME
# ===============================================
FROM eclipse-temurin:21-jre-alpine

# Instalar dependências de runtime
RUN apk add --no-cache \
    curl \
    tzdata \
    bash \
    dumb-init

# Configurar timezone
ENV TZ=America/Sao_Paulo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Criar usuário não-root para segurança
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Configurar diretório de trabalho
WORKDIR /app

# Copiar aplicação do estágio de build
ARG DEPENDENCY=/build/target/dependency
COPY --from=builder --chown=appuser:appgroup ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder --chown=appuser:appgroup ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder --chown=appuser:appgroup ${DEPENDENCY}/BOOT-INF/classes /app

# Copiar script de espera do MongoDB
COPY --chown=appuser:appgroup wait-for-mongodb.sh /app/wait-for-mongodb.sh
RUN chmod +x /app/wait-for-mongodb.sh

# Criar diretório de logs
RUN mkdir -p /app/logs && chown appuser:appgroup /app/logs

# Mudar para usuário não-root
USER appuser

# Configurar variáveis de ambiente
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8081
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Expor porta
EXPOSE 8081

# Health check melhorado
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Script de entrada
COPY --chown=appuser:appgroup docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

# Comando padrão usando dumb-init para melhor gerenciamento de processos
ENTRYPOINT ["dumb-init", "--"]
CMD ["/app/docker-entrypoint.sh"]
