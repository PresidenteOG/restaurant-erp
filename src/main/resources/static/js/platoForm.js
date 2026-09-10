/**
 * Gestión dinámica de categorías y subcategorías para el formulario de platos
 */
document.addEventListener('DOMContentLoaded', function() {

    // --- LÓGICA DE CATEGORÍAS Y SUBCATEGORÍAS ---
    const selectorCat = document.getElementById('selectorCategoria');
    const selectorSub = document.getElementById('selectorSubcategoria');

    // Función para filtrar subcategorías
    function filtrarSubcategorias() {
        const categoriaSeleccionada = selectorCat.value;
        const opciones = selectorSub.querySelectorAll('option');

        opciones.forEach(opt => {
            // Saltamos la opción por defecto ("Seleccione primero...")
            if (!opt.dataset.parent) return; 
            
            if (opt.dataset.parent === categoriaSeleccionada) {
                opt.style.display = 'block';
                opt.disabled = false;
            } else {
                opt.style.display = 'none';
                opt.disabled = true;
            }
        });

        // Si la subcategoría actualmente seleccionada no pertenece a la nueva categoría, reseteamos
        const opcionSeleccionada = selectorSub.options[selectorSub.selectedIndex];
        if (opcionSeleccionada && opcionSeleccionada.dataset.parent !== categoriaSeleccionada) {
            selectorSub.value = "";
        }
    }

    // Escuchar cambios en el selector de categoría
    if (selectorCat) {
        selectorCat.addEventListener('change', filtrarSubcategorias);
        
        // Ejecutar al cargar por si estamos en modo "Editar" y ya hay una categoría
        if (selectorCat.value) {
            filtrarSubcategorias();
        }
    }

    // --- LÓGICA DE PLATOS ESPECIALES ---
    const checkEspecial = document.getElementById('checkEspecial');
    const seccionFechas = document.getElementById('seccionFechas');

    if (checkEspecial && seccionFechas) {
        checkEspecial.addEventListener('change', function() {
            if (this.checked) {
                seccionFechas.style.display = 'block';
            } else {
                seccionFechas.style.display = 'none';
                // Limpiamos las fechas si se desmarca para evitar datos basura
                seccionFechas.querySelectorAll('input').forEach(i => i.value = '');
            }
        });
    }

    // --- VISTA PREVIA DE IMAGEN ---
    const inputImagen = document.getElementById('imageFile');
    const imgPreview = document.getElementById('preview');

    if (inputImagen && imgPreview) {
        inputImagen.addEventListener('change', function(evt) {
            const [file] = this.files;
            if (file) {
                imgPreview.src = URL.createObjectURL(file);
            }
        });
    }

    // --- VALIDACIÓN DE LÍMITE DE INGREDIENTES (19 máximo) ---
    const LIMITE_INGREDIENTES = 18;
    const inputsCantidad = document.querySelectorAll('input[name*="cantidad_necesaria"]');
    
    // Función para contar ingredientes con cantidad válida
    function contarIngredientesActivos() {
        let contador = 0;
        inputsCantidad.forEach(input => {
            const valor = parseFloat(input.value);
            if (valor && valor > 0) {
                contador++;
            }
        });
        return contador;
    }

    // Añadir listener a cada input de cantidad
    inputsCantidad.forEach(input => {
        input.addEventListener('input', function() {
            const valor = parseFloat(this.value);
            
            // Si el usuario está intentando añadir cantidad a un nuevo ingrediente
            if (valor && valor > 0) {
                const totalActivos = contarIngredientesActivos();
                
                // Si excede el límite, alertar y limpiar el campo
                if (totalActivos > LIMITE_INGREDIENTES) {
                    alert(`⚠️ No se pueden añadir más de ${LIMITE_INGREDIENTES} ingredientes a un plato.\n\nActualmente tienes ${LIMITE_INGREDIENTES} ingredientes seleccionados.`);
                    this.value = '';
                    this.focus();
                }
            }
        });
    });

    // --- OPTIMIZACIÓN DE ENVÍO DE INGREDIENTES ---
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function(e) {
            // Verificar límite antes de enviar
            const totalActivos = contarIngredientesActivos();
            if (totalActivos > LIMITE_INGREDIENTES) {
                e.preventDefault();
                alert(`⚠️ Error: No se puede guardar el plato.\n\nTienes ${totalActivos} ingredientes seleccionados, pero el límite es ${LIMITE_INGREDIENTES}.\n\nPor favor, elimina ${totalActivos - LIMITE_INGREDIENTES} ingrediente(s) antes de guardar.`);
                return false;
            }

            // Seleccionamos todos los inputs de cantidad de ingredientes
            inputsCantidad.forEach(input => {
                const valor = parseFloat(input.value);
                
                // Si el usuario NO ha puesto una cantidad válida
                if (!valor || valor <= 0) {
                    // Deshabilitamos el input de cantidad
                    input.disabled = true;
                    
                    // Extraemos el índice del nombre: ingredientes[0].cantidad_necesaria -> 0
                    const match = input.name.match(/ingredientes\[(\d+)\]/);
                    if (match) {
                        const index = match[1];
                        // Buscamos el input hidden correspondiente por su name
                        const inputId = document.querySelector(`input[name="ingredientes[${index}].ingrediente.id"]`);
                        if (inputId) {
                            inputId.disabled = true;
                        }
                    }
                }
            });
            
            // El formulario se enviará solo con los ingredientes que tengan cantidad > 0
            return true;
        });
    }
});