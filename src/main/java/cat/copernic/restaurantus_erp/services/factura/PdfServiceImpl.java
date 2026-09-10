package cat.copernic.restaurantus_erp.services.factura;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import cat.copernic.restaurantus_erp.domain.ArqueoCaja;
import cat.copernic.restaurantus_erp.domain.Ingrediente;
import cat.copernic.restaurantus_erp.domain.Pedido;
import cat.copernic.restaurantus_erp.domain.PedidoMenu;
import cat.copernic.restaurantus_erp.domain.PedidoPlato;
import cat.copernic.restaurantus_erp.domain.Plato;
import cat.copernic.restaurantus_erp.services.ingrediente.IngredienteService;

@Service
public class PdfServiceImpl implements PdfService {

    // Colores corporativos
    private static final Color COLOR_PRIMARIO = new Color(6, 78, 59); // Verde oscuro
    private static final Color COLOR_FONDO_TABLA = new Color(240, 253, 244); // Verde muy clarito
    
    @Override
    public byte[] generarFacturaBytes(Pedido pedido) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();
        // --- FUENTES ---
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, COLOR_PRIMARIO);
        Font fontEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
        Font fontInfo = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        Font fontCabeceraTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        
        Font fontCelda = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Font fontCeldaDetalle = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.DARK_GRAY);
        
        Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.WHITE);

        // --- ENCABEZADO ---
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);

        // Lado Izquierdo
        PdfPCell cellLogo = new PdfPCell();
        cellLogo.setBorder(Rectangle.NO_BORDER);
        cellLogo.addElement(new Paragraph("RESTAURANTUS", fontTitulo));
        cellLogo.addElement(new Paragraph("C/ Gastronomía 123\n08000 Barcelona\nNIF: B-12345678", fontInfo));

        // Lado Derecho
        PdfPCell cellDatos = new PdfPCell();
        cellDatos.setBorder(Rectangle.NO_BORDER);
        cellDatos.setHorizontalAlignment(Element.ALIGN_RIGHT);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Paragraph pNum = new Paragraph("FACTURA Nº: " + pedido.getId(), fontEmpresa);
        pNum.setAlignment(Element.ALIGN_RIGHT);
        Paragraph pFecha = new Paragraph("FECHA: " + LocalDateTime.now().format(dtf), fontInfo);
        pFecha.setAlignment(Element.ALIGN_RIGHT);
        
        // Control de nulos para la mesa
        String nombreMesa = "Barra";
        if (pedido.getMesa() != null) {
             nombreMesa = (pedido.getMesa().getNumMesa() != null) ? pedido.getMesa().getNumMesa() : "Mesa " + pedido.getMesa().getIdMesa();
        }
        Paragraph pMesa = new Paragraph("MESA: " + nombreMesa, fontInfo);
        pMesa.setAlignment(Element.ALIGN_RIGHT);

        // --- SECCIÓN: MÉTODO DE PAGO ---
        String metodo = "PENDIENTE";
        if (pedido.getMetodoPago() != null) {
            metodo = pedido.getMetodoPago().toString();
        }
        Paragraph pPago = new Paragraph("MÉTODO DE PAGO: " + metodo, fontInfo);
        pPago.setAlignment(Element.ALIGN_RIGHT);

        cellDatos.addElement(pNum);
        cellDatos.addElement(pFecha);
        cellDatos.addElement(pMesa);
        cellDatos.addElement(pPago);

        headerTable.addCell(cellLogo);
        headerTable.addCell(cellDatos);
        document.add(headerTable);

        document.add(new Paragraph("\n\n"));

        // --- TABLA DE PRODUCTOS ---
        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100f);
        tabla.setWidths(new float[] { 1f, 4f, 1.5f, 1.5f });
        tabla.setHeaderRows(1);

        agregarCabecera(tabla, "CANT", fontCabeceraTabla);
        agregarCabecera(tabla, "DESCRIPCIÓN", fontCabeceraTabla);
        agregarCabecera(tabla, "PRECIO", fontCabeceraTabla);
        agregarCabecera(tabla, "TOTAL", fontCabeceraTabla);

        double granTotal = 0.0;
        DecimalFormat df = new DecimalFormat("#,##0.00€");
        boolean alternarColor = false;

        // --- 1. PLATOS INDIVIDUALES ---
        if (pedido.getDetallesPlatos() != null) {
            for (PedidoPlato det : pedido.getDetallesPlatos()) {
                if (det.isEsMenu()) {
                    continue; 
                }

                Double precio = (det.getPlato().getPrecio() != null) ? det.getPlato().getPrecio() : 0.0;
                double subtotal = precio * det.getCantidad();
                granTotal += subtotal;

                agregarFila(tabla, String.valueOf(det.getCantidad()), fontCelda, alternarColor, Element.ALIGN_CENTER);
                agregarFila(tabla, det.getPlato().getNombre(), fontCelda, alternarColor, Element.ALIGN_LEFT);
                agregarFila(tabla, df.format(precio), fontCelda, alternarColor, Element.ALIGN_RIGHT);
                agregarFila(tabla, df.format(subtotal), fontCelda, alternarColor, Element.ALIGN_RIGHT);

                alternarColor = !alternarColor;
            }
        }

        // --- 2. MENÚS COMPLETOS ---
        if (pedido.getDetallesMenus() != null) {
            for (PedidoMenu det : pedido.getDetallesMenus()) {
                Double precio = (det.getMenu().getPrecio() != null) ? det.getMenu().getPrecio() : 0.0;
                double subtotal = precio * det.getCantidad();
                granTotal += subtotal;

                // A) LÍNEA PRINCIPAL DEL MENÚ
                agregarFila(tabla, String.valueOf(det.getCantidad()), fontCelda, alternarColor, Element.ALIGN_CENTER);
                agregarFila(tabla, "MENÚ: " + det.getMenu().getNombre(), fontCelda, alternarColor, Element.ALIGN_LEFT);
                agregarFila(tabla, df.format(precio), fontCelda, alternarColor, Element.ALIGN_RIGHT);
                agregarFila(tabla, df.format(subtotal), fontCelda, alternarColor, Element.ALIGN_RIGHT);

                // B) DESGLOSE INFORMATIVO
                if (det.getMenu().getPlatos() != null && !det.getMenu().getPlatos().isEmpty()) {
                    for (Plato plato : det.getMenu().getPlatos()) {
                        agregarFila(tabla, "", fontCeldaDetalle, alternarColor, Element.ALIGN_CENTER);
                        agregarFila(tabla, "   - " + plato.getNombre(), fontCeldaDetalle, alternarColor, Element.ALIGN_LEFT);
                        agregarFila(tabla, "", fontCeldaDetalle, alternarColor, Element.ALIGN_RIGHT);
                        agregarFila(tabla, "", fontCeldaDetalle, alternarColor, Element.ALIGN_RIGHT);
                    }
                }
                alternarColor = !alternarColor;
            }
        }

        tabla.setSpacingAfter(20f);
        document.add(tabla);

        // --- TOTAL ---
        PdfPTable tablaTotal = new PdfPTable(2);
        tablaTotal.setWidthPercentage(40); 
        tablaTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);

        double base = granTotal / 1.10;
        double iva = granTotal - base;

        agregarFilaTotal(tablaTotal, "Base Imponible:", df.format(base), fontInfo);
        agregarFilaTotal(tablaTotal, "IVA (10%):", df.format(iva), fontInfo);

        PdfPCell celdaTotalLabel = new PdfPCell(new Phrase("TOTAL A PAGAR", fontTotal));
        celdaTotalLabel.setBackgroundColor(COLOR_PRIMARIO);
        celdaTotalLabel.setPadding(10);
        celdaTotalLabel.setBorder(Rectangle.NO_BORDER);

        PdfPCell celdaTotalValue = new PdfPCell(new Phrase(df.format(granTotal), fontTotal));
        celdaTotalValue.setBackgroundColor(COLOR_PRIMARIO);
        celdaTotalValue.setPadding(10);
        celdaTotalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaTotalValue.setBorder(Rectangle.NO_BORDER);

        tablaTotal.addCell(celdaTotalLabel);
        tablaTotal.addCell(celdaTotalValue);

        // --- HE ELIMINADO EL BLOQUE DE CAMBIO PORQUE LA VARIABLE NO EXISTE ---
        // Si en el futuro quieres añadir el cambio, tendrás que añadir un campo
        // 'dineroEntregado' en la entidad Pedido.

        document.add(tablaTotal);

        // --- PIE ---
        document.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("Gracias por su visita.\nDocumento generado electrónicamente.", fontInfo);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    @Override
    public byte[] generarComprobanteArqueoBytes(ArqueoCaja arqueo, String platoTop, String menuTop) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        // --- FUENTES ARQUEO ---
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, COLOR_PRIMARIO);
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.GRAY);
        Font fontValor = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
        Font fontDiferenciaPositiva = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(16, 185, 129)); 
        Font fontDiferenciaNegativa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.RED);

        DecimalFormat df = new DecimalFormat("#,##0.00€");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // --- ENCABEZADO ---
        Paragraph titulo = new Paragraph("RESUMEN DEL CIERRE #" + arqueo.getId(), fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        
        Paragraph subtitulo = new Paragraph("Informe Detallado de Arqueo de Caja", fontLabel);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(20);
        document.add(subtitulo);

        // --- BLOQUE DE INDICADORES ---
        PdfPTable tablaTotales = new PdfPTable(3);
        tablaTotales.setWidthPercentage(100);
        tablaTotales.setSpacingAfter(20);

        double totalVentas = (arqueo.getVentasEfectivo() != null ? arqueo.getVentasEfectivo() : 0.0) + 
                             (arqueo.getVentasTarjeta() != null ? arqueo.getVentasTarjeta() : 0.0);
        agregarCeldaIndicador(tablaTotales, "TOTAL VENTAS", df.format(totalVentas), fontLabel, fontSubtitulo);
        
        agregarCeldaIndicador(tablaTotales, "EFECTIVO REAL", df.format(arqueo.getEfectivoReal()), fontLabel, fontSubtitulo);
        
        Font fontDif = (arqueo.getDiferenciaRT() >= 0) ? fontDiferenciaPositiva : fontDiferenciaNegativa;
        String signo = (arqueo.getDiferenciaRT() > 0) ? "+" : "";
        agregarCeldaIndicador(tablaTotales, "DIFERENCIA", signo + df.format(arqueo.getDiferenciaRT()), fontLabel, fontDif);

        document.add(tablaTotales);

        // --- BLOQUE: PRODUCTOS ESTRELLA ---
        PdfPTable tablaEstrellas = new PdfPTable(2);
        tablaEstrellas.setWidthPercentage(100);
        tablaEstrellas.setSpacingAfter(20);

        PdfPCell celdaPlato = new PdfPCell();
        celdaPlato.setPadding(10);
        celdaPlato.setBorderColor(new Color(59, 130, 246)); 
        celdaPlato.setBorderWidthLeft(4f);
        celdaPlato.addElement(new Paragraph("⭐ PLATO ESTRELLA", fontLabel));
        celdaPlato.addElement(new Paragraph(platoTop != null ? platoTop : "Sin datos", fontSubtitulo));
        
        PdfPCell celdaMenu = new PdfPCell();
        celdaMenu.setPadding(10);
        celdaMenu.setBorderColor(new Color(139, 92, 246)); 
        celdaMenu.setBorderWidthLeft(4f);
        celdaMenu.addElement(new Paragraph("🏆 MENÚ ESTRELLA", fontLabel));
        celdaMenu.addElement(new Paragraph(menuTop != null ? menuTop : "Sin datos", fontSubtitulo));

        tablaEstrellas.addCell(celdaPlato);
        tablaEstrellas.addCell(celdaMenu);
        document.add(tablaEstrellas);

        // --- CUERPO: TIEMPOS Y DESGLOSE ---
        PdfPTable cuerpo = new PdfPTable(2);
        cuerpo.setWidthPercentage(100);
        cuerpo.setWidths(new float[]{1, 1});

        PdfPCell celdaTiempos = new PdfPCell();
        celdaTiempos.setBorder(Rectangle.NO_BORDER);
        celdaTiempos.addElement(new Paragraph("TIEMPOS DEL TURNO", fontSubtitulo));
        celdaTiempos.addElement(new Paragraph("\n"));
        celdaTiempos.addElement(new Paragraph("Apertura: ", fontLabel));
        celdaTiempos.addElement(new Paragraph(arqueo.getFechaApertura().format(dtf), fontValor));
        celdaTiempos.addElement(new Paragraph("\nCierre: ", fontLabel));
        String fechaCierre = (arqueo.getFechaCierre() != null) ? arqueo.getFechaCierre().format(dtf) : "Turno Abierto";
        celdaTiempos.addElement(new Paragraph(fechaCierre, fontValor));
        celdaTiempos.addElement(new Paragraph("\nEstado: ", fontLabel));
        celdaTiempos.addElement(new Paragraph(arqueo.getCajaAbierta() == 1 ? "ABIERTA" : "CERRADA", fontValor));

        PdfPCell celdaDesglose = new PdfPCell();
        celdaDesglose.setBorder(Rectangle.NO_BORDER);
        celdaDesglose.setBackgroundColor(COLOR_FONDO_TABLA);
        celdaDesglose.setPadding(15);
        
        celdaDesglose.addElement(new Paragraph("DESGLOSE DE CAJA", fontSubtitulo));
        celdaDesglose.addElement(new Paragraph("\n"));
        
        agregarLineaDesglose(celdaDesglose, "Fondo Inicial:", df.format(arqueo.getEfectivoInicial()), fontLabel, fontValor);
        agregarLineaDesglose(celdaDesglose, "Ventas Efectivo:", df.format(arqueo.getVentasEfectivo()), fontLabel, fontValor);
        agregarLineaDesglose(celdaDesglose, "Ventas Tarjeta:", df.format(arqueo.getVentasTarjeta()), fontLabel, fontValor);
        agregarLineaDesglose(celdaDesglose, "Ventas Totales:", df.format(totalVentas), fontLabel, fontValor);
        
        celdaDesglose.addElement(new Paragraph("__________________________", fontLabel));
        agregarLineaDesglose(celdaDesglose, "EFECTIVO TEÓRICO:", df.format(arqueo.getEfectivoTeorico()), fontLabel, fontSubtitulo);

        cuerpo.addCell(celdaTiempos);
        cuerpo.addCell(celdaDesglose);
        document.add(cuerpo);

        document.add(new Paragraph("\n\n\n"));
        Paragraph p = new Paragraph("Restaurantus ERP - Comprobante Oficial de Auditoría", fontLabel);
        p.setAlignment(Element.ALIGN_CENTER);
        document.add(p);

        document.close();
        return baos.toByteArray();
    }

    // --- MÉTODOS AUXILIARES ---

    private void agregarCeldaIndicador(PdfPTable tabla, String label, String valor, Font fLabel, Font fValor) {
        PdfPCell celda = new PdfPCell();
        celda.setPadding(10);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setBorderColor(new Color(226, 232, 240));
        
        Paragraph pLabel = new Paragraph(label, fLabel);
        pLabel.setAlignment(Element.ALIGN_CENTER);
        celda.addElement(pLabel);
        
        Paragraph pValor = new Paragraph(valor, fValor);
        pValor.setAlignment(Element.ALIGN_CENTER);
        celda.addElement(pValor);
        
        tabla.addCell(celda);
    }

    private void agregarLineaDesglose(PdfPCell celdaPrincipal, String label, String valor, Font fLabel, Font fValor) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(label + " ", fLabel));
        p.add(new Phrase(valor, fValor));
        p.setSpacingAfter(5);
        celdaPrincipal.addElement(p);
    }

    private void agregarCabecera(PdfPTable tabla, String texto, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setBackgroundColor(COLOR_PRIMARIO);
        celda.setPadding(8);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setBorderColor(COLOR_PRIMARIO);
        tabla.addCell(celda);
    }

    private void agregarFila(PdfPTable tabla, String texto, Font font, boolean esColorAlterno, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setBackgroundColor(esColorAlterno ? COLOR_FONDO_TABLA : Color.WHITE);
        celda.setPadding(6);
        celda.setHorizontalAlignment(alineacion);
        celda.setBorderColor(new Color(230, 230, 230)); 
        tabla.addCell(celda);
    }

    private void agregarFilaTotal(PdfPTable tabla, String etiqueta, String valor, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(etiqueta, font));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        PdfPCell c2 = new PdfPCell(new Phrase(valor, font));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(c1);
        tabla.addCell(c2);
    }

    @Override
    public byte[] generarInformeInventarioBytes(List<Ingrediente> ingredientes, IngredienteService ingredienteService) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        // Fuentes
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, COLOR_PRIMARIO);
        Font fontCabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Font fontCelda = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

        // Encabezado
        Paragraph titulo = new Paragraph("INFORME DE INVENTARIO - ALMACÉN", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph subtitulo = new Paragraph("Generado el: " + fecha, FontFactory.getFont(FontFactory.HELVETICA, 10));
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(20);
        document.add(subtitulo);

        // Tabla (7 columnas: Ingrediente, Stock, Unidad, Lotes, Ubicación, Precio, Total)
        PdfPTable tabla = new PdfPTable(7);
        tabla.setWidthPercentage(100f);
        tabla.setWidths(new float[] { 2.8f, 1.3f, 1.5f, 1.2f, 1.7f, 1.7f, 1.8f });

        // Cabeceras
        String[] headers = {"INGREDIENTE", "STOCK", "UNIDAD", "LOTES", "UBICACIÓN", "PRECIO U.", "SUBTOTAL"};
        for (String h : headers) {
            agregarCabecera(tabla, h, fontCabecera);
        }

        double valorTotalInventario = 0.0;
        DecimalFormat df = new DecimalFormat("#,##0.00€");
        boolean alternar = false;

        for (Ingrediente ing : ingredientes) {
            int numLotes = ingredienteService.contarLotesPorIngrediente(ing.getId());
            double subtotal = ing.getStockActual() * ing.getPrecioUnitario();
            valorTotalInventario += subtotal;

            agregarFila(tabla, ing.getNombre(), fontCelda, alternar, Element.ALIGN_LEFT);
            agregarFila(tabla, String.valueOf(ing.getStockActual()), fontCelda, alternar, Element.ALIGN_CENTER);
            agregarFila(tabla, ing.getUnidadMedida(), fontCelda, alternar, Element.ALIGN_CENTER);
            agregarFila(tabla, String.valueOf(numLotes), fontCelda, alternar, Element.ALIGN_CENTER);
            agregarFila(tabla, ing.getUbicacion(), fontCelda, alternar, Element.ALIGN_LEFT);
            agregarFila(tabla, df.format(ing.getPrecioUnitario()), fontCelda, alternar, Element.ALIGN_RIGHT);
            agregarFila(tabla, df.format(subtotal), fontCelda, alternar, Element.ALIGN_RIGHT);
            
            alternar = !alternar;
        }

        document.add(tabla);

        // Total final
        Paragraph totalFinal = new Paragraph("\nVALOR TOTAL DEL INVENTARIO: " + df.format(valorTotalInventario), 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARIO));
        totalFinal.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalFinal);

        document.close();
        return baos.toByteArray();
    }
}