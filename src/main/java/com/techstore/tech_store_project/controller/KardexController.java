package com.techstore.tech_store_project.controller;

import com.techstore.tech_store_project.model.Movimiento;
import com.techstore.tech_store_project.model.Producto;
import com.techstore.tech_store_project.respository.MovimientoRepository;
import com.techstore.tech_store_project.respository.ProductoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
public class KardexController {

    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;

    public KardexController(ProductoRepository productoRepository,
                            MovimientoRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    // RF-09: Registrar entrada de mercadería sumando cantidad al stock actual
    @GetMapping("/entradas")
    public String listarEntradas(Model model) {
        // RF-13: Consultar historial del Kardex
        model.addAttribute("listaMovimientos", movimientoRepository.findByTipoOrderByFechaDesc("ENTRADA"));
        model.addAttribute("listaProductos", productoRepository.findAll().stream()
                .filter(Producto::isActivo).toList());
        return "entradas";
    }

    // RF-09: Guardar entrada de mercadería con trazabilidad completa
    @PostMapping("/entradas/guardar")
    public String guardarEntrada(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad,
            @RequestParam(required = false, defaultValue = "") String documentoRef,
            @RequestParam(required = false, defaultValue = "") String observacion,
            Authentication auth) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));

        if (cantidad <= 0) return "redirect:/entradas?errorCantidad";

        int stockAnterior = producto.getStock();
        // RF-09: Sumar cantidad al stock actual del producto
        producto.setStock(stockAnterior + cantidad);
        productoRepository.save(producto);

        // RF-12: Generar registro inalterable en el Kardex para auditoría
        Movimiento mov = new Movimiento();
        mov.setProducto(producto);
        mov.setTipo("ENTRADA");
        mov.setCantidad(cantidad);
        mov.setStockAnterior(stockAnterior);
        mov.setStockResultante(producto.getStock());
        mov.setFecha(LocalDateTime.now());
        mov.setUsuarioNombre(auth != null ? auth.getName() : "sistema");
        mov.setDocumentoRef(documentoRef);
        mov.setObservacion(observacion);
        movimientoRepository.save(mov);

        return "redirect:/entradas?exito";
    }

    // RF-10: Registrar salida de mercadería restando cantidad del stock actual
    @GetMapping("/salidas")
    public String listarSalidas(Model model) {
        // RF-13: Consultar historial del Kardex
        model.addAttribute("listaMovimientos", movimientoRepository.findByTipoOrderByFechaDesc("SALIDA"));
        model.addAttribute("listaProductos", productoRepository.findAll().stream()
                .filter(Producto::isActivo).toList());
        return "salidas";
    }

    // RF-10 + RF-11: Registrar salida y validar que no supere el stock disponible
    @PostMapping("/salidas/guardar")
    public String guardarSalida(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad,
            @RequestParam(required = false, defaultValue = "") String documentoRef,
            @RequestParam(required = false, defaultValue = "") String observacion,
            Authentication auth) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));

        if (cantidad <= 0) return "redirect:/salidas?errorCantidad";

        // RF-11: Validar que la cantidad solicitada no supere el stock disponible
        if (producto.getStock() < cantidad) return "redirect:/salidas?errorStock";

        int stockAnterior = producto.getStock();
        // RF-10: Restar cantidad del stock actual del producto
        producto.setStock(stockAnterior - cantidad);
        productoRepository.save(producto);

        // RF-12: Generar registro inalterable en el Kardex para auditoría
        Movimiento mov = new Movimiento();
        mov.setProducto(producto);
        mov.setTipo("SALIDA");
        mov.setCantidad(cantidad);
        mov.setStockAnterior(stockAnterior);
        mov.setStockResultante(producto.getStock());
        mov.setFecha(LocalDateTime.now());
        mov.setUsuarioNombre(auth != null ? auth.getName() : "sistema");
        mov.setDocumentoRef(documentoRef);
        mov.setObservacion(observacion);
        movimientoRepository.save(mov);

        return "redirect:/salidas?exito";
    }

    // RF-13: Consultar historial del Kardex filtrando por rango de fechas o tipo de movimiento
    @GetMapping("/movimientos")
    public String listarMovimientos(
            @RequestParam(required = false, defaultValue = "") String tipo,
            @RequestParam(required = false, defaultValue = "") String fechaDesde,
            @RequestParam(required = false, defaultValue = "") String fechaHasta,
            Model model) {

        // RF-13: Filtrar por rango de fechas
        LocalDateTime desde = fechaDesde.isEmpty()
                ? LocalDateTime.of(2000, 1, 1, 0, 0)
                : LocalDate.parse(fechaDesde).atStartOfDay();
        LocalDateTime hasta = fechaHasta.isEmpty()
                ? LocalDateTime.now().plusYears(100)
                : LocalDate.parse(fechaHasta).atTime(LocalTime.MAX);

        // RF-13: Listar movimientos (ENTRADA/SALIDA) en el rango especificado
        model.addAttribute("listaMovimientos", movimientoRepository.filtrar(tipo, desde, hasta));
        model.addAttribute("paramTipo", tipo);
        model.addAttribute("paramFechaDesde", fechaDesde);
        model.addAttribute("paramFechaHasta", fechaHasta);
        return "movimientos";
    }
}
