package com.eventos.controllers;

import com.eventos.models.Evento;
import com.eventos.models.Usuario;
import com.eventos.repositories.EventoRepository;
import com.eventos.services.AutenticacionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
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
    private VBox sideMenu;
    
    @FXML
    private VBox eventosListContainer;

    private final EventoRepository eventoRepository;
    private final AutenticacionService autenticacionService;
    private Usuario usuarioActual;
    private List<Evento> eventosActuales;

    /**
     * Constructor del controlador.
     */
    public EventosUsuarioController() {
        this.eventoRepository = new EventoRepository();
        this.autenticacionService = AutenticacionService.getInstance();
    }

    /**
     * Inicialización automática de JavaFX.
     */
    @FXML
    private void initialize() {
        usuarioActual = autenticacionService.getUsuarioActual();
        cargarEventos();
        
        // Configurar búsqueda en tiempo real
        busquedaField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 3 || newValue.isEmpty()) {
                buscarEventos(newValue);
            }
        });
    }

    /**
     * Carga todos los eventos disponibles.
     */
    private void cargarEventos() {
        try {
            eventosActuales = eventoRepository.findAll();
            mostrarEventos(eventosActuales);
        } catch (Exception e) {
            mostrarError("Error al cargar eventos: " + e.getMessage());
            e.printStackTrace();
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
        // TODO: Abrir ventana de detalles del evento y compra
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles del Evento");
        alert.setHeaderText(evento.getNombre());
        alert.setContentText("Funcionalidad de compra en desarrollo...\n\n" +
                           "Evento: " + evento.getNombre() + "\n" +
                           "Fecha: " + evento.getFechaInicio() + "\n" +
                           "Sede: " + evento.getSede().getNombre());
        alert.showAndWait();
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
        // TODO: Implementar vista de mis entradas
        mostrarInfo("Mis entradas en desarrollo");
        toggleMenu();
    }

    @FXML
    private void handleHistorial() {
        // TODO: Implementar historial de compras
        mostrarInfo("Historial de compras en desarrollo");
        toggleMenu();
    }

    @FXML
    private void handlePerfil() {
        // TODO: Implementar vista de perfil
        mostrarInfo("Perfil de usuario en desarrollo");
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
