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

import hudson.model.DependencyGraph;
import hudson.model.DependencyGraph.Dependency;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;

import java.io.IOException;
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
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
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
    private static FreeStyleProject project1, project2;

    @Override
    @Before
    public void setUp() {
        try {
            super.setUp();
        } catch (Exception exception) {
            exception.toString();
        }

    }

    @Test
    public void testBuildPipelineTrigger() {
        try {
            project1 = createFreeStyleProject(TEST_PROJECT1);
            // Add TEST_PROJECT2 as a post build action: build other project
            project1.getPublishersList().add(new BuildPipelineTrigger(TEST_PROJECT2));
            // Important; we must do this step to ensure that the dependency graphs are updated
            Hudson.getInstance().rebuildDependencyGraph();

            BuildPipelineTrigger myBPTrigger = new BuildPipelineTrigger(TEST_PROJECT1);

            assertNotNull("A valid BuildPipelineTrigger should have been created.", myBPTrigger);

            assertEquals("BuildPipelineTrigger downstream project is " + TEST_PROJECT1, TEST_PROJECT1, myBPTrigger.getDownstreamProjectNames());
        } catch (IOException e) {
            e.toString();
        }
    }

    @Test
    public void testProjectConfigurePage() {
        try {
            project1 = createFreeStyleProject(TEST_PROJECT1);
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
            final HtmlButton saveNewDownstreamJobButton = (HtmlButton) last(configureNewDownstreamJobForm
                    .getHtmlElementsByTagName("button"));
            saveNewDownstreamJobButton.removeAttribute("disabled");
            configureNewDownstreamJobForm.submit(saveNewDownstreamJobButton);

            // Select the Build-Pipeline-Plugin radio button
            final HtmlForm configureNewUpstreamJobForm = testUpstreamProjectConfigurePage.getFormByName("config");
            final HtmlCheckBoxInput BuildPipelinePluginCheckBox = testUpstreamProjectConfigurePage
                    .getElementByName(BUILD_PIPELINE_PLUGIN_NAME);
            BuildPipelinePluginCheckBox.setChecked(true);
            BuildPipelinePluginCheckBox.click();

            // Test that the Downstream Project Name select has the correct entries
            final HtmlSelect downstreamProjectSelect = testUpstreamProjectConfigurePage.getElementByName("downstreamProjectNames");
            assertEquals("The downstream project select should have 3 elements", 3, downstreamProjectSelect.getOptionSize());
            assertEquals("One of the options should be " + TEST_UPSTREAM_PROJECT, TEST_UPSTREAM_PROJECT, downstreamProjectSelect
                    .getOptionByValue(TEST_UPSTREAM_PROJECT).getValueAttribute());
            assertEquals("One of the options should be " + TEST_DOWNSTREAM_PROJECT, TEST_DOWNSTREAM_PROJECT, downstreamProjectSelect
                    .getOptionByValue(TEST_DOWNSTREAM_PROJECT).getValueAttribute());

            // Set the Downstream Project Name
            final HtmlButton saveNewUpstreamJobButton = (HtmlButton) last(configureNewUpstreamJobForm.getHtmlElementsByTagName("button"));
            saveNewUpstreamJobButton.removeAttribute("disabled");

            // Retrieve the configuration page for the Upstream project and test that the correct downstream project has been added
            final HtmlPage testUpstreamProjectFinalPage = new WebClient().goTo(".." + TEST_DOWNSTREAM_PROJECT_HREF);
            assertEquals("The downstream project should have been " + TEST_DOWNSTREAM_PROJECT_HREF, TEST_DOWNSTREAM_PROJECT_HREF,
                    testUpstreamProjectFinalPage.getAnchorByHref(TEST_DOWNSTREAM_PROJECT_HREF).getHrefAttribute());
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
    public void testBuildDependencyGraph() {
        try {
            project1 = createFreeStyleProject(TEST_PROJECT1);
            BuildPipelineTrigger trigger2 = new BuildPipelineTrigger(TEST_PROJECT2);
            // Add TEST_PROJECT2 as a post build action: build other project
            project1.getPublishersList().add(trigger2);
            // Important; we must do this step to ensure that the dependency graphs are updated
            //Hudson.getInstance().rebuildDependencyGraph();
            DependencyGraph graph = new DependencyGraph();

            trigger2.buildDependencyGraph(project1, graph);

            List<Dependency> dsDependencies = graph.getDownstreamDependencies(project1);
            assertEquals(project1, dsDependencies.get(0).getUpstreamProject());
        } catch (Exception e) {
            e.toString();
        }
    }

    private HtmlPage createNewJobPage(String newProjectName) throws Exception {

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
