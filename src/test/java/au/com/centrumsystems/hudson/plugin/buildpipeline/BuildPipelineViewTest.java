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
import hudson.model.Job;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.xml.sax.SAXException;

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

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testBuildPipelineViewPage() throws IOException, SAXException {
        String bpViewName = "MyTestView";

        final HtmlPage testViewPage = new WebClient().goTo("../newView");

        final HtmlForm form = testViewPage.getFormByName("createView");

        final HtmlTextInput viewName = (HtmlTextInput) testViewPage.getElementByName("name");
        viewName.setAttribute("value", bpViewName);
        viewName.click();

        final HtmlRadioButtonInput radioButton = testViewPage.getElementByName("mode");
        radioButton.setChecked(true);
        radioButton.click();

        final HtmlButton addViewButton = (HtmlButton) last(form.getHtmlElementsByTagName("button"));
        addViewButton.removeAttribute("disabled");

        final HtmlPage formReturnPage = (HtmlPage) form.submit(addViewButton);

        assertNotNull("Form Return Page", formReturnPage);
        assertNotNull("Add view button found", addViewButton);

        final HtmlPage testViewConfigPage = new WebClient().goTo("../view/" + bpViewName + "/configure");
        final HtmlElement buildViewTitle = testViewConfigPage.getElementByName("buildViewTitle");
        buildViewTitle.setAttribute("value", "My Build View Title");
        final HtmlElement selectedJob = testViewConfigPage.getElementByName("selectedJob");
        selectedJob.setAttribute("value", "TEST_PROJECT1");
        final HtmlElement noOfDisplayedBuilds = testViewConfigPage.getElementByName("noOfDisplayedBuilds");
        noOfDisplayedBuilds.setAttribute("value", "3");

        final HtmlForm viewConfigForm = testViewConfigPage.getFormByName("viewConfig");
        final HtmlButton viewConfigFormButton = (HtmlButton) last(viewConfigForm.getHtmlElementsByTagName("button"));
        viewConfigForm.submit(viewConfigFormButton);

        assertNotNull("noOfDisplayedBuilds", noOfDisplayedBuilds);
    }

    @Test
    public void testGetSelectedProject() throws IOException {
        String bpViewName = "MyTestView";
        String bpViewTitle = "MyTestViewTitle";
        String proj1 = "Proj1";
        String noOfBuilds = "5";
        createFreeStyleProject(proj1);

        // Test a valid case
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, proj1, noOfBuilds);
        Job<?, ?> testSelectedProject = testView.getSelectedProject();

        assertEquals(proj1, testSelectedProject.getName());

        // Test the null case
        testView = new BuildPipelineView(bpViewName, bpViewTitle, "", noOfBuilds);
        testSelectedProject = testView.getSelectedProject();

        assertNull(testSelectedProject);
    }

    @Test
    public void testHasSelectedProject() throws IOException {
        String bpViewName = "MyTestView";
        String bpViewTitle = "MyTestViewTitle";
        String proj1 = "Proj1";
        String noOfBuilds = "5";
        createFreeStyleProject(proj1);

        // Test a valid case
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, proj1, noOfBuilds);

        assertTrue(testView.hasSelectedProject());

        // Test the null case
        testView = new BuildPipelineView(bpViewName, bpViewTitle, "", noOfBuilds);
        assertFalse(testView.hasSelectedProject());
    }

    @Test
    public void testHasBuildPermission() throws IOException {
        String bpViewName = "MyTestView";
        String bpViewTitle = "MyTestViewTitle";
        String proj1 = "Proj1";
        String noOfBuilds = "5";
        FreeStyleProject project1 = createFreeStyleProject(proj1);

        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, proj1, noOfBuilds);
        assertTrue(testView.hasBuildPermission(project1));
    }

}
