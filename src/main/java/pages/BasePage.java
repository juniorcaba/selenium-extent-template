package pages;

import basetest.BaseTest;
import basetest.BaseTest.StepMode;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    private static final Logger logger = LoggerFactory.getLogger(BasePage.class);

    // Variables que cada página debe definir
    protected String pageUrl;
    protected By validationLocator;
    protected String pageName;

    public BasePage(WebDriver driver){
        if (driver == null){
            throw new IllegalArgumentException("webdriver no puede ser null. ");
        }
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void click(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
        } catch (Exception e) {
            System.out.println("Fallo al hacer clic en: " + locator);
            throw e;
        }
    }

    public static void pause(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Método goTo que usa las variables definidas por cada página
     * Cada página solo necesita definir: pageUrl, validationLocator, y pageName
     */
    public void goTo() throws InterruptedException {
        try {
            // Validar que la página haya definido los valores necesarios
            if (pageUrl == null || validationLocator == null || pageName == null) {
                throw new RuntimeException("La página debe definir pageUrl, validationLocator y pageName");
            }

            driver.get(pageUrl);
            wait.until(ExpectedConditions.presenceOfElementLocated(validationLocator));

            // Pequeña pausa para asegurar que la página esté completamente cargada
            Thread.sleep(500);

            BaseTest.createStep("Navegando a " + pageName, true, true, StepMode.IMMEDIATE);

        } catch (Exception e) {
            BaseTest.createStep("Error al navegar a " + pageName + ": " + e.getMessage(), false, true, StepMode.IMMEDIATE);
            throw e;
        }
    }

    /**
     * Método para hacer click con reporte usando el sistema unificado
     * @param locator Localizador del elemento
     * @param description Descripción para el reporte
     * @param mode Modo de procesamiento del step
     */
    public void clickWithReport(By locator, String description, StepMode mode) {
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
            BaseTest.createStep("Click exitoso: " + description, true, true, mode);
        } catch (Exception e) {
            BaseTest.createStep("Error al hacer click: " + description + " - " + e.getMessage(), false, true, mode);
            throw e;
        }
    }

    /**
     * Método para escribir texto con reporte usando el sistema unificado
     * @param locator Localizador del elemento
     * @param text Texto a escribir
     * @param description Descripción para el reporte
     * @param mode Modo de procesamiento del step
     */
    public void sendKeysWithReport(By locator, String text, String description, StepMode mode) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            element.clear();
            element.sendKeys(text);
            BaseTest.createStep("Texto ingresado: " + description, true, true, mode);
        } catch (Exception e) {
            BaseTest.createStep("Error al ingresar texto: " + description + " - " + e.getMessage(), false, true, mode);
            throw e;
        }
    }

    /**
     * Método para validaciones con reporte usando el sistema unificado
     * @param condition Condición a validar
     * @param successMessage Mensaje si la validación es exitosa
     * @param failureMessage Mensaje si la validación falla
     * @param mode Modo de procesamiento del step
     * @throws AssertionError Si la validación falla
     */
    public void validateWithReport(boolean condition, String successMessage, String failureMessage, StepMode mode) {
        if (condition) {
            BaseTest.createStep("Validación exitosa: " + successMessage, true, true, mode);
        } else {
            BaseTest.createStep("Validación fallida: " + failureMessage, false, true, mode);
            throw new AssertionError(failureMessage);
        }
    }
}

