# Coupon API

API REST para cadastro e exclusão lógica de cupons, com regras de negócio encapsuladas no domínio (DDD), tratamento de exceções padronizado e documentação OpenAPI (Swagger).

**Board do projeto (Kanban):** [GitHub Project →](https://github.com/users/Felipendev/projects/1) — Backlog, TODO, In progress, In review, Done.

## Requisitos

- Java 17+
- Maven 3.8+ (ou use o wrapper: `./mvnw`)

## Execução local

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Banco **H2** em memória; console H2 em http://localhost:8080/h2-console e Swagger em http://localhost:8080/swagger-ui.html.

## API containerizada na AWS

A API está disponível em ambiente de demonstração, rodando em um container Docker em uma instância **Amazon EC2** (região us-east-2).

| Recurso | URL |
|--------|-----|
| **Base** | http://ec2-3-144-142-96.us-east-2.compute.amazonaws.com:8080 |
| **Swagger UI** | http://ec2-3-144-142-96.us-east-2.compute.amazonaws.com:8080/swagger-ui/index.html |
| **OpenAPI (JSON)** | http://ec2-3-144-142-96.us-east-2.compute.amazonaws.com:8080/v3/api-docs |

A aplicação foi publicada como imagem Docker na máquina EC2 (porta 8080 exposta). O banco é H2 em memória dentro do container; os dados não persistem entre reinícios do container.

## Endpoints

| Método | Path        | Descrição |
|--------|-------------|------------|
| POST   | /coupon     | Cria um cupom. Body: `code`, `description`, `discountValue`, `expirationDate`, `published`. Código deve ter exatamente 6 caracteres alfanuméricos após sanitização (especiais removidos). |
| GET    | /coupon/{id} | Retorna o cupom pelo ID. 404 se não existir ou estiver deletado. |
| DELETE | /coupon/{id} | Soft delete do cupom. Retorna 204. 404 se o ID não existir. 422 se o cupom já estiver deletado. |

### Exemplo de criação (POST /coupon)

```json
{
  "code": "ABC123",
  "description": "Desconto de exemplo",
  "discountValue": 0.8,
  "expirationDate": "2026-12-12",
  "published": false
}
```

O campo `expirationDate` é uma **data** (formato `yyyy-MM-dd`), não data-hora.

Resposta (201): `id`, `code` (sanitizado, ex: `ABC123`), `description`, `discountValue`, `expirationDate`, `published`.

## Regras de negócio (domínio)

- **Código**: alfanumérico, **exatamente** 6 caracteres após sanitização; caracteres especiais são removidos antes de validar. Se tiver mais ou menos de 6 alfanuméricos, retorna erro.
- **Desconto**: valor mínimo 0,5.
- **Data de expiração**: não pode ser no passado.
- **Delete**: soft delete (campo `deleted`); não é possível deletar um cupom já deletado (422).

## Testes

```bash
./mvnw test
```

- Testes de **domínio** (`CouponTest`): criação, sanitização do código, validações (desconto, expiração, código longo/curto), soft delete e “já deletado”.
- Testes de **service** (`CouponServiceImplTest`): criação e delete com repositório mockado.
- Testes de **integração** (`CouponControllerIntegrationTest`): fluxo completo de criação, GET e delete via HTTP.

## CI (GitHub Actions)

O workflow em `.github/workflows/ci.yml` roda em **push** nas branches `main`, `dev`, `feature/**`, `fix/**`, `test/**`, `docs/**`, `chore/**` e `ci/**`, e em **pull request** para `main` e `dev`. Passos: checkout, Java 17 (Eclipse Temurin) com cache Maven e `./mvnw clean verify` (build, testes e checagem de cobertura ≥ 80%). O relatório JaCoCo é publicado como artefato da run.

### Cobertura (≥ 80% nas regras de negócio)

A cobertura é medida com **JaCoCo**. O mínimo de **80% de linhas** é exigido sobre o código de regras de negócio (domínio, service e controller); aplicação principal, DTOs e classes de exceção ficam fora da conta.

**Como ver o relatório:**

1. Rode os testes e gere o relatório:
   ```bash
   ./mvnw clean test jacoco:report
   ```
2. Abra no navegador o HTML:
   ```
   target/site/jacoco/index.html
   ```
   Você verá a cobertura por pacote e por classe (linhas e branches).

**Como garantir o mínimo de 80%:**

O plugin JaCoCo está configurado para **falhar o build** se a cobertura ficar abaixo de 80% no código considerado. Use:

```bash
./mvnw verify
```

Se a cobertura estiver abaixo do mínimo, o build falha com uma mensagem como:
`Rule violated for bundle coupon-api: lines covered ratio is 0.xx, but expected minimum is 0.80`.

O mínimo configurável está em `pom.xml` na propriedade `jacoco.minimum.line.coverage`.

## Docker

### Build a partir do código-fonte

Na raiz do projeto (onde estão `Dockerfile`, `pom.xml`, `mvnw` e `src/`):

```bash
docker build -t coupon-api .
```

### Build apenas com o JAR

Se você tiver só o JAR (ex.: `target/coupon-api-0.0.1-SNAPSHOT.jar`), coloque na mesma pasta o `Dockerfile.jar` e o JAR e rode:

```bash
docker build -f Dockerfile.jar -t coupon-api .
```

### Execução com Docker Compose

```bash
docker compose up --build
```

A aplicação fica disponível em `http://localhost:8080` (porta mapeada no `docker-compose.yml`).

## Decisões técnicas

- **Estrutura**: controller → service → domain + repository; DTOs separados da entidade JPA.
- **Domínio**: regras em `Coupon.create()` e `Coupon.delete()`; validações de expiração, desconto e sanitização do código no domínio.
- **Exceções**: `BusinessException` com status HTTP configurável; `GlobalExceptionHandler` com `@ControllerAdvice` e `@ResponseBody` (equivalente a `@RestControllerAdvice`) para respostas padronizadas e Bean Validation (erros de validação retornam mapa de campo → mensagem).
- **Soft delete**: campo `deleted` na entidade; `@SQLRestriction("deleted = false")` (Hibernate) para não retornar deletados nas buscas; delete por id verifica “já deletado” via query nativa antes de chamar o domínio.
- **Stack**: Spring Boot 4.0, Java 17, H2, JPA, SpringDoc (Swagger), Bean Validation.
