#!/usr/bin/env bash
# ===============================================
# WAIT-FOR-IT.SH
# ===============================================
# Script para aguardar que serviços estejam disponíveis
# Use: ./wait-for-mongodb.sh host:port [-t timeout] [-- command args]

WAITFORIT_cmdname=${0##*/}

echoerr() { if [[ $WAITFORIT_QUIET -ne 1 ]]; then echo "$@" 1>&2; fi }

usage()
{
    cat << USAGE >&2
Usage:
    $WAITFORIT_cmdname host:port [-s] [-t timeout] [-- command args]
    -h HOST | --host=HOST       Host ou IP a ser testado
    -p PORT | --port=PORT       Porta TCP a ser testada
    -s | --strict               Falhar apenas com erros de conexão
    -q | --quiet                Não mostrar saída
    -t TIMEOUT | --timeout=TIMEOUT
                                Timeout em segundos, zero para sem timeout
    -- COMMAND ARGS             Executar comando com argumentos após teste
                                ou se timeout for atingido
USAGE
    exit 1
}

wait_for()
{
    if [[ $WAITFORIT_TIMEOUT -gt 0 ]]; then
        echoerr "$WAITFORIT_cmdname: aguardando $WAITFORIT_HOST:$WAITFORIT_PORT por ${WAITFORIT_TIMEOUT} segundos"
    else
        echoerr "$WAITFORIT_cmdname: aguardando $WAITFORIT_HOST:$WAITFORIT_PORT indefinidamente"
    fi
    start_ts=$(date +%s)
    while :
    do
        if [[ $WAITFORIT_ISBUSY -eq 1 ]]; then
            nc -z $WAITFORIT_HOST $WAITFORIT_PORT
            result=$?
        else
            (echo > /dev/tcp/$WAITFORIT_HOST/$WAITFORIT_PORT) >/dev/null 2>&1
            result=$?
        fi
        if [[ $result -eq 0 ]]; then
            end_ts=$(date +%s)
            echoerr "$WAITFORIT_cmdname: $WAITFORIT_HOST:$WAITFORIT_PORT está disponível após $((end_ts - start_ts)) segundos"
            break
        fi
        sleep 1
    done
    return $result
}

wait_for_wrapper()
{
    # Para suportar conexões com timeout
    if [[ $WAITFORIT_CHILD -gt 0 ]]; then
        wait $WAITFORIT_CHILD
        WAITFORIT_RESULT=$?
    else
        WAITFORIT_RESULT=0
    fi
    
    if [[ $WAITFORIT_TIMEOUT -gt 0 ]]; then
        end_ts=$(date +%s)
        elapsed=$((end_ts - start_ts))
        if [[ $elapsed -ge $WAITFORIT_TIMEOUT ]]; then
            echoerr "$WAITFORIT_cmdname: timeout atingido após ${elapsed} segundos"
            kill -s TERM $WAITFORIT_CHILD 2>/dev/null
            WAITFORIT_RESULT=124
        fi
    fi
    
    if [[ $WAITFORIT_RESULT -ne 0 && $WAITFORIT_STRICT -eq 1 ]]; then
        echoerr "$WAITFORIT_cmdname: comando estrito executado com código de saída $WAITFORIT_RESULT"
        exit $WAITFORIT_RESULT
    fi
}

# Processar argumentos
WAITFORIT_TIMEOUT=15
WAITFORIT_STRICT=0
WAITFORIT_CHILD=0
WAITFORIT_QUIET=0

# Verificar se nc está disponível
WAITFORIT_ISBUSY=0
if command -v nc >/dev/null; then
    WAITFORIT_ISBUSY=1
fi

while [[ $# -gt 0 ]]
do
    case "$1" in
        *:* )
        WAITFORIT_hostport=(${1//:/ })
        WAITFORIT_HOST=${WAITFORIT_hostport[0]}
        WAITFORIT_PORT=${WAITFORIT_hostport[1]}
        shift 1
        ;;
        --child)
        WAITFORIT_CHILD=1
        shift 1
        ;;
        -q | --quiet)
        WAITFORIT_QUIET=1
        shift 1
        ;;
        -s | --strict)
        WAITFORIT_STRICT=1
        shift 1
        ;;
        -h)
        WAITFORIT_HOST="$2"
        if [[ $WAITFORIT_HOST == "" ]]; then break; fi
        shift 2
        ;;
        --host=*)
        WAITFORIT_HOST="${1#*=}"
        shift 1
        ;;
        -p)
        WAITFORIT_PORT="$2"
        if [[ $WAITFORIT_PORT == "" ]]; then break; fi
        shift 2
        ;;
        --port=*)
        WAITFORIT_PORT="${1#*=}"
        shift 1
        ;;
        -t)
        WAITFORIT_TIMEOUT="$2"
        if [[ $WAITFORIT_TIMEOUT == "" ]]; then break; fi
        shift 2
        ;;
        --timeout=*)
        WAITFORIT_TIMEOUT="${1#*=}"
        shift 1
        ;;
        --)
        shift
        WAITFORIT_CLI=("$@")
        break
        ;;
        --help)
        usage
        ;;
        *)
        echoerr "Argumento desconhecido: $1"
        usage
        ;;
    esac
done

if [[ "$WAITFORIT_HOST" == "" || "$WAITFORIT_PORT" == "" ]]; then
    echoerr "Erro: você precisa fornecer um host e porta para testar."
    usage
fi

start_ts=$(date +%s)

WAITFORIT_TIMEOUT=$((WAITFORIT_TIMEOUT))
if [[ $WAITFORIT_CHILD -gt 0 ]]; then
    wait_for
    WAITFORIT_RESULT=$?
    exit $WAITFORIT_RESULT
else
    if [[ $WAITFORIT_TIMEOUT -gt 0 ]]; then
        wait_for_wrapper &
        WAITFORIT_PID=$!
        trap "kill -s TERM $WAITFORIT_PID" TERM INT
        wait_for &
        WAITFORIT_CHILD=$!
        wait $WAITFORIT_CHILD
        WAITFORIT_RESULT=$?
        kill -s TERM $WAITFORIT_PID 2>/dev/null
    else
        wait_for
        WAITFORIT_RESULT=$?
    fi
fi

if [[ $WAITFORIT_CLI != "" ]]; then
    if [[ $WAITFORIT_RESULT -ne 0 && $WAITFORIT_STRICT -eq 1 ]]; then
        echoerr "$WAITFORIT_cmdname: execução estrita com código de saída $WAITFORIT_RESULT"
        exit $WAITFORIT_RESULT
    fi
    exec "${WAITFORIT_CLI[@]}"
else
    exit $WAITFORIT_RESULT
fi