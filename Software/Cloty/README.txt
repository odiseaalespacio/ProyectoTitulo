================================================================================
  CLOTY
================================================================================

Sistema para la gestion de prendas escolares identificadas con tarjetas NFC.
Permite registrar colegios, apoderados, alumnos y cursos; asignar tarjetas a
estudiantes; notificar a apoderados cuando se encuentra una prenda; y
administrar usuarios del sistema desde aplicaciones moviles Android.

El proyecto esta compuesto por una API REST (backend) y tres apps Android,
todas conectadas a la misma API.


================================================================================
  ARQUITECTURA
================================================================================

  +---------------------+  +---------------------+  +---------------------+
  | Cloty Administrador |  |    Cloty Colegio    |  |   Cloty Apoderado   |
  |      (Android)      |  |      (Android)      |  |      (Android)      |
  +----------+----------+  +----------+----------+  +----------+----------+
             |                         |                         |
             +-------------------------+-------------------------+
                                       |  HTTP / JSON (Retrofit)
                                       v
                            +---------------------+
                            |      cloty-api      |
                            |   Spring Boot REST  |
                            +----------+----------+
                                       |
                                       v
                            +---------------------+
                            |   MySQL / MariaDB   |
                            |      (cloty)        |
                            +---------------------+


================================================================================
  ROLES DE USUARIO
================================================================================

  Rol              App             Descripcion
  ---              ---             -----------
  SUPER_USUARIO    Administrador   Gestiona super usuarios y administradores
  ADMINISTRADOR    Administrador   Gestiona colegios, cursos, CSV y NFC
  COLEGIO          Colegio         Opera el colegio: alumnos, apoderados, etc.
  APODERADO        Apoderado       Consulta pupilos y recibe notificaciones


================================================================================
  FUNCIONALIDADES PRINCIPALES
================================================================================

Backend (cloty-api)
  - Autenticacion con JWT (login por usuario, RUT o correo segun rol)
  - Activacion de cuentas de colegio y apoderado con codigo por correo
  - Recuperacion de contrasena por correo
  - CRUD de colegios, administradores, super usuarios, apoderados, alumnos y cursos
  - Carga masiva CSV de apoderados y alumnos (cursos segun columna nivel)
  - Gestion de tarjetas NFC vinculadas a alumnos
  - Registro de eventos y notificaciones a apoderados
  - Envio de correos (bienvenida, activacion, recuperacion) via SMTP
  - Eliminacion en cascada al borrar colegios, apoderados, cursos, etc.

App Administrador (clotyadministrador)
  - Panel para super usuarios y administradores
  - Gestion de super usuarios, administradores, colegios, cursos y personas
  - Importacion CSV y asignacion de tarjetas NFC por lote
  - Lectura NFC desde el dispositivo

App Colegio (cloty_colegio)
  - Panel operativo del colegio
  - Gestion de apoderados, alumnos y cursos
  - Activacion de cuenta del colegio

App Apoderado (cloty_apoderado)
  - Consulta de pupilos vinculados
  - Activacion de cuenta y recuperacion de contrasena
  - Recepcion de notificaciones


================================================================================
  ESTRUCTURA DEL REPOSITORIO
================================================================================

  Cloty/
    cloty-api/                  API REST (Spring Boot)
    clotyadministrador/           App Android - panel administrativo
    cloty_colegio/                App Android - panel colegio
    cloty_apoderado/              App Android - panel apoderado
    scripts/                      Scripts de utilidad (AWS, conexion celular)
    cloty-mysql.sql               Esquema completo de la base de datos
    local.properties              IP/puerto de la API para las apps Android
    apoderados-ejemplo.csv        Ejemplo de carga masiva
    alumnos-ejemplo.csv           Ejemplo de carga masiva
    INSTRUCTIVO-AWS-PRUEBA.txt    Guia de despliegue en AWS


================================================================================
  TECNOLOGIAS UTILIZADAS
================================================================================

Backend (cloty-api)
  Java 17                         Lenguaje
  Spring Boot 4.0                 Framework principal
  Spring Web MVC                    API REST
  Spring Data JPA / Hibernate       Persistencia ORM
  Spring Security + JWT (jjwt)      Autenticacion y autorizacion
  Spring Mail                       Envio de correos
  Spring Validation                 Validacion de DTOs (RUT, telefono, etc.)
  MySQL / MariaDB                   Base de datos relacional
  Lombok                            Reduccion de boilerplate
  Maven                             Build y dependencias

Apps Android
  Kotlin                            Lenguaje
  Jetpack Compose + Material 3      Interfaz de usuario
  Navigation Compose                Navegacion entre pantallas
  ViewModel + StateFlow             Estado de la UI
  Retrofit 2 + Gson                 Cliente HTTP hacia la API
  OkHttp                            Cliente HTTP (interceptores, logs)
  DataStore Preferences             Persistencia del token JWT
  Android NFC API                   Lectura de UID de tarjetas (administrador)
  Gradle (Kotlin DSL)               Build

Infraestructura y herramientas
  XAMPP / MySQL                     Desarrollo local
  AWS EC2                           Despliegue de prueba en la nube
  Gmail SMTP                        Correos en desarrollo y prueba
  Git                               Control de versiones


================================================================================
  REQUISITOS
================================================================================

  - JDK 17+ (desarrollo local puede usar Java 21)
  - Maven o mvnw incluido en cloty-api
  - MySQL 8 o MariaDB 10.5+ (XAMPP en local)
  - Android Studio con SDK 24+ para compilar las apps
  - Dispositivo Android con NFC (opcional, solo para asignacion de tarjetas)


================================================================================
  INICIO RAPIDO (DESARROLLO LOCAL)
================================================================================

1. Base de datos

   Opcion A: dejar que la API cree el esquema (ddl-auto=update).

   Opcion B: ejecutar el script manual:
     mysql -u root -p < cloty-mysql.sql

2. API

     cd cloty-api
     mvnw spring-boot:run

   La API queda en http://localhost:8080

   Para correos en local, copie application-local.properties.example a
   application-local.properties y configure Gmail.

   Usuario inicial: superadmin / super123 (RUT 00000000-0)

3. Apps Android

   Configure local.properties (raiz del proyecto):
     cloty.api.host=127.0.0.1
     cloty.api.port=8080

   Con celular por USB:
     adb reverse tcp:8080 tcp:8080

   Compile e instale:
     cd clotyadministrador
     gradlew installDebug


================================================================================
  DESPLIEGUE EN AWS
================================================================================

  Consulte INSTRUCTIVO-AWS-PRUEBA.txt para subir la API a una instancia EC2
  con MariaDB, correo Gmail y conexion desde las apps moviles por internet.

  Plantillas de configuracion en scripts/aws/


================================================================================
  ARCHIVOS DE EJEMPLO
================================================================================

  apoderados-ejemplo.csv    Formato CSV para importar apoderados
  alumnos-ejemplo.csv       Formato CSV para importar alumnos (columna nivel)
  LEEME-csv-ejemplo.txt     Instrucciones de las columnas CSV


================================================================================
  LICENCIA
================================================================================

  Proyecto academico — Proyecto de Titulo.
