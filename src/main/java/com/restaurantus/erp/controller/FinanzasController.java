package com.restaurantus.erp.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.restaurantus.erp.domain.ArqueoCaja;
import com.restaurantus.erp.domain.EstadoPedido;
import com.restaurantus.erp.domain.Pedido;
import com.restaurantus.erp.services.arqueo.ArqueoService;
import com.restaurantus.erp.services.factura.PdfService;
import com.restaurantus.erp.services.pedido.PedidoService;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/finanzas")
public class FinanzasController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ArqueoService arqueoService;

    @Autowired
    private PdfService pdfService;

    @GetMapping("/pendientesCobro")
    public String verPendientesDeCobro(Model model) {
        model.addAttribute("enPago", pedidoService.buscarPorEstado(EstadoPedido.PAGANDO));
        model.addAttribute("hayCajaAbierta", arqueoService.existeCajaAbierta());
        return "finanzas/pendientesCobro";
    }

    @GetMapping("/pasarelaCobro/{id}")
    public String mostrarPantallaCobro(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoService.buscarPorId(id);
        pedido.calcularTotal();
        model.addAttribute("pedido", pedido);
        model.addAttribute("totalCalculado", pedido.getTotal());
        model.addAttribute("iva", this.pedidoService.getIVAPercent());
        return "finanzas/pantallaCobro";
    }

    @PostMapping("/cobrar/{id}")
    public String cobrarMesa(@PathVariable Long id, @RequestParam String metodo) {
        // 1. Procesamos cobro
        pedidoService.finalizarYCobrarPedido(id, metodo);
        
        // 2. Redirigir directamente a pendientes
        return "redirect:/finanzas/pendientesCobro";
    }


    @GetMapping("/arqueo")
    public String mostrarArqueo(Model model) {
        ArqueoCaja arqueoActual = arqueoService.obtenerArqueoAbierto();

        Double ventasEfectivo = arqueoService.calcularVentasEfectivoActual(arqueoActual);
        Double ventasTarjeta = arqueoService.calcularVentasTarjetaActual(arqueoActual);
        
        model.addAttribute("platoMasVendido", arqueoService.obtenerPlatoTop(arqueoActual));
        model.addAttribute("menuMasVendido", arqueoService.obtenerMenuTop(arqueoActual));
        model.addAttribute("arqueoActual", arqueoActual);
        model.addAttribute("ventasEfectivo", ventasEfectivo);
        model.addAttribute("ventasTarjeta", ventasTarjeta);
        model.addAttribute("totalVentas", ventasEfectivo + ventasTarjeta);
        return "finanzas/arqueoCaja";
    }

    @PostMapping("/guardarArqueo")
    public String guardarArqueo(@ModelAttribute ArqueoCaja arqueo, @RequestParam("id") Long id) {
        // 1. Guardar datos
        arqueoService.procesarYGuardarArqueo(arqueo, id);
        
        // 2. Redirigir directamente al listado
        return "redirect:/finanzas/pendientesCobro";
    }


    @GetMapping("/abrirCaja")
    public String mostrarAbrirArqueo() {
        return "finanzas/abrirCaja";
    }

    @PostMapping("/abrirArqueo")
    public String abrirCaja(@RequestParam Double efectivoInicial, RedirectAttributes flash) {
        try {
            arqueoService.abrirNuevoTurno(efectivoInicial);
            flash.addFlashAttribute("success", "Caja abierta correctamente.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/finanzas/abrirCaja";
        }
        return "redirect:/finanzas/pendientesCobro";
    }

    @GetMapping("/listadoCierres")
    public String listarCierres(Model model) {
        model.addAttribute("hayCajaAbierta", arqueoService.existeCajaAbierta());
        model.addAttribute("cierres", arqueoService.listarTodosLosCierres());
        return "finanzas/listadoCierres";
    }

    @GetMapping("/detalleCierre/{id}")
    public String verDetalleCierre(@PathVariable("id") Long id, Model model) {
        ArqueoCaja arqueo = arqueoService.obtenerPorId(id);
        Double totalVentas = (arqueo.getVentasEfectivo() != null ? arqueo.getVentasEfectivo() : 0.0) + (arqueo.getVentasTarjeta() != null ? arqueo.getVentasTarjeta() : 0.0);

        model.addAttribute("platoMasVendido", arqueoService.obtenerPlatoTop(arqueo));
        model.addAttribute("menuMasVendido", arqueoService.obtenerMenuTop(arqueo));
        model.addAttribute("caja", arqueo);
        model.addAttribute("totalVentas", totalVentas);
        return "finanzas/detalleCierre"; 
    }

    @GetMapping("/descargarFactura/{id}")
    public void descargarFactura(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Pedido pedido = pedidoService.buscarPorId(id);
        pedido.calcularTotal();

        byte[] pdfBytes = pdfService.generarFacturaBytes(pedido);

        String fechaStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Factura_" + pedido.getId() + "_" + fechaStr + ".pdf");
        response.setContentLength(pdfBytes.length);
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    @GetMapping("/descargarComprobanteArqueo/{id}")
    public void descargarComprobanteArqueo(@PathVariable Long id, HttpServletResponse response) throws IOException {
        ArqueoCaja arqueo = arqueoService.obtenerPorId(id);
        String platoTop = arqueoService.obtenerPlatoTop(arqueo);
        String menuTop = arqueoService.obtenerMenuTop(arqueo);

        byte[] pdfBytes = pdfService.generarComprobanteArqueoBytes(arqueo, platoTop, menuTop);

        String fechaStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Cierre_Caja_" + id + "_" + fechaStr + ".pdf");
        response.setContentLength(pdfBytes.length);
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }
}