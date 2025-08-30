package tests;

import basetest.BaseTest;
import pages.ExamplePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExampleTest extends BaseTest {

    @Test(description = "Verificar que YouTube se carga correctamente")
    public void verifyYouTubeHomePage() throws InterruptedException {
        ExamplePage youtubePage = new ExamplePage(getDriver());

        youtubePage.navigateTo();

        Assert.assertTrue(youtubePage.isPageLoaded(), "YouTube debería cargar correctamente");

        Assert.assertTrue(youtubePage.isSearchBoxDisplayed(), "La caja de búsqueda debería estar visible");

        String title = youtubePage.getPageTitle();
        Assert.assertTrue(title.contains("YouTube"), "El título debe contener 'YouTube'");

        String currentUrl = youtubePage.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("youtube.com"), "La URL debe contener 'youtube.com'");

        Assert.assertTrue(youtubePage.validatePageElements(), "Todos los elementos de la página deben estar presentes");
    }

    @Test(description = "Realizar una búsqueda en YouTube")
    public void performBasicSearch() throws InterruptedException {
        ExamplePage youtubePage = new ExamplePage(getDriver());

        // Navegar a YouTube - ya maneja el reporte automáticamente
        youtubePage.navigateTo();

        Assert.assertTrue(youtubePage.isPageLoaded(), "YouTube debe cargar antes de buscar");

        String searchTerm = "selenium automation tutorial";
        youtubePage.searchFor(searchTerm);

        String currentUrl = youtubePage.getCurrentUrl();

        youtubePage.validateWithReport(
            currentUrl.contains("results"),
            "Navegación exitosa a página de resultados",
            "No se navegó a la página de resultados. URL actual: " + currentUrl,
            StepMode.IMMEDIATE
        );
        Assert.assertTrue(currentUrl.contains("results"), "Debería estar en página de resultados");

        String title = youtubePage.getPageTitle();
        youtubePage.validateWithReport(
            title.contains(searchTerm),
            "El título contiene el término de búsqueda: " + searchTerm,
            "El título no contiene el término de búsqueda. Título actual: " + title,
            StepMode.IMMEDIATE
        );
        Assert.assertTrue(title.contains(searchTerm), "El título debe contener el término buscado");
    }

    @Test(description = "Demostrar uso avanzado del sistema de buffer")
    public void demonstrateBufferUsage() throws InterruptedException {
        ExamplePage youtubePage = new ExamplePage(getDriver());
        
        createStep("Iniciando test de demostración", true, false, StepMode.BUFFER);
        createStep("Preparando configuración inicial", true, false, StepMode.BUFFER);
        createStep("Verificando precondiciones", true, false, StepMode.BUFFER);
        processBuffer(BufferAction.COMMIT_SUCCESS, null, false);
        
        youtubePage.navigateTo(); 
        boolean pageValid = youtubePage.isPageLoaded(); 

        if (pageValid) {
            createStep("Validación completa de página exitosa - Lista para pruebas adicionales", true, true, StepMode.IMMEDIATE);
        }

        Assert.assertTrue(pageValid, "La página debe ser válida");
    }
}
