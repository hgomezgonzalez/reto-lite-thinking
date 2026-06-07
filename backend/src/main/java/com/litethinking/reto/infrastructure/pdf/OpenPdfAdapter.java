package com.litethinking.reto.infrastructure.pdf;

import com.litethinking.reto.application.dto.InventarioResponse;
import com.litethinking.reto.application.exception.BusinessException;
import com.litethinking.reto.application.port.PdfGenerator;
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
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Adaptador de generacion de PDF con OpenPDF (implementa el puerto PdfGenerator).
 * Produce un reporte corporativo del inventario con membrete y un pie de pagina
 * con los datos de contacto del autor.
 */
@Component
public class OpenPdfAdapter implements PdfGenerator {

    private static final Color BRAND = new Color(13, 71, 161);   // azul corporativo
    private static final Color BRAND_LIGHT = new Color(232, 240, 254);
    private static final String[] MONEDAS = {"COP", "USD", "EUR"};

    private static final String AUTOR = "Hugo Ferney Gomez Gonzalez";
    private static final String CONTACTO = "hgomezgonzalez@gmail.com  |  +57 3168343318";

    @Override
    public byte[] generateInventarioPdf(List<InventarioResponse> filas, String tituloEmpresa) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 54);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new FooterEvent());
            document.open();

            document.add(buildTitle());
            document.add(buildSubtitle(tituloEmpresa));
            document.add(buildTable(filas));

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new BusinessException("No se pudo generar el PDF: " + ex.getMessage());
        }
    }

    private Paragraph buildTitle() {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BRAND);
        Paragraph title = new Paragraph("Lite Thinking - Reporte de Inventario", font);
        title.setSpacingAfter(4);
        return title;
    }

    private Paragraph buildSubtitle(String tituloEmpresa) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
        Paragraph subtitle = new Paragraph("Empresa: " + tituloEmpresa, font);
        subtitle.setSpacingAfter(12);
        return subtitle;
    }

    private PdfPTable buildTable(List<InventarioResponse> filas) {
        PdfPTable table = new PdfPTable(new float[]{3f, 1.5f, 3f, 1.2f, 2f, 1.6f, 1.6f});
        table.setWidthPercentage(100);

        addHeaderCell(table, "Empresa");
        addHeaderCell(table, "Codigo");
        addHeaderCell(table, "Producto");
        addHeaderCell(table, "Cantidad");
        addHeaderCell(table, "Precio COP");
        addHeaderCell(table, "Precio USD");
        addHeaderCell(table, "Precio EUR");

        if (filas.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("Sin registros de inventario"));
            empty.setColspan(7);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            empty.setPadding(8);
            table.addCell(empty);
            return table;
        }

        for (InventarioResponse fila : filas) {
            addBodyCell(table, fila.empresaNombre(), Element.ALIGN_LEFT);
            addBodyCell(table, fila.productoCodigo(), Element.ALIGN_LEFT);
            addBodyCell(table, fila.productoNombre(), Element.ALIGN_LEFT);
            addBodyCell(table, String.valueOf(fila.cantidad()), Element.ALIGN_CENTER);
            for (String moneda : MONEDAS) {
                addBodyCell(table, formato(fila.precios().get(moneda), moneda), Element.ALIGN_RIGHT);
            }
        }
        return table;
    }

    private void addHeaderCell(PdfPTable table, String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BRAND);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, int alignment) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        cell.setBackgroundColor(table.getRows().size() % 2 == 0 ? Color.WHITE : BRAND_LIGHT);
        table.addCell(cell);
    }

    private String formato(BigDecimal valor, String moneda) {
        if (valor == null) {
            return "-";
        }
        return String.format("%s %,.2f", moneda, valor);
    }

    /** Pie de pagina con los datos de contacto del autor en cada hoja. */
    private static class FooterEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle page = document.getPageSize();
            Font font = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
            Phrase footer = new Phrase(AUTOR + "   |   " + CONTACTO, font);
            com.lowagie.text.pdf.ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    footer,
                    (page.getLeft() + page.getRight()) / 2,
                    page.getBottom() + 24,
                    0);
        }
    }
}
