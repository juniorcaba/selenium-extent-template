package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExtentManager {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    public static void createInstance() {
        if (extent == null) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String reportPath = System.getProperty("user.dir") + "/reports/ExtentReport_" + timestamp + ".html";

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setTheme(Theme.DARK);
            sparkReporter.config().setDocumentTitle("ToolsQA Automation Report");
            sparkReporter.config().setReportName("Test Execution Report");
            sparkReporter.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("Browser", "Chrome");
            extent.setSystemInfo("Environment", "QA");
            extent.setSystemInfo("User", System.getProperty("user.name"));
        }
    }

    public static ExtentTest createTest(String testName, String description) {
        ExtentTest extentTest = extent.createTest(testName, description);
        test.set(extentTest);
        return extentTest;
    }

    public static ExtentTest getTest() {
        return test.get();
    }

    public static void flushReport() {
        if (extent != null) {
            extent.flush();
        }
    }

    public static void removeTest() {
        test.remove();
    }

    // Método para crear steps con screenshot opcional
    public static void createStep(String stepDescription, boolean isPassed, boolean takeScreenshot) {
        ExtentTest currentTest = getTest();
        if (currentTest != null) {
            Status status = isPassed ? Status.PASS : Status.FAIL;
            currentTest.log(status, stepDescription);

            if (takeScreenshot) {
                captureScreenshotForStep(stepDescription);
            }
        }
    }

    // Método sobrecargado sin screenshot
    public static void createStep(String stepDescription, boolean isPassed) {
        createStep(stepDescription, isPassed, false);
    }

    // Método para capturar screenshot y embedderlo en base64
    private static void captureScreenshotForStep(String stepDescription) {
        try {
            // Obtener el driver del thread actual (necesitaremos pasarlo como parámetro)
            // Por ahora, lo manejaremos desde BaseTest
        } catch (Exception e) {
            getTest().log(Status.WARNING, "No se pudo capturar screenshot: " + e.getMessage());
        }
    }
}