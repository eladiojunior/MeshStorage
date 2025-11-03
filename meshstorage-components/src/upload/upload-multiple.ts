const sheet = new CSSStyleSheet();
sheet.replaceSync(`
    :root {
        ; ; ;
        ; ;
        --msu-radius:14px; --msu-pad:14px;
    }
    * { box-sizing: border-box; }
    .msu-container{ background:#fff; color:#151515; border:1px solid #e5e7eb;
    border-radius: 14px; padding:22px; max-width:880px; margin:24px auto; }
    .msu-header h2{ margin:0 0 4px; font-size:1.2rem; }
    .msu-sub{ margin:0; color:#6b7280; font-size:0.95rem; }
    .msu-actions{ display:flex; gap:12px; margin:16px 0; }
    .msu-btn{ border:1px solid #e5e7eb; background:#f9fafb; padding:8px 14px; border-radius:10px; cursor:pointer; }
    .msu-btn-primary{ background:#2563eb; color:#fff; border-color:transparent; }
    .msu-btn-ghost{ background:transparent; }
    .msu-btn:focus{ outline:2px solid #2563eb; outline-offset:2px; }
    .msu-drop{ border:2px dashed #e5e7eb; border-radius: 14px; padding:20px; text-align:center; }
    .msu-drop:focus{ outline:2px solid #2563eb; outline-offset:2px; }
    .msu-drop__icon{ font-size:26px; margin-bottom:6px; }
    .msu-muted{ color:#6b7280; }
    .msu-total{ display:flex; align-items:center; gap:10px; margin:16px 0; }
    .msu-total progress{ width:100%; height:14px; }
    .msu-list{ display:flex; flex-direction:column; gap:10px; }
    .msu-empty{ padding:16px; background:#f9fafb; border:1px dashed #e5e7eb; border-radius:10px; text-align:center; color:#6b7280;}
    .msu-item{ display:grid; grid-template-columns: 1fr 120px 100px 110px auto; gap:12px;
    align-items:center; border:1px solid #e5e7eb; border-radius:12px; padding:12px; }
    .msu-name{ overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
    .msu-meta{ color:#6b7280; font-size:0.9rem; }
    .msu-bar progress{ width:100%; height:10px; }
    .msu-status{ font-size:0.9rem; }
    .msu-status--ok{ color:#059669; }
    .msu-status--error{ color:#b91c1c; }
    .msu-status--running{ color:#b45309; }
    .msu-actions-row{ display:flex; gap:6px; justify-content:flex-end; }
    .msu-action{ border:1px solid #e5e7eb; padding:6px 10px; border-radius:8px; background:#fff; cursor:pointer; }
    .msu-action--danger{ border-color:#b91c1c; color:#b91c1c; }
    @media (max-width:720px){
      .msu-item{ grid-template-columns: 1fr; gap:8px; }
      .msu-actions-row{ justify-content:flex-start; }
    }
`);
type UploadTaskState = 'queued' | 'running' | 'complete' | 'error' | 'cancelled';
interface UploadTask {
    id: string;
    uploadId?: string;
    file: File;
    state: UploadTaskState;
    sentBytes: number;
    totalBytes: number;
    controller: AbortController;
    error?: string;
}
class UploadMultiple extends HTMLElement {

    static get observedAttributes(): string[] {
        return [
            'app-code', 'api-url-upload', 'api-token-jwt',
            'accept', 'max-total-files-size', 'max-file-size',
            'max-file-count', 'max-concurrent-upload'
        ];
    }

    // Constants padrão
    private MAX_TOTAL_FILES_SIZE: number = 50 * 1024 * 1024;    // 50MB Total máximo de arquivos em bytes
    private MAX_FILE_SIZE: number = 2 * 1024 * 1024;            // 2MB Tamanho de cada arquivo.
    private TYPE_ACCEPT: string = '.pdf';                       // .pdf Tipo padrão permitido
    private MAX_FILE_COUNT: number = 5;                         // 5 Arquivos no máximo
    private MAX_CONCURRENT_UPLOAD: number = 2;                  // 2 Arquivos enviado
    private CHUNK_SIZE: number = 1024 * 100;                    // 100 KB por bloco de envio

