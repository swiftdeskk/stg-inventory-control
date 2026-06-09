package com.techstore.tech_store_project.controller;

import com.techstore.tech_store_project.model.Producto;
import com.techstore.tech_store_project.respository.CategoriaRepository;
import com.techstore.tech_store_project.respository.MarcaRepository;
import com.techstore.tech_store_project.respository.MovimientoRepository;
import com.techstore.tech_store_project.respository.ProductoRepository;
import com.techstore.tech_store_project.respository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class WebController {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoRepository movimientoRepository;

    public WebController(ProductoRepository productoRepository,
                         CategoriaRepository categoriaRepository,
                         MarcaRepository marcaRepository,
                         UsuarioRepository usuarioRepository,
                         MovimientoRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
        this.usuarioRepository = usuarioRepository;
        this.movimientoRepository = movimientoRepository;
    }

    // RF-14: Dashboard que calcula el total de productos activos + RF-15: Alertas de stock bajo
    @GetMapping({"/", "/index"})
    public String dashboard(Model model) {
        long totalProductos = productoRepository.count();
        // RF-14: Calcular total de productos contabilizando solo aquellos en estado "Activo"
        long totalActivos   = productoRepository.countByActivoTrue();

        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalActivos", totalActivos);
        model.addAttribute("totalInactivos", totalProductos - totalActivos);
        model.addAttribute("totalCategorias", categoriaRepository.count());
        model.addAttribute("totalMarcas", marcaRepository.count());
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalEntradas", movimientoRepository.countByTipo("ENTRADA"));
        model.addAttribute("totalSalidas", movimientoRepository.countByTipo("SALIDA"));

        // RF-15: Emitir alerta visual indicando "Stock Bajo" para productos con nivel mínimo
        List<Producto> stockBajo = productoRepository.findProductosConStockBajo();
        model.addAttribute("productosStockBajo", stockBajo);
        model.addAttribute("stockBajoCount", stockBajo.size());

        // Valor total del inventario
        Double valorInventario = productoRepository.calcularValorInventario();
        model.addAttribute("valorInventario", valorInventario != null ? valorInventario : 0.0);

        // Productos activos por categoría (bar chart)
        List<Object[]> catData = productoRepository.countActivosByCategoria();
        List<String> categoriaLabels = new ArrayList<>();
        List<Long> categoriaCounts  = new ArrayList<>();
        for (Object[] row : catData) {
            categoriaLabels.add((String) row[0]);
            categoriaCounts.add((Long) row[1]);
        }
        model.addAttribute("categoriaLabels", categoriaLabels);
        model.addAttribute("categoriaCounts", categoriaCounts);

        // Movimientos por mes — últimos 6 meses (line chart)
        LocalDateTime desde6meses = LocalDateTime.now().minusMonths(6);
        List<Object[]> movData = movimientoRepository.contarMovimientosPorMes(desde6meses);

        DateTimeFormatter fmtMes = DateTimeFormatter.ofPattern("MMM yy", Locale.forLanguageTag("es"));
        List<String> mesesLabels = new ArrayList<>();
        List<Long> entradasMes   = new ArrayList<>();
        List<Long> salidasMes    = new ArrayList<>();

        LocalDate hoy = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate mes = hoy.minusMonths(i);
            int anio  = mes.getYear();
            int numMes = mes.getMonthValue();
            mesesLabels.add(mes.format(fmtMes));

            long ent = movData.stream()
                    .filter(r -> ((Number) r[0]).intValue() == anio
                              && ((Number) r[1]).intValue() == numMes
                              && "ENTRADA".equals(r[2]))
                    .mapToLong(r -> ((Number) r[3]).longValue()).sum();
            long sal = movData.stream()
                    .filter(r -> ((Number) r[0]).intValue() == anio
                              && ((Number) r[1]).intValue() == numMes
                              && "SALIDA".equals(r[2]))
                    .mapToLong(r -> ((Number) r[3]).longValue()).sum();

            entradasMes.add(ent);
            salidasMes.add(sal);
        }
        model.addAttribute("mesesLabels", mesesLabels);
        model.addAttribute("entradasMes", entradasMes);
        model.addAttribute("salidasMes", salidasMes);

        // Últimos 5 movimientos
        model.addAttribute("ultimosMovimientos", movimientoRepository.findTop5ByOrderByFechaDesc());

        return "index";
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/perfil")
    public String perfil() { return "perfil"; }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() { return "error/403"; }

    // API: stock-bajo badge count for navbar
    @GetMapping("/api/stock-bajo/count")
    @ResponseBody
    public Map<String, Long> stockBajoCount() {
        return Map.of("count", productoRepository.countProductosConStockBajo());
    }
}
