let listFileContentType = [];
let listContentTypeSelected = [];
Application_Create = {
    InitCreate: function () {
        Application_Create.ListContentTypes();
        Application_Create.MaxFileSize();
        Application_Create.InitFilterContentType();
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
            Application_Create.LoadListContentTypeSelected();
            Application_Create.LoadListContentType();
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
            Application_Create.LoadContentTypeSelected(item);
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
            Application_Create.LoadContentTypeSelected(item);
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
            Application_Create.LoadListContentTypeSelected();
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
            Application_Create.LoadListContentTypeSelected();
        });
    },
    MaxFileSize: function () {
        $("#MaximumFileSizeMB").on('change', function (){
            let result = $(this).val() + " MB";
            $("span.maximum-file-size").text(result);
        });
    },
    InitFilterContentType: function () {
        $("input.filtro-lista").on('keyup', function (event) {
            const value = $(this).val();
            const filter = value.toLowerCase();
            Application_Create.LoadListContentType(filter);
        });
    }
}
$(function () {
    Application_Create.InitCreate();
    Application_Create.LoadListContentTypeSelectedByInput();
});