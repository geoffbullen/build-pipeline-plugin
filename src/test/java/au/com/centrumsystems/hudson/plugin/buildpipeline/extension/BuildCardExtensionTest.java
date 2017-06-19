/***************************************************************************
 * Copyright (C) 2014 Ping Identity Corporation
 * All rights reserved.
 * <p/>
 * The contents of this file are the property of Ping Identity Corporation.
 * You may not copy or use this file, in either source code or executable
 * form, except in compliance with terms set by Ping Identity Corporation.
 * For further information please contact:
 * <p/>
 * Ping Identity Corporation
 * 1001 17th Street Suite 100
 * Denver, CO 80202
 * 303.468.2900
 * http://www.pingidentity.com
 **************************************************************************/
package au.com.centrumsystems.hudson.plugin.buildpipeline.extension;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineViewTest;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.StringParameterValue;
import hudson.triggers.SCMTrigger;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;

/**
 * @author dalvizu
 */
public class BuildCardExtensionTest
{

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testBuildCards() {
        assertEquals(1, BuildCardExtension.all().size());
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
                Arrays.asList((ParameterValue) new StringParameterValue("foo", "bar")));
        Cause.UpstreamCause upstreamCause = new hudson.model.Cause.UpstreamCause(
                (Run<?, ?>) upstreamBuild.getLastBuild());
        downstreamBuild.scheduleBuild2(0, upstreamCause, parametersAction);
        jenkins.waitUntilNoActivity();

        BuildPipelineView pipeline = BuildPipelineViewTest.BuildPipelineViewFactory.getBuildPipelineView("pipeline", "",
                new DownstreamProjectGridBuilder(upstreamBuild.getFullName()), "1", false);
        pipeline.setBuildCard(new StandardBuildCard());
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
        Cause.UpstreamCause upstreamCause = new hudson.model.Cause.UpstreamCause(
                (Run<?, ?>) upstreamBuild.getLastBuild());
        downstreamBuild.scheduleBuild2(0, upstreamCause);
        jenkins.waitUntilNoActivity();

        BuildPipelineView pipeline = BuildPipelineViewTest.BuildPipelineViewFactory.getBuildPipelineView("pipeline", "",
                new DownstreamProjectGridBuilder(upstreamBuild.getFullName()), "1", false);
        pipeline.setBuildCard(new StandardBuildCard());
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

    public static class MockAction implements Action, Serializable
    {

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
