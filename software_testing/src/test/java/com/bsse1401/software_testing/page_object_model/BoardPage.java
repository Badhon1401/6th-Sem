package com.bsse1401.software_testing.page_object_model;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class BoardPage extends BasePage {

    private final By addListButton = By.cssSelector(".inner");
    private final By addNewListButton = By.cssSelector(".add-new > .inner");
    private final By listNameHeader = By.cssSelector("h4");
    private final By addNewCardLink = By.linkText("Add a new card...");
    private final By addMemberButton = By.cssSelector("li > .add-new");

    public BoardPage(WebDriver driver) {
        super(driver);
    }


    public BoardPage addList(String listName) {
        clickWithRetry(addListButton);
        waitForPageStability();

        return createList(listName);
    }

    public BoardPage addAnotherList(String listName) {
        clickWithRetry(addNewListButton);
        waitForPageStability();

        return createList(listName);
    }


    private BoardPage createList(String listName) {
        sendKeysWithWait(By.id("list_name"), listName);
        clickWithRetry(By.cssSelector("button"));
        waitForPageStability();
        System.out.println("List created: " + listName);

        return this;
    }


    public BoardPage updateListName(String newListName) {
        clickWithRetry(listNameHeader);
        waitForPageStability();

        sendKeysWithWait(By.id("list_name"), newListName);
        clickWithRetry(By.cssSelector("button"));
        waitForPageStability();
        System.out.println("List name updated to: " + newListName);

        return this;
    }


    public BoardPage addCard(String cardName) {
        clickWithRetry(addNewCardLink);
        waitForPageStability();

        sendKeysWithWait(By.id("card_name"), cardName);
        clickWithRetry(By.cssSelector("button"));
        waitForPageStability();
        System.out.println("Card added: " + cardName);

        return this;
    }

    public BoardPage addMember(String memberEmail) {
        clickWithRetry(addMemberButton);
        waitForPageStability();

        sendKeysWithWait(By.id("crawljax_member_email"), memberEmail);
        clickWithRetry(By.cssSelector("button"));
        waitForPageStability();
        System.out.println("Member added: " + memberEmail);

        return this;
    }
}
