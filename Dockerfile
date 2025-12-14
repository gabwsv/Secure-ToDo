# ETAPA 1: Build (Compilação)
FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app

# Copia os arquivos de dependência e código fonte
COPY pom.xml .
COPY src ./src

# Compila o projeto
RUN mvn clean package -DskipTests

# ----------------------------------------------------------------

# ETAPA 2: Runtime (Execução)
FROM amazoncorretto:21-alpine
WORKDIR /app

# Cria a pasta de uploads
RUN mkdir -p uploads

# Copia o .jar gerado na etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Define a porta
EXPOSE 8080

# Comando para iniciar
ENTRYPOINT ["java", "-jar", "app.jar"]