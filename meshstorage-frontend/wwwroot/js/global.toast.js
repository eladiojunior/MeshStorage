window.Toast = (function () {

    let toastEl;
    let toastBody;
    let toastInstance;

    function init() {
        toastEl = document.getElementById('appToast');
        toastBody = document.getElementById('appToastBody');

        if (!toastEl || !toastBody) {
            console.warn('Toast container não encontrado.');
            return;
        }

        toastInstance = new bootstrap.Toast(toastEl, {
            delay: 5000,   // tempo padrão (ms)
            autohide: true
        });
    }

    function show(message, type = 'info', delay = 5000) {
        if (!toastInstance) init();

        if (!toastInstance) {
            alert(message);
            return;
        }

        // Limpa classes antigas
        toastEl.classList.remove(
            'bg-success',
            'bg-danger',
            'bg-warning',
            'bg-info'
        );

        // Define cor
        switch (type) {
            case 'success':
                toastEl.classList.add('bg-success');
                break;
            case 'error':
                toastEl.classList.add('bg-danger');
                break;
            case 'warning':
                toastEl.classList.add('bg-warning');
                toastEl.classList.add('text-dark');
                break;
            default:
                toastEl.classList.add('bg-info');
        }

        // Mensagem (array ou string)
        if (Array.isArray(message)) {
            toastBody.innerHTML = message.join('<br/>');
        } else {
            toastBody.innerHTML = message;
        }

        toastInstance._config.delay = delay;
        toastInstance.show();
    }

    function showByType(tipo, erros, mensagem) {
        let _continue = true;
        switch (tipo) {
            case 'ERROR':
                Toast.error(erros);
                _continue = false;
                break;
            case 'ALERT':
                Toast.warning(mensagem);
                _continue = false;
                break;
            case 'INFO':
                if (!mensagem && mensagem !== '')
                    Toast.info(mensagem);
                _continue = true;
                break;
        }
        return _continue;
    }
    
    return {
        success: (msg, delay) => show(msg, 'success', delay),
        error: (msg, delay) => show(msg, 'error', delay),
        warning: (msg, delay) => show(msg, 'warning', delay),
        info: (msg, delay) => show(msg, 'info', delay),
        checkType: (type, errors, msg) => showByType (type, errors, msg),
    };
})();