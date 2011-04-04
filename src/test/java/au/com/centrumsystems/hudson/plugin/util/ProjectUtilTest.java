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
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;

public class ProjectUtilTest extends HudsonTestCase {

    private static FreeStyleProject mainTestProject;
    private static FreeStyleProject dsTestProject;
    private static FreeStyleProject dsTestProject1;
    private static final String TEST_PROJECT1 = "Main Project";
    private static final String TEST_PROJECT2 = "Downstream Project";
    private static final String TEST_PROJECT3 = "Downstream Project 1";
    private static final String TEST_PROJECT1_URL = "/job/Main%20Project/";

    @Override
    @Before
    public void setUp() {

        try {
            super.setUp();
        } catch (Exception e) {
            e.toString();
        }
    }

    @Test
    public void testGetDownstreamProjects() {
        try {
            // Create a test project
            mainTestProject = createFreeStyleProject(TEST_PROJECT1);
            dsTestProject = createFreeStyleProject(TEST_PROJECT2);
            dsTestProject1 = createFreeStyleProject(TEST_PROJECT3);

            // Add project2 as a post build action: build other project
            mainTestProject.getPublishersList().add(new BuildPipelineTrigger(TEST_PROJECT2));
            mainTestProject.getPublishersList().add(new BuildPipelineTrigger(TEST_PROJECT3));

            // Important; we must do this step to ensure that the dependency graphs are updated
            Hudson.getInstance().rebuildDependencyGraph();

            // Test the method
            final List<AbstractProject<?, ?>> dsProjects = ProjectUtil.getDownstreamProjects(mainTestProject);
            assertEquals(mainTestProject.getName() + " should have a downstream project " + dsTestProject.getName(), dsTestProject,
                    dsProjects.get(0));

        } catch (IOException ioException) {
            ioException.toString();
        }
    }

    @Test
    public void testHasDownstreamProjects() {
        try {
            // Create a test project
            mainTestProject = createFreeStyleProject(TEST_PROJECT1);
            dsTestProject = createFreeStyleProject(TEST_PROJECT2);
            dsTestProject1 = createFreeStyleProject(TEST_PROJECT3);

            // Add TEST_PROJECT2 as a Manually executed pipeline project
            // Add TEST_PROJECT3 as a Post-build action -> build other projects
            mainTestProject.getPublishersList().add(new BuildPipelineTrigger(TEST_PROJECT2));
            mainTestProject.getPublishersList().add(new BuildTrigger(TEST_PROJECT3, true));

            // Important; we must do this step to ensure that the dependency graphs are updated
            Hudson.getInstance().rebuildDependencyGraph();

            // Test the method
            assertTrue(TEST_PROJECT2 + " should be a manual trigger", ProjectUtil.isManualTrigger(mainTestProject, dsTestProject));
            assertFalse(TEST_PROJECT3 + " should be an automatic trigger", ProjectUtil.isManualTrigger(mainTestProject, dsTestProject1));

        } catch (IOException ioException) {
            ioException.toString();
        }
    }

    @Test
    public void testIsManualTrigger() {
        try {
            // Create a test project
            mainTestProject = createFreeStyleProject(TEST_PROJECT1);
            dsTestProject = createFreeStyleProject(TEST_PROJECT2);
            dsTestProject1 = createFreeStyleProject(TEST_PROJECT3);

            // Add project2 as a post build action: build other project
            mainTestProject.getPublishersList().add(new BuildTrigger(TEST_PROJECT2, true));
            mainTestProject.getPublishersList().add(new BuildTrigger(TEST_PROJECT3, true));

            // Important; we must do this step to ensure that the dependency graphs are updated
            Hudson.getInstance().rebuildDependencyGraph();

            // Test the method
            assertTrue(mainTestProject.getName() + " should have downstream projects", ProjectUtil.hasDownstreamProjects(mainTestProject));

        } catch (IOException ioException) {
            ioException.toString();
        }
    }

    @Test
    public void testGetProjectURL() {
        try {
            mainTestProject = createFreeStyleProject(TEST_PROJECT1);
            assertEquals("The project URL should have been " + TEST_PROJECT1_URL, TEST_PROJECT1_URL,
                    ProjectUtil.getProjectURL(mainTestProject));
        } catch (Exception e) {
            e.toString();
        }
    }

}
