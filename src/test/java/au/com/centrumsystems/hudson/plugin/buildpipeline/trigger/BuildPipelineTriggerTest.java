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
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.StandardBuildCard;
import hudson.model.*;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.mockito.Mock;

import static org.junit.Assert.*;

/**
 * BuildPipelineTrigger test class
 *
 * @author Centrum Systems
 *
 */
public class BuildPipelineTriggerTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidConstructor() {
        new BuildPipelineTrigger(null, null);
    }

    @Test
    public void testBuildPipelineTrigger() throws IOException {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        // Add TEST_PROJECT2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));
        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        final BuildPipelineTrigger myBPTrigger = new BuildPipelineTrigger(proj1, null);

        assertNotNull("A valid BuildPipelineTrigger should have been created.", myBPTrigger);

        assertEquals("BuildPipelineTrigger downstream project is " + proj1, proj1, myBPTrigger.getDownstreamProjectNames());
    }

    @Test
    public void testOnDownstreamProjectRenamed() throws IOException {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";
        final BuildPipelineTrigger bpTrigger = new BuildPipelineTrigger(proj1, null);
        bpTrigger.setDownstreamProjectNames(proj2 + ", " + proj3);
        assertTrue(bpTrigger.onDownstreamProjectRenamed(proj2, proj2 + "NEW"));

        assertEquals(proj2 + "NEW," + proj3, bpTrigger.getDownstreamProjectNames());

        // Null case
        bpTrigger.setDownstreamProjectNames(null);
        assertFalse(bpTrigger.onDownstreamProjectRenamed(proj2, proj3));
    }

    @Test
    public void testOnDownstreamProjectDeleted() {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";
        final BuildPipelineTrigger bpTrigger = new BuildPipelineTrigger(proj1, null);
        bpTrigger.setDownstreamProjectNames(proj2 + ", " + proj3);
        assertTrue(bpTrigger.onDownstreamProjectDeleted(proj2));

        assertEquals(proj3, bpTrigger.getDownstreamProjectNames());

        // Null case
        bpTrigger.setDownstreamProjectNames(null);
        assertFalse(bpTrigger.onDownstreamProjectDeleted(proj2));
    }

    @Test
    public void testOnRenamed() throws IOException {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2 + "," + proj3, null));
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
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2 + "," + proj3, null));
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

    @Test
    public void testDoCheckDownstreamProjectNames() throws IOException, InterruptedException {
        final AbstractProject upstreamProject = jenkins.createFreeStyleProject("Upstream");

        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        jenkins.createFreeStyleProject(proj1);

        final BuildPipelineTrigger.DescriptorImpl di = new BuildPipelineTrigger.DescriptorImpl();

        assertEquals(FormValidation.ok(), di.doCheckDownstreamProjectNames(upstreamProject, proj1));
        assertThat(FormValidation.error("No such project ‘" + proj2 + "’. Did you mean ‘" + proj1 + "’?").toString(), is(di
                .doCheckDownstreamProjectNames(upstreamProject, proj2).toString()));
    }

    @Test
    public void testRemoveDownstreamTrigger() throws IOException, InterruptedException {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final BuildPipelineTrigger buildPipelineTrigger = new BuildPipelineTrigger(proj2, null);
        project1.getPublishersList().add(buildPipelineTrigger);
        Hudson.getInstance().rebuildDependencyGraph();

        buildPipelineTrigger.removeDownstreamTrigger(buildPipelineTrigger, project1, proj2);

        final DescribableList<Publisher, Descriptor<Publisher>> downstreamPublishersList = project1.getPublishersList();
        for (final Publisher downstreamPub : downstreamPublishersList) {
            if (downstreamPub instanceof BuildPipelineTrigger) {
                final String manualDownstreamProjects = ((BuildPipelineTrigger) downstreamPub).getDownstreamProjectNames();
                assertEquals("", manualDownstreamProjects);
            }
        }
    }

    @Test
    public void testCyclicDownstreamTrigger() throws IOException, InterruptedException {
        final String proj1 = "Proj1";
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final BuildPipelineTrigger cyclicPipelineTrigger = new BuildPipelineTrigger(proj1, null);
        project1.getPublishersList().add(cyclicPipelineTrigger);
        Hudson.getInstance().rebuildDependencyGraph();

        final DescribableList<Publisher, Descriptor<Publisher>> downstreamPublishersList = project1.getPublishersList();
        for (final Publisher downstreamPub : downstreamPublishersList) {
            if (downstreamPub instanceof BuildPipelineTrigger) {
                final String manualDownstreamProjects = ((BuildPipelineTrigger) downstreamPub).getDownstreamProjectNames();
                assertEquals("", manualDownstreamProjects);
            }
        }

    }

    @Test
    public void testGetBuilderConfigDescriptors() throws Exception {
        final BuildPipelineTrigger.DescriptorImpl di = new BuildPipelineTrigger.DescriptorImpl();

        assertThat(di.getBuilderConfigDescriptors(), is(not(Collections.<Descriptor<AbstractBuildParameters>>emptyList())));
    }

    @Test
    @Bug(22665)
    public void testManualTriggerCause() throws Exception
    {
        FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        FreeStyleProject projectB = jenkins.createFreeStyleProject("B");
        projectA.getPublishersList().add(new BuildPipelineTrigger("B", null));
        jenkins.getInstance().rebuildDependencyGraph();

        BuildPipelineView view = new BuildPipelineView("Pipeline", "Title", new DownstreamProjectGridBuilder("A"), "1", false, "");
        view.setBuildCard(new StandardBuildCard());
        jenkins.buildAndAssertSuccess(projectA);

        view.triggerManualBuild(1, "B", "A");
        jenkins.waitUntilNoActivity();

        assertNotNull(projectB.getLastBuild());
        FreeStyleBuild build = projectB.getLastBuild();
        Cause.UserIdCause cause = build.getCause(Cause.UserIdCause.class);
        assertNotNull(cause);
        //Check that cause is of core class Cause.UserIdCause and not MyUserIdCause
        assertEquals(Cause.UserIdCause.class.getName(), cause.getClass().getName());
        Cause.UpstreamCause upstreamCause = build.getCause(Cause.UpstreamCause.class);
        assertNotNull(upstreamCause);
    }

    @Test
    public void testTriggerProjectInFolder()
        throws Exception
    {

        //root folder
        MockFolder folder1 = jenkins.createFolder("Folder1");
        FreeStyleProject projectA = jenkins.createFreeStyleProject("A");

        //  /folder1
        folder1.createProject(MockFolder.class, "Folder2");
        MockFolder folder2 = (MockFolder) folder1.getItem("Folder2");

        //  /folder1/folder2
        folder2.createProject(FreeStyleProject.class, "B");

        projectA.getPublishersList().add(new BuildPipelineTrigger("B", null));
        jenkins.getInstance().rebuildDependencyGraph();

        BuildPipelineView view = new BuildPipelineView("Pipeline", "Title", new DownstreamProjectGridBuilder("A"), "1", false, "");
        view.setBuildCard(new StandardBuildCard());
        jenkins.buildAndAssertSuccess(projectA);

        view.triggerManualBuild(1, "B", "A");
        jenkins.waitUntilNoActivity();

        FreeStyleProject projectB = (FreeStyleProject) folder2.getItem("B");
        assertNotNull(projectB.getLastBuild());
        FreeStyleBuild build = projectB.getLastBuild();
        Cause.UserIdCause cause = build.getCause(Cause.UserIdCause.class);
        assertNotNull(cause);
        //Check that cause is of core class Cause.UserIdCause and not MyUserIdCause
        assertEquals(Cause.UserIdCause.class.getName(), cause.getClass().getName());
        Cause.UpstreamCause upstreamCause = build.getCause(Cause.UpstreamCause.class);
        assertNotNull(upstreamCause);
    }

    @Test
    public void testUpstreamProjectInFolder()
            throws Exception
    {

        //root folder
        MockFolder folder1 = jenkins.createFolder("Folder1");
        FreeStyleProject projectB = jenkins.createFreeStyleProject("B");

        //  /folder1
        folder1.createProject(FreeStyleProject.class, "A");
        FreeStyleProject projectA = (FreeStyleProject) folder1.getItem("A");

        projectA.getPublishersList().add(new BuildPipelineTrigger("B", null));
        jenkins.getInstance().rebuildDependencyGraph();

        BuildPipelineView view = new BuildPipelineView("Pipeline", "Title", new DownstreamProjectGridBuilder("A"), "1", false, "");
        view.setBuildCard(new StandardBuildCard());
        jenkins.buildAndAssertSuccess(projectA);

        view.triggerManualBuild(1, "B", "A");
        jenkins.waitUntilNoActivity();

        assertNotNull(projectB.getLastBuild());
        FreeStyleBuild build = projectB.getLastBuild();
        Cause.UserIdCause cause = build.getCause(Cause.UserIdCause.class);
        assertNotNull(cause);
        //Check that cause is of core class Cause.UserIdCause and not MyUserIdCause
        assertEquals(Cause.UserIdCause.class.getName(), cause.getClass().getName());
        Cause.UpstreamCause upstreamCause = build.getCause(Cause.UpstreamCause.class);
        assertNotNull(upstreamCause);
    }

    @Test (expected = NoSuchElementException.class)
    public void testUnableToFindProject()
            throws Exception
    {

        //root folder
        MockFolder folder1 = jenkins.createFolder("Folder1");
        FreeStyleProject projectB = jenkins.createFreeStyleProject("B");


        BuildPipelineView view = new BuildPipelineView("Pipeline", "Title", new DownstreamProjectGridBuilder("A"), "1", false, "");
        view.setBuildCard(new StandardBuildCard());

        view.triggerManualBuild(1, "B", "A");
        jenkins.waitUntilNoActivity();

    }

    @Test
    @Issue("JENKINS-24883")
    public void testReRunBuildPipelineTrigger()
        throws Exception
    {
        FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        FreeStyleProject projectB = jenkins.createFreeStyleProject("B");
        projectA.getPublishersList().add(new BuildPipelineTrigger("B", null));
        jenkins.getInstance().rebuildDependencyGraph();

        BuildPipelineView view = new BuildPipelineView("Pipeline", "Title", new DownstreamProjectGridBuilder("A"), "1", false, "");
        view.setBuildCard(new StandardBuildCard());
        jenkins.buildAndAssertSuccess(projectA);

        view.triggerManualBuild(1, "B", "A");
        jenkins.waitUntilNoActivity();

        assertNotNull(projectB.getLastBuild());
        FreeStyleBuild build = projectB.getLastBuild();
        Cause.UserIdCause cause = build.getCause(Cause.UserIdCause.class);
        assertNotNull(cause);
        //Check that cause is of core class Cause.UserIdCause and not MyUserIdCause
        assertEquals(Cause.UserIdCause.class.getName(), cause.getClass().getName());
        Cause.UpstreamCause upstreamCause = build.getCause(Cause.UpstreamCause.class);
        assertNotNull(upstreamCause);

        // re-triggering the build should preserve upstream context (JENKINS-24883
        view.rerunBuild(projectB.getLastBuild().getExternalizableId());

        jenkins.waitUntilNoActivity();
        build = projectB.getLastBuild();
        upstreamCause = build.getCause(Cause.UpstreamCause.class);
        assertNotNull(upstreamCause);
    }
}
