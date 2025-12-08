package com.eventos.services;

import com.eventos.exceptions.AutenticacionException;
import com.eventos.models.Usuario;
import com.eventos.repositories.UsuarioRepository;
import com.eventos.utils.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio de Autenticación
 * 
 * Responsabilidades:
 * - Gestionar el inicio de sesión (login) de usuarios
 * - Validar credenciales (email y contraseña)
 * - Mantener la sesión del usuario actual
 * - Gestionar el cierre de sesión (logout)
 * - Actualizar última conexión del usuario
 * 
 * Patrón Singleton: Solo existe una instancia de este servicio en toda la aplicación
 * para mantener consistencia en la sesión activa.
 */
public class AutenticacionService {
    
    private static final Logger logger = LoggerFactory.getLogger(AutenticacionService.class);
    
    // Instancia única del servicio (Patrón Singleton)
    private static AutenticacionService instance;
    
    // Repositorio para acceder a datos de usuarios
    private final UsuarioRepository usuarioRepository;
    
    // Usuario que tiene la sesión activa actualmente
    private Usuario usuarioActual;
    
    /**
     * Constructor privado (Patrón Singleton)
     * Solo se puede crear una instancia desde dentro de esta clase
     */
    private AutenticacionService() {
        this.usuarioRepository = new UsuarioRepository();
        this.usuarioActual = null;
    }
    
    /**
     * Obtiene la instancia única del servicio (Patrón Singleton)
     * Si no existe, la crea. Si ya existe, devuelve la existente.
     * 
     * @return Instancia única de AutenticacionService
     */
    public static AutenticacionService getInstance() {
        if (instance == null) {
            instance = new AutenticacionService();
        }
        return instance;
    }
    
    /**
     * Intenta iniciar sesión con las credenciales proporcionadas
     * 
     * Proceso:
     * 1. Valida que los campos no estén vacíos
     * 2. Busca el usuario por email en la base de datos
     * 3. Verifica que el usuario exista y esté activo
     * 4. Compara la contraseña ingresada con el hash almacenado (BCrypt)
     * 5. Si todo es correcto, establece el usuario como sesión actual
     * 6. Actualiza la fecha de última conexión
     * 
     * @param email Email del usuario
     * @param password Contraseña en texto plano
     * @return Usuario autenticado
     * @throws AutenticacionException Si las credenciales son inválidas o el usuario no existe/está inactivo
     */
    public Usuario login(String email, String password) throws AutenticacionException {
        logger.info("Intento de login para el usuario: {}", email);
        
        // Validación 1: Verificar que los campos no estén vacíos
        if (email == null || email.trim().isEmpty()) {
            logger.warn("Intento de login con email vacío");
            throw new AutenticacionException("El email es obligatorio");
        }
        
        if (password == null || password.trim().isEmpty()) {
            logger.warn("Intento de login con contraseña vacía");
            throw new AutenticacionException("La contraseña es obligatoria");
        }
        
        // Validación 2: Buscar usuario en la base de datos
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email.trim().toLowerCase());
        
        if (usuarioOpt.isEmpty()) {
            logger.warn("Login fallido: Usuario no encontrado - {}", email);
            throw new AutenticacionException("Email o contraseña incorrectos");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Validación 3: Verificar que el usuario esté activo
        if (!usuario.getActivo()) {
            logger.warn("Login fallido: Usuario inactivo - {}", email);
            throw new AutenticacionException("Usuario desactivado. Contacte con el administrador");
        }
        
        // Validación 4: Verificar contraseña usando BCrypt
        // BCrypt compara el hash almacenado con la contraseña en texto plano
        if (!PasswordUtil.checkPassword(password, usuario.getPassword())) {
            logger.warn("Login fallido: Contraseña incorrecta - {}", email);
            throw new AutenticacionException("Email o contraseña incorrectos");
        }
        
        // Login exitoso: Establecer sesión actual
        this.usuarioActual = usuario;
        
        // Actualizar última conexión en la base de datos
        usuario.setUltimaConexion(LocalDateTime.now());
        usuarioRepository.update(usuario);
        
        logger.info("Login exitoso para el usuario: {} - Rol: {}", 
                   usuario.getEmail(), usuario.getRol().getNombre());
        
        return usuario;
    }
    
    /**
     * Cierra la sesión del usuario actual
     * Elimina la referencia al usuario logueado
     */
    public void logout() {
        if (usuarioActual != null) {
            logger.info("Logout del usuario: {}", usuarioActual.getEmail());
            this.usuarioActual = null;
        }
    }
    
    /**
     * Obtiene el usuario que tiene la sesión activa
     * 
     * @return Usuario actual o null si no hay sesión activa
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
    
    /**
     * Verifica si hay un usuario con sesión activa
     * 
     * @return true si hay sesión activa, false en caso contrario
     */
    public boolean haySesionActiva() {
        return usuarioActual != null;
    }
    
    /**
     * Verifica si el usuario actual tiene un rol específico
     * Útil para controlar acceso a funcionalidades según el rol
     * 
     * @param nombreRol Nombre del rol a verificar (ej: "ADMINISTRADOR", "USUARIO")
     * @return true si el usuario tiene ese rol, false en caso contrario
     */
    public boolean tieneRol(String nombreRol) {
        if (usuarioActual == null || usuarioActual.getRol() == null) {
            return false;
        }
        return usuarioActual.getRol().getNombre().equalsIgnoreCase(nombreRol);
    }
    
    /**
     * Verifica si el usuario actual es administrador
     * 
     * @return true si es administrador, false en caso contrario
     */
    public boolean esAdministrador() {
        return tieneRol("ADMINISTRADOR") || tieneRol("ADMIN");
    }
    
    /**
     * Requiere que haya una sesión activa
     * Si no hay sesión, lanza una excepción
     * 
     * @throws AutenticacionException Si no hay sesión activa
     */
    public void requireSesionActiva() throws AutenticacionException {
        if (!haySesionActiva()) {
            throw new AutenticacionException("Debe iniciar sesión para acceder a esta funcionalidad");
        }
    }
    
    /**
     * Requiere que el usuario actual sea administrador
     * Si no hay sesión o no es admin, lanza una excepción
     * 
     * @throws AutenticacionException Si no es administrador
     */
    public void requireAdministrador() throws AutenticacionException {
        requireSesionActiva();
        if (!esAdministrador()) {
            throw new AutenticacionException("No tiene permisos de administrador para realizar esta acción");
        }
    }
}