    private appCode: string | undefined;                        // Code Application para registrar arquivo
    private apiBase: string | undefined;                        // URL base da API de Upload
    private token?: string;                                     // TOKEN JWT de Authozation
    private accept: string = this.TYPE_ACCEPT;
    private maxTotalFilesSize: number = this.MAX_TOTAL_FILES_SIZE;
    private maxFileSize: number = this.MAX_TOTAL_FILES_SIZE;
    private maxFileCount: number = this.MAX_FILE_COUNT;
    private maxConcurrent: number = this.MAX_CONCURRENT_UPLOAD;

    private root: ShadowRoot;
    private input!: HTMLInputElement;
    private queue: UploadTask[] = [];
    private running = 0;

    constructor() {
        super();
        this.root = this.attachShadow({mode: 'open'});
        this.root.adoptedStyleSheets = [sheet];
        this.root.innerHTML = `
        <section class="msu-container" aria-labelledby="msu_title">
            <header class="msu-header">
                <h2 id="msu_title">Enviar arquivos</h2>
                <p class="msu-sub">Selecione ou arraste arquivos. Tipos permitidos e limites são validados antes do envio.</p>
            </header>
            <!-- ações top -->
            <div class="msu-actions">
                <label class="msu-btn msu-btn-primary" aria-label="Selecionar arquivos">
                    Selecionar arquivos
                    <input id="msu_input" type="file" hidden multiple />
                </label>
                <button id="msu_cancel_all" class="msu-btn msu-btn-ghost" type="button">Cancelar tudo</button>
            </div>
            <!-- área de drop -->
            <div id="msu_drop" class="msu-drop" tabindex="0" role="button" aria-label="Solte seus arquivos aqui">
                <div class="msu-drop__icon" aria-hidden="true">⬆️</div>
                <div class="msu-drop__text">
                    Arraste & solte aqui <span class="msu-muted">ou clique em “Selecionar arquivos”</span>
                </div>
            </div>
            <!-- total progress -->
            <div class="msu-total" aria-live="polite">
                <progress id="msu_total_bar" max="100" value="0" aria-label="Progresso total"></progress>
                <span id="msu_total_label">0%</span>
            </div>
            <!-- lista -->
            <div id="msu_list" class="msu-list" role="list" aria-live="polite" aria-busy="false">
                <div id="msu_empty" class="msu-empty">Nenhum arquivo selecionado ainda.</div>
            </div>
        </section>
    `;
    }

    connectedCallback() {

        this.appCode = this.reqAttr('app-code');
        this.apiBase = this.reqAttr('api-url-upload');
        this.token = this.getAttribute('api-token-jwt') ?? undefined;
        this.accept = this.getAttribute('accept') ?? this.TYPE_ACCEPT;
        this.maxTotalFilesSize = this.attrNum('max-total-files-size') ?? this.MAX_TOTAL_FILES_SIZE;
        this.maxFileSize = this.attrNum('max-file-size') ?? this.MAX_FILE_SIZE;
        this.maxFileCount = this.attrNum('max-file-count') ?? this.MAX_FILE_COUNT;
        this.maxConcurrent = this.attrNum('max-concurrent-upload') ?? this.MAX_CONCURRENT_UPLOAD;
        this.input = <HTMLInputElement>this.root.querySelector('input[type=file]')!;
        this.input.addEventListener('change', () => this.onFilesSelected());
        if (this.accept) this.input.accept = this.accept;
        const cancelAllBtn = <HTMLButtonElement>this.root.querySelector('#msu_cancel_all');
        cancelAllBtn.addEventListener('click', () => this.onCancelAllClick());

    }

    private attrNum(name: string): number | undefined {
        const value = this.getAttribute(name);
        return value ? Number(value) : undefined;
    }

