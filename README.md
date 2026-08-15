# XYZ Bank Data Migration

Migración de datos bancarios con **Spring Boot 3.5** y **Spring Batch 5**. Procesa los CSV de `data/semana_1` mediante tres jobs independientes, cada uno con un único step chunk-oriented (`ItemReader` → `ItemProcessor` → `ItemWriter`).

La escritura a base relacional queda para la **Fase 2**. En esta fase los puertos de escritura se implementan con adapters de logging (consola).

## Stack

| Tecnología | Uso |
|---|---|
| Java 17+ | Lenguaje |
| Spring Boot 3.5 | Bootstrap y auto-configuración |
| Spring Batch 5 | Jobs, steps, skip/retry |
| H2 | Solo metadatos del `JobRepository` (no datos de negocio) |
| Maven | Build y ejecución |

## Arquitectura

Hexagonal por módulo de negocio. El dominio no depende de Spring. Batch vive en infraestructura.

```
src/main/java/com/xyzbank/migration/
├── BatchApplication.java
├── shared/
│   ├── domain/                 DomainError, Id, Money, BusinessDate
│   └── infrastructure/batch/   JobSummaryListener, LoggingSkipListener
├── dailytransactions/
│   ├── domain/
│   ├── application/ports/      DailyReportWriter (+ InMemory)
│   └── infrastructure/batch/   dailyTransactionsJob
├── monthlyinterests/
│   ├── domain/
│   ├── application/ports/      AccountBalanceWriter (+ InMemory)
│   └── infrastructure/batch/   monthlyInterestsJob
└── annualreports/
    ├── domain/
    ├── application/ports/      AnnualAuditWriter (+ InMemory)
    └── infrastructure/batch/   annualGenerationJob
```

```
JobLauncher
    └── Job (proceso de migración)
            └── Step (fase única)
                    ├── ItemReader   lee CSV
                    ├── ItemProcessor valida y transforma (dominio)
                    └── ItemWriter   escribe vía Puerto
JobRepository (H2) ← estado de ejecución
```

## Jobs

### 1. `dailyTransactionsJob`

- **CSV:** `data/semana_1/transacciones.csv`
- **Step:** `processDailyTransactions`
- **Procesa:** transacciones diarias, detecta anomalías (monto > 2000, duplicados) y genera resumen
- **Puerto:** `DailyReportWriter` → `LoggingDailyReportWriter`

### 2. `monthlyInterestsJob`

- **CSV:** `data/semana_1/intereses.csv`
- **Step:** `calculateMonthlyInterests`
- **Procesa:** aplica intereses individuales y actualiza saldo final
- **Puerto:** `AccountBalanceWriter` → `LoggingAccountBalanceWriter`

Tasas inferidas por cuenta:

| Tipo | Condición | Tasa |
|---|---|---|
| ahorro | edad menor a 65 | 1.00% |
| ahorro | edad 65 o más | 1.50% |
| prestamo | — | 1.50% |
| hipoteca | — | 0.80% |

`saldoFinal = saldo × (1 + tasa)`

### 3. `annualGenerationJob`

- **CSV:** `data/semana_1/cuentas_anuales.csv`
- **Step:** `compileAnnualAudit`
- **Procesa:** valida movimientos y consolida por `cuenta_id` el reporte de auditoría
- **Puerto:** `AnnualAuditWriter` → `LoggingAnnualAuditWriter`

## Políticas skip / retry

**Normalización (no skip):** fecha `yyyy/MM/dd` → `yyyy-MM-dd`; trim de strings.

**Skip** (`DomainError` / `FlatFileParseException`, `skipLimit=100`):

- Transacciones: `monto <= 0`, tipo inválido, campos obligatorios nulos, duplicados (fecha+monto+tipo)
- Intereses: `saldo <= 0`, edad fuera de 18–100, tipo inválido, campos nulos, `cuenta_id` duplicado
- Anual: depósito con `monto == 0`, tipo inválido, campos nulos, duplicados; retiro/compra negativos son válidos

**Retry:** 3 intentos solo para errores técnicos del writer (`TransientDataAccessException`).

Los processors stateful usan `processorNonTransactional()` para no reprocesar ítems válidos tras un skip en el chunk.

## Cómo ejecutar

Requisitos: JDK 17+ y Maven.

```bash
# Tests
mvn test

# Job de transacciones diarias
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.enabled=true --spring.batch.job.name=dailyTransactionsJob"

# Job de intereses mensuales
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.enabled=true --spring.batch.job.name=monthlyInterestsJob"

# Job de consolidado anual
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.enabled=true --spring.batch.job.name=annualGenerationJob"
```

Por defecto `spring.batch.job.enabled=false` para no lanzar los tres jobs a la vez.

## Fase 2 (pendiente)

Reemplazar los adapters `Logging*Writer` por implementaciones JDBC de los mismos puertos:

- `DailyReportWriter`
- `AccountBalanceWriter`
- `AnnualAuditWriter`

Sin cambios en dominio ni en la definición de los jobs.

## Datos de entrada

| Archivo | Job |
|---|---|
| [`data/semana_1/transacciones.csv`](data/semana_1/transacciones.csv) | dailyTransactionsJob |
| [`data/semana_1/intereses.csv`](data/semana_1/intereses.csv) | monthlyInterestsJob |
| [`data/semana_1/cuentas_anuales.csv`](data/semana_1/cuentas_anuales.csv) | annualGenerationJob |
