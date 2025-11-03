# meshstorage-components

Projeto com os componentes construídos para utilização da solução de Meshstorage com maior eficiência possível, aqui
irá encontrar Web Components (`<meshstorage-upload>`) que simplificará a integração com a solução dentro do frontend
da sua aplicação, pronto para uso em qualquer aplicação (React, Vue, Angular ou HTML puro).

### Componentes

## `<meshstorage-upload>`
WebComponent para integração com a solução MesgStorage que implementa o FileUpload com estrutura de envio de arquivos
particinados com configuração do tamanho máximo, tipos de extensões aceitas, progresso e checagem de completude do 
processo de upload do arquivno no frontend.

### Regras no cliente 
O componente valida limite de tamanho (max-bytes) e tipos (accept) no cliente, mas revalide no servidor.

### Eventos
- uploadstart → { fileName, size, totalChunks }
- uploadprogress → { loadedBytes, totalBytes, percent }
- uploadcomplete → { fileId, status }
- uploaderror → { message }

### Endpoints esperados (exemplo)
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