package pages;

import basetest.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    private static final Logger logger = LoggerFactory.getLogger(BasePage.class);



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

    public void goTo(String url){
        driver.get(url);
    }
}