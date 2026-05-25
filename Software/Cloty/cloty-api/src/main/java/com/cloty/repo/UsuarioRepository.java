package com.cloty.repo;

import com.cloty.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

	Optional<Usuario> findByUsername(String username);

	boolean existsByUsername(String username);

	Optional<Usuario> findByRut(String rut);

	boolean existsByRut(String rut);
}

