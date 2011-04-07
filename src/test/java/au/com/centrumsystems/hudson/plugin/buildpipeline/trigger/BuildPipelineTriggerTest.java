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

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
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

    private static final String BUILD_PIPELINE_PLUGIN_NAME = "au-com-centrumsystems-hudson-plugin-buildpipeline-trigger-BuildPipelineTrigger";
    private static final String CREATE_NEW_JOB_PAGE = "../view/All/newJob";

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Ignore
    @Test
    public void testBuildPipelineTrigger() throws IOException {
        String proj1 = "Proj1";
        String proj2 = "Proj2";
        FreeStyleProject project1 = createFreeStyleProject(proj1);
        // Add TEST_PROJECT2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2));
        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        BuildPipelineTrigger myBPTrigger = new BuildPipelineTrigger(proj1);

        assertNotNull("A valid BuildPipelineTrigger should have been created.", myBPTrigger);

        assertEquals("BuildPipelineTrigger downstream project is " + proj1, proj1, myBPTrigger.getDownstreamProjectNames());
    }

    @Test
    public void testProjectConfigurePage() throws IOException, SAXException, ElementNotFoundException {
        String proj1 = "Proj1";
        String proj2 = "Proj2";
        String proj1Href = ".." + "/job/" + proj1;
        String proj2Href = ".." + "/job/" + proj2;

        // Create two new projects
        final HtmlPage project1ConfigurePage = createNewFreeStyleProjectHtmlPage(proj1);
        final HtmlPage project2ConfigurePage = createNewFreeStyleProjectHtmlPage(proj2);

        // Test if the HTML pages were successfully returned
        assertNotNull("Create New Upstream Job Form Return Page failed", project1ConfigurePage);
        assertNotNull("Create New Downstream Job Form Return Page failed", project2ConfigurePage);

        // Save the configuration of the Downstream project
        final HtmlForm configureNewProject2Form = project1ConfigurePage.getFormByName("config");
        final HtmlButton saveNewProject2Button = (HtmlButton) last(configureNewProject2Form.getHtmlElementsByTagName("button"));
        saveNewProject2Button.removeAttribute("disabled");
        configureNewProject2Form.submit(saveNewProject2Button);

        // Select the Build-Pipeline-Plugin radio button
        final HtmlForm configureNewProject1Form = project2ConfigurePage.getFormByName("config");
        final HtmlCheckBoxInput buildPipelinePluginCheckBox = project2ConfigurePage.getElementByName(BUILD_PIPELINE_PLUGIN_NAME);
        buildPipelinePluginCheckBox.setChecked(true);
        buildPipelinePluginCheckBox.click();
        final HtmlTextInput buildPipelinePluginText = project1ConfigurePage.getElementByName("downstreamProjectNames");
        buildPipelinePluginText.setValueAttribute(proj2);

        // Set the Downstream Project Name
        final HtmlButton saveNewProject1Button = (HtmlButton) last(configureNewProject1Form.getHtmlElementsByTagName("button"));
        saveNewProject1Button.removeAttribute("disabled");
        // configureNewProject1Form.submit(saveNewProject1Button);
        final HtmlPage project1FinalPage = saveNewProject1Button.click();

        // Retrieve the configuration page for the Upstream project and test that the correct downstream project has been added
        // final HtmlPage project1FinalPage = new WebClient().goTo(proj1Href);
        assertEquals("The downstream project should have been " + proj2Href, proj2Href, project1FinalPage.getAnchorByHref(proj2Href)
                .getHrefAttribute());

    }

    @Test
    public void testOnDownstreamProjectRenamed() throws IOException {
        String proj1 = "Proj1";
        String proj2 = "Proj2";
        String proj3 = "Proj3";
        BuildPipelineTrigger bpTrigger = new BuildPipelineTrigger(proj1);
        bpTrigger.setDownstreamProjectNames(proj2 + ", " + proj3);
        assertTrue(bpTrigger.onDownstreamProjectRenamed(proj2, proj2 + "NEW"));

        assertEquals(proj2 + "NEW," + proj3, bpTrigger.getDownstreamProjectNames());
    }

    @Test
    public void testOnDownstreamProjectDeleted() {
        String proj1 = "Proj1";
        String proj2 = "Proj2";
        String proj3 = "Proj3";
        BuildPipelineTrigger bpTrigger = new BuildPipelineTrigger(proj1);
        bpTrigger.setDownstreamProjectNames(proj2 + ", " + proj3);
        assertTrue(bpTrigger.onDownstreamProjectDeleted(proj2));

        assertEquals(proj3, bpTrigger.getDownstreamProjectNames());
    }

    @Test
    public void testOnRenamed() throws IOException {
        String proj1 = "Proj1";
        String proj2 = "Proj2";
        String proj3 = "Proj3";
        FreeStyleProject project1 = createFreeStyleProject(proj1);
        FreeStyleProject project2 = createFreeStyleProject(proj2);
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2 + "," + proj3));
        Hudson.getInstance().rebuildDependencyGraph();

        project2.renameTo(proj2 + "NEW");

        final DescribableList<Publisher, Descriptor<Publisher>> downstreamPublishersList = project1.getPublishersList();
        for (final Publisher downstreamPub : downstreamPublishersList) {
            if (downstreamPub instanceof BuildPipelineTrigger) {
                final String manualDownstreamProjects = ((BuildPipelineTrigger) downstreamPub).getDownstreamProjectNames();
                assertEquals(proj2 + "NEW," + proj3, manualDownstreamProjects);
            }
        }
    }

    @Test
    public void testOnDeleted() throws IOException, InterruptedException {
        String proj1 = "Proj1";
        String proj2 = "Proj2";
        String proj3 = "Proj3";
        FreeStyleProject project1 = createFreeStyleProject(proj1);
        FreeStyleProject project2 = createFreeStyleProject(proj2);
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2 + "," + proj3));
        Hudson.getInstance().rebuildDependencyGraph();

        project2.delete();

        final DescribableList<Publisher, Descriptor<Publisher>> downstreamPublishersList = project1.getPublishersList();
        for (final Publisher downstreamPub : downstreamPublishersList) {
            if (downstreamPub instanceof BuildPipelineTrigger) {
                final String manualDownstreamProjects = ((BuildPipelineTrigger) downstreamPub).getDownstreamProjectNames();
                assertEquals(proj3, manualDownstreamProjects);
            }
        }
    }

    private HtmlPage createNewFreeStyleProjectHtmlPage(String newProjectName) throws SAXException, IOException {

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
        newJobName.setAttribute("value", newProjectName);
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
