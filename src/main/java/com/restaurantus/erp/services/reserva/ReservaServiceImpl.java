package com.restaurantus.erp.services.reserva;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurantus.erp.dao.mysql.ClienteLogDAO;
import com.restaurantus.erp.dao.mysql.MesaDAO;
import com.restaurantus.erp.dao.mysql.ReservaDAO;
import com.restaurantus.erp.domain.Mesa;
import com.restaurantus.erp.domain.Reserva;
import com.restaurantus.erp.domain.User;

@Service
public class ReservaServiceImpl implements ReservaService {

    @Value("${restaurantus.open_time}")
    private String restaurantOpenTime;

    @Value("${restaurantus.close_time}")
    private String restaurantCloseTime;

    @Autowired
    private ReservaDAO reservaDAO;

    @Autowired
    private MesaDAO mesaDAO;

    @Autowired
    private ClienteLogDAO clienteLogDAO;

    @Override
    public int getOpenTime() {
        try {
            return getConfigTime(this.restaurantOpenTime);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            return 12;
        }
    }

    @Override
    public int getCloseTime() {
        try {
            return getConfigTime(this.restaurantCloseTime);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            return 12;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> findAllReserves() {
        return (List<Reserva>) reservaDAO.findAll();
    }

    @Override
    @Transactional
    public void saveReserva(Reserva reserva, Long idClienteExistente) {
        // 1. Lógica de Cliente (Existente vs Nuevo)
        if (idClienteExistente != null && idClienteExistente > 0) {
            reserva.setClienteLog(clienteLogDAO.findById(idClienteExistente).orElse(null));
        } else if (reserva.getClienteLog() != null && reserva.getClienteLog().getNombreCliente() != null
                && !reserva.getClienteLog().getNombreCliente().trim().isEmpty()) {
            // Si es un cliente nuevo, lo guardamos primero
            clienteLogDAO.save(reserva.getClienteLog());
        } else {
            reserva.setClienteLog(null);
        }

        // 2. Asignación de Usuario del Sistema (ID 9)
        User userSystem = new User();
        userSystem.setId(9L);
        reserva.setUsuario(userSystem);

        // 3. Validación de Fecha
        if (reserva.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se pueden realizar reservas para fechas pasadas.");
        }

        // 4. Validación de Mesa y Capacidad
        Mesa mesaReal = mesaDAO.findById(reserva.getMesa().getIdMesa())
                .orElseThrow(() -> new IllegalArgumentException("La mesa seleccionada no existe."));

        if (reserva.getNumComensales() > mesaReal.getCapacidad()) {
            throw new IllegalArgumentException("La mesa " + mesaReal.getNumMesa()
                    + " solo admite un máximo de " + mesaReal.getCapacidad() + " comensales.");
        }

        validarSolapamiento(reserva.getMesa().getIdMesa(), reserva.getFechaHora(), null);

        // 5. Sincronización de Ubicación
        reserva.setPreferenciaUbicacion(mesaReal.getUbicacion());

        // 6. Guardado final
        reservaDAO.save(reserva);
    }

    @Override
    @Transactional
    public void actualizarReserva(Reserva reservaEditada) {
        // 1. Recuperar la reserva original de la DB
        Reserva reservaOriginal = reservaDAO.findById(reservaEditada.getIdReserva())
                .orElseThrow(() -> new IllegalArgumentException("La reserva no existe."));

        // 2. Recuperar la mesa (la nueva o la actual) para validar capacidad
        Mesa mesaReal = mesaDAO.findById(reservaEditada.getMesa().getIdMesa())
                .orElseThrow(() -> new IllegalArgumentException("La mesa seleccionada no es válida."));

        // 3. VALIDACIÓN: Capacidad de la mesa
        if (reservaEditada.getNumComensales() > mesaReal.getCapacidad()) {
            throw new IllegalArgumentException("La mesa " + mesaReal.getNumMesa()
                    + " solo admite un máximo de " + mesaReal.getCapacidad() + " comensales.");
        }

        validarSolapamiento(reservaEditada.getMesa().getIdMesa(), reservaEditada.getFechaHora(), reservaEditada.getIdReserva());

        // 4. VALIDACIÓN: Fecha (no permitir mover a fechas pasadas)
        if (reservaEditada.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se puede modificar una reserva a una fecha pasada.");
        }

        // 5. PROTECCIÓN: Mantener cliente y usuario originales (que no vienen del formulario de edición)
        reservaEditada.setClienteLog(reservaOriginal.getClienteLog());
        reservaEditada.setUsuario(reservaOriginal.getUsuario());

        // 6. AUTO-ASIGNACIÓN: Actualizar la ubicación según la mesa seleccionada
        reservaEditada.setPreferenciaUbicacion(mesaReal.getUbicacion());

        // 7. Guardar cambios
        reservaDAO.save(reservaEditada);
    }

    @Override
    @Transactional
    public void eliminarReserva(Long id) {
        reservaDAO.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Reserva buscarReserva(Long id) {
        return reservaDAO.findById(id).orElse(null);
    }

    

    //METODO AUXILIAR
    private void validarSolapamiento(Long idMesa, LocalDateTime fechaHora, Long idExcluir) {
        // Definimos que una reserva dura 1 hora y 30 minutos
        LocalDateTime inicioNueva = fechaHora;
        LocalDateTime finNueva = fechaHora.plusMinutes(90);

        // Llamamos al DAO
        List<Reserva> conflictos = reservaDAO.buscarSolapamientos(idMesa, inicioNueva, finNueva, idExcluir);

        if (!conflictos.isEmpty()) {
            Reserva r = conflictos.get(0);
            // Formateamos la hora para que el mensaje sea amigable
            String horaConflicto = r.getFechaHora().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            throw new IllegalArgumentException("La mesa ya está reservada en esa franja (hay una reserva a las " + horaConflicto + ").");
        }
    }

    private static int getConfigTime(String str) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        String[] split = str.split(" ");

        if (split.length > 1) {
            if ("AM".equals(split[1])) {//si es AM
                int h0 = Integer.parseInt(split[0]);

                if (h0 == 12) {//Las 12 AM equivale a 24h
                    return 24;
                }

                return h0;
            } else { //si es PM
                int h0 = Integer.parseInt(split[0]);

                if (h0 == 12) {//Las 12 PM equivale a 12h
                    return 12;
                }

                return h0 + 12;
            }
        }

        return Integer.parseInt(split[0]);
    }

    //home dashboard
    @Override
    @Transactional(readOnly = true)
    public int contarReservasHoy() {
        return reservaDAO.countReservasHoy();
    }
}
