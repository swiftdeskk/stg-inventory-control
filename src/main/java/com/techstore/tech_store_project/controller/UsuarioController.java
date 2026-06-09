package com.techstore.tech_store_project.controller;

import com.techstore.tech_store_project.model.Usuario;
import com.techstore.tech_store_project.respository.RolRepository;
import com.techstore.tech_store_project.respository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // RF-03: Listar todos los usuarios registrados
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("listaUsuarios", usuarioRepository.findAll());
        model.addAttribute("listaRoles", rolRepository.findByActivoTrue());
        return "usuarios";
    }

    // RF-03: Registrar nuevo usuario asignando rol de permisos específico
    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(Usuario usuario, Model model) {
        // RF-03: Validar que el username sea único
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            model.addAttribute("listaUsuarios", usuarioRepository.findAll());
            model.addAttribute("listaRoles", rolRepository.findByActivoTrue());
            model.addAttribute("errorModal", "username");
            model.addAttribute("usuarioMantenido", usuario);
            return "usuarios";
        }
        // RF-03: Validar que el correo sea único
        if (usuario.getCorreo() != null && !usuario.getCorreo().isBlank()
                && usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            model.addAttribute("listaUsuarios", usuarioRepository.findAll());
            model.addAttribute("listaRoles", rolRepository.findByActivoTrue());
            model.addAttribute("errorModal", "correo");
            model.addAttribute("usuarioMantenido", usuario);
            return "usuarios";
        }

        // RNF-02: Cifrar contraseña con BCrypt antes de guardar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        // RF-03: Asignar rol con formato ROLE_ADMIN, ROLE_ALMACENERO, ROLE_VENDEDOR
        usuario.setRol(normalizeRol(usuario.getRol()));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        return "redirect:/usuarios?exito";
    }

    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuario(@RequestParam Long id,
                                    @RequestParam String nombreCompleto,
                                    @RequestParam String correo,
                                    @RequestParam String rol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        usuario.setNombreCompleto(nombreCompleto);
        usuario.setCorreo(correo);
        usuario.setRol(normalizeRol(rol));
        usuarioRepository.save(usuario);

        return "redirect:/usuarios?exitoActualizar";
    }

    @PostMapping("/usuarios/toggleBloqueo")
    public String toggleBloqueo(@RequestParam Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        boolean bloqueado = !usuario.isCuentaBloqueada();
        usuario.setCuentaBloqueada(bloqueado);
        if (!bloqueado) usuario.setIntentosFallidos(0);
        usuarioRepository.save(usuario);

        return "redirect:/usuarios?exitoBloqueo";
    }

    // Handles both display names ("Administrador") and ROLE_ strings ("ROLE_ADMIN")
    private String normalizeRol(String rolEntrada) {
        if (rolEntrada == null) return "ROLE_VENDEDOR";
        return switch (rolEntrada) {
            case "Administrador" -> "ROLE_ADMIN";
            case "Almacenero"    -> "ROLE_ALMACENERO";
            case "Vendedor"      -> "ROLE_VENDEDOR";
            default              -> rolEntrada.startsWith("ROLE_") ? rolEntrada : "ROLE_VENDEDOR";
        };
    }
}
