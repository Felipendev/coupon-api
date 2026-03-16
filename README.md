# Coupon API

API REST para cadastro e exclusão lógica de cupons, com regras de negócio encapsuladas no domínio (DDD), tratamento de exceções padronizado e documentação OpenAPI (Swagger).

## Requisitos

- Java 17+
- Maven 3.8+ (ou use o wrapper: `./mvnw`)

## Execução local

```bash
./mvnw spring-boot:run
```

Ou, com Maven instalado:

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

- **Swagger UI**: http://localhost:8080/swagger-ui.html  
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs  

Banco em memória H2; console H2 em desenvolvimento: http://localhost:8080/h2-console (habilitado em `application.yaml`).

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

## Docker

Build da imagem:

```bash
./mvnw package -DskipTests
docker build -t coupon-api .
```

Execução com Docker Compose:

```bash
docker compose up --build
```

A aplicação fica disponível em `http://localhost:8080` (porta mapeada no `docker-compose.yml`).

## Decisões técnicas

- **Estrutura**: controller → service → domain + repository; DTOs separados da entidade JPA.
- **Domínio**: regras em `Coupon.create()` e `Coupon.delete()`; validações de expiração, desconto e sanitização do código no domínio.
- **Exceções**: `BusinessException` com status HTTP configurável; `GlobalExceptionHandler` (`@RestControllerAdvice`) para respostas padronizadas e Bean Validation (erros de validação retornam mapa de campo → mensagem).
- **Soft delete**: campo `deleted` na entidade; `@SQLRestriction("deleted = false")` (Hibernate) para não retornar deletados nas buscas; delete por id verifica “já deletado” via query nativa antes de chamar o domínio.
- **Stack**: Spring Boot 4.0, Java 17, H2, JPA, SpringDoc (Swagger), Bean Validation.
