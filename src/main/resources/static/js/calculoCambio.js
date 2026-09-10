document.addEventListener('DOMContentLoaded', function() {
    const totalElement = document.getElementById('totalComanda');
    const inputEntregado = document.getElementById('inputEntregado');
    const textoCambio = document.getElementById('textoCambio');
    const btnEfectivo = document.getElementById('btnEfectivo');
    const inputCambioOculto = document.getElementById('inputCambioOculto');

    const totalPedido = parseFloat(totalElement.getAttribute('data-total'));

    inputEntregado.addEventListener('input', function() {
        const entregado = parseFloat(this.value) || 0;
        const cambio = entregado - totalPedido;

        if (cambio >= 0) {
            textoCambio.innerText = cambio.toFixed(2).replace('.', ',') + '€';
            textoCambio.style.color = '#10b981';
            
            // ACTUALIZACIÓN: Guardamos el valor para el controlador
            inputCambioOculto.value = cambio.toFixed(2);
            
            btnEfectivo.style.opacity = "1";
            btnEfectivo.style.pointerEvents = "auto";
            btnEfectivo.disabled = false;
        } else {
            textoCambio.innerText = 'Faltan: ' + Math.abs(cambio).toFixed(2).replace('.', ',') + '€';
            textoCambio.style.color = '#ef4444';
            
            // Si falta dinero, reseteamos el cambio a 0
            inputCambioOculto.value = "0.00";
            
            btnEfectivo.style.opacity = "0.5";
            btnEfectivo.style.pointerEvents = "none";
            btnEfectivo.disabled = true;
        }
    });
});

// Función que dispara el submit (la que llaman tus botones)
function enviarFormulario(metodo) {
    if (confirm('¿Confirmar cobro en ' + metodo + '?')) {
        document.getElementById('metodoPago').value = metodo;
        const form = document.getElementById('formCobro');
        form.submit();

        setTimeout(function() {
            window.location.href = "/finanzas/pendientesCobro";
        }, 1200);
    }
}