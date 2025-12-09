package com.eventos.controllers;

import com.eventos.models.Evento;
import com.eventos.models.Usuario;
import com.eventos.repositories.EventoRepository;
import com.eventos.repositories.TipoEntradaRepository;
import com.eventos.repositories.EntradaRepository;
import com.eventos.repositories.CompraRepository;
import com.eventos.services.AutenticacionService;
import com.eventos.services.CompraService;
import com.eventos.models.TipoEntrada;
import com.eventos.utils.HotReloadManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.Cursor;
import javafx.stage.Popup;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.FontWeight;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Locale;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador para la vista de búsqueda y visualización de eventos (Usuario).
 * Gestiona la búsqueda, filtrado y selección de eventos para compra.
 */
public class EventosUsuarioController {

    @FXML
    private Button menuButton;
    
    @FXML
    private TextField busquedaField;
    
    @FXML
    private Button buscarButton;
    
    @FXML
    private Button avatarButton;
    
    @FXML
    private VBox perfilMenu;
    
    @FXML
    private VBox sideMenu;
    
    @FXML
    private VBox eventosListContainer;
    
    @FXML
    private StackPane mapaContainer;
    
    private javafx.scene.web.WebView mapaWebView;
    private javafx.scene.web.WebEngine webEngine;

    private final EventoRepository eventoRepository;
    private final AutenticacionService autenticacionService;
    private final CompraService compraService;
    private final TipoEntradaRepository tipoEntradaRepository;
    private final EntradaRepository entradaRepository;
    private final CompraRepository compraRepository;
    private Usuario usuarioActual;
    private List<Evento> eventosActuales;

    /**
     * Constructor del controlador.
     */
    public EventosUsuarioController() {
        this.eventoRepository = new EventoRepository();
        this.autenticacionService = AutenticacionService.getInstance();
        this.compraService = new CompraService();
        this.tipoEntradaRepository = new TipoEntradaRepository();
        this.entradaRepository = new EntradaRepository();
        this.compraRepository = new CompraRepository();
    }

