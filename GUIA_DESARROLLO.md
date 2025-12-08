# 🎭 Sistema de Gestión de Eventos - Guía de Desarrollo

## 📋 ¿Qué se ha implementado?

### ✅ Sistema de Autenticación Completo

Se ha creado el **módulo de autenticación** que es la base del sistema. Incluye:

#### 1️⃣ **Service (Lógica de Negocio)**
**Archivo**: `src/main/java/com/eventos/services/AutenticacionService.java`

- ✅ Login con validación de credenciales (email + contraseña)
- ✅ Encriptación de contraseñas con BCrypt
- ✅ Gestión de sesión (usuario actualmente logueado)
- ✅ Logout (cierre de sesión)
- ✅ Verificación de roles (Administrador / Usuario normal)
- ✅ Patrón Singleton para mantener una única sesión

**Métodos principales**:
```java
login(email, password)          // Iniciar sesión
logout()                         // Cerrar sesión
getUsuarioActual()              // Obtener usuario logueado
esAdministrador()               // Verificar si es admin
requireSesionActiva()           // Validar que haya sesión
```

---

#### 2️⃣ **Controller (Controlador de Login)**
**Archivo**: `src/main/java/com/eventos/controllers/LoginController.java`

- ✅ Gestiona la interfaz de inicio de sesión
- ✅ Valida campos antes de enviarlos al service
- ✅ Muestra mensajes de error al usuario
- ✅ Redirige al dashboard tras login exitoso
- ✅ Habilita/deshabilita botón según campos llenos

**Funcionalidades**:
- Login con botón o presionando Enter
- Validación de formato de email
- Limpieza de contraseña en caso de error
- Enlace a registro (preparado para futuro)

---

#### 3️⃣ **View (Vista de Login)**
**Archivo**: `src/main/resources/fxml/login.fxml`

Interfaz gráfica con JavaFX que incluye:
- ✅ Campo de email
- ✅ Campo de contraseña (oculta caracteres)
- ✅ Botón de iniciar sesión
- ✅ Mensaje de error (oculto por defecto)
- ✅ Enlace de registro
- ✅ Diseño responsive y centrado

---

#### 4️⃣ **Controller (Controlador del Dashboard)**
**Archivo**: `src/main/java/com/eventos/controllers/DashboardController.java`

- ✅ Pantalla principal tras login exitoso
- ✅ Muestra información del usuario logueado
- ✅ Menú lateral con opciones de navegación
- ✅ Menú de administrador (solo visible para admins)
- ✅ Estadísticas básicas (eventos, entradas, usuarios)
- ✅ Botón de cerrar sesión con confirmación

**Opciones del menú**:
- 🏠 Inicio
- 🎭 Eventos
- 🎟️ Mis Entradas  
- 🛒 Comprar Entradas
- **[Solo Admin]** 👥 Gestión de Usuarios
- **[Solo Admin]** 🎪 Gestión de Eventos
- **[Solo Admin]** 🏢 Gestión de Sedes
- **[Solo Admin]** 📊 Reportes

---

#### 5️⃣ **View (Vista del Dashboard)**
**Archivo**: `src/main/resources/fxml/dashboard.fxml`

Interfaz principal con:
- ✅ Barra superior con nombre de usuario, rol y botón de logout
- ✅ Menú lateral con opciones de navegación
- ✅ Área central para cargar contenido dinámicamente
- ✅ Cards con estadísticas (eventos, entradas, usuarios)

---

#### 6️⃣ **Main Application**
**Archivo**: `src/main/java/com/eventos/Main.java`

- ✅ Punto de entrada de la aplicación JavaFX
- ✅ Inicializa Hibernate automáticamente
- ✅ Carga la pantalla de login al iniciar
- ✅ Gestiona cierre de recursos al salir
- ✅ Logging detallado de todas las operaciones

---

#### 7️⃣ **Estilos CSS**
**Archivo**: `src/main/resources/css/styles.css`

- ✅ Diseño moderno y profesional
- ✅ Paleta de colores consistente
- ✅ Botones con efectos hover y pressed
- ✅ Campos de texto con focus destacado
- ✅ Estilos para tablas, listas, cards
- ✅ Mensajes de error con formato destacado

---

## 🚀 Cómo Ejecutar el Proyecto

### Requisitos Previos
1. **Java 17** instalado ✅ (ya lo tienes)
2. **Maven** instalado
3. **MySQL** con XAMPP
4. Base de datos creada y configurada

### Pasos para ejecutar:

1. **Iniciar MySQL en XAMPP**
   ```
   - Abrir XAMPP Control Panel
   - Iniciar Apache y MySQL
   ```

2. **Compilar el proyecto**
   ```bash
   mvn clean compile
   ```

3. **Ejecutar la aplicación**
   ```bash
   mvn javafx:run
   ```

   O desde tu IDE:
   - Ejecutar la clase `Main.java`

---

## 📁 Estructura del Proyecto Actualizada

