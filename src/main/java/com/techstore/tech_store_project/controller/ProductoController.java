package com.techstore.tech_store_project.controller;

import com.techstore.tech_store_project.model.Marca;
import com.techstore.tech_store_project.model.PrecioHistorial;
import com.techstore.tech_store_project.model.Producto;
import com.techstore.tech_store_project.respository.CategoriaRepository;
import com.techstore.tech_store_project.respository.MarcaRepository;
import com.techstore.tech_store_project.respository.MovimientoRepository;
import com.techstore.tech_store_project.respository.PrecioHistorialRepository;
import com.techstore.tech_store_project.respository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ProductoController {

    private static final int PAGE_SIZE = 12;

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final PrecioHistorialRepository precioHistorialRepository;
    private final MovimientoRepository movimientoRepository;

    public ProductoController(ProductoRepository productoRepository,
                               CategoriaRepository categoriaRepository,
                               MarcaRepository marcaRepository,
                               PrecioHistorialRepository precioHistorialRepository,
                               MovimientoRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
        this.precioHistorialRepository = precioHistorialRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @PostMapping("/productos/eliminar")
    public String eliminarProducto(@RequestParam Long id) {
        if (movimientoRepository.countByProductoId(id) > 0) {
            return "redirect:/productos?errorEliminar";
        }
        productoRepository.deleteById(id);
        return "redirect:/productos?exitoEliminar";
    }

    // RF-07: Baja Lógica - Cambiar estado del producto a "Inactivo" sin borrar el registro
    @PostMapping("/productos/estado")
    public String cambiarEstado(@RequestParam Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
        // RF-07: Invertir estado (Activo/Inactivo) para proteger historial de ventas
        producto.setActivo(!producto.isActivo());
        productoRepository.save(producto);
        return "redirect:/productos?exitoEstado";
    }

    // RF-08: Filtrar productos por SKU, categoría, marca y estado
    @GetMapping("/productos")
    public String listarProductos(
            @RequestParam(required = false, defaultValue = "") String sku,
            @RequestParam(required = false, defaultValue = "") String categoriaId,
            @RequestParam(required = false, defaultValue = "") String marcaId,
            @RequestParam(required = false, defaultValue = "") String estado,
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model) {

        Long catId = categoriaId.isEmpty() ? null : Long.parseLong(categoriaId);
        Long mrcId = marcaId.isEmpty() ? null : Long.parseLong(marcaId);
        Boolean activo = estado.isEmpty() ? null : Boolean.parseBoolean(estado);
        if (page < 0) page = 0;

        // RF-08: Búsqueda ágil en el catálogo usando filtros combinados
        Page<Producto> paginaProductos = productoRepository.filtrar(
                sku.trim(), catId, mrcId, activo, PageRequest.of(page, PAGE_SIZE));

        model.addAttribute("listaProductos", paginaProductos.getContent());
        model.addAttribute("paginaActual", page);
        model.addAttribute("totalPaginas", paginaProductos.getTotalPages());
        model.addAttribute("totalElementos", paginaProductos.getTotalElements());
        model.addAttribute("listaCategorias", categoriaRepository.findAll().stream()
                .filter(c -> c.isActiva()).toList());
        model.addAttribute("listaMarcas", marcaRepository.findAll().stream()
                .filter(m -> m.isActiva()).toList());
        model.addAttribute("paramSku", sku);
        model.addAttribute("paramCategoria", catId);
        model.addAttribute("paramMarca", mrcId);
        model.addAttribute("paramEstado", activo);

        return "productos";
    }

    // RF-05: Registrar nuevo producto validando que el código SKU sea único
    @PostMapping("/productos/guardar")
    public String guardarProducto(Producto producto,
                                   @RequestParam(required = false) Long marcaId,
                                   @RequestParam(required = false, defaultValue = "0") Integer stockMinimo) {
        // RF-05: Validar que el SKU sea único antes de crear el producto
        if (productoRepository.existsBySkuIgnoreCase(producto.getSku())) {
            return "redirect:/productos?error";
        }
        if (marcaId != null) {
            Marca m = new Marca();
            m.setId(marcaId);
            producto.setMarca(m);
        }
        // RF-05: Inicializar producto como activo con stock en 0
        producto.setActivo(true);
        if (producto.getStock() == null) producto.setStock(0);
        producto.setStockMinimo(stockMinimo);
        productoRepository.save(producto);
        return "redirect:/productos?exito";
    }

    // RF-06: Actualizar información del producto manteniendo el código SKU inalterable
    @PostMapping("/productos/actualizar")
    public String actualizarProducto(Producto productoActualizado,
                                      @RequestParam(required = false) Long marcaId,
                                      @RequestParam(required = false, defaultValue = "0") Integer stockMinimo,
                                      Authentication auth) {
        Producto existente = productoRepository.findById(productoActualizado.getId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoActualizado.getId()));

        // RF-06: Registrar cambios de precio en historial para auditoría
        if (!existente.getPrecio().equals(productoActualizado.getPrecio())) {
            PrecioHistorial hist = new PrecioHistorial();
            hist.setProducto(existente);
            hist.setPrecioAnterior(existente.getPrecio());
            hist.setPrecioNuevo(productoActualizado.getPrecio());
            hist.setFecha(LocalDateTime.now());
            hist.setUsuarioNombre(auth != null ? auth.getName() : "sistema");
            precioHistorialRepository.save(hist);
        }

        // RF-06: Actualizar campos permitidos (nombre, categoría, precio, descripción)
        // NOTA: SKU NO se modifica intencionalmente para mantener integridad de datos
        existente.setNombre(productoActualizado.getNombre());
        existente.setCategoria(productoActualizado.getCategoria());
        existente.setPrecio(productoActualizado.getPrecio());
        existente.setDescripcion(productoActualizado.getDescripcion());
        existente.setStockMinimo(stockMinimo);

        if (marcaId != null) {
            Marca m = new Marca();
            m.setId(marcaId);
            existente.setMarca(m);
        } else {
            existente.setMarca(null);
        }

        productoRepository.save(existente);
        return "redirect:/productos?exitoActualizar";
    }

    // AJAX: validate SKU uniqueness
    @GetMapping("/productos/validar-sku")
    @ResponseBody
    public Map<String, Boolean> validarSku(@RequestParam String sku) {
        return Map.of("existe", productoRepository.existsBySkuIgnoreCase(sku.trim()));
    }

    // AJAX: price history for a product
    @GetMapping("/productos/{id}/precios")
    @ResponseBody
    public List<Map<String, Object>> historialPrecios(@PathVariable Long id) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return precioHistorialRepository.findByProductoIdOrderByFechaDesc(id).stream()
                .map(h -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("fecha", h.getFecha().format(fmt));
                    m.put("anterior", h.getPrecioAnterior());
                    m.put("nuevo", h.getPrecioNuevo());
                    m.put("usuario", h.getUsuarioNombre());
                    return m;
                })
                .collect(Collectors.toList());
    }
}
