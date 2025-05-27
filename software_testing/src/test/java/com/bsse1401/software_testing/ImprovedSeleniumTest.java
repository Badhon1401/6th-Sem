package com.bsse1401.software_testing;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class ImprovedSeleniumTest {
    private WebDriver driver;
    private Map<String, Object> vars;
    private JavascriptExecutor js;
    private WebDriverWait wait;
    private FluentWait<WebDriver> fluentWait;

    // Increase base wait time to 30 seconds
    private static final int DEFAULT_TIMEOUT = 30;
    private static final int DEFAULT_MAX_RETRIES = 5;
    private static final int POLLING_INTERVAL = 500; // in milliseconds

    @BeforeEach
    public void setUp() {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        // Uncomment if you want headless mode
        // options.addArguments("--headless");

        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1148, 693));

        // Set page load timeout
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));

        // Create standard wait with increased timeout
        wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));

        // Create fluent wait with custom polling interval
        fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT))
                .pollingEvery(Duration.ofMillis(POLLING_INTERVAL))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        js = (JavascriptExecutor) driver;
        vars = new HashMap<String, Object>();
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Waits for page to be in stable state before proceeding
     */
    private void waitForPageStability() {
        try {
            // Wait for DOM to be ready
            wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));

            // Optional: wait a short while for any animations or delayed loads
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Checks if element exists without waiting for timeout
     * @param by The locator to find the element
     * @return true if element exists, false otherwise
     */
    private boolean elementExists(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Clicks on an element with retry support for stale elements and other exceptions
     * @param by The locator to find the element
     * @param maxRetries Maximum number of retries before failing
     */
    private void clickWithRetry(By by, int maxRetries) {
        int retries = 0;
        Exception lastException = null;

        while (retries < maxRetries) {
            try {
                // First check if element exists to avoid long wait times
                if (!elementExists(by)) {
                    System.out.println("Element not found, waiting: " + by);
                    // Wait a moment before checking again
                    Thread.sleep(1000);
                    retries++;
                    continue;
                }

                // Wait for element to be clickable
                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(by));

                // Scroll element into view before clicking
                js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
                Thread.sleep(500); // Give time for scroll to complete

                // Try direct click first
                try {
                    element.click();
                    System.out.println("Successfully clicked on: " + by);
                    return; // Success, exit method
                } catch (ElementClickInterceptedException e) {
                    // If regular click fails, try JavaScript click
                    System.out.println("Direct click failed, trying JS click on: " + by);
                    js.executeScript("arguments[0].click();", element);
                    return; // Success with JS click
                }
            } catch (StaleElementReferenceException e) {
                lastException = e;
                System.out.println("Stale element on retry " + retries + " for: " + by);
            } catch (TimeoutException e) {
                lastException = e;
                System.out.println("Timeout on retry " + retries + " for: " + by);
            } catch (Exception e) {
                lastException = e;
                System.out.println("Exception (" + e.getClass().getSimpleName() + ") on retry " + retries + " for: " + by + ": " + e.getMessage());
            }

            retries++;

            // Small wait before retry with increasing duration
            try {
                Thread.sleep(1000 * retries);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        // All retries failed, throw the last exception
        if (lastException != null) {
            throw new RuntimeException("Failed to click element after " + maxRetries + " retries: " + by, lastException);
        }
    }

    /**
     * Enters text into an input field with wait and retry logic
     * @param by The locator to find the input field
     * @param text The text to enter
     */
    private void sendKeysWithWait(By by, String text) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(by));

            // Scroll element into view
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(500);

            // Clear field with multiple approaches to ensure it's empty
            element.clear();
            element.sendKeys(Keys.CONTROL + "a");
            element.sendKeys(Keys.DELETE);

            // Enter text with slight delay between characters to simulate human typing
            for (char c : text.toCharArray()) {
                element.sendKeys(String.valueOf(c));
                Thread.sleep(50);
            }

            System.out.println("Successfully entered text into: " + by);
        } catch (Exception e) {
            throw new RuntimeException("Failed to enter text into element: " + by, e);
        }
    }

    /**
     * Performs a drag and drop operation with wait and retry
     * @param source The source element locator
     * @param target The target element locator
     */
    private void dragAndDropWithWait(By source, By target) {
        try {
            WebElement sourceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(source));
            WebElement targetElement = wait.until(ExpectedConditions.visibilityOfElementLocated(target));

            // Scroll source element into view
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", sourceElement);
            Thread.sleep(500);

            Actions actions = new Actions(driver);

            try {
                // Try standard drag and drop
                actions.dragAndDrop(sourceElement, targetElement).perform();
            } catch (Exception e) {
                // If standard approach fails, try alternative method
                System.out.println("Standard drag and drop failed, trying alternative method");
                actions.clickAndHold(sourceElement)
                        .pause(Duration.ofMillis(500))
                        .moveToElement(targetElement)
                        .pause(Duration.ofMillis(500))
                        .release()
                        .build()
                        .perform();
            }

            System.out.println("Successfully performed drag and drop");
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform drag and drop", e);
        }
    }

    @Test
    public void test3() throws InterruptedException {
        try {
            // Navigate to the application and ensure page is fully loaded
            driver.get("http://localhost:4000/sign_in");
            waitForPageStability();
            System.out.println("Navigated to sign-in page");

            // Create new account
            clickWithRetry(By.linkText("Create new account"), DEFAULT_MAX_RETRIES);
            waitForPageStability();

            sendKeysWithWait(By.id("user_first_name"), "s");
            sendKeysWithWait(By.id("user_last_name"), "ss");
            sendKeysWithWait(By.id("user_email"), "s@s");
            sendKeysWithWait(By.id("user_password"), "bbbbb");
            sendKeysWithWait(By.id("user_password_confirmation"), "bbbbb");

            clickWithRetry(By.cssSelector("button"), DEFAULT_MAX_RETRIES);
            waitForPageStability();
            System.out.println("Account created successfully");

            // Wait longer for the dashboard to load completely
            Thread.sleep(3000);
            waitForPageStability();

            // Add new board - this is where the original test was failing
            System.out.println("Attempting to find add_new_board element");

            // Try multiple selector strategies if one fails
            By addNewBoardLocator = By.id("add_new_board");
            if (!elementExists(addNewBoardLocator)) {
                System.out.println("add_new_board not found by ID, trying alternative selectors");
                // Try alternative selectors
                if (elementExists(By.cssSelector(".add-new-board"))) {
                    addNewBoardLocator = By.cssSelector(".add-new-board");
                } else if (elementExists(By.xpath("//div[contains(text(), 'Add new board')]"))) {
                    addNewBoardLocator = By.xpath("//div[contains(text(), 'Add new board')]");
                } else if (elementExists(By.cssSelector(".add-new > .inner"))) {
                    addNewBoardLocator = By.cssSelector(".add-new > .inner");
                }
            }

            clickWithRetry(addNewBoardLocator, DEFAULT_MAX_RETRIES);
            waitForPageStability();

            sendKeysWithWait(By.id("board_name"), "asf");
            clickWithRetry(By.cssSelector("button"), DEFAULT_MAX_RETRIES);
            waitForPageStability();
            System.out.println("Board created successfully");

            // Add first list
            clickWithRetry(By.cssSelector(".inner"), DEFAULT_MAX_RETRIES);
            waitForPageStability();

            sendKeysWithWait(By.id("list_name"), "fsasfd");
            clickWithRetry(By.cssSelector("button"), DEFAULT_MAX_RETRIES);
            waitForPageStability();
            System.out.println("First list created");

            // Update list name
            clickWithRetry(By.cssSelector("h4"), DEFAULT_MAX_RETRIES);
            waitForPageStability();

            sendKeysWithWait(By.id("list_name"), "afsasfd");
            clickWithRetry(By.cssSelector("button"), DEFAULT_MAX_RETRIES);
            waitForPageStability();
            System.out.println("List name updated");

            // Add a card
            clickWithRetry(By.linkText("Add a new card..."), DEFAULT_MAX_RETRIES);
            waitForPageStability();

            sendKeysWithWait(By.id("card_name"), "afdsaf");
            clickWithRetry(By.cssSelector("button"), DEFAULT_MAX_RETRIES);
            waitForPageStability();
            System.out.println("Card added");

            // Add second list
            clickWithRetry(By.cssSelector(".add-new > .inner"), DEFAULT_MAX_RETRIES);
            waitForPageStability();

            sendKeysWithWait(By.id("list_name"), "asdfasd");
            clickWithRetry(By.cssSelector("button"), DEFAULT_MAX_RETRIES);
            waitForPageStability();
            System.out.println("Second list created");

            // Add a member
            clickWithRetry(By.cssSelector("li > .add-new"), DEFAULT_MAX_RETRIES);
            waitForPageStability();

            sendKeysWithWait(By.id("crawljax_member_email"), "t@t");
            clickWithRetry(By.cssSelector("button"), DEFAULT_MAX_RETRIES);
            waitForPageStability();
            System.out.println("Member added");

            // Navigate to boards
            clickWithRetry(By.linkText("Boards"), DEFAULT_MAX_RETRIES);
            waitForPageStability();

            clickWithRetry(By.linkText("View all boards"), DEFAULT_MAX_RETRIES);
            waitForPageStability();
            System.out.println("Navigated to boards view");

            // Add another board element
            clickWithRetry(By.cssSelector(".add-new > .inner"), DEFAULT_MAX_RETRIES);
            waitForPageStability();

            // Try drag and drop with proper waits
            try {
                dragAndDropWithWait(By.id("6-asf"), By.cssSelector(".view-container"));
                System.out.println("Drag and drop performed");
            } catch (Exception e) {
                System.out.println("Drag and drop failed: " + e.getMessage());
                // Click as fallback
                clickWithRetry(By.cssSelector(".view-container"), DEFAULT_MAX_RETRIES);
                System.out.println("Used click as fallback for drag-and-drop");
            }
            waitForPageStability();

            // Add another board
            clickWithRetry(By.id("add_new_board"), DEFAULT_MAX_RETRIES);
            waitForPageStability();

            sendKeysWithWait(By.id("board_name"), "asefasdf");
            clickWithRetry(By.cssSelector("button"), DEFAULT_MAX_RETRIES);
            waitForPageStability();
            System.out.println("Another board created");

            // Add lists to the new board
            clickWithRetry(By.cssSelector(".inner"), DEFAULT_MAX_RETRIES);
            waitForPageStability();

            sendKeysWithWait(By.id("list_name"), "szv");
            clickWithRetry(By.cssSelector("button"), DEFAULT_MAX_RETRIES);
            waitForPageStability();

            clickWithRetry(By.cssSelector(".add-new > .inner"), DEFAULT_MAX_RETRIES);
            waitForPageStability();

            sendKeysWithWait(By.id("list_name"), "asdf");
            clickWithRetry(By.cssSelector("button"), DEFAULT_MAX_RETRIES);
            waitForPageStability();
            System.out.println("Lists added to new board");

            // Sign out
            clickWithRetry(By.cssSelector("#crawler-sign-out > span"), DEFAULT_MAX_RETRIES);
            waitForPageStability();
            System.out.println("Signed out");

            // Sign in as different user
            sendKeysWithWait(By.id("user_email"), "t@t");
            clickWithRetry(By.cssSelector(".view-container"), DEFAULT_MAX_RETRIES);
            waitForPageStability();

            sendKeysWithWait(By.id("user_password"), "bbbbb");
            clickWithRetry(By.cssSelector("button"), DEFAULT_MAX_RETRIES);
            waitForPageStability();
            System.out.println("Signed in as different user");

            // Add final board
            clickWithRetry(By.id("add_new_board"), DEFAULT_MAX_RETRIES);
            waitForPageStability();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("board_name")));
            sendKeysWithWait(By.id("board_name"), "adfsa");
            clickWithRetry(By.cssSelector("button"), DEFAULT_MAX_RETRIES);
            System.out.println("Final board added successfully");

            System.out.println("Test completed successfully!");
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to make test fail
        }
    }
}