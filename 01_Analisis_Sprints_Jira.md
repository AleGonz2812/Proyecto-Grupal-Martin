# Análisis de Sprints - Sistema de Gestión de Eventos

**Proyecto:** Sistema de Gestión de Empresa de Eventos  
**Equipo:** Fran/Ale/LuisM  
**Fecha:** 4 de noviembre de 2025

---

## 📋 Sprints Reorganizados

### **SPRINT 1 - Configuración y Autenticación Base (6 tareas)**
```
✅ PQDM-6: Configurar proyecto Java con Maven/Gradle
✅ PQDM-33: Diseñar modelo de datos usuarios y roles
✅ PQDM-34: Implementar persistencia con Hibernate
✅ PQDM-35: Desarrollar sistema de autenticación JWT/Session
✅ PQDM-1: Implementar endpoint de registro de usuario
✅ PQDM-2: Implementar endpoint de inicio de sesión
```

### **SPRINT 2 - Interfaces de Usuario y Eventos Base (7 tareas)**
```
✅ PQDM-43: Crear interfaz de registro de usuarios
✅ PQDM-44: Crear interfaz de inicio de sesión
✅ PQDM-4: Implementar menú de navegación principal
✅ PQDM-9: Diseñar modelo de datos para eventos
✅ PQDM-7: Completar CRUD de eventos (backend)
✅ PQDM-8: Implementar filtros de búsqueda
✅ PQDM-15: Pruebas unitarias de eventos
```

### **SPRINT 3 - Importación/Exportación XML/JSON (6 tareas)**
```
✅ PQDM-13: Desarrollar exportador XML de eventos
✅ PQDM-14: Implementar controladores de exportación
✅ PQDM-63: Crear exportador JSON de eventos
✅ PQDM-64: [NUEVA] Implementar importación de eventos desde XML
✅ PQDM-65: [NUEVA] Crear validador XSD para eventos
✅ PQDM-16: Pruebas unitarias exportación/importación
```

### **SPRINT 4 - Sedes, Equipamiento y Relaciones (7 tareas)**
```
✅ PQDM-18: Diseñar modelo de datos sedes y equipamiento
✅ PQDM-19: Implementar persistencia
✅ PQDM-20: Crear servicios de gestión de sedes
✅ PQDM-21: Desarrollar servicios de equipamiento
✅ PQDM-27: [MOVIDA] Implementar relaciones eventos-sedes-equipamiento
✅ PQDM-22: Interfaz administración de sedes
✅ PQDM-23: Interfaz gestión de inventario
```

### **SPRINT 5 - Sistema de Entradas y Precios (8 tareas)**
```
✅ PQDM-24: Diseñar modelo de tipos de entradas
✅ PQDM-25: Implementar persistencia de entradas
✅ PQDM-26: Servicios de gestión de tipos de entradas
✅ PQDM-28: Interfaz definición de tipos de entradas
✅ PQDM-30: Sistema de gestión de precios dinámicos
✅ PQDM-31: Sistema de control de disponibilidad
✅ PQDM-29: Pruebas unitarias
✅ PQDM-68: [NUEVA] Exportar configuración de precios en XML
```

### **SPRINT 6 - Proceso de Compra (7 tareas)**
```
✅ PQDM-36: Implementar flujo de compra de entradas
✅ PQDM-37: Sistema de procesamiento de pagos simulado
✅ PQDM-38: Generador de confirmaciones JSON
✅ PQDM-39: Biblioteca generación códigos QR
✅ PQDM-40: Interfaz de usuario para compra
✅ PQDM-42: Sistema de envío de confirmaciones (email)
✅ PQDM-46: Pruebas proceso de compra
```

### **SPRINT 7 - Validación y Historial (6 tareas)**
```
✅ PQDM-48: Sistema de verificación de entradas
✅ PQDM-49: Interfaz escaneo y validación QR
✅ PQDM-51: Registro de entrada a eventos
✅ PQDM-53: Sistema de historial de compras
✅ PQDM-54: Interfaz visualización de historial
✅ PQDM-32: Pruebas unitarias
```

### **SPRINT 8 - Administración Avanzada e Informes (6 tareas)**
```
✅ PQDM-3: [MOVIDA] Paneles completos del administrador
✅ PQDM-41: [MOVIDA] Sistema avanzado de roles y permisos
✅ PQDM-61: Motor de generación de informes
✅ PQDM-62: Servicios de recopilación de estadísticas
✅ PQDM-67: Interfaces de visualización de informes
✅ PQDM-70: Pruebas de informes
```

### **SPRINT 9 - Integración, Testing y Optimización (5 tareas)**
```
✅ PQDM-72: Pruebas de integración completas
✅ PQDM-73: [NUEVA] Optimización de consultas SQL
✅ PQDM-74: [NUEVA] Implementar sistema de caché
✅ PQDM-75: [NUEVA] Pruebas de carga y rendimiento
✅ PQDM-17: Refactorización y optimización del código
```

### **SPRINT 10 - Documentación y Entrega (4 tareas)**
```
✅ PQDM-45: [MOVIDA] Pruebas finales de autenticación
✅ PQDM-76: [NUEVA] Corrección de bugs críticos
✅ PQDM-77: Documentar API y sistema completo
✅ PQDM-78: Preparar manual de usuario
```

---

## 📈 Métricas de Sprints

| Sprint | Tareas Original | Tareas Propuesta | Carga |
|--------|-----------------|------------------|-------|
| 1      | 11 ❌           | 6 ✅             | Media |
| 2      | 4 ❌            | 7 ✅             | Alta  |
| 3      | 4 ⚠️            | 6 ✅             | Media |
| 4      | 6 ✅            | 7 ✅             | Alta  |
| 5      | 7 ✅            | 8 ✅             | Alta  |
| 6      | 6 ✅            | 7 ✅             | Alta  |
| 7      | 6 ✅            | 6 ✅             | Media |
| 8      | 5 ✅            | 6 ✅             | Media |
| 9      | 1 ❌            | 5 ✅             | Media |
| 10     | 2 ⚠️            | 4 ✅             | Baja  |

---

## 🎯 Recomendaciones Finales

### 1. **Priorización**
- ✅ Mantén las funcionalidades core en sprints tempranos
- ✅ Deja características avanzadas para sprints posteriores
- ✅ Reserva tiempo para testing e integración

### 2. **Gestión de Riesgos**
- ⚠️ El Sprint 5-6 son los más críticos (compra de entradas)
- ⚠️ Asegura buffer para bugs en Sprint 9-10
- ⚠️ Valida la integración de pagos temprano

### 3. **Cumplimiento del Enunciado**
- ✅ Java: Lenguaje principal
- ✅ XML: Exportación/importación de eventos, configuración
- ✅ JSON: APIs, confirmaciones, comunicación
- ✅ Persistencia: Hibernate/JPA con todas las entidades

### 4. **Distribución del Trabajo**
Para un equipo de 3 personas:
- **Persona 1:** Backend (persistencia, servicios, APIs)
- **Persona 2:** Frontend (interfaces de usuario)
- **Persona 3:** Integración (XML/JSON, QR, testing)

