package com.techstore.tech_store_project.config;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("usuarioActual")
    public String usuarioActual(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return "Usuario";
        return auth.getName();
    }

    @ModelAttribute("usuarioRol")
    public String usuarioRol(Authentication auth) {
        if (auth == null || auth.getAuthorities().isEmpty()) return "";
        return auth.getAuthorities().iterator().next().getAuthority();
    }
}