    private dispatch(evt: string, detail?: any) {
        this.dispatchEvent(new CustomEvent(evt, { detail }));
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

    private onFilesSelected() {
        const files = Array.from(this.input.files ?? []);
        if (!files.length) return;
        if (files.length > this.maxFileCount) {
            this.dispatch('upload-file-error', { fileName: 'all-files',
                error: `Quantidade de arquivos selecionados [${files.length}] 
                maior que o permitido [${this.maxFileCount}].`});
            return;
        }
        let totalFileSize:number = 0;
        for (const file of files)
            totalFileSize = totalFileSize + file.size;
        if (totalFileSize > this.maxTotalFilesSize) {
            this.dispatch('upload-file-error', { fileName: 'all-files',
                error: `Tamanho máximo (em bytes) dos arquivos selecionados 
                [${files.length}]=[${this.bytesToSizeXB(totalFileSize)}] 
                supera o tamanho máximo permitido [${this.bytesToSizeXB(this.maxTotalFilesSize)}].`});
            return;
        }
        for (const file of files) {
            if (this.maxFileSize && file.size > this.maxFileSize) {
                this.dispatch('upload-file-rejected', { fileName: file.name,
                    reason: `Arquivo com tamanho [${this.bytesToSizeXB(file.size)}] 
                    maior que o permitido por arquivo [${this.bytesToSizeXB(this.maxFileSize)}].` });
                continue;
            }
            if (this.accept && !this.isAccepted(file)) {
                this.dispatch('upload-file-rejected', { fileName: file.name,
                    reason: `Arquivo com tipo [${file.type}] diferente dos permitidos [${this.accept}].` });
                continue;
            }
            const task: UploadTask = {
                id: crypto.randomUUID(),
                file,
                state: 'queued',
                sentBytes: 0,
                totalBytes: file.size,
                controller: new AbortController(),
            };
            this.queue.push(task);
            this.renderItem(task);
        }
    }

    private isAccepted(file: File): boolean {
        const list = this.accept?.split(',').map(s => s.trim().toLowerCase()).filter(Boolean);
        if (list == undefined || list.length === 0) return true;
        const ext = '.' + (file.name.split('.').pop() || '').toLowerCase();
        const type = (file.type || '').toLowerCase();
        return list.some(a => a === ext || a === type || (a.endsWith('/*') && type.startsWith(a.slice(0, -1))));
    }

    private async pumpQueue() {
        while (this.running < this.maxConcurrent) {
            const next = this.queue.find(t => t.state === 'queued');
            if (!next) break;
            this.running++;
            this.startTask(next).finally(() => {
                this.running--;
                if (!this.queue.some(t => t.state === 'queued' || t.state === 'running')) {
                    this.dispatch('upload-all-complete');
                } else {
                    this.pumpQueue();
                }
            });
        }
    }

    private async startTask(task: UploadTask) {

        task.state = 'running';
        this.updateItem(task);

        const endpointInit      = `${this.apiBase}/file/uploadInChunk/init`;
        const endpointChunk     = `${this.apiBase}/file/uploadInChunk/chunk`;
        const endpointFinalize  = `${this.apiBase}/file/uploadInChunk/finalize`;

        try {

            // INIT upload
            const initPayload = {
                applicationCode: this.appCode,
                fileName: task.file.name,
                contentType: task.file.type || 'application/octet-stream',
                fileSize: task.file.size,
                checksumSha256: ''
            };
            const initResp = await fetch(endpointInit, {
                method: 'POST',
                headers: this.authHeaders({ 'Content-Type': 'application/json' }),
                body: JSON.stringify(initPayload),
                signal: task.controller.signal
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
            task.uploadId = uploadId;
            this.dispatch('upload-file-start', {fileName: task.file.name, uploadId: task.uploadId, chunkSize: chunkSize});

            // CHUNKS dos arquivos
            const size = task.file.size;
            const cs = Number(chunkSize) || this.CHUNK_SIZE;
            let offset = 0;
            let index = 0;

            while (offset < size) {

                const end = Math.min(offset + cs, size);
                const blob = task.file.slice(offset, end);
                const buf = await blob.arrayBuffer();

                const form = new FormData();
                form.append('uploadId', task.uploadId!);
                form.append('index', String(index));
                form.append('total', String(size));
                form.append('chunk', blob, task.file.name);

                const chunkResp = await fetch(endpointChunk, {
                    method: 'PUT',
                    headers: this.authHeaders({ 'Content-Type': 'application/octet-stream' }),
                    body: form,
                    signal: task.controller.signal
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
                offset = end;
                index++;
                task.sentBytes = end;
                this.updateItem(task);
                this.dispatch('upload-file-progress', {
                    fileName: task.file.name,
                    bytesSent: task.sentBytes,
                    totalBytes: task.totalBytes,
                    percent: Math.floor((task.sentBytes / task.totalBytes) * 100)
                });
                this.updateTotalProgress();
            }

            // FINALIZE upload
            const finResp = await fetch(endpointFinalize, {
                method: 'POST',
                headers: this.authHeaders(),
                body: JSON.stringify({uploadId: task.uploadId}),
                signal: task.controller.signal
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

            task.state = 'complete';
            this.updateItem(task);
            this.dispatch('upload-file-complete', { fileName: task.file.name, uploadId: task.uploadId });

        } catch (err: any) {
            // @ts-ignore
            if ('cancelled' !== task.state) {
                task.state = 'error';
                task.error = String(err?.message ?? err);
                this.updateItem(task);
                this.dispatch('upload-file-error', { fileName: task.file.name, error: task.error });
            }
        }

    }

    private authHeaders(additional?: Record<string,string>) {
        const h: Record<string,string> = { ...(additional ?? {}) };
        if (this.token && this.token.trim()) {
            h['Authorization'] = this.token;
        }
        return h;
    }

    private onCancelAllClick() {
        if (this.queue.length == 0) {
            console.log("Nenhum upload em andamento.");
            return;
        }
        for (const t of this.queue)
            this.cancelTask(t.id);
        console.log("Cancelado todos os uploads...");
    }

    public cancelTask(id: string) {
        const t = this.queue.find(x => x.id === id);
        if (!t) return;
        const endpointCancel    = `${this.apiBase}/file/uploadInChunk/cancel`;
        t.controller.abort();
        t.state = 'cancelled';
        this.updateItem(t);
        if (t.uploadId) {
            // melhor esforço (não espere)
            fetch(`${endpointCancel}/${encodeURIComponent(t.uploadId)}`, {
                method: 'DELETE',
                headers: this.authHeaders()
            }).catch(() => {});
        }
        this.dispatch('upload-file-cancelled', { fileName: t.file.name, uploadId: t.uploadId });
        this.updateTotalProgress();
    }

    private renderItem(task: UploadTask) {
        const list = this.root.querySelector('#msu_list')!;
        const row = document.createElement('div');
        row.className = 'msu__item';
        row.id = `msu_${task.id}`;
        row.innerHTML = `
        <div class="msu__name">${task.file.name}</div>
        <progress class="msu__bar" max="100" value="0"></progress>
        <span class="msu__label">0%</span>
        <button class="msu__cancel" type="button">Cancelar</button>
        `;
        row.querySelector<HTMLButtonElement>('.msu__cancel')!
            .addEventListener('click', () => this.cancelTask(task.id));
        list.appendChild(row);
    }

    private updateItem(task: UploadTask) {
        const row = this.querySelector(`#msu_${task.id}`) as HTMLElement | null;
        if (!row) return;
        const bar = row.querySelector('progress') as HTMLProgressElement;
        const label = row.querySelector('.msu__label') as HTMLElement;
        let pct = 0;
        if (task.totalBytes > 0) pct = Math.floor((task.sentBytes / task.totalBytes) * 100);
        bar.value = pct;
        label.textContent = `${pct}%`;
        row.dataset.state = task.state;
    }

    private updateTotalProgress() {
        const total = this.queue.reduce((acc, t) => acc + t.totalBytes, 0);
        const sent  = this.queue.reduce((acc, t) => acc + t.sentBytes, 0);
        const pct = total ? Math.floor((sent / total) * 100) : 0;
        (this.querySelector('.msu__totalbar') as HTMLProgressElement).value = pct;
        (this.querySelector('.msu__totallabel') as HTMLElement).textContent = `${pct}%`;
    }

    private reqAttr(name: string) {
        const v = this.getAttribute(name);
        if (!v) throw new Error(`Atributo obrigatório ausente: ${name}`);
        return v;
    }

}
customElements.define('meshstorage-upload-multiple', UploadMultiple);
export {};