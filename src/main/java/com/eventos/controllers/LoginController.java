package com.eventos.controllers;

import com.eventos.exceptions.AutenticacionException;
import com.eventos.models.Usuario;
import com.eventos.services.AutenticacionService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controlador de la vista de Login
 * 
 * Responsabilidades:
 * - Gestionar la interfaz de inicio de sesión
 * - Validar los campos del formulario
 * - Comunicarse con AutenticacionService para validar credenciales
 * - Mostrar mensajes de error al usuario
 * - Redirigir al dashboard principal tras login exitoso
 * - Gestionar el enlace de registro (futuro)
 * 
 * Patrón MVC:
 * - View: login.fxml
 * - Controller: LoginController.java (esta clase)
 * - Model/Service: AutenticacionService.java
 */
public class LoginController {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    // Inyección de componentes del FXML (anotación @FXML)
    // fx:id en el FXML debe coincidir con el nombre de la variable aquí
    
    @FXML
    private TextField emailField;           // Campo de texto para el email
    
    @FXML
    private PasswordField passwordField;    // Campo de contraseña (oculta caracteres)
    
    @FXML
    private TextField passwordVisibleField; // Campo de texto visible (para mostrar contraseña)
    
    @FXML
    private Button togglePasswordButton;    // Botón para mostrar/ocultar contraseña
    
    @FXML
    private Label errorLabel;               // Etiqueta para mostrar errores
    
    @FXML
    private Button loginButton;             // Botón de iniciar sesión
    
    @FXML
    private Hyperlink registerLink;         // Enlace de registro
    
    // Servicio de autenticación (Singleton)
    private final AutenticacionService authService;
    
    /**
     * Constructor del controlador
     * Se ejecuta cuando se crea una instancia de este controlador
     */
    public LoginController() {
        this.authService = AutenticacionService.getInstance();
    }
    
