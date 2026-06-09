package com.techstore.tech_store_project.controller;

import com.techstore.tech_store_project.model.Rol;
import com.techstore.tech_store_project.respository.RolRepository;
import com.techstore.tech_store_project.respository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class RolController {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    public RolController(RolRepository rolRepository, UsuarioRepository usuarioRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/roles")
    public String listarRoles(Model model) {
        var listaRoles = rolRepository.findAll();
        var listaUsuarios = usuarioRepository.findAll();

        Map<Long, Long> conteoPorRol = new HashMap<>();
        for (Rol rol : listaRoles) {
            long count = listaUsuarios.stream()
                    .filter(u -> u.getRol().equals(rol.getNivelAcceso()))
                    .count();
            conteoPorRol.put(rol.getId(), count);
        }

        model.addAttribute("listaRoles", listaRoles);
        model.addAttribute("conteoPorRol", conteoPorRol);
        return "roles";
    }

    @PostMapping("/roles/guardar")
    public String guardarRol(@RequestParam String nombre,
                              @RequestParam(required = false) String descripcion,
                              @RequestParam String nivelAcceso) {
        if (rolRepository.existsByNombreIgnoreCase(nombre)) {
            return "redirect:/roles?error";
        }
        Rol rol = new Rol();
        rol.setNombre(nombre.trim());
        rol.setDescripcion(descripcion);
        rol.setNivelAcceso(nivelAcceso);
        rol.setActivo(true);
        rolRepository.save(rol);
        return "redirect:/roles?exito";
    }

    @PostMapping("/roles/actualizar")
    public String actualizarRol(@RequestParam Long id,
                                 @RequestParam String nombre,
                                 @RequestParam(required = false) String descripcion,
                                 @RequestParam String nivelAcceso) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + id));

        if (!rol.getNombre().equalsIgnoreCase(nombre) && rolRepository.existsByNombreIgnoreCase(nombre)) {
            return "redirect:/roles?errorActualizar";
        }

        rol.setNombre(nombre.trim());
        rol.setDescripcion(descripcion);
        rol.setNivelAcceso(nivelAcceso);
        rolRepository.save(rol);
        return "redirect:/roles?exitoActualizar";
    }
}
