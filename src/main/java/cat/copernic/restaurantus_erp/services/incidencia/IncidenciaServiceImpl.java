package cat.copernic.restaurantus_erp.services.incidencia;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.restaurantus_erp.dao.mysql.IncidenciaDAO;
import cat.copernic.restaurantus_erp.domain.Incidencia;

@Service
public class IncidenciaServiceImpl implements IncidenciaService {

    @Autowired
    private IncidenciaDAO incidenciaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Incidencia> listarTodas() {
        return incidenciaRepository.findAllByOrderByFechaCreacionDesc();
    }

    @Override
    @Transactional
    public Incidencia guardar(Incidencia incidencia) {
        // CAMBIO AQUI: Añadir return
        return incidenciaRepository.save(incidencia);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        incidenciaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Incidencia encontrarPorId(Long id) {
        return incidenciaRepository.findById(id).orElse(null);
    }
    
    //Home dashboard - contar incidencias abiertas
    @Override
    @Transactional(readOnly = true)
    public Long contarIncidenciasAbiertas() {
        return incidenciaRepository.countByEstado("abierta");
    }
}