```
src/main/java/com/eventos/
├── Main.java                           ✅ ACTUALIZADO - Lanza JavaFX
├── config/
│   ├── HibernateUtil.java             ✅ (existente)
│   └── ConfigManager.java             ✅ (existente)
├── controllers/                        ✅ NUEVO
│   ├── LoginController.java           ✅ Login
│   └── DashboardController.java       ✅ Pantalla principal
├── services/                           ✅ ACTUALIZADO
│   ├── AutenticacionService.java      ✅ NUEVO - Lógica de login
│   ├── EventoJSONService.java         ✅ (existente)
│   └── EventoXMLService.java          ✅ (existente)
├── models/                             ✅ (10 entidades existentes)
├── repositories/                       ✅ (8 repositorios existentes)
├── dto/, exceptions/, utils/           ✅ (existentes)

src/main/resources/
├── fxml/                               ✅ NUEVO
│   ├── login.fxml                     ✅ Interfaz de login
│   └── dashboard.fxml                 ✅ Interfaz principal
├── css/                                ✅ NUEVO
│   └── styles.css                     ✅ Estilos visuales
├── config.properties                   ✅ (existente)
├── log4j2.xml                         ✅ (existente)
└── META-INF/persistence.xml           ✅ (existente)
```

---

## 🎯 Siguientes Pasos - Plan de Trabajo

### Para el siguiente compañero:

#### **OPCIÓN A: Módulo de Eventos**
Crear funcionalidades de gestión de eventos:

1. **EventoService.java** - Lógica de negocio
   - CRUD de eventos
   - Cambiar estado (Activo/Cancelado/Finalizado)
   - Gestionar aforo
   - Buscar eventos por filtros

2. **Vistas de Eventos**:
   - `lista-eventos.fxml` - Catálogo público de eventos
   - `detalle-evento.fxml` - Ver detalles completos
   - `form-evento.fxml` - Crear/editar evento (admin)

3. **Controllers**:
   - `ListaEventosController.java`
   - `DetalleEventoController.java`
   - `FormEventoController.java`

#### **OPCIÓN B: Módulo de Compras y Entradas**
Crear proceso de compra de entradas:

1. **CompraService.java** - Lógica de negocio
   - Crear compra
   - Calcular total
   - Generar código QR

2. **EntradaService.java**
   - Generar entradas
   - Validar entrada con QR
   - Registrar acceso

3. **Vistas**:
   - `compra.fxml` - Carrito de compra
   - `mis-entradas.fxml` - Entradas del usuario
   - `validar-entrada.fxml` - Escanear QR

4. **Controllers** correspondientes

---

## 🔑 Cómo Probar el Login

### Usuarios de prueba (según tu SQL)
Primero necesitas crear usuarios en la base de datos. Ejemplo:

```sql
-- Insertar un usuario de prueba
INSERT INTO usuarios (email, password, nombre, rol_id, activo, fecha_alta)
VALUES (
    'admin@eventos.com',
    '$2a$10$abcdefghijk...',  -- Hash BCrypt de "admin123"
    'Administrador',
    1,  -- ID del rol administrador
    true,
    NOW()
);
```

### Generar hash BCrypt para contraseñas:
Puedes usar el `PasswordUtil` que ya existe:

```java
String hash = PasswordUtil.encriptarPassword("tuContraseña");
System.out.println(hash);
```

---

## 📝 Comentarios en el Código

**TODOS los archivos creados tienen comentarios explicativos**:

- ✅ Javadoc en clases y métodos
- ✅ Comentarios inline para lógica compleja
- ✅ Explicación de patrones de diseño utilizados
- ✅ TODOs para funcionalidades futuras
- ✅ Descripción de responsabilidades

**Esto facilita**:
- Entender el código rápidamente
- Presentación del proyecto
- Trabajo en equipo
- Mantenimiento futuro

---

## 🎨 Características del Sistema

### Seguridad
- ✅ Contraseñas encriptadas con BCrypt
- ✅ Validación de sesión en todas las operaciones
- ✅ Control de acceso por roles
- ✅ Protección contra SQL injection (JPA/Hibernate)

### Usabilidad
- ✅ Interfaz intuitiva y moderna
- ✅ Mensajes de error claros
- ✅ Confirmaciones en acciones importantes
- ✅ Navegación fluida entre pantallas
- ✅ Feedback visual (hover, focus, pressed)

### Accesibilidad
- ✅ Navegación con teclado (Tab, Enter)
- ✅ Textos legibles
- ✅ Contraste de colores adecuado
- ✅ Tamaños de fuente apropiados

---

## 🐛 Solución de Problemas

### Error: "No se puede conectar a la base de datos"
- Verifica que MySQL esté corriendo en XAMPP
- Revisa `config.properties` con credenciales correctas
- Asegúrate que la base de datos existe

### Error: "No se encuentra el archivo FXML"
- Verifica que los archivos estén en `src/main/resources/fxml/`
- Ejecuta `mvn clean compile` para copiarlos a target

### Error: "No se cargan los estilos CSS"
- Verifica que `styles.css` esté en `src/main/resources/css/`
- Revisa los logs para ver si hay error al cargarlos

---

## 📞 Contacto

Para dudas sobre esta implementación:
- Revisar comentarios en el código
- Revisar logs de la aplicación (carpeta `logs/`)
- Consultar documentación de JavaFX y Hibernate

---

**¡El sistema está listo para empezar a trabajar! 🚀**

Ahora pueden continuar creando las demás funcionalidades siguiendo la misma estructura MVC.
