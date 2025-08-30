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
import java.util.ArrayList;
import java.util.List;

@Listeners(utils.ExtentTestListener.class)
public class BaseTest {
    private static ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<List<PendingStep>> pendingStepsThreadLocal = new ThreadLocal<>();

    public enum StepMode {
        BUFFER,     // Agrega al buffer
        IMMEDIATE,  // Escribe inmediatamente
        STATIC      // Alias para BUFFER
    }

    public enum BufferAction {
        COMMIT_SUCCESS,           // Todos los pasos como exitosos
        COMMIT_WITH_FAILURE,      // Pasos exitosos + nuevo fallo
        COMMIT_MERGED_FAILURE,    // Último paso combinado con mensaje de fallo
        DISCARD_AND_FAIL         // Descartar buffer y solo escribir fallo
    }

    public static class PendingStep {
        private String description;
        private boolean isPassed;
        private boolean takeScreenshot;
        private String screenshotBase64;

        public PendingStep(String description, boolean isPassed, boolean takeScreenshot) {
            this.description = description;
            this.isPassed = isPassed;
            this.takeScreenshot = takeScreenshot;

            if (takeScreenshot && getDriver() != null) {
                this.screenshotBase64 = captureScreenshotAsBase64();
            }
        }

        private String captureScreenshotAsBase64() {
            try {
                return ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.BASE64);
            } catch (Exception e) {
                System.err.println("Error capturando screenshot: " + e.getMessage());
                return null;
            }
        }

        // Getters esenciales únicamente
        public String getDescription() { return description; }
        public boolean isPassed() { return isPassed; }
        public boolean shouldTakeScreenshot() { return takeScreenshot; }
        public String getScreenshotBase64() { return screenshotBase64; }
    }

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
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driverThreadLocal.set(driver);
        pendingStepsThreadLocal.set(new ArrayList<>());
    }

    @AfterMethod
    public void tearDown() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
        }
        pendingStepsThreadLocal.remove();
        ExtentManager.removeTest();
    }

    @AfterSuite
    public void tearDownSuite() {
        ExtentManager.flushReport();
    }

    @AfterTest
    public void afterTestMethod() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
        }
    }

    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new RuntimeException("WebDriver no inicializado. Asegúrate de que @BeforeMethod se haya ejecutado.");
        }
        return driver;
    }

    private static List<PendingStep> getPendingSteps() {
        List<PendingStep> steps = pendingStepsThreadLocal.get();
        if (steps == null) {
            steps = new ArrayList<>();
            pendingStepsThreadLocal.set(steps);
        }
        return steps;
    }

    /**
     * Método principal y único para crear steps
     * @param description Descripción del step
     * @param isPassed Si el step fue exitoso o no
     * @param takeScreenshot Si debe tomar screenshot
     * @param mode Modo de procesamiento (BUFFER, IMMEDIATE, STATIC)
     */
    public static void createStep(String description, boolean isPassed, boolean takeScreenshot, StepMode mode) {
        switch (mode) {
            case BUFFER:
            case STATIC:
                List<PendingStep> steps = getPendingSteps();
                steps.add(new PendingStep(description, isPassed, takeScreenshot));
                break;

            case IMMEDIATE:
                writeStepDirectly(description, isPassed, takeScreenshot, getDriver());
                break;
        }
    }

    /**
     * Método principal y único para manejar el buffer
     * @param action Acción a realizar con el buffer
     * @param failureDescription Descripción del fallo (si aplica)
     * @param takeScreenshot Si debe tomar screenshot del fallo (si aplica)
     */
    public static void processBuffer(BufferAction action, String failureDescription, boolean takeScreenshot) {
        List<PendingStep> steps = getPendingSteps();
        WebDriver driver = getDriver();

        switch (action) {
            case COMMIT_SUCCESS:
                for (PendingStep step : steps) {
                    writeStepDirectlyWithStoredScreenshot(step);
                }
                break;

            case COMMIT_WITH_FAILURE:
                for (PendingStep step : steps) {
                    writeStepDirectlyWithStoredScreenshot(step);
                }
                writeStepDirectly(failureDescription, false, takeScreenshot, driver);
                break;

            case COMMIT_MERGED_FAILURE:
                if (steps.isEmpty()) {
                    writeStepDirectly(failureDescription, false, takeScreenshot, driver);
                    break;
                }

                for (int i = 0; i < steps.size() - 1; i++) {
                    writeStepDirectlyWithStoredScreenshot(steps.get(i));
                }

                PendingStep lastStep = steps.get(steps.size() - 1);
                String mergedMessage = lastStep.getDescription() + "<br>" + failureDescription;

                String failureScreenshot = null;
                if (takeScreenshot) {
                    try {
                        failureScreenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
                    } catch (Exception e) {
                        System.err.println("Error capturando screenshot de fallo: " + e.getMessage());
                    }
                }
                writeStepWithCustomScreenshot(mergedMessage, false, failureScreenshot);
                break;

            case DISCARD_AND_FAIL:
                writeStepDirectly(failureDescription, false, takeScreenshot, driver);
                break;
        }

        steps.clear();
    }

    private static void writeStepDirectlyWithStoredScreenshot(PendingStep step) {
        if (ExtentManager.getTest() == null) {
            System.err.println("No hay test activo para crear step: " + step.getDescription());
            return;
        }

        Status status = step.isPassed() ? Status.PASS : Status.FAIL;

        if (step.shouldTakeScreenshot() && step.getScreenshotBase64() != null) {
            try {
                String styleName = step.isPassed() ? "success" : "error";
                String imageHtml = ScreenshotUtils.generateScreenshotHtml(
                        step.getScreenshotBase64(), styleName, step.getDescription());
                ExtentManager.getTest().log(status, step.getDescription() + "<br>" + imageHtml);
            } catch (Exception e) {
                ExtentManager.getTest().log(status, step.getDescription());
                ExtentManager.getTest().log(Status.WARNING, "Error mostrando screenshot: " + e.getMessage());
            }
        } else {
            ExtentManager.getTest().log(status, step.getDescription());
        }
    }

    private static void writeStepWithCustomScreenshot(String stepDescription, boolean isPassed, String screenshotBase64) {
        if (ExtentManager.getTest() == null) {
            System.err.println("No hay test activo para crear step: " + stepDescription);
            return;
        }

        Status status = isPassed ? Status.PASS : Status.FAIL;

        if (screenshotBase64 != null) {
            try {
                String styleName = isPassed ? "success" : "error";
                String imageHtml = ScreenshotUtils.generateScreenshotHtml(screenshotBase64, styleName, stepDescription);
                ExtentManager.getTest().log(status, stepDescription + "<br>" + imageHtml);
            } catch (Exception e) {
                ExtentManager.getTest().log(status, stepDescription);
                ExtentManager.getTest().log(Status.WARNING, "Error mostrando screenshot: " + e.getMessage());
            }
        } else {
            ExtentManager.getTest().log(status, stepDescription);
        }
    }

    private static void writeStepDirectly(String stepDescription, boolean isPassed, boolean takeScreenshot, WebDriver driver) {
        if (ExtentManager.getTest() == null) {
            System.err.println("No hay test activo para crear step: " + stepDescription);
            return;
        }

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
}
