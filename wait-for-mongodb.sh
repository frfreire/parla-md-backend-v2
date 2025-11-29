#!/bin/bash

# Script simples para aguardar que o MongoDB esteja disponível

host="${1:-mongodb}"
port="${2:-27017}"
username="${3:-admin}"
password="${4:-admin123}"

echo "🔄 Aguardando MongoDB em $host:$port..."

# Função para testar conexão
test_connection() {
    mongosh --host "$host" --port "$port" \
            --username "$username" --password "$password" \
            --authenticationDatabase admin \
            --eval "db.adminCommand('ping')" \
            --quiet > /dev/null 2>&1
}

# Loop de espera
counter=0
max_attempts=30

while ! test_connection; do
    counter=$((counter + 1))
    if [ $counter -gt $max_attempts ]; then
        echo "❌ Timeout: MongoDB não ficou disponível após $max_attempts tentativas"
        exit 1
    fi
    echo "⏳ Tentativa $counter/$max_attempts - MongoDB não está disponível ainda..."
    sleep 3
done

echo "✅ MongoDB está disponível e respondendo!"

# Se há argumentos adicionais, executá-los
if [ $# -gt 4 ]; then
    shift 4
    echo "🚀 Executando comando: $@"
    exec "$@"
fi
