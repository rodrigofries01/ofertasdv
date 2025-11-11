# Multi-stage build para Spring Boot (Java 21)

# Etapa de build com Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Baixa dependências em cache
COPY pom.xml ./
RUN mvn -B -q -DskipTests dependency:go-offline

# Copia código e empacota
COPY src ./src
RUN mvn -B -DskipTests clean package

# Etapa de runtime com JRE leve
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o fat-jar da etapa de build
COPY --from=build /build/target/ofertasdv-*.jar /app/app.jar

# Variáveis de ambiente padrão (podem ser sobrescritas em runtime)
ENV JAVA_OPTS=""
ENV DB_URL="jdbc:postgresql://localhost:5432/ofertasdb"
ENV DB_USERNAME="postgres"
ENV DB_PASSWORD="postgres"
ENV DB_ADMIN_URL="jdbc:postgresql://localhost:5432/postgres"
ENV DB_ADMIN_USERNAME="postgres"
ENV DB_ADMIN_PASSWORD="postgres"
ENV JPA_DDL_AUTO="update"

EXPOSE 8080

# Usa variáveis para configurar o datasource por application.properties
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]

