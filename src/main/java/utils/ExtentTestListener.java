package utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;
import basetest.BaseTest;

public class ExtentTestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();

        if (description == null || description.isEmpty()) {
            description = "Test execution for: " + testName;
        }

        ExtentManager.createTest(testName, description);
        // REMOVIDO: No agregar log de "Test started" para mantener reporte limpio
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        // Método vacío - no agregar entradas adicionales al reporte
        // Los PASS steps ya se manejan individualmente en BaseTest.createStepStatic()

        // COMENTADO: La siguiente línea genera el mensaje "Test PASSED" que no quieres
        // ExtentTest test = ExtentManager.getTest();
        // test.log(Status.PASS, MarkupHelper.createLabel("Test PASSED: " + result.getMethod().getMethodName(), ExtentColor.GREEN));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        test.log(Status.FAIL, MarkupHelper.createLabel("Test FAILED: " + result.getMethod().getMethodName(), ExtentColor.RED));

        // Log del error
        test.log(Status.FAIL, "Failure reason: " + result.getThrowable().getMessage());

        // OPCIONAL: Solo capturar screenshot en fallas si no hay ninguno en los steps
        // captureScreenshot(result, "FAILED", "error");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        test.log(Status.SKIP, MarkupHelper.createLabel("Test SKIPPED: " + result.getMethod().getMethodName(), ExtentColor.YELLOW));
        test.log(Status.SKIP, "Skip reason: " + result.getThrowable().getMessage());
    }

    @Override
    public void onFinish(org.testng.ITestContext context) {
        ExtentManager.flushReport();
    }

    /**
     * Método mantenido por si necesitas usarlo en el futuro
     * Actualmente comentado para evitar screenshots duplicados
     */
    private void captureScreenshot(ITestResult result, String status, String styleName) {
        try {
            Object testClass = result.getInstance();
            if (testClass instanceof BaseTest) {
                WebDriver driver = ((BaseTest) testClass).getDriver();
                if (driver != null) {
                    String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
                    String imageHtml = ScreenshotUtils.generateScreenshotHtml(base64Screenshot, styleName, status);
                    ExtentManager.getTest().log(Status.INFO, imageHtml);
                }
            }
        } catch (Exception e) {
            ExtentManager.getTest().log(Status.WARNING, ScreenshotUtils.getErrorMessage() + ": " + e.getMessage());
        }
    }
}