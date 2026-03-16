# Stage 1: build
FROM eclipse-temurin:17 AS builder
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x mvnw && ./mvnw package -DskipTests -B

# Stage 2: run
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=builder /build/target/coupon-api-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
