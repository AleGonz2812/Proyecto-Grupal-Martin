package com.eventos.controllers;

import com.eventos.models.Usuario;
import com.eventos.services.AutenticacionService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Controlador del Dashboard Principal
 * 
 * Responsabilidades:
 * - Mostrar información del usuario logueado
 * - Gestionar el menú de navegación
 * - Cargar vistas dinámicamente en el área central
 * - Mostrar/ocultar opciones según el rol del usuario
 * - Gestionar el cierre de sesión
 * - Mostrar estadísticas básicas (eventos, entradas, etc.)
 * 
 * Este es el "hub" central desde donde se accede a todas las funcionalidades
 */
public class DashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    // Componentes de la interfaz
    @FXML private Label welcomeLabel;           // Etiqueta de bienvenida
    @FXML private Label roleLabel;              // Etiqueta del rol
    @FXML private Button logoutButton;          // Botón cerrar sesión
    @FXML private StackPane contentArea;        // Área donde se cargan las vistas
    @FXML private VBox adminMenuContainer;      // Contenedor del menú de admin
    @FXML private VBox adminStatsCard;          // Card de estadísticas de admin
    @FXML private Text welcomeMessageText;      // Mensaje de bienvenida
    @FXML private Text eventosCountText;        // Contador de eventos
    @FXML private Text entradasCountText;       // Contador de entradas
    @FXML private Text usuariosCountText;       // Contador de usuarios
    
    // Servicios
    private final AutenticacionService authService;
    private Usuario usuarioActual;
    
    /**
     * Constructor
     */
    public DashboardController() {
        this.authService = AutenticacionService.getInstance();
    }
    
    /**
     * Método de inicialización de JavaFX
     * Se ejecuta automáticamente después de cargar el FXML
     */
    @FXML
    private void initialize() {
        logger.info("Inicializando dashboard");
        
        // Por defecto, ocultar opciones de administrador
        if (adminMenuContainer != null) {
            adminMenuContainer.setVisible(false);
            adminMenuContainer.setManaged(false);
        }
        
        if (adminStatsCard != null) {
            adminStatsCard.setVisible(false);
            adminStatsCard.setManaged(false);
        }
    }
    
    /**
     * Inicializa el dashboard con los datos del usuario autenticado
     * Este método debe ser llamado después de un login exitoso
     * 
     * @param usuario Usuario que acaba de iniciar sesión
     */
    public void inicializarConUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        
        logger.info("Inicializando dashboard para usuario: {} - Rol: {}", 
                   usuario.getEmail(), usuario.getRol().getNombre());
        
        // Actualizar información del usuario en la interfaz
        actualizarInfoUsuario();
        
        // Configurar menú según el rol
        configurarMenuSegunRol();
        
        // Cargar estadísticas iniciales
        cargarEstadisticas();
        
        // Mensaje de bienvenida personalizado
        Platform.runLater(() -> {
            String saludo = obtenerSaludo();
            welcomeMessageText.setText(String.format("%s, %s!", 
                saludo, usuario.getNombre()));
        });
    }
    
    /**
     * Actualiza la información del usuario en la barra superior
     */
    private void actualizarInfoUsuario() {
        if (usuarioActual != null) {
            // Mostrar nombre completo o solo nombre si no tiene apellidos
            String nombreCompleto = usuarioActual.getNombre();
            if (usuarioActual.getApellidos() != null && !usuarioActual.getApellidos().isEmpty()) {
                nombreCompleto += " " + usuarioActual.getApellidos();
            }
            
            welcomeLabel.setText("Bienvenido/a, " + nombreCompleto);
            roleLabel.setText("(" + usuarioActual.getRol().getNombre() + ")");
        }
    }
    
    /**
     * Configura la visibilidad del menú según el rol del usuario
     * Los administradores ven opciones adicionales
     */
    private void configurarMenuSegunRol() {
        if (usuarioActual != null && authService.esAdministrador()) {
            logger.info("Usuario es administrador, mostrando opciones de administración");
            
            if (adminMenuContainer != null) {
                adminMenuContainer.setVisible(true);
                adminMenuContainer.setManaged(true);
            }
            
            if (adminStatsCard != null) {
                adminStatsCard.setVisible(true);
                adminStatsCard.setManaged(true);
            }
        }
    }
    
    /**
     * Carga las estadísticas básicas del dashboard
     * TODO: Implementar consultas reales a la base de datos
     */
    private void cargarEstadisticas() {
        // TODO: Implementar cuando tengamos los servicios correspondientes
        // Por ahora, valores de ejemplo
        
        Platform.runLater(() -> {
            eventosCountText.setText("0");
            entradasCountText.setText("0");
            
            if (authService.esAdministrador()) {
                usuariosCountText.setText("0");
            }
        });
        
        logger.info("Estadísticas cargadas (placeholder)");
    }
    
    /**
     * Obtiene un saludo según la hora del día
     * @return Saludo apropiado (Buenos días, Buenas tardes, Buenas noches)
     */
    private String obtenerSaludo() {
        int hora = java.time.LocalTime.now().getHour();
        
        if (hora >= 6 && hora < 12) {
            return "Buenos días";
        } else if (hora >= 12 && hora < 20) {
            return "Buenas tardes";
        } else {
            return "Buenas noches";
        }
    }
    
    // ==================== MANEJADORES DE MENÚ ====================
    
    /**
     * Maneja el clic en "Inicio"
     * Muestra el dashboard principal (ya estamos aquí)
     */
    @FXML
    private void handleInicio() {
        logger.info("Navegando a: Inicio");
        // Ya estamos en inicio, solo refrescar estadísticas
        cargarEstadisticas();
    }
    
    /**
     * Maneja el clic en "Eventos"
     * TODO: Cargar vista de catálogo de eventos
     */
    @FXML
    private void handleEventos() {
        logger.info("Navegando a: Eventos");
        mostrarMensajeEnDesarrollo("Catálogo de Eventos");
        // TODO: cargarVista("/fxml/eventos/lista-eventos.fxml");
    }
    
    /**
     * Maneja el clic en "Mis Entradas"
     * TODO: Cargar vista de entradas del usuario
     */
    @FXML
    private void handleMisEntradas() {
        logger.info("Navegando a: Mis Entradas");
        mostrarMensajeEnDesarrollo("Mis Entradas");
        // TODO: cargarVista("/fxml/entradas/mis-entradas.fxml");
    }
    
    /**
     * Maneja el clic en "Comprar Entradas"
     * TODO: Cargar vista de compra de entradas
     */
    @FXML
    private void handleComprar() {
        logger.info("Navegando a: Comprar");
        mostrarMensajeEnDesarrollo("Proceso de Compra");
        // TODO: cargarVista("/fxml/compras/comprar.fxml");
    }
    
    /**
     * Maneja el clic en "Gestión de Usuarios" (Solo Admin)
     * TODO: Cargar vista de gestión de usuarios
     */
    @FXML
    private void handleGestionUsuarios() {
        logger.info("Navegando a: Gestión de Usuarios");
        mostrarMensajeEnDesarrollo("Gestión de Usuarios");
        // TODO: cargarVista("/fxml/admin/gestion-usuarios.fxml");
    }
    
    /**
     * Maneja el clic en "Gestión de Eventos" (Solo Admin)
     * TODO: Cargar vista de gestión de eventos
     */
    @FXML
    private void handleGestionEventos() {
        logger.info("Navegando a: Gestión de Eventos");
        mostrarMensajeEnDesarrollo("Gestión de Eventos");
        // TODO: cargarVista("/fxml/admin/gestion-eventos.fxml");
    }
    
    /**
     * Maneja el clic en "Gestión de Sedes" (Solo Admin)
     * TODO: Cargar vista de gestión de sedes
     */
    @FXML
    private void handleGestionSedes() {
        logger.info("Navegando a: Gestión de Sedes");
        mostrarMensajeEnDesarrollo("Gestión de Sedes");
        // TODO: cargarVista("/fxml/admin/gestion-sedes.fxml");
    }
    
    /**
     * Maneja el clic en "Reportes" (Solo Admin)
     * TODO: Cargar vista de reportes y exportación
     */
    @FXML
    private void handleReportes() {
        logger.info("Navegando a: Reportes");
        mostrarMensajeEnDesarrollo("Reportes y Exportación");
        // TODO: cargarVista("/fxml/admin/reportes.fxml");
    }
    
    /**
     * Maneja el cierre de sesión
     * Confirma con el usuario y regresa a la pantalla de login
     */
    @FXML
    private void handleLogout() {
        logger.info("Usuario solicita cerrar sesión");
        
        // Confirmación antes de cerrar sesión
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar Sesión");
        alert.setHeaderText("¿Está seguro que desea cerrar sesión?");
        alert.setContentText("Tendrá que volver a iniciar sesión para acceder al sistema.");
        
        Optional<ButtonType> resultado = alert.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // Cerrar sesión en el servicio
            authService.logout();
            logger.info("Sesión cerrada correctamente");
            
            // Volver a la pantalla de login
            volverALogin();
        }
    }
    
    /**
     * Vuelve a la pantalla de login
     * Cierra la ventana actual y abre la de login
     */
    private void volverALogin() {
        try {
            logger.info("Volviendo a pantalla de login");
            
            // Cargar el FXML de login
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml")
            );
            Parent root = loader.load();
            
            // Crear nueva escena
            Scene scene = new Scene(root, 400, 600);
            
            // Cargar CSS
            try {
                String css = getClass().getResource("/css/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                logger.warn("No se pudieron cargar los estilos CSS", e);
            }
            
            // Obtener el Stage actual y cambiar la escena
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Sistema de Gestión de Eventos - Login");
            stage.centerOnScreen();
            
        } catch (IOException e) {
            logger.error("Error al cargar pantalla de login", e);
            mostrarError("Error al cerrar sesión", "No se pudo volver a la pantalla de login");
        }
    }
    
    /**
     * Carga una vista FXML en el área central del dashboard
     * Útil para navegación entre pantallas sin cerrar el dashboard
     * 
     * @param rutaFxml Ruta del archivo FXML a cargar
     */
    private void cargarVista(String rutaFxml) {
        try {
            logger.info("Cargando vista: {}", rutaFxml);
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Parent vista = loader.load();
            
            // Reemplazar contenido del área central
            contentArea.getChildren().clear();
            contentArea.getChildren().add(vista);
            
        } catch (IOException e) {
            logger.error("Error al cargar vista: {}", rutaFxml, e);
            mostrarError("Error", "No se pudo cargar la vista solicitada");
        }
    }
    
    /**
     * Muestra un mensaje temporal de "En desarrollo"
     * Mientras se implementan las demás vistas
     * 
     * @param funcionalidad Nombre de la funcionalidad
     */
    private void mostrarMensajeEnDesarrollo(String funcionalidad) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("En Desarrollo");
        alert.setHeaderText(funcionalidad);
        alert.setContentText("Esta funcionalidad está en desarrollo.\n\n" +
                           "Próximamente estará disponible.");
        alert.showAndWait();
    }
    
    /**
     * Muestra un mensaje de error al usuario
     * 
     * @param titulo Título del error
     * @param mensaje Mensaje descriptivo del error
     */
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
