# TecnoMovil Data – Procesamiento funcional de datos de transporte urbano

Módulo en **Java puro** (sin frameworks externos) que procesa registros simulados de
validadores del sistema de transporte de la ciudad de TecnoValle aplicando
**programación funcional**: funciones puras, inmutabilidad, lambdas, funciones de
orden superior y *Streams* (secuenciales y paralelos).

Actividad: **S20 - EA2. Creación de repositorio** – Programación Orientada a Objetos II
(PREICA2602B020046), Institución Universitaria Digital de Antioquia.

## Integrantes

- Clara Valentina Zapata Escobar
- Nicolás Alberto Santa Ochoa

## Estructura

```
src/co/edu/iudigital/tecnomovil/
├── modelo/          Registro (record inmutable) y Accion (enum)
├── datos/           Generador determinista de registros simulados
├── procesamiento/   Tareas a–f como funciones puras
├── reporte/         Formato de la salida por consola
├── referencia/      Versión imperativa para comparar
└── Main.java
test/                Pruebas propias (sin JUnit)
scripts/             Compilar, ejecutar y probar
```

## Requisitos

- JDK 17 o superior (desarrollado con JDK 26).
