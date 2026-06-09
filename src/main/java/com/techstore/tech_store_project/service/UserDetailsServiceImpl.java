package com.techstore.tech_store_project.service;

import com.techstore.tech_store_project.model.Usuario;
import com.techstore.tech_store_project.respository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // RF-01: Validar credenciales comparando usuario y contraseña con la base de datos
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // RF-01: Buscar usuario en BD por username
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return User.builder()
                .username(usuario.getUsername())
                // RNF-02: Contraseña almacenada en formato cifrado (BCrypt)
                .password(usuario.getPassword())
                // RF-03: Asignar roles de permisos específicos del usuario
                .roles(usuario.getRol().replace("ROLE_", ""))
                .disabled(!usuario.isActivo())
                // RF-02: Bloquear cuenta si tiene 3 intentos fallidos consecutivos
                .accountLocked(usuario.isCuentaBloqueada())
                .build();
    }
}
