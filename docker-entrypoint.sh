#!/bin/bash

# Script de entrada para container Docker do Parla-MD Backend
# Gerencia a inicialização da aplicação com verificações de dependências

set -e

echo "Iniciando Parla-MD Backend"
echo "============================="

# Função de log
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Função para aguardar serviço
wait_for_service() {
    local host="$1"
    local port="$2"
    local service_name="$3"
    local max_attempts=30
    local attempt=1
    
    log "Aguardando $service_name em $host:$port..."
    
    while ! nc -z "$host" "$port" 2>/dev/null; do
        if [ $attempt -ge $max_attempts ]; then
            log "Timeout: $service_name não ficou disponível"
            exit 1
        fi
        log "   Tentativa $attempt/$max_attempts..."
        sleep 3
        attempt=$((attempt + 1))
    done
    
    log "$service_name está disponível"
}

# Verificar variáveis de ambiente essenciais
log "Verificando configuração..."

# MongoDB
MONGODB_HOST=$(echo "${SPRING_DATA_MONGODB_URI}" | sed -n 's/.*@\([^:]*\):.*/\1/p')
MONGODB_PORT=$(echo "${SPRING_DATA_MONGODB_URI}" | sed -n 's/.*:\([0-9]*\)\/.*/\1/p')

if [ -n "$MONGODB_HOST" ] && [ -n "$MONGODB_PORT" ]; then
    wait_for_service "$MONGODB_HOST" "$MONGODB_PORT" "MongoDB"
else
    log "Configuração de MongoDB não encontrada, continuando..."
fi

# RabbitMQ
if [ -n "$SPRING_RABBITMQ_HOST" ] && [ -n "$SPRING_RABBITMQ_PORT" ]; then
    wait_for_service "$SPRING_RABBITMQ_HOST" "$SPRING_RABBITMQ_PORT" "RabbitMQ"
fi

# Keycloak (opcional)
KEYCLOAK_HOST=$(echo "${KEYCLOAK_AUTH_SERVER_URL}" | sed -n 's|.*://\([^:]*\):.*|\1|p')
KEYCLOAK_PORT=$(echo "${KEYCLOAK_AUTH_SERVER_URL}" | sed -n 's|.*:\([0-9]*\)|\1|p')

if [ -n "$KEYCLOAK_HOST" ] && [ -n "$KEYCLOAK_PORT" ]; then
    wait_for_service "$KEYCLOAK_HOST" "$KEYCLOAK_PORT" "Keycloak"
fi

# Aguardar um pouco mais para garantir que os serviços estejam totalmente prontos
log "Aguardando inicialização completa dos serviços..."
sleep 10

# Criar diretório de logs se não existir
mkdir -p /app/logs

# Definir JVM options baseado na memória disponível
if [ -z "$JAVA_OPTS" ]; then
    # Detectar memória disponível
    MEMORY_MB=$(awk '/MemAvailable/ {printf "%.0f", $2/1024}' /proc/meminfo 2>/dev/null || echo "1024")
    
    if [ "$MEMORY_MB" -gt 2048 ]; then
        JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
    elif [ "$MEMORY_MB" -gt 1024 ]; then
        JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
    else
        JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseSerialGC"
    fi
    
    # Adicionar opções de container
    JAVA_OPTS="$JAVA_OPTS -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
fi

# Log da configuração
log "Configuração da aplicação:"
log "   - Profile: ${SPRING_PROFILES_ACTIVE}"
log "   - Porta: ${SERVER_PORT}"
log "   - Memória JVM: ${JAVA_OPTS}"
log "   - MongoDB: ${MONGODB_HOST:-localhost}:${MONGODB_PORT:-27017}"
log "   - RabbitMQ: ${SPRING_RABBITMQ_HOST:-localhost}:${SPRING_RABBITMQ_PORT:-5672}"

# Executar aplicação
log "Iniciando aplicação Java..."

# Encontrar o JAR principal (Spring Boot 3.x)
MAIN_CLASS="br.gov.md.parla_md_backend.ParlaMdApplication"

exec java $JAVA_OPTS \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active="${SPRING_PROFILES_ACTIVE}" \
    -Dserver.port="${SERVER_PORT}" \
    -cp /app:/app/lib/* \
    "$MAIN_CLASS" "$@"
