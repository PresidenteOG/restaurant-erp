package com.restaurantus.erp.services.plato;

import org.springframework.web.multipart.MultipartFile;

import com.restaurantus.erp.domain.Plato; // <--- Importante para la imagen

public interface PlatoService {

    // Ahora este método acepta el plato Y la imagen
    void guardarPlato(Plato plato, MultipartFile imagen);

    void eliminarPlato(Long id);
}