package cat.copernic.restaurantus_erp.services.factura;

import java.io.IOException;
import java.util.List;

import cat.copernic.restaurantus_erp.domain.ArqueoCaja;
import cat.copernic.restaurantus_erp.domain.Ingrediente;
import cat.copernic.restaurantus_erp.domain.Pedido;
import cat.copernic.restaurantus_erp.services.ingrediente.IngredienteService;

public interface PdfService {
    byte[] generarFacturaBytes(Pedido pedido) throws IOException;
    byte[] generarComprobanteArqueoBytes(ArqueoCaja arqueo, String platoTop, String menuTop) throws IOException;
    byte[] generarInformeInventarioBytes(List<Ingrediente> ingredientes, IngredienteService ingredienteService) throws IOException;

    
}