type UploadTaskState = 'queued' | 'running' | 'complete' | 'error' | 'cancelled';
type UploadIconType = 'none' | 'success' | 'error';
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
    private messages!: HTMLDivElement;
    private message_text!: HTMLSpanElement;
    private list_uploads!: HTMLDivElement;
    private queue: UploadTask[] = [];
    private running = 0;

    constructor() {
        super();
        this.root = this.attachShadow({mode: 'open'});
        this.root.innerHTML = `
        <style>
            .msum-wrap {max-width:800px;margin:10px auto;padding:0 16px;}
        .msum-panel {background:#ffffff;border-radius:14px;box-shadow:0 6px 20px rgba(0,0,0,.06);padding:15px;}
        .msum-panel .hint {color:#6b7280;margin-top: 0; margin-bottom:15px}
        .msum-wrap .dropzone{ border:2px dashed #3169a3;border-radius:14px;background:#f8fafc; min-height:100px; display:flex; align-items:center; justify-content:center; text-align:center; padding:20px; transition:.2s ease; cursor:pointer; outline:none; }
        .msum-wrap .dropzone:focus {box-shadow:0 0 0 3px #e0e7ff;}
        .msum-wrap .dropzone .drag {background:#eef2ff;border-color:#1c65a2}
        .msum-wrap .dropzone .title {font-size:18px; margin-bottom:5px;}
        .msum-wrap .dropzone .subtitle {color:#1c65a2; font-weight:600}
        .msum-wrap .dropzone small {display:block; color:#6b7280}
        .msum-wrap .dropzone input[type=file] {display:none}
        .msum-panel .actions {display:flex;gap:10px;margin-top:12px;flex-wrap:wrap}
        .msum-panel .btn {background:#1c65a2;color:#fff;border:0;padding:10px 14px;border-radius:12px;cursor:pointer}
        .msum-panel .btn.secondary {background:#e5e7eb;color:#111827}
        .msum-panel .btn:disabled {opacity:.5;cursor:not-allowed}
        .msum-panel .grid {display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:14px;margin-top:18px}
        .msum-panel .msum-item {background:#ffffff;border:1px solid #e5e7eb;border-radius:14px;overflow:hidden;display:flex;flex-direction:column}
        .msum-panel .thumb {position:relative;background:#f3f4f6;aspect-ratio:16/10;display:flex;align-items:center;justify-content:center}
        .msum-panel .thumb img {max-width:100%;max-height:100%;object-fit:cover}
        .msum-panel .meta {padding:10px 12px}
        .msum-panel .name {font-size:14px;font-weight:600;white-space:nowrap;text-overflow:ellipsis;overflow:hidden}
        .msum-panel .sub {font-size:10px;color:#6b7280;margin-top:2px;white-space:nowrap;text-overflow:ellipsis;overflow:hidden}
        .msum-panel .progress {height:6px;background:#eef2ff;border-radius:5px;overflow:hidden;margin-top:10px}
        .msum-panel .progress > span{display:block;height:100%;width:0;background:linear-gradient(90deg,#60a5fa,#1c65a2)}
        .msum-panel .row {display:flex;gap:8px;align-items:center;justify-content:center;margin-top:5px}
        .msum-panel .icon-btn {background:transparent;border:0;padding:6px;border-radius:10px;cursor:pointer;display: flex;}
        .msum-panel .icon-btn:hover{background:#f3f4f6}
        .msum-panel .image-icon {width:52px;height:52px;opacity:.9;fill: #60a5fa}
        .msum-panel .thumb.success .image-icon {fill: #259543 }
        .msum-panel .thumb.error .image-icon {fill: #dc2626 }
        .msum-panel .alert-icon { position: absolute; right: 8px; bottom: 6px; width: 24px; height: 24px; display: flex; align-items: center; justify-content: center; }
        .msum-panel .thumb.error .alert-icon {fill: #dc2626 }
        .msum-panel .thumb.success .alert-icon {fill: #259543; width: 22px; height: 22px; }
        .msum-panel .messages {margin-top: 5px; display: flex; align-items: center; justify-content: flex-start;}
        .msum-panel .messages .icon {width:26px;height:26px;fill: #dc2626}
        .msum-panel .messages > span {margin-left: 5px;color: #dc2626}
        </style>
        <div class="msum-wrap">
            <div class="msum-panel" role="region" aria-labelledby="ttl">
                <p class="hint" id="msum_hint_uploads">Escolha apenas tipos: <strong>.ext</strong> até <strong>X</strong> arquivos de no máximo <strong>X MB</strong>.</p>
                <label class="dropzone" id="msum_drop_files_upload" tabindex="0" aria-label="Área para arrastar e soltar arquivos">
                    <div>
                        <div class="title">Arraste e solte aqui</div>
                        <span class="subtitle">ou clique para selecionar</span>
                        <small>Você pode escolher múltiplos arquivos</small>
                        <input id="msum_file_upload" type="file" multiple />
                    </div>
                </label>
                <div id="msum_messages">
                    <div class="messages">
                        <div class="icon">
                            <svg viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg"><defs><style>.cls-1{fill:none;}</style></defs><title/><g><path d="M16,19a1,1,0,0,1-1-.76l-1.51-6.06A2.5,2.5,0,0,1,14,10a2.56,2.56,0,0,1,4,0,2.5,2.5,0,0,1,.46,2.19L17,18.24A1,1,0,0,1,16,19Zm0-8a.54.54,0,0,0-.44.22.52.52,0,0,0-.1.48L16,13.88l.54-2.18a.52.52,0,0,0-.1-.48A.54.54,0,0,0,16,11Z"/><circle cx="16" cy="21.5" r="1.5"/><path d="M16,29A13,13,0,1,1,29,16,13,13,0,0,1,16,29ZM16,5A11,11,0,1,0,27,16,11,11,0,0,0,16,5Z"/></g><g id="frame"><rect class="cls-1" height="30" width="30"/></g></svg>
                        </div>
                        <span id="msum_message_text">{mensagem}</span>
                    </div>
                </div>
                <div class="actions">
                    <button class="btn" id="msum_send_all" disabled>Enviar tudo</button>
                    <button class="btn secondary" id="msum_cancel_all" disabled>Cancelar tudo</button>
                    <button class="btn secondary" id="msum_clear_all" disabled>Limpar</button>
                </div>
                <div class="grid" id="msum_list_uploads" aria-live="polite"></div>
            </div>
        </div>
        `;
    }

    connectedCallback() {

        //Recuperar os atributos do componente.
        this.appCode = this.requiredAttribute('app-code');
        this.apiBase = this.requiredAttribute('api-url-upload');
        this.token = this.getAttribute('api-token-jwt') ?? undefined;
        this.accept = this.getAttribute('accept') ?? this.TYPE_ACCEPT;
        this.maxTotalFilesSize = this.getAttributeNumber('max-total-files-size') ?? this.MAX_TOTAL_FILES_SIZE;
        this.maxFileSize = this.getAttributeNumber('max-file-size') ?? this.MAX_FILE_SIZE;
        this.maxFileCount = this.getAttributeNumber('max-file-count') ?? this.MAX_FILE_COUNT;
        this.maxConcurrent = this.getAttributeNumber('max-concurrent-upload') ?? this.MAX_CONCURRENT_UPLOAD;

        //Recuperra os campos da interface...
        const inputFile = this.selectorRoot<HTMLInputElement>('#msum_file_upload');
        inputFile.addEventListener('change', () => {
            const files = Array.from(inputFile.files ?? []);
            this.onFilesSelected(files);
        });
        if (this.accept)
            inputFile.accept = this.accept;

        const paragHint = this.selectorRoot<HTMLParagraphElement>('#msum_hint_uploads');
        paragHint.innerHTML = `Escolha apenas tipos: <strong>${this.accept}</strong> até <strong>${this.maxFileCount}</strong> arquivos de no máximo <strong>${this.bytesToSizeXB(this.maxFileSize)}</strong>.`;

        this.messages = this.selectorRoot<HTMLDivElement>('#msum_messages');
        this.messages.hidden = true;
        this.message_text = this.selectorRoot<HTMLSpanElement>('#msum_message_text');

        const btn_cancel_all = this.selectorRoot<HTMLButtonElement>('#msum_cancel_all');
        btn_cancel_all.addEventListener('click', () => this.onCancelAllClick());
        btn_cancel_all.hidden = true;
        const btn_send_all = this.selectorRoot<HTMLButtonElement>('#msum_send_all');
        btn_send_all.addEventListener('click', () => this.onSendAllClick());
        const btn_clean_all = this.selectorRoot<HTMLButtonElement>('#msum_clear_all');
        btn_clean_all.addEventListener('click', () => this.onClearAllClick());

        this.list_uploads = this.selectorRoot<HTMLDivElement>('#msum_list_uploads');

        const drop_uploads = this.selectorRoot<HTMLDivElement>('#msum_drop_files_upload');
        ;['dragenter','dragover'].forEach(evt =>
            drop_uploads.addEventListener(evt, (e: { preventDefault: () => void; })  => {
                e.preventDefault(); drop_uploads.classList.add('drag');
            })
        );
        ;['dragleave','drop'].forEach(evt =>
            drop_uploads.addEventListener(evt, (e: { preventDefault: () => void; }) => {
                e.preventDefault(); drop_uploads.classList.remove('drag');
            })
        );
        drop_uploads.addEventListener('drop', (e)=>{
            const files = Array.from(e.dataTransfer?.files || []);
            this.onFilesSelected(files);
        });
        drop_uploads.addEventListener('click', ()=> inputFile.click());

    }

    private selectorRoot<T extends Element>(sel: string): T {
        const el = this.root.querySelector<T>(sel);
        if (!el) throw new Error(`Elemento não encontrado: ${sel}`);
        return el;
    }
    private selectorParent<T extends Element>(el:Element, sel: string): T {
        const el_child = el.querySelector<T>(sel);
        if (!el_child) throw new Error(`Elemento não encontrado: ${sel}`);
        return el_child;
    }

    private dispatch(evt: string, detail?: any) {
        this.dispatchEvent(new CustomEvent(evt, { detail }));
    }

    private bytesToSizeXB(bytes: number): string {
        let u: string[];
        u = ['B', 'KB', 'MB', 'GB'];
        let i = 0;
        let n = bytes;
        while (n >= 1024 && i < u.length - 1) {
            n /= 1024;
            i++;
        }
        return `${n.toFixed((i === 0) ? 0 : 1)} ${u[i]}`;
    }

    private onFilesSelected(files: any) {
        if (!files.length)
            return;
        if (files.length > this.maxFileCount) {
            let message = `Quantidade de arquivos selecionados [${files.length}] 
                maior que o permitido [${this.maxFileCount}].`;
            this.alert(message);
            this.dispatch('upload-file-error', { fileName: 'all-files', error: message});
            return;
        }
        let totalFileSize:number = 0;
        for (const file of files)
            totalFileSize = totalFileSize + file.size;
        let message = `Tamanho máximo (em bytes) dos arquivos selecionados 
                [${files.length}]=[${this.bytesToSizeXB(totalFileSize)}] 
                supera o tamanho máximo permitido [${this.bytesToSizeXB(this.maxTotalFilesSize)}].`;
        if (totalFileSize > this.maxTotalFilesSize) {
            this.alert(message);
            this.dispatch('upload-file-error', { fileName: 'all-files', error: message});
            return;
        }
        const valid = [];
        for (const file of files) {
            if (this.maxFileSize && file.size > this.maxFileSize) {
                let message = `Arquivo com tamanho [${this.bytesToSizeXB(file.size)}] 
                    maior que o permitido por arquivo [${this.bytesToSizeXB(this.maxFileSize)}].`;
                this.alert(message);
                this.dispatch('upload-file-rejected', { fileName: file.name, reason: message });
                continue;
            }
            if (this.accept && !this.isAccepted(file)) {
                let message = `Arquivo com tipo [${file.type}] diferente dos permitidos [${this.accept}].`;
                this.alert(message);
                this.dispatch('upload-file-rejected', { fileName: file.name, reason: message });
                continue;
            }
            const task: UploadTask = {
                id: crypto.randomUUID(),
                file, state: 'queued', sentBytes: 0,
                totalBytes: file.size,
                controller: new AbortController(),
            };
            this.queue.push(task);
            this.renderItem(task);
            valid.push(task);
        }

        if (valid.length) {
            this.messages.hidden = true;
            this.pumpQueue().then(() => console.log('processando...'));
        }
        this.updateTotalProgress();

    }

    private async pumpQueue() {
        while (this.running < this.maxConcurrent) {
            const next = this.queue.find(t => t.state === 'queued');
            if (!next) break;
            this.running++;
            this.startTask(next).finally(() => {
                this.running--;
                if (!this.queue.some(t => t.state === 'queued' || t.state === 'running')) {
                    this.dispatch('upload-all-complete', 'Todos os uploads concluídos');
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
        const endpointComplete  = `${this.apiBase}/file/uploadInChunk/finalize`;

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
            await this.checkRespose(initResp);
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
                await this.checkRespose(chunkResp);
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
            const finResp = await fetch(endpointComplete, {
                method: 'POST',
                headers: this.authHeaders(),
                body: JSON.stringify({uploadId: task.uploadId}),
                signal: task.controller.signal
            });
            await this.checkRespose(finResp);
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

    private onClearAllClick() {
        if (this.queue.length == 0) {
            this.alert("Nenhum upload pendente de envio.", true);
            return;
        }
        for (const t of this.queue)
            this.removeTask(t.id);
        this.alert("Removido todos os uploads...");
    }


    private onSendAllClick() {
        if (this.queue.length == 0) {
            this.alert("Nenhum upload pendente de envio.", true);
            return;
        }
        for (const t of this.queue)
            this.sendTask(t.id);
        this.alert("Reenviando todos os uploads...");
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

    private sendTask(id: string) {
        console.log(`Enviar: ${id}`);
    }

    private removeTask(id: string) {
        console.log(`Remover: ${id}`);
    }

    private cancelTask(id: string) {
        const task = this.queue.find(x => x.id === id);
        if (!task) return;
        const endpointCancel    = `${this.apiBase}/file/uploadInChunk/cancel`;
        task.controller.abort();
        task.state = 'cancelled';
        this.updateItem(task);
        if (task.uploadId) {
            // melhor esforço (não espere)
            fetch(`${endpointCancel}/${encodeURIComponent(task.uploadId)}`, {
                method: 'DELETE',
                headers: this.authHeaders()
            }).catch(() => {});
        }
        this.dispatch('upload-file-cancelled', { fileName: task.file.name, uploadId: task.uploadId });
        this.updateTotalProgress();
    }

    private renderItem(task: UploadTask) {
        const row = document.createElement('div');
        row.className='msum-item'; row.id=`row_${task.id}`;
        row.innerHTML = `
        <div class="thumb">
            <div>
                ${this.iconForFile(task.file)}
            </div>
            <div class="alert-icon">
                <svg viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg"><defs><style>.cls-1{fill:none;}</style></defs><title/><g><path d="M16,19a1,1,0,0,1-1-.76l-1.51-6.06A2.5,2.5,0,0,1,14,10a2.56,2.56,0,0,1,4,0,2.5,2.5,0,0,1,.46,2.19L17,18.24A1,1,0,0,1,16,19Zm0-8a.54.54,0,0,0-.44.22.52.52,0,0,0-.1.48L16,13.88l.54-2.18a.52.52,0,0,0-.1-.48A.54.54,0,0,0,16,11Z"/><circle cx="16" cy="21.5" r="1.5"/><path d="M16,29A13,13,0,1,1,29,16,13,13,0,0,1,16,29ZM16,5A11,11,0,1,0,27,16,11,11,0,0,0,16,5Z"/></g><g><rect class="cls-1" height="30" width="30"/></g></svg>
            </div>
        </div>
        <div class="meta">
            <div class="name" title="${task.file.name}">${task.file.name}</div>
            <div class="sub">${this.bytesToSizeXB(task.file.size)} - ${task.file.type}</div>
            <div class="progress" aria-hidden="true"><span></span></div>
            <div class="row">
                <button class="icon-btn cancel" title="Cancelar" aria-label="Cancelar">
                    <svg viewBox="0 0 42 42" width="24px" height="24px" xml:space="preserve" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px">
                        <path fill-rule="evenodd" d="M21.002,26.588l10.357,10.604c1.039,1.072,1.715,1.083,2.773,0l2.078-2.128 c1.018-1.042,1.087-1.726,0-2.839L25.245,21L36.211,9.775c1.027-1.055,1.047-1.767,0-2.84l-2.078-2.127 c-1.078-1.104-1.744-1.053-2.773,0L21.002,15.412L10.645,4.809c-1.029-1.053-1.695-1.104-2.773,0L5.794,6.936 c-1.048,1.073-1.029,1.785,0,2.84L16.759,21L5.794,32.225c-1.087,1.113-1.029,1.797,0,2.839l2.077,2.128 c1.049,1.083,1.725,1.072,2.773,0L21.002,26.588z"/>
                    </svg>
                ️ </button>
                <button class="icon-btn send" title="Enviar" aria-label="Enviar">
                    <svg viewBox="0 0 48 48" width="24px" height="24px" xmlns="http://www.w3.org/2000/svg" xml:space="preserve">
                        <path d="M35,2H17c-0.2651367,0-0.5195313,0.1054688-0.7070313,0.2929688l-8,8C8.1054688,10.4804688,8,10.734375,8,11v30  c0,2.7568359,2.2431641,5,5,5h22c2.7568359,0,5-2.2431641,5-5V7C40,4.2431641,37.7568359,2,35,2z M38,41  c0,1.6542969-1.3457031,3-3,3H13c-1.6542969,0-3-1.3457031-3-3V11.4140625L17.4140625,4H35c1.6542969,0,3,1.3457031,3,3V41z M17,14  h-5c-0.5522461,0-1-0.4472656-1-1s0.4477539-1,1-1h5c0.5512695,0,1-0.4482422,1-1V6c0-0.5527344,0.4477539-1,1-1s1,0.4472656,1,1v5  C20,12.6542969,18.6542969,14,17,14z M24,17c-4.9624023,0-9,4.0371094-9,9s4.0375977,9,9,9s9-4.0371094,9-9S28.9624023,17,24,17z   M24,33c-3.8598633,0-7-3.140625-7-7s3.1401367-7,7-7s7,3.140625,7,7S27.8598633,33,24,33z M27.7070313,24.2929688  c0.390625,0.390625,0.390625,1.0234375,0,1.4140625C27.5117188,25.9023438,27.2558594,26,27,26  s-0.5117188-0.0976563-0.7070313-0.2929688L25,24.4140625V30c0,0.5527344-0.4477539,1-1,1s-1-0.4472656-1-1v-5.5859375  l-1.2929688,1.2929688c-0.390625,0.390625-1.0234375,0.390625-1.4140625,0s-0.390625-1.0234375,0-1.4140625l3-3  c0.390625-0.390625,1.0234375-0.390625,1.4140625,0L27.7070313,24.2929688z"/>
                    </svg>
                </button>
                <button class="icon-btn remove" title="Remover" aria-label="Remover">
                    <svg viewBox="0 0 91 91" height="30px" width="30px" xml:space="preserve" xmlns="http://www.w3.org/2000/svg"><g>
                        <path d="M67.305,36.442v-8.055c0-0.939-0.762-1.701-1.7-1.701H54.342v-5.524c0-0.938-0.761-1.7-1.699-1.7h-12.75   c-0.939,0-1.701,0.762-1.701,1.7v5.524H26.93c-0.939,0-1.7,0.762-1.7,1.701v8.055c0,0.938,0.761,1.699,1.7,1.699h0.488v34.021   c0,0.938,0.761,1.7,1.699,1.7h29.481c3.595,0,6.52-2.924,6.52-6.518V38.142h0.486C66.543,38.142,67.305,37.381,67.305,36.442z    M41.592,22.862h9.35v3.824h-9.35V22.862z M61.719,67.345c0,1.719-1.4,3.117-3.12,3.117h-27.78v-32.32l30.9,0.002V67.345z    M63.904,34.742H28.629v-4.655h11.264h12.75h11.262V34.742z"/><rect height="19.975" width="3.4" x="36.066" y="44.962"/><rect height="19.975" width="3.4" x="44.566" y="44.962"/><rect height="19.975" width="3.4" x="53.066" y="44.962"/></g>
                    </svg>
                ️</button>
            </div>
        </div>`;

        const btn_upload_send = this.selectorParent<HTMLButtonElement>(row, 'button.send');
        btn_upload_send.hidden = true;
        btn_upload_send.addEventListener('click', ()=> this.sendTask(task.id));

        const btn_upload_cancel = this.selectorParent<HTMLButtonElement>(row, 'button.cancel');
        btn_upload_cancel.hidden = true;
        btn_upload_cancel.addEventListener('click', ()=> this.cancelTask(task.id));

        const btn_upload_remove = this.selectorParent<HTMLButtonElement>(row, 'button.remove');
        btn_upload_remove.addEventListener('click', ()=> this.removeTask(task.id));

        this.list_uploads.appendChild(row);

    }

    private updateItem(task: UploadTask){
        const row = document.getElementById(`row_${task.id}`);
        if (!row) return;
        const status = row.querySelector('.msu-status') as HTMLElement;
        const bar = row.querySelector('progress') as HTMLProgressElement;

        let stateText = 'Aguardando';
        let cls = 'msu-status';

        if (task.state === 'running')   { stateText = 'Enviando'; cls = 'msu-status msu-status--running'; }
        if (task.state === 'complete')  { stateText = 'Concluído'; cls = 'msu-status msu-status--ok'; }
        if (task.state === 'error')     { stateText = 'Erro';      cls = 'msu-status msu-status--error'; }
        if (task.state === 'cancelled') {stateText = 'Cancelado'; cls = 'msu-status'; }

        status.textContent = stateText;
        status.className = cls;

        bar.value = task.totalBytes ? Math.floor((task.sentBytes / task.totalBytes) * 100) : 0;

    }

    private updateTotalProgress() {
        const total = this.queue.reduce((acc, t) => acc + t.totalBytes, 0);
        const sent  = this.queue.reduce((acc, t) => acc + t.sentBytes, 0);
        const pct = total ? Math.floor((sent / total) * 100) : 0;
        (this.root.querySelector('.msu__totalbar') as HTMLProgressElement).value = pct;
        (this.root.querySelector('.msu__totallabel') as HTMLElement).textContent = `${pct}%`;
    }

    private iconForFile(file: File) {
        const type = (file.type || '').toLowerCase();
        if (type.startsWith('image/'))
            return null;
        const ext = this.extension(file.name);
        const svg = (extension:string)=>
            `<svg class="image-icon" xmlns="http://www.w3.org/2000/svg" viewBox="2 2 18 22" width="18px" height="22px">
                <path d="M 4 2 L 14 2 L 20 8 L 20 22 C 20 23.105 19.105 24 18 24 L 4 24 C 2.895 24 2 23.105 2 22 L 2 4 C 2 2.895 2.895 2 4 2 Z M 12 3.5 L 12 9 L 18.5 9 L 12 3.5 Z M 3.695 13.471 L 3.695 17.982 C 3.695 18.616 4.209 19.13 4.843 19.13 L 17.341 19.13 C 17.975 19.13 18.489 18.616 18.489 17.982 L 18.489 13.471 C 18.489 12.837 17.975 12.323 17.341 12.323 L 4.843 12.323 C 4.209 12.323 3.695 12.837 3.695 13.471 Z"/>
                <text style="fill: rgb(51, 51, 51); font-family: Arial, sans-serif; font-size: 4px; font-weight: 700; text-anchor: middle; white-space: pre;" x="10.847" y="17.019">${extension}</text>
            </svg>`;
        return svg(ext);
    }

    // ------------ Util -------------------
    private extension(name:string):string{
        const i=name.lastIndexOf('.'); return i>0 ? name.slice(i).toLowerCase() : ''
    }
    private requiredAttribute(name: string):string {
        const v = this.getAttribute(name);
        if (!v) throw new Error(`Atributo obrigatório ausente: ${name}`);
        return v;
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
    private authHeaders(additional?: Record<string,string>) {
        const h: Record<string,string> = { ...(additional ?? {}) };
        if (this.token && this.token.trim()) {
            h['Authorization'] = this.token;
        }
        return h;
    }
    private alert(message: string, hasErro: boolean = true) {
        if (hasErro) {
            this.messages.hidden = false;
            this.message_text.innerHTML = message;
        }
        else
            console.log(`[Alert]: ${message}`);
    }
    private isAccepted(file: File): boolean {
        const list = this.accept?.split(',').map(s => s.trim().toLowerCase()).filter(Boolean);
        if (list == undefined || list.length === 0) return true;
        const ext = this.extension(file.name);
        const type = (file.type || '').toLowerCase();
        return list.some(a => a === ext || a === type || (a.endsWith('/*') &&
            type.startsWith(a.slice(0, -1))));
    }
    private getAttributeNumber(name: string): number | undefined {
        const value = this.getAttribute(name);
        return value ? Number(value) : undefined;
    }
}
customElements.define('meshstorage-upload-multiple', UploadMultiple);
export {};