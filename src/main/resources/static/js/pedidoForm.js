// --- 1. INICIALIZACIÓN ---
document.addEventListener("DOMContentLoaded", function () {
    const contenedorRaiz = document.getElementById("lista-seleccionados");
    if (contenedorRaiz) {
        window.idxPlato = Number(contenedorRaiz.getAttribute("data-size-platos")) || 0;
        window.idxMenu = Number(contenedorRaiz.getAttribute("data-size-menus")) || 0;
    }
    conectarWebSocket(); // Conectar al cargar la página
});

// --- 2. GESTIÓN DEL TICKET ---
function agregarAlPedido(id, nombre, tipo) {
    const mensajeVacio = document.getElementById("mensaje-vacio");
    if (mensajeVacio) mensajeVacio.style.display = "none";

    const contenedor = document.getElementById("lista-seleccionados");
    const div = document.createElement("div");
    div.className = "item-seleccionado";

    let inputName = (tipo === "plato") ? "detallesPlatos" : "detallesMenus";
    let idName = (tipo === "plato") ? "plato.id" : "menu.id";
    let currentIdx = (tipo === "plato") ? window.idxPlato++ : window.idxMenu++;

    div.innerHTML = `
        <div style="flex-grow: 1;">
            <strong style="color: #064e3b; font-size: 0.9rem;">
                ${tipo === "menu" ? "🍱 " : ""}${nombre}
            </strong>
            <input type="hidden" name="${inputName}[${currentIdx}].${idName}" value="${id}" />
        </div>
        <div style="display: flex; align-items: center; gap: 10px;">
            <input type="number" name="${inputName}[${currentIdx}].cantidad" value="1" min="1" 
                class="input-field" style="width: 55px; padding: 5px; text-align: center; margin:0;" />
            <button type="button" onclick="this.parentElement.parentElement.remove()" 
                    style="background: #fee2e2; color: #ef4444; border: none; border-radius: 8px; padding: 5px 10px; cursor: pointer;">×</button>
        </div>
    `;
    contenedor.appendChild(div);
}

// --- 3. FILTROS ---
function filtrarCategoria(categoria, event) {
    document.querySelectorAll(".filter-btn").forEach(btn => btn.classList.remove("active"));
    if (event) event.currentTarget.classList.add("active");

    const productos = document.querySelectorAll(".plato-btn");
    let platosVisible = false, menusVisible = false;

    productos.forEach(prod => {
        const cat = prod.getAttribute("data-categoria");
        const esMenu = prod.classList.contains("menu-btn");
        const visible = (categoria === "todos" || cat === categoria);
        prod.style.display = visible ? "flex" : "none";
        if (visible) { esMenu ? menusVisible = true : platosVisible = true; }
    });

    const headers = document.querySelectorAll(".section-header");
    if (headers.length >= 2) {
        headers[0].style.display = platosVisible ? "flex" : "none";
        headers[1].style.display = menusVisible ? "flex" : "none";
    }
}

// --- 4. WEBSOCKET Y ACTUALIZACIÓN PARCIAL ---
var stompClient = null;

function conectarWebSocket() {
    var socket = new SockJS("/ws-restaurantus");
    stompClient = Stomp.over(socket);
    stompClient.debug = null; 

    stompClient.connect({}, function (frame) {
        console.log("✅ WebSocket Formulario Pedido: Conectado");
        stompClient.subscribe("/topic/pedidos", function (msg) {
            actualizarSoloMesas();
        });
    }, function (error) {
        setTimeout(conectarWebSocket, 5000);
    });
}

function actualizarSoloMesas() {
    const selectPrevio = document.querySelector('select[name="mesa.idMesa"]');
    const valorSeleccionado = selectPrevio ? selectPrevio.value : "";

    fetch("/pedidos/fragmento-mesas")
        .then((response) => {
            if (!response.ok) {
                throw new Error("El servidor devolvió un error 500");
            }
            return response.text();
        })
        .then((html) => {
            const contenedor = document.getElementById("mesa-selector-container");
            if (contenedor) {
                // Usamos outerHTML para reemplazar el div completo y mantener los IDs limpios
                contenedor.outerHTML = html;

                // Esperamos un milisegundo a que el DOM se asiente y restauramos el valor
                setTimeout(() => {
                    const nuevoSelect = document.querySelector('select[name="mesa.idMesa"]');
                    if (valorSeleccionado && nuevoSelect) {
                        nuevoSelect.value = valorSeleccionado;
                    }
                }, 10);
            }
        })
        .catch((err) => console.error("Error crítico actualizando mesas:", err));
}