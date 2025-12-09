# Arquitectura Técnica - Sistema de Gestión de Eventos

**Proyecto:** Sistema de Gestión de Empresa de Eventos  
**Versión:** 1.0  
**Fecha:** 4 de noviembre de 2025

---

## 📐 Arquitectura General del Sistema

### **Patrón Arquitectónico: Arquitectura en Capas (Layered Architecture)**

```
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE PRESENTACIÓN                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Swing/     │  │   JavaFX     │  │   Web UI     │      │
│  │   Console    │  │   (Opcional) │  │  (Opcional)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   CAPA DE CONTROLADORES                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Usuario    │  │   Evento     │  │   Entrada    │      │
│  │  Controller  │  │  Controller  │  │  Controller  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │    Sede      │  │   Informe    │  │   Export     │      │
│  │  Controller  │  │  Controller  │  │  Controller  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE SERVICIOS                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Usuario    │  │   Evento     │  │   Entrada    │      │
│  │   Service    │  │   Service    │  │   Service    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Autenticación│  │  Exportación │  │     QR       │      │
│  │   Service    │  │   Service    │  │   Service    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     CAPA DE PERSISTENCIA                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Usuario    │  │   Evento     │  │   Entrada    │      │
│  │  Repository  │  │  Repository  │  │  Repository  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │    Sede      │  │  Equipamiento│  │   Compra     │      │
│  │  Repository  │  │  Repository  │  │  Repository  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE DATOS (ORM)                       │
│                      Hibernate / JPA                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Entidades: Usuario, Rol, Evento, Entrada, Sede,   │    │
│  │  Equipamiento, Compra, TipoEntrada, etc.           │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   BASE DE DATOS RELACIONAL                   │
│              MySQL / PostgreSQL / H2 (Desarrollo)           │
└─────────────────────────────────────────────────────────────┘

         ┌──────────────────────────────────┐
         │   MÓDULOS TRANSVERSALES          │
         ├──────────────────────────────────┤
         │  • Generador XML (JAXB/DOM)      │
         │  • Parser JSON (Jackson/Gson)    │
         │  • Generador QR (ZXing)          │
         │  • Sistema de Logging (Log4j)    │
         │  • Validación (Bean Validation)  │
         │  • Seguridad (BCrypt)            │
         └──────────────────────────────────┘
```

---

## 🏗️ Estructura de Directorios del Proyecto

