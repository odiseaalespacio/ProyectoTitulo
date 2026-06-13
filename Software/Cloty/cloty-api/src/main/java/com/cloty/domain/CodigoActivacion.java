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
@Table(name = "codigo_activacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoActivacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_codigo_activacion")
	private Integer idCodigoActivacion;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo", nullable = false, length = 20)
	private TipoEntidadActivacion tipo;

	@Column(name = "id_entidad", nullable = false)
	private Integer idEntidad;

	@Column(name = "codigo_hash", nullable = false, length = 255)
	private String codigoHash;

	@Column(name = "expira_en", nullable = false)
	private LocalDateTime expiraEn;

	@Column(name = "usado", nullable = false)
	private Boolean usado;

	@Column(name = "fecha_creacion", insertable = false, updatable = false)
	private LocalDateTime fechaCreacion;
}
