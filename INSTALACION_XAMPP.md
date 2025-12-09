# 📦 Guía de Instalación - XAMPP y MySQL

## Configuración del entorno de desarrollo con XAMPP

---

## 🔧 Paso 1: Instalar XAMPP

1. **Descargar XAMPP**
   - Ir a [https://www.apachefriends.org/](https://www.apachefriends.org/)
   - Descargar la versión para tu sistema operativo
   - Ejecutar el instalador

2. **Instalación**
   - Seleccionar componentes: **Apache** y **MySQL** (mínimo necesario)
   - Instalar en la ruta por defecto: `C:\xampp`
   - Completar la instalación

---

## 🚀 Paso 2: Iniciar Servicios

1. **Abrir XAMPP Control Panel**
   - Buscar "XAMPP Control Panel" en el menú inicio
   - Ejecutar como Administrador (recomendado)

2. **Iniciar MySQL**
   - Click en el botón **"Start"** junto a MySQL
   - Esperar a que el estado cambie a verde
   - (Opcional) Click en **"Start"** junto a Apache si quieres usar phpMyAdmin

---

## 💾 Paso 3: Crear la Base de Datos

### Opción A: Usando phpMyAdmin (Recomendado para principiantes)

1. **Acceder a phpMyAdmin**
   - Iniciar Apache en XAMPP
   - Abrir navegador: `http://localhost/phpmyadmin`

2. **Crear Base de Datos**
   - Click en "Nueva" en el panel izquierdo
   - Nombre de la base de datos: `eventos_db`
   - Cotejamiento: `utf8mb4_unicode_ci`
   - Click en "Crear"

3. **Ejecutar Scripts SQL**
   - Seleccionar la base de datos `eventos_db`
   - Click en la pestaña "SQL"
   - Copiar y pegar el contenido de `src/main/resources/sql/schema.sql`
   - Click en "Continuar"
   - Repetir con `src/main/resources/sql/data.sql`

### Opción B: Usando línea de comandos

```bash
# 1. Abrir terminal
# 2. Navegar a la carpeta de MySQL
cd C:\xampp\mysql\bin

# 3. Conectar a MySQL
mysql -u root -p
# (Presionar Enter si no hay password)

# 4. Ejecutar scripts
source C:/ruta/al/proyecto/src/main/resources/sql/schema.sql
source C:/ruta/al/proyecto/src/main/resources/sql/data.sql

# 5. Verificar
USE eventos_db;
SHOW TABLES;
```

### Opción C: Dejar que Hibernate lo haga automáticamente

- La aplicación creará las tablas automáticamente al ejecutarse
- Configurado en `persistence.xml` con `hibernate.hbm2ddl.auto=update`
- La base de datos `eventos_db` se creará si no existe (gracias al parámetro `createDatabaseIfNotExist=true`)

---

## ✅ Paso 4: Verificar la Configuración

### Verificar conexión MySQL

1. **En phpMyAdmin**
   - Verificar que existe la base de datos `eventos_db`
   - Verificar que hay tablas creadas
   - Verificar que hay datos iniciales (1 usuario admin, roles, etc.)

2. **Desde el proyecto**
   - Ejecutar la aplicación Java
   - Verificar en los logs que la conexión es exitosa

### Datos de acceso por defecto

**Usuario Admin:**
- Email: `admin@eventos.com`
- Password: `admin123`

**MySQL (XAMPP):**
- Host: `localhost`
- Puerto: `3306`
- Usuario: `root`
- Contraseña: *(vacía por defecto)*
- Base de datos: `eventos_db`

---

## 🔒 Configuración de Seguridad (Opcional)

Si quieres añadir una contraseña a MySQL:

1. **Abrir phpMyAdmin**
2. **Ir a "Cuentas de usuario"**
3. **Editar usuario "root"**
4. **Cambiar contraseña**
5. **Actualizar archivos de configuración del proyecto:**
   - `src/main/resources/META-INF/persistence.xml`
   - `src/main/resources/config.properties`
   
   Cambiar:
   ```xml
   <property name="jakarta.persistence.jdbc.password" value="tu_nueva_contraseña"/>
   ```

---

## 🐛 Solución de Problemas Comunes

### Error: "Port 3306 already in use"

**Problema:** Otro servicio está usando el puerto MySQL

**Solución:**
1. Abrir XAMPP Config → MySQL
2. Cambiar puerto de `3306` a `3307`
3. Actualizar `persistence.xml` y `config.properties` con el nuevo puerto

### Error: "Access denied for user 'root'@'localhost'"

**Problema:** Contraseña incorrecta

**Solución:**
1. Verificar la contraseña de MySQL
2. Actualizar los archivos de configuración del proyecto

### Error: "Unknown database 'eventos_db'"

**Problema:** La base de datos no existe

**Solución:**
1. Ejecutar el script `schema.sql` manualmente
2. O verificar que la URL incluya `createDatabaseIfNotExist=true`

### MySQL no inicia en XAMPP

**Problema:** El servicio MySQL no arranca

**Soluciones posibles:**
1. Verificar que el puerto 3306 esté libre
2. Ejecutar XAMPP como Administrador
3. Revisar los logs en `C:\xampp\mysql\data\mysql_error.log`
4. Reinstalar MySQL desde XAMPP

---

## 📝 Comandos Útiles MySQL

```sql
-- Ver todas las bases de datos
SHOW DATABASES;

-- Usar una base de datos
USE eventos_db;

-- Ver todas las tablas
SHOW TABLES;

-- Ver estructura de una tabla
DESCRIBE usuarios;

-- Ver todos los usuarios
SELECT * FROM usuarios;

-- Ver todos los roles
SELECT * FROM roles;

-- Ver eventos
SELECT * FROM eventos;

-- Limpiar todas las tablas (¡CUIDADO!)
DROP DATABASE eventos_db;
```

---

## 🔄 Actualizar Base de Datos

Si haces cambios en las entidades JPA:

1. **Modo automático (desarrollo):**
   - Hibernate actualizará las tablas automáticamente
   - Configurado con `hibernate.hbm2ddl.auto=update`

2. **Modo manual (producción):**
   - Cambiar `update` a `validate` en `persistence.xml`
   - Ejecutar scripts SQL manualmente

---

## ✅ Checklist de Instalación

- [ ] XAMPP instalado
- [ ] MySQL iniciado en XAMPP
- [ ] Base de datos `eventos_db` creada
- [ ] Tablas creadas (schema.sql ejecutado)
- [ ] Datos iniciales cargados (data.sql ejecutado)
- [ ] Usuario admin creado y funcional
- [ ] Proyecto Java compilado sin errores
- [ ] Conexión a base de datos exitosa

---

**Si todo está correcto, ¡ya puedes empezar a desarrollar!** 🚀
