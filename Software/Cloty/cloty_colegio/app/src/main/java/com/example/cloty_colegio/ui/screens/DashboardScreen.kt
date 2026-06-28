package com.example.cloty_colegio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloty_colegio.data.api.ActividadReciente
import com.example.cloty_colegio.data.api.ColegioDashboard
import com.example.cloty_colegio.data.api.DashboardAlumnoItem
import com.example.cloty_colegio.data.api.DashboardApoderadoItem
import com.example.cloty_colegio.data.api.DashboardComunidadDetalle
import com.example.cloty_colegio.data.api.DashboardCursoDetalle
import com.example.cloty_colegio.data.api.DashboardNotificacionItem
import com.example.cloty_colegio.data.api.DashboardNotificacionesDetalle
import com.example.cloty_colegio.data.api.DashboardPrendasDetalle
import com.example.cloty_colegio.data.api.DashboardTarjetaItem
import com.example.cloty_colegio.data.api.DashboardTarjetasDetalle
import com.example.cloty_colegio.ui.ClotyViewModel
import com.example.cloty_colegio.ui.components.ClotyPullRefresh

@Composable
fun DashboardScreen(
    viewModel: ClotyViewModel,
    contentPadding: PaddingValues,
    onOpenSection: (String) -> Unit,
    onOpenCurso: (Int) -> Unit
) {
    val dashboard by viewModel.dashboard.collectAsState()
    val nombreColegio by viewModel.nombreColegio.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val d = dashboard

    ClotyPullRefresh(
        refreshing = refreshing,
        onRefresh = { viewModel.refrescarDashboard() },
        modifier = Modifier.fillMaxSize().padding(contentPadding)
    ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(nombreColegio ?: "Mi colegio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Resumen del establecimiento", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (d != null) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardCompactStat("Alumnos", d.totalAlumnos.toString(), Modifier.weight(1f))
                    DashboardCompactStat("Apoderados", d.totalApoderados.toString(), Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardCompactStat("Tarjetas activas", d.tarjetasActivas.toString(), Modifier.weight(1f))
                    DashboardCompactStat("Prendas hoy", d.prendasEncontradasHoy.toString(), Modifier.weight(1f))
                }
            }

            item { Text("Explorar información", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold) }

            item {
                DashboardNavCard(
                    title = "Comunidad escolar",
                    subtitle = "${d.totalAlumnos} alumnos · ${d.totalApoderados} apoderados · ${d.totalCursos} cursos",
                    icon = Icons.Default.Groups,
                    onClick = { onOpenSection("comunidad") }
                )
            }
            item {
                DashboardNavCard(
                    title = "Tarjetas NFC",
                    subtitle = "${d.tarjetasActivas} activas · ${d.tarjetasPerdidas} perdidas · ${d.tarjetasDesactivadas} desactivadas",
                    icon = Icons.Default.CreditCard,
                    onClick = { onOpenSection("tarjetas") }
                )
            }
            item {
                DashboardNavCard(
                    title = "Prendas",
                    subtitle = "${d.prendasEncontradasTotal} encontradas · ${d.prendasEntregadasTotal} entregadas",
                    icon = Icons.Default.Search,
                    onClick = { onOpenSection("prendas") }
                )
            }
            item {
                DashboardNavCard(
                    title = "Notificaciones",
                    subtitle = "${d.notificacionesEnviadas} enviadas · ${d.notificacionesPendientes} pendientes",
                    icon = Icons.Default.Notifications,
                    onClick = { onOpenSection("notificaciones") }
                )
            }
            item {
                DashboardNavCard(
                    title = "Alumnos por curso",
                    subtitle = "${d.totalCursos} cursos registrados",
                    icon = Icons.Default.School,
                    onClick = { onOpenSection("cursos") }
                )
            }
            item {
                DashboardNavCard(
                    title = "Actividad reciente",
                    subtitle = "${d.ultimasAcciones?.size ?: 0} eventos recientes",
                    icon = Icons.Default.History,
                    onClick = { onOpenSection("actividad") }
                )
            }

            item {
                Text("Últimos movimientos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            if (d.ultimasAcciones.isNullOrEmpty()) {
                item { DashboardEmptyState("No hay actividad reciente registrada") }
            } else {
                items(d.ultimasAcciones.take(3)) { ActividadCompactRow(it) }
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardDetailScaffold(
    title: String,
    onBack: () -> Unit,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        ClotyPullRefresh(
            refreshing = refreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            content(PaddingValues(0.dp))
        }
    }
}

@Composable
fun DashboardComunidadDetailScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val detalle by viewModel.dashboardComunidad.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var search by rememberSaveable { mutableStateOf("") }
    var filtroAlumnos by rememberSaveable { mutableStateOf("todos") }
    var filtroApoderados by rememberSaveable { mutableStateOf("todos") }
    LaunchedEffect(Unit) { viewModel.cargarDashboardComunidad() }

    DashboardDetailScaffold("Comunidad escolar", onBack, refreshing, { viewModel.refrescarDashboardComunidad() }) { padding ->
        val d = detalle
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (d == null) {
                Text("Cargando…", modifier = Modifier.padding(16.dp))
                return@Column
            }
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Alumnos (${d.alumnosLista?.size ?: 0})") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Apoderados (${d.apoderadosLista?.size ?: 0})") })
            }
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                item {
                    DashboardSearchField(
                        query = search,
                        onQueryChange = { search = it },
                        placeholder = if (tab == 0) "Buscar alumno, RUT o curso…" else "Buscar apoderado, RUT o email…"
                    )
                }
                if (tab == 0) {
                    item {
                        DashboardFilterChips(
                            options = listOf(
                                "todos" to "Todos",
                                "con_tarjeta" to "Con tarjeta",
                                "sin_tarjeta" to "Sin tarjeta"
                            ),
                            selected = filtroAlumnos,
                            onSelect = { filtroAlumnos = it }
                        )
                    }
                    val lista = d.alumnosLista.orEmpty()
                    val filtrada = lista.filter { alumno ->
                        val okFiltro = when (filtroAlumnos) {
                            "con_tarjeta" -> alumno.tieneTarjeta
                            "sin_tarjeta" -> !alumno.tieneTarjeta
                            else -> true
                        }
                        okFiltro && matchesSearch(
                            search,
                            alumno.nombres,
                            alumno.apellidos,
                            alumno.rut,
                            alumno.nombreCurso,
                            alumno.nombreApoderado
                        )
                    }
                    item { DashboardResultsBanner(lista.size, filtrada.size, "alumnos") }
                    if (filtrada.isEmpty()) {
                        item { DashboardEmptyState(emptyMessageAlumnos(lista.size, filtroAlumnos, search)) }
                    } else {
                        items(filtrada, key = { it.idAlumno ?: it.rut ?: it.hashCode() }) { AlumnoCompactRow(it) }
                    }
                } else {
                    item {
                        DashboardFilterChips(
                            options = listOf(
                                "todos" to "Todos",
                                "con_cuenta" to "Con cuenta",
                                "sin_cuenta" to "Sin cuenta"
                            ),
                            selected = filtroApoderados,
                            onSelect = { filtroApoderados = it }
                        )
                    }
                    val lista = d.apoderadosLista.orEmpty()
                    val filtrada = lista.filter { ap ->
                        val okFiltro = when (filtroApoderados) {
                            "con_cuenta" -> ap.tieneCuenta
                            "sin_cuenta" -> !ap.tieneCuenta
                            else -> true
                        }
                        okFiltro && matchesSearch(search, ap.nombres, ap.apellidos, ap.rut, ap.email)
                    }
                    item { DashboardResultsBanner(lista.size, filtrada.size, "apoderados") }
                    if (filtrada.isEmpty()) {
                        item { DashboardEmptyState(emptyMessageApoderados(lista.size, filtroApoderados, search)) }
                    } else {
                        items(filtrada, key = { it.idApoderado ?: it.rut ?: it.hashCode() }) { ApoderadoCompactRow(it) }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardTarjetasDetailScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val detalle by viewModel.dashboardTarjetas.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    var search by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("todas") }
    LaunchedEffect(Unit) { viewModel.cargarDashboardTarjetas() }

    DashboardDetailScaffold("Tarjetas NFC", onBack, refreshing, { viewModel.refrescarDashboardTarjetas() }) { padding ->
        val d = detalle
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (d == null) {
                item { Text("Cargando…") }
            } else {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DashboardCompactStat("Activas", d.tarjetasActivas.toString(), Modifier.weight(1f))
                        DashboardCompactStat("Perdidas", d.tarjetasPerdidas.toString(), Modifier.weight(1f))
                        DashboardCompactStat("Desactivadas", d.tarjetasDesactivadas.toString(), Modifier.weight(1f))
                    }
                }
                item {
                    DashboardSearchField(search, { search = it }, "Buscar por alumno, UID o prenda…")
                }
                item {
                    DashboardFilterChips(
                        options = listOf(
                            "todas" to "Todas",
                            "ACTIVA" to "Activas",
                            "PERDIDA" to "Perdidas",
                            "DESACTIVADA" to "Desactivadas"
                        ),
                        selected = filtro,
                        onSelect = { filtro = it }
                    )
                }
                val lista = d.tarjetas.orEmpty()
                val filtrada = lista.filter { t ->
                    val okEstado = filtro == "todas" || t.estado.equals(filtro, ignoreCase = true)
                    okEstado && matchesSearch(search, t.nombreAlumno, t.uidNfc, t.tipoPrenda, t.nombreCurso)
                }
                item { DashboardResultsBanner(lista.size, filtrada.size, "tarjetas") }
                if (filtrada.isEmpty()) {
                    item { DashboardEmptyState(emptyMessageTarjetas(lista.size, filtro, search)) }
                } else {
                    items(filtrada, key = { it.idTarjeta ?: it.uidNfc ?: it.hashCode() }) { TarjetaCompactRow(it) }
                }
            }
        }
    }
}

