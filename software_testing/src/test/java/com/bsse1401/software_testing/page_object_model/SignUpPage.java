package com.bsse1401.software_testing.page_object_model;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SignUpPage extends BasePage {

    private final By firstNameInput = By.id("user_first_name");
    private final By lastNameInput = By.id("user_last_name");
    private final By emailInput = By.id("user_email");
    private final By passwordInput = By.id("user_password");
    private final By passwordConfirmationInput = By.id("user_password_confirmation");
    private final By signUpButton = By.cssSelector("button");

    public SignUpPage(WebDriver driver) {
        super(driver);
    }


    public DashboardPage createAccount(String firstName, String lastName, String email, String password,String confirmPassword) {
        sendKeysWithWait(firstNameInput, firstName);
        sendKeysWithWait(lastNameInput, lastName);
        sendKeysWithWait(emailInput, email);
        sendKeysWithWait(passwordInput, password);
        sendKeysWithWait(passwordConfirmationInput, confirmPassword);

        clickWithRetry(signUpButton);
        waitForPageStability();

        try {

            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        waitForPageStability();
        System.out.println("Account created successfully with email: " + email);

        return new DashboardPage(driver);
    }
}
