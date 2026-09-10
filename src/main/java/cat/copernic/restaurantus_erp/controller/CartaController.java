package cat.copernic.restaurantus_erp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.restaurantus_erp.domain.Menu;
import cat.copernic.restaurantus_erp.domain.Plato;
import cat.copernic.restaurantus_erp.services.carta.CartaService;
import cat.copernic.restaurantus_erp.services.ingrediente.IngredienteService;
import cat.copernic.restaurantus_erp.services.plato.PlatoService; // <--- IMPORTANTE

@Controller
@RequestMapping("/carta")
public class CartaController {

    @Autowired
    private CartaService cartaService;

    @Autowired
    private PlatoService platoService; // <--- AÑADIR ESTO (Es el que tiene la lógica buena)

    @Autowired
    private IngredienteService ingredienteService; 

    // 1. Ver toda la carta (Platos y Menús)
    @GetMapping
    public String verCarta(Model model) {
        // Puedes usar platoService o cartaService según prefieras, pero para guardar usaremos platoService
        model.addAttribute("platos", cartaService.listarPlatos());
        model.addAttribute("menus", cartaService.listarMenus());
        model.addAttribute("activePage", "menus");
        return "carta/verCarta"; 
    }

    // --- SECCIÓN PLATOS ---

    @GetMapping("/plato/nuevo")
    public String formularioPlato(Model model) {
        model.addAttribute("plato", new Plato());
        model.addAttribute("todosLosIngredientes", ingredienteService.listarTodos());
        return "carta/nuevoPlato";
    }

    // --- ESTE ES EL MÉTODO QUE CAMBIA ---
    @PostMapping("/plato/guardar")
    public String guardarPlato(@ModelAttribute("plato") Plato plato, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        platoService.guardarPlato(plato, imageFile);
        
        return "redirect:/carta";
    }

    @GetMapping("/plato/editar/{id}")
    public String editarPlato(@PathVariable("id") Long id, Model model) {
        Plato plato = cartaService.buscarPlatoPorId(id); // O platoService.buscarPlato(id)
        model.addAttribute("plato", plato);
        model.addAttribute("todosLosIngredientes", ingredienteService.listarTodos());
        return "carta/nuevoPlato";
    }

    @GetMapping("/plato/eliminar/{id}")
    public String eliminarPlato(@PathVariable("id") Long id) {
        // Usamos platoService para que también lance el aviso WebSocket al borrar
        platoService.eliminarPlato(id);
        return "redirect:/carta";
    }

    // --- SECCIÓN MENÚS (Sin cambios) ---

    @GetMapping("/menu/nuevo")
    public String formularioMenu(Model model) {
        cartaService.rellenarFormMenu(model, null);
        return "carta/nuevoMenu";
    }

    @PostMapping("/menu/guardar")
    public String guardarMenu(@ModelAttribute Menu menu) {
        cartaService.guardarMenu(menu);
        return "redirect:/carta";
    }

    @GetMapping("/menu/editar/{id}")
    public String editarMenu(@PathVariable("id") Long id, Model model) {
        cartaService.rellenarFormMenu(model, id);
        return "carta/nuevoMenu";
    }

    @GetMapping("/menu/eliminar/{id}")
    public String eliminarMenu(@PathVariable("id") Long id) {
        cartaService.eliminarMenu(id);
        return "redirect:/carta";
    }

    @GetMapping("/detalleMenu/{id}")
    public String detalleMenu(@PathVariable("id") Long id, Model model) {
        Menu menu = cartaService.buscarMenuPorId(id);
        model.addAttribute("menu", menu);
        return "carta/detalleMenu";
    }
}