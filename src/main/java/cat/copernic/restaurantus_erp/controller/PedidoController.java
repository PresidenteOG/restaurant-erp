package cat.copernic.restaurantus_erp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cat.copernic.restaurantus_erp.domain.EstadoPedido;
import cat.copernic.restaurantus_erp.domain.Mesa;   // <--- IMPORTADO
import cat.copernic.restaurantus_erp.domain.Pedido; // <--- IMPORTADO
import cat.copernic.restaurantus_erp.services.carta.CartaService;
import cat.copernic.restaurantus_erp.services.cocina.CocinaService;
import cat.copernic.restaurantus_erp.services.mesa.MesaService;
import cat.copernic.restaurantus_erp.services.pedido.PedidoService;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private CocinaService cocinaService;
    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private CartaService cartaService;
    @Autowired
    private MesaService mesaService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 1. VISTA GENERAL
    @GetMapping("/listado")
    public String vistaGeneral(Model model) {
        model.addAttribute("pedidosCocina", cocinaService.listarPedidosCocina());
        model.addAttribute("listos", pedidoService.buscarPorEstado(EstadoPedido.LISTO));
        model.addAttribute("enPago", pedidoService.buscarPorEstado(EstadoPedido.PAGANDO));
        model.addAttribute("historial", pedidoService.buscarPorEstado(EstadoPedido.SERVIDO));
        model.addAttribute("cancelados", pedidoService.buscarPorEstado(EstadoPedido.CANCELADO));
        model.addAttribute("activePage", "orders");
        return "pedido/listadoPedidos";
    }

    // 2. FORMULARIO NUEVO
    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("pedido", new Pedido());
        model.addAttribute("cartaPlatos", cartaService.listarPlatos());
        model.addAttribute("cartaMenus", cartaService.listarMenus());
        model.addAttribute("listaMesas", mesaService.listarMesasParaNuevoPedido());
        return "pedido/formPedido";
    }

    // 3. GUARDAR
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Pedido pedido) {
        pedidoService.guardarPedido(pedido);
        messagingTemplate.convertAndSend("/topic/pedidos", "actualizar");
        return "redirect:/pedidos/listado";
    }

    @GetMapping("/pedidoReserva/{id}")
    public String abrirDesdeReserva(@PathVariable("id") Long idReserva, Model model) {
        Pedido pedidoCreado = pedidoService.crearPedidoDesdeReserva(idReserva);
        return "redirect:/pedidos/editar/" + pedidoCreado.getId();
    }

    // 4. EDITAR
    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoService.buscarPorId(id);
        model.addAttribute("pedido", pedido);
        model.addAttribute("cartaPlatos", cartaService.listarPlatos());
        model.addAttribute("cartaMenus", cartaService.listarMenus());
        model.addAttribute("listaMesas", mesaService.findAllMesas());
        return "pedido/formPedido";
    }

    // 5. ACCIONES DE ESTADO
    @GetMapping("/cancelar/{id}")
    public String cancelarPedidoEntero(@PathVariable Long id) {
        pedidoService.cambiarEstado(id, EstadoPedido.CANCELADO);
        messagingTemplate.convertAndSend("/topic/pedidos", "cancelado");
        return "redirect:/pedidos/listado";
    }

    @GetMapping("/cerrar/{id}")
    public String cerrarPedido(@PathVariable("id") Long id) {
        pedidoService.cambiarEstado(id, EstadoPedido.FINALIZADO);
        messagingTemplate.convertAndSend("/topic/pedidos", "finalizado");
        return "redirect:/pedidos/servidos";
    }

    @GetMapping("/entregar/{id}")
    public String entregarPedido(@PathVariable Long id) {
        pedidoService.cambiarEstado(id, EstadoPedido.PAGANDO);
        messagingTemplate.convertAndSend("/topic/pedidos", "entregado");
        return "redirect:/pedidos/listado";
    }

    // 7. HISTORIAL PAGINADO
    @GetMapping("/finalizados")
    public String pedidosServidos(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());
        Page<Pedido> pedidosPage = pedidoService.listarPedidosPaginados(pageable);

        model.addAttribute("pedidos", pedidosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pedidosPage.getTotalPages());
        model.addAttribute("totalItems", pedidosPage.getTotalElements());

        return "pedido/pedidosServidos";
    }

    @GetMapping("/detalle/{id}")
    public String verDetallePedido(@PathVariable("id") Long id, Model model) {
        Pedido pedido = pedidoService.buscarPorId(id);
        pedido.calcularTotal();
        model.addAttribute("pedido", pedido);
        return "pedido/detallePedido";
    }

    // --- AQUÍ ESTÁ EL CAMBIO IMPORTANTE ---
    @GetMapping("/fragmento-mesas")
    public String obtenerFragmentoMesas(Model model) {
        // 1. Cargar lista de mesas (usando el servicio de mesas como antes)
        model.addAttribute("listaMesas", mesaService.listarMesasParaNuevoPedido());

        // 2. Usar el servicio de PEDIDOS para obtener el objeto preparado
        model.addAttribute("pedido", pedidoService.crearPedidoVacioConMesa());

        return "pedido/formPedido :: #mesa-selector-container";
    }
}