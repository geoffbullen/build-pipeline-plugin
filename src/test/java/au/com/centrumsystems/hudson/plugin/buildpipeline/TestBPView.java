/*
 * The MIT License
 * 
 * Copyright (c) 2011, Centrumsystems Pty Ltd
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.model.BallColor;
import hudson.model.FreeStyleBuild;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Job;

import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.xml.sax.SAXException;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * Test Build Pipeline View
 * @author RayC
 * 
 */
public class TestBPView extends HudsonTestCase {

    private static BuildPipelineView testView;
    private static final String TEST_PROJECT1 = "JUnitJob";
    private static final String TEST_PROJECT2 = "JUnitJob2";

    private static final String VALUE = "value";
    private static final String TEST_BUILD_PIPELINE_VIEW_NAME = "MyTestView";
    private static final String TEST_NAME = "TestView";
    private static final String TEST_TITLE = "Centrum View Test";
    private static final String TEST_SELECTED_JOB = "Test Project";
    private static final String TEST_DISPLAYED_BUILDS = "5";
    private static FreeStyleProject project1;

    private static BuildPipelineTrigger buildPipelineTrigger;

    private static final boolean MANUAL_BUILD = true;

    @Before
    public void setUp() {
        try {
            super.setUp();
            project1 = createFreeStyleProject(TEST_PROJECT1);
            createFreeStyleProject(TEST_PROJECT2);

            // Add project2 as a post build action: build other project
            buildPipelineTrigger = new BuildPipelineTrigger(TEST_PROJECT2, false);
            project1.getPublishersList().add(buildPipelineTrigger);

            // Important; we must do this step to ensure that the dependency graphs are updated
            Hudson.getInstance().rebuildDependencyGraph();

        } catch (Exception exception) {
            exception.toString();
        }

    }

    @Test
    public void testBuildPipelineViewPage() {
        try {
            
            final HtmlPage testViewPage = new WebClient().goTo("../newView");

            final HtmlForm form = testViewPage.getFormByName("createView");
            
            final HtmlTextInput viewName = (HtmlTextInput) testViewPage.getElementByName("name");
            viewName.setAttribute(VALUE, TEST_BUILD_PIPELINE_VIEW_NAME);            
            viewName.click();
            
            final HtmlRadioButtonInput radioButton = testViewPage.getElementByName("mode");
            radioButton.setChecked(true);
            radioButton.click();
            
            final HtmlButton addViewButton = (HtmlButton) last(form.getHtmlElementsByTagName("button"));
            addViewButton.removeAttribute("disabled");
            
            final HtmlPage formReturnPage = (HtmlPage) form.submit(addViewButton);
            
            assertNotNull("Form Return Page", formReturnPage);
            assertNotNull("Add view button found", addViewButton);
            

            final HtmlPage testViewConfigPage = new WebClient().goTo("../view/"+TEST_BUILD_PIPELINE_VIEW_NAME+"/configure");
            final HtmlElement buildViewTitle= (HtmlElement) testViewConfigPage.getElementByName("buildViewTitle");
            buildViewTitle.setAttribute(VALUE, "My Build View Title");
            final HtmlElement selectedJob= (HtmlElement) testViewConfigPage.getElementByName("selectedJob");
            selectedJob.setAttribute(VALUE, "TEST_PROJECT1");
            final HtmlElement noOfDisplayedBuilds= (HtmlElement) testViewConfigPage.getElementByName("noOfDisplayedBuilds");
            noOfDisplayedBuilds.setAttribute(VALUE, "3");
            
            final HtmlForm viewConfigForm = testViewConfigPage.getFormByName("viewConfig");
            final HtmlButton viewConfigFormButton = (HtmlButton) last(viewConfigForm.getHtmlElementsByTagName("button"));
            viewConfigForm.submit(viewConfigFormButton);
            
            assertNotNull("noOfDisplayedBuilds", noOfDisplayedBuilds);

            
        } catch (IOException e) {
            e.toString();
        } catch (SAXException e) {
            e.toString();
        } catch (ElementNotFoundException e) {
            e.toString();
        } catch (Exception e) {
            e.toString();
        }
    }
    
