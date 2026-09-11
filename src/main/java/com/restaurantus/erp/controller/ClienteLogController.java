package com.restaurantus.erp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.restaurantus.erp.domain.ClienteLog;
import com.restaurantus.erp.services.clienteLog.ClienteLogService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/clientes")
public class ClienteLogController {

    @Autowired
    private ClienteLogService clienteLogService;

    @GetMapping("/listar")
    public String listarClientes(Model model) {
        List<ClienteLog> clientes = clienteLogService.listarClientesLog();
        model.addAttribute("clientes", clientes);
        model.addAttribute("activePage", "customers");
        return "clienteLog/lista_clientes";
    }

    @GetMapping("/nuevo")
    public String nuevoCliente(Model model) {
        model.addAttribute("cliente", new ClienteLog());
        model.addAttribute("activePage", "customers");
        return "clienteLog/nuevo_cliente";
    }

    @PostMapping("/guardar")
    public String guardarCliente(@Valid ClienteLog cliente, BindingResult result, RedirectAttributes flash) {

        if (result.hasErrors()) {
            String mensaje = "Datos inválidos";

            if (result.hasFieldErrors("email")) {
                mensaje = result.getFieldError("email").getDefaultMessage();
            } else if (result.hasFieldErrors("telefono")) {
                mensaje = result.getFieldError("telefono").getDefaultMessage();
            }

            flash.addFlashAttribute("error", "Error: " + mensaje);
            return "redirect:/clientes/nuevo";
        }

        try {
            clienteLogService.guardarClienteLog(cliente);
            flash.addFlashAttribute("success", "¡Cliente registrado con éxito!");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error: Datos duplicados o problema en el servidor.");
        }

        return "redirect:/clientes/nuevo";
    }
    

    @PostMapping("/actualizar")
    public String actualizarCliente(@Valid ClienteLog cliente, BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            String mensaje = "Datos inválidos";

            if (result.hasFieldErrors("email")) {
                mensaje = result.getFieldError("email").getDefaultMessage();
            } else if (result.hasFieldErrors("telefono")) {
                mensaje = result.getFieldError("telefono").getDefaultMessage();
            }

            flash.addFlashAttribute("error", "Error: " + mensaje);
            return "redirect:/clientes/editar/" + cliente.getIdClienteLog();
        }

        try {
            clienteLogService.guardarClienteLog(cliente);

            // El mensaje que verás en el banner verde
            flash.addFlashAttribute("success", "¡Cliente actualizado con éxito!");

            // ESTA ES LA CLAVE: Redirigir de vuelta al formulario de este cliente
            return "redirect:/clientes/editar/" + cliente.getIdClienteLog();

        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al actualizar el cliente: " + e.getMessage());
            return "redirect:/clientes/editar/" + cliente.getIdClienteLog();
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model, RedirectAttributes flash) {
        ClienteLog cliente = clienteLogService.buscarPorId(id);

        if (cliente == null) {
            flash.addFlashAttribute("error", "El cliente solicitado no existe.");
            return "redirect:/clientes/listar";
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("activePage", "customers");
        return "clienteLog/editar_cliente";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable("id") Long id, RedirectAttributes flash) {
        try {
            clienteLogService.eliminarPorId(id);
            flash.addFlashAttribute("success", "Cliente eliminado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se puede eliminar: Este cliente tiene reservas activas.");
        }
        return "redirect:/clientes/listar";
    }

}
