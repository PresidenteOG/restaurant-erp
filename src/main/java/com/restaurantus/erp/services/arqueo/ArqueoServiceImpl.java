package com.restaurantus.erp.services.arqueo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurantus.erp.dao.mysql.ArqueoCajaDAO;
import com.restaurantus.erp.dao.mysql.PedidoDAO;
import com.restaurantus.erp.domain.ArqueoCaja;
import com.restaurantus.erp.domain.MetodoPago;

@Service
public class ArqueoServiceImpl implements ArqueoService {
    @Autowired
    private PedidoDAO pedidoDAO;

    @Autowired
    private ArqueoCajaDAO arqueoCajaDAO;

    @Override
    public ArqueoCaja obtenerPorId(Long id) {
        return arqueoCajaDAO.findById(id).orElse(null);
    }

    @Override
    public ArqueoCaja obtenerArqueoAbierto() {
        // Buscamos el registro que tenga cajaAbierta = 1
        // Tendrás que añadir este método a tu ArqueoCajaDAO
        return arqueoCajaDAO.findFirstByCajaAbiertaOrderByFechaAperturaDesc(1);
    }

    @Override
    public Double calcularVentasEfectivoActual(ArqueoCaja arqueo) {
        if (arqueo == null) return 0.0;
        
        LocalDateTime inicio = arqueo.getFechaApertura();
        // Si la caja está abierta (1), sumamos hasta "ahora". Si está cerrada, hasta su fin.
        LocalDateTime fin = (arqueo.getCajaAbierta() == 1) ? LocalDateTime.now() : arqueo.getFechaCierre();
        
        // Usamos el DAO/Repository para que la DB haga el trabajo sucio
        Double total = pedidoDAO.sumarVentasPorTipoPagoYFecha(MetodoPago.EFECTIVO, inicio, fin);
        return (total != null) ? total : 0.0;
    }

    @Override
    public Double calcularVentasTarjetaActual(ArqueoCaja arqueo) {
        if (arqueo == null) return 0.0;
        
        LocalDateTime inicio = arqueo.getFechaApertura();
        LocalDateTime fin = (arqueo.getCajaAbierta() == 1) ? LocalDateTime.now() : arqueo.getFechaCierre();
        
        Double total = pedidoDAO.sumarVentasPorTipoPagoYFecha(MetodoPago.TARJETA, inicio, fin);
        return (total != null) ? total : 0.0;
    }

    @Override
    @Transactional
    public void procesarYGuardarArqueo(ArqueoCaja datosFormulario, Long idExistente) {
        ArqueoCaja arqueo = arqueoCajaDAO.findById(idExistente)
                .orElseThrow(() -> new RuntimeException("Error: Turno no encontrado"));

        // Actualizamos los datos del cierre
        arqueo.setVentasEfectivo(datosFormulario.getVentasEfectivo());
        arqueo.setVentasTarjeta(datosFormulario.getVentasTarjeta());
        arqueo.setSalidasEfectivo(datosFormulario.getSalidasEfectivo());
        arqueo.setEfectivoTeorico(datosFormulario.getEfectivoTeorico());
        arqueo.setEfectivoReal(datosFormulario.getEfectivoReal());
        arqueo.setDiferenciaRT(datosFormulario.getEfectivoReal() - datosFormulario.getEfectivoTeorico());
        
        arqueo.setFechaCierre(LocalDateTime.now());
        arqueo.setCajaAbierta(0); // Estado: Cerrada

        arqueoCajaDAO.save(arqueo);
    }

    @Override
    @Transactional
    public ArqueoCaja abrirNuevoTurno(Double efectivoInicial) {
        // Verificar si ya existe una caja abierta
        ArqueoCaja cajaExistente = arqueoCajaDAO.findFirstByCajaAbiertaOrderByFechaAperturaDesc(1);
        
        if (cajaExistente != null) {
            // Si ya hay una, lanzamos una excepción o devolvemos null
            throw new RuntimeException("No se puede abrir una caja porque ya existe un turno activo.");
        }

        ArqueoCaja nuevoArqueo = new ArqueoCaja();
        
        nuevoArqueo.setEfectivoInicial(efectivoInicial);
        nuevoArqueo.setFechaApertura(LocalDateTime.now()); // Registro del momento exacto
        nuevoArqueo.setCajaAbierta(1); // Estado: Abierta
        
        // Inicializamos a cero el resto para evitar valores nulos en cálculos
        nuevoArqueo.setVentasTarjeta(0.0);
        nuevoArqueo.setVentasEfectivo(0.0);
        nuevoArqueo.setSalidasEfectivo(0.0);
        nuevoArqueo.setEfectivoTeorico(efectivoInicial); 
        nuevoArqueo.setEfectivoReal(0.0);
        nuevoArqueo.setDiferenciaRT(0.0);
        
        return arqueoCajaDAO.save(nuevoArqueo);
    }

   @Override
    public List<ArqueoCaja> listarTodosLosCierres() {
        return arqueoCajaDAO.findAll();
    }

    @Override
    public boolean existeCajaAbierta() {
        // Si encuentra algo, devuelve true
        return arqueoCajaDAO.findByCajaAbierta(1) != null;
    }

    @Override
    public String obtenerPlatoTop(ArqueoCaja arqueo) {
        if (arqueo == null || arqueo.getFechaApertura() == null) return "Sin datos";
        
        LocalDateTime inicio = arqueo.getFechaApertura();
        LocalDateTime fin = (arqueo.getCajaAbierta() == 1) ? LocalDateTime.now() : arqueo.getFechaCierre();
        
        String plato = pedidoDAO.obtenerPlatoMasVendido(inicio, fin);
        return (plato != null) ? plato : "Sin ventas";
    }

    @Override
    public String obtenerMenuTop(ArqueoCaja arqueo) {
        if (arqueo == null || arqueo.getFechaApertura() == null) return "Sin datos";
        
        LocalDateTime inicio = arqueo.getFechaApertura();
        LocalDateTime fin = (arqueo.getCajaAbierta() == 1) ? LocalDateTime.now() : arqueo.getFechaCierre();
        
        String menu = pedidoDAO.obtenerMenuMasVendido(inicio, fin);
        return (menu != null) ? menu : "Sin ventas";
    }
}
