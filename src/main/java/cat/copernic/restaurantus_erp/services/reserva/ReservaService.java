package cat.copernic.restaurantus_erp.services.reserva;

import java.util.List;

import cat.copernic.restaurantus_erp.domain.Reserva;

public interface ReservaService {

    int getOpenTime();

    int getCloseTime();

    List<Reserva> findAllReserves();

    void saveReserva(Reserva reserva, Long idClienteExistente);

    void actualizarReserva(Reserva reservaEditada);

    void eliminarReserva(Long id);

    Reserva buscarReserva(Long id);

    ///home dashboard
    int contarReservasHoy();
}
