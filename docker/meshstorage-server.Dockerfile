# Etapa 1: Build da aplicação
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copia o projeto do server
COPY ../meshstorage-server /app/meshstorage-server

# Copia o projeto common (assumindo que está na mesma raiz)
COPY ../meshstorage-common /app/meshstorage-common

# Instala o módulo common no repositório local dentro do container
RUN cd /app/meshstorage-common && mvn clean install -DskipTests

# Agora compila o projeto server
RUN cd /app/meshstorage-server && mvn clean package -DskipTests

# Etapa 2: Criação da imagem final
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copia o JAR do build anterior
COPY --from=build /app/meshstorage-server/target/meshstorage-server-1.0.0.jar app.jar

EXPOSE 3001

# (Opcional) Healthcheck via Actuator
# Ajuste o path se seu Actuator estiver em outro endpoint ou desabilite esta seção
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://127.0.0.1:3001/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
