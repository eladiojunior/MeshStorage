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
    private messages!: HTMLDivElement;
    private list_uploads!: HTMLDivElement;
    private queue: UploadTask[] = [];
    private running = 0;

    constructor() {
        super();
        this.root = this.attachShadow({mode: 'open'});
        this.root.innerHTML = `
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

        const inputFile = this.selector<HTMLInputElement>('#msu_input');
        inputFile.addEventListener('change', () => {
            const files = Array.from(inputFile.files ?? []);
            this.onFilesSelected(files);
        });
        if (this.accept)
            inputFile.accept = this.accept;

        const cancelAllBtn = this.selector<HTMLButtonElement>('#msu_cancel_all');
        cancelAllBtn.addEventListener('click', () => this.onCancelAllClick());

        this.messages = this.selector<HTMLDivElement>('#msu_message');
        this.list_uploads = this.selector<HTMLDivElement>('#msu_list');

        const drop = this.selector<HTMLDivElement>('#msu_drop');
        ;['dragenter','dragover'].forEach(evt =>
            drop.addEventListener(evt, (e: { preventDefault: () => void; })  => {
                e.preventDefault(); drop.classList.add('is-over');
            })
        );
        ;['dragleave','drop'].forEach(evt =>
            drop.addEventListener(evt, (e: { preventDefault: () => void; }) => {
                e.preventDefault(); drop.classList.remove('is-over');
            })
        );
        drop.addEventListener('drop', (e)=>{
            const files = Array.from(e.dataTransfer?.files || []);
            this.onFilesSelected(files);
        });
        drop.addEventListener('click', ()=> inputFile.click());
    }

    private selector<T extends Element>(sel: string): T {
        const el = this.root.querySelector<T>(sel);
        if (!el) throw new Error(`Elemento não encontrado: ${sel}`);
        return el;
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
        const row = document.createElement('div');
        row.className='msu-item'; row.id=`row_${task.id}`;
        row.innerHTML = `
        <div class="msu-name" title="${task.file.name}">${task.file.name}</div>
        <div class="msu-meta">${this.bytesToSizeXB(task.file.size)}</div>
        <div class="msu-status">Aguardando</div>
        <div class="msu-bar"><progress max="100" value="0"></progress></div>
        <div class="msu-actions-row">
          <button class="msu-action msu-action--danger" type="button">Cancelar</button>
        </div>`;
        row.querySelector('button')!.addEventListener('click', ()=> this.cancelTask(task.id));
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
        const ext = this.extension(file.name);
        const color = '#60a5fa';
        const svg = (path:any)=> `<svg class="ico" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path fill="${color}" d="${path}"/></svg>`;
        if (type.startsWith('image/'))
            return null; // terá preview
        if (type==='application/pdf' || ext==='.pdf') {
            return svg("M14 2H6a2 2 0 0 0-2 2v16c0 1.1.9 2 2 2h12a2 2 0 0 0 2-2V8l-6-6Zm1 7V3.5L19.5 9H15Z M8 14h8v2H8v-2Zm0 4h8v2H8v-2Zm0-8h5v2H8V10Z");
        }
        if (type.includes('word') || ext==='.doc' || ext==='.docx') {
            return svg("M4 2h10l6 6v14a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Zm8 1.5V9h6.5L12 3.5ZM6.5 12h2l1 5 1-5h2l1 5 1-5h2l-2 8h-2l-1-5-1 5H8.5l-2-8Z");
        }
        if (type==='application/zip' || ext==='.zip') {
            return svg("M7 2h6l4 4v16a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Zm6 1.5V7h4.5L13 3.5ZM10 6h2v2h-2V6Zm0 3h2v2h-2V9Zm0 3h2v2h-2v-2Zm0 3h2v3h-2v-3Z");
        }
        return svg("M6 2h7l5 5v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Zm6 .5V7h5.5L12 2.5Z");
    }

    // ------------ Util -------------------
    private extension(name:string):string{
        const i=name.lastIndexOf('.'); return i>0 ? name.slice(i).toLowerCase() : ''
    }
    private reqAttr(name: string):string {
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
        if (hasErro)
            console.error(`[Error]: ${message}`);
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
    private attrNum(name: string): number | undefined {
        const value = this.getAttribute(name);
        return value ? Number(value) : undefined;
    }
}
customElements.define('meshstorage-upload-multiple', UploadMultiple);
export {};