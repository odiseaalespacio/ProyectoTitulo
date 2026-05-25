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
		name = "colegio_apoderado",
		uniqueConstraints = @UniqueConstraint(name = "uq_colegio_apoderado", columnNames = { "id_colegio", "id_apoderado" })
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColegioApoderado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_colegio_apoderado")
	private Integer idColegioApoderado;

	@Column(name = "id_colegio", nullable = false)
	private Integer idColegio;

	@Column(name = "id_apoderado", nullable = false)
	private Integer idApoderado;

	@Column(name = "fecha_asociacion", insertable = false, updatable = false)
	private LocalDateTime fechaAsociacion;
}
