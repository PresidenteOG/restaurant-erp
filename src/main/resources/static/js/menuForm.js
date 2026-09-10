// Script para cambiar entre categorías
        document.addEventListener('DOMContentLoaded', function() {
            const filtro = document.getElementById('filtroCategoria');
            const secciones = document.querySelectorAll('.categoria-section');

            filtro.addEventListener('change', function() {
                const categoriaSeleccionada = this.value;
                
                // Ocultar todas las secciones
                secciones.forEach(seccion => {
                    seccion.style.display = 'none';
                });
                
                // Mostrar solo la sección seleccionada
                const seccionActiva = document.getElementById('categoria-' + categoriaSeleccionada);
                if (seccionActiva) {
                    seccionActiva.style.display = 'block';
                }
            });
        });