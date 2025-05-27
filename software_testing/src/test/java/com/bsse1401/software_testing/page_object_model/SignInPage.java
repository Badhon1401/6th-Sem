package com.bsse1401.software_testing.page_object_model;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SignInPage extends BasePage {
    // Locators
    private final By createAccountLink = By.linkText("Create new account");
    private final By emailInput = By.id("user_email");
    private final By passwordInput = By.id("user_password");
    private final By signInButton = By.cssSelector("button");
    private final By viewContainer = By.cssSelector(".view-container");

    private final String PAGE_URL = "http://localhost:4000/sign_in";

    public SignInPage(WebDriver driver) {
        super(driver);
    }

    public SignInPage goToSignInPage() {
        navigateTo(PAGE_URL);
        return this;
    }

    public SignUpPage clickCreateAccount() {
        clickWithRetry(createAccountLink);
        waitForPageStability();
        return new SignUpPage(driver);
    }


    public DashboardPage signIn(String email, String password) {
        sendKeysWithWait(emailInput, email);
        clickWithRetry(viewContainer);
        waitForPageStability();

        sendKeysWithWait(passwordInput, password);
        clickWithRetry(signInButton);
        waitForPageStability();
        System.out.println("Signed in with email: " + email);
        return new DashboardPage(driver);
    }
}