@Composable
fun DashboardPrendasDetailScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val detalle by viewModel.dashboardPrendas.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    var search by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("todas") }
    LaunchedEffect(Unit) { viewModel.cargarDashboardPrendas() }

    DashboardDetailScaffold("Prendas", onBack, refreshing, { viewModel.refrescarDashboardPrendas() }) { padding ->
        val d = detalle
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (d == null) {
                item { Text("Cargando…") }
            } else {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DashboardCompactStat("Encontradas hoy", d.prendasEncontradasHoy.toString(), Modifier.weight(1f))
                        DashboardCompactStat("Entregadas hoy", d.prendasEntregadasHoy.toString(), Modifier.weight(1f))
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DashboardCompactStat("Total encontradas", d.prendasEncontradasTotal.toString(), Modifier.weight(1f))
                        DashboardCompactStat("Total entregadas", d.prendasEntregadasTotal.toString(), Modifier.weight(1f))
                    }
                }
                item { DashboardSearchField(search, { search = it }, "Buscar por alumno, curso o prenda…") }
                item {
                    DashboardFilterChips(
                        options = listOf(
                            "todas" to "Todas",
                            "PRENDA_ENCONTRADA" to "Encontradas",
                            "PRENDA_RECUPERADA" to "Entregadas"
                        ),
                        selected = filtro,
                        onSelect = { filtro = it }
                    )
                }
                val lista = d.actividad.orEmpty()
                val filtrada = lista.filter { a ->
                    val okTipo = filtro == "todas" || a.tipoEvento.equals(filtro, ignoreCase = true)
                    okTipo && matchesSearch(search, a.nombreAlumno, a.nombreCurso, a.tipoPrenda, a.descripcion, a.uidNfc)
                }
                item { DashboardResultsBanner(lista.size, filtrada.size, "movimientos") }
                if (filtrada.isEmpty()) {
                    item { DashboardEmptyState(emptyMessagePrendas(lista.size, filtro, search)) }
                } else {
                    items(filtrada, key = { it.idEvento ?: "${it.fecha}-${it.uidNfc}" }) { ActividadCompactRow(it) }
                }
            }
        }
    }
}

