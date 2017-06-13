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

import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.BuildVariablesHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.NullColumnHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.PipelineHeaderExtension;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.SimpleColumnHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.SimpleRowHeader;
import hudson.Launcher;
import hudson.model.*;
import hudson.model.Cause.UpstreamCause;
import hudson.security.Permission;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hudson.triggers.SCMTrigger;
import jenkins.model.Jenkins;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.recipes.LocalData;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Test Build Pipeline View
 * 
 * @author Centrum Systems
 * 
 */
public class BuildPipelineViewTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();


    @Test
    public void testGetSelectedProject() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String noOfBuilds = "5";
        jenkins.createFreeStyleProject(proj1);

		// Test a valid case
        DownstreamProjectGridBuilder gridBuilder = new DownstreamProjectGridBuilder(proj1);
		BuildPipelineView testView = BuildPipelineViewFactory.getBuildPipelineView(bpViewName, bpViewTitle, gridBuilder, noOfBuilds, false);

        Job<?, ?> testSelectedProject = gridBuilder.getFirstJob(testView);

        assertEquals(proj1, testSelectedProject.getName());

        // Test the null case
        gridBuilder = new DownstreamProjectGridBuilder("");
		testView = BuildPipelineViewFactory.getBuildPipelineView(bpViewName, bpViewTitle, gridBuilder, noOfBuilds, false);
		testSelectedProject = gridBuilder.getFirstJob(testView);

        assertNull(testSelectedProject);
    }

    @Test
    public void testHasBuildPermission() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String noOfBuilds = "5";
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);

		final BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle,
                new DownstreamProjectGridBuilder(proj1), noOfBuilds, false, null);
		assertTrue(testView.hasBuildPermission());
	}

    @Test
    public void testTriggerOnlyLatestJob() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String noOfBuilds = "5";
        jenkins.createFreeStyleProject(proj1);

		// True
		BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, true, null);
		assertTrue(proj1, testView.isTriggerOnlyLatestJob());

		// False
		testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(""), noOfBuilds, false, null);
		assertFalse(proj1, testView.isTriggerOnlyLatestJob());
	}

    @Test
    public void testAlwaysAllowManualTrigger() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String noOfBuilds = "5";
        jenkins.createFreeStyleProject(proj1);

		// True
		BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, true, true, false, false, false, 2, null, null, null, null);
		assertTrue("Failed to set AlwaysAllowManualTrigger flag", testView.isAlwaysAllowManualTrigger());

		// False
		testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(""), noOfBuilds, true, false, false, false, false, 2, null, null, null, null);
		assertFalse("Failed to unset AlwaysAllowManualTrigger flag", testView.isAlwaysAllowManualTrigger());
	}

    @Test
    public void testShowPipelineDefinitionHeader() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String noOfBuilds = "5";
        jenkins.createFreeStyleProject(proj1);

		// True
		BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, true, false, false, false, true, 2, null, null, null, null);
		assertTrue("Failed to set ShowPipelineDefinitionHeader flag", testView.isShowPipelineDefinitionHeader());

		// False
		testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(""), noOfBuilds, true, false, false, false, false, 2, null, null, null, null);
		assertFalse("Failed to unset ShowPipelineDefinitionHeader flag", testView.isShowPipelineDefinitionHeader());
	}

    @Test
    public void testMigration() throws IOException {
        jenkins.createFreeStyleProject("Sample Project");

        BuildPipelineView testView = new BuildPipelineView("My Build Pipeline Name",
                "My Build Pipeline Title", new DownstreamProjectGridBuilder("Sample Project"),
                "3", false, false,
                true /* showPipelineParameters */,
                true /* showPipelineParametersInHeaders */,
                true /* showPipelineDefinitionHeader */,
                3,
                null,
                null,
                null,
                null);
        testView.readResolve();
        assertNotNull(testView.getColumnHeaders());
        assertEquals(BuildVariablesHeader.class, testView.getColumnHeaders().getClass());
        assertNotNull(testView.getRowHeaders());
        assertEquals(BuildVariablesHeader.class, testView.getRowHeaders().getClass());

        testView = new BuildPipelineView("My Build Pipeline Name",
                "My Build Pipeline Title", new DownstreamProjectGridBuilder("Sample Project"),
                "3", false, false,
                false /* showPipelineParameters */,
                true /* showPipelineParametersInHeaders */,
                false /* showPipelineDefinitionHeader */,
                3,
                null,
                null,
                null,
                null);
        testView.readResolve();
        assertNotNull(testView.getColumnHeaders());
        assertEquals(NullColumnHeader.class, testView.getColumnHeaders().getClass());
        assertNotNull(testView.getRowHeaders());
        assertEquals(SimpleRowHeader.class, testView.getRowHeaders().getClass());

        testView = new BuildPipelineView("My Build Pipeline Name",
                "My Build Pipeline Title", new DownstreamProjectGridBuilder("Sample Project"),
                "3", false, false,
                false /* showPipelineParameters */,
                false /* showPipelineParametersInHeaders */,
                true /* showPipelineDefinitionHeader */,
                3,
                null,
                null,
                null,
                null);
        testView.readResolve();
        assertNotNull(testView.getColumnHeaders());
        assertEquals(SimpleColumnHeader.class, testView.getColumnHeaders().getClass());
    }

	@Test
	public void testHasDownstreamProjects() throws IOException {
		final String bpViewName = "MyTestView";
		final String bpViewTitle = "MyTestViewTitle";
		final String proj1 = "Proj1";
		final String proj2 = "Proj2";
		final String noOfBuilds = "5";
		final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
		final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));

        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        Hudson.getInstance().rebuildDependencyGraph();

		// Test a valid case
		final BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, false, null);

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
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));

        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        Hudson.getInstance().rebuildDependencyGraph();

		// Test a valid case
		final BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, false, null);

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
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);
        final FreeStyleProject project3 = jenkins.createFreeStyleProject(proj3);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));
        project2.getPublishersList().add(new BuildPipelineTrigger(proj3, null));

        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1
        build1 = jenkins.buildAndAssertSuccess(project1);
        jenkins.waitUntilNoActivity();
		// Test a valid case
		final BuildPipelineView testView = BuildPipelineViewFactory.getBuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, false);

        UpstreamCause upstreamCause = new hudson.model.Cause.UpstreamCause(
                (Run<?, ?>) build1);
        final List<Action> buildActions = new ArrayList<Action>();
        project2.scheduleBuild(0, upstreamCause,
                buildActions.toArray(new Action[buildActions.size()]));
        jenkins.waitUntilNoActivity();

        upstreamCause = new hudson.model.Cause.UpstreamCause(
                (Run<?, ?>) project2.getBuildByNumber(1));
        project3.scheduleBuild(0, upstreamCause,
                buildActions.toArray(new Action[buildActions.size()]));
        jenkins.waitUntilNoActivity();

        final BuildPipelineForm testForm = testView.getBuildPipelineForm();

        assertEquals(testForm.getProjectGrid().get(0, 0).getName(), proj1);
        assertEquals(testForm.getProjectGrid().get(0, 1).getName(), proj2);
        assertEquals(testForm.getProjectGrid().get(0, 2).getName(), proj3);

        // Test a null case
        testView.setGridBuilder(new DownstreamProjectGridBuilder(""));
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
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));

        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        Hudson.getInstance().rebuildDependencyGraph();

		// Test a valid case
		final BuildPipelineView testView = BuildPipelineViewFactory.getBuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, false);

        assertEquals(testView.getJob(proj1), project1);
        project1.renameTo(proj3);
        assertEquals(testView.getJob(proj3), project1);
    }

    @Test
    public void testGetItems() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String noOfBuilds = "5";
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);

		final BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle,
                new DownstreamProjectGridBuilder(proj1), noOfBuilds, false, null);
		TopLevelItem item1 = Jenkins.getInstance().getItem(proj1);
		assertNotNull(item1);
		assertTrue(testView.getItems().contains(item1));

        final String proj2 = "Proj2";
        final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);
        TopLevelItem item2 = Jenkins.getInstance().getItem(proj2);
        assertNotNull(item2);
        assertFalse(testView.getItems().contains(item2));
    }

    @Test
    public void testHasPermission() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String noOfBuilds = "5";

        final BuildPipelineView testView = new BuildPipelineView(bpViewName,
                bpViewTitle, new DownstreamProjectGridBuilder(proj1),
                noOfBuilds, false, null);

        assertTrue(testView.hasPermission(Permission.READ));
    }

    @Test
    @LocalData
    @Bug(19755)
    public void testMyUserIdCauseConversion() throws Exception {
        FreeStyleProject projectB = (FreeStyleProject) jenkins.getInstance().getItem("B");
        FreeStyleBuild buildB = projectB.getBuildByNumber(1);
        assertNotNull(buildB);
        Cause.UserIdCause cause = buildB.getCause(Cause.UserIdCause.class);
        assertNotNull(cause);
        assertEquals("bill", cause.getUserId());
    }

    /**
     * This is a factory to create an instance of the class under test. This
     * helps to avoid a NPE in View.java when calling getOwnerItemGroup and it's
     * not set. This doesn't solve the root cause and it't only intended to make
     * our tests succeed.
     */
    static class BuildPipelineViewFactory {
        public static BuildPipelineView getBuildPipelineView(final String bpViewName, final String bpViewTitle, final ProjectGridBuilder gridBuilder,
                final String noOfBuilds, final boolean triggerOnlyLatestJob) {
            return new BuildPipelineView(bpViewName, bpViewTitle, gridBuilder, noOfBuilds, triggerOnlyLatestJob, null) {

                @Override
                public ItemGroup<? extends TopLevelItem> getOwnerItemGroup() {
                    return Hudson.getInstance();
                }
            };
        }
    }

    @Test
    @Issue("JENKINS-30801")
    public void testRetriggerSuccessfulBuild() throws Exception {
        final FreeStyleProject upstreamBuild = jenkins.createFreeStyleProject("upstream");
        final FreeStyleProject downstreamBuild = jenkins.createFreeStyleProject("downstream");
        upstreamBuild.getPublishersList().add(new BuildPipelineTrigger("downstream", null));
        downstreamBuild.getBuildersList().add(new TestBuilder()
        {
            @Override
            public boolean perform(AbstractBuild<?, ?> abstractBuild, Launcher launcher, BuildListener buildListener)
                    throws InterruptedException, IOException
            {
                abstractBuild.addAction(new MockAction());
                return true;
            }
        });

        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // mock the upstream build as being caused by SCM trigger
        Cause mockScmTriggerCause = new SCMTrigger.SCMTriggerCause("mock");
        upstreamBuild.scheduleBuild2(0, mockScmTriggerCause);
        jenkins.waitUntilNoActivity();

        // mock trigget the downstream build as being triggered by upstream
        ParametersAction parametersAction = new ParametersAction(
                Arrays.asList((ParameterValue)new StringParameterValue("foo", "bar")));
        UpstreamCause upstreamCause = new hudson.model.Cause.UpstreamCause(
                (Run<?, ?>) upstreamBuild.getLastBuild());
        downstreamBuild.scheduleBuild2(0, upstreamCause, parametersAction);
        jenkins.waitUntilNoActivity();

        BuildPipelineView pipeline = BuildPipelineViewFactory.getBuildPipelineView("pipeline", "",
                new DownstreamProjectGridBuilder(upstreamBuild.getFullName()), "1", false);

        jenkins.getInstance().addView(pipeline);
        assertNotNull(downstreamBuild.getLastBuild());
        // re-run the build as if we clicked re-run in the UI
        pipeline.rerunBuild(downstreamBuild.getLastBuild().getExternalizableId());
        jenkins.waitUntilNoActivity();

        // MockAction is not copied from one run to another
        assertEquals(1, downstreamBuild.getLastBuild().getActions(MockAction.class).size());
        // upstream cause copied
        assertEquals(1, downstreamBuild.getLastBuild().getCauses().size());
        // parametersAction copied
        assertNotNull(downstreamBuild.getLastBuild().getAction(ParametersAction.class));
        StringParameterValue stringParam = (StringParameterValue) downstreamBuild.getLastBuild()
                .getAction(ParametersAction.class).getParameter("foo");
        assertEquals("bar", stringParam.value);
        assertEquals(upstreamCause, downstreamBuild.getLastBuild().getCauses().get(0));
        assertEquals(mockScmTriggerCause, upstreamCause.getUpstreamCauses().get(0));
    }

    @Test
    public void testFilterUserIdCause() throws Exception {
        final FreeStyleProject upstreamBuild = jenkins.createFreeStyleProject("upstream");
        final FreeStyleProject downstreamBuild = jenkins.createFreeStyleProject("downstream");
        upstreamBuild.getPublishersList().add(new BuildPipelineTrigger("downstream", null));
        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        Hudson.getInstance().rebuildDependencyGraph();
        Cause mockUserIdCause = mock(Cause.UserIdCause.class);
        upstreamBuild.scheduleBuild2(0, mockUserIdCause);
        jenkins.waitUntilNoActivity();
        UpstreamCause upstreamCause = new hudson.model.Cause.UpstreamCause(
                (Run<?, ?>) upstreamBuild.getLastBuild());
        downstreamBuild.scheduleBuild2(0, upstreamCause);
        jenkins.waitUntilNoActivity();

        BuildPipelineView pipeline = BuildPipelineViewFactory.getBuildPipelineView("pipeline", "",
                new DownstreamProjectGridBuilder(upstreamBuild.getFullName()), "1", false);
        jenkins.getInstance().addView(pipeline);
        assertNotNull(downstreamBuild.getLastBuild());
        // re-run the build as if we clicked re-run in the UI
        pipeline.rerunBuild(upstreamBuild.getLastBuild().getExternalizableId());
        jenkins.waitUntilNoActivity();
        assertEquals(2, upstreamBuild.getBuilds().size());
        assertNotNull(upstreamBuild.getLastBuild().getCause(Cause.UserIdCause.class));
        assertNotSame(upstreamBuild.getLastBuild().getCause(Cause.UserIdCause.class),
                mockUserIdCause);
    }

    @Test
    public void testDescriptorDiscovery() {
        BuildPipelineView.DescriptorImpl descriptor = new BuildPipelineView.DescriptorImpl();
        assertEquals(5, descriptor.getColumnHeaderDescriptors().size());
        assertEquals(4, descriptor.getRowHeaderDescriptors().size());
    }

    public static class MockAction implements Action, Serializable {

        private static final long serialVersionUID = 5677631606354259250L;

        @Override
        public String getIconFileName()
        {
            return null;
        }

        @Override
        public String getDisplayName()
        {
            return null;
        }

        @Override
        public String getUrlName()
        {
            return null;
        }
    }
}
