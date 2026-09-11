package com.restaurantus.erp.services.ingrediente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurantus.erp.dao.mysql.ConsumoDAO;
import com.restaurantus.erp.dao.mysql.IngredienteDAO;
import com.restaurantus.erp.dao.mysql.LoteDAO;
import com.restaurantus.erp.dao.mysql.PlatoIngredienteDAO;
import com.restaurantus.erp.dao.mysql.SolicitudStockDAO;
import com.restaurantus.erp.domain.Consumo;
import com.restaurantus.erp.domain.Ingrediente;
import com.restaurantus.erp.domain.Lote;
import com.restaurantus.erp.domain.SolicitudStock;
import com.restaurantus.erp.services.telegram.TelegramService;

@Service
public class IngredienteServiceImpl implements IngredienteService {
    
    @Autowired
    private IngredienteDAO ingredienteDAO;

    @Autowired
    private PlatoIngredienteDAO platoIngredienteDAO;
    
    @Autowired
    private SolicitudStockDAO solicitudStockDAO;

    @Autowired
    private LoteDAO loteDAO;

    @Autowired
    private ConsumoDAO consumoDAO;

    @Autowired
    private TelegramService telegramService;

    private final String ADMIN_CHAT_ID = "6379770764";

    @Override 
    public List<Ingrediente> listarTodos() { 
        return ingredienteDAO.findAll(); 
    }

    @Override 
    public List<SolicitudStock> listarSolicitudes() {
        return solicitudStockDAO.findAll();
    }

    @Override 
    public List<Consumo> listarConsumosPorFecha() {
        return consumoDAO.findAllByOrderByFechaConsumoDesc();
    }

    @Override 
    public List<Lote> listarLotes(Long ingredienteId) {
        return loteDAO.findByIngrediente(ingredienteId);
    }

    @Override 
    public Ingrediente guardar(Ingrediente i) { 
        if (i.getId() != null) {
            Ingrediente oldIng = ingredienteDAO.findById(i.getId()).orElse(null);

            if (i.getStockActual() < oldIng.getStockActual()) {
                this.restarCantidadDeLotes(i.getId(), (oldIng.getStockActual() - i.getStockActual()));
            }
        }

        // 1. Guardamos el ingrediente
        Ingrediente guardado = ingredienteDAO.save(i);
        
        // 2. IMPORTANTE: Verificamos el stock justo después de guardar
        // Si no pones esto, al editar desde la web NO saltará la alarma.
        this.verificarStock(guardado);
        
        return guardado;
    }

    @Override 
    public Ingrediente buscarPorId(Long id) { 
        return ingredienteDAO.findById(id).orElse(null); 
    }

    @Transactional
    @Override
    public void eliminar(Long id) {
        Ingrediente ing = ingredienteDAO.findById(id).orElse(null);
        if(ing!=null) { 
            platoIngredienteDAO.deleteByIngrediente(ing); 
            ingredienteDAO.delete(ing); 
        }
    }

    @Transactional
    public void restarCantidadDeLotes(Long id, Double cantidad) {
        List<Lote> list = this.loteDAO.findActiveOrderByOldest(id);
        double restaCantidad = Math.abs(cantidad.doubleValue());

        //restar de los lotes la cantidad consumida
        for (int i = 0; i < list.size() && restaCantidad > 0; i++) {
            Lote lote = list.get(i);

            if (lote.getCantidadRestante() - restaCantidad < 0) {
                restaCantidad -= lote.getCantidadRestante();
                lote.setCantidadRestante(0.0);
            } else {
                lote.setCantidadRestante(lote.getCantidadRestante() - restaCantidad);
                restaCantidad = 0;
            }

            this.loteDAO.save(lote);
        }
    }

    @Transactional
    @Override
    public void consumirStock(Long id, Double cantidad) {
        this.restarCantidadDeLotes(id, cantidad);
        this.ingredienteDAO.restarStock(id, cantidad);

        Ingrediente actualizado = this.ingredienteDAO.findById(id).orElse(null);
        
        if (actualizado != null) {
            System.out.println("📉 Stock consumido. Nuevo stock: " + actualizado.getStockActual());
            this.verificarStock(actualizado);
        }
    }

    @Override 
    public SolicitudStock buscarSolicitudPorId(Long id) { 
        return this.solicitudStockDAO.findById(id).orElse(null); 
    }