@Composable
fun DashboardNotificacionesDetailScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val detalle by viewModel.dashboardNotificaciones.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    var search by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("todas") }
    LaunchedEffect(Unit) { viewModel.cargarDashboardNotificaciones() }

    DashboardDetailScaffold("Notificaciones", onBack, refreshing, { viewModel.refrescarDashboardNotificaciones() }) { padding ->
        val d = detalle
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (d == null) {
                item { Text("Cargando…") }
            } else {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DashboardCompactStat("Enviadas", d.notificacionesEnviadas.toString(), Modifier.weight(1f))
                        DashboardCompactStat("Pendientes", d.notificacionesPendientes.toString(), Modifier.weight(1f))
                    }
                }
                item { DashboardSearchField(search, { search = it }, "Buscar por título, apoderado o mensaje…") }
                item {
                    DashboardFilterChips(
                        options = listOf(
                            "todas" to "Todas",
                            "ENVIADA" to "Enviadas",
                            "PENDIENTE" to "Pendientes"
                        ),
                        selected = filtro,
                        onSelect = { filtro = it }
                    )
                }
                val lista = d.recientes.orEmpty()
                val filtrada = lista.filter { n ->
                    val okEstado = filtro == "todas" || n.estado.equals(filtro, ignoreCase = true)
                    okEstado && matchesSearch(search, n.titulo, n.mensaje, n.nombreApoderado)
                }
                item { DashboardResultsBanner(lista.size, filtrada.size, "notificaciones") }
                if (filtrada.isEmpty()) {
                    item { DashboardEmptyState(emptyMessageNotificaciones(lista.size, filtro, search)) }
                } else {
                    items(filtrada, key = { it.idNotificacion ?: "${it.titulo}-${it.fechaEnvio}" }) { NotificacionCompactRow(it) }
                }
            }
        }
    }
}

