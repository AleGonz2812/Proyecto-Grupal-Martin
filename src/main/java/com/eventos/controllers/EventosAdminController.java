package com.eventos.controllers;

import com.eventos.models.Evento;
import com.eventos.models.EstadoEvento;
import com.eventos.models.Usuario;
import com.eventos.repositories.EventoRepository;
import com.eventos.services.AutenticacionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador para la vista de gestión de eventos (Administrador).
 * Permite crear, modificar y eliminar eventos.
 */
public class EventosAdminController {

    @FXML
    private Button menuButton;
    
    @FXML
    private Button avatarButton;
    
    @FXML
    private VBox sideMenu;
    
    @FXML
    private TextField busquedaField;
    
    @FXML
    private ComboBox<String> filtroTipoCombo;
    
    @FXML
    private ComboBox<String> filtroEstadoCombo;
    
    @FXML
    private TableView<Evento> eventosTable;
    
    @FXML
    private TableColumn<Evento, String> idColumn;
    
    @FXML
    private TableColumn<Evento, String> nombreColumn;
    
    @FXML
    private TableColumn<Evento, String> tipoColumn;
    
    @FXML
    private TableColumn<Evento, String> fechaColumn;
    
    @FXML
    private TableColumn<Evento, String> sedeColumn;
    
    @FXML
    private TableColumn<Evento, String> estadoColumn;
    
    @FXML
    private Label infoLabel;

    private final EventoRepository eventoRepository;
    private final AutenticacionService autenticacionService;
    private Usuario usuarioActual;
    private ObservableList<Evento> eventosObservable;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Constructor del controlador.
     */
    public EventosAdminController() {
        this.eventoRepository = new EventoRepository();
        this.autenticacionService = AutenticacionService.getInstance();
    }

