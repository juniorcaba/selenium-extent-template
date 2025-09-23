package basetest;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.*;
import utils.ConfigReader;
import utils.ExtentManager;
import utils.ScreenshotUtils;
import com.aventstack.extentreports.Status;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Listeners(utils.ExtentTestListener.class)
public class BaseTest {
    private static ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<List<PendingStep>> pendingStepsThreadLocal = new ThreadLocal<>();

    // Browser options constants
    private static final String HEADLESS_ARG = "--headless";
    private static final String NO_SANDBOX_ARG = "--no-sandbox";
    private static final String DISABLE_DEV_SHM_ARG = "--disable-dev-shm-usage";
    private static final String DISABLE_WEB_SECURITY_ARG = "--disable-web-security";
    private static final String DISABLE_VIZ_COMPOSITOR_ARG = "--disable-features=VizDisplayCompositor";

    public enum StepMode {
        BUFFER,     // Add to buffer
        IMMEDIATE,  // Write immediately
        STATIC      // Alias for BUFFER
    }

    public enum BufferAction {
        COMMIT_SUCCESS,           // All steps as successful
        COMMIT_WITH_FAILURE,      // Successful steps + new failure
        COMMIT_MERGED_FAILURE,    // Last step combined with failure message
        DISCARD_AND_FAIL         // Discard buffer and write only failure
    }

    public static class PendingStep {
        private final String description;
        private final boolean isPassed;
        private final boolean takeScreenshot;
        private final String screenshotBase64;

        public PendingStep(String description, boolean isPassed, boolean takeScreenshot) {
            this.description = description;
            this.isPassed = isPassed;
            this.takeScreenshot = takeScreenshot;
            this.screenshotBase64 = takeScreenshot && getDriverSafe() != null ? captureScreenshotAsBase64() : null;
        }

        private String captureScreenshotAsBase64() {
            try {
                WebDriver driver = getDriverSafe();
                if (driver != null) {
                    return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
                }
            } catch (Exception e) {
                System.err.println("Error capturing screenshot: " + e.getMessage());
            }
            return null;
        }

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

        // Configuration summary
        System.out.println("=== TEST CONFIGURATION ===");
        try {
            System.out.println("Base URL: " + ConfigReader.getBaseUrl());
            System.out.println("Browser: " + ConfigReader.getBrowser());
            System.out.println("Headless: " + ConfigReader.isHeadless());
            System.out.println("Timeout: " + ConfigReader.getTimeout());
            System.out.println("===========================");
        } catch (Exception e) {
            System.out.println("Using default configuration");
            System.out.println("===========================");
        }
    }

    @BeforeMethod
    public void setUp() {
        WebDriver driver = createDriver();
        driverThreadLocal.set(driver);
        pendingStepsThreadLocal.set(new ArrayList<>());

        configureTimeouts(driver);
        driver.manage().window().maximize();
        navigateToBaseUrl();
    }

    private WebDriver createDriver() {
        try {
            String browser = ConfigReader.getBrowser().toLowerCase();
            boolean headless = ConfigReader.isHeadless();

            switch (browser) {
                case "chrome":
                    return createChromeDriver(headless);
                case "firefox":
                    return createFirefoxDriver(headless);
                case "edge":
                    return createEdgeDriver(headless);
                default:
                    System.out.println("Unknown browser: '" + browser + "'. Using Chrome as default.");
                    return createChromeDriver(headless);
            }
        } catch (Exception e) {
            System.err.println("Error creating driver: " + e.getMessage());
            return createChromeDriver(false); // Fallback to basic Chrome
        }
    }

