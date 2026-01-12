# meshstorage-components

Projeto com os componentes construídos para utilização da solução de Meshstorage com maior eficiência possível, aqui
irá encontrar Web Components (`<meshstorage-upload>` e `<meshstorage-upload-multiple>`) que simplificará a integração 
com a solução dentro do frontend da sua aplicação, pronto para uso em qualquer aplicação (React, Vue, Angular ou HTML 
puro).

### Componentes

## `<meshstorage-upload>`
Componente para integração com a solução Meshstorage que implementa o FileUpload com estrutura de envio de arquivos
particinados com configuração do tamanho máximo, tipos de extensões aceitas, progresso e checagem de completude do 
processo de upload do arquivo no frontend.

### Regras no cliente 
O componente valida tamanho máximo do arquivo (max-file-size) e tipos permitidos (accept) no cliente e também 
revalidado no servidor.

### Eventos
- upload-start → { fileName, size, totalChunks }
- upload-progress → { loadedBytes, totalBytes, percent }
- upload-complete → { fileId, status }
- upload-error → { message }

## `<meshstorage-upload-multiple>`
Componente para integração com a solução Meshstorage que implementa o FileUpload com estrutura de envio de vários 
arquivos concorrentes de forma particinada (em blocos) com configuração do tamanho máximo, tipos de extensões aceitas, 
progresso e checagem de completude de cada arquivo no frontend.

### Regras no cliente
O componente valida limite de tamanho (max-file-size) por arquivo, tamanho total dos arquivos (max-total-files-size), 
quantidade de arquivos permitidos (max-file-count), tipos de arquivos permitidos (accept) e concorrência de envio 
(max-concurrent-upload). Tudo isso verificado no cliente e também revalidado no servidor.

### Eventos
- upload-file-complete  → { fileName, uploadId }
- upload-file-error → { fileName, error }
- upload-file-rejected  → { fileName, reason }
- upload-file-progress  → {fileName, bytesSent, totalBytes, percent}
- upload-file-cancelled → { fileName: t.file.name, uploadId: t.uploadId }
- upload-file-start →  {fileName: task.file.name, uploadId: task.uploadId, chunkSize: chunkSize}
- upload-all-complete → [não envia nenhum objeto]

## Endpoints esperados (exemplo)
- POST /api/v1/upload/init → { uploadId }
- POST /api/v1/upload/chunk (multipart: uploadId, index, total, chunk[blob])
- POST /api/v1/upload/finalize → { fileId, status }

## Scripts

```sh
npm install
npm run dev      # ambiente local com hot-reload (abre index.html)
npm run build    # gera dist/meshstorage-upload.js (library)
npm run preview  # serve build para teste
```