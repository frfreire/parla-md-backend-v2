# ============================================================================
# PARLA-MD - SCRIPT DE VERIFICACAO DE SERVICOS
# ============================================================================
# Verifica se todos os containers e servicos estao funcionando corretamente
# ============================================================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "PARLA-MD - VERIFICACAO DE SERVICOS" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Cores
$SUCCESS = "Green"
$ERROR = "Red"
$INFO = "Yellow"
$GRAY = "Gray"

# Funcao para testar endpoint
function Test-Endpoint {
    param(
        [string]$Url,
        [string]$ServiceName
    )

    try {
        $response = Invoke-WebRequest -Uri $Url -Method GET -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "[OK] $ServiceName" -ForegroundColor $SUCCESS -NoNewline
            Write-Host " - HTTP $($response.StatusCode)" -ForegroundColor $GRAY
            return $true
        }
    }
    catch {
        Write-Host "[FALHOU] $ServiceName" -ForegroundColor $ERROR
        return $false
    }
}

# Funcao para testar porta TCP
function Test-Port {
    param(
        [string]$HostName,
        [int]$Port,
        [string]$ServiceName
    )

    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $tcpClient.Connect($HostName, $Port)
        $tcpClient.Close()
        Write-Host "[OK] $ServiceName" -ForegroundColor $SUCCESS -NoNewline
        Write-Host " - Porta $Port acessivel" -ForegroundColor $GRAY
        return $true
    }
    catch {
        Write-Host "[FALHOU] $ServiceName" -ForegroundColor $ERROR -NoNewline
        Write-Host " - Porta $Port inacessivel" -ForegroundColor $GRAY
        return $false
    }
}

# ============================================================================
# 1. VERIFICAR STATUS DOS CONTAINERS
# ============================================================================
Write-Host "`n1. STATUS DOS CONTAINERS:" -ForegroundColor $INFO
Write-Host "============================================`n" -ForegroundColor $INFO

$allRunning = $true

# Lista de containers esperados
$expectedContainers = @(
    "parlamd-backend",
    "parlamd-mongodb",
    "parlamd-redis",
    "parlamd-rabbitmq",
    "parlamd-keycloak",
    "parlamd-keycloak-postgres",
    "parlamd-ollama"
)

try {
    # Usar docker ps diretamente (mais confiavel que docker-compose ps --format json)
    $runningContainers = docker ps --format "{{.Names}}::{{.Status}}" 2>&1

    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERRO] Nao foi possivel executar docker ps" -ForegroundColor $ERROR
        Write-Host "Certifique-se que o Docker esta rodando" -ForegroundColor $INFO
        exit 1
    }

    # Criar hashtable com status dos containers
    $containerStatus = @{}
    foreach ($line in $runningContainers) {
        if ($line -match "(.+)::(.+)") {
            $name = $matches[1]
            $status = $matches[2]
            $containerStatus[$name] = $status
        }
    }

    # Verificar cada container esperado
    foreach ($containerName in $expectedContainers) {
        if ($containerStatus.ContainsKey($containerName)) {
            $status = $containerStatus[$containerName]
            if ($status -like "*Up*") {
                Write-Host "[OK] $containerName" -ForegroundColor $SUCCESS -NoNewline
                Write-Host " - Running" -ForegroundColor $GRAY
            }
            else {
                Write-Host "[FALHOU] $containerName" -ForegroundColor $ERROR -NoNewline
                Write-Host " - $status" -ForegroundColor $GRAY
                $allRunning = $false
            }
        }
        else {
            Write-Host "[FALHOU] $containerName" -ForegroundColor $ERROR -NoNewline
            Write-Host " - Nao encontrado" -ForegroundColor $GRAY
            $allRunning = $false
        }
    }

    # Verificar se ha containers rodando
    if ($containerStatus.Count -eq 0) {
        Write-Host "`n[ERRO] Nenhum container em execucao!" -ForegroundColor $ERROR
        Write-Host "Execute: docker-compose up -d`n" -ForegroundColor $INFO
        exit 1
    }
}
catch {
    Write-Host "[ERRO] Falha ao verificar containers: $($_.Exception.Message)" -ForegroundColor $ERROR
    Write-Host "`nTentando metodo alternativo..." -ForegroundColor $INFO

    # Metodo alternativo: docker-compose ps sem JSON
    try {
        $psOutput = docker-compose ps 2>&1
        Write-Host "`n$psOutput" -ForegroundColor $GRAY
    }
    catch {
        Write-Host "[ERRO] Tambem falhou: $($_.Exception.Message)" -ForegroundColor $ERROR
    }
}

if (-not $allRunning) {
    Write-Host "`n[AVISO] Alguns containers nao estao rodando!" -ForegroundColor $ERROR
    Write-Host "Execute: docker-compose logs [nome-do-container]`n" -ForegroundColor $INFO
}

# ============================================================================
# 2. VERIFICAR HEALTH CHECKS
# ============================================================================
Write-Host "`n2. HEALTH CHECKS (HTTP):" -ForegroundColor $INFO
Write-Host "============================================`n" -ForegroundColor $INFO

$allHealthy = $true

$healthChecks = @(
    @{Name="Backend API"; Url="http://localhost:8081/actuator/health"},
    @{Name="Keycloak"; Url="http://localhost:8180/health/ready"},
    @{Name="RabbitMQ Management"; Url="http://localhost:15672"},
    @{Name="Ollama"; Url="http://localhost:11434/api/tags"}
)

