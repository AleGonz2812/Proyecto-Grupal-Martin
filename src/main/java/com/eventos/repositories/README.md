# 📚 Guía de Uso - Repositorios

Los repositorios proporcionan acceso a la base de datos de forma sencilla y organizada.

## 📦 Repositorios Disponibles

- `UsuarioRepository` - Gestión de usuarios
- `EventoRepository` - Gestión de eventos  
- `CompraRepository` - Gestión de compras
- `EntradaRepository` - Gestión de entradas
- `SedeRepository` - Gestión de sedes
- `RolRepository` - Gestión de roles
- `TipoEventoRepository` - Gestión de tipos de evento

---

## 🔧 Ejemplos de Uso

### 1️⃣ UsuarioRepository

```java
// Crear instancia del repositorio
UsuarioRepository usuarioRepo = new UsuarioRepository();

// Buscar usuario por email
Optional<Usuario> usuario = usuarioRepo.findByEmail("admin@eventos.com");
if (usuario.isPresent()) {
    System.out.println("Usuario: " + usuario.get().getNombre());
}

// Obtener todos los usuarios
List<Usuario> todos = usuarioRepo.findAll();

// Buscar usuarios activos
List<Usuario> activos = usuarioRepo.findActivos();

// Buscar por nombre
List<Usuario> encontrados = usuarioRepo.searchByName("Juan");

// Guardar nuevo usuario
Usuario nuevo = new Usuario();
nuevo.setEmail("nuevo@eventos.com");
nuevo.setNombre("Juan");
nuevo.setActivo(true);
Usuario guardado = usuarioRepo.save(nuevo);

// Actualizar usuario
Usuario actualizar = usuarioRepo.findById(1L).orElse(null);
if (actualizar != null) {
    actualizar.setNombre("Nuevo Nombre");
    usuarioRepo.update(actualizar);
}

// Eliminar usuario
boolean eliminado = usuarioRepo.delete(1L);
```

### 2️⃣ EventoRepository

```java
EventoRepository eventoRepo = new EventoRepository();

// Eventos próximos
List<Evento> proximos = eventoRepo.findProximosEventos();

// Eventos con entradas disponibles
List<Evento> disponibles = eventoRepo.findConEntradasDisponibles();

// Buscar por estado (PROGRAMADO, EN_CURSO, FINALIZADO, CANCELADO)
List<Evento> programados = eventoRepo.findByEstado("PROGRAMADO");

// Buscar en rango de fechas
LocalDateTime inicio = LocalDateTime.now();
LocalDateTime fin = inicio.plusMonths(1);
List<Evento> delMes = eventoRepo.findByFechaRange(inicio, fin);

// Buscar por nombre
List<Evento> conciertos = eventoRepo.searchByNombre("Concierto");

// Eventos de una sede
List<Evento> eventosSede = eventoRepo.findBySede(1L);
```

### 3️⃣ CompraRepository

```java
CompraRepository compraRepo = new CompraRepository();

// Compras de un usuario
List<Compra> misCompras = compraRepo.findByUsuario(1L);

// Buscar por código de confirmación
Optional<Compra> compra = compraRepo.findByCodigoConfirmacion("CONF-12345");

// Buscar por estado (PENDIENTE, COMPLETADA, CANCELADA, REEMBOLSADA)
List<Compra> completadas = compraRepo.findByEstado("COMPLETADA");

// Total gastado por usuario
Double total = compraRepo.getTotalGastadoPorUsuario(1L);

// Últimas 10 compras
List<Compra> ultimas = compraRepo.findUltimasCompras(10);
```

### 4️⃣ EntradaRepository

```java
EntradaRepository entradaRepo = new EntradaRepository();

// Entradas de un evento
List<Entrada> entradasEvento = entradaRepo.findByEvento(1L);

// Entradas de una compra
List<Entrada> entradasCompra = entradaRepo.findByCompra(1L);

// Buscar por código QR
Optional<Entrada> entrada = entradaRepo.findByCodigoQR("QR-ABC123");

// Buscar por número
Optional<Entrada> porNumero = entradaRepo.findByNumero("ENT-001");

// Contar entradas
long total = entradaRepo.countByEvento(1L);
long validadas = entradaRepo.countValidadasByEvento(1L);

// Sin validar
List<Entrada> sinValidar = entradaRepo.findByValidada(false);
```

