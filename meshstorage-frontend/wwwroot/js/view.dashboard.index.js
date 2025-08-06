Dashboard = {
    InitDashboard: function () {
        Dashboard.ListStorages();
        Dashboard.ListApplications();
    }, 
    ListStorages: function () {
        $.ajax({
            cache: false,
            type: "GET",
            url: _contexto + "Dashboard/ListAllStorages",
            dataType: "json",
            success: function (result) {
                if (result.HasErro) {
                    Global.ExibirMensagem(result.Erros, true);
                    return;
                }
                $("div.lista-storages").html(result.Model);
            }, error: function (XMLHttpRequest, textStatus, errorThrown) {
                alert(errorThrown);
            }
        });
    },
    ListApplications: function () {
        $.ajax({
            cache: false,
            type: "GET",
            url: _contexto + "Dashboard/ListAllApplications",
            dataType: "json",
            success: function (result) {
                if (result.HasErro) {
                    Global.ExibirMensagem(result.Erros, true);
                    return;
                }
                $("div.lista-aplicacoes").html(result.Model);
            }, error: function (XMLHttpRequest, textStatus, errorThrown) {
                alert(errorThrown);
            }
        });
    }
}
$(function () {
    Dashboard.InitDashboard();
});