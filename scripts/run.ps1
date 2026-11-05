# Compila y ejecuta TecnoMovil Data (Windows / PowerShell).
# Uso:  powershell -ExecutionPolicy Bypass -File scripts\run.ps1 [cantidad] [--secuencial] [--sin-rendimiento]
# Ej.:  powershell -ExecutionPolicy Bypass -File scripts\run.ps1 6000000
$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Push-Location $raiz
try {
    if (Test-Path out) { Remove-Item -Recurse -Force out }
    $fuentes = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
    javac -encoding UTF-8 -d out $fuentes
    if ($LASTEXITCODE -ne 0) { exit 1 }
    java -Xmx4g "-Dstdout.encoding=UTF-8" -cp out co.edu.iudigital.tecnomovil.Main @args
} finally {
    Pop-Location
}
