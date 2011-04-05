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
package au.com.centrumsystems.hudson.plugin.buildpipeline.trigger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.tasks.BuildTrigger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * BuildPipelineTrigger test class
 * 
 * @author KevinV
 * 
 */
public class BuildPipelineTriggerTest extends HudsonTestCase {

    private static final String VALUE = "value";
    private static final String TEST_UPSTREAM_PROJECT = "TestUpstreamProject";
    private static final String TEST_DOWNSTREAM_PROJECT = "TestDownstreamProject";
    private static final String TEST_DOWNSTREAM_PROJECT_HREF = "/job/" + TEST_DOWNSTREAM_PROJECT + "/";
    private static final String BUILD_PIPELINE_PLUGIN_NAME = "au-com-centrumsystems-hudson-plugin-buildpipeline-trigger-BuildPipelineTrigger";
    private static final String CREATE_NEW_JOB_PAGE = "../view/All/newJob";

    private static final String TEST_PROJECT1 = "Project 1";
    private static final String TEST_PROJECT2 = "Downstream Project 2";

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testBuildPipelineTrigger() throws IOException {

        FreeStyleProject project1 = createFreeStyleProject(TEST_PROJECT1);
        // Add TEST_PROJECT2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(TEST_PROJECT2));
        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        BuildPipelineTrigger myBPTrigger = new BuildPipelineTrigger(TEST_PROJECT1);

        assertNotNull("A valid BuildPipelineTrigger should have been created.", myBPTrigger);

        assertEquals("BuildPipelineTrigger downstream project is " + TEST_PROJECT1, TEST_PROJECT1, myBPTrigger.getDownstreamProjectNames());

    }

    @Test
    public void testProjectConfigurePage() throws IOException, SAXException, ElementNotFoundException {

        FreeStyleProject project1 = createFreeStyleProject(TEST_PROJECT1);
        // Add TEST_PROJECT2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(TEST_PROJECT2));
        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Create two new projects
        final HtmlPage testDownstreamProjectConfigurePage = createNewJobPage(TEST_DOWNSTREAM_PROJECT);
        final HtmlPage testUpstreamProjectConfigurePage = createNewJobPage(TEST_UPSTREAM_PROJECT);

        // Test if the HTML pages were successfully returned
        assertNotNull("Create New Upstream Job Form Return Page failed", testUpstreamProjectConfigurePage);
        assertNotNull("Create New Upstream Job Form Return Page failed", testDownstreamProjectConfigurePage);

        // Save the configuration of the Downstream project
        final HtmlForm configureNewDownstreamJobForm = testDownstreamProjectConfigurePage.getFormByName("config");
        final HtmlButton saveNewDownstreamJobButton = (HtmlButton) last(configureNewDownstreamJobForm.getHtmlElementsByTagName("button"));
        saveNewDownstreamJobButton.removeAttribute("disabled");
        configureNewDownstreamJobForm.submit(saveNewDownstreamJobButton);

        // Select the Build-Pipeline-Plugin radio button
        final HtmlForm configureNewUpstreamJobForm = testUpstreamProjectConfigurePage.getFormByName("config");
        final HtmlCheckBoxInput BuildPipelinePluginCheckBox = testUpstreamProjectConfigurePage.getElementByName(BUILD_PIPELINE_PLUGIN_NAME);
        BuildPipelinePluginCheckBox.setChecked(true);
        BuildPipelinePluginCheckBox.click();

        // Set the Downstream Project Name
        final HtmlButton saveNewUpstreamJobButton = (HtmlButton) last(configureNewUpstreamJobForm.getHtmlElementsByTagName("button"));
        saveNewUpstreamJobButton.removeAttribute("disabled");

        // Retrieve the configuration page for the Upstream project and test that the correct downstream project has been added
        final HtmlPage testUpstreamProjectFinalPage = new WebClient().goTo(".." + TEST_DOWNSTREAM_PROJECT_HREF);
        assertEquals("The downstream project should have been " + TEST_DOWNSTREAM_PROJECT_HREF, TEST_DOWNSTREAM_PROJECT_HREF,
                testUpstreamProjectFinalPage.getAnchorByHref(TEST_DOWNSTREAM_PROJECT_HREF).getHrefAttribute());

    }

    @Test
    public void testBuildDependencyGraphAndUnrelatedProjectsDontAffectEachother() throws IOException {
        // SETUP
        FreeStyleProject project1 = createFreeStyleProject("Proj1");
        String proj2 = "Proj2";
        createFreeStyleProject(proj2);
        String proj3 = "Proj3";
        createFreeStyleProject(proj3);
        FreeStyleProject project4 = createFreeStyleProject("Proj4");
        String proj5 = "Proj5";
        createFreeStyleProject(proj5);
        String proj6 = "Proj6";
        createFreeStyleProject(proj6);

        BuildTrigger trigger2 = new BuildTrigger(proj2, false);
        BuildPipelineTrigger trigger3 = new BuildPipelineTrigger(proj3);

        // add 2 downstream builds for project1, one auto and one build pipeline one
        project1.getPublishersList().add(trigger2);
        project1.getPublishersList().add(trigger3);
        // project1.save();
        // Important; we must do this step to ensure that the dependency graphs are updated
        hudson.rebuildDependencyGraph();

        BuildTrigger trigger5 = new BuildTrigger(proj5, false);
        BuildPipelineTrigger trigger6 = new BuildPipelineTrigger(proj6);

        // Add TEST_PROJECT2 as a post build action: build other project
        project4.getPublishersList().add(trigger5);
        project4.getPublishersList().add(trigger6);
        // project4.save();
        // Important; we must do this step to ensure that the dependency graphs are updated
        hudson.rebuildDependencyGraph();

        // VERIFY
        List<String> projectNames = new ArrayList<String>();
        for (BuildTrigger buildTrigger : project1.getPublishersList().getAll(BuildTrigger.class)) {
            projectNames.add(buildTrigger.getChildProjectsValue());
        }
        assertThat(projectNames, is(Arrays.asList(proj2, proj3)));

        projectNames = new ArrayList<String>();
        for (BuildTrigger buildTrigger : project4.getPublishersList().getAll(BuildTrigger.class)) {
            projectNames.add(buildTrigger.getChildProjectsValue());
        }
        assertThat(projectNames, is(Arrays.asList(proj5, proj6)));
    }

    private HtmlPage createNewJobPage(String newProjectName) throws SAXException, IOException {

        final HtmlPage testProjectConfigurePage = new WebClient().goTo(CREATE_NEW_JOB_PAGE);

        HtmlForm createNewJobForm = null;
        // Retrieve the New Job form
        List<HtmlForm> formsList = testProjectConfigurePage.getForms();
        for (HtmlForm currentForm : formsList) {
            if (currentForm.getActionAttribute().equalsIgnoreCase("createItem")) {
                createNewJobForm = currentForm;
                break;
            }
        }

        // Enter the New Job Name
        final HtmlTextInput newJobName = (HtmlTextInput) testProjectConfigurePage.getElementById("name");
        newJobName.setAttribute(VALUE, newProjectName);
        newJobName.click();

        // Select a Free Style Job
        final HtmlRadioButtonInput freeStyleProjectRadioButton = testProjectConfigurePage.getElementByName("mode");
        freeStyleProjectRadioButton.setChecked(true);
        freeStyleProjectRadioButton.click();

        final HtmlButton createNewJobButton = (HtmlButton) last(createNewJobForm.getHtmlElementsByTagName("button"));
        createNewJobButton.removeAttribute("disabled");

        return (HtmlPage) createNewJobForm.submit(createNewJobButton);
    }

}
