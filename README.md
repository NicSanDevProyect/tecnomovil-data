# TecnoMovil Data – Procesamiento funcional de datos de transporte urbano

Módulo en **Java puro** (sin frameworks externos) que procesa registros simulados de
validadores del sistema de transporte de la ciudad de TecnoValle aplicando
**programación funcional**: funciones puras, inmutabilidad, lambdas, funciones de
orden superior y *Streams* (secuenciales y paralelos).

Actividad: **S20 - EA2. Creación de repositorio** – Programación Orientada a Objetos II
(PREICA2602B020046), Institución Universitaria Digital de Antioquia.
Docente: Jorge Armando Julio Cruz.

## Integrantes

- Clara Valentina Zapata Escobar
- Nicolás Alberto Santa Ochoa

## Tareas implementadas

| Tarea | Clase | Técnica principal |
|---|---|---|
| a) Afluencia por estación | `AfluenciaEstaciones` | `filter` + `groupingBy` + `counting` |
| b) Horas pico | `HorasPico` | Agrupación por hora y función que devuelve la regla de clasificación |
| c) Rutas más utilizadas | `RutasPopulares` | Conteo + `sorted` + `limit` |
| d) Patrones de viaje por usuario | `PatronesViaje` | `groupingBy` + `mapping` a lista inmodificable |
| e) Tiempo promedio entre estaciones | `TiemposEntreEstaciones` | Pares consecutivos con `IntStream` + `averagingLong` |
| f) Detección de sobrecarga | `SobrecargaRutas` | Capacidad (`Function`) y umbral (`Predicate`) recibidos como parámetros |

Además, el proyecto incluye:

- Una versión **imperativa** de referencia (`EnfoqueImperativo`) que muestra la condición de
  carrera de un contador compartido frente a la versión funcional.
- Una **verificación de inmutabilidad** al ejecutar el programa.
- Una **comparación de rendimiento** entre `stream()` y `parallelStream()`.
- **Pruebas propias** sin JUnit (`test/PruebasTecnoMovil.java`).

## Estructura

```
src/co/edu/iudigital/tecnomovil/
├── modelo/          Registro (record inmutable), Accion (enum), Ruta (record)
├── datos/           CatalogoRutas y GeneradorRegistros (determinista, semilla 2026)
├── procesamiento/   Tareas a–f como funciones puras + Operaciones de orden superior
├── reporte/         FormateadorReporte (funciones puras que devuelven texto)
├── referencia/      EnfoqueImperativo (solo para comparar)
├── rendimiento/     Medidor (stream vs. parallelStream)
└── Main.java        Única capa con efectos secundarios (imprimir en consola)
test/                Pruebas propias (sin JUnit)
scripts/             Compilar, ejecutar y probar
```

## Requisitos

- JDK 17 o superior (desarrollado y probado con JDK 26). No requiere Maven, Gradle ni librerías.

## Ejecución

**Windows (PowerShell):**

```powershell
powershell -ExecutionPolicy Bypass -File scripts\run.ps1                 # 1.000.000 de registros
powershell -ExecutionPolicy Bypass -File scripts\run.ps1 6000000         # 6.000.000 de registros
powershell -ExecutionPolicy Bypass -File scripts\run.ps1 --secuencial    # todo con stream()
powershell -ExecutionPolicy Bypass -File scripts\test.ps1                # pruebas
```

**Linux / macOS / Git Bash:**

```bash
./scripts/run.sh [cantidad] [--secuencial] [--sin-rendimiento]
./scripts/test.sh
```

**Manual:**

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -Xmx4g -cp out co.edu.iudigital.tecnomovil.Main 1000000
```

Con 6 millones de registros se recomienda `-Xmx4g` (los scripts ya lo incluyen).

## IntelliJ IDEA

1. *File → Open* y seleccionar la carpeta del proyecto.
2. Clic derecho en `src` → *Mark Directory as → Sources Root* (y `test` → *Test Sources Root*).
3. Ejecutar `Main`. Para otra cantidad de registros: *Run → Edit Configurations → Program arguments*
   (por ejemplo `6000000`) y en *VM options* `-Xmx4g`.
