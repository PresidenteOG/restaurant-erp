package com.restaurantus.erp.services.factura;

import java.io.IOException;
import java.util.List;

import com.restaurantus.erp.domain.ArqueoCaja;
import com.restaurantus.erp.domain.Ingrediente;
import com.restaurantus.erp.domain.Pedido;
import com.restaurantus.erp.services.ingrediente.IngredienteService;

public interface PdfService {
    byte[] generarFacturaBytes(Pedido pedido) throws IOException;
    byte[] generarComprobanteArqueoBytes(ArqueoCaja arqueo, String platoTop, String menuTop) throws IOException;
    byte[] generarInformeInventarioBytes(List<Ingrediente> ingredientes, IngredienteService ingredienteService) throws IOException;

    
}