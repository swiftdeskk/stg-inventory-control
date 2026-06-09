package com.techstore.tech_store_project.controller;

import com.techstore.tech_store_project.model.Marca;
import com.techstore.tech_store_project.respository.MarcaRepository;
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
public class MarcaController {

    private final MarcaRepository marcaRepository;
    private final ProductoRepository productoRepository;

    public MarcaController(MarcaRepository marcaRepository,
                           ProductoRepository productoRepository) {
        this.marcaRepository = marcaRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/marcas")
    public String listarMarcas(Model model) {
        List<Marca> marcas = marcaRepository.findAll();
        Map<Long, Long> conteos = new HashMap<>();
        for (Marca m : marcas) {
            conteos.put(m.getId(), productoRepository.countByMarcaId(m.getId()));
        }
        model.addAttribute("listaMarcas", marcas);
        model.addAttribute("conteosProductos", conteos);
        return "marcas";
    }

    @PostMapping("/marcas/guardar")
    public String guardarMarca(Marca marca) {
        if (marcaRepository.existsByNombreIgnoreCase(marca.getNombre())) {
            return "redirect:/marcas?errorDuplicado";
        }
        marca.setActiva(true);
        marcaRepository.save(marca);
        return "redirect:/marcas?exito";
    }

    @PostMapping("/marcas/actualizar")
    public String actualizarMarca(Marca marcaActualizada) {
        Marca marcaExistente = marcaRepository.findById(marcaActualizada.getId())
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada"));

        if (!marcaExistente.getNombre().equalsIgnoreCase(marcaActualizada.getNombre()) &&
                marcaRepository.existsByNombreIgnoreCase(marcaActualizada.getNombre())) {
            return "redirect:/marcas?errorDuplicado";
        }

        marcaExistente.setNombre(marcaActualizada.getNombre());
        marcaExistente.setDescripcion(marcaActualizada.getDescripcion());
        marcaRepository.save(marcaExistente);

        return "redirect:/marcas?exitoActualizar";
    }

    @PostMapping("/marcas/estado")
    public String cambiarEstadoMarca(@RequestParam Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada: " + id));
        marca.setActiva(!marca.isActiva());
        marcaRepository.save(marca);
        return "redirect:/marcas?exitoEstado";
    }

    @PostMapping("/marcas/eliminar")
    public String eliminarMarca(@RequestParam Long id) {
        if (productoRepository.countByMarcaId(id) > 0) {
            return "redirect:/marcas?errorEliminar";
        }
        marcaRepository.deleteById(id);
        return "redirect:/marcas?exitoEliminar";
    }
}