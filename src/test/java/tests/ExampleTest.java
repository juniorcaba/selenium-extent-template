package tests;

import basetest.BaseTest;
import pages.ExamplePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExampleTest extends BaseTest {

    @Test(description = "Verificar que YouTube se carga correctamente")
    public void verifyYouTubeHomePage() {
        ExamplePage youtubePage = new ExamplePage(getDriver());

        // Navegar a YouTube
        youtubePage.navigateTo();

        // Verificar que la página se cargó
        Assert.assertTrue(youtubePage.isPageLoaded(), "YouTube debería cargar correctamente");

        // Verificar que la caja de búsqueda está presente
        Assert.assertTrue(youtubePage.isSearchBoxDisplayed(), "La caja de búsqueda debería estar visible");

        // Verificar el título
        String title = youtubePage.getPageTitle();
        Assert.assertTrue(title.contains("YouTube"), "El título debe contener 'YouTube'");

        // Verificar la URL
        String currentUrl = youtubePage.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("youtube.com"), "La URL debe contener 'youtube.com'");
    }

    @Test(description = "Realizar una búsqueda en YouTube")
    public void performBasicSearch() {
        ExamplePage youtubePage = new ExamplePage(getDriver());

        // Navegar a YouTube
        youtubePage.navigateTo();

        // Verificar que la página cargó
        Assert.assertTrue(youtubePage.isPageLoaded(), "YouTube debe cargar antes de buscar");

        // Realizar búsqueda
        String searchTerm = "selenium automation tutorial";
        youtubePage.searchFor(searchTerm);

        // Verificar que se redirigió a página de resultados
        String currentUrl = youtubePage.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("results"), "Debería estar en página de resultados");

        // Verificar que el título cambió e incluye el término de búsqueda
        String title = youtubePage.getPageTitle();
        Assert.assertTrue(title.contains(searchTerm), "El título debe contener el término buscado");
    }
}