package cat.copernic.restaurantus_erp.services.carta;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import cat.copernic.restaurantus_erp.dao.mysql.MenuDAO;
import cat.copernic.restaurantus_erp.dao.mysql.PlatoDAO;
import cat.copernic.restaurantus_erp.domain.CategoriaPlato;
import cat.copernic.restaurantus_erp.domain.Menu;
import cat.copernic.restaurantus_erp.domain.Plato;

@Service
public class CartaServiceImpl implements CartaService {
    @Autowired
    private PlatoDAO platoDAO;

    @Autowired
    private MenuDAO menuDAO;

    // --- Métodos para Platos ---
    @Override
    public List<Plato> listarPlatos() {
        return platoDAO.findAll();
    }

    @Override
    public Plato buscarPlatoPorId(Long id) {
        return platoDAO.findById(id).orElse(null);
    }

    // --- Métodos para Menús ---
    @Override
    public List<Menu> listarMenus() {
        return menuDAO.findAll();
    }

    @Override
    public Menu guardarMenu(Menu menu) {
        return menuDAO.save(menu);
    }

    @Override
    public Menu buscarMenuPorId(Long id) {
        return menuDAO.findById(id).orElse(null);
    }
    @Override
    public void eliminarMenu(Long id) {
        menuDAO.deleteById(id);
    }

    @Override
    public void rellenarFormMenu(Model model, Long id) {
        //Decidir si es un menú nuevo o uno existente
        Menu menu;
        if (id != null) {
            menu = menuDAO.findById(id).orElse(new Menu());
        } else {
            menu = new Menu();
        }
        //Creamos el objeto menú vacío para el formulario
        model.addAttribute("menu", menu);

        //Obtenemos todos los platos
        List<Plato> todos = platoDAO.findAll();

        //Creamos listas vacías para clasificar
        List<Plato> platosPrincipales = new java.util.ArrayList<>();
        List<Plato> bebidas = new java.util.ArrayList<>();
        List<Plato> postres = new java.util.ArrayList<>();

        //Clasificamos con un bucle tradicional
        for (Plato p : todos) {
            if (p.getCategoria() == CategoriaPlato.PLATO) {
                platosPrincipales.add(p);
            } else if (p.getCategoria() == CategoriaPlato.BEBIDA) {
                bebidas.add(p);
            } else if (p.getCategoria() == CategoriaPlato.POSTRE) {
                postres.add(p);
            }
        }

        //Metemos las listas clasificadas en el model
        model.addAttribute("platos", platosPrincipales);
        model.addAttribute("bebidas", bebidas);
        model.addAttribute("postres", postres);
    }
}
