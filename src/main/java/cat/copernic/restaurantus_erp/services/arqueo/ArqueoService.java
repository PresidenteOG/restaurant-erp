package cat.copernic.restaurantus_erp.services.arqueo;

import java.util.List;

import cat.copernic.restaurantus_erp.domain.ArqueoCaja;

public interface ArqueoService {
    ArqueoCaja obtenerPorId(Long id);
    ArqueoCaja obtenerArqueoAbierto();
    Double calcularVentasEfectivoActual(ArqueoCaja arqueo);
    Double calcularVentasTarjetaActual(ArqueoCaja arqueo);
    void procesarYGuardarArqueo(ArqueoCaja arqueo, Long idExistente);
    ArqueoCaja abrirNuevoTurno(Double efectivoInicial);
    List<ArqueoCaja> listarTodosLosCierres();
    boolean existeCajaAbierta();
    String obtenerPlatoTop(ArqueoCaja arqueo);
    String obtenerMenuTop(ArqueoCaja arqueo);
}
