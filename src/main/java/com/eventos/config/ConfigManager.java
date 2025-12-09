package com.eventos.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Gestor de configuración de la aplicación.
 * Lee las propiedades desde config.properties
 */
public class ConfigManager {
    private static final Properties properties = new Properties();
    
    static {
        try (InputStream input = ConfigManager.class.getResourceAsStream("/config.properties")) {
            if (input == null) {
                throw new RuntimeException("No se pudo encontrar config.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar configuración", e);
        }
    }
    
    /**
     * Obtiene el valor de una propiedad
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Obtiene el valor de una propiedad o un valor por defecto
     */
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    // Métodos de conveniencia para propiedades comunes
    
    public static String getDbUrl() {
        return get("db.url");
    }
    
    public static String getDbUsername() {
        return get("db.username");
    }
    
    public static String getDbPassword() {
        return get("db.password");
    }
    
    public static String getExportPath() {
        return get("app.export.path");
    }
    
    public static String getQrPath() {
        return get("app.qr.path");
    }
    
    public static String getImportsPath() {
        return get("app.imports.path");
    }
    
    public static String getAppName() {
        return get("app.name");
    }
    
    public static String getAppVersion() {
        return get("app.version");
    }
}
