# Etapa 1: Build da aplicação
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copia o projeto do client
COPY meshstorage-client /app/meshstorage-client

# Copia o projeto common (assumindo que está na mesma raiz)
COPY meshstorage-common /app/meshstorage-common

# Instala o módulo common no repositório local dentro do container
RUN cd /app/meshstorage-common && mvn clean install -DskipTests

# Agora compila o projeto client/agent
RUN cd /app/meshstorage-client && mvn clean package -DskipTests

# Etapa 2: Criação da imagem final
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copia o JAR do build anterior
COPY --from=build /app/meshstorage-client/target/meshstorage-client-1.0.0.jar app.jar

VOLUME ["/storage"]

ENV SERVER_NAME=fileserver-docker
ENV STORAGE_NAME=storage-docker
ENV STORAGE_PATH=/storage
ENV URL_WEBSOCKET_SERVER=ws://0.0.0.0:3001/server-storage-websocket

ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar \
  -url-websocket-server $URL_WEBSOCKET_SERVER \
  -server-name $SERVER_NAME \
  -storage-name $STORAGE_NAME \
  -storage-path $STORAGE_PATH"]