    private WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments(HEADLESS_ARG, NO_SANDBOX_ARG, DISABLE_DEV_SHM_ARG);
        }

        options.addArguments(DISABLE_WEB_SECURITY_ARG, DISABLE_VIZ_COMPOSITOR_ARG);
        return new ChromeDriver(options);
    }

    private WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();

        if (headless) {
            options.addArguments(HEADLESS_ARG);
        }

        return new FirefoxDriver(options);
    }

    private WebDriver createEdgeDriver(boolean headless) {
        try {
            WebDriverManager.edgedriver().setup();
        } catch (Exception e) {
            // Fallback to manual driver if WebDriverManager fails
            String edgeDriverPath = System.getProperty("user.dir") + "/src/main/resources/drivers/msedgedriver.exe";
            System.setProperty("webdriver.edge.driver", edgeDriverPath);

            File driverFile = new File(edgeDriverPath);
            if (!driverFile.exists()) {
                throw new RuntimeException("Edge driver not found at: " + edgeDriverPath);
            }
        }

        EdgeOptions options = new EdgeOptions();

        if (headless) {
            options.addArguments(HEADLESS_ARG, NO_SANDBOX_ARG, DISABLE_DEV_SHM_ARG);
        }

        options.addArguments(DISABLE_WEB_SECURITY_ARG, DISABLE_VIZ_COMPOSITOR_ARG);
        return new EdgeDriver(options);
    }

    private void configureTimeouts(WebDriver driver) {
        try {
            int timeout = ConfigReader.getTimeout();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeout));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        } catch (Exception e) {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        }
    }

    private void navigateToBaseUrl() {
        try {
            String baseUrl = ConfigReader.getBaseUrl();
            getDriver().get(baseUrl);
        } catch (Exception e) {
            String defaultUrl = "https://automationexercise.com";
            System.err.println("Error getting base URL: " + e.getMessage());
            getDriver().get(defaultUrl);
        }
    }

    @AfterMethod
    public void tearDown() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("Error closing driver: " + e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }

        List<PendingStep> steps = pendingStepsThreadLocal.get();
        if (steps != null) {
            steps.clear();
            pendingStepsThreadLocal.remove();
        }

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
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("Error closing driver in @AfterTest: " + e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new RuntimeException("WebDriver not initialized. Make sure @BeforeMethod has been executed.");
        }
        return driver;
    }

    private static WebDriver getDriverSafe() {
        return driverThreadLocal.get();
    }

    // Navigation Helper Methods
    public static void navigateToUrl(String url) {
        try {
            getDriver().get(url);
        } catch (Exception e) {
            throw e;
        }
    }

    public static void switchToEnvironment(String environment) {
        try {
            String url = ConfigReader.getUrl(environment);
            if (url == null) {
                url = ConfigReader.getBaseUrl();
            }
            navigateToUrl(url);

        } catch (Exception e) {
            throw e;
        }
    }

    public static void navigateToPageWithValidation(String pageUrl, String pageName, Object validationLocator) {
        try {
            getDriver().get(pageUrl);
            createStep("Navigating to " + pageName + ": " + pageUrl, true, true, StepMode.BUFFER);

            if (validationLocator != null) {
                createStep("Validating " + pageName + " loaded successfully", true, false, StepMode.BUFFER);
            }

            processBuffer(BufferAction.COMMIT_SUCCESS, null, false);

        } catch (Exception e) {
            String errorMsg = "Error navigating to " + pageName + ": " + e.getMessage();
            processBuffer(BufferAction.COMMIT_WITH_FAILURE, errorMsg, true);
            throw e;
        }
    }


    // Step Management Methods
    private static List<PendingStep> getPendingSteps() {
        List<PendingStep> steps = pendingStepsThreadLocal.get();
        if (steps == null) {
            steps = new ArrayList<>();
            pendingStepsThreadLocal.set(steps);
        }
        return steps;
    }

    /**
     * Main method for creating test steps
     * @param description Step description
     * @param isPassed Whether the step passed or failed
     * @param takeScreenshot Whether to capture screenshot
     * @param mode Processing mode (BUFFER, IMMEDIATE, STATIC)
     */
    public static void createStep(String description, boolean isPassed, boolean takeScreenshot, StepMode mode) {
        switch (mode) {
            case BUFFER:
            case STATIC:
                List<PendingStep> steps = getPendingSteps();
                steps.add(new PendingStep(description, isPassed, takeScreenshot));
                break;
            case IMMEDIATE:
                writeStepDirectly(description, isPassed, takeScreenshot, getDriverSafe());
                break;
        }
    }

    /**
     * Main method for processing the step buffer
     * @param action Action to perform with the buffer
     * @param failureDescription Failure description (if applicable)
     * @param takeScreenshot Whether to capture failure screenshot (if applicable)
     */
    public static void processBuffer(BufferAction action, String failureDescription, boolean takeScreenshot) {
        List<PendingStep> steps = getPendingSteps();
        WebDriver driver = getDriverSafe();

        switch (action) {
            case COMMIT_SUCCESS:
                steps.forEach(BaseTest::writeStepDirectlyWithStoredScreenshot);
                break;

            case COMMIT_WITH_FAILURE:
                steps.forEach(BaseTest::writeStepDirectlyWithStoredScreenshot);
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
                if (takeScreenshot && driver != null) {
                    try {
                        failureScreenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
                    } catch (Exception e) {
                        System.err.println("Error capturing failure screenshot: " + e.getMessage());
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

    // Private Step Writing Methods
    private static void writeStepDirectlyWithStoredScreenshot(PendingStep step) {
        if (ExtentManager.getTest() == null) {
            System.err.println("No active test to create step: " + step.getDescription());
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
                ExtentManager.getTest().log(Status.WARNING, "Error displaying screenshot: " + e.getMessage());
            }
        } else {
            ExtentManager.getTest().log(status, step.getDescription());
        }
    }

    private static void writeStepWithCustomScreenshot(String stepDescription, boolean isPassed, String screenshotBase64) {
        if (ExtentManager.getTest() == null) {
            System.err.println("No active test to create step: " + stepDescription);
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
                ExtentManager.getTest().log(Status.WARNING, "Error displaying screenshot: " + e.getMessage());
            }
        } else {
            ExtentManager.getTest().log(status, stepDescription);
        }
    }

    private static void writeStepDirectly(String stepDescription, boolean isPassed, boolean takeScreenshot, WebDriver driver) {
        if (ExtentManager.getTest() == null) {
            System.err.println("No active test to create step: " + stepDescription);
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

    public static void navigateToPage(String pageUrl) {
        getDriver().get(pageUrl);
    }

    public static void refreshPage() {
        getDriver().navigate().refresh();
    }

    public static void goBack() {
        getDriver().navigate().back();
    }

    public static void goForward() {
        getDriver().navigate().forward();
    }

    public static String getPageTitle() {
        return getDriver().getTitle();
    }

    public static boolean isTitleContains(String expectedTitle) {
        return getPageTitle().contains(expectedTitle);
    }

    public static void navigateToPageWithValidation(String pageUrl, String pageName) {
        navigateToPageWithValidation(pageUrl, pageName, null);
    }

    public static String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    public static boolean isOnExpectedUrl(String expectedUrl) {
        return getCurrentUrl().contains(expectedUrl);
    }
}
