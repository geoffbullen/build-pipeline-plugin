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

import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Job;

import java.io.IOException;

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
 *
 * @author RayC
 *
 */
public class BuildPipelineViewTest extends HudsonTestCase {

    private static final String TEST_PROJECT1 = "Project 1";
    private static final String TEST_PROJECT2 = "Downstream Project 2";
    private static final String TEST_PROJECT3 = "Downstream Project 3";

    private static final String VALUE = "value";
    private static final String TEST_BUILD_PIPELINE_VIEW_NAME = "MyTestView";
    private static final String TEST_NAME = "TestView";
    private static final String TEST_TITLE = "Build-Pipeline View Test";
    private static final String TEST_SELECTED_JOB = "Test Project";
    private static final String TEST_DISPLAYED_BUILDS = "5";
    private FreeStyleProject project1, project2, project3;

    @Override
    @Before
    public void setUp() {
        try {
            super.setUp();
            project1 = createFreeStyleProject(TEST_PROJECT1);
            project2 = createFreeStyleProject(TEST_PROJECT2);
            project3 = createFreeStyleProject(TEST_PROJECT3);

            // Add TEST_PROJECT2 & TEST_PROJECT3 as a post build action: build other project
            project1.getPublishersList().add(new BuildPipelineTrigger(TEST_PROJECT2));
            project2.getPublishersList().add(new BuildPipelineTrigger(TEST_PROJECT3));

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

            final HtmlPage testViewConfigPage = new WebClient().goTo("../view/" + TEST_BUILD_PIPELINE_VIEW_NAME + "/configure");
            final HtmlElement buildViewTitle = testViewConfigPage.getElementByName("buildViewTitle");
            buildViewTitle.setAttribute(VALUE, "My Build View Title");
            final HtmlElement selectedJob = testViewConfigPage.getElementByName("selectedJob");
            selectedJob.setAttribute(VALUE, "TEST_PROJECT1");
            final HtmlElement noOfDisplayedBuilds = testViewConfigPage.getElementByName("noOfDisplayedBuilds");
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
    public void testGetSelectedProject() {
        // Test a valid case
        BuildPipelineView testView = new BuildPipelineView(TEST_NAME, TEST_TITLE, TEST_PROJECT1, TEST_DISPLAYED_BUILDS);
        Job<?, ?> testSelectedProject = testView.getSelectedProject();

        assertEquals("Check Abstract Project Name", TEST_PROJECT1, testSelectedProject.getName());

        // Test the null case
        testView = new BuildPipelineView(TEST_NAME, TEST_TITLE, TEST_SELECTED_JOB, TEST_DISPLAYED_BUILDS);
        testSelectedProject = testView.getSelectedProject();

        assertNull("Check testJob ", testSelectedProject);
    }


    @Test
    public void testHasBuildPermission() {
        BuildPipelineView testView = new BuildPipelineView(TEST_NAME, TEST_TITLE, TEST_PROJECT1, TEST_DISPLAYED_BUILDS);
        assertTrue(testView.hasBuildPermission(project1));
    }

}
