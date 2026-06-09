package com.techstore.tech_store_project.respository;

import com.techstore.tech_store_project.model.Marca;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarcaRepository extends JpaRepository<Marca, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
}