package com.eventos.controllers;

import com.eventos.models.Sede;
import com.eventos.repositories.SedeRepository;
import com.eventos.utils.DialogStyler;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * Controlador para la vista de administración de sedes.
 * Permite crear, modificar y eliminar sedes.
 */
public class SedesAdminController {

    @FXML
    private Button menuButton;
    
    @FXML
    private Button avatarButton;
    
    @FXML
    private VBox perfilMenu;
    
    @FXML
    private VBox sideMenu;
    
    @FXML
    private TextField busquedaField;
    
    @FXML
    private ComboBox<String> filtroCiudadCombo;
    
    @FXML
    private ComboBox<String> filtroActivaCombo;
    
    @FXML
    private TableView<Sede> sedesTable;
    
    @FXML
    private TableColumn<Sede, String> idColumn;
    
    @FXML
    private TableColumn<Sede, String> nombreColumn;
    
    @FXML
    private TableColumn<Sede, String> direccionColumn;
    
    @FXML
    private TableColumn<Sede, String> ciudadColumn;
    
    @FXML
    private TableColumn<Sede, String> capacidadColumn;
    
    @FXML
    private TableColumn<Sede, String> activaColumn;
    
    @FXML
    private Label infoLabel;
    
    private SedeRepository sedeRepository;
    private ObservableList<Sede> sedesData;
    private ObservableList<Sede> todasLasSedes;

