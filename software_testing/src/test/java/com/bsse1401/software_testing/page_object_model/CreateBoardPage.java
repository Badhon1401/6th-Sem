package com.bsse1401.software_testing.page_object_model;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CreateBoardPage extends BasePage {

    private final By boardNameInput = By.id("board_name");
    private final By createButton = By.cssSelector("button");

    public CreateBoardPage(WebDriver driver) {
        super(driver);
    }


    public BoardPage createBoard(String boardName) {
        sendKeysWithWait(boardNameInput, boardName);
        clickWithRetry(createButton);
        waitForPageStability();
        System.out.println("Board created: " + boardName);

        return new BoardPage(driver);
    }
}
