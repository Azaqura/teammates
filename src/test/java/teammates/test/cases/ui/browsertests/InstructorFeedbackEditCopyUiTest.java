package teammates.test.cases.ui.browsertests;

import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.datatransfer.DataBundle;
import teammates.common.util.AppUrl;
import teammates.common.util.Const;
import teammates.test.pageobjects.Browser;
import teammates.test.pageobjects.BrowserPool;
import teammates.test.pageobjects.InstructorFeedbackEditPage;

public class InstructorFeedbackEditCopyUiTest extends BaseUiTestCase {
    private static Browser browser;
    private static InstructorFeedbackEditPage feedbackEditPage;
    private static DataBundle testData;
    private static String instructorId;
    private static String courseId;
    private static String feedbackSessionName;

    @BeforeClass
    public static void classSetup() {
        printTestClassHeader();
        testData = loadDataBundle("/InstructorFeedbackEditCopyTest.json");
        removeAndRestoreTestDataOnServer(testData);
        instructorId = testData.accounts.get("instructorWithSessions").googleId;
        courseId = testData.courses.get("course").getId();
        feedbackSessionName = testData.feedbackSessions.get("openSession").getFeedbackSessionName();

        browser = BrowserPool.getBrowser();
    }

    @Test
    public void allTests() throws Exception {
        feedbackEditPage = getFeedbackEditPage();
        
        ______TS("Submit empty course list");
        feedbackEditPage.clickFsCopyButton();
        feedbackEditPage.getFsCopyToModal().waitForModalToLoad();

        // Full HTML verification already done in InstructorFeedbackEditPageUiTest
        feedbackEditPage.verifyHtmlMainContent("/instructorFeedbackEditCopyPage.html");
        
        feedbackEditPage.getFsCopyToModal().clickSubmitButton();
        feedbackEditPage.getFsCopyToModal().waitForFormSubmissionErrorMessagePresence();
        assertTrue(feedbackEditPage.getFsCopyToModal().isFormSubmissionStatusMessageVisible());
        feedbackEditPage.getFsCopyToModal().verifyStatusMessage(Const.StatusMessages.FEEDBACK_SESSION_COPY_NONESELECTED);
        
        feedbackEditPage.getFsCopyToModal().clickCloseButton();
        
        ______TS("Copying fails due to fs with same name in course selected");
        feedbackEditPage.clickFsCopyButton();
        feedbackEditPage.getFsCopyToModal().waitForModalToLoad();
        feedbackEditPage.getFsCopyToModal().fillFormWithAllCoursesSelected(feedbackSessionName);
        
        feedbackEditPage.getFsCopyToModal().clickSubmitButton();
        feedbackEditPage.getFsCopyToModal().waitForFormSubmissionErrorMessagePresence();
        assertTrue(feedbackEditPage.getFsCopyToModal().isFormSubmissionStatusMessageVisible());
        
        feedbackEditPage.getFsCopyToModal()
                        .verifyStatusMessage(
                                 String.format(Const.StatusMessages.FEEDBACK_SESSION_COPY_ALREADYEXISTS,
                                               feedbackSessionName,
                                               testData.courses.get("course").getId()));
        

        // Full HTML verification already done in InstructorFeedbackEditPageUiTest
        feedbackEditPage.verifyHtmlMainContent("/instructorFeedbackEditCopyFail.html");
        
        feedbackEditPage.getFsCopyToModal().clickCloseButton();
        
        ______TS("Copying fails due to fs with invalid name");
        feedbackEditPage.clickFsCopyButton();
        feedbackEditPage.getFsCopyToModal().waitForModalToLoad();
        feedbackEditPage.getFsCopyToModal().fillFormWithAllCoursesSelected("Invalid name | for feedback session");
        
        feedbackEditPage.getFsCopyToModal().clickSubmitButton();
        
        feedbackEditPage.getFsCopyToModal().waitForFormSubmissionErrorMessagePresence();
        assertTrue(feedbackEditPage.getFsCopyToModal().isFormSubmissionStatusMessageVisible());
        feedbackEditPage.getFsCopyToModal().verifyStatusMessage(
                "\"Invalid name | for feedback session\" is not acceptable to TEAMMATES as a/an "
                + "feedback session name because it contains invalid characters. "
                + "All feedback session name must start with an alphanumeric character, "
                + "and cannot contain any vertical bar (|) or percent sign (%).");
        
        
        feedbackEditPage.getFsCopyToModal().clickCloseButton();
        
        ______TS("Successful case");
        feedbackEditPage.clickFsCopyButton();
        feedbackEditPage.getFsCopyToModal().waitForModalToLoad();
        feedbackEditPage.getFsCopyToModal().fillFormWithAllCoursesSelected("New name!");
        
        feedbackEditPage.getFsCopyToModal().clickSubmitButton();
        feedbackEditPage.waitForPageToLoad();
        
        feedbackEditPage.verifyStatus(Const.StatusMessages.FEEDBACK_SESSION_COPIED);
        feedbackEditPage.waitForElementPresence(By.id("table-sessions"));

        // Full HTML verification already done in InstructorFeedbackEditPageUiTest
        feedbackEditPage.verifyHtmlMainContent("/instructorFeedbackEditCopySuccess.html");
        
    }

    @AfterClass
    public static void classTearDown() {
        BrowserPool.release(browser);
    }

    private InstructorFeedbackEditPage getFeedbackEditPage() {
        AppUrl feedbackPageLink = createUrl(Const.ActionURIs.INSTRUCTOR_FEEDBACK_EDIT_PAGE)
                                             .withUserId(instructorId)
                                             .withCourseId(courseId)
                                             .withSessionName(feedbackSessionName);
        return loginAdminToPage(browser, feedbackPageLink, InstructorFeedbackEditPage.class);
    }

}
