package com.restaurantus.erp.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.restaurantus.erp.domain.Consumo;
import com.restaurantus.erp.domain.Ingrediente;
import com.restaurantus.erp.domain.Lote;
import com.restaurantus.erp.services.factura.PdfService;
import com.restaurantus.erp.services.ingrediente.IngredienteService;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/almacen")
public class IngredienteController {

    @Autowired
    private IngredienteService ingredienteService;

    @Autowired
    private PdfService pdfService;
    

    // Listar todos los ingredientes (Inventario)
    @GetMapping("/inventario")
    public String listarInventario(Model model) {
        model.addAttribute("ingredientes", ingredienteService.listarTodos());
        model.addAttribute("activePage", "inventory");
        return "almacen/listado";
    }

    // Formulario para añadir un nuevo ingrediente
    @GetMapping("/nuevo")
    public String formularioNuevoIngrediente(Model model) {
        model.addAttribute("ingrediente", new Ingrediente());
        return "almacen/formIngrediente";
    }

    // Guardar el ingrediente (Nuevo o Editado)
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Ingrediente ingrediente) {
        ingredienteService.guardar(ingrediente);
        return "redirect:/almacen/inventario";
    }

    // Editar stock manualmente
    @GetMapping("/editar/{id}")
    public String editarIngrediente(@PathVariable Long id, Model model) {
        Ingrediente ing = ingredienteService.buscarPorId(id);
        model.addAttribute("ingrediente", ing);
        return "almacen/formIngrediente";
    }

    // Eliminar ingrediente
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        ingredienteService.eliminar(id);
        return "redirect:/almacen/inventario";
    }

    // Mostrar la página con la tabla
    @GetMapping("/stock")
    public String verSolicitudes(Model model) {
        model.addAttribute("listaSolicitudes", ingredienteService.listarSolicitudes());
        return "almacen/stock"; // Nombre del archivo HTML
    }

    // Acción del link "Rellenar"
    @GetMapping("/stock/completar/{id}")
    public String rellenarStock(@PathVariable Long id, Model model) {
        model.addAttribute("lote", this.ingredienteService.crearLote(id));
        return "almacen/formLote";
    }

    // Acción del botón "Rellenar" en añadir lote
    @PostMapping("/stock/completar")
    public String completarSolicitud(@ModelAttribute Lote lote) {
        ingredienteService.completarSolicitud(lote);
        return "redirect:/almacen/stock";
    }

    @GetMapping("/consumos")
    public String listarConsumos(Model model) {
        List<Consumo> consumos = ingredienteService.listarConsumosPorFecha();
        model.addAttribute("consumos", consumos);
        return "almacen/listadoConsumos";
    }

    @GetMapping("/lotes/{id}")
    public String verLotes(@PathVariable Long id, Model model) {
        model.addAttribute("lotes", ingredienteService.listarLotes(id));
        return "almacen/listadoLotes";
    }

    @GetMapping("/inventario/informePdf")
    public void exportarInventarioPdf(HttpServletResponse response) throws IOException {

        List<Ingrediente> ingredientes = ingredienteService.listarTodos();

        byte[] pdfBytes = pdfService.generarInformeInventarioBytes(ingredientes, ingredienteService);

        response.setContentType("application/pdf");
        response.setHeader(
            "Content-Disposition",
            "attachment; filename=inventario_" + LocalDate.now() + ".pdf"
        );
        response.setContentLength(pdfBytes.length);

        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

}
