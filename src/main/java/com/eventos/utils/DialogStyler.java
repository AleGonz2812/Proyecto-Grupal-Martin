package com.eventos.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

/**
 * Utilidad para aplicar estilos oscuros personalizados a los diálogos de JavaFX.
 * Aplica la hoja de estilos CSS de la aplicación a todos los diálogos.
 */
public class DialogStyler {
    
    private static final String STYLESHEET_PATH = "/css/styles.css";
    
    /**
     * Aplica estilos oscuros a un diálogo genérico.
     * 
     * @param dialog El diálogo a estilizar
     */
    public static void styleDialog(Dialog<?> dialog) {
        if (dialog != null && dialog.getDialogPane() != null) {
            DialogPane dialogPane = dialog.getDialogPane();
            
            // Aplicar la hoja de estilos
            try {
                String stylesheet = DialogStyler.class.getResource(STYLESHEET_PATH).toExternalForm();
                dialogPane.getStylesheets().add(stylesheet);
                
                // Aplicar clase CSS al DialogPane
                dialogPane.getStyleClass().add("dialog-pane");
                
                // Forzar el tema oscuro removiendo estilos por defecto
                dialogPane.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #1a2332, #2c3e50);" +
                    "-fx-background-radius: 20;" +
                    "-fx-border-color: rgba(255, 255, 255, 0.15);" +
                    "-fx-border-width: 2;" +
                    "-fx-border-radius: 20;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.6), 30, 0, 0, 10);"
                );
                
            } catch (Exception e) {
                System.err.println("Error al aplicar estilos al diálogo: " + e.getMessage());
            }
        }
    }
    
    /**
     * Aplica estilos oscuros a una alerta.
     * También añade una clase CSS específica según el tipo de alerta.
     * 
     * @param alert La alerta a estilizar
     */
    public static void styleAlert(Alert alert) {
        if (alert != null && alert.getDialogPane() != null) {
            DialogPane dialogPane = alert.getDialogPane();
            
            // Aplicar la hoja de estilos
            try {
                String stylesheet = DialogStyler.class.getResource(STYLESHEET_PATH).toExternalForm();
                dialogPane.getStylesheets().add(stylesheet);
                
                // Aplicar clase CSS según el tipo de alerta
                switch (alert.getAlertType()) {
                    case INFORMATION:
                        alert.getDialogPane().getStyleClass().add("information");
                        break;
                    case WARNING:
                        alert.getDialogPane().getStyleClass().add("warning");
                        break;
                    case ERROR:
                        alert.getDialogPane().getStyleClass().add("error");
                        break;
                    case CONFIRMATION:
                        alert.getDialogPane().getStyleClass().add("confirmation");
                        break;
                }
                
                // Forzar el tema oscuro
                dialogPane.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #1a2332, #2c3e50);" +
                    "-fx-background-radius: 20;" +
                    "-fx-border-color: rgba(255, 255, 255, 0.15);" +
                    "-fx-border-width: 2;" +
                    "-fx-border-radius: 20;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.6), 30, 0, 0, 10);"
                );
                
            } catch (Exception e) {
                System.err.println("Error al aplicar estilos a la alerta: " + e.getMessage());
            }
        }
    }
}
