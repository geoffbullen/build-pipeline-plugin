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

import hudson.model.Action;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.Run;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import hudson.model.Cause.UpstreamCause;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;

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

    @Test
    public void testHasDownstreamProjects() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String noOfBuilds = "5";
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final FreeStyleProject project2 = createFreeStyleProject(proj2);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2));

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Test a valid case
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, proj1, noOfBuilds, false);

        assertTrue(testView.hasDownstreamProjects(project1));
        assertFalse(testView.hasDownstreamProjects(project2));
    }

    @Test
    public void testGetDownstreamProjects() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String noOfBuilds = "5";
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final FreeStyleProject project2 = createFreeStyleProject(proj2);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2));

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Test a valid case
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, proj1, noOfBuilds, false);

        assertEquals(testView.getDownstreamProjects(project1).get(0), project2);
        assertEquals(testView.getDownstreamProjects(project2).size(), 0);
    }

    @Test
    public void testGetBuildPipelineForm() throws Exception {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";
        FreeStyleBuild build1;
        final String noOfBuilds = "5";
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final FreeStyleProject project2 = createFreeStyleProject(proj2);
        final FreeStyleProject project3 = createFreeStyleProject(proj3);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2));
        project2.getPublishersList().add(new BuildPipelineTrigger(proj3));

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1
        build1 = buildAndAssertSuccess(project1);
        waitUntilNoActivity();

        // Test a valid case
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, proj1, noOfBuilds, false);
        
        UpstreamCause upstreamCause = new hudson.model.Cause.UpstreamCause((Run<?, ?>) build1);
        final List<Action> buildActions = new ArrayList<Action>();
        project2.scheduleBuild(0, upstreamCause, buildActions.toArray(new Action[buildActions.size()]));
        waitUntilNoActivity();
        
        upstreamCause = new hudson.model.Cause.UpstreamCause((Run<?, ?>) project2.getBuildByNumber(1));
        project3.scheduleBuild(0, upstreamCause, buildActions.toArray(new Action[buildActions.size()]));
        waitUntilNoActivity();

        BuildPipelineForm testForm = testView.getBuildPipelineForm();
        
        assertEquals(testForm.getProjectGrid().get(0).get(0).getName(), proj1);
        assertEquals(testForm.getProjectGrid().get(0).get(1).getName(), proj2);
        assertEquals(testForm.getProjectGrid().get(0).get(2).getName(), proj3);

        assertEquals(testForm.getBuildGrids().get(0).get(0).get(0).getName(), build1.getFullDisplayName());
        assertEquals(testForm.getBuildGrids().get(0).get(0).get(1).getName(), proj2 + " #1");
        assertEquals(testForm.getBuildGrids().get(0).get(0).get(2).getName(), proj3 + " #1");

        // Test a null case
        testView.setSelectedJob(null);
        assertNull(testView.getBuildPipelineForm());
    }

    @Test
    public void testOnJobRenamed() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";
        final String noOfBuilds = "5";
        final FreeStyleProject project1 = createFreeStyleProject(proj1);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2));

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Test a valid case
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, proj1, noOfBuilds, false);
        
        assertEquals(testView.getJob(proj1), project1);
        project1.renameTo(proj3);
        assertEquals(testView.getJob(proj3), project1);
    }
}
