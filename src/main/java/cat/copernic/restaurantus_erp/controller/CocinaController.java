package cat.copernic.restaurantus_erp.controller;
// Arreglado
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; 
import org.springframework.web.bind.annotation.RequestMapping;

import cat.copernic.restaurantus_erp.services.cocina.CocinaService;

@Controller
@RequestMapping("/cocina")
public class CocinaController {

    @Autowired
    private CocinaService cocinaService;

    @GetMapping("/pendientes")
    public String verPendientes(Model model) {
        //Solo mostramos pedidos con estado "PENDIENTE"
        model.addAttribute("pedidos", cocinaService.listarPedidosCocina());
        model.addAttribute("notasPedidos", cocinaService.listarNotasPedidos());
        model.addAttribute("activePage", "kitchen");
        return "cocina/listadoPendientes";
    }

    //Método para empezar un plato
    @GetMapping("/empezarPlato/{pedidoId}/{lineaId}")
    public String empezarPlato(@PathVariable Long pedidoId, @PathVariable Long lineaId) {
        cocinaService.empezarPlato(pedidoId, lineaId);
        return "redirect:/cocina/pendientes";
    }

    //Marcar PLATO individual como LISTO
    @GetMapping("/platoListo/{pedidoId}/{lineaId}")
    public String marcarPlatoListo(@PathVariable Long pedidoId, @PathVariable Long lineaId) {
        cocinaService.marcarListoElemento(pedidoId, lineaId, "PLATO");
        return "redirect:/cocina/pendientes";
    }
}
