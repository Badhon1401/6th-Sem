
package com.bsse1401.software_testing.page_object_model;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected FluentWait<WebDriver> fluentWait;
    protected JavascriptExecutor js;

    protected static final int DEFAULT_TIMEOUT = 30;
    protected static final int DEFAULT_MAX_RETRIES = 5;
    protected static final int POLLING_INTERVAL = 500;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        this.fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT))
                .pollingEvery(Duration.ofMillis(POLLING_INTERVAL))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
        this.js = (JavascriptExecutor) driver;
    }


    protected void waitForPageStability() {
        try {

            wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));


            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    protected boolean elementExists(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }


    protected void clickWithRetry(By by) {
        clickWithRetry(by, DEFAULT_MAX_RETRIES);
    }

    protected void clickWithRetry(By by, int maxRetries) {
        int retries = 0;
        Exception lastException = null;

        while (retries < maxRetries) {
            try {

                if (!elementExists(by)) {
                    System.out.println("Element not found, waiting: " + by);

                    Thread.sleep(1000);
                    retries++;
                    continue;
                }


                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(by));


                js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
                Thread.sleep(500);


                try {
                    element.click();
                    System.out.println("Successfully clicked on: " + by);
                    return;
                } catch (ElementClickInterceptedException e) {

                    System.out.println("Direct click failed, trying JS click on: " + by);
                    js.executeScript("arguments[0].click();", element);
                    return;
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

           
            try {
                Thread.sleep(1000 * retries);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }


        if (lastException != null) {
            throw new RuntimeException("Failed to click element after " + maxRetries + " retries: " + by, lastException);
        }
    }


    protected void sendKeysWithWait(By by, String text) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(by));


            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(500);


            element.clear();
            element.sendKeys(Keys.CONTROL + "a");
            element.sendKeys(Keys.DELETE);


            for (char c : text.toCharArray()) {
                element.sendKeys(String.valueOf(c));
                Thread.sleep(50);
            }

            System.out.println("Successfully entered text into: " + by);
        } catch (Exception e) {
            throw new RuntimeException("Failed to enter text into element: " + by, e);
        }
    }


    protected void dragAndDropWithWait(By source, By target) {
        try {
            WebElement sourceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(source));
            WebElement targetElement = wait.until(ExpectedConditions.visibilityOfElementLocated(target));


            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", sourceElement);
            Thread.sleep(500);

            Actions actions = new Actions(driver);

            try {

                actions.dragAndDrop(sourceElement, targetElement).perform();
            } catch (Exception e) {

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

    public BasePage navigateTo(String url) {
        driver.get(url);
        waitForPageStability();
        System.out.println("Navigated to: " + url);
        return this;
    }
}