    /**
     * Inicialización automática de JavaFX.
     */
    @FXML
    private void initialize() {
        usuarioActual = autenticacionService.getUsuarioActual();
        configurarTabla();
        configurarFiltros();
        cargarEventos();
        
        // Listener para selección en tabla
        eventosTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> actualizarInfoEvento(newValue)
        );
    }

    /**
     * Configura las columnas de la tabla.
     */
    private void configurarTabla() {
        idColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getId().toString())
        );
        
        nombreColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNombre())
        );
        
        tipoColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTipoEvento().getNombre())
        );
        
        fechaColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFechaInicio().format(dateFormatter))
        );
        
        sedeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSede().getNombre())
        );
        
        estadoColumn.setCellValueFactory(cellData -> {
            String estado = cellData.getValue().getEstado().name();
            return new SimpleStringProperty(estado.equals("ACTIVO") ? "✅ Activo" : "❌ Inactivo");
        });
    }

    /**
     * Configura los filtros (ComboBox).
     */
    private void configurarFiltros() {
        // TODO: Cargar tipos de eventos desde BD
        filtroTipoCombo.setItems(FXCollections.observableArrayList(
            "Todos", "Concierto", "Deportivo", "Teatro", "Conferencia"
        ));
        filtroTipoCombo.setValue("Todos");
        
        filtroEstadoCombo.setItems(FXCollections.observableArrayList(
            "Todos", "Activos", "Inactivos"
        ));
        filtroEstadoCombo.setValue("Todos");
    }

    /**
     * Carga todos los eventos desde la base de datos.
     */
    private void cargarEventos() {
        try {
            List<Evento> eventos = eventoRepository.findAll();
            eventosObservable = FXCollections.observableArrayList(eventos);
            eventosTable.setItems(eventosObservable);
            infoLabel.setText(eventos.size() + " eventos encontrados");
        } catch (Exception e) {
            mostrarError("Error al cargar eventos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Actualiza la información del evento seleccionado.
     */
    private void actualizarInfoEvento(Evento evento) {
        if (evento == null) {
            infoLabel.setText("Selecciona un evento para ver detalles");
            return;
        }
        
        infoLabel.setText(String.format("📍 %s | 📅 %s | 🏬 %s | Capacidad: %d",
            evento.getNombre(),
            evento.getFechaInicio().format(dateFormatter),
            evento.getSede().getNombre(),
            evento.getSede().getCapacidad()
        ));
    }

    // ========== MÉTODOS DE ACCIONES FXML ==========

    @FXML
    private void handleCrearEvento() {
        // TODO: Abrir diálogo de creación de evento
        mostrarInfo("Funcionalidad de crear evento en desarrollo");
    }

    @FXML
    private void handleModificarEvento() {
        Evento eventoSeleccionado = eventosTable.getSelectionModel().getSelectedItem();
        
        if (eventoSeleccionado == null) {
            mostrarAdvertencia("Por favor, selecciona un evento de la tabla");
            return;
        }
        
        // TODO: Abrir diálogo de modificación de evento
        mostrarInfo("Funcionalidad de modificar evento en desarrollo\nEvento: " + 
                   eventoSeleccionado.getNombre());
    }

    @FXML
    private void handleEliminarEvento() {
        Evento eventoSeleccionado = eventosTable.getSelectionModel().getSelectedItem();
        
        if (eventoSeleccionado == null) {
            mostrarAdvertencia("Por favor, selecciona un evento de la tabla");
            return;
        }
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar evento?");
        confirmacion.setContentText("¿Estás seguro de eliminar el evento:\n" + 
                                   eventoSeleccionado.getNombre() + "?");
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    eventoRepository.delete(eventoSeleccionado.getId());
                    cargarEventos();
                    mostrarExito("Evento eliminado correctamente");
                } catch (Exception e) {
                    mostrarError("Error al eliminar evento: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleBuscar() {
        String textoBusqueda = busquedaField.getText().trim().toLowerCase();
        
        if (textoBusqueda.isEmpty()) {
            cargarEventos();
            return;
        }
        
        try {
            List<Evento> todosEventos = eventoRepository.findAll();
            List<Evento> eventosFiltrados = todosEventos.stream()
                .filter(e -> e.getNombre().toLowerCase().contains(textoBusqueda) ||
                            e.getDescripcion().toLowerCase().contains(textoBusqueda))
                .toList();
            
            eventosObservable = FXCollections.observableArrayList(eventosFiltrados);
            eventosTable.setItems(eventosObservable);
            infoLabel.setText(eventosFiltrados.size() + " eventos encontrados");
        } catch (Exception e) {
            mostrarError("Error al buscar: " + e.getMessage());
        }
    }

    @FXML
    private void handleFiltrarTipo() {
        // TODO: Implementar filtro por tipo
        mostrarInfo("Filtro por tipo en desarrollo");
    }

    @FXML
    private void handleFiltrarEstado() {
        String estadoSeleccionado = filtroEstadoCombo.getValue();
        
        if (estadoSeleccionado == null || estadoSeleccionado.equals("Todos")) {
            cargarEventos();
            return;
        }
        
        try {
            List<Evento> todosEventos = eventoRepository.findAll();
            String estadoBuscado = estadoSeleccionado.equals("Activos") ? "ACTIVO" : "INACTIVO";
            
            List<Evento> eventosFiltrados = todosEventos.stream()
                .filter(e -> e.getEstado().name().equals(estadoBuscado))
                .toList();
            
            eventosObservable = FXCollections.observableArrayList(eventosFiltrados);
            eventosTable.setItems(eventosObservable);
            infoLabel.setText(eventosFiltrados.size() + " eventos " + estadoSeleccionado.toLowerCase());
        } catch (Exception e) {
            mostrarError("Error al filtrar: " + e.getMessage());
        }
    }

    @FXML
    private void handleLimpiarFiltros() {
        busquedaField.clear();
        filtroTipoCombo.setValue("Todos");
        filtroEstadoCombo.setValue("Todos");
        cargarEventos();
    }

    @FXML
    private void toggleMenu() {
        boolean isVisible = sideMenu.isVisible();
        sideMenu.setVisible(!isVisible);
        sideMenu.setManaged(!isVisible);
    }

    @FXML
    private void handleMisSedes() {
        // TODO: Implementar vista de gestión de sedes
        mostrarInfo("Gestión de sedes en desarrollo");
        toggleMenu();
    }

    @FXML
    private void handleMisEventos() {
        // Recargar la vista de eventos
        cargarEventos();
        toggleMenu();
    }

    @FXML
    private void handleGestionarUsuarios() {
        // TODO: Implementar vista de gestión de usuarios
        mostrarInfo("Gestión de usuarios en desarrollo");
        toggleMenu();
    }

    @FXML
    private void handleEstadisticas() {
        // TODO: Implementar vista de estadísticas
        mostrarInfo("Estadísticas en desarrollo");
        toggleMenu();
    }

    @FXML
    private void handlePerfil() {
        // TODO: Implementar vista de perfil
        mostrarInfo("Perfil de administrador en desarrollo");
        toggleMenu();
    }

    @FXML
    private void handleCerrarSesion() {
        autenticacionService.cerrarSesion();
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/login.fxml")
            );
            javafx.scene.Parent loginRoot = loader.load();
            Stage stage = (Stage) avatarButton.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(loginRoot);
            stage.setScene(scene);
            stage.setTitle("Sistema de Gestión de Eventos - Login");
            stage.show();
        } catch (Exception e) {
            mostrarError("Error al cerrar sesión: " + e.getMessage());
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Muestra un mensaje de éxito.
     */
    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de información.
     */
    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de advertencia.
     */
    private void mostrarAdvertencia(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de error.
     */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
