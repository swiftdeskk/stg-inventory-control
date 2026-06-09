package com.techstore.tech_store_project.respository;

import com.techstore.tech_store_project.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolRepository extends JpaRepository<Rol, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    List<Rol> findByActivoTrue();
}
