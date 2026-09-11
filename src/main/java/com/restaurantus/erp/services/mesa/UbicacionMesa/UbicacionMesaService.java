package com.restaurantus.erp.services.mesa.UbicacionMesa;

import java.util.List;

public interface UbicacionMesaService {

    List<String> listarNombresUbicaciones();
    void guardarUbicacionMesa(String nombre);
}