    /**
     * Inicializa el controlador después de cargar el FXML
     */
    @FXML
    public void initialize() {
        sedeRepository = new SedeRepository();
        sedesData = FXCollections.observableArrayList();
        todasLasSedes = FXCollections.observableArrayList();
        
        configurarTabla();
        configurarFiltros();
        cargarSedes();
        
        // Configurar menús ocultos por defecto
        if (sideMenu != null) {
            sideMenu.setVisible(false);
            sideMenu.setManaged(false);
        }
        
        if (perfilMenu != null) {
            perfilMenu.setVisible(false);
            perfilMenu.setManaged(false);
        }
    }
    
    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        idColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getId() != null ? data.getValue().getId().toString() : ""));
        
        nombreColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getNombre()));
        
        direccionColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDireccion()));
        
        ciudadColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getCiudad()));
        
        capacidadColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getCapacidad() != null ? 
                data.getValue().getCapacidad().toString() : "0"));
        
        activaColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(Boolean.TRUE.equals(data.getValue().getActiva()) ? "Activa" : "Inactiva"));
        
        sedesTable.setItems(sedesData);
        
        // Listener para selección de sede
        sedesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                mostrarInfoSede(newSelection);
            }
        });
    }
    
    /**
     * Configura los filtros
     */
    private void configurarFiltros() {
        // Filtro de estado
        filtroActivaCombo.getItems().addAll("Todas", "Activas", "Inactivas");
        filtroActivaCombo.setValue("Todas");
    }
    
    /**
     * Carga todas las sedes desde la base de datos
     */
    private void cargarSedes() {
        try {
            List<Sede> sedes = sedeRepository.findAll();
            todasLasSedes.clear();
            todasLasSedes.addAll(sedes);
            sedesData.clear();
            sedesData.addAll(sedes);
            
            // Actualizar filtro de ciudades
            List<String> ciudades = sedes.stream()
                .map(Sede::getCiudad)
                .distinct()
                .sorted()
                .toList();
            
            filtroCiudadCombo.getItems().clear();
            filtroCiudadCombo.getItems().add("Todas");
            filtroCiudadCombo.getItems().addAll(ciudades);
            filtroCiudadCombo.setValue("Todas");
            
            actualizarInfoLabel();
        } catch (Exception e) {
            mostrarError("Error al cargar sedes", e.getMessage());
        }
    }
    
    /**
     * Muestra información de la sede seleccionada
     */
    private void mostrarInfoSede(Sede sede) {
        String info = String.format("Sede: %s | Ciudad: %s | Capacidad: %d | Estado: %s",
            sede.getNombre(),
            sede.getCiudad(),
            sede.getCapacidad(),
            Boolean.TRUE.equals(sede.getActiva()) ? "Activa" : "Inactiva"
        );
        infoLabel.setText(info);
    }
    
    /**
     * Actualiza el label de información general
     */
    private void actualizarInfoLabel() {
        if (sedesData.isEmpty()) {
            infoLabel.setText("No hay sedes para mostrar");
        } else {
            infoLabel.setText(String.format("Mostrando %d de %d sedes", 
                sedesData.size(), todasLasSedes.size()));
        }
    }
    
    // ==================== ACCIONES CRUD ====================
    
    /**
     * Maneja la acción de crear una nueva sede
     */
    @FXML
    private void handleCrearSede() {
        Dialog<Sede> dialog = crearDialogoSede(null);
        Optional<Sede> resultado = dialog.showAndWait();
        
        resultado.ifPresent(sede -> {
            try {
                sedeRepository.save(sede);
                cargarSedes();
                mostrarInfo("Sede creada", "La sede ha sido creada exitosamente");
            } catch (Exception e) {
                mostrarError("Error al crear sede", e.getMessage());
            }
        });
    }
    
    /**
     * Maneja la acción de modificar una sede existente
     */
    @FXML
    private void handleModificarSede() {
        Sede sedeSeleccionada = sedesTable.getSelectionModel().getSelectedItem();
        
        if (sedeSeleccionada == null) {
            mostrarAdvertencia("Selección requerida", "Por favor selecciona una sede para modificar");
            return;
        }
        
        Dialog<Sede> dialog = crearDialogoSede(sedeSeleccionada);
        Optional<Sede> resultado = dialog.showAndWait();
        
        resultado.ifPresent(sede -> {
            try {
                sedeRepository.update(sede);
                cargarSedes();
                mostrarInfo("Sede modificada", "La sede ha sido modificada exitosamente");
            } catch (Exception e) {
                mostrarError("Error al modificar sede", e.getMessage());
            }
        });
    }
    
    /**
     * Maneja la acción de eliminar una sede
     */
    @FXML
    private void handleEliminarSede() {
        Sede sedeSeleccionada = sedesTable.getSelectionModel().getSelectedItem();
        
        if (sedeSeleccionada == null) {
            mostrarAdvertencia("Selección requerida", "Por favor selecciona una sede para eliminar");
            return;
        }
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar sede?");
        confirmacion.setContentText("¿Estás seguro de que deseas eliminar la sede: " + 
            sedeSeleccionada.getNombre() + "?\n\nEsta acción no se puede deshacer.");
        DialogStyler.styleAlert(confirmacion);
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                sedeRepository.delete(sedeSeleccionada.getId());
                cargarSedes();
                mostrarInfo("Sede eliminada", "La sede ha sido eliminada exitosamente");
            } catch (Exception e) {
                mostrarError("Error al eliminar sede", 
                    "No se puede eliminar la sede porque tiene eventos asociados.\n" + e.getMessage());
            }
        }
    }
    
    /**
     * Crea el diálogo para crear/editar una sede
     */
    private Dialog<Sede> crearDialogoSede(Sede sedeExistente) {
        Dialog<Sede> dialog = new Dialog<>();
        dialog.setTitle(sedeExistente == null ? "Crear Sede" : "Modificar Sede");
        dialog.setHeaderText(sedeExistente == null ? 
            "Ingresa los datos de la nueva sede" : 
            "Modifica los datos de la sede");
        
        // Aplicar estilos oscuros
        DialogStyler.styleDialog(dialog);
        
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);
        
        // Crear el formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre de la sede");
        
        TextField direccionField = new TextField();
        direccionField.setPromptText("Dirección");
        
        TextField ciudadField = new TextField();
        ciudadField.setPromptText("Ciudad");
        
        TextField provinciaField = new TextField();
        provinciaField.setPromptText("Provincia");
        
        TextField codigoPostalField = new TextField();
        codigoPostalField.setPromptText("Código Postal");
        
        TextField capacidadField = new TextField();
        capacidadField.setPromptText("Capacidad");
        
        TextField telefonoField = new TextField();
        telefonoField.setPromptText("Teléfono");
        
        TextArea descripcionArea = new TextArea();
        descripcionArea.setPromptText("Descripción");
        descripcionArea.setPrefRowCount(3);
        
        TextField latitudField = new TextField();
        latitudField.setPromptText("Latitud (opcional)");
        
        TextField longitudField = new TextField();
        longitudField.setPromptText("Longitud (opcional)");
        
        CheckBox activaCheckBox = new CheckBox("Sede Activa");
        activaCheckBox.setSelected(true);
        
        // Si es modificación, cargar datos existentes
        if (sedeExistente != null) {
            nombreField.setText(sedeExistente.getNombre());
            direccionField.setText(sedeExistente.getDireccion());
            ciudadField.setText(sedeExistente.getCiudad());
            provinciaField.setText(sedeExistente.getProvincia());
            codigoPostalField.setText(sedeExistente.getCodigoPostal());
            capacidadField.setText(sedeExistente.getCapacidad() != null ? 
                sedeExistente.getCapacidad().toString() : "");
            telefonoField.setText(sedeExistente.getTelefono());
            descripcionArea.setText(sedeExistente.getDescripcion());
            latitudField.setText(sedeExistente.getLatitud() != null ? 
                sedeExistente.getLatitud().toString() : "");
            longitudField.setText(sedeExistente.getLongitud() != null ? 
                sedeExistente.getLongitud().toString() : "");
            activaCheckBox.setSelected(Boolean.TRUE.equals(sedeExistente.getActiva()));
        }
        
        // Agregar campos al grid
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Dirección:"), 0, 1);
        grid.add(direccionField, 1, 1);
        grid.add(new Label("Ciudad:"), 0, 2);
        grid.add(ciudadField, 1, 2);
        grid.add(new Label("Provincia:"), 0, 3);
        grid.add(provinciaField, 1, 3);
        grid.add(new Label("Código Postal:"), 0, 4);
        grid.add(codigoPostalField, 1, 4);
        grid.add(new Label("Capacidad:"), 0, 5);
        grid.add(capacidadField, 1, 5);
        grid.add(new Label("Teléfono:"), 0, 6);
        grid.add(telefonoField, 1, 6);
        grid.add(new Label("Descripción:"), 0, 7);
        grid.add(descripcionArea, 1, 7);
        grid.add(new Label("Latitud:"), 0, 8);
        grid.add(latitudField, 1, 8);
        grid.add(new Label("Longitud:"), 0, 9);
        grid.add(longitudField, 1, 9);
        grid.add(activaCheckBox, 1, 10);
        
        dialog.getDialogPane().setContent(grid);
        
        // Validación y conversión del resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                try {
                    Sede sede = sedeExistente != null ? sedeExistente : new Sede();
                    
                    // Validaciones
                    if (nombreField.getText().trim().isEmpty()) {
                        mostrarError("Campo requerido", "El nombre es obligatorio");
                        return null;
                    }
                    if (direccionField.getText().trim().isEmpty()) {
                        mostrarError("Campo requerido", "La dirección es obligatoria");
                        return null;
                    }
                    if (ciudadField.getText().trim().isEmpty()) {
                        mostrarError("Campo requerido", "La ciudad es obligatoria");
                        return null;
                    }
                    if (capacidadField.getText().trim().isEmpty()) {
                        mostrarError("Campo requerido", "La capacidad es obligatoria");
                        return null;
                    }
                    
                    sede.setNombre(nombreField.getText().trim());
                    sede.setDireccion(direccionField.getText().trim());
                    sede.setCiudad(ciudadField.getText().trim());
                    sede.setProvincia(provinciaField.getText().trim());
                    sede.setCodigoPostal(codigoPostalField.getText().trim());
                    sede.setCapacidad(Integer.parseInt(capacidadField.getText().trim()));
                    sede.setTelefono(telefonoField.getText().trim());
                    sede.setDescripcion(descripcionArea.getText().trim());
                    sede.setActiva(activaCheckBox.isSelected());
                    
                    // Latitud y longitud opcionales
                    if (!latitudField.getText().trim().isEmpty()) {
                        sede.setLatitud(Double.parseDouble(latitudField.getText().trim()));
                    }
                    if (!longitudField.getText().trim().isEmpty()) {
                        sede.setLongitud(Double.parseDouble(longitudField.getText().trim()));
                    }
                    
                    return sede;
                } catch (NumberFormatException e) {
                    mostrarError("Error de formato", "Verifica que los números sean válidos");
                    return null;
                }
            }
            return null;
        });
        
        return dialog;
    }
    
    // ==================== FILTROS Y BÚSQUEDA ====================
    
    /**
     * Maneja la búsqueda de sedes
     */
    @FXML
    private void handleBuscar() {
        aplicarFiltros();
    }
    
    /**
     * Maneja el filtro por ciudad
     */
    @FXML
    private void handleFiltrarCiudad() {
        aplicarFiltros();
    }
    
    /**
     * Maneja el filtro por estado
     */
    @FXML
    private void handleFiltrarActiva() {
        aplicarFiltros();
    }
    
    /**
     * Aplica todos los filtros activos
     */
    private void aplicarFiltros() {
        String busqueda = busquedaField.getText().toLowerCase();
        String ciudadSeleccionada = filtroCiudadCombo.getValue();
        String estadoSeleccionado = filtroActivaCombo.getValue();
        
        List<Sede> sedesFiltradas = todasLasSedes.stream()
            .filter(sede -> {
                // Filtro por búsqueda
                if (!busqueda.isEmpty()) {
                    return sede.getNombre().toLowerCase().contains(busqueda) ||
                           sede.getDireccion().toLowerCase().contains(busqueda);
                }
                return true;
            })
            .filter(sede -> {
                // Filtro por ciudad
                if (ciudadSeleccionada != null && !ciudadSeleccionada.equals("Todas")) {
                    return sede.getCiudad().equals(ciudadSeleccionada);
                }
                return true;
            })
            .filter(sede -> {
                // Filtro por estado
                if (estadoSeleccionado != null) {
                    if (estadoSeleccionado.equals("Activas")) {
                        return Boolean.TRUE.equals(sede.getActiva());
                    } else if (estadoSeleccionado.equals("Inactivas")) {
                        return !Boolean.TRUE.equals(sede.getActiva());
                    }
                }
                return true;
            })
            .toList();
        
        sedesData.clear();
        sedesData.addAll(sedesFiltradas);
        actualizarInfoLabel();
    }
    
    /**
     * Limpia todos los filtros
     */
    @FXML
    private void handleLimpiarFiltros() {
        busquedaField.clear();
        filtroCiudadCombo.setValue("Todas");
        filtroActivaCombo.setValue("Todas");
        sedesData.clear();
        sedesData.addAll(todasLasSedes);
        actualizarInfoLabel();
    }
    
    // ==================== MENÚS ====================
    
    /**
     * Alterna la visibilidad del menú lateral
     */
    @FXML
    private void toggleMenu() {
        if (sideMenu != null) {
            boolean visible = sideMenu.isVisible();
            sideMenu.setVisible(!visible);
            sideMenu.setManaged(!visible);
        }
    }
    
    /**
     * Alterna la visibilidad del menú de perfil
     */
    @FXML
    private void togglePerfilMenu() {
        if (perfilMenu != null) {
            boolean visible = perfilMenu.isVisible();
            perfilMenu.setVisible(!visible);
            perfilMenu.setManaged(!visible);
        }
    }
    
    /**
     * Navega a la vista de eventos
     */
    @FXML
    private void handleVolverEventos() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/eventos_admin.fxml")
            );
            javafx.scene.Parent eventosRoot = loader.load();
            Stage stage = (Stage) sedesTable.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(eventosRoot);
            stage.setScene(scene);
            stage.setTitle("Sistema de Gestión de Eventos - Admin");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            mostrarError("Error de navegación", e.getMessage());
        }
    }
    
    /**
     * Maneja la acción del menú Mi Perfil
     */
    @FXML
    private void handleMiPerfil() {
        mostrarInfo("Mi Perfil", "Funcionalidad en desarrollo");
    }
    
    /**
     * Maneja la acción del menú Panel Admin
     */
    @FXML
    private void handlePanelAdmin() {
        mostrarInfo("Panel Admin", "Funcionalidad en desarrollo");
    }
    
    /**
     * Maneja la acción del menú Configuración
     */
    @FXML
    private void handleConfiguracion() {
        mostrarInfo("Configuración", "Funcionalidad en desarrollo");
    }
    
    /**
     * Maneja el cierre de sesión desde el dropdown
     */
    @FXML
    private void handleCerrarSesionDropdown() {
        handleCerrarSesion();
    }
    
    /**
     * Cierra la sesión del usuario
     */
    @FXML
    private void handleCerrarSesion() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cerrar Sesión");
        confirmacion.setHeaderText("¿Deseas cerrar sesión?");
        confirmacion.setContentText("Serás redirigido a la pantalla de login");
        DialogStyler.styleAlert(confirmacion);
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
                );
                javafx.scene.Parent loginRoot = loader.load();
                Stage stage = (Stage) sedesTable.getScene().getWindow();
                javafx.scene.Scene scene = new javafx.scene.Scene(loginRoot, 600, 500);
                stage.setScene(scene);
                stage.setTitle("Sistema de Gestión de Eventos - Login");
                stage.setResizable(true);
                stage.centerOnScreen();
                stage.show();
            } catch (Exception e) {
                mostrarError("Error al cerrar sesión", e.getMessage());
            }
        }
    }
    
    // ==================== UTILIDADES ====================
    
    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        DialogStyler.styleAlert(alert);
        alert.showAndWait();
    }
    
    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        DialogStyler.styleAlert(alert);
        alert.showAndWait();
    }
    
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        DialogStyler.styleAlert(alert);
        alert.showAndWait();
    }
}
