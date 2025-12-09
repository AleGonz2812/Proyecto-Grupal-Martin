package com.eventos.controllers;

import com.eventos.models.Evento;
import com.eventos.models.Usuario;
import com.eventos.models.Compra;
import com.eventos.models.Entrada;
import com.eventos.models.EstadoCompra;
import com.eventos.repositories.EventoRepository;
import com.eventos.repositories.TipoEntradaRepository;
import com.eventos.repositories.EntradaRepository;
import com.eventos.repositories.CompraRepository;
import com.eventos.services.AutenticacionService;
import com.eventos.services.CompraService;
import com.eventos.services.PagoService;
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
import java.math.BigDecimal;

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
        
        // Verificar disponibilidad del evento
        if (!evento.hayDisponibilidad()) {
            mostrarError("Este evento no tiene entradas disponibles");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Comprar entradas - " + evento.getNombre());
        dialog.setHeaderText("🎫 Información del Evento");

        ButtonType comprarBtn = new ButtonType("Comprar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(comprarBtn, ButtonType.CANCEL);

        // Componentes del formulario
        ChoiceBox<TipoEntrada> tipoChoice = new ChoiceBox<>();
        Spinner<Integer> cantidadSpinner = new Spinner<>(1, 10, 1);
        cantidadSpinner.setEditable(true);
        Label totalLabel = new Label("Total: $0.00");
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        Label precioUnitLabel = new Label("$0.00");
        Label disponibilidadLabel = new Label();
        Label descripcionTipoLabel = new Label();
        descripcionTipoLabel.setWrapText(true);
        descripcionTipoLabel.setMaxWidth(300);
        descripcionTipoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px; -fx-font-style: italic;");

        // Cargar tipos de entrada
        try {
            List<TipoEntrada> tipos = tipoEntradaRepository.findAll().stream()
                .filter(t -> t.getActivo() != null && t.getActivo())
                .toList();
            
            if (tipos.isEmpty()) {
                mostrarError("No hay tipos de entrada disponibles para este evento");
                return;
            }
            
            tipoChoice.getItems().addAll(tipos);
            tipoChoice.setValue(tipos.get(0));
        } catch (Exception e) {
            mostrarError("No se pudieron cargar los tipos de entrada: " + e.getMessage());
            return;
        }

        // Actualizar información del aforo disponible
        int aforoDisponible = evento.getAforoMaximo() - evento.getAforoActual();
        disponibilidadLabel.setText("Disponibles: " + aforoDisponible + " de " + evento.getAforoMaximo());
        disponibilidadLabel.setStyle("-fx-text-fill: " + (aforoDisponible < 10 ? "#e74c3c" : "#3498db") + "; -fx-font-weight: bold;");
        
        // Actualizar límite del spinner según disponibilidad
        cantidadSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Math.min(10, aforoDisponible), 1));

        // Listeners para actualizar totales
        tipoChoice.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            actualizarTotales(sel, cantidadSpinner.getValue(), totalLabel, precioUnitLabel);
            if (sel != null && sel.getDescripcion() != null && !sel.getDescripcion().isEmpty()) {
                descripcionTipoLabel.setText("ℹ️ " + sel.getDescripcion());
                if (sel.getBeneficios() != null && !sel.getBeneficios().isEmpty()) {
                    descripcionTipoLabel.setText(descripcionTipoLabel.getText() + "\n✨ " + sel.getBeneficios());
                }
            } else {
                descripcionTipoLabel.setText("");
            }
        });
        
        cantidadSpinner.valueProperty().addListener((obs, old, val) -> {
            actualizarTotales(tipoChoice.getValue(), val, totalLabel, precioUnitLabel);
        });
        
        // Actualizar valores iniciales
        actualizarTotales(tipoChoice.getValue(), cantidadSpinner.getValue(), totalLabel, precioUnitLabel);
        TipoEntrada inicial = tipoChoice.getValue();
        if (inicial != null && inicial.getDescripcion() != null && !inicial.getDescripcion().isEmpty()) {
            descripcionTipoLabel.setText("ℹ️ " + inicial.getDescripcion());
            if (inicial.getBeneficios() != null && !inicial.getBeneficios().isEmpty()) {
                descripcionTipoLabel.setText(descripcionTipoLabel.getText() + "\n✨ " + inicial.getBeneficios());
            }
        }

        // Crear grid con información
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");
        
        // Información del evento
        Label eventoInfo = new Label("📅 " + evento.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        eventoInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");
        Label sedeInfo = new Label("📍 " + evento.getSede().getNombre() + " - " + evento.getSede().getCiudad());
        sedeInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");
        
        grid.add(eventoInfo, 0, 0, 2, 1);
        grid.add(sedeInfo, 0, 1, 2, 1);
        grid.add(disponibilidadLabel, 0, 2, 2, 1);
        
        Separator separator = new Separator();
        grid.add(separator, 0, 3, 2, 1);
        
        grid.add(new Label("Tipo de entrada:"), 0, 4);
        grid.add(tipoChoice, 1, 4);
        grid.add(descripcionTipoLabel, 0, 5, 2, 1);
        grid.add(new Label("Cantidad:"), 0, 6);
        grid.add(cantidadSpinner, 1, 6);
        grid.add(new Label("Precio unitario:"), 0, 7);
        grid.add(precioUnitLabel, 1, 7);
        
        Separator separator2 = new Separator();
        grid.add(separator2, 0, 8, 2, 1);
        
        grid.add(new Label("TOTAL:"), 0, 9);
        grid.add(totalLabel, 1, 9);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setMinWidth(450);

        dialog.setResultConverter(dialogButton -> dialogButton == comprarBtn ? comprarBtn : null);
        dialog.showAndWait().ifPresent(result -> {
            TipoEntrada sel = tipoChoice.getValue();
            int cantidad = cantidadSpinner.getValue();
            
            if (sel == null) {
                mostrarError("Selecciona un tipo de entrada");
                return;
            }
            
            if (cantidad > aforoDisponible) {
                mostrarError("No hay suficientes entradas disponibles. Solo quedan " + aforoDisponible);
                return;
            }
            
            // Mostrar diálogo de pago
            BigDecimal total = sel.getPrecio().multiply(BigDecimal.valueOf(cantidad));
            mostrarDialogoPago(evento, sel, cantidad, total);
        });
    }

    /**
     * Muestra el diálogo de pago con validación de tarjeta.
     */
    private void mostrarDialogoPago(Evento evento, TipoEntrada tipoEntrada, int cantidad, BigDecimal total) {
        Dialog<ButtonType> pagoDialog = new Dialog<>();
        pagoDialog.setTitle("Procesamiento de Pago");
        pagoDialog.setHeaderText("💳 Ingresa los datos de tu tarjeta");

        ButtonType pagarBtn = new ButtonType("Pagar $" + total, ButtonBar.ButtonData.OK_DONE);
        ButtonType testCardsBtn = new ButtonType("Ver tarjetas de prueba", ButtonBar.ButtonData.HELP);
        pagoDialog.getDialogPane().getButtonTypes().addAll(pagarBtn, testCardsBtn, ButtonType.CANCEL);

        // Campos del formulario
        TextField numeroTarjetaField = new TextField();
        numeroTarjetaField.setPromptText("1234 5678 9012 3456");
        Label tipoTarjetaLabel = new Label("");
        tipoTarjetaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        
        TextField expiracionField = new TextField();
        expiracionField.setPromptText("MM/YY");
        expiracionField.setMaxWidth(100);
        
        TextField cvvField = new TextField();
        cvvField.setPromptText("123");
        cvvField.setMaxWidth(80);
        
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre como aparece en la tarjeta");

        // Validación en tiempo real del número de tarjeta
        numeroTarjetaField.textProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null && !nuevo.isEmpty()) {
                String tipoTarjeta = PagoService.detectarTipoTarjeta(nuevo);
                if (!tipoTarjeta.equals("Desconocido")) {
                    tipoTarjetaLabel.setText("🔹 " + tipoTarjeta);
                } else {
                    tipoTarjetaLabel.setText("");
                }
            } else {
                tipoTarjetaLabel.setText("");
            }
        });

        // Grid del formulario
        GridPane pagoGrid = new GridPane();
        pagoGrid.setHgap(10);
        pagoGrid.setVgap(12);
        pagoGrid.setStyle("-fx-padding: 20;");

        Label totalInfoLabel = new Label("Total a pagar: $" + total);
        totalInfoLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label detalleLabel = new Label(cantidad + " entrada(s) - " + tipoEntrada.getNombre());
        detalleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        pagoGrid.add(totalInfoLabel, 0, 0, 2, 1);
        pagoGrid.add(detalleLabel, 0, 1, 2, 1);
        
        Separator sep1 = new Separator();
        pagoGrid.add(sep1, 0, 2, 2, 1);

        pagoGrid.add(new Label("Número de tarjeta:"), 0, 3);
        pagoGrid.add(numeroTarjetaField, 1, 3);
        pagoGrid.add(tipoTarjetaLabel, 1, 4);

        HBox expirationCvvBox = new HBox(10);
        VBox expBox = new VBox(5);
        expBox.getChildren().addAll(new Label("Expiración:"), expiracionField);
        VBox cvvBox = new VBox(5);
        Label cvvLabelWithHelp = new Label("CVV:");
        cvvLabelWithHelp.setTooltip(new javafx.scene.control.Tooltip("Los 3 dígitos en el reverso de tu tarjeta\n(4 dígitos para American Express)"));
        cvvBox.getChildren().addAll(cvvLabelWithHelp, cvvField);
        expirationCvvBox.getChildren().addAll(expBox, cvvBox);
        
        pagoGrid.add(expirationCvvBox, 0, 5, 2, 1);

        pagoGrid.add(new Label("Nombre del titular:"), 0, 6);
        pagoGrid.add(nombreField, 1, 6);

        pagoDialog.getDialogPane().setContent(pagoGrid);
        pagoDialog.getDialogPane().setMinWidth(500);

        // Manejar botón de tarjetas de prueba
        Button testCardsButton = (Button) pagoDialog.getDialogPane().lookupButton(testCardsBtn);
        testCardsButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            mostrarTarjetasPrueba();
        });

        // Procesar resultado
        pagoDialog.setResultConverter(dialogButton -> {
            if (dialogButton == pagarBtn) {
                return pagarBtn;
            }
            return null;
        });

        pagoDialog.showAndWait().ifPresent(result -> {
            String numeroTarjeta = numeroTarjetaField.getText().trim();
            String expiracion = expiracionField.getText().trim();
            String cvv = cvvField.getText().trim();
            String nombre = nombreField.getText().trim();

            // Validar campos vacíos
            if (numeroTarjeta.isEmpty() || expiracion.isEmpty() || cvv.isEmpty() || nombre.isEmpty()) {
                mostrarError("❌ Todos los campos son obligatorios");
                return;
            }

            // Procesar pago con animación
            procesarPagoConAnimacion(numeroTarjeta, expiracion, cvv, nombre, total, evento, tipoEntrada, cantidad);
        });
    }

    /**
     * Procesa el pago mostrando una animación de carga.
     */
    private void procesarPagoConAnimacion(String numeroTarjeta, String expiracion, String cvv, 
                                          String nombre, BigDecimal monto, Evento evento, 
                                          TipoEntrada tipoEntrada, int cantidad) {
        // Crear ventana de procesamiento
        Dialog<Void> processingDialog = new Dialog<>();
        processingDialog.setTitle("Procesando Pago");
        processingDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Button closeButton = (Button) processingDialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setVisible(false);

        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 30; -fx-alignment: center;");
        
        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(60, 60);
        
        Label messageLabel = new Label("Validando información de pago...");
        messageLabel.setStyle("-fx-font-size: 14px;");
        
        content.getChildren().addAll(progress, messageLabel);
        processingDialog.getDialogPane().setContent(content);
        processingDialog.getDialogPane().setMinWidth(400);
        processingDialog.getDialogPane().setMinHeight(200);

        processingDialog.show();

        // Procesar en thread separado
        new Thread(() -> {
            try {
                PagoService.ResultadoPago resultado = PagoService.procesarPago(
                    numeroTarjeta, expiracion, cvv, nombre, monto.doubleValue()
                );

                javafx.application.Platform.runLater(() -> {
                    processingDialog.close();
                    
                    if (resultado.isExitoso()) {
                        // Pago exitoso - procesar compra
                        try {
                            String tipoTarjeta = PagoService.detectarTipoTarjeta(numeroTarjeta);
                            compraService.procesarCompra(
                                usuarioActual.getId(), 
                                evento.getId(), 
                                tipoEntrada.getId(), 
                                cantidad, 
                                tipoTarjeta
                            );
                            
                            mostrarInfo("✅ PAGO APROBADO\n\n" +
                                       "Número de autorización: " + resultado.getNumeroAutorizacion() + "\n" +
                                       "Número de transacción: " + resultado.getNumeroTransaccion() + "\n\n" +
                                       "Se han generado " + cantidad + " entrada(s) con código QR.\n" +
                                       "Puedes ver tus entradas en 'Historial de Compras'.");
                            cargarEventos(); // Recargar para actualizar aforo
                        } catch (Exception e) {
                            mostrarError("❌ El pago fue aprobado pero hubo un error al generar las entradas:\n" + e.getMessage());
                        }
                    } else {
                        // Pago rechazado
                        mostrarError("❌ PAGO RECHAZADO\n\n" + resultado.getMensaje());
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    processingDialog.close();
                    mostrarError("❌ Error en el procesamiento: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Muestra las tarjetas de prueba disponibles.
     */
    private void mostrarTarjetasPrueba() {
        String[] tarjetas = PagoService.obtenerTarjetasPrueba();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tarjetas de Prueba");
        alert.setHeaderText("🎯 Usa estas tarjetas para probar el sistema");
        
        StringBuilder content = new StringBuilder();
        content.append("Todas las tarjetas tienen:\n");
        content.append("• CVV: 123 (456 para Amex)\n");
        content.append("• Fecha: 12/25 o posterior\n");
        content.append("• Nombre: Cualquier nombre\n\n");
        content.append("═══════════════════════════\n\n");
        
        for (String tarjeta : tarjetas) {
            String tipo = PagoService.detectarTipoTarjeta(tarjeta);
            content.append("🔹 ").append(tipo).append("\n");
            content.append("   ").append(tarjeta).append("\n\n");
        }
        
        content.append("═══════════════════════════\n");
        content.append("Tasa de éxito: 95%");
        
        TextArea textArea = new TextArea(content.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(15);
        textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setMinWidth(450);
        alert.showAndWait();
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
     * Busca eventos por nombre del evento únicamente.
     */
    private void buscarEventos(String textoBusqueda) {
        if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
            mostrarEventos(eventosActuales);
            return;
        }

        String busqueda = textoBusqueda.toLowerCase().trim();
        List<Evento> eventosFiltrados = eventosActuales.stream()
            .filter(e -> e.getNombre().toLowerCase().contains(busqueda))
            .toList();

        mostrarEventos(eventosFiltrados);
        
        if (eventosFiltrados.isEmpty()) {
            mostrarInfo("No se encontraron eventos con el nombre: \"" + textoBusqueda + "\"");
        }
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
        // Obtener tipos de eventos únicos
        List<String> tiposUnicos = eventosActuales.stream()
            .filter(e -> e.getTipoEvento() != null)
            .map(e -> e.getTipoEvento().getNombre())
            .distinct()
            .sorted()
            .toList();
        
        if (tiposUnicos.isEmpty()) {
            mostrarInfo("No hay tipos de eventos disponibles");
            return;
        }
        
        // Crear diálogo de selección
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Todos", tiposUnicos);
        dialog.setTitle("Filtrar por Tipo de Evento");
        dialog.setHeaderText("Selecciona un tipo de evento");
        dialog.setContentText("Tipo:");
        
        dialog.showAndWait().ifPresent(tipoSeleccionado -> {
            if ("Todos".equals(tipoSeleccionado)) {
                mostrarEventos(eventosActuales);
            } else {
                List<Evento> eventosFiltrados = eventosActuales.stream()
                    .filter(e -> e.getTipoEvento() != null && 
                                e.getTipoEvento().getNombre().equals(tipoSeleccionado))
                    .toList();
                mostrarEventos(eventosFiltrados);
                mostrarInfo("Mostrando " + eventosFiltrados.size() + " evento(s) de tipo: " + tipoSeleccionado);
            }
        });
    }

    @FXML
    private void handleFiltrarFechas() {
        // Crear diálogo con DatePicker
        Dialog<java.time.LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Filtrar por Fecha");
        dialog.setHeaderText("Selecciona la fecha máxima para los eventos");
        
        // Configurar botones
        ButtonType filtrarButtonType = new ButtonType("Filtrar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(filtrarButtonType, ButtonType.CANCEL);
        
        // Crear DatePicker
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(java.time.LocalDate.now().plusDays(30)); // Por defecto 30 días
        datePicker.setPromptText("dd/MM/yyyy");
        
        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        Label instruccion = new Label("Los eventos se mostrarán desde hoy hasta la fecha seleccionada:");
        instruccion.setWrapText(true);
        
        grid.add(instruccion, 0, 0);
        grid.add(new Label("Fecha máxima:"), 0, 1);
        grid.add(datePicker, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == filtrarButtonType) {
                return datePicker.getValue();
            }
            return null;
        });
        
        // Procesar resultado
        dialog.showAndWait().ifPresent(fechaMaxima -> {
            java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
            java.time.LocalDateTime fechaLimite = fechaMaxima.atTime(23, 59, 59);
            
            if (fechaLimite.isBefore(ahora)) {
                mostrarError("La fecha seleccionada debe ser igual o posterior a hoy");
                return;
            }
            
            List<Evento> eventosFiltrados = eventosActuales.stream()
                .filter(e -> e.getFechaInicio() != null && 
                            !e.getFechaInicio().isBefore(ahora) &&
                            !e.getFechaInicio().isAfter(fechaLimite))
                .toList();
            
            mostrarEventos(eventosFiltrados);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            mostrarInfo("Mostrando " + eventosFiltrados.size() + 
                       " evento(s) hasta el " + fechaMaxima.format(formatter));
        });
    }

    @FXML
    private void handleFiltrarPrecio() {
        TextInputDialog dialog = new TextInputDialog("1000");
        dialog.setTitle("Filtrar por Precio");
        dialog.setHeaderText("Precio máximo");
        dialog.setContentText("Ingrese el precio máximo que desea pagar:");
        
        dialog.showAndWait().ifPresent(precioTexto -> {
            try {
                // Validar que sea un número válido
                java.math.BigDecimal precioMaximo = new java.math.BigDecimal(precioTexto.trim());
                
                if (precioMaximo.compareTo(java.math.BigDecimal.ZERO) < 0) {
                    mostrarError("El precio debe ser mayor o igual a 0");
                    return;
                }
                
                // Filtrar eventos por precio
                List<Evento> eventosFiltrados = eventosActuales.stream()
                    .filter(e -> e.getPrecioBase() != null && 
                                e.getPrecioBase().compareTo(precioMaximo) <= 0)
                    .toList();
                
                mostrarEventos(eventosFiltrados);
                mostrarInfo("Mostrando " + eventosFiltrados.size() + 
                           " evento(s) con precio hasta $" + precioMaximo);
                           
            } catch (NumberFormatException e) {
                mostrarError("Por favor, ingrese un número válido");
            }
        });
    }

    @FXML
    private void handleLocalizacion() {
        // Obtener ciudades únicas
        List<String> ciudadesUnicas = eventosActuales.stream()
            .filter(e -> e.getSede() != null && e.getSede().getCiudad() != null)
            .map(e -> e.getSede().getCiudad())
            .distinct()
            .sorted()
            .toList();
        
        if (ciudadesUnicas.isEmpty()) {
            mostrarInfo("No hay ciudades disponibles");
            return;
        }
        
        // Crear diálogo de selección
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Todas", ciudadesUnicas);
        dialog.setTitle("Filtrar por Localización");
        dialog.setHeaderText("Selecciona una ciudad");
        dialog.setContentText("Ciudad:");
        
        dialog.showAndWait().ifPresent(ciudadSeleccionada -> {
            if ("Todas".equals(ciudadSeleccionada)) {
                mostrarEventos(eventosActuales);
            } else {
                List<Evento> eventosFiltrados = eventosActuales.stream()
                    .filter(e -> e.getSede() != null && 
                                e.getSede().getCiudad() != null &&
                                e.getSede().getCiudad().equals(ciudadSeleccionada))
                    .toList();
                mostrarEventos(eventosFiltrados);
                mostrarInfo("Mostrando " + eventosFiltrados.size() + " evento(s) en: " + ciudadSeleccionada);
            }
        });
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
        toggleMenu();
        
        if (usuarioActual == null) {
            mostrarError("Debes iniciar sesión");
            return;
        }
        
        try {
            List<Compra> compras = compraRepository.findAll().stream()
                .filter(c -> c.getUsuario().getId().equals(usuarioActual.getId()))
                .sorted((c1, c2) -> c2.getFechaCompra().compareTo(c1.getFechaCompra()))
                .toList();
            
            if (compras.isEmpty()) {
                mostrarInfo("No tienes compras registradas");
                return;
            }
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Historial de Compras");
            dialog.setHeaderText("🎫 Mis Compras (" + compras.size() + ")");
            
            VBox content = new VBox(12);
            content.setStyle("-fx-padding: 20;");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            for (Compra compra : compras) {
                VBox compraCard = new VBox(8);
                compraCard.setStyle("-fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 8; " +
                                   "-fx-background-color: #ecf0f1; -fx-background-radius: 8; -fx-padding: 15;");
                
                // Encabezado de la compra
                HBox header = new HBox(10);
                header.setStyle("-fx-alignment: center-left;");
                
                Label codigoLabel = new Label("📋 Compra: " + compra.getCodigoConfirmacion());
                codigoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                
                Label fechaLabel = new Label("📅 " + compra.getFechaCompra().format(formatter));
                fechaLabel.setStyle("-fx-text-fill: #7f8c8d;");
                
                Label estadoLabel = new Label(compra.getEstado().name());
                estadoLabel.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                                    "-fx-padding: 3 8; -fx-border-radius: 10; -fx-background-radius: 10;");
                
                header.getChildren().addAll(codigoLabel, fechaLabel, estadoLabel);
                
                // Detalles de entradas
                VBox entradasBox = new VBox(5);
                List<Entrada> entradas = entradaRepository.findAll().stream()
                    .filter(e -> e.getCompra().getId().equals(compra.getId()))
                    .toList();
                
                for (Entrada entrada : entradas) {
                    HBox entradaRow = new HBox(10);
                    entradaRow.setStyle("-fx-alignment: center-left; -fx-padding: 5; -fx-background-color: white; " +
                                       "-fx-border-radius: 5; -fx-background-radius: 5;");
                    
                    Label numLabel = new Label("🎫 " + entrada.getNumeroEntrada());
                    numLabel.setStyle("-fx-font-size: 11px; -fx-font-family: monospace;");
                    
                    Label eventoLabel = new Label(entrada.getEvento().getNombre());
                    eventoLabel.setStyle("-fx-font-weight: bold;");
                    
                    Label tipoLabel = new Label(entrada.getTipoEntrada().getNombre() + " - $" + entrada.getTipoEntrada().getPrecio());
                    tipoLabel.setStyle("-fx-text-fill: #3498db;");
                    
                    Label validadaLabel = new Label(entrada.getValidada() ? "✅ Validada" : "⏳ Pendiente");
                    validadaLabel.setStyle(entrada.getValidada() ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e67e22;");
                    
                    Button btnQR = new Button("Ver QR");
                    btnQR.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 10px;");
                    btnQR.setOnAction(e -> mostrarQR(entrada));
                    
                    entradaRow.getChildren().addAll(numLabel, eventoLabel, tipoLabel, validadaLabel, btnQR);
                    entradasBox.getChildren().add(entradaRow);
                }
                
                Separator sep = new Separator();
                
                // Total
                Label totalLabel = new Label("💰 Total: $" + compra.getTotal());
                totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #27ae60;");
                
                compraCard.getChildren().addAll(header, sep, entradasBox, totalLabel);
                content.getChildren().add(compraCard);
            }
            
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(500);
            scrollPane.setPrefWidth(700);
            
            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
            
        } catch (Exception e) {
            mostrarError("Error al cargar historial: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void mostrarQR(Entrada entrada) {
        if (entrada.getCodigoQR() == null || entrada.getCodigoQR().isEmpty()) {
            mostrarError("Esta entrada no tiene código QR");
            return;
        }
        
        try {
            Dialog<Void> qrDialog = new Dialog<>();
            qrDialog.setTitle("Código QR");
            qrDialog.setHeaderText("🎫 " + entrada.getEvento().getNombre());
            
            VBox content = new VBox(10);
            content.setStyle("-fx-alignment: center; -fx-padding: 20;");
            
            // Decodificar base64 y mostrar imagen
            String base64Data = entrada.getCodigoQR();
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1];
            }
            
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(imageBytes);
            javafx.scene.image.Image qrImage = new javafx.scene.image.Image(bis);
            
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(qrImage);
            imageView.setFitWidth(300);
            imageView.setFitHeight(300);
            imageView.setPreserveRatio(true);
            
            Label infoLabel = new Label("Número: " + entrada.getNumeroEntrada());
            infoLabel.setStyle("-fx-font-size: 12px; -fx-font-family: monospace;");
            
            Label tipoLabel = new Label(entrada.getTipoEntrada().getNombre());
            tipoLabel.setStyle("-fx-font-weight: bold;");
            
            content.getChildren().addAll(imageView, infoLabel, tipoLabel);
            
            qrDialog.getDialogPane().setContent(content);
            qrDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            qrDialog.showAndWait();
            
        } catch (Exception e) {
            mostrarError("Error al mostrar QR: " + e.getMessage());
        }
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
            Scene scene = new Scene(loginRoot, 600, 500);
            stage.setScene(scene);
            stage.setTitle("Sistema de Gesti\u00f3n de Eventos - Login");
            stage.setResizable(true);
            stage.centerOnScreen();
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