    @Test
    public void testGetJob() {
        // Test a valid case
        testView = new BuildPipelineView(TEST_NAME, TEST_TITLE, TEST_PROJECT1, TEST_DISPLAYED_BUILDS);
        Job<?, ?> testJob = testView.getJob();

        assertEquals("Check Abstract Project Name", TEST_PROJECT1, testJob.getName());

        // Test the null case
        testView = new BuildPipelineView(TEST_NAME, TEST_TITLE, TEST_SELECTED_JOB, TEST_DISPLAYED_BUILDS);
        testJob = testView.getJob();

        assertNull("Check testJob ", testJob);

    }

    @Test
    public void testBuildPipelineTrigger() {
        assertNotNull("Build Pipeline Trigger is not null", buildPipelineTrigger);
        buildPipelineTrigger.setManualBuild(MANUAL_BUILD);
        buildPipelineTrigger.setDownstreamProjectName(TEST_PROJECT2);
        assertEquals("Is manual build", MANUAL_BUILD, buildPipelineTrigger.getIsManualBuild());
        assertEquals("Is manual build", TEST_PROJECT2, buildPipelineTrigger.getDownstreamProjectName());
    }

    @Test
    public void testGetProjectPipeline() throws IOException {

        testView = new BuildPipelineView(TEST_NAME, TEST_TITLE, TEST_PROJECT1, TEST_DISPLAYED_BUILDS);

        List<AbstractProject<?, ?>> projectPipeline = testView.getProjectPipeline();

        assertEquals("Check Ball Color", BallColor.GREY, projectPipeline.get(0).getIconColor());

        // Test the null case
        testView = new BuildPipelineView(TEST_NAME, TEST_TITLE, TEST_SELECTED_JOB, TEST_DISPLAYED_BUILDS);

        projectPipeline = testView.getProjectPipeline();
        assertNotNull("Check Project Pipeline", projectPipeline);

        testView.setSelectedJob(TEST_PROJECT1);
        assertEquals("CHECK Selected Job", TEST_PROJECT1, testView.getSelectedJob());

        testView.setBuildViewTitle(TEST_PROJECT1);
        assertEquals("CHECK BuildViewTitle", TEST_PROJECT1, testView.getBuildViewTitle());

        testView.setNoOfDisplayedBuilds(TEST_DISPLAYED_BUILDS);
        assertEquals("Check No Of display builds", TEST_DISPLAYED_BUILDS, testView.getNoOfDisplayedBuilds());

        try {
            // another way of kick off the build
            final FreeStyleBuild build1 = buildAndAssertSuccess(project1);

            testView.setLastBuildOfAPipeline(build1);

            // TRUE next build is required as "build1" has downstream build
            assertTrue("next build is required as build1 has downstream build", testView.isNextBuildRequire());
            assertNotNull("Check Build URL", testView.getBuildResultURL(build1));
            assertNotNull("Check Build History", testView.getBuildsHistory());
            assertNotNull("Check Build getHudsonResult", testView.getHudsonResult(build1));
            assertNotNull("Check Build getLastBuildOfAPipeline", testView.getLastBuildOfAPipeline());
            assertNotNull("Check Next Project from build pipeline", testView.getNextProject(build1));
        
        } catch (Exception e) {
            e.toString();
        }

        assertNotNull("Test to make sure that testView is not null", testView);
    }

    @Test
    public void testBuildTriggerPostBuildActionsOnJobConfigPage() {
         
        try {
            // Load the job configuration page for project1
            final HtmlPage project1ConfigPage = createWebClient().getPage(project1, "configure");
            assertNotNull("project1 config page cannot be null as the project1 is created", project1ConfigPage);
            
            // When the job configuration page is submitted, Hudson will call BuildPipelineTrigger.DescriptorImpl.newInstance() method.
            // But this method call
            submit(project1ConfigPage.getFormByName("config"));
        } catch (IOException e) {
            e.toString();
        } catch (SAXException e) {
            e.toString();
        } catch (ElementNotFoundException e) {
            e.toString();
        } catch (Exception e) {
            e.toString();
        }

    }

}
