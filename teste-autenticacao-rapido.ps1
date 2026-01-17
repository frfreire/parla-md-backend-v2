# ============================================================================
# PARLA-MD - TESTE RÁPIDO DE AUTENTICAÇÃO
# ============================================================================
# Versão simplificada para teste rápido
# ============================================================================

param(
    [string]$Username = "admin.parla",
    [string]$Password = "Admin@123"
)

$KeycloakUrl = "http://localhost:8180"
$BackendUrl = "http://localhost:8081"
$Realm = "parlamd"
$ClientId = "parla-md-client"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TESTE RÁPIDO DE AUTENTICAÇÃO" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# ============================================================================
# 1. OBTER TOKEN
# ============================================================================
Write-Host "1. Obtendo token para: $Username" -ForegroundColor Yellow

$tokenUrl = "$KeycloakUrl/realms/$Realm/protocol/openid-connect/token"

$body = @{
    grant_type = "password"
    client_id = $ClientId
    username = $Username
    password = $Password
}

try {
    $response = Invoke-RestMethod -Uri $tokenUrl -Method POST -Body $body -ContentType "application/x-www-form-urlencoded" -ErrorAction Stop
    
    $token = $response.access_token
    
    Write-Host "[OK] Token obtido com sucesso!" -ForegroundColor Green
    Write-Host "`nToken (primeiros 50 caracteres):" -ForegroundColor Gray
    Write-Host $token.Substring(0, [Math]::Min(50, $token.Length))... -ForegroundColor Gray
    
} catch {
    Write-Host "[ERRO] Falha ao obter token" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Gray
    exit 1
}

# ============================================================================
# 2. DECODIFICAR TOKEN
# ============================================================================
Write-Host "`n2. Decodificando token..." -ForegroundColor Yellow

try {
    $parts = $token.Split('.')
    $payload = $parts[1]
    
    # Adicionar padding
    $padding = $payload.Length % 4
    if ($padding -gt 0) {
        $payload += "=" * (4 - $padding)
    }
    
    $bytes = [System.Convert]::FromBase64String($payload)
    $json = [System.Text.Encoding]::UTF8.GetString($bytes)
    $tokenData = $json | ConvertFrom-Json
    
    Write-Host "[OK] Token decodificado" -ForegroundColor Green
    
    Write-Host "`nInformações do Token:" -ForegroundColor Cyan
    Write-Host "  Username: $($tokenData.preferred_username)" -ForegroundColor Gray
    Write-Host "  Email:    $($tokenData.email)" -ForegroundColor Gray
    Write-Host "  Nome:     $($tokenData.name)" -ForegroundColor Gray
    
    if ($tokenData.realm_access -and $tokenData.realm_access.roles) {
        Write-Host "  Roles:    $($tokenData.realm_access.roles -join ', ')" -ForegroundColor Gray
    }
    
    $exp = [DateTimeOffset]::FromUnixTimeSeconds($tokenData.exp).LocalDateTime
    Write-Host "  Expira:   $exp" -ForegroundColor Gray
    
} catch {
    Write-Host "[ERRO] Falha ao decodificar token" -ForegroundColor Red
}

# ============================================================================
# 3. TESTAR ENDPOINT PROTEGIDO
# ============================================================================
Write-Host "`n3. Testando endpoint protegido..." -ForegroundColor Yellow

$headers = @{
    "Authorization" = "Bearer $token"
}

try {
    $userInfo = Invoke-RestMethod -Uri "$BackendUrl/api/auth/me" -Method GET -Headers $headers -ErrorAction Stop
    
    Write-Host "[OK] Endpoint acessível!" -ForegroundColor Green
    
    Write-Host "`nDados retornados pela API:" -ForegroundColor Cyan
    Write-Host "  ID:       $($userInfo.id)" -ForegroundColor Gray
    Write-Host "  Username: $($userInfo.username)" -ForegroundColor Gray
    Write-Host "  Email:    $($userInfo.email)" -ForegroundColor Gray
    Write-Host "  Nome:     $($userInfo.nomeCompleto)" -ForegroundColor Gray
    Write-Host "  Roles:    $($userInfo.roles -join ', ')" -ForegroundColor Gray
    Write-Host "  Ativo:    $($userInfo.ativo)" -ForegroundColor Gray
    
    if ($userInfo.permissoes) {
        Write-Host "  Permissões: $($userInfo.permissoes.Count) permissões" -ForegroundColor Gray
        
        if ($userInfo.permissoes.Count -le 10) {
            Write-Host "`n  Lista de Permissões:" -ForegroundColor Cyan
            $userInfo.permissoes | ForEach-Object {
                Write-Host "    - $_" -ForegroundColor Gray
            }
        }
    }
    
} catch {
    Write-Host "[ERRO] Falha ao acessar endpoint" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Gray
    exit 1
}

# ============================================================================
# 4. TESTE ADICIONAL - HEALTH CHECK
# ============================================================================
Write-Host "`n4. Testando health check..." -ForegroundColor Yellow

try {
    $health = Invoke-RestMethod -Uri "$BackendUrl/api/auth/health" -Method GET -Headers $headers -ErrorAction Stop
    
    Write-Host "[OK] Health check: $($health.status)" -ForegroundColor Green
    
} catch {
    Write-Host "[AVISO] Endpoint health não acessível" -ForegroundColor Yellow
}

# ============================================================================
# RESUMO
# ============================================================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "RESUMO" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "✓ AUTENTICAÇÃO FUNCIONANDO CORRETAMENTE!" -ForegroundColor Green

Write-Host "`nPróximos passos:" -ForegroundColor Yellow
Write-Host "  1. Use o token em suas requisições HTTP" -ForegroundColor Gray
Write-Host "  2. Adicione 'Authorization: Bearer [token]' no header" -ForegroundColor Gray
Write-Host "  3. Token válido por ~30 minutos" -ForegroundColor Gray

Write-Host "`nExemplo de uso:" -ForegroundColor Cyan
Write-Host @"
`$headers = @{
    "Authorization" = "Bearer $($token.Substring(0, 20))..."
}

`$processos = Invoke-RestMethod ``
    -Uri "$BackendUrl/api/processos" ``
    -Method GET ``
    -Headers `$headers
"@ -ForegroundColor Gray

Write-Host "`n"
