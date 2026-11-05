# Compila y ejecuta las pruebas propias (sin JUnit) en Windows / PowerShell.
# Uso:  powershell -ExecutionPolicy Bypass -File scripts\test.ps1
$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Push-Location $raiz
try {
    if (Test-Path out-test) { Remove-Item -Recurse -Force out-test }
    $fuentes = Get-ChildItem -Recurse src, test -Filter *.java | ForEach-Object { $_.FullName }
    javac -encoding UTF-8 -d out-test $fuentes
    if ($LASTEXITCODE -ne 0) { exit 1 }
    java "-Dstdout.encoding=UTF-8" -cp out-test co.edu.iudigital.tecnomovil.PruebasTecnoMovil
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
