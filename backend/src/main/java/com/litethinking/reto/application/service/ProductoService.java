package com.litethinking.reto.application.service;

import com.litethinking.reto.application.dto.ProductoRequest;
import com.litethinking.reto.application.dto.ProductoResponse;
import com.litethinking.reto.application.exception.BusinessException;
import com.litethinking.reto.application.exception.ResourceNotFoundException;
import com.litethinking.reto.application.mapper.ProductoMapper;
import com.litethinking.reto.application.port.CurrencyConverter;
import com.litethinking.reto.domain.model.Categoria;
import com.litethinking.reto.domain.model.Empresa;
import com.litethinking.reto.domain.model.Inventario;
import com.litethinking.reto.domain.model.Producto;
import com.litethinking.reto.infrastructure.persistence.CategoriaRepository;
import com.litethinking.reto.infrastructure.persistence.EmpresaRepository;
import com.litethinking.reto.infrastructure.persistence.InventarioRepository;
import com.litethinking.reto.infrastructure.persistence.ProductoRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de Producto (requisitos b y e). Al registrar un producto el
 * administrador lo asocia a una empresa, a varias categorias (N-M) y, si envia
 * una cantidad inicial, queda registrado en la tabla inventario.
 */
@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final EmpresaRepository empresaRepository;
    private final CategoriaRepository categoriaRepository;
    private final InventarioRepository inventarioRepository;
    private final CurrencyConverter currencyConverter;

    public ProductoService(ProductoRepository productoRepository,
                           EmpresaRepository empresaRepository,
                           CategoriaRepository categoriaRepository,
                           InventarioRepository inventarioRepository,
                           CurrencyConverter currencyConverter) {
        this.productoRepository = productoRepository;
        this.empresaRepository = empresaRepository;
        this.categoriaRepository = categoriaRepository;
        this.inventarioRepository = inventarioRepository;
        this.currencyConverter = currencyConverter;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> findAll() {
        return productoRepository.findAll().stream()
                .map(this::toResponseWithPrices)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> findByEmpresa(String nit) {
        return productoRepository.findByEmpresaNit(nit).stream()
                .map(this::toResponseWithPrices)
                .toList();
    }

    public ProductoResponse create(ProductoRequest request) {
        if (productoRepository.existsByCodigo(request.codigo())) {
            throw new BusinessException("Ya existe un producto con el codigo " + request.codigo());
        }
        Empresa empresa = getEmpresa(request.empresaNit());

        Producto producto = new Producto();
        producto.setCodigo(request.codigo());
        applyEditableFields(producto, request, empresa);
        Producto guardado = productoRepository.save(producto);

        registrarInventarioSiAplica(guardado, empresa, request.cantidadInventario());

        return toResponseWithPrices(guardado);
    }

    public ProductoResponse update(Long id, ProductoRequest request) {
        Producto producto = getProducto(id);
        if (!producto.getCodigo().equals(request.codigo())
                && productoRepository.existsByCodigo(request.codigo())) {
            throw new BusinessException("Ya existe un producto con el codigo " + request.codigo());
        }
        Empresa empresa = getEmpresa(request.empresaNit());
        producto.setCodigo(request.codigo());
        applyEditableFields(producto, request, empresa);
        return toResponseWithPrices(productoRepository.save(producto));
    }

    public void delete(Long id) {
        Producto producto = getProducto(id);
        productoRepository.delete(producto);
    }

    // ---- helpers privados ------------------------------------------------

    private void applyEditableFields(Producto producto, ProductoRequest request, Empresa empresa) {
        producto.setNombre(request.nombre());
        producto.setCaracteristicas(request.caracteristicas());
        producto.setPrecioBase(request.precioBase());
        producto.setMonedaBase(request.monedaBase());
        producto.setEmpresa(empresa);
        producto.setCategorias(resolverCategorias(request.categoriaIds()));
    }

    private Set<Categoria> resolverCategorias(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        Set<Categoria> encontradas = categoriaRepository.findByIdIn(ids);
        if (encontradas.size() != ids.size()) {
            throw new BusinessException("Una o mas categorias no existen");
        }
        return encontradas;
    }

    private void registrarInventarioSiAplica(Producto producto, Empresa empresa, Integer cantidad) {
        if (cantidad == null) {
            return;
        }
        Inventario inventario = inventarioRepository
                .findByEmpresaNitAndProductoId(empresa.getNit(), producto.getId())
                .orElseGet(() -> {
                    Inventario nuevo = new Inventario();
                    nuevo.setEmpresa(empresa);
                    nuevo.setProducto(producto);
                    return nuevo;
                });
        inventario.setCantidad(cantidad);
        inventarioRepository.save(inventario);
    }

    private ProductoResponse toResponseWithPrices(Producto producto) {
        return ProductoMapper.toResponse(
                producto,
                currencyConverter.convertToAll(producto.getPrecioBase(), producto.getMonedaBase()));
    }

    private Empresa getEmpresa(String nit) {
        return empresaRepository.findById(nit)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la empresa con NIT " + nit));
    }

    private Producto getProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el producto con id " + id));
    }
}
