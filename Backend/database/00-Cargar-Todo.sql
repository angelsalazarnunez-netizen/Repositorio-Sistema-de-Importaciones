-- Autor: Equipo de Desarrollo SIM
-- Fecha: 18/10/2026
-- Descripción: Carga completa de la base de datos sim. Ejecuta, en orden, todos los scripts de
--   esta carpeta, para no tener que correrlos uno por uno.
-- Motor: MySQL 8+
--
-- Cómo se usa (desde la carpeta Backend\database, porque las rutas son relativas a ella):
--   cd C:\Users\Angel\Documents\GitHub\Repositorio-2026-07\Backend\database
--   mysql -u root -p < 00-Cargar-Todo.sql
-- o, dentro del cliente mysql abierto en esa carpeta:
--   SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/00-Cargar-Todo.sql;
-- En PowerShell, si mysql no está en el PATH:
--   cmd /c '"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < 00-Cargar-Todo.sql'
--
-- Importante:
--   - SOURCE es un comando del cliente mysql (consola o MySQL Shell en modo SQL). MySQL Workbench
--     no lo ejecuta: ahí hay que abrir y correr cada script por separado.
--   - 02-Tablas-Tramites.sql vuelve a crear las tablas, así que esta carga borra los datos que
--     tenga la base. Sirve para dejar la base limpia y al día.
--   - Cuando se agregue un script nuevo (16-..., 17-...), hay que agregar su línea SOURCE al
--     final de esta lista.

SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/02-Tablas-Tramites.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/03-Datos-Iniciales.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/04-Tabla-Sesion-HU004.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/04-Datos-Prueba.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/05-Tablas-Pedido-Importacion-HU006.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/06-Tablas-Tramites-Temporales.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/06-Tablas-exoneraciones.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/07-Datos-Prueba-Temporales.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/08-Control-Documental-Exoneraciones.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/09-Usuario-Pruebas-Administrador.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/10-Aprobacion-Exoneraciones.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/11-Panel-Tramites-Activos.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/12-Tablas-Autorizacion-Exonet.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/13-Prorrogas-Tramites-Temporales-HU028.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/14-Permisos-Seguridad.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/15-Ajustes-Comentarios-Unidad.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/16-Modulos-Permisos-Roles-Base.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/17-Partidas-Pedido-Importacion.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/18-Cantidad-Pedido-Opcional.sql;
SOURCE C:/Users/Angel/Documents/GitHub/Repositorio-2026-07/Backend/database/19-Ajustes-Pruebas-Usabilidad.sql;


SELECT 'Carga completa de la base sim terminada' AS MENSAJE;
