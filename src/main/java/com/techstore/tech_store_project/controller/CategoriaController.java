package com.techstore.tech_store_project.controller;

import com.techstore.tech_store_project.model.Categoria;
import com.techstore.tech_store_project.respository.CategoriaRepository;
import com.techstore.tech_store_project.respository.ProductoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaController(CategoriaRepository categoriaRepository,
                               ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/categorias")
    public String listarCategorias(Model model) {
        List<Categoria> categorias = categoriaRepository.findAll();

        Map<Long, Long> conteos = new HashMap<>();
        for (Categoria c : categorias) {
            conteos.put(c.getId(), productoRepository.countByCategoriaId(c.getId()));
        }

        model.addAttribute("listaCategorias", categorias);
        model.addAttribute("conteosProductos", conteos);
        return "categorias";
    }

    // RF-04: Registrar nueva categoría validando que el nombre no exista previamente
    @PostMapping("/categorias/guardar")
    public String guardarCategoria(Categoria categoria, Model model) {
        // RF-04: Validar que no exista otra categoría con el mismo nombre (case-insensitive)
        if (categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())) {
            List<Categoria> categorias = categoriaRepository.findAll();
            Map<Long, Long> conteos = new HashMap<>();
            for (Categoria c : categorias) {
                conteos.put(c.getId(), productoRepository.countByCategoriaId(c.getId()));
            }
            model.addAttribute("listaCategorias", categorias);
            model.addAttribute("conteosProductos", conteos);
            model.addAttribute("errorModal", true);
            model.addAttribute("categoriaMantenida", categoria);
            return "categorias";
        }

        // RF-04: Generar código automático de categoría (CAT-01, CAT-02, etc.)
        long total = categoriaRepository.count() + 1;
        categoria.setCodigo(String.format("CAT-%02d", total));
        categoria.setActiva(true);
        categoriaRepository.save(categoria);

        return "redirect:/categorias?exito";
    }

    @PostMapping("/categorias/actualizar")
    public String actualizarCategoria(Categoria categoriaActualizada) {
        Categoria existente = categoriaRepository.findById(categoriaActualizada.getId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada: " + categoriaActualizada.getId()));

        if (!existente.getNombre().equalsIgnoreCase(categoriaActualizada.getNombre()) &&
                categoriaRepository.existsByNombreIgnoreCase(categoriaActualizada.getNombre())) {
            return "redirect:/categorias?errorDuplicado";
        }

        existente.setNombre(categoriaActualizada.getNombre());
        existente.setDescripcion(categoriaActualizada.getDescripcion());
        categoriaRepository.save(existente);

        return "redirect:/categorias?exitoActualizar";
    }

    @PostMapping("/categorias/estado")
    public String cambiarEstadoCategoria(@RequestParam Long id) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada: " + id));
        cat.setActiva(!cat.isActiva());
        categoriaRepository.save(cat);
        return "redirect:/categorias?exitoEstado";
    }

    @PostMapping("/categorias/eliminar")
    public String eliminarCategoria(@RequestParam Long id) {
        if (productoRepository.countByCategoriaId(id) > 0) {
            return "redirect:/categorias?errorEliminar";
        }
        categoriaRepository.deleteById(id);
        return "redirect:/categorias?exitoEliminar";
    }
}
