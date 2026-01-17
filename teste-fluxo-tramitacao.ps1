# ============================================================================
# PARLA-MD - TESTE DE MÁQUINA DE ESTADOS (WORKFLOW DE TRAMITACAO)
# ============================================================================

$BackendUrl = "http://localhost:8081"
$AuthUrl = "http://localhost:8180"
$AnalistaUser = "analista@defesa.gov.br"
$AnalistaPass = "Analista@123"

# Configurações Visuais
$SUCCESS = "Green"; $ERROR = "Red"; $INFO = "Cyan"; $WARN = "Yellow"

function Write-Step { param($msg) Write-Host "`n[PASSO] $msg" -ForegroundColor $INFO }
function Write-Ok { param($msg) Write-Host "   [OK] $msg" -ForegroundColor $SUCCESS }
function Write-Fail { param($msg) Write-Host "   [ERRO] $msg" -ForegroundColor $ERROR }

# 1. Autenticação
Write-Step "Autenticando como Analista ($AnalistaUser)..."
$body = @{ grant_type = "password"; client_id = "parla-md-client"; username = $AnalistaUser; password = $AnalistaPass }
try {
    $tokenResp = Invoke-RestMethod -Uri "$AuthUrl/realms/parlamd/protocol/openid-connect/token" -Method POST -Body $body
    $token = $tokenResp.access_token
    $headers = @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" }
    Write-Ok "Login realizado com sucesso."
} catch {
    Write-Fail "Falha no login. Verifique o Keycloak."
    exit
}

# 2. Buscar Tramitação Pendente
Write-Step "Buscando tramitações recebidas..."
try {
    $resp = Invoke-RestMethod -Uri "$BackendUrl/api/tramitacoes/recebidas" -Method GET -Headers $headers
    $tramitacoes = $resp.content

    if ($tramitacoes.Count -eq 0) {
        Write-Host "   [AVISO] Nenhuma tramitação encontrada para o Analista." -ForegroundColor $WARN
        Write-Host "   Necessário logar como GESTOR e encaminhar algo para o ANALISTA primeiro." -ForegroundColor $WARN
        exit
    }

    $alvo = $tramitacoes[0]
    $id = $alvo.id
    Write-Ok "Alvo selecionado: ID $id (Status Atual: $($alvo.status))"
} catch {
    Write-Fail "Erro ao buscar tramitações: $($_.Exception.Message)"
    exit
}

# Função auxiliar para transição
function Testar-Transicao {
    param($Acao, $UrlSuffix, $StatusEsperado)
    Write-Step "Testando ação: $Acao..."
    try {
        $resp = Invoke-RestMethod -Uri "$BackendUrl/api/tramitacoes/$id/$UrlSuffix" -Method PUT -Headers $headers
        if ($resp.status -eq $StatusEsperado) {
            Write-Ok "Sucesso! Status mudou para: $($resp.status)"
        } else {
            Write-Fail "Status inesperado: $($resp.status) (Esperado: $StatusEsperado)"
        }
    } catch {
        $err = $_.Exception.Response
        if ($err) {
            $code = $err.StatusCode.value__
            Write-Fail "Falha na transição ($code). Detalhe: $($_.ErrorDetails.Message)"
        } else {
            Write-Fail "Erro de conexão: $($_.Exception.Message)"
        }
    }
}

# 3. Execução do Fluxo Completo (Happy Path)

# A. Receber (Se ainda estiver pendente)
if ($alvo.status -eq "PENDENTE") {
    Testar-Transicao "RECEBER" "receber" "RECEBIDO"
}

# B. Iniciar Análise
Testar-Transicao "INICIAR ANÁLISE" "iniciar-analise" "EM_ANALISE"

# C. Solicitar Parecer
Testar-Transicao "SOLICITAR PARECER" "solicitar-parecer" "AGUARDANDO_PARECER"

# D. Suspender (Volta para Pendente)
Testar-Transicao "SUSPENDER (Bloqueio)" "suspender" "PENDENTE"

# E. Retomar (Volta para Análise)
Testar-Transicao "RETOMAR TRABALHO" "retomar" "EM_ANALISE"

# F. Concluir
Testar-Transicao "CONCLUIR TRAMITAÇÃO" "concluir" "CONCLUIDO"

Write-Host "`n=== FIM DOS TESTES ===" -ForegroundColor $INFO