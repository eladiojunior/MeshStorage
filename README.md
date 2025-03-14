# MeshStorage

MeshStorage é um sistema distribuído inteligente para armazenamento de arquivos, utilizando uma malha de file servers interconectados. 
O servidor central gerencia a disponibilidade e capacidade de cada file server, distribuindo os arquivos de maneira eficiente para otimizar o
uso de espaço e garantir alta disponibilidade.

## 🚀 Visão Geral

O MeshStorage consiste em:
- **MeshStorage Server**: O servidor central que gerencia os file servers, monitora disponibilidade e decide onde armazenar arquivos.
- **MeshStorage Clinet**: Um cliente (agente) instalado em cada file server, que reporta status ao servidor e recebe comandos de armazenamento.
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

### 🔹 **Passo 2: Iniciar o Servidor**
```sh
$ mvn spring-boot:run
```
O servidor inicia na porta `8080`.

### 🔹 **Passo 3: Iniciar os Clientes (Agents)**
Nos file servers, execute:
```sh
$ java -jar meshstorage-client.jar --server.url=http://server-ip:8080
```

## 🌐 Endpoints Principais
### 🔹 REST API (Administração)
| Método  | Endpoint                      | Descrição                     |
|---------|--------------------------------|--------------------------------|
| `POST`  | `/fileserver/register`        | Registra um novo file server  |
| `GET`   | `/fileserver/status`          | Lista todos os servidores     |
| `POST`  | `/fileserver/update`          | Atualiza status de um agent   |

### 🔹 WebSocket (Comunicação em Tempo Real)
- **Conectar:** `ws://server-ip:8080/fileserver`
- **Mensagens suportadas:**
    - `status-update` → Enviado pelos agentes para reportar espaço livre.
    - `store-file` → Enviado pelo servidor para designar um file server para armazenamento.

## 📜 Licença
Este projeto é licenciado sob a **MIT License**.

## ✨ Contato
📧 Email: eladiojunior@gmail.com (Aceito PIX)