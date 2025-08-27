package basetest;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.*;
import utils.ExtentManager;
import utils.ScreenshotUtils;
import com.aventstack.extentreports.Status;

import java.io.File;

@Listeners(utils.ExtentTestListener.class)
public class BaseTest {
    private WebDriver driver;

    @BeforeSuite
    public void setUpSuite() {
        ExtentManager.createInstance();
        File reportsDir = new File(System.getProperty("user.dir") + "/reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
    }

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        ExtentManager.removeTest();
    }

    @AfterSuite
    public void tearDownSuite() {
        ExtentManager.flushReport();
    }

    public WebDriver getDriver() {
        return driver;
    }

    // ============= MÉTODOS DE INSTANCIA =============

    /**
     * Método principal de instancia - Con screenshot opcional
     */
    public void createStep(String stepDescription, boolean isPassed, boolean takeScreenshot) {
        Status status = isPassed ? Status.PASS : Status.FAIL;

        if (takeScreenshot && driver != null) {
            try {
                String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
                String styleName = isPassed ? "success" : "error";
                String imageHtml = ScreenshotUtils.generateScreenshotHtml(base64Screenshot, styleName, stepDescription);
                ExtentManager.getTest().log(status, stepDescription + "<br>" + imageHtml);
            } catch (Exception e) {
                ExtentManager.getTest().log(status, stepDescription);
                ExtentManager.getTest().log(Status.WARNING, ScreenshotUtils.getErrorMessage() + ": " + e.getMessage());
            }
        } else {
            ExtentManager.getTest().log(status, stepDescription);
        }
    }

    /**
     * Método sobrecargado - Sin screenshot
     */
    public void createStep(String stepDescription, boolean isPassed) {
        createStep(stepDescription, isPassed, false);
    }

    // ============= MÉTODO ESTÁTICO PRINCIPAL =============

    /**
     * METODO ESTÁTICO
     * Automáticamente selecciona el estilo basado en isPassed
     */
    public static void createStepStatic(String stepDescription, boolean isPassed, boolean takeScreenshot, WebDriver driver) {
        // Verificar que existe un test activo
        if (ExtentManager.getTest() == null) {
            System.err.println("No hay test activo para crear step: " + stepDescription);
            return;
        }

        Status status = isPassed ? Status.PASS : Status.FAIL;

        if (takeScreenshot && driver != null) {
            try {
                String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);

                // Auto-selección de estilo basado en el resultado
                String styleName = isPassed ? "success" : "error";

                String imageHtml = ScreenshotUtils.generateScreenshotHtml(base64Screenshot, styleName, stepDescription);
                ExtentManager.getTest().log(status, stepDescription + "<br>" + imageHtml);

            } catch (Exception e) {
                ExtentManager.getTest().log(status, stepDescription);
                ExtentManager.getTest().log(Status.WARNING, ScreenshotUtils.getErrorMessage() + ": " + e.getMessage());
            }
        } else {
            ExtentManager.getTest().log(status, stepDescription);
        }
    }

    // ============= MÉTODOS ESTÁTICOS SOBRECARGADOS (OPCIONALES) =============

    /**
     * Sin screenshot (más común)
     */
    public static void createStepStatic(String stepDescription, boolean isPassed, WebDriver driver) {
        createStepStatic(stepDescription, isPassed, false, driver);
    }

    /**
     * Control manual del estilo (casos especiales)
     * Por ejemplo: warnings que no son ni pass ni fail
     */
    public static void createStepWithCustomStyle(String stepDescription, Status status, boolean takeScreenshot, WebDriver driver, String styleName) {
        if (ExtentManager.getTest() == null) {
            System.err.println("No hay test activo para crear step: " + stepDescription);
            return;
        }

        if (takeScreenshot && driver != null) {
            try {
                String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
                String imageHtml = ScreenshotUtils.generateScreenshotHtml(base64Screenshot, styleName, stepDescription);
                ExtentManager.getTest().log(status, stepDescription + "<br>" + imageHtml);
            } catch (Exception e) {
                ExtentManager.getTest().log(status, stepDescription);
                ExtentManager.getTest().log(Status.WARNING, ScreenshotUtils.getErrorMessage() + ": " + e.getMessage());
            }
        } else {
            ExtentManager.getTest().log(status, stepDescription);
        }
    }

    @AfterTest
    public void afterTestMethod() {
        if (driver != null) {
            driver.quit();
        }
    }
}
