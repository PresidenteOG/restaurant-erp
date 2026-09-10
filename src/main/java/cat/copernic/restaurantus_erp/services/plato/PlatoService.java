package cat.copernic.restaurantus_erp.services.plato;

import org.springframework.web.multipart.MultipartFile;

import cat.copernic.restaurantus_erp.domain.Plato; // <--- Importante para la imagen

public interface PlatoService {

    // Ahora este método acepta el plato Y la imagen
    void guardarPlato(Plato plato, MultipartFile imagen);

    void eliminarPlato(Long id);
}