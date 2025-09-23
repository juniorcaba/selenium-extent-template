package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;
    private static final String CONFIG_PATH = "src/main/resources/config.properties";

    static {
        loadProperties();
    }

    private static void loadProperties() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Error loading config.properties: " + e.getMessage());
            // Valores por defecto si no se encuentra el archivo
            properties = new Properties();
            properties.setProperty("base.url", "https://automationexercise.com");
            properties.setProperty("browser", "chrome");
            properties.setProperty("timeout", "10");
            properties.setProperty("headless", "false");
        }
    }

    public static String getProperty(String key) {
        // Primero busca en System Properties, luego en config.properties
        return System.getProperty(key, properties.getProperty(key));
    }

    public static String getBaseUrl() {
        return getProperty("base.url");
    }

    public static String getBrowser() {
        return getProperty("browser");
    }

    public static int getTimeout() {
        return Integer.parseInt(getProperty("timeout"));
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(getProperty("headless"));
    }

    // Método para obtener URLs específicas
    public static String getUrl(String urlKey) {
        return getProperty(urlKey + ".url");
    }
}
