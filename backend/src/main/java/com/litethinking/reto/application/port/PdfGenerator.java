package com.litethinking.reto.application.port;

import com.litethinking.reto.application.dto.InventarioResponse;
import java.util.List;

/**
 * Puerto de generacion de PDF del inventario (requisito d). La implementacion
 * concreta (OpenPDF) vive en la capa de infraestructura.
 */
public interface PdfGenerator {

    byte[] generateInventarioPdf(List<InventarioResponse> filas, String tituloEmpresa);
}
