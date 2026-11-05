#!/usr/bin/env bash
# Compila y ejecuta las pruebas propias (sin JUnit) en Linux / macOS / Git Bash.
set -euo pipefail
cd "$(dirname "$0")/.."
rm -rf out-test
javac -encoding UTF-8 -d out-test $(find src test -name "*.java")
java -Dstdout.encoding=UTF-8 -cp out-test co.edu.iudigital.tecnomovil.PruebasTecnoMovil
