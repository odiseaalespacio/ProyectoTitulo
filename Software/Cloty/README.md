# Cloty

Sistema para la gestión de prendas escolares identificadas con **tarjetas NFC**. Permite registrar colegios, apoderados, alumnos y cursos; asignar tarjetas a estudiantes; notificar a apoderados cuando se encuentra una prenda; y administrar usuarios del sistema desde aplicaciones móviles Android.

El proyecto está compuesto por una **API REST** (backend) y **tres apps Android**, todas conectadas a la misma API.

---

## Arquitectura

```
┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐
│  Cloty Administrador│  │    Cloty Colegio    │  │   Cloty Apoderado   │
│      (Android)      │  │      (Android)      │  │      (Android)      │
└──────────┬──────────┘  └──────────┬──────────┘  └──────────┬──────────┘
           │                         │                         │
           └─────────────────────────┼─────────────────────────┘
                                     │  HTTP / JSON (Retrofit)
                                     ▼
                          ┌─────────────────────┐
                          │      cloty-api      │
                          │   Spring Boot REST  │
                          └──────────┬──────────┘
                                     │
                                     ▼
                          ┌─────────────────────┐
                          │   MySQL / MariaDB   │
                          │      (cloty)        │
                          └─────────────────────┘
```

---

## Roles de usuario

| Rol | App | Descripción |
|-----|-----|-------------|
| **SUPER_USUARIO** | Administrador | Gestiona super usuarios y administradores del sistema |
| **ADMINISTRADOR** | Administrador | Gestiona colegios, cursos, personas, carga CSV y tarjetas NFC |
| **COLEGIO** | Colegio | Opera el colegio: alumnos, apoderados, cursos, notificaciones |
| **APODERADO** | Apoderado | Consulta información de sus pupilos y recibe notificaciones |

---

## Funcionalidades principales

### Backend (`cloty-api`)
- Autenticación con **JWT** (login por usuario, RUT o correo según rol)
- Activación de cuentas de colegio y apoderado con **código por correo**
- Recuperación de contraseña por correo
- CRUD de colegios, administradores, super usuarios, apoderados, alumnos y cursos
- **Carga masiva CSV** de apoderados y alumnos (los cursos se crean según la columna `nivel`)
- Gestión de **tarjetas NFC** vinculadas a alumnos
- Registro de **eventos** (prenda encontrada, recuperada, etc.) y **notificaciones** a apoderados
- Envío de correos (bienvenida, códigos de activación, recuperación de contraseña) vía SMTP
- Eliminación en cascada al borrar colegios, apoderados, cursos y otros registros

### App Administrador (`clotyadministrador`)
- Panel para super usuarios y administradores
- Gestión de super usuarios, administradores, colegios, cursos y personas
- Importación CSV y asignación de tarjetas NFC por lote
- Lectura NFC desde el dispositivo

### App Colegio (`cloty_colegio`)
- Panel operativo del colegio
- Gestión de apoderados, alumnos y cursos
- Activación de cuenta del colegio

### App Apoderado (`cloty_apoderado`)
- Consulta de pupilos vinculados
- Activación de cuenta y recuperación de contraseña
- Recepción de notificaciones

---

## Estructura del repositorio

```
Cloty/
├── cloty-api/              # API REST (Spring Boot)
├── clotyadministrador/     # App Android — panel administrativo
├── cloty_colegio/          # App Android — panel colegio
├── cloty_apoderado/        # App Android — panel apoderado
├── scripts/                # Scripts de utilidad (AWS, conexión celular)
├── cloty-mysql.sql         # Esquema completo de la base de datos
├── local.properties        # IP/puerto de la API para las apps Android
├── apoderados-ejemplo.csv  # Ejemplo de carga masiva
├── alumnos-ejemplo.csv     # Ejemplo de carga masiva
└── INSTRUCTIVO-AWS-PRUEBA.txt  # Guía de despliegue en AWS
```

---

## Tecnologías utilizadas

### Backend — `cloty-api`

| Tecnología | Uso |
|------------|-----|
| **Java 17** | Lenguaje |
| **Spring Boot 4.0** | Framework principal |
| **Spring Web MVC** | API REST |
| **Spring Data JPA / Hibernate** | Persistencia ORM |
| **Spring Security + JWT (jjwt)** | Autenticación y autorización |
| **Spring Mail** | Envío de correos |
| **Spring Validation** | Validación de DTOs (RUT, teléfono chileno, etc.) |
| **MySQL / MariaDB** | Base de datos relacional |
| **Lombok** | Reducción de boilerplate |
| **Maven** | Build y dependencias |

### Apps Android

| Tecnología | Uso |
|------------|-----|
| **Kotlin** | Lenguaje |
| **Jetpack Compose + Material 3** | Interfaz de usuario |
| **Navigation Compose** | Navegación entre pantallas |
| **ViewModel + StateFlow** | Estado de la UI |
| **Retrofit 2 + Gson** | Cliente HTTP hacia la API |
| **OkHttp** | Cliente HTTP (interceptores, logs) |
| **DataStore Preferences** | Persistencia del token JWT |
| **Android NFC API** | Lectura de UID de tarjetas (app administrador) |
| **Gradle (Kotlin DSL)** | Build |

### Infraestructura y herramientas

| Tecnología | Uso |
|------------|-----|
| **XAMPP / MySQL** | Desarrollo local |
| **AWS EC2** | Despliegue de prueba en la nube |
| **Gmail SMTP** | Correos en desarrollo y prueba |
| **Git** | Control de versiones |

---

## Requisitos

- **JDK 17+** (desarrollo local puede usar Java 21)
- **Maven** o `./mvnw` incluido en `cloty-api`
- **MySQL 8** o **MariaDB 10.5+** (XAMPP en local)
- **Android Studio** con SDK 24+ para compilar las apps
- **Dispositivo Android** con NFC (opcional, solo para asignación de tarjetas)

---

## Inicio rápido (desarrollo local)

### 1. Base de datos

Opción A — dejar que la API cree el esquema automáticamente (`ddl-auto=update`).

Opción B — ejecutar el script manual:

```bash
mysql -u root -p < cloty-mysql.sql
```

### 2. API

```bash
cd cloty-api
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080`.

Para correos en local, copie `application-local.properties.example` a `application-local.properties` y configure Gmail.

**Usuario inicial:** `superadmin` / `super123` (RUT `00000000-0`).

### 3. Apps Android

Configure la URL de la API en `local.properties` (raíz del proyecto):

```properties
cloty.api.host=127.0.0.1
cloty.api.port=8080
```

Con celular por USB:

```powershell
adb reverse tcp:8080 tcp:8080
```

Compile e instale cada app desde Android Studio o Gradle:

```bash
cd clotyadministrador && ./gradlew installDebug
```

---

## Despliegue en AWS

Consulte **`INSTRUCTIVO-AWS-PRUEBA.txt`** para subir la API a una instancia EC2 con MariaDB, correo Gmail y conexión desde las apps móviles por internet.

Plantillas de configuración en `scripts/aws/`.

---

## Archivos de ejemplo

| Archivo | Descripción |
|---------|-------------|
| `apoderados-ejemplo.csv` | Formato CSV para importar apoderados |
| `alumnos-ejemplo.csv` | Formato CSV para importar alumnos (incluye columna `nivel`) |
| `LEEME-csv-ejemplo.txt` | Instrucciones de las columnas CSV |

---

## Licencia

Proyecto académico — Proyecto de Título.
