package com.eventos.config;

import com.eventos.models.Rol;
import com.eventos.models.Usuario;
import com.eventos.repositories.RolRepository;
import com.eventos.repositories.UsuarioRepository;
import com.eventos.utils.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Inicializa datos mínimos en la base de datos si no existen.
 * - Roles (ADMIN, USUARIO, EMPLEADO)
 * - Usuario administrador por defecto (admin@eventos.com / admin123)
 */
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final RolRepository rolRepository = new RolRepository();
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    /**
     * Ejecuta la inicialización segura. No falla si los datos ya existen.
     */
    public void initialize() {
        logger.info("Verificando datos iniciales de la base de datos...");
        ensureRoles();
        ensureDefaultAdmin();
    }

    private void ensureRoles() {
        createRoleIfMissing(
            "ADMIN",
            "Administrador del sistema con todos los permisos",
            List.of(
                "CREAR_EVENTO", "EDITAR_EVENTO", "ELIMINAR_EVENTO", "VER_INFORMES",
                "GESTIONAR_USUARIOS", "GESTIONAR_SEDES", "GESTIONAR_EQUIPAMIENTO",
                "EXPORTAR_DATOS", "IMPORTAR_DATOS"
            )
        );

        createRoleIfMissing(
            "USUARIO",
            "Usuario normal que puede comprar entradas",
            List.of("COMPRAR_ENTRADA", "VER_HISTORIAL", "VER_EVENTOS")
        );

        createRoleIfMissing(
            "EMPLEADO",
            "Empleado que valida entradas en eventos",
            List.of("VALIDAR_ENTRADA", "VER_EVENTOS", "REGISTRAR_ACCESO")
        );
    }

    private Rol createRoleIfMissing(String nombre, String descripcion, List<String> permisos) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol rol = new Rol(nombre, descripcion);
            rol.setPermisos(new ArrayList<>(permisos));
            Rol saved = rolRepository.save(rol);
            logger.info("Rol creado: {}", nombre);
            return saved;
        });
    }

    private void ensureDefaultAdmin() {
        final String adminEmail = "admin@eventos.com";
        if (usuarioRepository.findByEmail(adminEmail).isPresent()) {
            logger.info("Usuario administrador ya existe: {}", adminEmail);
            return;
        }

        Rol adminRol = rolRepository.findByNombre("ADMIN").orElse(null);
        if (adminRol == null) {
            logger.warn("No se pudo crear el usuario admin porque el rol ADMIN no está disponible");
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setApellidos("Sistema");
        admin.setEmail(adminEmail);
        admin.setPassword(PasswordUtil.hashPassword("admin123"));
        admin.setRol(adminRol);
        admin.setActivo(true);
        admin.setFechaAlta(LocalDateTime.now());
        admin.setDni("00000000T");
        usuarioRepository.save(admin);
        logger.info("Usuario administrador creado por defecto ({} / admin123)", adminEmail);
    }
}
