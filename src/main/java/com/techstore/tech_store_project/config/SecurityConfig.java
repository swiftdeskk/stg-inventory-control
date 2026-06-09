package com.techstore.tech_store_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // RNF-02: Cifrar todas las contraseñas de usuarios utilizando el algoritmo hash BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // RF-01: Permitir acceso público a login y recursos estáticos
                .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico", "/login").permitAll()
                // RF-03: Restricción de acceso - Solo ADMIN puede gestionar usuarios y roles
                .requestMatchers("/usuarios/**", "/roles", "/roles/**").hasRole("ADMIN")
                // RF-03, RF-04, RF-09: ADMIN y ALMACENERO pueden gestionar categorías, marcas y entradas
                .requestMatchers("/categorias/**", "/marcas/**", "/entradas/**").hasAnyRole("ADMIN", "ALMACENERO")
                // RF-10, RF-13: ADMIN, ALMACENERO y VENDEDOR pueden registrar salidas
                .requestMatchers("/salidas/**").hasAnyRole("ADMIN", "ALMACENERO", "VENDEDOR")
                .anyRequest().authenticated()
            )
            // RF-01: Configurar página de login y redirección post-autenticación
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                // RF-03: Página de acceso denegado cuando usuario sin permisos intenta acceder
                .accessDeniedPage("/acceso-denegado")
            );

        return http.build();
    }
}