```
gestion-eventos/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── eventos/
│   │   │           ├── config/              # Configuración
│   │   │           │   ├── DatabaseConfig.java
│   │   │           │   ├── HibernateUtil.java
│   │   │           │   └── AppConfig.java
│   │   │           │
│   │   │           ├── models/              # Entidades JPA
│   │   │           │   ├── Usuario.java
│   │   │           │   ├── Rol.java
│   │   │           │   ├── Evento.java
│   │   │           │   ├── TipoEvento.java
│   │   │           │   ├── Sede.java
│   │   │           │   ├── Equipamiento.java
│   │   │           │   ├── TipoEntrada.java
│   │   │           │   ├── Entrada.java
│   │   │           │   ├── Compra.java
│   │   │           │   └── RegistroEntrada.java
│   │   │           │
│   │   │           ├── repositories/        # Capa de acceso a datos
│   │   │           │   ├── IRepository.java (Interfaz genérica)
│   │   │           │   ├── UsuarioRepository.java
│   │   │           │   ├── EventoRepository.java
│   │   │           │   ├── SedeRepository.java
│   │   │           │   ├── EntradaRepository.java
│   │   │           │   └── CompraRepository.java
│   │   │           │
│   │   │           ├── services/            # Lógica de negocio
│   │   │           │   ├── AutenticacionService.java
│   │   │           │   ├── UsuarioService.java
│   │   │           │   ├── EventoService.java
│   │   │           │   ├── SedeService.java
│   │   │           │   ├── EntradaService.java
│   │   │           │   ├── CompraService.java
│   │   │           │   ├── QRService.java
│   │   │           │   ├── ExportacionService.java
│   │   │           │   ├── ImportacionService.java
│   │   │           │   └── InformeService.java
│   │   │           │
│   │   │           ├── controllers/         # Controladores
│   │   │           │   ├── UsuarioController.java
│   │   │           │   ├── EventoController.java
│   │   │           │   ├── SedeController.java
│   │   │           │   ├── CompraController.java
│   │   │           │   └── AdminController.java
│   │   │           │
│   │   │           ├── views/               # Interfaz de usuario
│   │   │           │   ├── MainFrame.java
│   │   │           │   ├── LoginView.java
│   │   │           │   ├── RegistroView.java
│   │   │           │   ├── EventoView.java
│   │   │           │   ├── CompraView.java
│   │   │           │   └── AdminView.java
│   │   │           │
│   │   │           ├── dto/                 # Data Transfer Objects
│   │   │           │   ├── UsuarioDTO.java
│   │   │           │   ├── EventoDTO.java
│   │   │           │   ├── CompraDTO.java
│   │   │           │   └── InformeDTO.java
│   │   │           │
│   │   │           ├── utils/               # Utilidades
│   │   │           │   ├── XMLParser.java
│   │   │           │   ├── JSONParser.java
│   │   │           │   ├── QRGenerator.java
│   │   │           │   ├── PasswordUtil.java
│   │   │           │   ├── Validator.java
│   │   │           │   └── DateUtil.java
│   │   │           │
│   │   │           ├── exceptions/          # Excepciones personalizadas
│   │   │           │   ├── EventoException.java
│   │   │           │   ├── AutenticacionException.java
│   │   │           │   ├── CompraException.java
│   │   │           │   └── ValidationException.java
│   │   │           │
│   │   │           └── Main.java            # Punto de entrada
│   │   │
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── persistence.xml          # Configuración JPA
│   │       ├── hibernate.cfg.xml            # Configuración Hibernate
│   │       ├── log4j2.xml                   # Configuración logging
│   │       ├── schema/                      # Esquemas XSD
│   │       │   ├── eventos.xsd
│   │       │   ├── sedes.xsd
│   │       │   └── compras.xsd
│   │       └── sql/
│   │           ├── schema.sql               # Script creación BD
│   │           └── data.sql                 # Datos iniciales
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── eventos/
│                   ├── services/
│                   │   ├── AutenticacionServiceTest.java
│                   │   ├── EventoServiceTest.java
│                   │   └── CompraServiceTest.java
│                   ├── repositories/
│                   │   └── UsuarioRepositoryTest.java
│                   └── utils/
│                       ├── XMLParserTest.java
│                       └── QRGeneratorTest.java
│
├── exports/                                 # Archivos exportados
│   ├── xml/
│   └── json/
│
├── imports/                                 # Archivos para importar
│   ├── xml/
│   └── json/
│
├── qr-codes/                               # Códigos QR generados
│
├── docs/                                   # Documentación
│   ├── api/
│   ├── manual-usuario.pdf
│   └── diagrama-clases.png
│
├── pom.xml                                 # Maven dependencies
├── .gitignore
└── README.md
```

---

## 🛠️ Stack Tecnológico

### **Core**
- **Lenguaje:** Java 17+ (LTS)
- **Build Tool:** Maven 3.9+ o Gradle 8+
- **IDE Recomendado:** IntelliJ IDEA / Eclipse / VS Code

### **Persistencia**
- **ORM:** Hibernate 6.x
- **JPA:** 3.1
- **Base de Datos:**
  - **Desarrollo:** H2 Database (en memoria)
  - **Producción:** MySQL 8.0+ / PostgreSQL 15+
- **Pool de Conexiones:** HikariCP

### **Interfaz de Usuario**
- **Opción 1 (Recomendada):** Java Swing + MigLayout
- **Opción 2:** JavaFX 17+
- **Opción 3 (Avanzada):** Spring Boot + Thymeleaf (Web)

### **XML/JSON**
- **XML:**
  - JAXB (Jakarta XML Binding) - Marshalling/Unmarshalling
  - DOM Parser - Manipulación de documentos
  - XSD - Validación de esquemas
- **JSON:**
  - Jackson 2.15+ - Serialización/Deserialización
  - Gson 2.10+ (Alternativa)

### **Códigos QR**
- **Librería:** ZXing (Zebra Crossing) 3.5+
  ```xml
  <dependency>
      <groupId>com.google.zxing</groupId>
      <artifactId>core</artifactId>
      <version>3.5.2</version>
  </dependency>
  ```

### **Seguridad**
- **Hash de Contraseñas:** BCrypt (jBCrypt)
- **Tokens (opcional):** JWT (jjwt)

### **Testing**
- **Framework:** JUnit 5 (Jupiter)
- **Mocking:** Mockito 5.x
- **Cobertura:** JaCoCo

### **Logging**
- **Framework:** SLF4J + Log4j2

