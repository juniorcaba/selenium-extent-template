package pages;

import basetest.BaseTest;
import basetest.BaseTest.StepMode;
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

    public ExamplePage(WebDriver driver) {
        super(driver);
        
        this.pageUrl = "https://www.youtube.com";
        this.validationLocator = youtubeLogo;  // Usar el logo como elemento de validación
        this.pageName = "YouTube Home Page";
    }

    public void navigateTo() throws InterruptedException {
        goTo(); // Ahora usa el método de BasePage que maneja todo automáticamente
    }

    public boolean isYouTubeLogoDisplayed() {
        try {
            WebElement logo = wait.until(ExpectedConditions.presenceOfElementLocated(youtubeLogo));
            boolean isDisplayed = logo.isDisplayed();
            
            validateWithReport(
                isDisplayed,
                "El logo de YouTube está visible",
                "El logo de YouTube no está visible",
                StepMode.IMMEDIATE
            );
            
            return isDisplayed;
        } catch (Exception e) {
            BaseTest.createStep("Error al verificar logo: " + e.getMessage(), false, true, StepMode.IMMEDIATE);
            return false;
        }
    }

    public boolean isSearchBoxDisplayed() {
        try {
            WebElement searchBoxElement = wait.until(ExpectedConditions.visibilityOfElementLocated(searchBox));
            boolean isDisplayed = searchBoxElement.isDisplayed();
            
            validateWithReport(
                isDisplayed,
                "La caja de búsqueda está visible",
                "La caja de búsqueda no está visible",
                StepMode.IMMEDIATE
            );
            return isDisplayed;
        } catch (Exception e) {
            BaseTest.createStep("Error al verificar caja de búsqueda: " + e.getMessage(), false, true, StepMode.IMMEDIATE);
            return false;
        }
    }

    public void searchFor(String searchTerm) {
        try {
            clickWithReport(searchBox, "Click en caja de búsqueda", StepMode.BUFFER);
            WebElement searchBoxElement = wait.until(ExpectedConditions.elementToBeClickable(searchBox));
            searchBoxElement.clear();
            sendKeysWithReport(searchBox, searchTerm, "Ingresar término de búsqueda: " + searchTerm, StepMode.BUFFER);
            searchBoxElement.sendKeys(Keys.ENTER);
            BaseTest.createStep("Presionar Enter para buscar", true, false, StepMode.BUFFER);
            Thread.sleep(2000);
            BaseTest.processBuffer(BaseTest.BufferAction.COMMIT_SUCCESS, null, false);
            
        } catch (Exception e) {
            BaseTest.processBuffer(BaseTest.BufferAction.DISCARD_AND_FAIL, 
                "Error al realizar búsqueda: " + e.getMessage(), true);
            throw new RuntimeException("Error al realizar búsqueda: " + e.getMessage());
        }
    }
    
    public String getPageTitle() {
        String title = driver.getTitle();
        BaseTest.createStep("Título obtenido: " + title, true, false, StepMode.IMMEDIATE);
        return title;
    }
    
    public String getCurrentUrl() {
        String url = driver.getCurrentUrl();
        BaseTest.createStep("URL actual: " + url, true, false, StepMode.IMMEDIATE);
        return url;
    }
    
    public boolean isPageLoaded() {
        try {
            String title = driver.getTitle();
            boolean isLoaded = title != null && title.contains("YouTube");
            validateWithReport(
                isLoaded,
                "La página de YouTube se cargó correctamente. Título: " + title,
                "La página de YouTube no se cargó correctamente. Título: " + title,
                StepMode.IMMEDIATE
            );
            return isLoaded;
        } catch (Exception e) {
            BaseTest.createStep("Error al verificar si la página cargó: " + e.getMessage(), false, true, StepMode.IMMEDIATE);
            return false;
        }
    }

    public boolean validatePageElements() {
        try {
            BaseTest.createStep("Verificando logo de YouTube", true, false, StepMode.BUFFER);
            boolean logoDisplayed = wait.until(ExpectedConditions.presenceOfElementLocated(youtubeLogo)).isDisplayed();
            BaseTest.createStep("Verificando caja de búsqueda", true, false, StepMode.BUFFER);
            boolean searchBoxDisplayed = wait.until(ExpectedConditions.visibilityOfElementLocated(searchBox)).isDisplayed();
            BaseTest.createStep("Verificando título de página", true, false, StepMode.BUFFER);
            boolean titleCorrect = driver.getTitle().contains("YouTube");
            
            if (logoDisplayed && searchBoxDisplayed && titleCorrect) {
                BaseTest.processBuffer(BaseTest.BufferAction.COMMIT_SUCCESS, null, false);
                return true;
            } else {
                BaseTest.processBuffer(BaseTest.BufferAction.COMMIT_MERGED_FAILURE, 
                    "Una o más validaciones de elementos fallaron", true);
                return false;
            }
        } catch (Exception e) {
            BaseTest.processBuffer(BaseTest.BufferAction.DISCARD_AND_FAIL, 
                "Error durante validación de elementos: " + e.getMessage(), true);
            return false;
        }
    }
}
