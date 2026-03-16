# Coupon API

API REST para cadastro e exclusão lógica de cupons, com regras de negócio encapsuladas no domínio (DDD), tratamento de exceções padronizado e documentação OpenAPI (Swagger).

## Requisitos

- Java 17+
- Maven 3.8+ (ou use o wrapper: `./mvnw`)

## Ambientes (dev / prod)

A aplicação usa **perfis Spring** para separar desenvolvimento e produção.

| Perfil | Banco        | Uso                          |
|--------|--------------|------------------------------|
| **dev** (padrão) | H2 em memória | Desenvolvimento local, testes |
| **prod**        | PostgreSQL   | Produção ou simulação com Docker |

### Desenvolvimento (dev – padrão)

```bash
./mvnw spring-boot:run
```

Ou explicitamente:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

- Banco **H2** em memória; schema recriado a cada subida (`ddl-auto: create-drop`).
- **Console H2** habilitado: http://localhost:8080/h2-console  
- **Swagger** habilitado: http://localhost:8080/swagger-ui.html  

### Produção (prod – PostgreSQL)

Variáveis de ambiente (recomendado em produção):

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/coupondb`
- `SPRING_DATASOURCE_USERNAME=...`
- `SPRING_DATASOURCE_PASSWORD=...`

Exemplo local com Postgres na porta 5432:

```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/coupondb
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
./mvnw spring-boot:run
```

- Schema gerenciado pelo Hibernate (`ddl-auto: update`).
- Console H2 e Swagger desligados por padrão (podem ser reativados via `SPRINGDOC_*` e `spring.h2.console.enabled`).
- Pool Hikari configurado (tamanho, timeouts) para maior robustez.

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

### Build da imagem

```bash
docker build -t coupon-api .
```

### Execução com Docker Compose (ambiente prod + PostgreSQL)

Sobe a API e um container PostgreSQL; a API usa o perfil **prod** e conecta no Postgres.

```bash
docker compose up --build
```

- **API**: http://localhost:8080  
- **PostgreSQL**: porta 5432 (usuário/senha: `postgres`, banco: `coupondb`)

O healthcheck do Postgres garante que a API só inicia após o banco estar pronto.

## Decisões técnicas

- **Estrutura**: controller → service → domain + repository; DTOs separados da entidade JPA.
- **Domínio**: regras em `Coupon.create()` e `Coupon.delete()`; validações de expiração, desconto e sanitização do código no domínio.
- **Exceções**: `BusinessException` com status HTTP configurável; `GlobalExceptionHandler` (`@RestControllerAdvice`) para respostas padronizadas e Bean Validation (erros de validação retornam mapa de campo → mensagem).
- **Soft delete**: campo `deleted` na entidade; `@SQLRestriction("deleted = false")` (Hibernate) para não retornar deletados nas buscas; delete por id verifica “já deletado” via query nativa antes de chamar o domínio.
- **Stack**: Spring Boot 4.0, Java 17, H2 (dev), PostgreSQL (prod), JPA, SpringDoc (Swagger), Bean Validation.
- **Ambientes**: perfil `dev` (H2, console e Swagger ativos) e `prod` (PostgreSQL, variáveis de ambiente, pool Hikari, Swagger/H2 desabilitados por padrão).
