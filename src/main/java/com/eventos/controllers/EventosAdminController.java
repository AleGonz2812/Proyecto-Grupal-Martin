package com.eventos.controllers;

import com.eventos.models.Evento;
import com.eventos.models.EstadoEvento;
import com.eventos.models.Sede;
import com.eventos.models.TipoEvento;
import com.eventos.models.Usuario;
import com.eventos.repositories.SedeRepository;
import com.eventos.repositories.TipoEventoRepository;
import com.eventos.services.AutenticacionService;
import com.eventos.services.EventoService;
import com.eventos.utils.HotReloadManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
    private VBox perfilMenu;
    
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

    @FXML
    private WebView mapaWebView;

    private final EventoService eventoService;
    private final SedeRepository sedeRepository;
    private final TipoEventoRepository tipoEventoRepository;
    private final AutenticacionService autenticacionService;
    private ObservableList<Evento> eventosObservable;
    private List<Evento> eventosActuales = new ArrayList<>();
    private WebEngine webEngine;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Constructor del controlador.
     */
    public EventosAdminController() {
        this.eventoService = new EventoService();
        this.sedeRepository = new SedeRepository();
        this.tipoEventoRepository = new TipoEventoRepository();
        this.autenticacionService = AutenticacionService.getInstance();
    }

    /**
     * Inicialización automática de JavaFX.
     */
    @FXML
    private void initialize() {
        configurarTabla();
        configurarFiltros();
        inicializarMapa();
        cargarEventos();
        
        // Listener para selección en tabla
        eventosTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> actualizarInfoEvento(newValue)
        );
        
        // Configurar Hot Reload en la escena
        avatarButton.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                HotReloadManager.setupHotReloadShortcuts(newScene, stage, "/fxml/eventos_admin.fxml");
                
                // Configurar cierre de menú dropdown
                if (perfilMenu != null) {
                    newScene.setOnMouseClicked(event -> {
                        if (perfilMenu.isVisible() && !perfilMenu.getBoundsInParent().contains(event.getX(), event.getY())) {
                            perfilMenu.setVisible(false);
                        }
                    });
                }
            }
        });
    }

    /**
     * Inicializa el mapa Leaflet en el WebView y expone el controlador a JS.
     */
    private void inicializarMapa() {
        if (mapaWebView == null) {
            return;
        }

        webEngine = mapaWebView.getEngine();
        String mapaPath = getClass().getResource("/html/mapa.html").toExternalForm();
        webEngine.load(mapaPath);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaController", this);

                javafx.application.Platform.runLater(() -> {
                    if (!eventosActuales.isEmpty()) {
                        cargarMarcadoresEnMapa(eventosActuales);
                    }
                });
            }
        });
    }
    
    /**
     * Método público para recargar la vista (útil para hot reload).
     */
    @FXML
    private void handleRecargarVista() {
        Stage stage = (Stage) avatarButton.getScene().getWindow();
        HotReloadManager.reloadScene(stage, "/fxml/eventos_admin.fxml", 
            stage.getWidth(), stage.getHeight());
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
            List<Evento> eventos = eventoService.listarTodos();
            eventosObservable = FXCollections.observableArrayList(eventos);
            eventosTable.setItems(eventosObservable);
            infoLabel.setText(eventos.size() + " eventos encontrados");
            actualizarMapaConLista(eventos);
        } catch (Exception e) {
            mostrarError("Error al cargar eventos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga los marcadores de los eventos en el mapa.
     */
    private void cargarMarcadoresEnMapa(List<Evento> eventos) {
        if (webEngine == null) {
            return;
        }

        webEngine.executeScript("if (window.limpiarMarcadores) window.limpiarMarcadores();");

        int agregados = 0;
        for (Evento evento : eventos) {
            if (evento.getSede() != null &&
                evento.getSede().getLatitud() != null &&
                evento.getSede().getLongitud() != null) {

                double lat = evento.getSede().getLatitud();
                double lng = evento.getSede().getLongitud();
                String nombre = escaparJS(evento.getNombre());
                String sede = escaparJS(evento.getSede().getNombre());
                String fecha = escaparJS(evento.getFechaInicio().format(dateFormatter));
                String tipo = escaparJS(evento.getTipoEvento().getNombre());
                Long id = evento.getId();

                String script = String.format(Locale.US,
                    "if (window.agregarMarcador) { window.agregarMarcador(%f, %f, '%s', '%s', '%s', '%s', %d); }",
                    lat, lng, nombre, sede, fecha, tipo, id
                );

                try {
                    webEngine.executeScript(script);
                    agregados++;
                } catch (Exception e) {
                    System.err.println("Error agregando marcador: " + e.getMessage());
                }
            }
        }

        webEngine.executeScript("if (window.forzarRepaint) window.forzarRepaint();");
        System.out.println("Marcadores cargados en admin: " + agregados);
    }

    private void actualizarMapaConLista(List<Evento> eventos) {
        eventosActuales = eventos;
        if (webEngine != null) {
            cargarMarcadoresEnMapa(eventosActuales);
        }
    }

    private String escaparJS(String texto) {
        if (texto == null) return "";
        return texto.replace("'", "\\'").replace("\"", "\\\"").replace("\n", " ");
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
        mostrarFormularioEvento(null);
    }

    @FXML
    private void handleModificarEvento() {
        Evento eventoSeleccionado = eventosTable.getSelectionModel().getSelectedItem();
        
        if (eventoSeleccionado == null) {
            mostrarAdvertencia("Por favor, selecciona un evento de la tabla");
            return;
        }
        
        // TODO: Abrir diálogo de modificación de evento
        mostrarFormularioEvento(eventoSeleccionado);
    }

    private void mostrarFormularioEvento(Evento existente) {
        Dialog<Evento> dialog = new Dialog<>();
        dialog.setTitle(existente == null ? "Crear evento" : "Editar evento");
        dialog.setHeaderText(null);

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(10, 10, 10, 10));

        TextField nombreField = new TextField();
        TextField descripcionField = new TextField();
        ComboBox<TipoEvento> tipoCombo = new ComboBox<>();
        ComboBox<Sede> sedeCombo = new ComboBox<>();
        TextField fechaInicioField = new TextField();
        TextField fechaFinField = new TextField();
        TextField aforoField = new TextField();
        TextField precioBaseField = new TextField();
        ComboBox<EstadoEvento> estadoCombo = new ComboBox<>();

        tipoCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(TipoEvento object) {
                return object != null ? object.getNombre() : "";
            }

            @Override
            public TipoEvento fromString(String string) {
                return null; // no se usa
            }
        });

        sedeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Sede object) {
                return object != null ? object.getNombre() : "";
            }

            @Override
            public Sede fromString(String string) {
                return null;
            }
        });

        try {
            tipoCombo.setItems(FXCollections.observableArrayList(tipoEventoRepository.findAll()));
            sedeCombo.setItems(FXCollections.observableArrayList(sedeRepository.findAll()));
        } catch (Exception e) {
            mostrarError("No se pudieron cargar tipos o sedes: " + e.getMessage());
        }

        estadoCombo.setItems(FXCollections.observableArrayList(EstadoEvento.values()));

        if (existente != null) {
            nombreField.setText(existente.getNombre());
            descripcionField.setText(existente.getDescripcion());
            tipoCombo.setValue(existente.getTipoEvento());
            sedeCombo.setValue(existente.getSede());
            fechaInicioField.setText(existente.getFechaInicio().toString());
            fechaFinField.setText(existente.getFechaFin().toString());
            aforoField.setText(String.valueOf(existente.getAforoMaximo()));
            if (existente.getPrecioBase() != null) {
                precioBaseField.setText(existente.getPrecioBase().toString());
            }
            estadoCombo.setValue(existente.getEstado());
        } else {
            precioBaseField.setText("0"); // Valor por defecto
        }

        grid.add(new Label("Nombre"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Descripción"), 0, 1);
        grid.add(descripcionField, 1, 1);
        grid.add(new Label("Tipo"), 0, 2);
        grid.add(tipoCombo, 1, 2);
        grid.add(new Label("Sede"), 0, 3);
        grid.add(sedeCombo, 1, 3);
        grid.add(new Label("Fecha inicio (yyyy-MM-ddTHH:mm)"), 0, 4);
        grid.add(fechaInicioField, 1, 4);
        grid.add(new Label("Fecha fin (yyyy-MM-ddTHH:mm)"), 0, 5);
        grid.add(fechaFinField, 1, 5);
        grid.add(new Label("Aforo máximo"), 0, 6);
        grid.add(aforoField, 1, 6);
        grid.add(new Label("Precio base ($)"), 0, 7);
        grid.add(precioBaseField, 1, 7);
        grid.add(new Label("Estado"), 0, 8);
        grid.add(estadoCombo, 1, 8);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarBtn) {
                try {
                    Evento ev = existente != null ? existente : new Evento();
                    ev.setNombre(nombreField.getText());
                    ev.setDescripcion(descripcionField.getText());
                    ev.setTipoEvento(tipoCombo.getValue());
                    ev.setSede(sedeCombo.getValue());
                    ev.setFechaInicio(parseFecha(fechaInicioField.getText()));
                    ev.setFechaFin(parseFecha(fechaFinField.getText()));
                    ev.setAforoMaximo(Integer.parseInt(aforoField.getText()));
                    
                    // Parsear precio base
                    String precioTexto = precioBaseField.getText().trim();
                    if (!precioTexto.isEmpty()) {
                        ev.setPrecioBase(new java.math.BigDecimal(precioTexto));
                    } else {
                        ev.setPrecioBase(java.math.BigDecimal.ZERO);
                    }
                    
                    if (estadoCombo.getValue() != null) {
                        ev.setEstado(estadoCombo.getValue());
                    }
                    return ev;
                } catch (NumberFormatException e) {
                    mostrarError("Error: El precio debe ser un número válido");
                    return null;
                }
            }
            return null;
        });

        Optional<Evento> resultado = dialog.showAndWait();

        resultado.ifPresent(ev -> {
            try {
                if (ev.getId() == null) {
                    eventoService.crear(ev);
                    mostrarExito("Evento creado correctamente");
                } else {
                    eventoService.actualizar(ev);
                    mostrarExito("Evento actualizado correctamente");
                }
                cargarEventos();
            } catch (Exception ex) {
                mostrarError("Error al guardar evento: " + ex.getMessage());
            }
        });
    }

    private LocalDateTime parseFecha(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new com.eventos.exceptions.ValidationException("La fecha es obligatoria");
        }
        
        try {
            // Intentar formato ISO con T (yyyy-MM-ddTHH:mm)
            return LocalDateTime.parse(texto);
        } catch (Exception e1) {
            try {
                // Intentar formato con espacio (yyyy-MM-dd HH:mm)
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                return LocalDateTime.parse(texto, formatter);
            } catch (Exception e2) {
                try {
                    // Intentar formato dd/MM/yyyy HH:mm
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    return LocalDateTime.parse(texto, formatter);
                } catch (Exception e3) {
                    throw new com.eventos.exceptions.ValidationException(
                        "Formato de fecha inválido. Usa uno de estos formatos:\n" +
                        "- 2025-12-09T15:30\n" +
                        "- 2025-12-09 15:30\n" +
                        "- 09/12/2025 15:30"
                    );
                }
            }
        }
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
                    eventoService.eliminar(eventoSeleccionado.getId());
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
            List<Evento> todosEventos = eventoService.listarTodos();
            List<Evento> eventosFiltrados = todosEventos.stream()
                .filter(e -> e.getNombre().toLowerCase().contains(textoBusqueda) ||
                            e.getDescripcion().toLowerCase().contains(textoBusqueda))
                .toList();
            
            eventosObservable = FXCollections.observableArrayList(eventosFiltrados);
            eventosTable.setItems(eventosObservable);
            infoLabel.setText(eventosFiltrados.size() + " eventos encontrados");
            actualizarMapaConLista(eventosFiltrados);
        } catch (Exception e) {
            mostrarError("Error al buscar: " + e.getMessage());
        }
    }

    @FXML
    private void handleFiltrarTipo() {
        String tipoSeleccionado = filtroTipoCombo.getValue();
        if (tipoSeleccionado == null || tipoSeleccionado.equals("Todos")) {
            cargarEventos();
            return;
        }

        try {
            List<Evento> filtrados = eventoService.listarTodos().stream()
                .filter(e -> e.getTipoEvento().getNombre().equalsIgnoreCase(tipoSeleccionado))
                .toList();
            eventosObservable = FXCollections.observableArrayList(filtrados);
            eventosTable.setItems(eventosObservable);
            infoLabel.setText(filtrados.size() + " eventos del tipo " + tipoSeleccionado);
            actualizarMapaConLista(filtrados);
        } catch (Exception e) {
            mostrarError("Error al filtrar por tipo: " + e.getMessage());
        }
    }

    @FXML
    private void handleFiltrarEstado() {
        String estadoSeleccionado = filtroEstadoCombo.getValue();
        
        if (estadoSeleccionado == null || estadoSeleccionado.equals("Todos")) {
            cargarEventos();
            return;
        }
        
        try {
            String estadoBuscado = estadoSeleccionado.equals("Activos") ? EstadoEvento.ACTIVO.name() : EstadoEvento.CANCELADO.name();
            List<Evento> eventosFiltrados = eventoService.filtrarPorEstado(EstadoEvento.valueOf(estadoBuscado));
            eventosObservable = FXCollections.observableArrayList(eventosFiltrados);
            eventosTable.setItems(eventosObservable);
            infoLabel.setText(eventosFiltrados.size() + " eventos " + estadoSeleccionado.toLowerCase());
            actualizarMapaConLista(eventosFiltrados);
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

    /**
     * Método llamado desde JavaScript cuando se selecciona un marcador.
     */
    public void seleccionarEventoDesdeJS(int eventoId) {
        if (eventosObservable == null) return;

        eventosObservable.stream()
            .filter(e -> e.getId() != null && e.getId().intValue() == eventoId)
            .findFirst()
            .ifPresent(evento -> {
                eventosTable.getSelectionModel().select(evento);
                eventosTable.scrollTo(evento);
                actualizarInfoEvento(evento);
            });
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
    private void togglePerfilMenu() {
        if (perfilMenu == null) return;
        boolean visible = perfilMenu.isVisible();
        perfilMenu.setVisible(!visible);
        perfilMenu.setManaged(!visible);
    }
    
    @FXML
    private void handleMiPerfil() {
        if (perfilMenu != null) {
            perfilMenu.setVisible(false);
            perfilMenu.setManaged(false);
        }
        
        Usuario admin = autenticacionService.getUsuarioActual();
        if (admin == null) {
            mostrarError("No hay sesión activa");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mi Perfil");
        alert.setHeaderText("Información del Administrador");
        alert.setContentText(
            "Nombre: " + admin.getNombre() + "\n" +
            "Email: " + admin.getEmail() + "\n" +
            "DNI: " + admin.getDni() + "\n" +
            "Rol: " + admin.getRol().getNombre()
        );
        alert.showAndWait();
    }
    
    @FXML
    private void handlePanelAdmin() {
        if (perfilMenu != null) {
            perfilMenu.setVisible(false);
            perfilMenu.setManaged(false);
        }
        mostrarInfo("Panel de administración");
    }
    
    @FXML
    private void handleConfiguracion() {
        if (perfilMenu != null) {
            perfilMenu.setVisible(false);
            perfilMenu.setManaged(false);
        }
        mostrarInfo("Configuración en desarrollo");
    }
    
    @FXML
    private void handleCerrarSesionDropdown() {
        if (perfilMenu != null) {
            perfilMenu.setVisible(false);
            perfilMenu.setManaged(false);
        }
        handleCerrarSesion();
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
            javafx.scene.Scene scene = new javafx.scene.Scene(loginRoot, 600, 500);
            stage.setScene(scene);
            stage.setTitle("Sistema de Gesti\u00f3n de Eventos - Login");
            stage.setResizable(true);
            stage.centerOnScreen();
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
