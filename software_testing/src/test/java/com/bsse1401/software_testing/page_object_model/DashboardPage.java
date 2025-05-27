package com.bsse1401.software_testing.page_object_model;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {

    private final By addNewBoardId = By.id("add_new_board");
    private final By addNewBoardClass = By.cssSelector(".add-new-board");
    private final By addNewBoardXPath = By.xpath("//div[contains(text(), 'Add new board')]");
    private final By addNewBoardInner = By.cssSelector(".add-new > .inner");
    private final By boardsLink = By.linkText("Boards");
    private final By viewAllBoardsLink = By.linkText("View all boards");
    private final By signOutButton = By.cssSelector("#crawler-sign-out > span");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }


    public CreateBoardPage clickAddNewBoard() {
        System.out.println("Attempting to find add_new_board element");


        By addNewBoardLocator = addNewBoardId;
        if (!elementExists(addNewBoardLocator)) {
            System.out.println("add_new_board not found by ID, trying alternative selectors");


            if (elementExists(addNewBoardClass)) {
                addNewBoardLocator = addNewBoardClass;
            } else if (elementExists(addNewBoardXPath)) {
                addNewBoardLocator = addNewBoardXPath;
            } else if (elementExists(addNewBoardInner)) {
                addNewBoardLocator = addNewBoardInner;
            }
        }

        clickWithRetry(addNewBoardLocator);
        waitForPageStability();

        return new CreateBoardPage(driver);
    }


    public BoardsPage goToBoardsPage() {
        clickWithRetry(boardsLink);
        waitForPageStability();

        clickWithRetry(viewAllBoardsLink);
        waitForPageStability();
        System.out.println("Navigated to boards view");

        return new BoardsPage(driver);
    }


    public SignInPage signOut() {
        clickWithRetry(signOutButton);
        waitForPageStability();
        System.out.println("Signed out");

        return new SignInPage(driver);
    }
}
