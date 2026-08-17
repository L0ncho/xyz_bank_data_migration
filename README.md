# XYZ Bank Data Migration

Migración de datos bancarios con **Spring Boot 3.5** y **Spring Batch 5**. Procesa los CSV de `data/semana_1` mediante tres jobs independientes (Reader → Processor → Writer), con persistencia JDBC en **MySQL**.

Documentación ampliada:

- [docs/jobs.md](docs/jobs.md) — diagramas y flujo de cada job
- [docs/mysql.md](docs/mysql.md) — Docker MySQL, conexión y consultas de reportes

## Stack

| Tecnología | Uso |
|---|---|
| Java 17+ | Lenguaje |
| Spring Boot 3.5 | Bootstrap |
| Spring Batch 5 | Jobs, steps, skip/retry |
| MySQL 8.4 | Datos de negocio + JobRepository Batch |
| Docker Compose | MySQL local |
| Maven | Build y ejecución |

## Arquitectura

Hexagonal por módulo. Dominio sin Spring. Batch e adapters JDBC en infraestructura.

```
src/main/java/com/xyzbank/migration/
├── shared/
│   ├── application/ports/      MigrationExecutionPort
│   └── infrastructure/
│       ├── adapters/           JdbcMigrationExecutionAdapter
│       └── batch/              Guard, LedgerListener, summary
├── dailytransactions/
│   ├── application/ports/      DailyReportWriter
│   └── infrastructure/
│       ├── adapters/           JdbcDailyReportWriter
│       └── batch/              dailyTransactionsJob
├── monthlyinterests/ ...       AccountBalanceWriter → JdbcAccountBalanceWriter
└── annualreports/ ...          AnnualAuditWriter → JdbcAnnualAuditWriter
```

## Jobs (resumen)

| Job | Guard | Process | Tabla MySQL |
|---|---|---|---|
| `dailyTransactionsJob` | `checkDailyMigrationNotDone` | `processDailyTransactions` | `daily_transaction_reports` |
| `monthlyInterestsJob` | `checkMonthlyMigrationNotDone` | `calculateMonthlyInterests` | `account_balances` |
| `annualGenerationJob` | `checkAnnualMigrationNotDone` | `compileAnnualAudit` | `annual_audit_reports` |

Si el job ya tiene `SUCCESS` en `migration_executions`, se omite el process (`ALREADY_MIGRATED`). Cada lanzamiento usa `RunIdIncrementer` para crear una nueva instancia Batch y consultar el ledger. Detalle en [docs/jobs.md](docs/jobs.md).

## Reglas de negocio

### Transacciones diarias

Se omiten (`skip`) registros con:

- `monto <= 0`
- tipo inválido
- campos obligatorios nulos
- duplicados por `fecha + monto + tipo`

Las anomalías (monto alto, duplicados detectados) se registran en el reporte sin bloquear la escritura cuando el registro es válido.

### Intereses mensuales

Se omiten registros con:

- `saldo <= 0`
- edad fuera del rango 18–100
- tipo inválido
- campos nulos
- `cuenta_id` duplicado

Tasas inferidas:

| Tipo | Condición | Tasa |
|---|---|---|
| ahorro | edad menor a 65 | 1.00% |
| ahorro | edad 65 o más | 1.50% |
| prestamo | — | 1.50% |
| hipoteca | — | 0.80% |

### Auditoría anual

Se omiten registros con:

- depósito con `monto == 0`
- tipo inválido
- campos nulos
- duplicados

Los retiros/compras con montos negativos son válidos. El writer consolida por `cuenta_id`.

## Cómo ejecutar

### 1. Levantar MySQL

```bash
docker compose up -d
```

Conexión: `localhost:3306`, DB `xyz_bank_migration`, user/password `migration`/`migration`. Más detalle en [docs/mysql.md](docs/mysql.md).

### 2. Tests

```bash
mvn test
```

### 3. Correr un job

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.enabled=true --spring.batch.job.name=dailyTransactionsJob"

mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.enabled=true --spring.batch.job.name=monthlyInterestsJob"

mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.enabled=true --spring.batch.job.name=annualGenerationJob"
```

Por defecto `spring.batch.job.enabled=false`.

### 4. Ver reportes migrados

```bash
docker compose exec mysql mysql -umigration -pmigration xyz_bank_migration -e "SELECT * FROM migration_executions; SELECT * FROM daily_transaction_reports LIMIT 10;"
```

Consultas adicionales en [docs/mysql.md](docs/mysql.md).

## Revertir y volver a migrar

```bash
docker compose exec -T mysql mysql -umigration -pmigration xyz_bank_migration < scripts/revert-migration.sql
```

Luego vuelve a ejecutar el job deseado. El script limpia tablas de negocio, `migration_executions` y metadatos `BATCH_*`.

## Datos de entrada

| Archivo | Job |
|---|---|
| [`data/semana_1/transacciones.csv`](data/semana_1/transacciones.csv) | dailyTransactionsJob |
| [`data/semana_1/intereses.csv`](data/semana_1/intereses.csv) | monthlyInterestsJob |
| [`data/semana_1/cuentas_anuales.csv`](data/semana_1/cuentas_anuales.csv) | annualGenerationJob |
