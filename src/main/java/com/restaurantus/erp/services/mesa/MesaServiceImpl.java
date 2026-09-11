package com.restaurantus.erp.services.mesa;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.restaurantus.erp.dao.mysql.MesaDAO;
import com.restaurantus.erp.dao.mysql.ReservaDAO;
import com.restaurantus.erp.domain.Mesa;
@Service
public class MesaServiceImpl  implements MesaService {

    @Autowired
    private MesaDAO mesaDAO;
    @Autowired
    private ReservaDAO reservaDAO;

    @Override
    public List<Mesa> findAllMesas() {
        return mesaDAO.findAll();
    }

    @Override
    public void guardarMesa(Mesa mesa) {
        // 1. Validación de Capacidad (Regla de negocio: entre 1 y 20)
        if (mesa.getCapacidad() == null || mesa.getCapacidad() < 1 || mesa.getCapacidad() > 20) {
            throw new IllegalArgumentException("La capacidad de la mesa debe estar entre 1 y 20 personas.");
        }
        mesaDAO.save(mesa);
    }

    @Override
    public Mesa buscarPorId(Long idMesa) {
        return mesaDAO.findById(idMesa).orElse(null);
    }

    @Override
    public void eliminarPorId(Long idMesa) {
        mesaDAO.deleteById(idMesa);
    }

    @Override
    public List<Mesa> listarMesasParaNuevoPedido() {
        List<Mesa> todas = mesaDAO.findAll();
        List<Mesa> disponiblesReales = new ArrayList<>();
        
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime margen15Min = ahora.plusMinutes(15);

        for (Mesa mesa : todas) {
            // 1. Miramos si la mesa está físicamente libre ahora
            boolean estaLibre = mesa.getEstado().equals("available");
            
            // 2. Miramos si hay alguna reserva que empiece en los próximos 15 min
            boolean tieneReservaPronto = reservaDAO.existeReservaProxima(mesa.getIdMesa(), ahora, margen15Min);

            // Si está libre Y NO tiene reserva pronto, la añadimos a la lista
            if (estaLibre && !tieneReservaPronto) {
                disponiblesReales.add(mesa);
            }
        }
        
        return disponiblesReales;
    }

    //home dashboard KPI
    @Override
    public long countByEstado(String estado) {
        return mesaDAO.countByEstado(estado);
    }

    @Override
    public long countAll() {
        return mesaDAO.countAll();
    }
}