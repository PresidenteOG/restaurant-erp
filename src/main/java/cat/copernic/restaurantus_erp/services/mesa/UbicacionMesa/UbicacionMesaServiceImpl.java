package cat.copernic.restaurantus_erp.services.mesa.UbicacionMesa;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cat.copernic.restaurantus_erp.dao.mysql.UbicacionMesaDAO;
import cat.copernic.restaurantus_erp.domain.UbicacionMesa;

@Service
public class UbicacionMesaServiceImpl implements UbicacionMesaService {

    @Autowired
    private UbicacionMesaDAO ubicacionMesaDAO;

    @Override
    public List<String> listarNombresUbicaciones() {
        return ubicacionMesaDAO.findAllNombres();
    }

    @Override
    public void guardarUbicacionMesa(String nombre) {
        UbicacionMesa ubMesa = new UbicacionMesa(nombre);
        ubicacionMesaDAO.save(ubMesa);
    }

}
