package com.eventos.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * Gestor de Hot Reload para archivos FXML y CSS.
 * Permite recargar vistas sin reiniciar la aplicación.
 */
public class HotReloadManager {
    
    private static final Logger logger = LoggerFactory.getLogger(HotReloadManager.class);
    
    /**
     * Recarga una escena desde su archivo FXML.
     * Mantiene el mismo Stage pero recrea la Scene con el FXML actualizado.
     * 
     * @param stage Stage actual que contiene la escena
     * @param fxmlPath Ruta al archivo FXML (ej: "/fxml/eventos_usuario.fxml")
     * @param width Ancho de la ventana
     * @param height Alto de la ventana
     * @return true si la recarga fue exitosa, false en caso contrario
     */
    public static boolean reloadScene(Stage stage, String fxmlPath, double width, double height) {
        try {
            logger.info("🔄 Recargando escena: {}", fxmlPath);
            
            // Cargar el FXML actualizado
            URL fxmlUrl = HotReloadManager.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                logger.error("❌ No se encontró el archivo FXML: {}", fxmlPath);
                return false;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            // Crear nueva escena
            Scene newScene = new Scene(root, width, height);
            
            // Recargar CSS (forzar recarga desde disco)
            String cssPath = "/css/styles.css";
            URL cssUrl = HotReloadManager.class.getResource(cssPath);
            if (cssUrl != null) {
                // Añadir timestamp para forzar recarga
                String cssWithTimestamp = cssUrl.toExternalForm() + "?t=" + System.currentTimeMillis();
                newScene.getStylesheets().add(cssWithTimestamp);
                logger.info("✓ CSS recargado: {}", cssPath);
            }
            
            // Aplicar la nueva escena al Stage
            stage.setScene(newScene);
            
            logger.info("✓ Escena recargada exitosamente");
            return true;
            
        } catch (IOException e) {
            logger.error("❌ Error al recargar la escena: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Recarga solo el CSS de la escena actual.
     * Útil para ver cambios de estilo sin recargar toda la vista.
     * 
     * @param scene Escena actual
     * @return true si la recarga fue exitosa, false en caso contrario
     */
    public static boolean reloadCSS(Scene scene) {
        try {
            logger.info("🎨 Recargando estilos CSS...");
            
            // Limpiar estilos actuales
            scene.getStylesheets().clear();
            
            // Recargar CSS con timestamp para evitar caché
            String cssPath = "/css/styles.css";
            URL cssUrl = HotReloadManager.class.getResource(cssPath);
            if (cssUrl != null) {
                String cssWithTimestamp = cssUrl.toExternalForm() + "?t=" + System.currentTimeMillis();
                scene.getStylesheets().add(cssWithTimestamp);
                logger.info("✓ CSS recargado exitosamente");
                return true;
            } else {
                logger.error("❌ No se encontró el archivo CSS: {}", cssPath);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("❌ Error al recargar CSS: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Configura atajos de teclado para hot reload.
     * F5 = Recargar escena completa (FXML + CSS)
     * F6 = Recargar solo CSS
     * 
     * @param scene Escena donde configurar los atajos
     * @param stage Stage de la aplicación
     * @param fxmlPath Ruta al FXML de la vista actual
     */
    public static void setupHotReloadShortcuts(Scene scene, Stage stage, String fxmlPath) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case F5:
                    // Recargar escena completa
                    logger.info("⌨️ F5 presionado - Recargando vista completa...");
                    double width = stage.getWidth();
                    double height = stage.getHeight();
                    reloadScene(stage, fxmlPath, width, height);
                    event.consume();
                    break;
                    
                case F6:
                    // Recargar solo CSS
                    logger.info("⌨️ F6 presionado - Recargando CSS...");
                    reloadCSS(scene);
                    event.consume();
                    break;
                    
                default:
                    // No hacer nada con otras teclas
                    break;
            }
        });
        
        logger.info("✓ Atajos de hot reload configurados (F5=FXML+CSS, F6=CSS)");
    }
}
