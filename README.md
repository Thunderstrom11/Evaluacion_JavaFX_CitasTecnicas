# Evaluacion_JavaFX_CitasTecnicas

Sistema de gestion de citas de atencion tecnica para TecnoAsistencia Nicaragua S.A.
Aplicacion de escritorio JavaFX conectada a PostgreSQL mediante JDBC.

## Integrantes y responsabilidades

| Integrante | Usuario GitHub | Responsabilidad |
|---|---|---|
| Jose Salgado | EtherealWeb5167 | Script SQL, conexion JDBC, DAO y CRUD de citas, validaciones, pantalla de carga con hilos |
| Diego Chamendy | Thunderstrom11 | Configuracion Maven, modelo y DAO de cliente, CRUD de clientes, menu principal, utilidades |

## Requisitos

- JDK 21
- Maven
- PostgreSQL 14 o superior
- IntelliJ IDEA

## Configuracion de PostgreSQL

1. Crear la base de datos:

   ```sql
   CREATE DATABASE gestion_citas;
   ```

2. Ejecutar el script `gestion_citas.sql` en la base `gestion_citas`.

3. Abrir `src/main/java/ni/edu/uam/evaluacion_javafx_citastecnicas/util/ConexionDB.java`
   y ajustar el usuario y la clave a los de su instalacion local:

   ```java
   private static final String URL = "jdbc:postgresql://localhost:5432/gestion_citas";
   private static final String USUARIO = "postgres";
   private static final String CLAVE = "postgres";
   ```

   Las credenciales reales no se suben al repositorio.

## Como ejecutar

Desde IntelliJ: abrir el proyecto, esperar la sincronizacion de Maven y ejecutar
`Launcher`.

Desde la terminal:

   ```bash
mvn clean javafx:run
   ```

## Funcionalidades

- CRUD completo de clientes con busqueda por nombre, apellido o correo.
- CRUD completo de citas tecnicas con seleccion de cliente en ComboBox.
- Filtros de citas por estado y por fecha, con opcion de limpiarlos.
- Validacion de campos obligatorios, formato de correo, correo duplicado,
  fecha y hora requeridas y estados permitidos.
- Confirmacion antes de eliminar y bloqueo de la eliminacion de un cliente
  que tenga citas asociadas.
- Pantalla de carga con un hilo de trabajo que valida la conexion y cuenta
  los registros sin bloquear la interfaz.

## Estructura del proyecto

- `model/` clases Cliente y CitaTecnica
- `dao/` acceso a datos con PreparedStatement y ResultSet
- `controller/` controladores FXML
- `util/` conexion, alertas, validaciones y navegacion
- `resources/ni/edu/uam/evaluacion_javafx_citastecnicas/` interfaces declarativas
- `gestion_citas.sql` script de creacion de la base de datos
