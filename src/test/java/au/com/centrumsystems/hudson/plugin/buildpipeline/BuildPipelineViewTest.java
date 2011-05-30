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
    public void testGetSelectedProject() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String noOfBuilds = "5";
        createFreeStyleProject(proj1);

        // Test a valid case
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, proj1, noOfBuilds, false);
        Job<?, ?> testSelectedProject = testView.getSelectedProject();

        assertEquals(proj1, testSelectedProject.getName());

        // Test the null case
        testView = new BuildPipelineView(bpViewName, bpViewTitle, "", noOfBuilds, false);
        testSelectedProject = testView.getSelectedProject();

        assertNull(testSelectedProject);
    }

    @Test
    public void testHasSelectedProject() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String noOfBuilds = "5";
        createFreeStyleProject(proj1);

        // Test a valid case
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, proj1, noOfBuilds, false);

        assertTrue(testView.hasSelectedProject());

        // Test the null case
        testView = new BuildPipelineView(bpViewName, bpViewTitle, "", noOfBuilds, false);
        assertFalse(testView.hasSelectedProject());
    }

    @Test
    public void testHasBuildPermission() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String noOfBuilds = "5";
        final FreeStyleProject project1 = createFreeStyleProject(proj1);

        final BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, proj1, noOfBuilds, false);
        assertTrue(testView.hasBuildPermission(project1));
    }

    @Test
    public void testTriggerOnlyLatestJob() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String noOfBuilds = "5";
        createFreeStyleProject(proj1);

        // True
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, proj1, noOfBuilds, true);
        assertTrue(proj1, testView.isTriggerOnlyLatestJob());

        // False
        testView = new BuildPipelineView(bpViewName, bpViewTitle, "", noOfBuilds, false);
        assertFalse(proj1, testView.isTriggerOnlyLatestJob());
    }
}
