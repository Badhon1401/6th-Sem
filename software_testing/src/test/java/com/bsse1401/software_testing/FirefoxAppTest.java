package com.bsse1401.software_testing;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Firefox test for local web application running on port 4000
 */
public class FirefoxAppTest {
    private WebDriver driver;
    private Map<String, Object> vars;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();
        // Comment the next line if you want to see the browser UI during test
        // options.addArguments("-headless");

        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1148, 692));

        // Create an explicit wait with 15 seconds timeout
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        vars = new HashMap<>();
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Clicks on an element with retry support for stale elements
     * @param by The locator to find the element
     * @param maxRetries Maximum number of retries before failing
     */
    private void clickWithRetry(By by, int maxRetries) {
        int retries = 0;
        while (retries < maxRetries) {
            try {
                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(by));
                element.click();
                return; // Success, exit method
            } catch (StaleElementReferenceException e) {
                if (retries == maxRetries - 1) {
                    throw e; // Last retry failed, rethrow
                }
                System.out.println("Stale element, retrying click on: " + by);
                retries++;
                // Small wait before retry
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Test
    public void testAppWorkflow() {
        // Navigate to the application
        driver.get("http://localhost:4000/");

        // Sign out first (if signed in)
        try {
            clickWithRetry(By.cssSelector("#crawler-sign-out > span"), 3);
        } catch (Exception e) {
            // Already signed out, continue with test
            System.out.println("User already signed out or element not found");
        }

        // Click button (likely sign in)
        clickWithRetry(By.cssSelector("button"), 3);

        // Add new board
        clickWithRetry(By.id("add_new_board"), 3);

        // Enter board name
        WebElement boardNameField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("board_name"))
        );
        boardNameField.sendKeys("Test Board");

        // Click submit button
        clickWithRetry(By.cssSelector("button"), 3);

        // Click on inner element
        clickWithRetry(By.cssSelector(".inner"), 3);

        // Enter list name
        WebElement listNameField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("list_name"))
        );
        listNameField.sendKeys("List 1");

        // Click submit button
        clickWithRetry(By.cssSelector("button"), 3);

        // Click add new inner element
        clickWithRetry(By.cssSelector(".add-new > .inner"), 3);

        // Enter second list name
        listNameField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("list_name"))
        );
        listNameField.sendKeys("List 2");

        // Click submit button
        clickWithRetry(By.cssSelector("button"), 3);

        // Navigate to boards
        clickWithRetry(By.cssSelector("#boards_nav span"), 3);

        // Click on a specific board (4th one in the list)
        clickWithRetry(By.cssSelector("li:nth-child(4) > a"), 3);

        // Click add new inner element
        clickWithRetry(By.cssSelector(".add-new > .inner"), 3);

        // Enter third list name
        listNameField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("list_name"))
        );
        listNameField.sendKeys("List 3");

        // Click submit button
        clickWithRetry(By.cssSelector("button"), 3);

        // Add a new card
        clickWithRetry(By.cssSelector("li > .add-new"), 3);

        // Enter email for member
        WebElement emailField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("crawljax_member_email"))
        );
        emailField.sendKeys("test@example.com");

        // Click submit button
        clickWithRetry(By.cssSelector("button"), 3);

        // Click on canvas
        clickWithRetry(By.cssSelector(".canvas"), 3);

        // Sign out
        clickWithRetry(By.cssSelector("#crawler-sign-out > span"), 3);
    }
}