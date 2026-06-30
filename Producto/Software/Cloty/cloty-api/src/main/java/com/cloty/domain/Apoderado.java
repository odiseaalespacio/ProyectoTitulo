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
@Table(name = "apoderado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apoderado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_apoderado")
	private Integer idApoderado;

	@Column(name = "id_usuario", unique = true)
	private Integer idUsuario;

	@Column(name = "rut", nullable = false, unique = true, length = 12)
	private String rut;

	@Column(name = "nombres", nullable = false, length = 100)
	private String nombres;

	@Column(name = "apellidos", nullable = false, length = 100)
	private String apellidos;

	@Column(name = "email", length = 150)
	private String email;

	@Column(name = "telefono", length = 20)
	private String telefono;

	@Column(name = "codigo_comuna", length = 5)
	private String codigoComuna;

	@Column(name = "calle_numero", length = 255)
	private String calleNumero;

	@Column(name = "fecha_creacion", insertable = false, updatable = false)
	private LocalDateTime fechaCreacion;
}
