package com.cloty.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
		name = "curso",
		uniqueConstraints = @UniqueConstraint(name = "uq_curso_colegio", columnNames = { "id_colegio", "nombre" })
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_curso")
	private Integer idCurso;

	@Column(name = "id_colegio", nullable = false)
	private Integer idColegio;

	@Column(name = "nombre", nullable = false, length = 50)
	private String nombre;

	@Column(name = "nivel", length = 50)
	private String nivel;

	@Column(name = "estado")
	private Boolean estado;

	@Column(name = "fecha_creacion", insertable = false, updatable = false)
	private LocalDateTime fechaCreacion;
}