    @Transactional
    @Override
    public void completarSolicitud(Lote lote) {
        SolicitudStock solicitud = lote.getSolicitudStock();

        if (solicitud != null && "PENDIENTE".equals(solicitud.getEstado())) {
            lote.setFechaEntrada(LocalDateTime.now());
            Ingrediente ing = solicitud.getIngrediente();
            double cantidadAReponer = lote.getCantidadInicial(); 
            ing.setStockActual(ing.getStockActual() + cantidadAReponer);

            solicitud.setEstado("COMPLETADO");
            
            this.ingredienteDAO.save(ing);
            this.solicitudStockDAO.save(solicitud);
            
            this.telegramService.enviarMensaje(ADMIN_CHAT_ID, "✅ REPOSICIÓN COMPLETADA\nIngrediente: " + ing.getNombre());
            lote.setCantidadRestante(cantidadAReponer);
            this.loteDAO.save(lote);
        }
    }

    @Override
    public Lote crearLote(Long solicitudId) {
        SolicitudStock solicitud = this.buscarSolicitudPorId(solicitudId);
        Lote lote = new Lote();
        lote.setCantidadInicial(solicitud.getCantidadSolicitada());
        lote.setSolicitudStock(solicitud);
        lote.setIngrediente(solicitud.getIngrediente());
        return lote;
    }

    // --- LÓGICA DE ALERTA ---
    private void verificarStock(Ingrediente ing) {
        // Verifica si el stock es 5 o menos
        if (ing.getStockActual() != null && ing.getStockActual() <= 5.0) {

            // Comprueba si ya existe solicitud pendiente para no repetir
            boolean yaSolicitado = solicitudStockDAO.existsByIngredienteAndEstado(ing, "PENDIENTE");
            
            if (!yaSolicitado) {
                System.out.println("📝 Creando solicitud automática...");
                
                SolicitudStock solicitud = new SolicitudStock();
                solicitud.setIngrediente(ing);
                solicitud.setCantidadSolicitada(50.0);
                solicitud.setFechaSolicitud(LocalDateTime.now());
                solicitud.setEstado("PENDIENTE");
                
                solicitudStockDAO.save(solicitud);

                String mensaje = "⚠️ ALERTA DE STOCK BAJO ⚠️\n\n" +
                                 "📦 Ingrediente: " + ing.getNombre() + "\n" +
                                 "📉 Stock actual: " + ing.getStockActual() + " " + ing.getUnidadMedida() + "\n\n" +
                                 "✅ Solicitud Automática #" + solicitud.getId() + " generada.\n" +
                                 "Por favor, reponer inventario.";
                
                telegramService.enviarMensaje(ADMIN_CHAT_ID, mensaje);
                System.out.println("🚀 Mensaje enviado a Telegram");
            } 
        }
    }

    @Scheduled(cron = "0 0 1 * * ?") // Se ejecuta cada noche a la 1:00 AM
    @Transactional
    public void revisarCaducidades() {
        List<Lote> lotesActivos = this.loteDAO.findByCaducadoFalse();
        LocalDate hoy = LocalDate.now();

        for (Lote lote : lotesActivos) {
            if (lote.getFechaCaducidad().isBefore(hoy)) {
                this.marcarComoCaducado(lote);
            }
        }
    }

    @Transactional
    public void marcarComoCaducado(Lote lote) {
        lote.setCaducado(true);
        
        // Descontar del stock general del ingrediente
        Ingrediente ing = lote.getIngrediente();
        ing.setStockActual(ing.getStockActual() - lote.getCantidadRestante());
        this.ingredienteDAO.save(ing);

        // Registrar en el historial (Log)
        Consumo logCaducado = new Consumo();
        logCaducado.setIngrediente(ing);
        logCaducado.setCantidad(lote.getCantidadRestante());
        logCaducado.setFechaConsumo(LocalDateTime.now());
        logCaducado.setPlato(null);//Si el plato es null, es caducado
        logCaducado.setCostoPlato(0.0);
        this.consumoDAO.save(logCaducado);
        
        this.loteDAO.save(lote);
        System.out.println("El lote " + lote.getId() + " del ingrediente " + ing.getNombre()
         + " ha sido marcado como caducado: Se han perdido " + lote.getCantidadRestante()
         + " " + ing.getUnidadMedida());
        lote.setCantidadRestante(0.0); // El lote ya no tiene stock disponible
        this.verificarStock(ing);
    }
    @Override
    public int contarLotesPorIngrediente(Long ingredienteId) {
        return loteDAO.countByIngredienteId(ingredienteId);
    }
}