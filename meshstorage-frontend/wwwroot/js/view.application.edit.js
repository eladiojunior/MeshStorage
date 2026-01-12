let listFileContentType = [];
let listContentTypeSelected = [];
Application_Edit = {
    InitEdit: function () {
        Application_Edit.ListContentTypes();
        Application_Edit.MaxFileSize();
        Application_Edit.InitFilterContentType();
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
            Application_Edit.LoadListContentTypeSelected();
            Application_Edit.LoadListContentType();
        }).catch(err => {
            Toast.error(err.mensage || "Erro ao listar os tipos de arquivos.");
        });
    },
    LoadListContentTypeSelectedByInput: function () {
        const textContentTypes= $("#AllowedFileTypes").val();
        if (!textContentTypes.trim()) return;
        listContentTypeSelected = textContentTypes.split(';');
        $("div.lista-itens-selected").empty();
        listContentTypeSelected.forEach(item=> {
            Application_Edit.LoadContentTypeSelected(item);
        });
    },
    LoadListContentTypeSelected: function () {
        $("div.lista-itens-selected").empty();
        const $inputAllowedFileTypes = $("#AllowedFileTypes");
        const $spanCountContentType = $("span.count-content-types"); 
        $inputAllowedFileTypes.val('');
        $spanCountContentType.text('0');
        //Verificar se a lista está vazia!
        if (!listContentTypeSelected || listContentTypeSelected.length === 0)
            return;
        $inputAllowedFileTypes.val(listContentTypeSelected.join(';'));
        $spanCountContentType.text(listContentTypeSelected.length+'');
        listContentTypeSelected.forEach(item=> {
            Application_Edit.LoadContentTypeSelected(item);
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
            Application_Edit.LoadListContentTypeSelected();
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
            Application_Edit.LoadListContentTypeSelected();
        });
    },
    MaxFileSize: function () {
        const $fieldMaxFileSizeMB = $("#MaximumFileSizeMB");
        const $spanMaxFileSizeMB = $("span.maximum-file-size");
        //atualizar o texto recuperado.
        const maxFile = $fieldMaxFileSizeMB.val() + " MB";
        $spanMaxFileSizeMB.text(maxFile);
        //Criar evento para atualizar quando mudar...
        $fieldMaxFileSizeMB.on('change', function (){
            let result = $(this).val() + " MB";
            $spanMaxFileSizeMB.text(result);
        });
    },
    InitFilterContentType: function () {
        $("input.filtro-lista").on('keyup', function (event) {
            const value = $(this).val();
            const filter = value.toLowerCase();
            Application_Edit.LoadListContentType(filter);
        });
    }
}
$(function () {
    Application_Edit.InitEdit();
    Application_Edit.LoadListContentTypeSelectedByInput();
});