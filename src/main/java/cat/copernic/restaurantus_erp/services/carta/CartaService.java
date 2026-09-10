package cat.copernic.restaurantus_erp.services.carta;

import java.util.List;

import org.springframework.ui.Model;

import cat.copernic.restaurantus_erp.domain.Menu;
import cat.copernic.restaurantus_erp.domain.Plato;

public interface CartaService {
    // Gestión de Platos
    List<Plato> listarPlatos();
    Plato buscarPlatoPorId(Long id);

    // Gestión de Menús
    List<Menu> listarMenus();
    Menu guardarMenu(Menu menu);
    Menu buscarMenuPorId(Long id);
    void eliminarMenu(Long id);
    void rellenarFormMenu(Model model, Long id);
}
