package cat.copernic.restaurantus_erp.services.plato;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.restaurantus_erp.dao.mysql.PlatoDAO;
import cat.copernic.restaurantus_erp.domain.Plato;

@Service
public class PlatoServiceImpl implements PlatoService {

    @Autowired
    private PlatoDAO platoDAO;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void guardarPlato(Plato plato, MultipartFile imagen) {
        
        System.out.println("🔵 Iniciando proceso de guardado para: " + plato.getNombre());

        // --- 1. GESTIÓN DE LA IMAGEN (Mantengo tu lógica) ---
        if (imagen != null && !imagen.isEmpty()) {
            try {
                String rutaRaiz = System.getProperty("user.dir");
                Path directorioImagenes = Paths.get(rutaRaiz, "src", "main", "resources", "static", "images", "plato");
                if (!Files.exists(directorioImagenes)) Files.createDirectories(directorioImagenes);

                String nombreFichero = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
                Path rutaCompleta = directorioImagenes.resolve(nombreFichero);
                Files.copy(imagen.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);
                plato.setImgUrl(nombreFichero);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // --- 2. GESTIÓN DE ACTUALIZACIÓN (SOLUCIÓN AL ERROR) ---
        if (plato.getId() != null) {
            // Buscamos la versión actual en la DB
            Plato platoExistente = platoDAO.findById(plato.getId()).orElse(null);
            if (platoExistente != null) {
                // Si no se subió imagen nueva, mantenemos la anterior
                if (plato.getImgUrl() == null) {
                    plato.setImgUrl(platoExistente.getImgUrl());
                }
                // LIMPIAMOS los ingredientes viejos para que Hibernate no intente duplicarlos
                platoExistente.getIngredientes().clear();
                // Forzamos el borrado antes de insertar los nuevos
                platoDAO.saveAndFlush(platoExistente);
            }
        }

        // --- 3. VINCULACIÓN DE NUEVOS INGREDIENTES ---
        if (plato.getIngredientes() != null) {
            // Quitamos vacíos
            plato.getIngredientes().removeIf(pi -> pi.getIngrediente() == null || pi.getIngrediente().getId() == null);
            
            // Asignamos el padre a cada relación
            plato.getIngredientes().forEach(pi -> pi.setPlato(plato));
        }

        // --- 4. GUARDAR EN BASE DE DATOS ---
        // Ahora save() funcionará porque la tabla intermedia ya está limpia para este plato ID
        platoDAO.save(plato);
        System.out.println("✅ Plato e ingredientes guardados sin duplicados.");

        // --- 5. NOTIFICAR WEBSOCKET ---
        sendRefreshSignal();
    }
    
    private void sendRefreshSignal() {
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Esperar un segundo
                System.out.println("🚀 Enviando señal WebSocket REFRESH...");
                messagingTemplate.convertAndSend("/topic/carta", "REFRESH");
                System.out.println("📡 Señal enviada.");
            } catch (Exception e) {
                System.err.println("❌ Error enviando WebSocket: " + e.getMessage());
            }
        }).start();
    }

    @Override
    @Transactional
    public void eliminarPlato(Long id) {
        // 1. Buscamos el plato para saber qué imagen tiene
        Plato plato = platoDAO.findById(id).orElse(null);
        
        if (plato != null) {
            // 2. [NUEVO] Desvincular el plato de cualquier menú
            // Esto borra la fila en la tabla 'menu_platos' y evita el error de Constraint
            platoDAO.eliminarDeMenus(id);

            // 3. Borrar archivo físico de imagen si existe
            if (plato.getImgUrl() != null && !plato.getImgUrl().isEmpty()) {
                try {
                    String rutaRaiz = System.getProperty("user.dir");
                    Path directorioImagenes = Paths.get(rutaRaiz, "src", "main", "resources", "static", "images", "plato");
                    Path rutaArchivo = directorioImagenes.resolve(plato.getImgUrl());
                    
                    Files.deleteIfExists(rutaArchivo);
                    System.out.println("🗑️ Imagen eliminada: " + rutaArchivo);
                } catch (Exception e) {
                    System.err.println("⚠️ No se pudo borrar la imagen: " + e.getMessage());
                }
            }
            
            // 4. Borrar el plato de la BD (Ahora ya no fallará)
            platoDAO.deleteById(id);
            
            // 5. Notificar cambios por WebSocket
            sendRefreshSignal();
        }
    }
}