package com.cloty.security;

import com.cloty.domain.Usuario;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClotyUserDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;
	private final ColegioRepository colegioRepository;
	private final ApoderadoRepository apoderadoRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario u = usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
		Integer idColegio = colegioRepository.findByIdUsuario(u.getIdUsuario())
				.map(c -> c.getIdColegio())
				.orElse(null);
		Integer idApoderado = apoderadoRepository.findByIdUsuario(u.getIdUsuario())
				.map(a -> a.getIdApoderado())
				.orElse(null);
		boolean enabled = u.getEstado() != null && u.getEstado();
		return new ClotyUserDetails(
				u.getIdUsuario(),
				u.getUsername(),
				u.getPasswordHash(),
				enabled,
				u.getRol(),
				idColegio,
				idApoderado
		);
	}
}
