Dashboard = {
    InitDashboard: function () {
        Dashboard.ListStorages();
        Dashboard.ListApplications();
    }, 
    ListStorages: function () {
        fetch(`${_contexto}Dashboard/ListAllStorages`, {
            method: 'GET'
        }).then(r => {
            if (!r.ok) {
                Toast.error("Erro não esperado do servidor para listar os Storages.");
                return;
            }
            return r.json();
        }).then(result => {
            const _continue = Toast.checkType(result.Tipo, result.Erros, result.Mensagem);
            if (!_continue) return;
            $("div.lista-storages").html(result.Model);
            Dashboard.InitRemovalStorage();
        }).catch(err => {
            Toast.error(err.mensage || "Erro ao listar os storages ativos.");
        });
    },
    ListApplications: function () {
        fetch(`${_contexto}Dashboard/ListAllApplications`, {
            method: 'GET'
        }).then(r => {
            if (!r.ok) {
                Toast.error("Erro não esperado do servidor para listar as aplicações.");
                return;
            }
            return r.json();
        }).then(result => {
            const _continue = Toast.checkType(result.Tipo, result.Erros, result.Mensagem);
            if (!_continue) return;
            $("div.lista-aplicacoes").html(result.Model);
        }).catch(err => {
            Toast.error(err.mensage || "Erro ao listar todas as aplicações.");
        });
    },
    InitRemovalStorage: function () {
        const modelConfirmRemovalStorage = $("#modalConfirmRemovalStorage");
        if (!modelConfirmRemovalStorage) return;
        const btnConfirmRemovalStorage = $("button.confirm-removal-storage");
        btnConfirmRemovalStorage.click(function (){
            const inputIdStorage = $("input[name='idStorage']");
            if (!inputIdStorage) return;
            let id_storage = inputIdStorage.val();
            Dashboard.RemovalStorage(id_storage);
        });
        $("button.removal-storage").click(function (){
            let id_storage = $(this).data("id_storage");
            const inputIdStorage = $("input[name='idStorage']");
            if (!inputIdStorage) return;
            inputIdStorage.val(id_storage);
            modelConfirmRemovalStorage.modal('show');
        })
    },
    RemovalStorage: function (idStorage) {
        if (!idStorage || idStorage === '0')
            return;
        fetch(`${_contexto}Storage/RemoveStorage?idServerStorage=${idStorage}`, {
            method: 'DELETE'
        }).then(r => {
            if (!r.ok) {
                Toast.error("Erro não esperado do servidor ao remover Storage.");
                return;
            }
            return r.json();
        }).then(result => {
            const _continue = Toast.checkType(result.Tipo, result.Erros, result.Mensagem);
            if (!_continue) return;
            const modelConfirmRemovalStorage = $("#modalConfirmRemovalStorage");
            if (!modelConfirmRemovalStorage) return;
            modelConfirmRemovalStorage.modal('hide');
            Dashboard.ListStorages();
        }).catch(err => {
            Toast.error(err.mensage || "Erro ao remover o Server Storage.");
        });
    }
}
$(function () {
    Dashboard.InitDashboard();
});