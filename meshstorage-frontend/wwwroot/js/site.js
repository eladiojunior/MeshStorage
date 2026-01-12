Global = {
    ExibirMensagem: function (msg, hasErro) {

        if (!msg || msg === "") return;
        var alert = $(".mensagens .alert");
        if (alert.length > 0) {
            var msgAlert = msg;
            if ((Object.prototype.toString.call(msg) === '[object Array]')) {
                msgAlert = "";
                for (var i = 0; i < msg.length; i++) {
                    if (msgAlert !== "") msgAlert += '<br/>';
                    msgAlert += msg[i];
                }
            }
            alert.html(msgAlert);
            alert.removeClass("alert-danger").removeClass("alert-info");
            alert.addClass(hasErro ? "alert-danger" : "alert-info");
            $(".mensagens").show();
            $("html,body").scrollTop(0);
        }
    }
    
}