@Composable
fun DashboardCursosDetailScreen(viewModel: ClotyViewModel, onBack: () -> Unit, onOpenCurso: (Int) -> Unit) {
    val cursos by viewModel.dashboardCursos.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    var search by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("todos") }
    LaunchedEffect(Unit) { viewModel.cargarDashboardCursos() }

    DashboardDetailScaffold("Alumnos por curso", onBack, refreshing, { viewModel.refrescarDashboardCursos() }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item { DashboardSearchField(search, { search = it }, "Buscar curso o nivel…") }
            item {
                DashboardFilterChips(
                    options = listOf(
                        "todos" to "Todos",
                        "con_pendientes" to "Con alumnos sin tarjeta",
                        "completos" to "Todos con tarjeta"
                    ),
                    selected = filtro,
                    onSelect = { filtro = it }
                )
            }
            val filtrados = cursos.filter { curso ->
                val okFiltro = when (filtro) {
                    "con_pendientes" -> curso.alumnosSinTarjeta > 0
                    "completos" -> curso.totalAlumnos > 0 && curso.alumnosSinTarjeta == 0L
                    else -> true
                }
                okFiltro && matchesSearch(search, curso.nombre, curso.nivel)
            }
            item { DashboardResultsBanner(cursos.size, filtrados.size, "cursos") }
            if (filtrados.isEmpty()) {
                item { DashboardEmptyState(emptyMessageCursos(cursos.size, filtro, search)) }
            } else {
                items(filtrados, key = { it.idCurso ?: it.nombre ?: it.hashCode() }) { curso ->
                    CursoCompactRow(curso, onClick = { curso.idCurso?.let(onOpenCurso) })
                }
            }
        }
    }
}

@Composable
fun DashboardCursoDetailScreen(viewModel: ClotyViewModel, idCurso: Int, onBack: () -> Unit) {
    val detalle by viewModel.dashboardCursoDetalle.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    var search by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("todos") }
    LaunchedEffect(idCurso) { viewModel.cargarDashboardCurso(idCurso) }

    DashboardDetailScaffold(detalle?.nombre ?: "Detalle del curso", onBack, refreshing, { viewModel.refrescarDashboardCurso(idCurso) }) { padding ->
        val d = detalle
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (d == null) {
                item { Text("Cargando…") }
            } else {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DashboardCompactStat("Alumnos", d.totalAlumnos.toString(), Modifier.weight(1f))
                        DashboardCompactStat("Con tarjeta", d.alumnosConTarjeta.toString(), Modifier.weight(1f))
                        DashboardCompactStat("Sin tarjeta", d.alumnosSinTarjeta.toString(), Modifier.weight(1f))
                    }
                }
                d.nivel?.let { nivel -> item { Text(nivel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                item { DashboardSearchField(search, { search = it }, "Buscar alumno o RUT…") }
                item {
                    DashboardFilterChips(
                        options = listOf("todos" to "Todos", "con_tarjeta" to "Con tarjeta", "sin_tarjeta" to "Sin tarjeta"),
                        selected = filtro,
                        onSelect = { filtro = it }
                    )
                }
                val lista = d.alumnos.orEmpty()
                val filtrada = lista.filter { a ->
                    val ok = when (filtro) {
                        "con_tarjeta" -> a.tieneTarjeta
                        "sin_tarjeta" -> !a.tieneTarjeta
                        else -> true
                    }
                    ok && matchesSearch(search, a.nombres, a.apellidos, a.rut, a.nombreApoderado)
                }
                item { DashboardResultsBanner(lista.size, filtrada.size, "alumnos") }
                if (filtrada.isEmpty()) {
                    item { DashboardEmptyState(emptyMessageAlumnos(lista.size, filtro, search)) }
                } else {
                    items(filtrada, key = { it.idAlumno ?: it.rut ?: it.hashCode() }) { AlumnoCompactRow(it) }
                }
            }
        }
    }
}

