# Sistema de Recursos Humanos - IU Digital de Antioquia

Aplicación de escritorio en **Java Swing** para gestionar los funcionarios de la institución
(datos personales, grupo familiar e información académica). Actividad de *Desarrollo de Software
Seguro* que aplica el **patrón DAO** y el manejo de **excepciones**.

- Lenguaje: Java 21 · Interfaz: Swing · Base de datos: PostgreSQL · Construcción: Maven
- Integrante: (Marcelo Suárez Higuita)_

## Estructura

```
db/                           Scripts SQL y diagrama entidad-relación
src/main/java/co/edu/iud/
 ├── Main.java                Punto de entrada
 ├── conexion/ConexionDB      Conexión a PostgreSQL (credenciales desde config.properties)
 ├── modelo/                  Funcionario, MiembroFamiliar, InformacionAcademica, Catalogo
 ├── dao/                     FuncionarioDAO (interfaz), FuncionarioDAOImpl, CatalogoDAO
 ├── excepciones/             DAOException, ValidacionException
 ├── util/Validador           Validación de entradas antes de llegar al DAO
 └── vista/                   VentanaPrincipal, FuncionarioDialog (Swing)
```

## Cómo ejecutarlo

1. Instalar PostgreSQL y crear la base de datos: `CREATE DATABASE recursos_humanos;`
2. Conectado a esa base, ejecutar en orden los scripts de la carpeta `db/`:
   1. `crear_bd.sql` (tablas, índices y triggers)
   2. `poblar_bd.sql` (catálogos y datos de ejemplo)
   3. `usuario_app.sql` (rol `app_rrhh` con permisos mínimos; **cambie la contraseña** antes de usarlo)
3. Copiar `config.properties.example` como `config.properties` (en la raíz del proyecto) y escribir
   la contraseña real del rol `app_rrhh`. Este archivo está en el `.gitignore`.
4. Abrir el proyecto en IntelliJ IDEA (Maven descarga el driver de PostgreSQL) y ejecutar `Main`.
   La aplicación debe ejecutarse con la raíz del proyecto como directorio de trabajo.

## Cómo se aplica el patrón DAO

La vista (`vista/`) nunca escribe SQL: solo usa la interfaz `FuncionarioDAO` (`listar`, `buscar`,
`buscarPorId`, `crear`, `actualizar`, `eliminar`). `FuncionarioDAOImpl` contiene todo el JDBC. Cambiar
de motor de base de datos implica escribir otra implementación, sin tocar las ventanas.

## Excepciones

- `DAOException`: envuelve las `SQLException`. Su mensaje siempre es apto para el usuario; el detalle
  técnico va a la causa y al log, nunca a la pantalla.
- `ValidacionException`: un dato ingresado no cumple las reglas (campo vacío, formato, fecha imposible).
  La vista la muestra como advertencia para que el usuario corrija.

## Decisiones de seguridad

- **`PreparedStatement` en todas las consultas**: ningún dato del usuario se concatena en el SQL.
- **Validación con lista blanca** (`Validador`) antes de llamar al DAO, con los mismos límites que la BD.
- **Transacciones**: crear y actualizar guardan funcionario, familiares y estudios completos o nada.
- **Mínimo privilegio**: la aplicación usa el rol `app_rrhh` (solo `SELECT` en catálogos), nunca `postgres`.
- **Sin secretos en el repositorio**: la contraseña vive en `config.properties`, ignorado por Git.
- **Errores sin filtrar información interna**: los mensajes de PostgreSQL no se muestran al usuario.
- **Integridad referencial**: `ON DELETE CASCADE` para familiares y estudios; `RESTRICT` en los catálogos.
