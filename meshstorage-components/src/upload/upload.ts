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
    private inputUpload!: HTMLInputElement;
    private buttonStartUpload!: HTMLButtonElement;
    private buttonCancelUpload!: HTMLButtonElement;
    private progress!: HTMLProgressElement;
    private divStatusUpload!: HTMLDivElement;
    private labelFileName!: HTMLLabelElement;
    private file: File | null = null;
    private abortCtrl: AbortController | null = null;
    private uploadId: string | null = null;
    private maxFileSizeBytes = 10 * 1024 * 1024; // 10MB
    private appCode: string | null = null;
    private apiBase: string | null = null;
    private tokenAuth: string | null = null;

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
            button.upload { border:none; border-radius:10px; padding:10px 14px; cursor:pointer; background:#1c65a2; color:#fff;}
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
                    <span id="file-name-upload" class="upload-file-name" aria-describedby="file-upload">
                        Nenhum arquivo selecionado
                    </span>
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

        //Carregar os elementros da interface...
        this.inputUpload = <HTMLInputElement>this.root.querySelector('#file-upload')!;
        this.labelFileName = <HTMLLabelElement>this.root.querySelector('#file-name-upload')!;
        this.buttonStartUpload = <HTMLButtonElement>this.root.querySelector('button.upload.start')!;
        this.buttonCancelUpload = <HTMLButtonElement>this.root.querySelector('button.upload.cancel')!;
        this.progress = <HTMLProgressElement>this.root.querySelector('progress.upload')!;
        this.divStatusUpload = <HTMLDivElement>this.root.querySelector('.upload-status')!;
        const buttonChooseUpload = <HTMLButtonElement>this.root.querySelector('button.upload.choose')!;

        //Recuperar os atributos informados no componente do frontend.
        this.appCode = this.requiredAttribute('app-code');
        this.apiBase = this.requiredAttribute('api-url-upload');
        this.tokenAuth = this.getAttribute('api-token-jwt');

        //Recuperar os tipos de arquivos permitidos...
        const accept = this.requiredAttribute('accept');
        if (accept) this.inputUpload.accept = accept;
        //Recuperar o tamanho (em bytes) permitido para o arquivo upload...
        const maxFileSize = this.getAttribute('max-file-size');
        if (maxFileSize)
            this.maxFileSizeBytes = parseInt(maxFileSize, 10) || this.maxFileSizeBytes;

        //Apresentar as opções de seleção.
        const rulesUpload = `Arquivo tipos (<strong>${accept}</strong>) e no máximo <strong>${this.bytesToSizeXB(this.maxFileSizeBytes)}</strong>.`
        this.setStatus(rulesUpload);

        //Adicionar ação de selecionar arquivo para Upload ao clicar no buttonChoose
        buttonChooseUpload.addEventListener('click', () => this.inputUpload.click());

        //Desativar os botoes de iniciar e cancelar Upload...
        this.buttonStartUpload.disabled = true;
        this.buttonCancelUpload.disabled = true;

        //Adicionar ação de verificar o arquivo sempre que o input File for alterado...
        this.inputUpload.addEventListener('change', () => {
            this.file = this.inputUpload.files && this.inputUpload.files[0] ? this.inputUpload.files[0] : null;
            if (!this.file) {
                this.labelFileName.textContent = `Selecione um arquivo`;
                this.setStatus('Nenhum arquivo selecionado.', true);
                this.buttonStartUpload.disabled = true;
                return;
            }
            if (this.file.size > this.maxFileSizeBytes) {
                this.setStatus(`Arquivo excede o limite de <strong>${this.bytesToSizeXB(this.maxFileSizeBytes)}</strong>.`, true);
                this.buttonStartUpload.disabled = true;
                return;
            }
            if (accept && !this.matchAccept(this.file, accept)) {
                this.setStatus(`Tipo de arquivo não permitido <strong>${this.file.type || this.file.name}</strong>.`, true);
                this.buttonStartUpload.disabled = true;
                return;
            }
            this.labelFileName.textContent = `Selecionado: ${this.file.name}`;
            this.setStatus(`Pronto para envio, tamanho do arquivo: <strong>${this.bytesToSizeXB(this.file.size)}</strong>.`);
            this.buttonStartUpload.disabled = false;
        });

        //Adicinar ação nos buttons Start e Cancel de Upload...
        this.buttonStartUpload.addEventListener('click', () =>
            this.startUpload().catch(e => this.fail(e)));
        this.buttonCancelUpload.addEventListener('click', () =>
            this.cancelUpload());
    }

    private setStatus(msg: string, error: boolean = false) {
        this.divStatusUpload.classList.remove("upload-error");
        if (error)
            this.divStatusUpload.classList.add("upload-error");
        this.divStatusUpload.innerHTML = msg;
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

    private authHeaders(additional?: Record<string,string>) {
        const h: Record<string,string> = { ...(additional ?? {}) };
        if (this.tokenAuth && this.tokenAuth.trim()) {
            h['Authorization'] = this.tokenAuth
        }
        return h;
    }
    private async checkRespose(response: Response) {
        if (response.ok)
            return;
        if (response.status === 401)
            throw new Error(`Falha na autorização do Token: ${response.status}`);
        if (response.status === 400) {
            const respError = await response.json();
            throw new Error(`[${respError.codeError}] - ${respError.messageError}`);
        }
        throw new Error(`Falha no upload: ${response.status}`);
    }

    private async startUpload() {
        if (!this.file)
            return;

        this.buttonStartUpload.disabled = true;
        this.buttonCancelUpload.disabled = false;
        this.progress.value = 0;
        this.setStatus('Iniciando upload...');
        this.abortCtrl = new AbortController();

        //Iniciar upload...
        const endpointInit = `${this.apiBase}/file/uploadInChunk/init`;
        const initPayload = {
            applicationCode: this.appCode,
            fileName: this.file.name,
            contentType: this.file.type || 'application/octet-stream',
            fileSize: this.file.size,
            checksumSha256: ''
        };
        const initResp = await fetch(endpointInit, {
            method: 'POST',
            headers: this.authHeaders({ 'Content-Type': 'application/json' }),
            body: JSON.stringify(initPayload),
            signal: this.abortCtrl.signal
        });
        await this.checkRespose(initResp);
        const { uploadId, chunkSize } = await initResp.json();
        this.uploadId = uploadId;
        const cs = Number(chunkSize) || this.CHUNK_SIZE;
        this.dispatch('upload-start', {fileName: this.file.name, size: this.file.size, cs});

        //Enviar blocos do arquivo de upload...
        const endpointChunk = `${this.apiBase}/file/uploadInChunk/chunk`;
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
                headers: this.authHeaders(),
                body: form,
                signal: this.abortCtrl.signal
            });
            await this.checkRespose(chunkResp);
            sent += (end - start);
            const percent = Math.floor((sent / this.file.size) * 100);
            this.progress.value = percent;
            this.setStatus(`Enviado ${percent}% (${this.bytesToSizeXB(sent)} de ${this.bytesToSizeXB(this.file.size)})`);
            this.dispatch('upload-progress', {loadedBytes: sent, totalBytes: this.file.size, percent});
        }

        //Finalizar Upload...
        const endpointFinalize  = `${this.apiBase}/file/uploadInChunk/finalize`;
        const finResp = await fetch(endpointFinalize, {
            method: 'POST',
            headers: this.authHeaders({ 'Content-Type': 'application/json' }),
            body: JSON.stringify({uploadId: this.uploadId}),
            signal: this.abortCtrl.signal
        });
        await this.checkRespose(finResp);
        const finData = await finResp.json();
        this.progress.value = 100;
        this.setStatus('Upload concluído com sucesso.');
        this.buttonCancelUpload.disabled = true;
        this.dispatch('upload-complete', finData);
        this.abortCtrl = null;
        this.uploadId = null;
        this.buttonStartUpload.disabled = true;
    }

    private cancelUpload() {
        if (this.abortCtrl) {
            const endpointCancel  = `${this.apiBase}/file/uploadInChunk/cancel`;
            if (this.uploadId) {
                // melhor esforço (não espere)
                fetch(`${endpointCancel}/${encodeURIComponent(this.uploadId)}`, {
                    method: 'DELETE',
                    headers: this.authHeaders()
                }).catch(() => {});
            }
            this.abortCtrl.abort();
            this.abortCtrl = null;
            this.setStatus('Upload cancelado.');
            this.dispatch('upload-cancel', {message: 'cancelled'});
        }
        this.buttonStartUpload.disabled = false;
        this.buttonCancelUpload.disabled = true;
    }

    private requiredAttribute(name: string) {
        const v = this.getAttribute(name);
        if (!v) throw new Error(`Atributo obrigatório ausente: ${name}`);
        return v;
    }

    private fail(e: unknown) {
        const msg = e instanceof Error ? e.message : String(e);
        this.setStatus(`Erro: ${msg}`, true);
        this.dispatch('upload-error', {message: msg});
        this.buttonStartUpload.disabled = false;
        this.buttonCancelUpload.disabled = true;
    }

}
customElements.define('meshstorage-upload', Upload);
export {};