@Composable
fun DashboardActividadDetailScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val actividad by viewModel.dashboardActividad.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    var search by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("todas") }
    LaunchedEffect(Unit) { viewModel.cargarDashboardActividad() }

    DashboardDetailScaffold("Actividad reciente", onBack, refreshing, { viewModel.refrescarDashboardActividad() }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item { DashboardSearchField(search, { search = it }, "Buscar por alumno, curso o descripción…") }
            item {
                DashboardFilterChips(
                    options = listOf(
                        "todas" to "Todas",
                        "PRENDA_ENCONTRADA" to "Encontradas",
                        "PRENDA_RECUPERADA" to "Entregadas",
                        "NOTIFICACION_ENVIADA" to "Notificaciones",
                        "TARJETA_DESACTIVADA" to "Tarjetas"
                    ),
                    selected = filtro,
                    onSelect = { filtro = it }
                )
            }
            val filtrada = actividad.filter { a ->
                val ok = filtro == "todas" || a.tipoEvento.equals(filtro, ignoreCase = true)
                ok && matchesSearch(search, a.nombreAlumno, a.nombreCurso, a.descripcion, a.tipoPrenda, a.uidNfc)
            }
            item { DashboardResultsBanner(actividad.size, filtrada.size, "eventos") }
            if (filtrada.isEmpty()) {
                item { DashboardEmptyState(emptyMessageActividad(actividad.size, filtro, search)) }
            } else {
                items(filtrada, key = { it.idEvento ?: "${it.fecha}-${it.tipoEvento}" }) { ActividadCompactRow(it) }
            }
        }
    }
}

@Composable
private fun AlumnoCompactRow(alumno: DashboardAlumnoItem) {
    DashboardListRow(
        title = "${alumno.nombres.orEmpty()} ${alumno.apellidos.orEmpty()}".trim(),
        subtitle = listOfNotNull(
            alumno.rut?.let { "RUT $it" },
            alumno.nombreCurso,
            alumno.nombreApoderado?.let { "Apoderado: $it" }
        ).joinToString(" · "),
        badge = if (alumno.tieneTarjeta) "Con tarjeta" else "Sin tarjeta",
        badgeHighlight = alumno.tieneTarjeta
    )
}

@Composable
private fun ApoderadoCompactRow(apoderado: DashboardApoderadoItem) {
    DashboardListRow(
        title = "${apoderado.nombres.orEmpty()} ${apoderado.apellidos.orEmpty()}".trim(),
        subtitle = listOfNotNull(apoderado.rut?.let { "RUT $it" }, apoderado.email).joinToString(" · "),
        badge = if (apoderado.tieneCuenta) "Cuenta activa" else "Sin cuenta",
        badgeHighlight = apoderado.tieneCuenta
    )
}

@Composable
private fun TarjetaCompactRow(tarjeta: DashboardTarjetaItem) {
    DashboardListRow(
        title = tarjeta.nombreAlumno ?: "Sin alumno",
        subtitle = listOfNotNull(
            tarjeta.nombreCurso,
            tarjeta.uidNfc?.let { "UID $it" },
            tarjeta.tipoPrenda
        ).joinToString(" · "),
        badge = formatEstadoLabel(tarjeta.estado),
        badgeHighlight = tarjeta.estado.equals("ACTIVA", ignoreCase = true)
    )
}

@Composable
private fun NotificacionCompactRow(notif: DashboardNotificacionItem) {
    DashboardListRow(
        title = notif.titulo ?: "Notificación",
        subtitle = listOfNotNull(
            notif.nombreApoderado?.let { "Para: $it" },
            notif.mensaje,
            notif.fechaEnvio?.take(16)?.replace('T', ' ')
        ).joinToString(" · "),
        badge = formatEstadoLabel(notif.estado),
        badgeHighlight = notif.estado.equals("ENVIADA", ignoreCase = true)
    )
}

