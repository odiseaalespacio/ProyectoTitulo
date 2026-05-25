package com.cloty.repo;

import com.cloty.domain.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {

	Optional<Administrador> findByIdUsuario(Integer idUsuario);

	Optional<Administrador> findByRut(String rut);
}
