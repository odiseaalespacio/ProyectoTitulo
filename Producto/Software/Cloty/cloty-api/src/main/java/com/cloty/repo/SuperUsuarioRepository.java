package com.cloty.repo;

import com.cloty.domain.SuperUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuperUsuarioRepository extends JpaRepository<SuperUsuario, Integer> {

	Optional<SuperUsuario> findByIdUsuario(Integer idUsuario);

	Optional<SuperUsuario> findByRut(String rut);
}
