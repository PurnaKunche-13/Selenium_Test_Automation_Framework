package com.purna.automation.framework;

import com.purna.automation.base.BaseTest;
import com.purna.automation.base.DriverFactory;
import com.purna.automation.listeners.ReportManager;
import com.purna.automation.listeners.TestListener;
import com.purna.automation.utilities.ScreenshotUtils;
import java.io.IOException;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/** Explicit diagnostic suite only: a nonzero exit is expected. */
public class FailureEvidenceTest extends BaseTest {
    @Override protected String startPageUrl() {
        return "data:text/html,%3Chtml%3E%3Cbody%3E%3Ch1%3EScreenshot%20diagnostic%3C/h1%3E%3C/body%3E%3C/html%3E";
    }
    @Test(groups = "diagnostic", description = "Intentional failure to verify screenshot, report and cleanup")
    public void screenshotIsCapturedBeforeBrowserQuit() {
        Assert.fail("Intentional diagnostic failure. Verify PNG and embedded report image.");
    }
    @Override @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        super.tearDown(result);
        Assert.assertTrue(DriverFactory.current().isEmpty(), "Driver slot must be cleared after cleanup");
        Object recorded = result.getAttribute(TestListener.EVIDENCE);
        Assert.assertTrue(recorded instanceof ScreenshotUtils.Evidence, "Listener must record screenshot evidence");
        var evidence = (ScreenshotUtils.Evidence) recorded;
        try {
            Assert.assertTrue(Files.isRegularFile(evidence.path()), "PNG must be saved");
            Assert.assertNotNull(ImageIO.read(evidence.path().toFile()), "Evidence must be a decodable image");
            ReportManager.flush();
            String html = Files.readString(ReportManager.directory().resolve("index.html"));
            Assert.assertTrue(html.contains(evidence.base64()), "Screenshot must be embedded in the report");
            Assert.assertTrue(html.contains("screenshotIsCapturedBeforeBrowserQuit"), "Test name must be in the report");
            result.setAttribute("automation.diagnosticVerified", true);
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot verify diagnostic evidence", failure);
        }
    }
}
