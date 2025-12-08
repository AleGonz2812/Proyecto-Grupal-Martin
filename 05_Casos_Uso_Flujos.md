# Casos de Uso y Flujos de Trabajo - Sistema de Gestión de Eventos

**Proyecto:** Sistema de Gestión de Empresa de Eventos  
**Versión:** 1.0  
**Fecha:** 4 de noviembre de 2025

---

## 👥 Actores del Sistema

| Actor | Descripción | Permisos |
|-------|-------------|----------|
| **Usuario Normal** | Cliente que compra entradas | Registrarse, iniciar sesión, comprar entradas, ver historial |
| **Administrador** | Gestiona el sistema completo | Crear eventos, gestionar sedes, ver informes, gestionar usuarios |
| **Empleado** | Valida entradas en eventos | Escanear QR, validar entradas, registrar ingresos |

---

## 📋 Casos de Uso Principales

### **CU-01: Registro de Usuario**

**Actor:** Usuario no registrado  
**Precondición:** Ninguna  
**Flujo Principal:**
1. El usuario accede a la opción "Registrarse"
2. El sistema muestra formulario de registro
3. El usuario ingresa: email, contraseña, nombre, teléfono
4. El sistema valida los datos
5. El sistema crea el usuario con rol "USUARIO"
6. El sistema muestra mensaje de éxito
7. El usuario puede iniciar sesión

**Flujos Alternativos:**
- 4a. Email ya registrado → Mostrar error
- 4b. Contraseña débil → Solicitar contraseña más fuerte

---

### **CU-02: Inicio de Sesión**

**Actor:** Usuario registrado  
**Precondición:** Tener cuenta registrada  
**Flujo Principal:**
1. El usuario accede a "Iniciar Sesión"
2. El sistema muestra formulario de login
3. El usuario ingresa email y contraseña
4. El sistema valida credenciales
5. El sistema genera sesión
6. El sistema redirige al panel correspondiente (Admin/Usuario)

**Flujos Alternativos:**
- 4a. Credenciales incorrectas → Mostrar error
- 4b. Usuario desactivado → Mostrar mensaje de cuenta desactivada

---

### **CU-03: Buscar Eventos**

**Actor:** Usuario  
**Precondición:** Haber iniciado sesión  
**Flujo Principal:**
1. El usuario accede a "Buscar Eventos"
2. El sistema muestra lista de eventos activos
3. El usuario aplica filtros (fecha, tipo, ciudad, precio)
4. El sistema muestra eventos filtrados
5. El usuario selecciona un evento para ver detalles

**Filtros disponibles:**
- Por tipo de evento (concierto, teatro, deportivo)
- Por rango de fechas
- Por ciudad/sede
- Por rango de precios
- Por disponibilidad

---

### **CU-04: Comprar Entradas**

**Actor:** Usuario  
**Precondición:** Haber iniciado sesión, evento con disponibilidad  
**Flujo Principal:**
1. El usuario selecciona un evento
2. El sistema muestra detalles y tipos de entrada
3. El usuario selecciona tipo y cantidad de entradas
4. El sistema calcula el total
5. El sistema muestra resumen de compra
6. El usuario confirma la compra
7. El sistema procesa el pago (simulado)
8. El sistema crea la compra y las entradas
9. El sistema genera códigos QR para cada entrada
10. El sistema genera confirmación JSON
11. El sistema envía confirmación por email
12. El sistema muestra mensaje de éxito y códigos QR

**Flujos Alternativos:**
- 3a. No hay suficiente disponibilidad → Mostrar error
- 7a. Pago rechazado → Cancelar compra
- 9a. Error generando QR → Reintentar

**Postcondición:**
- Compra registrada en BD
- Aforo del evento actualizado
- QR generados y guardados
- Email enviado al usuario

---

### **CU-05: Ver Historial de Compras**

**Actor:** Usuario  
**Precondición:** Haber iniciado sesión  
**Flujo Principal:**
1. El usuario accede a "Mis Compras"
2. El sistema muestra lista de compras del usuario
3. El usuario selecciona una compra
4. El sistema muestra detalles completos:
   - Código de confirmación
   - Fecha de compra
   - Evento(s)
   - Entradas con códigos QR
   - Total pagado
5. El usuario puede descargar códigos QR
6. El usuario puede exportar confirmación JSON

---

### **CU-06: Validar Entrada (Empleado)**

