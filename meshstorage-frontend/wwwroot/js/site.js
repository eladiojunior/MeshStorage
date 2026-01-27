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
    },
    TimeInMinutesFormat: function (totalMinutes) {

        if (totalMinutes <= 0)
            return "0 minutos";

        const minutesPerHour = 60;
        const minutesPerDay = 1440;      // 60 * 24
        const minutesPerYear = 525600;   // 60 * 24 * 365

        const years = Math.floor(totalMinutes / minutesPerYear);
        let remaining = totalMinutes % minutesPerYear;

        const days = Math.floor(remaining / minutesPerDay);
        remaining %= minutesPerDay;

        const hours = Math.floor(remaining / minutesPerHour);
        const minutes = remaining % minutesPerHour;

        const parts = [];

        if (years > 0)
            parts.push(`${years} ${years === 1 ? "ano" : "anos"}`);

        if (days > 0)
            parts.push(`${days} ${days === 1 ? "dia" : "dias"}`);

        if (hours > 0)
            parts.push(`${hours} ${hours === 1 ? "hora" : "horas"}`);

        if (minutes > 0)
            parts.push(`${minutes} ${minutes === 1 ? "minuto" : "minutos"}`);

        return parts.join(" ");
    },
    CheckMensagemArray: function (message) {
        let _result = '';
        // Mensagem (array ou string)
        if (Array.isArray(message))
            _result = message.join('\n');
        else 
            _result = message.trim();
        return _result;
    }
    
}
