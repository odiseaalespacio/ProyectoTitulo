package com.cloty.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "tarjeta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarjeta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_tarjeta")
	private Integer idTarjeta;

	@Column(name = "id_alumno", nullable = false)
	private Integer idAlumno;

	@Column(name = "uid_nfc", nullable = false, unique = true, length = 100)
	private String uidNfc;

	@Column(name = "codigo_visual", length = 100)
	private String codigoVisual;

	@Column(name = "tipo_prenda", length = 100)
	private String tipoPrenda;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", length = 20)
	@Builder.Default
	private EstadoTarjeta estado = EstadoTarjeta.ACTIVA;

	@Column(name = "fecha_asignacion", insertable = false, updatable = false)
	private LocalDateTime fechaAsignacion;
}