**Actor:** Empleado  
**Precondición:** Haber iniciado sesión como empleado  
**Flujo Principal:**
1. El empleado accede a "Validar Entrada"
2. El sistema muestra opción de escanear QR
3. El empleado escanea el código QR
4. El sistema decodifica el QR y obtiene datos JSON
5. El sistema valida:
   - Entrada existe
   - Entrada no ha sido validada previamente
   - Entrada corresponde al evento actual
6. El sistema marca la entrada como validada
7. El sistema registra la entrada (RegistroEntrada)
8. El sistema muestra mensaje de éxito

**Flujos Alternativos:**
- 5a. Entrada no existe → Mostrar error "Entrada inválida"
- 5b. Entrada ya validada → Mostrar "Entrada ya utilizada"
- 5c. Entrada para otro evento → Mostrar "Entrada incorrecta"

**Validación manual (alternativa):**
1. El empleado ingresa código de entrada manualmente
2. El sistema busca la entrada
3. Continúa desde paso 5

---

### **CU-07: Crear Evento (Administrador)**

**Actor:** Administrador  
**Precondición:** Haber iniciado sesión como administrador  
**Flujo Principal:**
1. El administrador accede a "Crear Evento"
2. El sistema muestra formulario
3. El administrador completa:
   - Nombre del evento
   - Descripción
   - Tipo de evento
   - Sede
   - Fecha inicio y fin
   - Aforo máximo
   - Precio base
4. El sistema valida los datos
5. El sistema crea el evento
6. El sistema permite definir tipos de entrada
7. El administrador configura precios por tipo
8. El sistema guarda el evento completo

**Flujos Alternativos:**
- 4a. Sede no disponible en esa fecha → Mostrar conflicto
- 4b. Fecha en el pasado → Mostrar error

---

### **CU-08: Gestionar Sedes (Administrador)**

**Actor:** Administrador  
**Flujo Principal:**
1. El administrador accede a "Gestión de Sedes"
2. El sistema muestra lista de sedes
3. El administrador puede:
   - Crear nueva sede
   - Editar sede existente
   - Desactivar sede
   - Ver equipamiento de la sede
4. El sistema actualiza los cambios

**Gestión de Equipamiento:**
1. El administrador selecciona una sede
2. El sistema muestra equipamiento
3. El administrador puede añadir/editar equipamiento
4. El sistema actualiza el inventario

---

### **CU-09: Exportar Eventos a XML (Administrador)**

**Actor:** Administrador  
**Precondición:** Existen eventos en el sistema  
**Flujo Principal:**
1. El administrador accede a "Exportar Datos"
2. El sistema muestra opciones de exportación
3. El administrador selecciona "Exportar Eventos XML"
4. El administrador aplica filtros (fecha, tipo, estado)
5. El sistema genera archivo XML con eventos
6. El sistema valida XML contra esquema XSD
7. El sistema guarda archivo en carpeta `exports/xml/`
8. El sistema muestra mensaje con ruta del archivo

**Formato del archivo:** `eventos_[fecha].xml`

---

### **CU-10: Importar Eventos desde XML (Administrador)**

**Actor:** Administrador  
**Precondición:** Tener archivo XML válido  
**Flujo Principal:**
1. El administrador accede a "Importar Datos"
2. El sistema muestra opción "Importar Eventos XML"
3. El administrador selecciona archivo XML
4. El sistema valida XML contra esquema XSD
5. El sistema parsea el XML
6. El sistema muestra preview de eventos a importar
7. El administrador confirma importación
8. El sistema crea eventos, sedes y tipos necesarios
9. El sistema muestra resumen de importación

**Flujos Alternativos:**
- 4a. XML inválido → Mostrar errores de validación
- 8a. Evento duplicado → Opción de actualizar o saltar

---

### **CU-11: Generar Informes (Administrador)**

**Actor:** Administrador  
**Flujo Principal:**
1. El administrador accede a "Informes y Estadísticas"
2. El sistema muestra tipos de informes:
   - Ventas por evento
   - Ventas por periodo
   - Eventos más populares
   - Ingresos totales
   - Aforo por evento
3. El administrador selecciona tipo de informe
4. El administrador define parámetros (fechas, eventos)
5. El sistema genera el informe
6. El sistema muestra datos y gráficos
7. El administrador puede exportar a:
   - XML estructurado
   - JSON
   - PDF (opcional)

---

## 🔄 Flujos de Trabajo Completos

