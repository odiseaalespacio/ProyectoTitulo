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
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_notificacion")
	private Integer idNotificacion;

	@Column(name = "id_evento", nullable = false)
	private Integer idEvento;

	@Column(name = "id_apoderado", nullable = false)
	private Integer idApoderado;

	@Column(name = "titulo", nullable = false, length = 200)
	private String titulo;

	@Column(name = "mensaje", nullable = false, length = 500)
	private String mensaje;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", length = 20)
	@Builder.Default
	private EstadoNotificacion estado = EstadoNotificacion.PENDIENTE;

	@Column(name = "leida")
	@Builder.Default
	private Boolean leida = false;

	@Column(name = "fecha_envio", insertable = false, updatable = false)
	private LocalDateTime fechaEnvio;
}
