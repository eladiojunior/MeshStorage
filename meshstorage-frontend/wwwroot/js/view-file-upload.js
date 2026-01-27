/**
 * FileUpload - Módulo de upload de arquivos em blocos (chunks)
 * Suporta drag and drop, validação, progresso e upload concorrente
 */
const ViewFileUpload = (function() {
    'use strict';

    // Configurações
    let config = {
        contexto: '/',
        chunkSize: 1024 * 1024, // 1MB
        maxConcurrentUploads: 3
    };

    // Estado
    let state = {
        files: [],
        currentApplication: null,
        maxFileSize: 0,
        allowedTypes: [],
        isUploading: false,
        uploadQueue: [],
        activeUploads: 0,
        completedUploads: 0,
        failedUploads: 0
    };

    // Elementos DOM
    let elements = {};

    /**
     * Inicializa o módulo
     */
    function init(options) {
        config = { ...config, ...options };
        cacheElements();
        bindEvents();
        handleApplicationChange();
    }

    /**
     * Cache dos elementos DOM
     */
    function cacheElements() {
        elements = {
            dropZone: $('#dropZone'),
            fileInput: $('#fileInput'),
            fileList: $('#fileList'),
            fileListContainer: $('#fileListContainer'),
            fileCount: $('#fileCount'),
            selectApplication: $('#selectApplication'),
            btnStartUpload: $('#btnStartUpload'),
            btnCancelUpload: $('#btnCancelUpload'),
            btnClearAll: $('#btnClearAll'),
            overallProgressContainer: $('#overallProgressContainer'),
            overallProgressBar: $('#overallProgressBar'),
            overallProgressText: $('#overallProgressText'),
            uploadStats: $('#uploadStats'),
            rulesPlaceholder: $('#rulesPlaceholder'),
            rulesContent: $('#rulesContent'),
            ruleMaxSize: $('#ruleMaxSize'),
            ruleAllowedTypes: $('#ruleAllowedTypes'),
            settingsPlaceholder: $('#settingsPlaceholder'),
            settingsContent: $('#settingsContent'),
            settingCompress: $('#settingCompress'),
            settingConvertWebp: $('#settingConvertWebp'),
            settingOcr: $('#settingOcr'),
            settingDuplicate: $('#settingDuplicate'),
            settingReplication: $('#settingReplication')
        };
    }

    /**
     * Bindagem de eventos
     */
    function bindEvents() {
        // Drag and Drop
        elements.dropZone
            .on('dragover dragenter', handleDragOver)
            .on('dragleave dragend', handleDragLeave)
            .on('drop', handleDrop)
            .on('click', () => elements.fileInput.trigger('click'));

        // File Input
        elements.fileInput.on('change', handleFileSelect);

        // Seleção de aplicação
        elements.selectApplication.on('change', handleApplicationChange);

        // Botões
        elements.btnStartUpload.on('click', startUpload);
        elements.btnCancelUpload.on('click', cancelAllUploads);
        elements.btnClearAll.on('click', clearAllFiles);

        // Delegação para remover arquivos individuais
        elements.fileList.on('click', '.btn-remove-file', function() {
            const fileId = $(this).closest('.file-item').data('file-id');
            removeFile(fileId);
        });
    }

    /**
     * Handlers de Drag and Drop
     */
    function handleDragOver(e) {
        e.preventDefault();
        e.stopPropagation();
        elements.dropZone.addClass('drag-over');
    }

    function handleDragLeave(e) {
        e.preventDefault();
        e.stopPropagation();
        elements.dropZone.removeClass('drag-over');
    }

    function handleDrop(e) {
        e.preventDefault();
        e.stopPropagation();
        elements.dropZone.removeClass('drag-over');

        const files = e.originalEvent.dataTransfer.files;
        processFiles(files);
    }

    /**
     * Handler de seleção de arquivos via input
     */
    function handleFileSelect(e) {
        const files = e.target.files;
        processFiles(files);
        elements.fileInput.val(''); // Reset input
    }

    /**
     * Handler de mudança de aplicação
     */
    function handleApplicationChange() {
        const $selected = elements.selectApplication.find(':selected');
        
        if (!$selected.val()) {
            state.currentApplication = null;
            state.maxFileSize = 0;
            state.allowedTypes = [];
            showRulesPlaceholder();
            showSettingsPlaceholder();
            validateAllFiles();
            updateButtonState();
            return;
        }

        state.currentApplication = $selected.val();
        state.maxFileSize = parseInt($selected.data('max-size')) || 0;
        state.allowedTypes = ($selected.data('allowed-types') || '').split(',').filter(t => t);

        updateRulesDisplay();
        updateSettingsDisplay($selected);
        validateAllFiles();
        updateButtonState();
    }

    /**
     * Processa arquivos selecionados/dropados
     */
    function processFiles(fileList) {
        for (let i = 0; i < fileList.length; i++) {
            const file = fileList[i];
            const fileId = generateId();
            
            const fileData = {
                id: fileId,
                file: file,
                name: file.name,
                size: file.size,
                type: file.type,
                extension: getFileExtension(file.name),
                status: 'pending',
                progress: 0,
                uploadId: null,
                error: null,
                validation: null
            };

            // Valida o arquivo
            fileData.validation = validateFile(fileData);
            
            state.files.push(fileData);
            renderFileItem(fileData);
        }

        updateFileListVisibility();
        updateButtonState();
    }

    /**
     * Valida um arquivo
     */
    function validateFile(fileData) {
        const errors = [];

        if (!state.currentApplication) {
            return null; // Não valida sem aplicação selecionada
        }

        // Validação de tamanho
        if (state.maxFileSize > 0) {
            const convertToBytes = state.maxFileSize * 1048576; //de MB para Bytes;
            if (fileData.size > convertToBytes) {
                errors.push(`Tamanho excede o limite de ${formatBytes(convertToBytes)}`);
            }
        }

        // Validação de tipo
        if (state.allowedTypes.length > 0) {
            const ext = `.${fileData.extension.toLowerCase()}`;
            if (!state.allowedTypes.some(t => t.toLowerCase() === ext)) {
                errors.push(`Tipo .${ext} não é permitido`);
            }
        }

        return errors.length > 0 ? errors : null;
    }

    /**
     * Revalida todos os arquivos
     */
    function validateAllFiles() {
        state.files.forEach(fileData => {
            fileData.validation = validateFile(fileData);
            updateFileItemValidation(fileData);
        });
    }

    /**
     * Renderiza um item de arquivo na lista
     */
    function renderFileItem(fileData) {
        const iconInfo = getFileIcon(fileData.extension);
        const hasError = fileData.validation && fileData.validation.length > 0;
        
        const html = `
            <div class="file-item ${hasError ? 'error' : ''}" data-file-id="${fileData.id}">
                <div class="file-item-icon ${iconInfo.class}">
                    <span class="material-icons">${iconInfo.icon}</span>
                </div>
                <div class="file-item-info">
                    <div class="file-item-name" title="${fileData.name}">${fileData.name}</div>
                    <div class="file-item-meta">${formatBytes(fileData.size)}</div>
                    ${hasError ? `<div class="file-item-validation">${fileData.validation.join('; ')}</div>` : ''}
                </div>
                <div class="file-item-progress">
                    <div class="progress">
                        <div class="progress-bar" role="progressbar" style="width: 0%"></div>
                    </div>
                    <div class="text-center small text-muted mt-1">
                        <span class="progress-text">0%</span>
                    </div>
                </div>
                <div class="file-item-status pending">
                    <span class="material-icons">${hasError ? 'error' : 'schedule'}</span>
                </div>
                <div class="file-item-actions">
                    <button type="button" class="btn btn-outline-danger btn-sm btn-remove-file" title="Remover">
                        <span class="material-icons">close</span>
                    </button>
                </div>
            </div>
        `;
        
        elements.fileList.append(html);
    }

    /**
     * Atualiza a exibição de validação de um arquivo
     */
    function updateFileItemValidation(fileData) {
        const $item = $(`.file-item[data-file-id="${fileData.id}"]`);
        const hasError = fileData.validation && fileData.validation.length > 0;
        
        $item.toggleClass('error', hasError);
        
        let $validation = $item.find('.file-item-validation');
        if (hasError) {
            if ($validation.length === 0) {
                $item.find('.file-item-info').append(`<div class="file-item-validation">${fileData.validation.join('; ')}</div>`);
            } else {
                $validation.text(fileData.validation.join('; '));
            }
            $item.find('.file-item-status .material-icons').text('error');
        } else {
            $validation.remove();
            if (fileData.status === 'pending') {
                $item.find('.file-item-status .material-icons').text('schedule');
            }
        }
    }

    /**
     * Atualiza a exibição das regras
     */
    function updateRulesDisplay() {
        elements.rulesPlaceholder.addClass('d-none');
        elements.rulesContent.removeClass('d-none');

        // Tamanho máximo
        elements.ruleMaxSize.text(state.maxFileSize > 0 ? state.maxFileSize + ' MB' : 'Sem limite');

        // Tipos permitidos
        if (state.allowedTypes.length > 0) {
            const badges = state.allowedTypes.map(t => `<span class="type-badge">${t}</span>`).join('');
            elements.ruleAllowedTypes.html(badges);
        } else {
            elements.ruleAllowedTypes.html('<span class="text-muted">Todos os tipos</span>');
        }
    }

    /**
     * Atualiza a exibição das configurações
     */
    function updateSettingsDisplay($selected) {
        elements.settingsPlaceholder.addClass('d-none');
        elements.settingsContent.removeClass('d-none');

        const settings = [
            { el: elements.settingCompress, value: $selected.data('compress') },
            { el: elements.settingConvertWebp, value: $selected.data('convert-webp') },
            { el: elements.settingOcr, value: $selected.data('ocr') },
            { el: elements.settingDuplicate, value: $selected.data('allow-duplicate') },
            { el: elements.settingReplication, value: $selected.data('replication') }
        ];

        settings.forEach(s => {
            const enabled = s.value === true || s.value === 'true';
            s.el.text(enabled ? 'Sim' : 'Não')
               .removeClass('enabled disabled')
               .addClass(enabled ? 'enabled' : 'disabled');
        });
    }

    /**
     * Mostra placeholder de regras
     */
    function showRulesPlaceholder() {
        elements.rulesPlaceholder.removeClass('d-none');
        elements.rulesContent.addClass('d-none');
    }

    /**
     * Mostra placeholder de configurações
     */
    function showSettingsPlaceholder() {
        elements.settingsPlaceholder.removeClass('d-none');
        elements.settingsContent.addClass('d-none');
    }

    /**
     * Inicia o processo de upload
     */
    async function startUpload() {
        if (!state.currentApplication) {
            Toast.warning('Selecione uma aplicação antes de iniciar o upload.');
            return;
        }

        const validFiles = state.files.filter(f => !f.validation && f.status === 'pending');
        if (validFiles.length === 0) {
            Toast.warning('Nenhum arquivo válido para upload.');
            return;
        }

        state.isUploading = true;
        state.uploadQueue = [...validFiles];
        state.completedUploads = 0;
        state.failedUploads = 0;

        elements.btnStartUpload.prop('disabled', true);
        elements.btnCancelUpload.prop('disabled', false);
        elements.selectApplication.prop('disabled', true);
        elements.overallProgressContainer.removeClass('d-none');

        updateOverallProgress();

        // Inicia uploads concorrentes
        for (let i = 0; i < config.maxConcurrentUploads && state.uploadQueue.length > 0; i++) {
            processNextUpload();
        }
    }

    /**
     * Processa o próximo upload da fila
     */
    async function processNextUpload() {
        if (!state.isUploading || state.uploadQueue.length === 0) {
            checkUploadCompletion();
            return;
        }

        const fileData = state.uploadQueue.shift();
        state.activeUploads++;

        try {
            await uploadFile(fileData);
            state.completedUploads++;
            updateFileStatus(fileData, 'success');
        } catch (error) {
            state.failedUploads++;
            fileData.error = error.message || 'Erro no upload';
            updateFileStatus(fileData, 'error');
        }

        state.activeUploads--;
        updateOverallProgress();
        processNextUpload();
    }

    /**
     * Realiza o upload de um arquivo em chunks
     */
    async function uploadFile(fileData) {
        
        updateFileStatus(fileData, 'uploading');

        // 1. Iniciar upload
        const initResponse = await initUpload(fileData);
        fileData.uploadId = initResponse.UploadId;

        // 2. Enviar chunks
        let _chunkSize= 0;
        if (!initResponse.ChunkSize || parseInt(initResponse.ChunkSize) === 0)
            _chunkSize = config.chunkSize
        else
            _chunkSize = parseInt(initResponse.ChunkSize);
        let totalChunks = 0;
        if (!initResponse.TotalChunks || parseInt(initResponse.TotalChunks) === 0) {
            totalChunks = Math.ceil(fileData.size / _chunkSize);    
        } else {
            totalChunks = parseInt(initResponse.TotalChunks);
        }
        
        for (let chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
            if (!state.isUploading) {
                throw new Error('Upload cancelado');
            }
            const start = chunkIndex * _chunkSize;
            const end = Math.min(start + _chunkSize, fileData.size);
            const chunk = fileData.file.slice(start, end);

            await sendChunk(fileData.uploadId, chunk, chunkIndex, totalChunks);

            // Atualiza progresso
            const progress = Math.round(((chunkIndex + 1) / totalChunks) * 100);
            updateFileProgress(fileData, progress);
        }

        // 3. Finalizar upload
        await finalizeUpload(fileData.uploadId);
    }

    /**
     * Inicializa o upload na API
     */
    async function initUpload(fileData) {
        const response = await fetch(`${_contexto}File/JsonUploadInit`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                fileName: fileData.name,
                fileSize: fileData.size,
                contentType: fileData.type,
                applicationCode: state.currentApplication
            })
        });
        if (!response.ok) {
            throw new Error('Falha ao iniciar upload');
        }
        const result = await response.json();
        const _continue = Toast.checkType(result.Tipo, result.Erros, result.Mensagem);
        if (!_continue)
            throw new Error(Global.CheckMensagemArray(result.Erros, '\n'));
        return result.Model;
    }

    /**
     * Envia um chunk para a API
     */
    async function sendChunk(uploadId, chunk, chunkIndex, totalChunks) {
        const formData = new FormData();
        formData.append('uploadId', uploadId);
        formData.append('chunk', chunk);
        formData.append('chunkIndex', chunkIndex);
        formData.append('totalChunks', totalChunks);
        const response = await fetch(`${_contexto}File/JsonUploadChunk`, {
            method: 'POST',
            body: formData
        });
        if (!response.ok) {
            throw new Error(`Falha ao enviar chunk ${chunkIndex + 1}`);
        }
        const result = await response.json();
        const _continue = Toast.checkType(result.Tipo, result.Erros, '');
        if (!_continue)
            throw new Error(Global.CheckMensagemArray(result.Erros, '\n'));
        return result.Model;
    }

    /**
     * Finaliza o upload na API
     */
    async function finalizeUpload(uploadId) {
        const response = await fetch(`${_contexto}File/JsonUploadFinalize`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                uploadId: uploadId
            })
        });
        if (!response.ok) {
            throw new Error('Falha ao finalizar upload');
        }
        const result = await response.json();
        const _continue = Toast.checkType(result.Tipo, result.Erros, '');
        if (!_continue)
            throw new Error(Global.CheckMensagemArray(result.Erros, '\n'));
        return result.Model;
    }

    /**
     * Cancela um upload via Controller .NET
     */
    async function cancelUpload(uploadId) {
        const response = await fetch(`${_contexto}File/JsonUploadCancel`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                uploadId: uploadId
            })
        });

        if (!response.ok) {
            throw new Error('Erro ao cancelar upload');
        }
        const result = await response.json();
        const _continue = Toast.checkType(result.Tipo, result.Erros, '');
        if (!_continue)
            throw new Error(Global.CheckMensagemArray(result.Erros, '\n'));
        return result.Model;
    }

    /**
     * Cancela todos os uploads
     */
    async function cancelAllUploads() {
        state.isUploading = false;
        state.uploadQueue = [];
        // Cancela uploads ativos
        const activeFiles = state.files.filter(f => f.status === 'uploading' && f.uploadId);

        for (const fileData of activeFiles) {
            try {
                await cancelUpload(fileData.uploadId);
            } catch (e) {
                console.error('Erro ao cancelar upload:', e);
            }
            updateFileStatus(fileData, 'pending');
            updateFileProgress(fileData, 0);
        }
        resetUploadState();
        Toast.info('Uploads cancelados.');
    }

    /**
     * Verifica se todos os uploads foram concluídos
     */
    function checkUploadCompletion() {
        if (state.activeUploads === 0 && state.uploadQueue.length === 0) {
            resetUploadState();
            showResultModal();
        }
    }

    /**
     * Reseta o estado de upload
     */
    function resetUploadState() {
        state.isUploading = false;
        elements.btnStartUpload.prop('disabled', false);
        elements.btnCancelUpload.prop('disabled', true);
        elements.selectApplication.prop('disabled', false);
        updateButtonState();
    }

    /**
     * Exibe modal de resultado
     */
    function showResultModal() {
        const total = state.completedUploads + state.failedUploads;
        const $modal = $('#modalResult');
        const $icon = $('#modalResultIcon');
        const $title = $('#modalResultTitle');
        const $body = $('#modalResultBody');

        if (state.failedUploads === 0) {
            $icon.text('check_circle').removeClass('text-danger text-warning').addClass('text-success');
            $title.text('Upload Concluído');
            $body.html(`<p class="mb-0">${state.completedUploads} arquivo(s) enviado(s) com sucesso!</p>`);
        } else if (state.completedUploads === 0) {
            $icon.text('error').removeClass('text-success text-warning').addClass('text-danger');
            $title.text('Falha no Upload');
            $body.html(`<p class="mb-0">Nenhum arquivo foi enviado. ${state.failedUploads} erro(s).</p>`);
        } else {
            $icon.text('warning').removeClass('text-success text-danger').addClass('text-warning');
            $title.text('Upload Parcial');
            $body.html(`
                <p class="mb-2">${state.completedUploads} arquivo(s) enviado(s) com sucesso.</p>
                <p class="mb-0 text-danger">${state.failedUploads} arquivo(s) falharam.</p>
            `);
        }

        new bootstrap.Modal($modal).show();
    }

    /**
     * Atualiza o status visual de um arquivo
     */
    function updateFileStatus(fileData, status) {
        fileData.status = status;
        const $item = $(`.file-item[data-file-id="${fileData.id}"]`);
        
        $item.removeClass('uploading success error');
        
        const $status = $item.find('.file-item-status');
        $status.removeClass('pending uploading success error').addClass(status);

        const icons = {
            pending: 'schedule',
            uploading: 'sync',
            success: 'check_circle',
            error: 'error'
        };

        const $icon = $status.find('.material-icons');
        $icon.text(icons[status]);
        
        if (status === 'uploading') {
            $item.addClass('uploading');
            $icon.addClass('spin');
        } else {
            $icon.removeClass('spin');
            if (status === 'success') $item.addClass('success');
            if (status === 'error') $item.addClass('error');
        }

        // Esconde botão de remover durante upload
        $item.find('.btn-remove-file').toggle(status !== 'uploading');
    }

    /**
     * Atualiza o progresso de um arquivo
     */
    function updateFileProgress(fileData, progress) {
        fileData.progress = progress;
        const $item = $(`.file-item[data-file-id="${fileData.id}"]`);
        $item.find('.progress-bar').css('width', `${progress}%`);
        $item.find('.progress-text').text(`${progress}%`);
    }

    /**
     * Atualiza o progresso geral
     */
    function updateOverallProgress() {
        const total = state.completedUploads + state.failedUploads + state.uploadQueue.length + state.activeUploads;
        const completed = state.completedUploads + state.failedUploads;
        const percent = total > 0 ? Math.round((completed / total) * 100) : 0;

        elements.overallProgressBar.css('width', `${percent}%`);
        elements.overallProgressText.text(`${percent}%`);
        elements.uploadStats.text(`${completed} de ${total} arquivos processados`);
    }

    /**
     * Remove um arquivo da lista
     */
    function removeFile(fileId) {
        const index = state.files.findIndex(f => f.id === fileId);
        if (index > -1) {
            state.files.splice(index, 1);
            $(`.file-item[data-file-id="${fileId}"]`).fadeOut(200, function() {
                $(this).remove();
                updateFileListVisibility();
                updateButtonState();
            });
        }
    }

    /**
     * Limpa todos os arquivos
     */
    function clearAllFiles() {
        state.files = [];
        elements.fileList.empty();
        updateFileListVisibility();
        updateButtonState();
        elements.overallProgressContainer.addClass('d-none');
    }

    /**
     * Atualiza visibilidade da lista de arquivos
     */
    function updateFileListVisibility() {
        const hasFiles = state.files.length > 0;
        elements.fileListContainer.toggleClass('d-none', !hasFiles);
        elements.fileCount.text(state.files.length);
    }

    /**
     * Atualiza estado dos botões
     */
    function updateButtonState() {
        const hasValidFiles = state.files.some(f => !f.validation && f.status === 'pending');
        const hasApplication = !!state.currentApplication;
        
        elements.btnStartUpload.prop('disabled', !hasValidFiles || !hasApplication || state.isUploading);
    }

    // =========================================================================
    // Utilitários
    // =========================================================================

    function generateId() {
        return 'file_' + Math.random().toString(36).substr(2, 9);
    }

    function getFileExtension(filename) {
        return filename.split('.').pop() || '';
    }

    function formatBytes(bytes) {
        if (bytes === 0) return '0 MB';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    function getFileIcon(extension) {
        const ext = extension.toLowerCase();
        
        const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'];
        const docExts = ['doc', 'docx', 'txt', 'rtf', 'odt'];
        const videoExts = ['mp4', 'avi', 'mov', 'wmv', 'mkv', 'webm'];
        const audioExts = ['mp3', 'wav', 'ogg', 'flac', 'aac'];
        const archiveExts = ['zip', 'rar', '7z', 'tar', 'gz'];

        if (imageExts.includes(ext)) return { icon: 'image', class: 'image' };
        if (ext === 'pdf') return { icon: 'picture_as_pdf', class: 'pdf' };
        if (docExts.includes(ext)) return { icon: 'description', class: 'document' };
        if (videoExts.includes(ext)) return { icon: 'videocam', class: 'video' };
        if (audioExts.includes(ext)) return { icon: 'audiotrack', class: 'audio' };
        if (archiveExts.includes(ext)) return { icon: 'folder_zip', class: 'archive' };

        return { icon: 'insert_drive_file', class: '' };
    }

    // API pública
    return {
        init: init
    };

})();
