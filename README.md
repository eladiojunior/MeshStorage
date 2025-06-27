# MeshStorage

MeshStorage é um sistema distribuído inteligente para armazenamento de arquivos, utilizando uma malha de file servers interconectados. 
O servidor central gerencia a disponibilidade e capacidade de cada file server, distribuindo os arquivos de maneira eficiente para otimizar o
uso de espaço e garantir alta disponibilidade.

## 🚀 Visão Geral

O MeshStorage consiste em:
- **MeshStorage Server**: O servidor central que gerencia os file servers, monitora disponibilidade e decide onde armazenar arquivos.
- **MeshStorage Client**: Um cliente (agente) instalado em cada file server, que reporta status ao servidor e recebe comandos de armazenamento.
- **MeshStorage Interface**: Apresentação de um dashboard com informações de storages (armazenamento, clients) total de armazenamento disponível e utilizado, aplicações registradas e quantidade de arquivos registrados.
- **Comunicação em Tempo Real**: Utiliza WebSockets para interação de baixa latência e REST API para operações administrativas.

## 🎯 Recursos Principais

✅ Distribuição automática de arquivos entre file servers.
✅ Monitoramento de espaço livre e disponibilidade dos file servers.
✅ Balanceamento dinâmico baseado em capacidade de armazenamento.
✅ Comunicação híbrida (WebSockets + REST API).
✅ Alta disponibilidade e escalabilidade.

## 🏗️ Arquitetura

```plaintext
+---------------------+       +---------------------+
|  MeshStorage Server | <---> | MeshStorage Clients |
+---------------------+       +---------------------+
                                    |                     +---------------+
                                    +-------------------> | File Server 1 | 
                                    |                     +---------------+
                                    +-------------------> | File Server 2 |
                                    |                     +---------------+
                                    +-------------------> | File Server N |
                                                          +---------------+
```

## 📦 Instalação e Configuração

### 🔹 **Requisitos**
- Java 17+
- Spring Boot 3+
- Banco de dados (H2/PostgreSQL)
- WebSockets e REST API habilitados

### 🔹 **Passo 1: Clonar o repositório**
```sh
$ git clone https://github.com/eladiojunior/MeshStorage.git
$ cd MeshStorage
```

### 🔹 **Passo 2: Iniciar o Servidor (backend)**
```sh
$ mvn spring-boot:run
```
O servidor inicia na porta `3001`.

### 🔹 **Passo 3: Iniciar os Clientes (Agents)**
Nos file servers, execute:
```sh
$ java -jar meshstorage-client.jar -url-websocket-server=ws://localhost:3001/server-storage-websocket -server-name=HOSTNAME -storage-name=STORAGE_X -storage-path=\storage\xpto
```
### 🔹 **Passo 4: Iniciar o Dashboard (frontend)**
```sh
$ mvn spring-boot:run
```

## 🌐 Endpoints Principais
### 🔹 REST API 
#### Swagger: http://localhost:3001/swagger-ui/index.html

| Método    | Endpoint                     | Descrição                                                                                 |
|-----------|------------------------------|-------------------------------------------------------------------------------------------|
| `GET`     | `api/system/status`          | Verifica o status (saúde) e informações quantitativas do MeshStorage como um todo.        |
| `POST`    | `api/app/register`           | Registrar uma aplicação que irá utilizar o servidor de armazenamento de arquivos físicos. |
| `PUT`     | `api/app/update/{id}`        | Atualizar uma aplicação, pelo ID, para armazenamento de arquivos físicos.                 |
| `GET`     | `api/app/list`               | Lista todas as aplicações para armazenamento de arquivos físicos.                         |
| `DELETE`  | `api/app/remove/{id}`        | Remover (logicamente) uma aplicação do processo de armazemanto de arquivos físicos.       |
| `POST`    | `api/file/upload`            | Registrar um arquivo no ServerStorage.                                                    |
| `GET`     | `api/file/list`              | Lista os arquivos de uma aplicação (nome) de forma paginada.                              |
| `GET`     | `api/file/listStatusCode`    | Lista os codigos/descrições dos status arquivos do ServerSorage.                          |
| `GET`     | `api/file/download/{idFile}` | Baixa um arquivo do ServerStorage pelo identificador do arquivo (chave de acesso).        |
| `DELETE`  | `api/file/delete/{idFile}`   | Remover um arquivo do ServerStorage pelo identificador do arquivo (chave de acesso).      |
| `GET`     | `api/server/list`            | Lista todos os Server Storages para armazenamento de arquivos físicos.                    |
| `GET`     | `api/server/best`            | Obter o melhor Server Storage para armazenamento de arquivos físicos.                     |

### 🔹 WebSocket (Comunicação em Tempo Real)
- **Conectar:** `ws://localhost:3001/server-storage-websocket`
- **Mensagens suportadas:**
  - ***Servidor***
    - `FILE_REGISTER` → Enviado pelo servidor as informações do arquivo para armazenamento, transmissão fragmentada do conteúdo do arquivo.
    - `FILE_DELETE` → Enviado pelo servidor um identificador de arquivo para remoção.
    - `FILE_DOWNLOAD` → Enviado pelo servidor um identificador de arquivo para download.
  - ***Cliente (Agente)***
    - `status-file-storage` → Enviado pelo agente o resultado do envio do arquivo pelo servidor.
    - `download-file-storage` → Enviado pelo agente as informações do arquivo solicitado, transmissão fragmentada do conteúdo do arquivo.
    - `status-update-client` → Enviado pelo agente a situação do cliente de armazenamento, além de informações como espaço total e disponível em disco.

## 📜 Licença
Este projeto é licenciado sob a **MIT License**.

## ✨ Contato
📧 Email: eladiojunior@gmail.com (Aceito PIX)