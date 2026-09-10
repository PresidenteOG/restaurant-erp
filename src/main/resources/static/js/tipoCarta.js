/**
 * Gestión de pestañas/secciones para la vista de la carta
 */
document.addEventListener('DOMContentLoaded', function() {
    const selector = document.getElementById('switchCarta');
    const secciones = document.querySelectorAll('.carta-section');

    function filtrarCartas() {
        const targetId = selector.value;

        secciones.forEach(section => {
            if (section.id === targetId) {
                section.style.display = 'block';
            } else {
                section.style.display = 'none';
            }
        });
        
        console.log("Mostrando sección: " + targetId);
    }

    if (selector) {
        selector.addEventListener('change', filtrarCartas);
        // Forzar la ejecución al cargar la página
        filtrarCartas();
    }
});