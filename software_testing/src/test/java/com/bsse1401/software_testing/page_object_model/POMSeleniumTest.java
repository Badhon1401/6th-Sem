package com.bsse1401.software_testing.page_object_model;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.Random;

public class POMSeleniumTest {
    private WebDriver driver;


    private String generateRandomEmail() {
        return "user" + System.currentTimeMillis() + "@test.com";
    }

    private String generateRandomName() {
        String[] names = {"John", "Alice", "Bob", "Emma", "David", "Sarah"};
        return names[new Random().nextInt(names.length)];
    }

    @BeforeEach
    public void setUp() {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");


        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1148, 693));


        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testBoardCreationAndManagement() {
        try {

            String firstUser = generateRandomName();
            String firstUserEmail = generateRandomEmail();
            String secondUserEmail = generateRandomEmail();
            String password = "bbbbb";


            SignInPage signInPage = new SignInPage(driver)
                    .goToSignInPage();


            DashboardPage dashboardPage = signInPage
                    .clickCreateAccount()
                    .createAccount(firstUser, "LastName", firstUserEmail, password,password);


            BoardPage boardPage = dashboardPage
                    .clickAddNewBoard()
                    .createBoard("asf");


            boardPage
                    .addList("fsasfd")
                    .updateListName("afsasfd")
                    .addCard("afdsaf")
                    .addAnotherList("asdfasd")
                    .addMember(secondUserEmail);


            BoardsPage boardsPage = dashboardPage
                    .goToBoardsPage();


            boardsPage.dragBoardToViewContainer("6-asf");


            BoardPage secondBoard = boardsPage
                    .goToCreateBoard()
                    .createBoard("asefasdf");


            secondBoard
                    .addList("szv")
                    .addAnotherList("asdf");


            signInPage = dashboardPage.signOut();


            dashboardPage = signInPage.signIn(secondUserEmail, password);


            dashboardPage
                    .clickAddNewBoard()
                    .createBoard("adfsa");

            System.out.println("Test completed successfully!");

        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}