foreach ($check in $healthChecks) {
    if (-not (Test-Endpoint -Url $check.Url -ServiceName $check.Name)) {
        $allHealthy = $false
    }
    Start-Sleep -Milliseconds 500
}

# ============================================================================
# 3. VERIFICAR PORTAS TCP
# ============================================================================
Write-Host "`n3. CONECTIVIDADE TCP:" -ForegroundColor $INFO
Write-Host "============================================`n" -ForegroundColor $INFO

$tcpChecks = @(
    @{Name="MongoDB"; HostName="localhost"; Port=27017},
    @{Name="Redis"; HostName="localhost"; Port=6379},
    @{Name="RabbitMQ AMQP"; HostName="localhost"; Port=5672},
    @{Name="Keycloak"; HostName="localhost"; Port=8180}
)

foreach ($check in $tcpChecks) {
    Test-Port -HostName $check.HostName -Port $check.Port -ServiceName $check.Name | Out-Null
    Start-Sleep -Milliseconds 500
}

# ============================================================================
# 4. VERIFICAR LOGS DE ERRO
# ============================================================================
Write-Host "`n4. VERIFICACAO DE LOGS:" -ForegroundColor $INFO
Write-Host "============================================`n" -ForegroundColor $INFO

$services = @("parlamd-backend", "parlamd-keycloak", "parlamd-rabbitmq")

foreach ($service in $services) {
    try {
        $logs = docker logs $service --tail 50 2>&1
        $errors = $logs | Select-String -Pattern "ERROR|FATAL|Exception" -SimpleMatch

        if ($errors) {
            Write-Host "[AVISO] $service" -ForegroundColor $ERROR -NoNewline
            Write-Host " - Erros encontrados nos logs" -ForegroundColor $GRAY
            Write-Host "  Execute: docker logs $service" -ForegroundColor $INFO
        }
        else {
            Write-Host "[OK] $service" -ForegroundColor $SUCCESS -NoNewline
            Write-Host " - Sem erros nos logs" -ForegroundColor $GRAY
        }
    }
    catch {
        Write-Host "[AVISO] $service" -ForegroundColor $ERROR -NoNewline
        Write-Host " - Container nao encontrado ou nao acessivel" -ForegroundColor $GRAY
    }
}

# ============================================================================
# 5. TESTE DE API - BACKEND
# ============================================================================
Write-Host "`n5. TESTE DE API (BACKEND):" -ForegroundColor $INFO
Write-Host "============================================`n" -ForegroundColor $INFO

try {
    $healthResponse = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -Method GET -TimeoutSec 5 -ErrorAction Stop

    Write-Host "Backend Health Status:" -ForegroundColor $INFO
    Write-Host "  Status Geral: " -NoNewline

    if ($healthResponse.status -eq "UP") {
        Write-Host "UP" -ForegroundColor $SUCCESS
    }
    else {
        Write-Host $healthResponse.status -ForegroundColor $ERROR
    }

    if ($healthResponse.components) {
        Write-Host "`n  Componentes:" -ForegroundColor $GRAY

        foreach ($component in $healthResponse.components.PSObject.Properties) {
            $name = $component.Name
            $status = $component.Value.status

            Write-Host "    - $name : " -NoNewline
            if ($status -eq "UP") {
                Write-Host $status -ForegroundColor $SUCCESS
            }
            else {
                Write-Host $status -ForegroundColor $ERROR
            }
        }
    }
}
catch {
    Write-Host "[ERRO] Nao foi possivel consultar API do Backend" -ForegroundColor $ERROR
    Write-Host "  Verifique se o container parlamd-backend esta rodando" -ForegroundColor $INFO
}

# ============================================================================
# 6. RESUMO FINAL
# ============================================================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "RESUMO DA VERIFICACAO" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

if ($allRunning -and $allHealthy) {
    Write-Host "[SUCESSO] TODOS OS SERVICOS ESTAO FUNCIONANDO!" -ForegroundColor $SUCCESS
    Write-Host "`nURLs de Acesso:" -ForegroundColor $INFO
    Write-Host "  - Backend API:    http://localhost:8081" -ForegroundColor $GRAY
    Write-Host "  - Swagger UI:     http://localhost:8081/swagger-ui.html" -ForegroundColor $GRAY
    Write-Host "  - Health Check:   http://localhost:8081/actuator/health" -ForegroundColor $GRAY
    Write-Host "  - Keycloak Admin: http://localhost:8180" -ForegroundColor $GRAY
    Write-Host "  - RabbitMQ:       http://localhost:15672 (parlamd/rabbit_secure_2024)" -ForegroundColor $GRAY
}
else {
    Write-Host "[AVISO] ALGUNS SERVICOS APRESENTARAM PROBLEMAS" -ForegroundColor $ERROR
    Write-Host "`nComandos uteis:" -ForegroundColor $INFO
    Write-Host "  - Ver status:     docker-compose ps" -ForegroundColor $GRAY
    Write-Host "  - Ver logs:       docker-compose logs -f [servico]" -ForegroundColor $GRAY
    Write-Host "  - Reiniciar:      docker-compose restart [servico]" -ForegroundColor $GRAY
    Write-Host "  - Recriar tudo:   docker-compose down ; docker-compose up -d" -ForegroundColor $GRAY
}

Write-Host "`n========================================`n" -ForegroundColor Cyan

# Pausar para ver resultado
Write-Host "Pressione qualquer tecla para continuar..." -ForegroundColor $GRAY
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Retornar codigo de saida
if ($allRunning -and $allHealthy) {
    exit 0
}
else {
    exit 1
}