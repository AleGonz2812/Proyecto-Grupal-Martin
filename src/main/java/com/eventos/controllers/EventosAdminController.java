package com.eventos.controllers;

import com.eventos.models.Evento;
import com.eventos.models.EstadoEvento;
import com.eventos.models.Sede;
import com.eventos.models.TipoEvento;
import com.eventos.models.Usuario;
import com.eventos.models.Compra;
import com.eventos.models.Entrada;
import com.eventos.repositories.SedeRepository;
import com.eventos.repositories.TipoEventoRepository;
import com.eventos.repositories.UsuarioRepository;
import com.eventos.repositories.CompraRepository;
import com.eventos.repositories.EntradaRepository;
import com.eventos.services.AutenticacionService;
import com.eventos.services.EventoService;
import com.eventos.utils.HotReloadManager;
import com.eventos.utils.DialogStyler;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
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
    private final UsuarioRepository usuarioRepository;
    private final CompraRepository compraRepository;
    private final EntradaRepository entradaRepository;
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
        this.usuarioRepository = new UsuarioRepository();
        this.compraRepository = new CompraRepository();
        this.entradaRepository = new EntradaRepository();
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
                    // Forzar recarga inicial del mapa para asegurar que las teselas se carguen
                    try {
                        webEngine.executeScript("if (window.forzarRecargaInicial) window.forzarRecargaInicial();");
                    } catch (Exception e) {
                        System.err.println("Error al forzar recarga inicial: " + e.getMessage());
                    }
                    
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
            new SimpleStringProperty(formatId(cellData.getValue()))
        );

        nombreColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(nullSafe(cellData.getValue() != null ? cellData.getValue().getNombre() : null))
        );

        tipoColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(nullSafe(cellData.getValue() != null && cellData.getValue().getTipoEvento() != null
                ? cellData.getValue().getTipoEvento().getNombre()
                : "Sin tipo"))
        );

        fechaColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(formatFecha(cellData.getValue()))
        );

        sedeColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(nullSafe(cellData.getValue() != null && cellData.getValue().getSede() != null
                ? cellData.getValue().getSede().getNombre()
                : "Sin sede"))
        );

        estadoColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(formatEstado(cellData.getValue()))
        );

        if (eventosTable != null) {
            eventosTable.setPlaceholder(new Label("No hay eventos para mostrar"));
                eventosTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            // Asegura texto legible en todas las celdas
            aplicarEstiloColumna(idColumn);
            aplicarEstiloColumna(nombreColumn);
            aplicarEstiloColumna(tipoColumn);
            aplicarEstiloColumna(fechaColumn);
            aplicarEstiloColumna(sedeColumn);
            aplicarEstiloColumna(estadoColumn);
        }
    }

    /**
     * Fuerza color de texto y evita celdas en blanco por estilos.
     */
    private void aplicarEstiloColumna(TableColumn<Evento, String> columna) {
        columna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setGraphic(null);
                }
                // Aplicar estilos inline para garantizar visibilidad
                setStyle("-fx-text-fill: #eaf0ff; -fx-alignment: CENTER_LEFT; -fx-opacity: 1.0; -fx-background-color: transparent;");
            }
        });
    }

    private String nullSafe(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }

    private String formatId(Evento evento) {
        return (evento != null && evento.getId() != null)
            ? evento.getId().toString()
            : "-";
    }

    private String formatFecha(Evento evento) {
        if (evento == null || evento.getFechaInicio() == null) {
            return "-";
        }
        try {
            return evento.getFechaInicio().format(dateFormatter);
        } catch (Exception e) {
            return "-";
        }
    }

    private String formatEstado(Evento evento) {
        if (evento == null || evento.getEstado() == null) {
            return "Sin estado";
        }

        return switch (evento.getEstado()) {
            case PLANIFICADO -> "📅 Planificado";
            case ACTIVO -> "✅ Activo";
            case CANCELADO -> "❌ Cancelado";
            case FINALIZADO -> "✔️ Finalizado";
        };
    }

    /**
     * Configura los filtros (ComboBox).
     */
    private void configurarFiltros() {
        // Cargar tipos de eventos dinámicamente desde la base de datos
        try {
            List<String> tiposEventos = tipoEventoRepository.findAll().stream()
                .map(TipoEvento::getNombre)
                .sorted()
                .toList();
            List<String> tiposConTodos = new ArrayList<>();
            tiposConTodos.add("Todos");
            tiposConTodos.addAll(tiposEventos);
            filtroTipoCombo.setItems(FXCollections.observableArrayList(tiposConTodos));
            filtroTipoCombo.setValue("Todos");
        } catch (Exception e) {
            filtroTipoCombo.setItems(FXCollections.observableArrayList("Todos"));
            filtroTipoCombo.setValue("Todos");
        }
        
        filtroEstadoCombo.setItems(FXCollections.observableArrayList(
            "Todos", "Planificados", "Activos", "Cancelados", "Finalizados"
        ));
        filtroEstadoCombo.setValue("Todos");
        
        // Agregar listeners para aplicar filtros automáticamente
        filtroTipoCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) handleFiltrarTipo();
        });
        filtroEstadoCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) handleFiltrarEstado();
        });
    }

    /**
     * Carga todos los eventos desde la base de datos.
     */
    private void cargarEventos() {
        try {
            List<Evento> eventos = eventoService.listarTodos();
            actualizarTablaYMapa(eventos, eventos.size() + " eventos encontrados");
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
            javafx.application.Platform.runLater(() -> {
                cargarMarcadoresEnMapa(eventosActuales);
            });
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
        
        // Aplicar estilos oscuros
        DialogStyler.styleDialog(dialog);

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
        DialogStyler.styleAlert(confirmacion);
        
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
            actualizarTablaYMapa(eventosFiltrados, eventosFiltrados.size() + " eventos encontrados");
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
            actualizarTablaYMapa(filtrados, filtrados.size() + " eventos del tipo " + tipoSeleccionado);
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
            EstadoEvento estado;
            switch (estadoSeleccionado) {
                case "Planificados" -> estado = EstadoEvento.PLANIFICADO;
                case "Activos" -> estado = EstadoEvento.ACTIVO;
                case "Cancelados" -> estado = EstadoEvento.CANCELADO;
                case "Finalizados" -> estado = EstadoEvento.FINALIZADO;
                default -> {
                    cargarEventos();
                    return;
                }
            }
            
            List<Evento> eventosFiltrados = eventoService.filtrarPorEstado(estado);
            actualizarTablaYMapa(eventosFiltrados, eventosFiltrados.size() + " eventos " + estadoSeleccionado.toLowerCase());
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

    private void actualizarTablaYMapa(List<Evento> eventos, String infoTexto) {
        eventosObservable = FXCollections.observableArrayList(eventos);
        if (eventosTable != null) {
            eventosTable.setItems(eventosObservable);
            eventosTable.refresh();
        }
        infoLabel.setText(infoTexto);
        actualizarMapaConLista(eventos);
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
        toggleMenu();
        
        try {
            List<Sede> sedes = sedeRepository.findAll();
            
            if (sedes.isEmpty()) {
                mostrarInfo("No hay sedes registradas");
                return;
            }
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Mis Sedes");
            dialog.setHeaderText("📍 Sedes Registradas (" + sedes.size() + ")");
            
            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 20;");
            
            for (Sede sede : sedes) {
                VBox sedeCard = new VBox(5);
                sedeCard.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 5; " +
                                 "-fx-background-color: white; -fx-background-radius: 5; -fx-padding: 15;");
                
                Label nombreLabel = new Label("🏢 " + sede.getNombre());
                nombreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                
                Label ubicacionLabel = new Label("📍 " + sede.getCiudad() + ", " + sede.getProvincia());
                Label direccionLabel = new Label("   " + sede.getDireccion());
                Label capacidadLabel = new Label("👥 Capacidad: " + sede.getCapacidad() + " personas");
                Label estadoLabel = new Label(sede.getActiva() ? "✅ Activa" : "❌ Inactiva");
                estadoLabel.setStyle(sede.getActiva() ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
                
                sedeCard.getChildren().addAll(nombreLabel, ubicacionLabel, direccionLabel, capacidadLabel, estadoLabel);
                content.getChildren().add(sedeCard);
            }
            
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400);
            scrollPane.setPrefWidth(500);
            
            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
            
        } catch (Exception e) {
            mostrarError("Error al cargar sedes: " + e.getMessage());
        }
    }

    @FXML
    private void handleMisEventos() {
        toggleMenu();
        
        try {
            List<Evento> eventos = eventoService.listarTodos();
            
            if (eventos.isEmpty()) {
                mostrarInfo("No hay eventos registrados");
                return;
            }
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Mis Eventos");
            dialog.setHeaderText("🎭 Eventos del Sistema (" + eventos.size() + ")");
            
            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 20;");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            for (Evento evento : eventos) {
                VBox eventoCard = new VBox(5);
                String borderColor;
                switch (evento.getEstado()) {
                    case PLANIFICADO -> borderColor = "#3498db";
                    case ACTIVO -> borderColor = "#27ae60";
                    case CANCELADO -> borderColor = "#e74c3c";
                    case FINALIZADO -> borderColor = "#95a5a6";
                    default -> borderColor = "#95a5a6";
                }
                
                eventoCard.setStyle("-fx-border-color: " + borderColor + "; -fx-border-width: 2; -fx-border-radius: 5; " +
                                   "-fx-background-color: white; -fx-background-radius: 5; -fx-padding: 15;");
                
                Label nombreLabel = new Label("🎭 " + evento.getNombre());
                nombreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                
                Label tipoLabel = new Label("📋 " + evento.getTipoEvento().getNombre());
                Label sedeLabel = new Label("📍 " + evento.getSede().getNombre() + " - " + evento.getSede().getCiudad());
                Label fechaLabel = new Label("📅 " + evento.getFechaInicio().format(formatter) + " - " + evento.getFechaFin().format(formatter));
                Label aforoLabel = new Label("👥 Aforo: " + evento.getAforoActual() + "/" + evento.getAforoMaximo());
                
                String estadoTexto;
                switch (evento.getEstado()) {
                    case PLANIFICADO -> estadoTexto = "📅 Planificado";
                    case ACTIVO -> estadoTexto = "✅ Activo";
                    case CANCELADO -> estadoTexto = "❌ Cancelado";
                    case FINALIZADO -> estadoTexto = "✔️ Finalizado";
                    default -> estadoTexto = "❓ Desconocido";
                }
                Label estadoLabel = new Label(estadoTexto);
                estadoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + borderColor + ";");
                
                eventoCard.getChildren().addAll(nombreLabel, tipoLabel, sedeLabel, fechaLabel, aforoLabel, estadoLabel);
                
                if (evento.getPrecioBase() != null) {
                    Label precioLabel = new Label("💰 Precio base: $" + evento.getPrecioBase());
                    precioLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    eventoCard.getChildren().add(precioLabel);
                }
                
                content.getChildren().add(eventoCard);
            }
            
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(450);
            scrollPane.setPrefWidth(600);
            
            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
            
        } catch (Exception e) {
            mostrarError("Error al cargar eventos: " + e.getMessage());
        }
    }

    @FXML
    private void handleGestionarUsuarios() {
        toggleMenu();
        
        try {
            List<Usuario> usuarios = usuarioRepository.findAll().stream()
                .filter(u -> !u.getRol().getNombre().equalsIgnoreCase("ADMIN"))
                .toList();
            
            if (usuarios.isEmpty()) {
                mostrarInfo("No hay usuarios registrados");
                return;
            }
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Gestionar Usuarios");
            dialog.setHeaderText("👥 Usuarios del Sistema (" + usuarios.size() + ")");
            
            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 20;");
            
            for (Usuario usuario : usuarios) {
                HBox usuarioCard = new HBox(15);
                usuarioCard.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 5; " +
                                    "-fx-background-color: white; -fx-background-radius: 5; -fx-padding: 10; -fx-alignment: center-left;");
                
                VBox infoBox = new VBox(3);
                infoBox.setStyle("-fx-flex-grow: 1;");
                
                Label nombreLabel = new Label("👤 " + usuario.getNombre());
                nombreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                
                Label emailLabel = new Label("📧 " + usuario.getEmail());
                Label dniLabel = new Label("🆔 " + usuario.getDni());
                Label telefonoLabel = new Label("📞 " + (usuario.getTelefono() != null ? usuario.getTelefono() : "N/A"));
                
                infoBox.getChildren().addAll(nombreLabel, emailLabel, dniLabel, telefonoLabel);
                
                VBox botonesBox = new VBox(5);
                
                Button btnEliminar = new Button("🗑️ Eliminar");
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                btnEliminar.setOnAction(e -> {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Confirmar Eliminación");
                    confirmAlert.setHeaderText("¿Eliminar usuario?");
                    confirmAlert.setContentText("¿Estás seguro de eliminar a " + usuario.getNombre() + "?");
                    
                    confirmAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                usuarioRepository.delete(usuario.getId());
                                mostrarExito("Usuario eliminado correctamente");
                                dialog.close();
                                handleGestionarUsuarios();
                            } catch (Exception ex) {
                                mostrarError("Error al eliminar usuario: " + ex.getMessage());
                            }
                        }
                    });
                });
                
                botonesBox.getChildren().add(btnEliminar);
                
                usuarioCard.getChildren().addAll(infoBox, botonesBox);
                content.getChildren().add(usuarioCard);
            }
            
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(450);
            scrollPane.setPrefWidth(600);
            
            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
            
        } catch (Exception e) {
            mostrarError("Error al cargar usuarios: " + e.getMessage());
        }
    }

    @FXML
    private void handleEstadisticas() {
        toggleMenu();
        
        try {
            // Obtener estadísticas
            List<Evento> todosEventos = eventoService.listarTodos();
            List<Compra> todasCompras = compraRepository.findAll();
            List<Entrada> todasEntradas = entradaRepository.findAll();
            List<Usuario> todosUsuarios = usuarioRepository.findAll();
            
            long eventosActivos = todosEventos.stream().filter(e -> e.getEstado() == EstadoEvento.ACTIVO).count();
            long eventosCancelados = todosEventos.stream().filter(e -> e.getEstado() == EstadoEvento.CANCELADO).count();
            long eventosFinalizados = todosEventos.stream().filter(e -> e.getEstado() == EstadoEvento.FINALIZADO).count();
            
            long totalEntradas = todasEntradas.size();
            long entradasValidadas = todasEntradas.stream().filter(e -> e.getValidada() != null && e.getValidada()).count();
            
            java.math.BigDecimal dineroTotal = todasCompras.stream()
                .map(Compra::getTotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            
            int usuariosRegistrados = todosUsuarios.size() - 1; // Sin contar admin
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Estadísticas del Sistema");
            dialog.setHeaderText("📊 Panel de Estadísticas");
            
            GridPane grid = new GridPane();
            grid.setHgap(20);
            grid.setVgap(15);
            grid.setStyle("-fx-padding: 25;");
            
            int row = 0;
            
            // Eventos
            Label eventosHeader = new Label("🎭 EVENTOS");
            eventosHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
            grid.add(eventosHeader, 0, row++, 2, 1);
            
            grid.add(new Label("Total de eventos:"), 0, row);
            Label totalEventosLabel = new Label(String.valueOf(todosEventos.size()));
            totalEventosLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            grid.add(totalEventosLabel, 1, row++);
            
            grid.add(new Label("✅ Eventos activos:"), 0, row);
            Label activosLabel = new Label(String.valueOf(eventosActivos));
            activosLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            grid.add(activosLabel, 1, row++);
            
            grid.add(new Label("❌ Eventos cancelados:"), 0, row);
            Label canceladosLabel = new Label(String.valueOf(eventosCancelados));
            canceladosLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            grid.add(canceladosLabel, 1, row++);
            
            grid.add(new Label("✔️ Eventos finalizados:"), 0, row);
            Label finalizadosLabel = new Label(String.valueOf(eventosFinalizados));
            finalizadosLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold;");
            grid.add(finalizadosLabel, 1, row++);
            
            grid.add(new Separator(), 0, row++, 2, 1);
            
            // Entradas
            Label entradasHeader = new Label("🎫 ENTRADAS");
            entradasHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
            grid.add(entradasHeader, 0, row++, 2, 1);
            
            grid.add(new Label("Total vendidas:"), 0, row);
            Label totalEntradasLabel = new Label(String.valueOf(totalEntradas));
            totalEntradasLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            grid.add(totalEntradasLabel, 1, row++);
            
            grid.add(new Label("✅ Validadas:"), 0, row);
            Label validadasLabel = new Label(String.valueOf(entradasValidadas));
            validadasLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            grid.add(validadasLabel, 1, row++);
            
            grid.add(new Separator(), 0, row++, 2, 1);
            
            // Ingresos
            Label ingresosHeader = new Label("💰 INGRESOS");
            ingresosHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
            grid.add(ingresosHeader, 0, row++, 2, 1);
            
            grid.add(new Label("Total compras:"), 0, row);
            Label totalComprasLabel = new Label(String.valueOf(todasCompras.size()));
            totalComprasLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            grid.add(totalComprasLabel, 1, row++);
            
            grid.add(new Label("Dinero generado:"), 0, row);
            Label dineroLabel = new Label("$" + dineroTotal.toString());
            dineroLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 18px;");
            grid.add(dineroLabel, 1, row++);
            
            grid.add(new Separator(), 0, row++, 2, 1);
            
            // Usuarios
            Label usuariosHeader = new Label("👥 USUARIOS");
            usuariosHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
            grid.add(usuariosHeader, 0, row++, 2, 1);
            
            grid.add(new Label("Usuarios registrados:"), 0, row);
            Label usuariosLabel = new Label(String.valueOf(usuariosRegistrados));
            usuariosLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            grid.add(usuariosLabel, 1, row++);
            
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
            
        } catch (Exception e) {
            mostrarError("Error al cargar estadísticas: " + e.getMessage());
        }
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
    private void handleAdministrarSedes() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/sedes_admin.fxml")
            );
            javafx.scene.Parent sedesRoot = loader.load();
            Stage stage = (Stage) avatarButton.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(sedesRoot);
            stage.setScene(scene);
            stage.setTitle("Sistema de Gestión de Eventos - Administrar Sedes");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            mostrarError("Error al cargar administración de sedes: " + e.getMessage());
            e.printStackTrace();
        }
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
        DialogStyler.styleAlert(alert);
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
        DialogStyler.styleAlert(alert);
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
        DialogStyler.styleAlert(alert);
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
        DialogStyler.styleAlert(alert);
        alert.showAndWait();
    }
}
