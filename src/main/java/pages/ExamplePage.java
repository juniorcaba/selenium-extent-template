package pages;

import basetest.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ExamplePage extends BasePage {

    // Locators de YouTube
    private final By searchBox = By.name("search_query");
    private final By youtubeLogo = By.xpath("//yt-icon[@id='logo-icon']");
    private final By searchButton = By.id("search-icon-legacy");

    // URL base
    private final String youtubeUrl = "https://www.youtube.com";

    public ExamplePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navega a YouTube
     */
    public void navigateTo() {
        goTo(youtubeUrl);
    }

    /**
     * Verifica si el logo de YouTube está visible
     */
    public boolean isYouTubeLogoDisplayed() {
        try {
            WebElement logo = wait.until(ExpectedConditions.presenceOfElementLocated(youtubeLogo));
            return logo.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si la caja de búsqueda está presente
     */
    public boolean isSearchBoxDisplayed() {
        try {
            WebElement searchBoxElement = wait.until(ExpectedConditions.visibilityOfElementLocated(searchBox));
            return searchBoxElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Realiza una búsqueda en YouTube
     */
    public void searchFor(String searchTerm) {
        try {
            WebElement searchBoxElement = wait.until(ExpectedConditions.elementToBeClickable(searchBox));
            searchBoxElement.clear();
            searchBoxElement.sendKeys(searchTerm);

            // Presionar Enter (más confiable que hacer clic en el botón)
            searchBoxElement.sendKeys(Keys.ENTER);
            BaseTest.createStepStatic("Se ingresa la busqueda", true, true, driver);

            // Esperar a que aparezcan los resultados
            Thread.sleep(2000); // Pausa breve para cargar resultados

        } catch (Exception e) {
            throw new RuntimeException("Error al realizar búsqueda: " + e.getMessage());
        }
    }

    /**
     * Obtiene el título de la página
     */
    public String getPageTitle() {
        return driver.getTitle();
    }

    /**
     * Obtiene la URL actual
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Verifica si la página se cargó correctamente
     */
    public boolean isPageLoaded() {
        try {
            // Verificar que el título contenga "YouTube"
            String title = getPageTitle();
            return title != null && title.contains("YouTube");
        } catch (Exception e) {
            return false;
        }
    }
}