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
package au.com.centrumsystems.hudson.plugin.util;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.tasks.BuildTrigger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;

public class ProjectUtilTest extends HudsonTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetDownstreamProjects() throws IOException {
        String proj1 = "Proj1";
        String proj2 = "Proj2";
        String proj3 = "Proj3";

        // Create a test project
        FreeStyleProject project1 = createFreeStyleProject(proj1);
        FreeStyleProject project2 = createFreeStyleProject(proj2);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2));
        project1.getPublishersList().add(new BuildPipelineTrigger(proj3));

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Test the method
        final List<AbstractProject<?, ?>> dsProjects = ProjectUtil.getDownstreamProjects(project1);
        assertEquals(project1.getName() + " should have a downstream project " + project2.getName(), project2, dsProjects.get(0));
    }

    @Test
    public void testIsManualTrigger() throws IOException {
        String proj1 = "Proj1";
        String proj2 = "Proj2";
        String proj3 = "Proj3";

        // Create a test project
        FreeStyleProject project1 = createFreeStyleProject(proj1);
        FreeStyleProject project2 = createFreeStyleProject(proj2);
        FreeStyleProject project3 = createFreeStyleProject(proj3);

        // Add TEST_PROJECT2 as a Manually executed pipeline project
        // Add TEST_PROJECT3 as a Post-build action -> build other projects
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2));
        project1.getPublishersList().add(new BuildTrigger(proj3, true));

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Test the method
        assertTrue(proj2 + " should be a manual trigger", ProjectUtil.isManualTrigger(project1, project2));
        assertFalse(proj3 + " should be an automatic trigger", ProjectUtil.isManualTrigger(project1, project3));

    }

    @Test
    public void testHasDownstreamProjects() throws IOException {
        String proj1 = "Proj1";
        String proj2 = "Proj2";
        String proj3 = "Proj3";

        // Create a test project
        FreeStyleProject project1 = createFreeStyleProject(proj1);
        createFreeStyleProject(proj2);
        createFreeStyleProject(proj3);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2));
        project1.getPublishersList().add(new BuildTrigger(proj3, true));

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Test the method
        assertTrue(project1.getName() + " should have downstream projects", ProjectUtil.hasDownstreamProjects(project1));
    }

    @Test
    public void testGetProjectURL() throws URISyntaxException, IOException {
        String proj1 = "Proj 1";
        String proj1Url = "/job/Proj%201/";

        // Create a test project
        FreeStyleProject project1 = createFreeStyleProject(proj1);

        assertEquals("The project URL should have been " + proj1Url, proj1Url, ProjectUtil.getProjectURL(project1));
    }

}