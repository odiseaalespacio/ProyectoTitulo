package com.cloty.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "colegio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Colegio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_colegio")
	private Integer idColegio;

	@Column(name = "id_usuario", unique = true)
	private Integer idUsuario;

	@Column(name = "rut", unique = true, length = 12)
	private String rut;

	@Column(name = "nombre", nullable = false, length = 150)
	private String nombre;

	/** Correo de contacto; obligatorio al registrar. Se usa para avisos y activación de cuenta. */
	@Column(name = "email", length = 150)
	private String email;

	@Column(name = "telefono", length = 20)
	private String telefono;

	@Column(name = "direccion", length = 255)
	private String direccion;

	@Column(name = "fecha_creacion", insertable = false, updatable = false)
	private LocalDateTime fechaCreacion;
}
