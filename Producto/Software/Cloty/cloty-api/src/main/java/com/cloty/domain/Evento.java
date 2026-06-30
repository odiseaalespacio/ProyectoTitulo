package com.cloty.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "evento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_evento")
	private Integer idEvento;

	@Column(name = "id_tarjeta", nullable = false)
	private Integer idTarjeta;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_evento", nullable = false, length = 40)
	private TipoEvento tipoEvento;

	@Column(name = "descripcion", length = 500)
	private String descripcion;

	@Column(name = "ubicacion", length = 255)
	private String ubicacion;

	@Column(name = "fecha_evento")
	private LocalDateTime fechaEvento;

	@Column(name = "registrado_por")
	private Integer registradoPor;

	@PrePersist
	void prePersist() {
		if (fechaEvento == null) {
			fechaEvento = LocalDateTime.now();
		}
	}
}