    /**
     * Inicialización automática de JavaFX.
     */
    @FXML
    private void initialize() {
        usuarioActual = autenticacionService.getUsuarioActual();
        inicializarMapa();
        cargarEventos();
        
        // Configurar búsqueda en tiempo real
        busquedaField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 3 || newValue.isEmpty()) {
                buscarEventos(newValue);
            }
        });
        
        // Configurar Hot Reload en la escena
        avatarButton.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                HotReloadManager.setupHotReloadShortcuts(newScene, stage, "/fxml/eventos_usuario.fxml");
                
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
     * Método público para recargar la vista (útil para hot reload).
     */
    @FXML
    private void handleRecargarVista() {
        Stage stage = (Stage) avatarButton.getScene().getWindow();
        HotReloadManager.reloadScene(stage, "/fxml/eventos_usuario.fxml", 
            stage.getWidth(), stage.getHeight());
    }

    /**
     * Inicializa el mapa con WebView y Leaflet.
     */
    private void inicializarMapa() {
        if (mapaContainer != null) {
            // Crear WebView
            mapaWebView = new javafx.scene.web.WebView();
            webEngine = mapaWebView.getEngine();
            
            // Cargar el archivo HTML del mapa
            String mapaPath = getClass().getResource("/html/mapa.html").toExternalForm();
            webEngine.load(mapaPath);
            
            // Agregar al container
            mapaContainer.getChildren().add(mapaWebView);
            
            // Esperar a que cargue el mapa
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    System.out.println("WebEngine cargado exitosamente");
                    
                    // Exponer el controller a JavaScript
                    netscape.javascript.JSObject window = (netscape.javascript.JSObject) webEngine.executeScript("window");
                    window.setMember("javaController", this);
                    
                    // Dar tiempo a Leaflet para inicializar completamente
                    javafx.application.Platform.runLater(() -> {
                        try {
                            Thread.sleep(500); // Esperar 500ms
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        
                        if (eventosActuales != null && !eventosActuales.isEmpty()) {
                            System.out.println("Intentando cargar marcadores después del delay...");
                            cargarMarcadoresEnMapa(eventosActuales);
                        }
                    });
                }
            });
        }
    }
    
    /**
     * Carga todos los eventos disponibles.
     */
    private void cargarEventos() {
        try {
            eventosActuales = eventoRepository.findAll();
            System.out.println("Eventos cargados: " + eventosActuales.size());
            mostrarEventos(eventosActuales);
            
            // Cargar marcadores en el mapa si ya está inicializado
            if (webEngine != null) {
                System.out.println("Cargando marcadores desde cargarEventos()...");
                cargarMarcadoresEnMapa(eventosActuales);
            }
        } catch (Exception e) {
            mostrarError("Error al cargar eventos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Carga los marcadores de eventos en el mapa usando JavaScript/Leaflet.
     */
    private void cargarMarcadoresEnMapa(List<Evento> eventos) {
        if (webEngine == null) {
            System.out.println("WebEngine es null, no se pueden cargar marcadores");
            return;
        }
        
        System.out.println("Cargando " + eventos.size() + " eventos en el mapa");
        
        // Limpiar marcadores anteriores
        webEngine.executeScript("if (window.limpiarMarcadores) window.limpiarMarcadores();");
        
        // Agregar cada evento al mapa
        int marcadoresAgregados = 0;
        for (Evento evento : eventos) {
            if (evento.getSede() != null && 
                evento.getSede().getLatitud() != null && 
                evento.getSede().getLongitud() != null) {
                
                double lat = evento.getSede().getLatitud();
                double lng = evento.getSede().getLongitud();
                String nombre = escaparJS(evento.getNombre());
                String sede = escaparJS(evento.getSede().getNombre());
                String fecha = escaparJS(evento.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                String categoria = escaparJS(evento.getTipoEvento().getNombre());
                Long id = evento.getId();
                
                String script = String.format(Locale.US,
                    "if (window.agregarMarcador) { window.agregarMarcador(%f, %f, '%s', '%s', '%s', '%s', %d); }",
                    lat, lng, nombre, sede, fecha, categoria, id
                );
                
                try {
                    webEngine.executeScript(script);
                    marcadoresAgregados++;
                } catch (Exception e) {
                    System.err.println("Error agregando marcador: " + e.getMessage());
                }
            }
        }
        
        System.out.println("Marcadores agregados: " + marcadoresAgregados);
        
        // Forzar repaint del mapa después de agregar todos los marcadores
        webEngine.executeScript("if (window.forzarRepaint) window.forzarRepaint();");
    }
    
    /**
     * Escapa comillas para JavaScript.
     */
    private String escaparJS(String texto) {
        if (texto == null) return "";
        return texto.replace("'", "\\'").replace("\"", "\\\"").replace("\n", " ");
    }
    
    /**
     * Método llamado desde JavaScript cuando se hace click en "Ver Detalles".
     */
    public void seleccionarEventoDesdeJS(int eventoId) {
        seleccionarEvento(eventoId);
    }
    
    /**
     * Método llamado desde JavaScript cuando se selecciona un evento en el mapa.
     */
    public void seleccionarEvento(int eventoId) {
        // Encontrar el evento y mostrarlo
        Evento evento = eventosActuales.stream()
            .filter(e -> e.getId().equals((long) eventoId))
            .findFirst()
            .orElse(null);
        
        if (evento != null) {
            handleVerDetalles(evento);
        }
    }

    /**
     * Muestra los eventos en la lista.
     */
    private void mostrarEventos(List<Evento> eventos) {
        eventosListContainer.getChildren().clear();
        
        if (eventos.isEmpty()) {
            Label noEventos = new Label("No se encontraron eventos");
            noEventos.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");
            eventosListContainer.getChildren().add(noEventos);
            return;
        }

        for (Evento evento : eventos) {
            VBox eventoCard = crearEventoCard(evento);
            eventosListContainer.getChildren().add(eventoCard);
        }
    }

    /**
     * Crea una tarjeta visual para un evento.
     */
    private VBox crearEventoCard(Evento evento) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; " +
                     "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; " +
                     "-fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Nombre del evento
        Label nombreLabel = new Label(evento.getNombre());
        nombreLabel.setFont(Font.font("System Bold", 16));
        nombreLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Tipo de evento
        Label tipoLabel = new Label("📍 " + evento.getTipoEvento().getNombre());
        tipoLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px;");

        // Fecha y hora
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label fechaLabel = new Label("📅 " + evento.getFechaInicio().format(formatter));
        fechaLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        // Sede
        Label sedeLabel = new Label("🏢 " + evento.getSede().getNombre());
        sedeLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        // Descripción (resumida)
        if (evento.getDescripcion() != null && !evento.getDescripcion().isEmpty()) {
            String descripcionCorta = evento.getDescripcion().length() > 100 
                ? evento.getDescripcion().substring(0, 100) + "..." 
                : evento.getDescripcion();
            Label descripcionLabel = new Label(descripcionCorta);
            descripcionLabel.setWrapText(true);
            descripcionLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");
            card.getChildren().add(descripcionLabel);
        }

        // Botón de ver detalles
        Button verDetallesBtn = new Button("Ver Detalles y Comprar");
        verDetallesBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                               "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        verDetallesBtn.setOnAction(e -> handleVerDetalles(evento));

        HBox botonesBox = new HBox(verDetallesBtn);
        botonesBox.setStyle("-fx-alignment: center-right;");

        card.getChildren().addAll(nombreLabel, tipoLabel, fechaLabel, sedeLabel, botonesBox);

        return card;
    }

    /**
     * Maneja el clic en "Ver Detalles" de un evento.
     */
    private void handleVerDetalles(Evento evento) {
        if (usuarioActual == null) {
            mostrarError("Debes iniciar sesión para comprar");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Comprar entradas");
        dialog.setHeaderText(evento.getNombre());

        ButtonType comprarBtn = new ButtonType("Comprar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(comprarBtn, ButtonType.CANCEL);

        ChoiceBox<TipoEntrada> tipoChoice = new ChoiceBox<>();
        Spinner<Integer> cantidadSpinner = new Spinner<>(1, 10, 1);
        Label totalLabel = new Label("Total: -");
        Label precioUnitLabel = new Label();

        try {
            List<TipoEntrada> tipos = tipoEntradaRepository.findAll();
            tipoChoice.getItems().addAll(tipos);
            if (!tipos.isEmpty()) {
                tipoChoice.setValue(tipos.get(0));
            }
        } catch (Exception e) {
            mostrarError("No se pudieron cargar los tipos de entrada");
            return;
        }

        tipoChoice.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> actualizarTotales(sel, cantidadSpinner.getValue(), totalLabel, precioUnitLabel));
        cantidadSpinner.valueProperty().addListener((obs, old, val) -> actualizarTotales(tipoChoice.getValue(), val, totalLabel, precioUnitLabel));
        actualizarTotales(tipoChoice.getValue(), cantidadSpinner.getValue(), totalLabel, precioUnitLabel);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Tipo de entrada:"), 0, 0);
        grid.add(tipoChoice, 1, 0);
        grid.add(new Label("Cantidad:"), 0, 1);
        grid.add(cantidadSpinner, 1, 1);
        grid.add(new Label("Precio unitario:"), 0, 2);
        grid.add(precioUnitLabel, 1, 2);
        grid.add(totalLabel, 1, 3);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> dialogButton == comprarBtn ? comprarBtn : null);
        dialog.showAndWait().ifPresent(result -> {
            TipoEntrada sel = tipoChoice.getValue();
            int cantidad = cantidadSpinner.getValue();
            if (sel == null) {
                mostrarError("Selecciona un tipo de entrada");
                return;
            }
            try {
                compraService.procesarCompra(usuarioActual.getId(), evento.getId(), sel.getId(), cantidad, "Tarjeta");
                mostrarInfo("Compra realizada. Código de confirmación generado.");
                cargarEventos();
            } catch (Exception e) {
                mostrarError("No se pudo completar la compra: " + e.getMessage());
            }
        });
    }

    private void actualizarTotales(TipoEntrada tipo, int cantidad, Label totalLabel, Label precioUnitLabel) {
        if (tipo == null) {
            totalLabel.setText("Total: -");
            precioUnitLabel.setText("-");
            return;
        }
        precioUnitLabel.setText("$ " + tipo.getPrecio());
        totalLabel.setText("Total: $ " + tipo.getPrecio().multiply(java.math.BigDecimal.valueOf(cantidad)));
    }

    /**
     * Busca eventos por texto.
     */
    private void buscarEventos(String textoBusqueda) {
        if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
            mostrarEventos(eventosActuales);
            return;
        }

        List<Evento> eventosFiltrados = eventosActuales.stream()
            .filter(e -> e.getNombre().toLowerCase().contains(textoBusqueda.toLowerCase()) ||
                        e.getDescripcion().toLowerCase().contains(textoBusqueda.toLowerCase()))
            .toList();

        mostrarEventos(eventosFiltrados);
    }

    // ========== MÉTODOS DE ACCIONES FXML ==========

    @FXML
    private void handleBuscar() {
        buscarEventos(busquedaField.getText());
    }

    @FXML
    private void toggleMenu() {
        boolean visible = sideMenu.isVisible();
        sideMenu.setVisible(!visible);
        sideMenu.setManaged(!visible);
    }

    @FXML
    private void handleFiltrarEventos() {
        // TODO: Implementar filtro por tipo de evento
        mostrarInfo("Filtro por tipo de evento en desarrollo");
    }

    @FXML
    private void handleFiltrarFechas() {
        // TODO: Implementar filtro por fechas
        mostrarInfo("Filtro por fechas en desarrollo");
    }

    @FXML
    private void handleFiltrarPrecio() {
        // TODO: Implementar filtro por precio
        mostrarInfo("Filtro por precio en desarrollo");
    }

    @FXML
    private void handleLocalizacion() {
        // TODO: Implementar filtro por localización
        mostrarInfo("Filtro por localización en desarrollo");
    }

    @FXML
    private void handleInicio() {
        cargarEventos();
        toggleMenu();
    }

    @FXML
    private void handleMisEntradas() {
        if (usuarioActual == null) {
            mostrarError("Debes iniciar sesión para ver tus entradas");
            return;
        }

        List<com.eventos.models.Compra> compras = compraRepository.findByUsuario(usuarioActual.getId());
        VBox lista = new VBox(12);
        lista.setStyle("-fx-padding: 10;");

        if (compras.isEmpty()) {
            lista.getChildren().add(new Label("No tienes compras todavía."));
        } else {
            for (com.eventos.models.Compra compra : compras) {
                Label cabecera = new Label("Compra " + compra.getCodigoConfirmacion() + " - " + compra.getFechaCompra());
                cabecera.setStyle("-fx-font-weight: bold;");
                lista.getChildren().add(cabecera);

                List<com.eventos.models.Entrada> entradas = entradaRepository.findByCompra(compra.getId());
                for (com.eventos.models.Entrada entrada : entradas) {
                    lista.getChildren().add(crearTarjetaEntrada(entrada));
                }
            }
        }

        ScrollPane scroll = new ScrollPane(lista);
        scroll.setFitToWidth(true);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Mis entradas");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setContent(scroll);
        dialog.showAndWait();
        toggleMenu();
    }

    @FXML
    private void handleHistorial() {
        // TODO: Implementar historial de compras
        mostrarInfo("Historial de compras en desarrollo");
        toggleMenu();
    }

    private HBox crearTarjetaEntrada(com.eventos.models.Entrada entrada) {
        HBox card = new HBox(12);
        card.setStyle("-fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 6; -fx-background-radius: 6;");

        VBox info = new VBox(4);
        info.getChildren().add(new Label("Evento: " + entrada.getEvento().getNombre()));
        info.getChildren().add(new Label("Tipo: " + entrada.getTipoEntrada().getNombre()));
        info.getChildren().add(new Label("Número: " + entrada.getNumeroEntrada()));
        info.getChildren().add(new Label("Estado: " + (Boolean.TRUE.equals(entrada.getValidada()) ? "Validada" : "Pendiente")));

        ImageView qrView = new ImageView();
        qrView.setFitWidth(100);
        qrView.setFitHeight(100);
        qrView.setPreserveRatio(true);
        if (entrada.getCodigoQR() != null) {
            try {
                byte[] bytes = Base64.getDecoder().decode(entrada.getCodigoQR());
                qrView.setImage(new Image(new ByteArrayInputStream(bytes)));
            } catch (IllegalArgumentException ignored) {
                // Si el QR no es válido, dejar vacío
            }
        }

        card.getChildren().addAll(qrView, info);
        return card;
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
        
        if (usuarioActual == null) {
            mostrarError("No hay sesión activa");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mi Perfil");
        alert.setHeaderText("Información del Usuario");
        alert.setContentText(
            "Nombre: " + usuarioActual.getNombre() + "\n" +
            "Email: " + usuarioActual.getEmail() + "\n" +
            "DNI: " + usuarioActual.getDni() + "\n" +
            "Rol: " + usuarioActual.getRol().getNombre()
        );
        alert.showAndWait();
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
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cerrar Sesión");
        confirmacion.setHeaderText("¿Estás seguro?");
        confirmacion.setContentText("¿Deseas cerrar sesión?");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                autenticacionService.logout();
                volverAlLogin();
            }
        });
    }

    /**
     * Vuelve a la pantalla de login.
     */
    private void volverAlLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) menuButton.getScene().getWindow();
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
