package com.eventos.controllers;

import com.eventos.exceptions.ValidationException;
import com.eventos.models.Rol;
import com.eventos.models.Usuario;
import com.eventos.repositories.RolRepository;
import com.eventos.repositories.UsuarioRepository;
import com.eventos.services.AutenticacionService;
import com.eventos.utils.PasswordUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Controlador para la pantalla de registro de nuevos usuarios.
 * Gestiona la validación de datos, creación de cuenta y navegación.
 * 
 * @author Sistema de Gestión de Eventos
 * @version 1.0
 */
public class RegistroController {

    // ========== CAMPOS FXML ==========
    @FXML
    private TextField nombreField;              // Campo de nombre completo
    
    @FXML
    private TextField emailField;               // Campo de email
    
    @FXML
    private TextField telefonoField;            // Campo de teléfono
    
    @FXML
    private TextField dniField;                 // Campo de DNI/NIE
    
    @FXML
    private PasswordField passwordField;        // Campo de contraseña (oculta)
    
    @FXML
    private TextField passwordVisibleField;     // Campo de contraseña (visible)
    
    @FXML
    private Button togglePasswordButton;        // Botón mostrar/ocultar contraseña
    
    @FXML
    private PasswordField confirmarPasswordField;       // Campo confirmar contraseña (oculta)
    
    @FXML
    private TextField confirmarPasswordVisibleField;    // Campo confirmar contraseña (visible)
    
    @FXML
    private Button toggleConfirmarPasswordButton;       // Botón mostrar/ocultar confirmar
    
    @FXML
    private Label errorLabel;                   // Etiqueta para mostrar errores
    
    @FXML
    private Button registrarButton;             // Botón de registro
    
    @FXML
    private CheckBox esAdminCheckBox;           // Checkbox para registrar como admin

    // ========== REPOSITORIOS ==========
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final AutenticacionService autenticacionService;

    // ========== EXPRESIONES REGULARES ==========
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern TELEFONO_PATTERN = Pattern.compile(
        "^[+]?[0-9]{9,15}$"
    );
    
    private static final Pattern DNI_PATTERN = Pattern.compile(
        "^[0-9]{8}[A-Z]$"
    );

    /**
     * Constructor del controlador.
     * Inicializa los repositorios necesarios.
     */
    public RegistroController() {
        this.usuarioRepository = new UsuarioRepository();
        this.rolRepository = new RolRepository();
        this.autenticacionService = AutenticacionService.getInstance();
    }