### 5️⃣ SedeRepository

```java
SedeRepository sedeRepo = new SedeRepository();

// Sedes activas
List<Sede> activas = sedeRepo.findActivas();

// Por ciudad
List<Sede> sedesMadrid = sedeRepo.findByCiudad("Madrid");

// Con capacidad mínima
List<Sede> grandes = sedeRepo.findByCapacidadMinima(1000);

// Buscar por nombre
List<Sede> auditorios = sedeRepo.searchByNombre("Auditorio");
```

### 6️⃣ RolRepository

```java
RolRepository rolRepo = new RolRepository();

// Buscar rol por nombre
Optional<Rol> admin = rolRepo.findByNombre("ADMIN");
Optional<Rol> usuario = rolRepo.findByNombre("USUARIO");
Optional<Rol> empleado = rolRepo.findByNombre("EMPLEADO");

// Todos los roles
List<Rol> roles = rolRepo.findAll();
```

### 7️⃣ TipoEventoRepository

```java
TipoEventoRepository tipoRepo = new TipoEventoRepository();

// Por categoría (CULTURAL, DEPORTIVO, CORPORATIVO, ENTRETENIMIENTO)
List<TipoEvento> culturales = tipoRepo.findByCategoria("CULTURAL");
List<TipoEvento> deportivos = tipoRepo.findByCategoria("DEPORTIVO");

// Buscar por nombre
List<TipoEvento> conciertos = tipoRepo.searchByNombre("Concierto");

// Todos
List<TipoEvento> todos = tipoRepo.findAll();
```

---

## 💡 Operaciones Comunes (Heredadas de GenericRepository)

Todos los repositorios tienen estos métodos básicos:

```java
// Guardar nueva entidad
T save(T entity)

// Actualizar entidad existente
T update(T entity)

// Buscar por ID
Optional<T> findById(ID id)

// Obtener todos
List<T> findAll()

// Eliminar por ID
boolean delete(ID id)

// Contar total
long count()

// Verificar existencia
boolean existsById(ID id)
```

---

## 🎯 Caso de Uso Completo: Login de Usuario

```java
public Usuario login(String email, String password) {
    UsuarioRepository usuarioRepo = new UsuarioRepository();
    
    // Buscar usuario
    Optional<Usuario> usuarioOpt = usuarioRepo.findByEmail(email);
    
    if (usuarioOpt.isEmpty()) {
        System.out.println("Usuario no encontrado");
        return null;
    }
    
    Usuario usuario = usuarioOpt.get();
    
    // Verificar si está activo
    if (!usuario.getActivo()) {
        System.out.println("Usuario inactivo");
        return null;
    }
    
    // Verificar contraseña (usando PasswordUtil)
    if (!PasswordUtil.checkPassword(password, usuario.getPassword())) {
        System.out.println("Contraseña incorrecta");
        return null;
    }
    
    // Actualizar última conexión
    usuario.setUltimaConexion(LocalDateTime.now());
    usuarioRepo.update(usuario);
    
    return usuario;
}
```

---

## 📝 Notas Importantes

1. **EntityManager**: Todos los repositorios usan `HibernateUtil.getEntityManager()`
2. **Transacciones**: Los métodos `save`, `update` y `delete` manejan transacciones automáticamente
3. **Optional**: Usa `.isPresent()` y `.get()` o `.orElse()` para manejar resultados que pueden no existir
4. **Excepciones**: Los repositorios lanzan `RuntimeException` en caso de error en BD

---

## 🔄 Próximos Pasos

Una vez domines los repositorios, puedes:
1. Crear **Servicios** que usen múltiples repositorios
2. Implementar **Controladores** para la lógica de la UI
3. Desarrollar **Exportación/Importación** XML/JSON
4. Crear las **Vistas** JavaFX

---

**Equipo:** Fran / Ale / LuisM  
**Proyecto:** Sistema de Gestión de Eventos  
**Fecha:** Diciembre 2025
