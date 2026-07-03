package guru.qa.niffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Selenide.*;

public class FriendsPage {
    private final SelenideElement tabFriends = $x("//h2[contains(text()= 'Friends')]");
    private final SelenideElement tabAllPeople = $("[href = '/people/all']");
    private final SelenideElement titleListFriends = $$("div").findBy(text("My friends"));
    private final SelenideElement titleFriendsRequest = $$("div").findBy(text("Friend requests"));

    private final ElementsCollection friendsTableRows = $("#friends").$$("tbody tr");
    private final ElementsCollection allPeopleTableRows = $("#all").$$("tbody tr");
    private final ElementsCollection requestsTableRows = $("#requests").$$("tbody tr");

    private final SelenideElement linkMain = $(".link[href='/main']");
    private final SelenideElement newSpendingButton = $x("//a[text()='New spending']");
    private final SelenideElement personIconButton = $("button > div > svg[data-testid='PersonIcon']");
    private final SelenideElement textThereAreNoUserYet = $$("div").findBy(text("There are no users yet"));

    public MainPage openMainPage() {
        linkMain.shouldBe(visible).click();
        return new MainPage();
    }

    public EditSpendingPage clickNewSpendingButton() {
        newSpendingButton.shouldBe(visible).click();
        return new EditSpendingPage();
    }

    public FriendsPage clickPersonIconButton() {
        personIconButton.shouldBe(visible).click();
        return new FriendsPage();
    }

    public FriendsPage openFriendsTable() {
        tabFriends.shouldBe(visible).click();
        return new FriendsPage();
    }

    public FriendsPage openAllPeopleTable() {
        tabAllPeople.shouldBe(visible).click();
        return new FriendsPage();
    }

    public void checkFriendsTableIsEmpty() {
        textThereAreNoUserYet.shouldBe(visible);
        titleListFriends.shouldNotBe(visible);
    }

    public void checkFriendVisibleInTable(String friendName) {
        titleListFriends.shouldBe(visible);
        friendsTableRows.shouldHave(sizeGreaterThan(0));
        findFriendRow(friendName).shouldBe(visible);
        findFriendRow(friendName).findAll("td").get(1).shouldHave(text("Unfriend"));

    }

    public void checkOutcomeRequestBeVisible(String name) {
        allPeopleTableRows.shouldHave(sizeGreaterThan(0));
        findAllPeopleRow(name).shouldBe(visible);
        findAllPeopleRow(name).findAll("td").get(1).shouldHave(text("Waiting..."));

    }

    public void checkIncomeRequestBeVisible(String name) {
        titleFriendsRequest.shouldBe(visible);
        requestsTableRows.shouldHave(sizeGreaterThan(0));
        findRequestRow(name).shouldBe(visible);
        findRequestRow(name).findAll("td").get(1).find(byText("Accept")).shouldBe(visible);
        findRequestRow(name).findAll("td").get(1).find(byText("Decline")).shouldBe(visible);

    }

    public SelenideElement findFriendRow(String name) {
        return friendsTableRows.findBy(text(name));
    }

    public SelenideElement findAllPeopleRow(String name) {
        return allPeopleTableRows.findBy(text(name));
    }

    public SelenideElement findRequestRow(String name) {
        return requestsTableRows.findBy(text(name));
    }


}
