# ============================================================================
# PARLA-MD - TESTE DE AUTENTICACAO KEYCLOAK (VERSAO ROBUSTA)
# ============================================================================
# Tech Lead: Refatorado para incluir retries, timeouts maiores e diagnósticos
# ============================================================================

param(
    [string]$KeycloakUrl = "http://localhost:8180",
    [string]$BackendUrl = "http://localhost:8081",
    [string]$Realm = "parlamd",
    [string]$ClientId = "parla-md-client",
    [string]$ClientSecret = "RnMndotWYb52i2aNpIe2fQNb12kRUSxa",
    [switch]$Verbose,
    [switch]$SaveResults,
    [switch]$IgnoreSslErrors
)

# Configurações Globais
$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"
$MaxRetries = 5
$RetryDelaySec = 2

# Cores
$SUCCESS = "Green"
$ERROR_COLOR = "Red"
$INFO = "Yellow"
$HIGHLIGHT = "Cyan"
$GRAY = "Gray"
$WARN = "Magenta"

# Ignorar erros de SSL se solicitado (ambiente dev)
if ($IgnoreSslErrors) {
    [System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12
}

# Dados de Teste
$usuarios = @(
    @{ Username = "admin.parla@parla.md.gov.br"; Password = "Admin@123"; RoleEsperada = "ADMIN"; Descricao = "Administrador" },
    @{ Username = "gestor@defesa.gov.br";        Password = "Gestor@123"; RoleEsperada = "GESTOR"; Descricao = "Gestor" },
    @{ Username = "analista@defesa.gov.br";      Password = "Analista@123"; RoleEsperada = "ANALISTA"; Descricao = "Analista" },
    @{ Username = "externo@eb.mil.br";           Password = "Externo@123"; RoleEsperada = "EXTERNO"; Descricao = "Externo" }
)

$resultados = @{
    Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Keycloak = @{}
    Backend = @{}
    Usuarios = @()
    Resumo = @{ Total = 0; Sucesso = 0; Falha = 0 }
}

# ============================================================================
# FUNCOES AUXILIARES
# ============================================================================

function Write-Section {
    param([string]$Title)
    Write-Host "`n========================================" -ForegroundColor $HIGHLIGHT
    Write-Host $Title -ForegroundColor $HIGHLIGHT
    Write-Host "========================================`n" -ForegroundColor $HIGHLIGHT
}

function Write-Success { param([string]$Message); Write-Host "[OK] $Message" -ForegroundColor $SUCCESS }
function Write-Failure { param([string]$Message); Write-Host "[FALHA] $Message" -ForegroundColor $ERROR_COLOR }
function Write-Warn    { param([string]$Message); Write-Host "[AVISO] $Message" -ForegroundColor $WARN }
function Write-Info    { param([string]$Message); Write-Host "[INFO] $Message" -ForegroundColor $INFO }
function Write-Detail  { param([string]$Message); if ($Verbose) { Write-Host "  $Message" -ForegroundColor $GRAY } }

function Invoke-WebRequestWithRetry {
    param([string]$Uri, [int]$Retries = 3, [string]$Method = "GET")

    for ($i = 0; $i -lt $Retries; $i++) {
        try {
            $response = Invoke-WebRequest -Uri $Uri -Method $Method -TimeoutSec 15 -UseBasicParsing -ErrorAction Stop
            return $response
        }
        catch {
            $err = $_.Exception.Message
            if ($i -lt ($Retries - 1)) {
                Write-Host "  ...Tentativa $($i+1)/$Retries falhou ($err). Aguardando ${RetryDelaySec}s..." -ForegroundColor $GRAY
                Start-Sleep -Seconds $RetryDelaySec
            } else {
                throw $_
            }
        }
    }
}

function Get-TokenPayload {
    param([string]$Token)
    try {
        $parts = $Token.Split('.')
        if ($parts.Count -lt 2) { return $null }
        $payload = $parts[1]
        $padding = $payload.Length % 4
        if ($padding -gt 0) { $payload += "=" * (4 - $padding) }
        $bytes = [System.Convert]::FromBase64String($payload)
        $json = [System.Text.Encoding]::UTF8.GetString($bytes)
        return $json | ConvertFrom-Json
    } catch { return $null }
}

# ============================================================================
# LOGICA DE TESTE
# ============================================================================

function Test-KeycloakHealth {
    Write-Section "1. TESTE DE CONECTIVIDADE (COM RETRY)"

    # Teste Keycloak
    Write-Info "Testando Keycloak ($KeycloakUrl)..."
    try {
        $resp = Invoke-WebRequestWithRetry -Uri "$KeycloakUrl" -Retries $MaxRetries
        Write-Success "Keycloak acessivel (HTTP $($resp.StatusCode))"
        $resultados.Keycloak.Conectividade = $true
    } catch {
        Write-Failure "Keycloak inacessivel apos $MaxRetries tentativas."
        Write-Detail "Erro: $($_.Exception.Message)"
        return $false
    }

    # Teste Backend
    Write-Info "Testando Backend ($BackendUrl)..."
    try {
        $resp = Invoke-WebRequestWithRetry -Uri "$BackendUrl/actuator/health" -Retries $MaxRetries
        Write-Success "Backend acessivel (HTTP $($resp.StatusCode))"
        $resultados.Backend.Conectividade = $true
    } catch {
        Write-Failure "Backend inacessivel apos $MaxRetries tentativas."
        Write-Detail "Erro: $($_.Exception.Message)"
        return $false
    }

    return $true
}

function Test-Usuario {
    param([hashtable]$Usuario)

    $username = $Usuario.Username
    $password = $Usuario.Password
    $roleEsperada = $Usuario.RoleEsperada

    Write-Info "--- Testando: $username ---"

    $resultado = @{ Username = $username; TokenObtido = $false; AutenticacaoBackend = $false; Erros = @() }

    # 1. Obter Token
    $tokenUrl = "$KeycloakUrl/realms/$Realm/protocol/openid-connect/token"
    $body = @{ grant_type = "password"; client_id = $ClientId; username = $username; password = $password }
    if ($ClientSecret) { $body.client_secret = $ClientSecret }

    try {
        $tokenResp = Invoke-RestMethod -Uri $tokenUrl -Method POST -Body $body -ContentType "application/x-www-form-urlencoded" -ErrorAction Stop
        $token = $tokenResp.access_token
        Write-Success "Token JWT obtido"
        $resultado.TokenObtido = $true

        # Debug do Token
        $payload = Get-TokenPayload -Token $token
        Write-Detail "Issuer no Token: $($payload.iss)"
        Write-Detail "Roles no Token: $($payload.realm_access.roles -join ', ')"
    } catch {
        Write-Failure "Falha ao obter token: $($_.Exception.Message)"
        if ($_.ErrorDetails) { Write-Detail "Detalhe: $($_.ErrorDetails.Message)" }
        return $resultado
    }

    # 2. Testar Acesso Protegido
    $endpoint = "$BackendUrl/actuator/info" # Endpoint de teste padrão

    try {
        $apiResp = Invoke-RestMethod -Uri $endpoint -Method GET -Headers @{ "Authorization" = "Bearer $token" } -TimeoutSec 10 -ErrorAction Stop
        Write-Success "Backend aceitou o token (HTTP 200)"
        $resultado.AutenticacaoBackend = $true
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__

        if ($statusCode -eq 401) {
            Write-Failure "Backend rejeitou o token (401 Unauthorized) - POSSIVEL ERRO DE ISSUER/ASSINATURA"
        } elseif ($statusCode -eq 403) {
            Write-Success "Backend aceitou o token, mas negou acesso (403 Forbidden). Autenticacao OK!"
            $resultado.AutenticacaoBackend = $true
        } elseif ($statusCode -eq 404) {
            Write-Success "Backend aceitou o token, mas recurso nao existe (404 Not Found). Autenticacao OK!"
            $resultado.AutenticacaoBackend = $true
        } else {
            Write-Failure "Erro inesperado no backend: $statusCode"
        }
        Write-Detail "Erro API: $($_.Exception.Message)"
    }

    return $resultado
}

# ============================================================================
# EXECUCAO PRINCIPAL
# ============================================================================

Write-Host "Iniciando bateria de testes..." -ForegroundColor Cyan
if (-not (Test-KeycloakHealth)) {
    Write-Warn "Abortando testes de usuario devido a falha na infraestrutura."
    exit 1
}

Write-Section "2. TESTE DE AUTENTICACAO"

foreach ($u in $usuarios) {
    $res = Test-Usuario -Usuario $u
    $resultados.Usuarios += $res
    $resultados.Resumo.Total++

    if ($res.TokenObtido -and $res.AutenticacaoBackend) {
        $resultados.Resumo.Sucesso++
    } else {
        $resultados.Resumo.Falha++
    }
    Start-Sleep -Milliseconds 500
}

Write-Section "3. RELATORIO FINAL"
Write-Host "Total:   $($resultados.Resumo.Total)"
Write-Host "Sucesso: $($resultados.Resumo.Sucesso)" -ForegroundColor $SUCCESS
Write-Host "Falha:   $($resultados.Resumo.Falha)" -ForegroundColor $(if ($resultados.Resumo.Falha -gt 0) { $ERROR_COLOR } else { $SUCCESS })

if ($resultados.Resumo.Falha -eq 0) {
    exit 0
} else {
    exit 1
}