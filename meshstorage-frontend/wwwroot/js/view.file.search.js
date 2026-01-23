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
    }
}
$(function () {
    File_Search.InitSearch();
    File_Search.LoadListContentTypeSelectedByInput();
});