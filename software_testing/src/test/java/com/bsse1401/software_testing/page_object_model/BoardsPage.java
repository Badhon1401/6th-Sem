package com.bsse1401.software_testing.page_object_model;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class BoardsPage extends BasePage {

    private final By addNewBoardButton = By.cssSelector(".add-new > .inner");
    private final By addNewBoardId = By.id("add_new_board");
    private final By viewContainer = By.cssSelector(".view-container");

    public BoardsPage(WebDriver driver) {
        super(driver);
    }


    public CreateBoardPage clickAddNewBoard() {
        clickWithRetry(addNewBoardButton);
        waitForPageStability();

        return new CreateBoardPage(driver);
    }

    public BoardPage clickOnBoard(String boardId) {
        clickWithRetry(By.id(boardId));
        waitForPageStability();

        return new BoardPage(driver);
    }


    public BoardPage dragBoardToViewContainer(String boardId) {
        try {
            dragAndDropWithWait(By.id(boardId), viewContainer);
            System.out.println("Drag and drop performed");
        } catch (Exception e) {
            System.out.println("Drag and drop failed: " + e.getMessage());

            clickWithRetry(viewContainer);
            System.out.println("Used click as fallback for drag-and-drop");
        }
        waitForPageStability();

        return new BoardPage(driver);
    }


    public CreateBoardPage goToCreateBoard() {
        clickWithRetry(addNewBoardId);
        waitForPageStability();

        return new CreateBoardPage(driver);
    }
}
