package com.cloty.dto;

import java.util.List;

public record DashboardTarjetasDetalleResponse(
		long tarjetasActivas,
		long tarjetasPerdidas,
		long tarjetasDesactivadas,
		List<DashboardTarjetaItem> tarjetas
) {
}
