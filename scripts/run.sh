#!/usr/bin/env bash
# Compila y ejecuta TecnoMovil Data (Linux / macOS / Git Bash).
# Uso:  ./scripts/run.sh [cantidad] [--secuencial] [--sin-rendimiento]
set -euo pipefail
cd "$(dirname "$0")/.."
rm -rf out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -Xmx4g -Dstdout.encoding=UTF-8 -cp out co.edu.iudigital.tecnomovil.Main "$@"
