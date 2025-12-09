package com.eventos;

import com.eventos.config.HibernateUtil;
import com.eventos.config.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase principal de la aplicación - Sistema de Gestión de Eventos
 * 
 * Esta clase extiende de Application (JavaFX) y es el punto de entrada del programa.
 * 
 * Responsabilidades:
 * - Inicializar la conexión a la base de datos (Hibernate)
 * - Configurar y mostrar la ventana principal (JavaFX Stage)
 * - Cargar la primera pantalla (Login)
 * - Gestionar el cierre correcto de recursos al salir
 * 
 * Flujo de ejecución:
 * 1. main() -> Lanza la aplicación JavaFX
 * 2. init() -> Inicializa Hibernate antes de mostrar la interfaz
 * 3. start() -> Carga la pantalla de login y la muestra
 * 4. stop() -> Cierra recursos al cerrar la aplicación
 */
public class Main extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    /**
     * Método principal - Punto de entrada del programa
     * Lanza la aplicación JavaFX
     * 
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        logger.info("=================================================");
        logger.info("  Sistema de Gestión de Eventos v1.0.0");
        logger.info("=================================================");
        
        // Lanzar aplicación JavaFX
        // Esto llamará automáticamente a init() -> start()
        launch(args);
    }
    
    /**
     * Método de inicialización de JavaFX
     * Se ejecuta ANTES de mostrar la interfaz gráfica
     * Ideal para inicializar conexiones y recursos
     * 
     * @throws Exception Si hay error en la inicialización
     */
    @Override
    public void init() throws Exception {
        super.init();
        
        logger.info("Inicializando aplicación...");
        
        try {
            // Inicializar conexión a base de datos con Hibernate
            logger.info("Inicializando conexión a base de datos...");
            HibernateUtil.getEntityManagerFactory();
            logger.info("✓ Conexión a base de datos establecida correctamente");

            // Sembrar datos mínimos para evitar errores en instalaciones nuevas
            new DatabaseInitializer().initialize();
            
        } catch (Exception e) {
            logger.error("✗ Error al inicializar la base de datos", e);
            throw e; // Re-lanzar excepción para que JavaFX maneje el error
        }
        
        logger.info("✓ Aplicación inicializada correctamente");
    }
    
    /**
     * Método start de JavaFX
     * Se ejecuta cuando la aplicación está lista para mostrar la interfaz
     * Aquí se configura la ventana principal y se carga la primera pantalla
     * 
     * @param primaryStage Ventana principal de la aplicación (Stage)
     * @throws Exception Si hay error al cargar la interfaz
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Iniciando interfaz gráfica...");
        
        try {
            // Cargar el archivo FXML de la pantalla de login
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml")
            );
            Parent root = loader.load();
            
            // Crear la escena con el contenido del login
            Scene scene = new Scene(root, 400, 600);
            
            // Cargar estilos CSS
            try {
                String css = getClass().getResource("/css/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
                logger.info("✓ Estilos CSS cargados");
            } catch (Exception e) {
                logger.warn("⚠ No se pudieron cargar los estilos CSS: {}", e.getMessage());
            }
            
            // Configurar la ventana principal
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

            primaryStage.setTitle("Sistema de Gestión de Eventos - Login");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(360);
            primaryStage.setMinHeight(520);
            primaryStage.setWidth(Math.min(480, bounds.getWidth() * 0.8));
            primaryStage.setHeight(Math.min(720, bounds.getHeight() * 0.85));
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            
            // Mostrar la ventana
            primaryStage.show();
            
            logger.info("✓ Interfaz gráfica cargada correctamente");
            logger.info("Sistema listo para usar");
            
        } catch (Exception e) {
            logger.error("✗ Error al cargar la interfaz gráfica", e);
            throw e;
        }
    }
    
    /**
     * Método stop de JavaFX
     * Se ejecuta cuando se cierra la aplicación
     * Aquí se liberan recursos y se cierran conexiones
     * 
     * @throws Exception Si hay error al cerrar recursos
     */
    @Override
    public void stop() throws Exception {
        logger.info("Cerrando aplicación...");
        
        try {
            // Cerrar conexión a base de datos (Hibernate)
            HibernateUtil.shutdown();
            logger.info("✓ Conexión a base de datos cerrada correctamente");
            
        } catch (Exception e) {
            logger.error("✗ Error al cerrar recursos", e);
        }
        
        logger.info("=================================================");
        logger.info("  Aplicación cerrada correctamente");
        logger.info("=================================================");
        
        super.stop();
    }
}