### **Flujo: Ciclo de Vida de un Evento**

```
[ADMINISTRADOR]
    │
    ├─ 1. Crear Evento
    │    ├─ Definir detalles
    │    ├─ Asignar sede
    │    └─ Configurar tipos de entrada
    │
    ├─ 2. Publicar Evento
    │    └─ Estado: PLANIFICADO → ACTIVO
    │
[USUARIO]
    │
    ├─ 3. Buscar Evento
    │
    ├─ 4. Comprar Entradas
    │    ├─ Seleccionar cantidad y tipo
    │    ├─ Procesar pago
    │    ├─ Recibir confirmación JSON
    │    └─ Obtener códigos QR
    │
    ├─ 5. Día del Evento
    │
[EMPLEADO]
    │
    ├─ 6. Validar Entradas
    │    ├─ Escanear QR
    │    ├─ Verificar autenticidad
    │    └─ Registrar entrada
    │
[ADMINISTRADOR]
    │
    ├─ 7. Finalizar Evento
    │    └─ Estado: ACTIVO → FINALIZADO
    │
    └─ 8. Generar Informe
         ├─ Ventas totales
         ├─ Asistencia real
         └─ Exportar XML/JSON
```

---

### **Flujo: Backup y Restauración de Datos**

```
[BACKUP - Exportación]
    │
    ├─ 1. Exportar Eventos → XML
    │    └─ exports/xml/eventos_2025-11-04.xml
    │
    ├─ 2. Exportar Sedes → XML
    │    └─ exports/xml/sedes_2025-11-04.xml
    │
    ├─ 3. Exportar Compras → JSON
    │    └─ exports/json/compras_2025-11-04.json
    │
    └─ 4. Backup Base de Datos
         └─ SQL dump

[RESTAURACIÓN - Importación]
    │
    ├─ 1. Importar Eventos desde XML
    │    ├─ Validar XSD
    │    ├─ Parsear XML
    │    └─ Crear entidades
    │
    ├─ 2. Importar Sedes desde XML
    │
    └─ 3. Verificar integridad
```

---

## 📱 Interfaces de Usuario (Mockups)

### **Pantalla Principal - Usuario**

```
┌─────────────────────────────────────────────────────────────┐
│  GESTIÓN DE EVENTOS          [Juan Pérez ▼] [Cerrar Sesión] │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  [🔍 Buscar Eventos]  [🎟️ Mis Entradas]  [📜 Historial]     │
│                                                               │
│  ┌───────────────────────────────────────────────────────┐   │
│  │  EVENTOS DISPONIBLES                                  │   │
│  │  ───────────────────────────────────────────────────  │   │
│  │                                                       │   │
│  │  📅 Concierto de Rock en Vivo                        │   │
│  │     15 de Diciembre, 2025 - 20:00h                   │   │
│  │     📍 Auditorio Nacional, Madrid                    │   │
│  │     💰 Desde 45€ | 🎫 Disponibles: 3750/5000        │   │
│  │     [Ver Detalles] [Comprar Entradas]               │   │
│  │  ───────────────────────────────────────────────────  │   │
│  │                                                       │   │
│  │  🎭 Obra de Teatro - El Quijote                      │   │
│  │     20 de Noviembre, 2025 - 19:00h                   │   │
│  │     📍 Palacio de Congresos, Barcelona              │   │
│  │     💰 Desde 30€ | 🎫 Disponibles: 2200/3000        │   │
│  │     [Ver Detalles] [Comprar Entradas]               │   │
│  │                                                       │   │
│  └───────────────────────────────────────────────────────┘   │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

### **Pantalla de Compra**

```
┌─────────────────────────────────────────────────────────────┐
│  COMPRAR ENTRADAS - Concierto de Rock en Vivo      [← Volver]│
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  📅 15 de Diciembre, 2025 - 20:00h                           │
│  📍 Auditorio Nacional, Madrid                               │
│                                                               │
│  ┌───────────────────────────────────────────────────────┐   │
│  │  SELECCIONA TUS ENTRADAS                              │   │
│  │                                                       │   │
│  │  ○ General - 45.00€                                  │   │
│  │     Cantidad: [▼ 2]                                  │   │
│  │                                                       │   │
│  │  ○ Preferente - 75.00€                               │   │
│  │     Cantidad: [▼ 0]                                  │   │
│  │                                                       │   │
│  │  ○ VIP - 120.00€                                     │   │
│  │     Cantidad: [▼ 1]                                  │   │
│  │                                                       │   │
│  │  ─────────────────────────────────────────────────   │   │
│  │  TOTAL: 210.00€                                      │   │
│  │                                                       │   │
│  │  Método de pago: [▼ Tarjeta de Crédito]             │   │
│  │                                                       │   │
│  │  [Cancelar]              [Confirmar Compra]          │   │
│  └───────────────────────────────────────────────────────┘   │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