### **Utilidades**
- **Validación:** Hibernate Validator (Bean Validation)
- **Lombok:** Reducir boilerplate
- **Apache Commons:** Utilidades generales

---

## 📦 Configuración de Maven (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.eventos</groupId>
    <artifactId>gestion-eventos</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <hibernate.version>6.3.1.Final</hibernate.version>
        <jackson.version>2.15.3</jackson.version>
    </properties>

    <dependencies>
        <!-- Hibernate / JPA -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>${hibernate.version}</version>
        </dependency>

        <!-- Base de Datos -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.2.0</version>
        </dependency>

        <!-- HikariCP Connection Pool -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>5.1.0</version>
        </dependency>

        <!-- JAXB para XML -->
        <dependency>
            <groupId>jakarta.xml.bind</groupId>
            <artifactId>jakarta.xml.bind-api</artifactId>
            <version>4.0.1</version>
        </dependency>
        <dependency>
            <groupId>com.sun.xml.bind</groupId>
            <artifactId>jaxb-impl</artifactId>
            <version>4.0.4</version>
        </dependency>

        <!-- Jackson para JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>${jackson.version}</version>
        </dependency>

        <!-- Generación de códigos QR -->
        <dependency>
            <groupId>com.google.zxing</groupId>
            <artifactId>core</artifactId>
            <version>3.5.2</version>
        </dependency>
        <dependency>
            <groupId>com.google.zxing</groupId>
            <artifactId>javase</artifactId>
            <version>3.5.2</version>
        </dependency>

        <!-- BCrypt para hash de contraseñas -->
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>

        <!-- Validación -->
        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>8.0.1.Final</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.30</version>
            <scope>provided</scope>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>2.21.1</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.9</version>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>5.7.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.2</version>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## ⚙️ Configuración de Hibernate (persistence.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
             https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
             version="3.0">

    <persistence-unit name="EventosPU" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
        
        <!-- Entidades -->
        <class>com.eventos.models.Usuario</class>
        <class>com.eventos.models.Rol</class>
        <class>com.eventos.models.Evento</class>
        <class>com.eventos.models.TipoEvento</class>
        <class>com.eventos.models.Sede</class>
        <class>com.eventos.models.Equipamiento</class>
        <class>com.eventos.models.TipoEntrada</class>
        <class>com.eventos.models.Entrada</class>
        <class>com.eventos.models.Compra</class>
        <class>com.eventos.models.RegistroEntrada</class>

        <properties>
            <!-- Configuración de conexión -->
            <property name="jakarta.persistence.jdbc.driver" value="org.h2.Driver"/>
            <property name="jakarta.persistence.jdbc.url" value="jdbc:h2:./data/eventos;AUTO_SERVER=TRUE"/>
            <property name="jakarta.persistence.jdbc.user" value="sa"/>
            <property name="jakarta.persistence.jdbc.password" value=""/>

            <!-- Configuración de Hibernate -->
            <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
            <property name="hibernate.hbm2ddl.auto" value="update"/>
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
            
            <!-- Pool de conexiones -->
            <property name="hibernate.hikari.minimumIdle" value="5"/>
            <property name="hibernate.hikari.maximumPoolSize" value="10"/>
            <property name="hibernate.hikari.idleTimeout" value="300000"/>
        </properties>
    </persistence-unit>
</persistence>
```

---

## 🔐 Patrones de Diseño Utilizados

### 1. **Repository Pattern**
Abstrae el acceso a datos y facilita cambios en la capa de persistencia.

```java
public interface IRepository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void delete(T entity);
    void update(T entity);
}
```

### 2. **Service Layer Pattern**
Encapsula la lógica de negocio separándola de controladores y persistencia.

### 3. **DTO Pattern**
Transfiere datos entre capas sin exponer entidades JPA directamente.

### 4. **Singleton Pattern**
Para gestores de configuración y conexiones a BD.

### 5. **Factory Pattern**
Para creación de objetos complejos (eventos, entradas).

### 6. **Strategy Pattern**
Para diferentes tipos de exportación (XML, JSON).

---

## 🚀 Flujo de Datos Típico

```
[Usuario interactúa con Vista]
          ↓
[Vista llama a Controlador]
          ↓
[Controlador llama a Servicio]
          ↓
[Servicio ejecuta lógica de negocio]
          ↓
[Servicio usa Repository para persistencia]
          ↓
[Repository usa Hibernate/JPA]
          ↓
[Base de Datos]
```

---

**Siguiente documento:** Modelo de Datos y Entidades