    /**
     * Método de inicialización automático de JavaFX.
     * Se ejecuta después de cargar el FXML.
     */
    @FXML
    private void initialize() {
        // Configurar validación en tiempo real
        nombreField.textProperty().addListener((observable, oldValue, newValue) -> {
            validarCamposParaHabilitarBoton();
        });
        
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            validarCamposParaHabilitarBoton();
        });
        
        telefonoField.textProperty().addListener((observable, oldValue, newValue) -> {
            validarCamposParaHabilitarBoton();
        });
        
        dniField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Convertir a mayúsculas automáticamente
            if (!newValue.equals(newValue.toUpperCase())) {
                dniField.setText(newValue.toUpperCase());
            }
            validarCamposParaHabilitarBoton();
        });
        
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validarCamposParaHabilitarBoton();
            // Sincronizar con campo visible
            passwordVisibleField.setText(newValue);
        });
        
        passwordVisibleField.textProperty().addListener((observable, oldValue, newValue) -> {
            passwordField.setText(newValue);
        });
        
        confirmarPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validarCamposParaHabilitarBoton();
            // Sincronizar con campo visible
            confirmarPasswordVisibleField.setText(newValue);
        });
        
        confirmarPasswordVisibleField.textProperty().addListener((observable, oldValue, newValue) -> {
            confirmarPasswordField.setText(newValue);
        });
        
        // Deshabilitar botón inicialmente
        registrarButton.setDisable(true);
        
        // Focus automático en el campo de nombre
        Platform.runLater(() -> nombreField.requestFocus());
    }

    /**
     * Valida que todos los campos estén completos para habilitar el botón de registro.
     */
    private void validarCamposParaHabilitarBoton() {
        boolean todosCompletos = !nombreField.getText().trim().isEmpty() &&
                                 !emailField.getText().trim().isEmpty() &&
                                 !telefonoField.getText().trim().isEmpty() &&
                                 !dniField.getText().trim().isEmpty() &&
                                 !passwordField.getText().isEmpty() &&
                                 !confirmarPasswordField.getText().isEmpty();
        
        registrarButton.setDisable(!todosCompletos);
        
        // Limpiar error cuando el usuario empiece a escribir
        if (!errorLabel.getText().isEmpty()) {
            errorLabel.setText("");
        }
    }

    /**
     * Alterna la visibilidad de la contraseña principal.
     */
    @FXML
    private void togglePasswordVisibility() {
        if (passwordField.isVisible()) {
            // Mostrar contraseña
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            togglePasswordButton.setText("🙈");
        } else {
            // Ocultar contraseña
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            togglePasswordButton.setText("👁");
        }
    }

    /**
     * Alterna la visibilidad del campo de confirmar contraseña.
     */
    @FXML
    private void toggleConfirmarPasswordVisibility() {
        if (confirmarPasswordField.isVisible()) {
            // Mostrar contraseña
            confirmarPasswordField.setVisible(false);
            confirmarPasswordField.setManaged(false);
            confirmarPasswordVisibleField.setVisible(true);
            confirmarPasswordVisibleField.setManaged(true);
            toggleConfirmarPasswordButton.setText("🙈");
        } else {
            // Ocultar contraseña
            confirmarPasswordField.setVisible(true);
            confirmarPasswordField.setManaged(true);
            confirmarPasswordVisibleField.setVisible(false);
            confirmarPasswordVisibleField.setManaged(false);
            toggleConfirmarPasswordButton.setText("👁");
        }
    }

    /**
     * Maneja el evento de registro de un nuevo usuario.
     * Valida los datos, crea la cuenta y redirige al dashboard.
     */
    @FXML
    private void handleRegistro() {
        try {
            // Obtener y limpiar valores
            String nombre = nombreField.getText().trim();
            String email = emailField.getText().trim().toLowerCase();
            String telefono = telefonoField.getText().trim().replaceAll("\\s+", "");
            String dni = dniField.getText().trim().toUpperCase();
            String password = passwordField.getText();
            String confirmarPassword = confirmarPasswordField.getText();

            // Validaciones
            validarDatosRegistro(nombre, email, telefono, dni, password, confirmarPassword);

            // Verificar si el email ya existe
            if (usuarioRepository.findByEmail(email).isPresent()) {
                mostrarError("El correo electrónico ya está registrado");
                return;
            }

            // Validar que el DNI sea único
            if (usuarioRepository.existsByDni(dni)) {
                mostrarError("El DNI ya está registrado");
                return;
            }

            // Determinar el rol según el checkbox
            boolean esAdmin = esAdminCheckBox != null && esAdminCheckBox.isSelected();
            String rolNombre = esAdmin ? "ADMIN" : "USUARIO";

            Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new ValidationException("No se encontró el rol " + rolNombre + ". Ejecuta la app una vez para inicializar datos."));

            // Crear nuevo usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setTelefono(telefono);
            nuevoUsuario.setPassword(PasswordUtil.hashPassword(password));
            nuevoUsuario.setRol(rol);
            nuevoUsuario.setActivo(true);
            nuevoUsuario.setFechaAlta(LocalDateTime.now());
            nuevoUsuario.setDni(dni);

            // Guardar en base de datos
            usuarioRepository.save(nuevoUsuario);

            // Mostrar mensaje de éxito y redirigir al login
            mostrarExito("Cuenta creada exitosamente. Por favor, inicia sesión.");
            
            // Esperar un momento antes de redirigir
            Platform.runLater(() -> {
                try {
                    Thread.sleep(1500);
                    handleVolverLogin();
                } catch (InterruptedException e) {
                    handleVolverLogin();
                }
            });

        } catch (ValidationException e) {
            mostrarError(e.getMessage());
        } catch (Exception e) {
            mostrarError("Error al crear la cuenta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Valida todos los datos del formulario de registro.
     */
    private void validarDatosRegistro(String nombre, String email, String telefono, 
                                      String dni, String password, String confirmarPassword) 
            throws ValidationException {
        
        // Validar nombre
        if (nombre.length() < 3) {
            throw new ValidationException("El nombre debe tener al menos 3 caracteres");
        }

        // Validar email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("El formato del correo electrónico no es válido");
        }

        // Validar teléfono
        if (!TELEFONO_PATTERN.matcher(telefono).matches()) {
            throw new ValidationException("El formato del teléfono no es válido (9-15 dígitos)");
        }

        // Validar DNI
        if (!DNI_PATTERN.matcher(dni).matches()) {
            throw new ValidationException("El formato del DNI no es válido (8 números + letra)");
        }
        
        if (!validarLetraDNI(dni)) {
            throw new ValidationException("La letra del DNI no es correcta");
        }

        // Validar contraseña
        if (password.length() < 6) {
            throw new ValidationException("La contraseña debe tener al menos 6 caracteres");
        }

        // Validar que las contraseñas coincidan
        if (!password.equals(confirmarPassword)) {
            throw new ValidationException("Las contraseñas no coinciden");
        }
    }

    /**
     * Valida la letra del DNI español según el algoritmo oficial.
     */
    private boolean validarLetraDNI(String dni) {
        String letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        String numeros = dni.substring(0, 8);
        char letra = dni.charAt(8);
        
        try {
            int numeroDNI = Integer.parseInt(numeros);
            char letraCorrecta = letras.charAt(numeroDNI % 23);
            return letra == letraCorrecta;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Carga la vista de eventos después del registro exitoso.
     * Los usuarios nuevos siempre tendrán rol de Usuario (no Admin).
     */
    private void cargarDashboard() {
        try {
            // Los nuevos usuarios siempre son rol Usuario, cargar vista de búsqueda
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/eventos_usuario.fxml")
            );
            Parent eventosRoot = loader.load();

            // Cambiar la escena
            Stage stage = (Stage) registrarButton.getScene().getWindow();
            Scene scene = new Scene(eventosRoot);
            stage.setScene(scene);
            stage.setTitle("Sistema de Gestión de Eventos");
            stage.setWidth(1280);
            stage.setHeight(800);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            mostrarError("Error al cargar la pantalla principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Maneja el evento de cancelar el registro.
     * Vuelve a la pantalla de login.
     */
    @FXML
    private void handleCancelar() {
        handleVolverLogin();
    }

    /**
     * Maneja el evento de volver al login.
     * Redirige a la pantalla de inicio de sesión.
     */
    @FXML
    private void handleVolverLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml")
            );
            Parent loginRoot = loader.load();

            Stage stage = (Stage) registrarButton.getScene().getWindow();
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.setTitle("Sistema de Gestión de Eventos - Login");
            stage.show();

        } catch (IOException e) {
            mostrarError("Error al cargar el login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra un mensaje de error en la etiqueta correspondiente.
     */
    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setVisible(true);
    }
    
    /**
     * Muestra un mensaje de éxito en la etiqueta correspondiente.
     */
    private void mostrarExito(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setStyle("-fx-text-fill: #2ecc71;");
        errorLabel.setVisible(true);
    }
}
