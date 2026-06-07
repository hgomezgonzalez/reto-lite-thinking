package com.litethinking.reto.application.service;

import com.litethinking.reto.application.dto.InventarioRequest;
import com.litethinking.reto.application.dto.InventarioResponse;
import com.litethinking.reto.application.exception.ResourceNotFoundException;
import com.litethinking.reto.application.mapper.InventarioMapper;
import com.litethinking.reto.application.port.CurrencyConverter;
import com.litethinking.reto.application.port.EmailSender;
import com.litethinking.reto.application.port.PdfGenerator;
import com.litethinking.reto.domain.model.Empresa;
import com.litethinking.reto.domain.model.Inventario;
import com.litethinking.reto.domain.model.Producto;
import com.litethinking.reto.infrastructure.persistence.EmpresaRepository;
import com.litethinking.reto.infrastructure.persistence.InventarioRepository;
import com.litethinking.reto.infrastructure.persistence.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de Inventario (requisitos d y e): listar productos por empresa,
 * ajustar stock, generar el PDF del inventario y enviarlo por correo.
 */
@Service
@Transactional
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ProductoRepository productoRepository;
    private final CurrencyConverter currencyConverter;
    private final PdfGenerator pdfGenerator;
    private final EmailSender emailSender;

    public InventarioService(InventarioRepository inventarioRepository,
                             EmpresaRepository empresaRepository,
                             ProductoRepository productoRepository,
                             CurrencyConverter currencyConverter,
                             PdfGenerator pdfGenerator,
                             EmailSender emailSender) {
        this.inventarioRepository = inventarioRepository;
        this.empresaRepository = empresaRepository;
        this.productoRepository = productoRepository;
        this.currencyConverter = currencyConverter;
        this.pdfGenerator = pdfGenerator;
        this.emailSender = emailSender;
    }

    @Transactional(readOnly = true)
    public List<InventarioResponse> findAll() {
        return inventarioRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventarioResponse> findByEmpresa(String nit) {
        return inventarioRepository.findByEmpresaNit(nit).stream()
                .map(this::toResponse)
                .toList();
    }

    public InventarioResponse upsert(InventarioRequest request) {
        Empresa empresa = empresaRepository.findById(request.empresaNit())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la empresa con NIT " + request.empresaNit()));
        Producto producto = productoRepository.findById(request.productoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el producto con id " + request.productoId()));

        Inventario inventario = inventarioRepository
                .findByEmpresaNitAndProductoId(empresa.getNit(), producto.getId())
                .orElseGet(() -> {
                    Inventario nuevo = new Inventario();
                    nuevo.setEmpresa(empresa);
                    nuevo.setProducto(producto);
                    return nuevo;
                });
        inventario.setCantidad(request.cantidad());
        return toResponse(inventarioRepository.save(inventario));
    }

    /** Genera el PDF del inventario (todas las empresas o filtrado por una). */
    @Transactional(readOnly = true)
    public byte[] generarPdf(String nit) {
        List<InventarioResponse> filas = (nit == null || nit.isBlank())
                ? findAll()
                : findByEmpresa(nit);
        String titulo = (nit == null || nit.isBlank())
                ? "Todas las empresas"
                : empresaRepository.findById(nit).map(Empresa::getNombre).orElse(nit);
        return pdfGenerator.generateInventarioPdf(filas, titulo);
    }

    /** Genera el PDF y lo envia por correo (requisito d). */
    public void enviarPdfPorCorreo(String nit, String destinatario) {
        byte[] pdf = generarPdf(nit);
        emailSender.sendWithAttachment(
                destinatario,
                "Reporte de Inventario - Lite Thinking",
                "Adjunto encontrara el reporte de inventario en formato PDF.\n\n"
                        + "Generado automaticamente por la aplicacion del reto tecnico.",
                pdf,
                "inventario.pdf");
    }

    private InventarioResponse toResponse(Inventario inventario) {
        Producto producto = inventario.getProducto();
        return InventarioMapper.toResponse(
                inventario,
                currencyConverter.convertToAll(producto.getPrecioBase(), producto.getMonedaBase()));
    }
}
