package au.com.centrumsystems.hudson.plugin.buildpipeline;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.NullColumnHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.PipelineHeaderExtension;
import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.BuildCardComponent;
import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.PipelinePage;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.Assert.*;

public class BuildPipelineViewDisplayNameTest {
    protected WebDriver webDriver;

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void before() {
        webDriver = new FirefoxDriver();
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(30));
    }

    @After
    public void cleanUpWebDriver() {
        if (webDriver != null) {
            webDriver.close();
            webDriver.quit();
        }
    }

    /**
     * checks that pipeline box uses displayName
     */
    @Ignore
    @Test
    public void testDisplayName() throws Exception {
        final FreeStyleProject freestyle1 = j.createFreeStyleProject("freestyle1");

        freestyle1.setDisplayName("fancyname1");

        freestyle1.scheduleBuild();
        j.waitUntilNoActivity();

        BuildPipelineView pipeline = new BuildPipelineView("pipeline", "",
                new DownstreamProjectGridBuilder(freestyle1.getFullName()),
                "1", //num displayed
                false, //trigger only latest
                true,  // manual trigger
                false, // parameters
                false, //params in header
                false, //definition header
                1, null, null, null, null, null);



        j.getInstance().addView(pipeline);

        PipelinePage pipelinePage = new PipelinePage(webDriver, pipeline.getViewName(), j.getURL());
        pipelinePage.open();

        BuildCardComponent buildCardComponent = pipelinePage.buildCard(1, 1, 2);

        assertTrue("The displayName should be visible",
                buildCardComponent.hasDisplayName("fancyname1"));
    }
}