    /**
     * Método de inicialización de JavaFX
     * Se ejecuta automáticamente después de cargar el FXML
     * Útil para configurar componentes o cargar datos iniciales
     */
    @FXML
    private void initialize() {
        logger.info("Inicializando pantalla de login");
        
        // Ocultar mensaje de error al inicio
        errorLabel.setVisible(false);
        
        // Configurar que el botón de login esté deshabilitado si los campos están vacíos
        loginButton.setDisable(true);
        
        // Listener: habilitar botón solo si ambos campos tienen texto
        emailField.textProperty().addListener((observable, oldValue, newValue) -> 
            validarCamposParaHabilitarBoton()
        );
        
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validarCamposParaHabilitarBoton();
            // Sincronizar con el campo visible
            passwordVisibleField.setText(newValue);
        });
        
        // Sincronizar campo visible con el oculto
        passwordVisibleField.textProperty().addListener((observable, oldValue, newValue) -> {
            passwordField.setText(newValue);
        });
        
        // Focus automático en el campo de email
        Platform.runLater(() -> emailField.requestFocus());
    }
    
    /**
     * Maneja el evento del enlace "Regístrate aquí"
     * Navega a la pantalla de registro
     */
    @FXML
    private void handleRegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/registro.fxml")
            );
            Parent registroRoot = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(registroRoot);
            stage.setScene(scene);
            stage.setTitle("Sistema de Gestión de Eventos - Registro");
            stage.show();

        } catch (IOException e) {
            mostrarError("Error al cargar la pantalla de registro: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Valida que ambos campos tengan texto para habilitar el botón de login
     * Esto mejora la experiencia de usuario (UX)
     */
    private void validarCamposParaHabilitarBoton() {
        boolean camposLlenos = !emailField.getText().trim().isEmpty() 
                            && !passwordField.getText().trim().isEmpty();
        loginButton.setDisable(!camposLlenos);
    }
    
    /**
     * Alterna entre mostrar y ocultar la contraseña
     * Cambia entre PasswordField (oculta) y TextField (visible)
     */
    @FXML
    private void togglePasswordVisibility() {
        if (passwordField.isVisible()) {
            // Mostrar contraseña
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            togglePasswordButton.setText("🙈"); // Icono de "ocultar"
        } else {
            // Ocultar contraseña
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            togglePasswordButton.setText("👁"); // Icono de "ver"
        }
    }
    
    /**
     * Maneja el evento de clic en el botón "Iniciar Sesión"
     * También se ejecuta al presionar Enter en el campo de contraseña
     * 
     * Proceso:
     * 1. Obtener valores de los campos
     * 2. Validar campos en el cliente (antes de ir al servidor)
     * 3. Llamar al servicio de autenticación
     * 4. Si es exitoso, cargar el dashboard
     * 5. Si falla, mostrar mensaje de error
     */
    @FXML
    private void handleLogin() {
        logger.info("Intentando iniciar sesión...");
        
        // Ocultar mensajes de error previos
        ocultarError();
        
        // Obtener valores de los campos
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // Validación básica en el cliente
        if (email.isEmpty()) {
            mostrarError("Por favor, ingrese su email");
            return;
        }
        
        if (password.isEmpty()) {
            mostrarError("Por favor, ingrese su contraseña");
            return;
        }
        
        // Validar formato de email (validación básica)
        if (!email.contains("@") || !email.contains(".")) {
            mostrarError("Por favor, ingrese un email válido");
            return;
        }
        
        // Deshabilitar botón mientras se procesa (evitar doble clic)
        loginButton.setDisable(true);
        loginButton.setText("Iniciando sesión...");
        
        try {
            // Llamar al servicio de autenticación
            Usuario usuario = authService.login(email, password);
            
            logger.info("Login exitoso para: {} - Rol: {}", 
                       usuario.getEmail(), usuario.getRol().getNombre());
            
            // Redirigir al dashboard principal
            cargarDashboard(usuario);
            
        } catch (AutenticacionException e) {
            // Error de autenticación: mostrar mensaje al usuario
            logger.warn("Error de autenticación: {}", e.getMessage());
            mostrarError(e.getMessage());
            
            // Limpiar campo de contraseña por seguridad
            passwordField.clear();
            passwordField.requestFocus();
            
        } catch (Exception e) {
            // Error inesperado del sistema
            logger.error("Error inesperado durante el login", e);
            mostrarError("Error del sistema. Por favor, inténtelo más tarde.");
            
        } finally {
            // Rehabilitar botón
            loginButton.setDisable(false);
            loginButton.setText("Iniciar Sesión");
        }
    }
    
    /**
     * Carga la vista de eventos según el rol del usuario tras un login exitoso
     * Admin: Vista de gestión de eventos
     * Usuario: Vista de búsqueda y compra de eventos
     * 
     * @param usuario Usuario autenticado
     */
    private void cargarDashboard(Usuario usuario) {
        try {
            logger.info("Cargando vista de eventos para rol: {}", usuario.getRol().getNombre());
            
            // Determinar qué vista cargar según el rol
            String vistaFXML;
            String titulo;
            
            if (authService.esAdministrador()) {
                vistaFXML = "/fxml/eventos_admin.fxml";
                titulo = "Gestión de Eventos - Administrador";
            } else {
                vistaFXML = "/fxml/eventos_usuario.fxml";
                titulo = "Sistema de Gestión de Eventos";
            }
            
            // Cargar el archivo FXML correspondiente
            FXMLLoader loader = new FXMLLoader(getClass().getResource(vistaFXML));
            Parent root = loader.load();
            
            // Crear nueva escena
            Scene scene = new Scene(root);
            
            // Cargar CSS
            try {
                String css = getClass().getResource("/css/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                logger.warn("No se pudieron cargar los estilos CSS", e);
            }
            
            // Obtener el Stage actual (ventana) y cambiar la escena
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

            double targetWidth = Math.min(1280, bounds.getWidth() * 0.95);
            double targetHeight = Math.min(900, bounds.getHeight() * 0.9);

            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.setMinWidth(900);
            stage.setMinHeight(650);
            stage.setWidth(targetWidth);
            stage.setHeight(targetHeight);
            stage.setResizable(true);
            stage.centerOnScreen();
            
            logger.info("Vista de eventos cargada correctamente: {}", vistaFXML);
            
        } catch (IOException e) {
            logger.error("Error al cargar la vista de eventos", e);
            mostrarError("Error al cargar la pantalla principal");
        }
    }
    
    /**
     * Maneja el clic en el enlace "Regístrate aquí"
     * TODO: Implementar pantalla de registro
     */
    @FXML
    private void handleRegister() {
        logger.info("Usuario solicitó pantalla de registro");
        
        // Por ahora, mostrar un mensaje informativo
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registro");
        alert.setHeaderText("Funcionalidad en desarrollo");
        alert.setContentText("La pantalla de registro estará disponible próximamente.\n\n" +
                           "Por favor, contacte con el administrador para crear una cuenta.");
        alert.showAndWait();
        
        // TODO: Implementar
        // cargarPantallaRegistro();
    }
    
    /**
     * Muestra un mensaje de error en la interfaz
     * 
     * @param mensaje Mensaje de error a mostrar
     */
    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
        
        // Efecto visual: hacer parpadear el mensaje de error
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }
    
    /**
     * Oculta el mensaje de error
     */
    private void ocultarError() {
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }
    
    /**
     * Limpia todos los campos del formulario
     * Útil si se quiere resetear el formulario
     */
    @SuppressWarnings("unused")
    private void limpiarFormulario() {
        emailField.clear();
        passwordField.clear();
        ocultarError();
    }
}
