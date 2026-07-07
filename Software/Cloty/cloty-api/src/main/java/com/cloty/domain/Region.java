package com.cloty.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "region")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {

	@Id
	@Column(name = "codigo_region", length = 2)
	private String codigoRegion;

	@Column(name = "nombre", nullable = false, length = 100)
	private String nombre;
}
