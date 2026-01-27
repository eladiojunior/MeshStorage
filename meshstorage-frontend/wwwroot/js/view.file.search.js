let listFileContentType = [];
let listContentTypeSelected = [];
File_Search = {
    InitSearch: function () {
        File_Search.ListContentTypes();
        File_Search.InitFilterContentType();
    },
    ListContentTypes: function () {
        fetch(`${_contexto}Application/ListFileContentTypes`, {
            method: 'GET'
        }).then(r => {
            if (!r.ok) {
                Toast.error("Erro não esperado do servidor para listar os tipos de arquivos.");
                return;
            }
            return r.json();
        }).then(result => {
            const _continue = Toast.checkType(result.Tipo, result.Erros, result.Mensagem);
            if (!_continue) return;
            listFileContentType = result.Model;
            File_Search.LoadListContentTypeSelected();
            File_Search.LoadListContentType();
        }).catch(err => {
            Toast.error(err.mensage || "Erro ao listar os tipos de arquivos.");
        });
    },
    LoadListContentTypeSelectedByInput: function () {
        const textContentTypes= $("#Filter_FileContentType").val();
        if (!textContentTypes.trim()) return;
        listContentTypeSelected = textContentTypes.split(';');
        $("div.lista-itens-selected").empty();
        listContentTypeSelected.forEach(item=> {
            File_Search.LoadContentTypeSelected(item);
        });
    },
    LoadListContentTypeSelected: function () {
        $("div.lista-itens-selected").empty();
        const $inputFileContentTypes = $("#Filter_FileContentType");
        const $spanCountContentType = $("span.count-content-types");
        $inputFileContentTypes.val('');
        $spanCountContentType.text('0');
        //Verificar se a lista está vazia!
        if (!listContentTypeSelected || listContentTypeSelected.length === 0)
            return;
        $inputFileContentTypes.val(listContentTypeSelected.join(';'));
        $spanCountContentType.text(listContentTypeSelected.length+'');
        listContentTypeSelected.forEach(item=> {
            File_Search.LoadContentTypeSelected(item);
        });
    },
    LoadContentTypeSelected: function (contentType) {
        if (!contentType.trim()) return;
        const itemContentType = listFileContentType.find(item => item.ContentType === contentType);
        const $divListItensSelected = $("div.lista-itens-selected");
        const $label = $(`
          <label class="item-content-type-selected list-group-item d-flex justify-content-between align-items-start" data-item="${itemContentType.ContentType}">
            <div class="ms-2 me-auto">
                <div class="fw-bold">${itemContentType.ContentType}</div>
                ${itemContentType.Description}
            </div>
            <span class="badge bg-primary">${itemContentType.Extension}</span>
          </label>
        `);
        $divListItensSelected.append($label);
        $("label.item-content-type-selected").on('click', function (){
            let contentType = $(this).data('item');
            listContentTypeSelected = listContentTypeSelected.filter(function (item){
                return item !== contentType;
            });
            $("label.item-content-type").filter(function() {
                const itemContentType = $(this).data('item');
                if (itemContentType === contentType)
                    $(this).removeClass('visually-hidden');
            });
            File_Search.LoadListContentTypeSelected();
        });
    },
    LoadListContentType: function (filter) {
        const $divListItens = $("div.lista-itens").empty();
        listFileContentType.forEach(item => {
            let isSelected = listContentTypeSelected.includes(item.ContentType);
            if (!isSelected && filter!==undefined && filter !== "")
            {//Verificar filtro
                isSelected = !(item.ContentType.toLowerCase().includes(filter) ||
                    item.Extension.toLowerCase().includes(filter) ||
                    item.Description.toLowerCase().includes(filter));
            }
            const $label = $(`
              <label class="item-content-type list-group-item d-flex justify-content-between align-items-start${isSelected?' visually-hidden':''}" data-item="${item.ContentType}">
                <div class="ms-2 me-auto">
                    <div class="fw-bold">${item.ContentType}</div>
                    ${item.Description}
                </div>
                <span class="badge bg-primary">${item.Extension}</span>
              </label>
            `);
            $divListItens.append($label);
        });
        $("label.item-content-type").on('click', function (){
            let contentType = $(this).data('item');
            if (!contentType.trim()) return;
            listContentTypeSelected.push(contentType);
            $(this).addClass('visually-hidden');
            File_Search.LoadListContentTypeSelected();
        });
    },
    InitFilterContentType: function () {
        $("input.filtro-lista").on('keyup', function (event) {
            const value = $(this).val();
            const filter = value.toLowerCase();
            File_Search.LoadListContentType(filter);
        });
    },
    InitRemoveFile: function () {
        const modalConfirmRemovalFile = $("#modalConfirmRemovalFile");
        if (!modalConfirmRemovalFile) return;
        $("button.remove-file").click(function (){
            let id_file = $(this).data("id_file");
            const inputIdFile = $("input[name='idFileRemove']");
            if (!inputIdFile) return;
            inputIdFile.val(id_file);
            const name_file = $(this).data("name_file");
            const spanNameFile = $("span.name-file-remove");
            if (spanNameFile) spanNameFile.text(name_file)
            modalConfirmRemovalFile.modal('show');
        });
        const btnConfirmRemovalFile = $("button.confirm-removal-file");
        btnConfirmRemovalFile.click(function (){
            const inputIdFile = $("input[name='idFileRemove']");
            if (!inputIdFile) return;
            let id_file = inputIdFile.val();
            File_Search.RemoveFile(id_file);
        });
        
    },
    RemoveFile: function (idFile) {
        if (!idFile || idFile === '')
            return;
        fetch(`${_contexto}File/JsonRemoveFile?idFile=${idFile}`, {
            method: 'DELETE'
        }).then(r => {
            if (!r.ok) {
                Toast.error("Erro não esperado do servidor ao remover arquivo.");
                return;
            }
            return r.json();
        }).then(result => {
            const _continue = Toast.checkType(result.Tipo, result.Erros, result.Mensagem);
            if (!_continue) return;
            const modalConfirmRemovalFile = $("#modalConfirmRemovalFile");
            if (!modalConfirmRemovalFile) return;
            modalConfirmRemovalFile.modal('hide');
            const rowFile = $("#"+idFile);
            if (!rowFile) return;
            rowFile.remove();
        }).catch(err => {
            Toast.error(err.mensage || "Erro ao remover arquivo do Server Storage.");
        });
    },
    InitDownlodFile: function () {
        $("button.download-file").click(function (){
            const idFile = $(this).data("id_file");
            if (!idFile || idFile === '') return;
            window.location.href = `${_contexto}File/DownloadFile?idFile=${idFile}`;
        })
    },
    InitGenerateQrCodeFile: function () {
        const modalGenerateQrCodeFile = $("#modalGenerateQrCodeFile");
        if (!modalGenerateQrCodeFile) return;
        const btnQrCodeFile = $("button.qrcode-file");
        if (!btnQrCodeFile) return;
        btnQrCodeFile.click(function (){
            const id_file = $(this).data("id_file");
            const inputIdFile = $("input[name='idFileQrCode']");
            if (!inputIdFile) return;
            if (id_file !== inputIdFile.val()) {
                //Limpar o QRCode
                $("div.result-generate-qrcode").html('<div class="alert alert-info">Informe os ' +
                    'parametros acima e clique em "Gerar" para criar o link e imagem QR Code para ' +
                    'acesso ao arquivo.</div>');
            }
            inputIdFile.val(id_file);
            const name_file = $(this).data("name_file");
            const spanNameFile = $("span.name-file-qrcode");
            if (spanNameFile) spanNameFile.text(name_file)
            modalGenerateQrCodeFile.modal('show');
        });
        const btnGenerateQrCodeFile = $("button.generate-qrcode-file");
        btnGenerateQrCodeFile.click(function (){
            const inputIdFile = $("input[name='idFileQrCode']");
            if (!inputIdFile) return;
            let id_file = inputIdFile.val();
            File_Search.GenerateQrCodeFile(id_file);
        });
        File_Search.TokenExpirationTime();
        File_Search.MaximumAccessesToken();
    },
    GenerateQrCodeFile: function (idFile) {
        if (!idFile || idFile === '')
            return;
        const tokenExpirationTime = $('#tokenExpirationTime').val();
        const maximumAccessesToken = $('#maximumAccessesToken').val();
        fetch(`${_contexto}File/JsonGenerateQrCodeFile`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                idFile: idFile,
                tokenExpirationTime: tokenExpirationTime,
                maximumAccessesToken: maximumAccessesToken
            })
        }).then(r => {
            if (!r.ok) {
                Toast.error("Erro não esperado do servidor ao gerar QrCode e link para o arquivo.");
                return;
            }
            return r.json();
        }).then(result => {
            const _continue = Toast.checkType(result.Tipo, result.Erros, result.Mensagem);
            if (!_continue) return;
            $("div.result-generate-qrcode").html(result.Model);
            File_Search.InitCopiarLinkFile();
        }).catch(err => {
            Toast.error(err.mensage || "Erro ao gerar QrCode e link de acesso ao arquivo.");
        });
    },
    InitCopiarLinkFile: function () {
        $('#btnCopiarLink').on('click', function() {
            const link = $('#downloadLink').val();
            navigator.clipboard.writeText(link).then(function() {
                Toast.success("Link copiado!");
            });
        });
    },
    TokenExpirationTime: function () {
        $("#tokenExpirationTime").on('change', function (){
            let result = 'Nunca expira';
            const tokenExpirationTime = parseInt($(this).val());
            switch (tokenExpirationTime) {
                case 0:
                    result = 'Nunca expira';
                    break;
                case 1:
                    result = 'Expira em 1 minuto';
                    break;
                default:
                    result = 'Expira em ' + Global.TimeInMinutesFormat(tokenExpirationTime);
                    break;
            }
            $("span.token-expiration-time").text(result);
        });
    },
    MaximumAccessesToken: function () {
        $("#maximumAccessesToken").on('change', function (){
            let result = 'Nunca expira';
            const maximumAccessesToken = parseInt($(this).val());
            switch (maximumAccessesToken) {
                case 0:
                    result = 'Não tem limite';
                    break;
                case 1:
                    result = 'Apenas 1 acesso';
                    break;
                default:
                    result = maximumAccessesToken + ' acessos';
                    break;
            }
            $("span.maximum-accesses-token").text(result);
        });
    },
}
$(function () {
    File_Search.InitSearch();
    File_Search.LoadListContentTypeSelectedByInput();
    File_Search.InitRemoveFile();
    File_Search.InitDownlodFile();
    File_Search.InitGenerateQrCodeFile();
});