### **Pantalla de Confirmación**

```
┌─────────────────────────────────────────────────────────────┐
│  ✅ COMPRA REALIZADA CON ÉXITO                               │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Código de Confirmación: COMP-1730716200000                  │
│  Fecha: 04/11/2025 15:30:00                                  │
│                                                               │
│  ┌───────────────────────────────────────────────────────┐   │
│  │  TUS ENTRADAS                                         │   │
│  │                                                       │   │
│  │  Entrada 1: VIP                                      │   │
│  │  ┌─────────────────┐                                 │   │
│  │  │  [Código QR 1]  │  ENT-1730716201234-5678         │   │
│  │  └─────────────────┘                                 │   │
│  │  [Descargar QR] [Enviar por Email]                   │   │
│  │                                                       │   │
│  │  Entrada 2: General                                  │   │
│  │  ┌─────────────────┐                                 │   │
│  │  │  [Código QR 2]  │  ENT-1730716201234-5679         │   │
│  │  └─────────────────┘                                 │   │
│  │  [Descargar QR] [Enviar por Email]                   │   │
│  │                                                       │   │
│  │  ─────────────────────────────────────────────────   │   │
│  │  Total Pagado: 210.00€                               │   │
│  │                                                       │   │
│  └───────────────────────────────────────────────────────┘   │
│                                                               │
│  📧 Se ha enviado la confirmación a tu email                 │
│                                                               │
│  [Descargar Confirmación JSON] [Ver Mis Compras] [Inicio]    │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

### **Panel Administrador**

```
┌─────────────────────────────────────────────────────────────┐
│  PANEL ADMINISTRADOR              [Admin ▼] [Cerrar Sesión]  │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │   EVENTOS    │  │    SEDES     │  │   USUARIOS   │       │
│  │              │  │              │  │              │       │
│  │  Crear       │  │  Gestionar   │  │  Ver lista   │       │
│  │  Modificar   │  │  Equipamiento│  │  Roles       │       │
│  │  Eliminar    │  │              │  │              │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │   INFORMES   │  │ IMPORTAR XML │  │ EXPORTAR XML │       │
│  │              │  │              │  │              │       │
│  │  Ventas      │  │  Eventos     │  │  Eventos     │       │
│  │  Estadísticas│  │  Sedes       │  │  Sedes       │       │
│  │  Gráficos    │  │              │  │  Informes    │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│                                                               │
│  ┌───────────────────────────────────────────────────────┐   │
│  │  ESTADÍSTICAS RÁPIDAS                                 │   │
│  │                                                       │   │
│  │  📊 Eventos Activos: 12                              │   │
│  │  💰 Ingresos del mes: 45,230.50€                     │   │
│  │  🎫 Entradas vendidas hoy: 156                       │   │
│  │  👥 Usuarios registrados: 3,421                      │   │
│  │                                                       │   │
│  └───────────────────────────────────────────────────────┘   │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

## 🧪 Escenarios de Prueba

### **Test 1: Flujo Completo de Compra**
1. ✅ Registrar nuevo usuario
2. ✅ Iniciar sesión
3. ✅ Buscar evento disponible
4. ✅ Añadir 2 entradas al carrito
5. ✅ Procesar pago
6. ✅ Verificar generación de QR
7. ✅ Verificar creación de JSON
8. ✅ Verificar actualización de aforo

### **Test 2: Validación de Entrada**
1. ✅ Comprar entrada
2. ✅ Generar QR
3. ✅ Empleado escanea QR
4. ✅ Sistema valida entrada
5. ✅ Verificar que no se puede validar dos veces

### **Test 3: Exportación/Importación XML**
1. ✅ Crear 5 eventos
2. ✅ Exportar a XML
3. ✅ Validar XML con XSD
4. ✅ Eliminar eventos de BD
5. ✅ Importar desde XML
6. ✅ Verificar datos restaurados

---

**Siguiente documento:** Recomendaciones de Desarrollo y Mejores Prácticas