@Composable
private fun CursoCompactRow(curso: DashboardCursoDetalle, onClick: () -> Unit) {
    DashboardNavCard(
        title = curso.nombre ?: "Curso",
        subtitle = listOfNotNull(
            curso.nivel,
            "${curso.totalAlumnos} alumnos",
            "${curso.alumnosConTarjeta} con tarjeta",
            if (curso.alumnosSinTarjeta > 0) "${curso.alumnosSinTarjeta} sin tarjeta" else null
        ).joinToString(" · "),
        icon = Icons.Default.School,
        onClick = onClick
    )
}

@Composable
private fun ActividadCompactRow(a: ActividadReciente) {
    DashboardListRow(
        title = a.accion ?: formatEstadoLabel(a.tipoEvento),
        subtitle = listOfNotNull(
            a.nombreAlumno,
            a.nombreCurso,
            a.tipoPrenda,
            a.fecha?.take(16)?.replace('T', ' '),
            a.descripcion
        ).joinToString(" · "),
        badge = formatEstadoLabel(a.tipoEvento),
        badgeHighlight = a.tipoEvento != "TARJETA_DESACTIVADA"
    )
}

private fun emptyMessageAlumnos(total: Int, filtro: String, search: String): String = when {
    total == 0 -> "No hay alumnos registrados en el colegio"
    search.isNotBlank() -> "Ningún alumno coincide con \"$search\""
    filtro == "con_tarjeta" -> "Ningún alumno tiene tarjeta NFC asignada"
    filtro == "sin_tarjeta" -> "Todos los alumnos tienen tarjeta NFC"
    else -> "No hay alumnos para mostrar"
}

private fun emptyMessageApoderados(total: Int, filtro: String, search: String): String = when {
    total == 0 -> "No hay apoderados registrados en el colegio"
    search.isNotBlank() -> "Ningún apoderado coincide con \"$search\""
    filtro == "con_cuenta" -> "Ningún apoderado tiene cuenta activa"
    filtro == "sin_cuenta" -> "Todos los apoderados tienen cuenta activa"
    else -> "No hay apoderados para mostrar"
}

private fun emptyMessageTarjetas(total: Int, filtro: String, search: String): String = when {
    total == 0 -> "No hay tarjetas NFC registradas"
    search.isNotBlank() -> "Ninguna tarjeta coincide con \"$search\""
    filtro != "todas" -> "No hay tarjetas en estado ${formatEstadoLabel(filtro)}"
    else -> "No hay tarjetas para mostrar"
}

private fun emptyMessagePrendas(total: Int, filtro: String, search: String): String = when {
    total == 0 -> "No hay movimientos de prendas registrados"
    search.isNotBlank() -> "Ningún movimiento coincide con \"$search\""
    filtro != "todas" -> "No hay prendas ${formatEstadoLabel(filtro).lowercase()}"
    else -> "No hay movimientos para mostrar"
}

private fun emptyMessageNotificaciones(total: Int, filtro: String, search: String): String = when {
    total == 0 -> "No hay notificaciones registradas"
    search.isNotBlank() -> "Ninguna notificación coincide con \"$search\""
    filtro != "todas" -> "No hay notificaciones ${formatEstadoLabel(filtro).lowercase()}"
    else -> "No hay notificaciones para mostrar"
}

private fun emptyMessageCursos(total: Int, filtro: String, search: String): String = when {
    total == 0 -> "No hay cursos registrados en el colegio"
    search.isNotBlank() -> "Ningún curso coincide con \"$search\""
    filtro == "con_pendientes" -> "Ningún curso tiene alumnos sin tarjeta"
    filtro == "completos" -> "Ningún curso tiene todos sus alumnos con tarjeta"
    else -> "No hay cursos para mostrar"
}

private fun emptyMessageActividad(total: Int, filtro: String, search: String): String = when {
    total == 0 -> "No hay actividad registrada en el colegio"
    search.isNotBlank() -> "Ningún evento coincide con \"$search\""
    filtro != "todas" -> "No hay eventos de tipo ${formatEstadoLabel(filtro).lowercase()}"
    else -> "No hay eventos para mostrar"
}
