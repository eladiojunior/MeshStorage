class Upload extends HTMLElement {
    static get observedAttributes() {
        return [
            'app-code',
            'api-url-upload',
            'api-token-jwt', //opcional
            'accept',
            'max-file-size'
        ];
    }
    private root: ShadowRoot;
    private input!: HTMLInputElement;
    private startBtn!: HTMLButtonElement;
    private cancelBtn!: HTMLButtonElement;
    private progress!: HTMLProgressElement;
    private statusEl!: HTMLDivElement;
    private fileNameLbl!: HTMLLabelElement;
    private file: File | null = null;
    private abortCtrl: AbortController | null = null;
    private uploadId: string | null = null;
    private maxFileSizeBytes = 10 * 1024 * 1024; // 10MB

    private CHUNK_SIZE: number = 1024 * 100;                    // 100 KB por bloco de envio

    constructor() {
        super();
        this.root = this.attachShadow({mode: 'open'});
        this.root.innerHTML = `
        <style>
            .upload-card { border:1px solid #ddd; border-radius:12px; padding:16px; box-shadow:0 2px 6px rgba(0,0,0,.04); }
            .upload-row { display:flex; gap:8px; align-items:center; flex-wrap:wrap; }
            .upload-grow { flex:1; min-width:250px; }
            input[type=file].upload { display: none; }
            button.upload {
                border:none; border-radius:10px; padding:10px 14px; cursor:pointer;
                background:#1c65a2; color:#fff;
            }
            button.upload.secondary { background:#6b7280; }
            button.upload:disabled { opacity:.6; cursor:not-allowed; }
            progress.upload { width:100%; height:20px; }
            .upload-status { font-size:.9rem; color:#374151; margin-top:8px; min-height:1.2em; }
            .upload-error { color:#cc0303; }
        </style>
      <div class="upload-card" role="group" aria-label="Uploader de arquivos MeshStorage">
        <div class="upload-row">
          <div class="upload-grow">
            <input id="file-upload" type="file" class="upload">
            <span id="file-name-upload" class="upload-file-name" 
                aria-describedby="file-upload">Nenhum arquivo selecionado</span>
          </div>
          <button class="upload choose">Selecionar</button>
          <button class="upload start">Enviar</button>
          <button class="upload cancel secondary">Cancelar</button>
        </div>
        <div class="upload-row" style="margin-top:12px">
          <progress class="upload" max="100" value="0" aria-label="Progresso do upload"></progress>
        </div>
        <div class="upload-status" aria-live="polite"></div>
      </div>
    `;
    }

    connectedCallback() {
        this.input = <HTMLInputElement>this.root.querySelector('#file-upload')!;
        this.fileNameLbl = <HTMLLabelElement>this.root.querySelector('#file-name-upload')!;
        this.startBtn = <HTMLButtonElement>this.root.querySelector('button.upload.start')!;
        this.cancelBtn = <HTMLButtonElement>this.root.querySelector('button.upload.cancel')!;
        const chooseBtn = <HTMLButtonElement>this.root.querySelector('button.upload.choose')!;
        this.progress = <HTMLProgressElement>this.root.querySelector('progress.upload')!;
        this.statusEl = <HTMLDivElement>this.root.querySelector('.upload-status')!;

        const accept = this.getAttribute('accept');
        if (accept) this.input.accept = accept;
        const maxFileSize = this.getAttribute('max-file-size');
        if (maxFileSize) this.maxFileSizeBytes = parseInt(maxFileSize, 10) || this.maxFileSizeBytes;

        this.startBtn.disabled = true;
        this.cancelBtn.disabled = true;

        chooseBtn.addEventListener('click', () => this.input.click());
        this.input.addEventListener('change', () => {
            this.file = this.input.files && this.input.files[0] ? this.input.files[0] : null;
            if (!this.file) {
                this.fileNameLbl.textContent = `Selecione um arquivo`;
                this.setStatus('Nenhum arquivo selecionado.', true);
                this.startBtn.disabled = true;
                return;
            }
            if (this.file.size > this.maxFileSizeBytes) {
                this.setStatus(`Arquivo excede o limite de <strong>${this.bytesToSizeXB(this.maxFileSizeBytes)}</strong>.`, true);
                this.startBtn.disabled = true;
                return;
            }
            if (accept && !this.matchAccept(this.file, accept)) {
                this.setStatus(`Tipo de arquivo não permitido <strong>${this.file.type || this.file.name}</strong>.`, true);
                this.startBtn.disabled = true;
                return;
            }
            this.fileNameLbl.textContent = `Selecionado: ${this.file.name}`;
            this.setStatus(`Pronto para envio, tamanho do arquivo: <strong>${this.bytesToSizeXB(this.file.size)}</strong>.`);
            this.startBtn.disabled = false;
        });

        this.startBtn.addEventListener('click', () => this.startUpload().catch(e => this.fail(e)));
        this.cancelBtn.addEventListener('click', () => this.cancelUpload());
    }

    private getAttr(name: string, fallback: string) {
        return this.getAttribute(name) ?? fallback;
    }

    private setStatus(msg: string, error: boolean = false) {
        this.statusEl.classList.remove("upload-error");
        if (error)
            this.statusEl.classList.add("upload-error");
        this.statusEl.innerHTML = msg;
    }

    private dispatch(name: string, detail: any) {
        this.dispatchEvent(new CustomEvent(name, {detail, bubbles: true, composed: true}));
    }

    private matchAccept(file: File, accept: string): boolean {
        const list = accept.split(',').map(s => s.trim().toLowerCase()).filter(Boolean);
        if (list.length === 0) return true;
        const ext = '.' + (file.name.split('.').pop() || '').toLowerCase();
        const type = (file.type || '').toLowerCase();
        return list.some(a => a === ext || a === type || (a.endsWith('/*') && type.startsWith(a.slice(0, -1))));
    }

    private bytesToSizeXB(bytes: number) {
        const u = ['B', 'KB', 'MB', 'GB'];
        let i = 0;
        let n = bytes;
        while (n >= 1024 && i < u.length - 1) {
            n /= 1024;
            i++;
        }
        return `${n.toFixed((i === 0) ? 0 : 1)} ${u[i]}`;
    }

    private async startUpload() {
        if (!this.file) return;
        this.startBtn.disabled = true;
        this.cancelBtn.disabled = false;
        this.progress.value = 0;
        this.setStatus('Iniciando upload...');
        this.abortCtrl = new AbortController();

        const appCode = this.reqAttr('app-code');
        const apiUploadInChunk = this.reqAttr('api-url-upload');
        const tokenJwtAuth = this.getAttribute('api-token-jwt');

        const endpointInit      = apiUploadInChunk + '/file/uploadInChunk/init';
        const endpointChunk     = apiUploadInChunk + '/file/uploadInChunk/chunk';
        const endpointFinalize  = apiUploadInChunk + '/file/uploadInChunk/finalize';

        const initPayload = {
            applicationCode: appCode,
            fileName: this.file.name,
            contentType: this.file.type || 'application/octet-stream',
            fileSize: this.file.size,
            checksumSha256: ''
        };

        const headersInitFin: Record<string, string> = {
            'Content-Type': 'application/json',
            ...(tokenJwtAuth?.trim()
            ? { Authorization: `Bearer ${tokenJwtAuth?.trim()}` } : {})
        };
        const headersChunk: Record<string, string> = {
            ...(tokenJwtAuth?.trim()
                ? { Authorization: `Bearer ${tokenJwtAuth?.trim()}` } : {})
        };

        const initResp = await fetch(endpointInit, {
            method: 'POST',
            headers: headersInitFin,
            body: JSON.stringify(initPayload),
            signal: this.abortCtrl.signal
        });
        if (!initResp.ok) {
            if (initResp.status === 401)
                throw new Error(`Falha na autorização do Token: ${initResp.status}`);
            if (initResp.status === 400) {
                const respError = await initResp.json();
                throw new Error(`[${respError.codeError}] - ${respError.messageError}`);
            }
            throw new Error(`Falha no init do upload em bloco: ${initResp.status}`);
        }
        const { uploadId, chunkSize } = await initResp.json();
        this.uploadId = uploadId;
        const cs = Number(chunkSize) || this.CHUNK_SIZE;
        this.dispatch('upload-start', {fileName: this.file.name, size: this.file.size, cs});

        let sent = 0;
        for (let index = 0; index < chunkSize; index++) {
            if (!this.abortCtrl)
                throw new Error('Upload cancelado');

            const start = index * chunkSize;
            const end = Math.min(start + cs, this.file.size);
            const blob = this.file.slice(start, end);

            const form = new FormData();
            form.append('uploadId', this.uploadId!);
            form.append('index', String(index));
            form.append('total', String(cs));
            form.append('chunk', blob, this.file.name);

            const chunkResp = await fetch(endpointChunk, {
                method: 'PUT',
                headers: headersChunk,
                body: form,
                signal: this.abortCtrl.signal
            });
            if (!chunkResp.ok) {
                if (chunkResp.status === 401)
                    throw new Error(`Falha na autorização do Token: ${initResp.status}`);
                if (chunkResp.status === 400) {
                    const respError = await chunkResp.json();
                    throw new Error(`[${respError.codeError}] - ${respError.messageError}`);
                }
                throw new Error(`Falha no envio do bloco [${index}] de upload: ${chunkResp.status}`);
            }
            sent += (end - start);
            const percent = Math.floor((sent / this.file.size) * 100);
            this.progress.value = percent;
            this.setStatus(`Enviado ${percent}% (${this.bytesToSizeXB(sent)} de ${this.bytesToSizeXB(this.file.size)})`);
            this.dispatch('upload-progress', {loadedBytes: sent, totalBytes: this.file.size, percent});
        }
        const finResp = await fetch(endpointFinalize, {
            method: 'POST',
            headers: headersInitFin,
            body: JSON.stringify({uploadId: this.uploadId}),
            signal: this.abortCtrl.signal
        });
        if (!finResp.ok) {
            if (finResp.status === 401)
                throw new Error(`Falha na autorização do Token: ${initResp.status}`);
            if (finResp.status === 400) {
                const respError = await finResp.json();
                throw new Error(`[${respError.codeError}] - ${respError.messageError}`);
            }
            throw new Error(`Falha ao finalizar upload em bloco: ${finResp.status}`);
        }
        const finData = await finResp.json();

        this.progress.value = 100;
        this.setStatus('Upload concluído com sucesso.');
        this.cancelBtn.disabled = true;
        this.dispatch('upload-complete', finData);
        this.abortCtrl = null;
        this.uploadId = null;
        this.startBtn.disabled = true;
    }

    private cancelUpload() {
        if (this.abortCtrl) {
            this.abortCtrl.abort();
            this.abortCtrl = null;
            this.setStatus('Upload cancelado.');
            this.dispatch('upload-cancel', {message: 'cancelled'});
        }
        this.startBtn.disabled = false;
        this.cancelBtn.disabled = true;
    }

    private reqAttr(name: string) {
        const v = this.getAttribute(name);
        if (!v) throw new Error(`Atributo obrigatório ausente: ${name}`);
        return v;
    }

    private fail(e: unknown) {
        const msg = e instanceof Error ? e.message : String(e);
        this.setStatus(`Erro: ${msg}`, true);
        this.dispatch('upload-error', {message: msg});
        this.startBtn.disabled = false;
        this.cancelBtn.disabled = true;
    }

}
customElements.define('meshstorage-upload', Upload);
export {};