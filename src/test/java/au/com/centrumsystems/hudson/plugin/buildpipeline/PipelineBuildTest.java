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

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.tasks.BuildTrigger;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.MockBuilder;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import au.com.centrumsystems.hudson.plugin.util.HudsonResult;

public class PipelineBuildTest extends HudsonTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetDownstreamPipeline() throws Exception {
        String proj1 = "Proj1";
        String proj2 = "Proj2";
        String proj3 = "Proj3";
        String proj4 = "Proj4";
        String proj5 = "Proj5";
        FreeStyleProject project1, project2, project3, project4, project5;
        MockBuilder builder1, builder2, builder3, builder4, builder5;
        FreeStyleBuild build1 = null;
        BuildTrigger trigger2, trigger3, trigger4, trigger5;
        final String RESULT1 = "-Project: " + proj1 + " : Build: 1\n" + "--Project: " + proj2 + " : Build: 1\n" + "---Project: " + proj4
                + " : Build: 1\n" + "--Project: " + proj3 + " : Build: 1\n";
        final String RESULT2 = "-Project: " + proj1 + " : Build: 2\n" + "--Project: " + proj2 + " : Build: 2\n" + "--Project: " + proj3
                + " : Build: 2\n" + "---Project: " + proj4 + " : Build: 2\n";
        final String RESULT3 = "-Project: " + proj1 + " : Build: 3\n" + "--Project: " + proj2 + " : Build: 3\n" + "--Project: " + proj3
                + " : Build: 3\n" + "---Project: " + proj4 + " : Build: 3\n" + "---Project: " + proj5 + " : Build: 1\n";

        project1 = createFreeStyleProject(proj1);
        builder1 = new MockBuilder(Result.SUCCESS);
        project2 = createFreeStyleProject(proj2);
        builder2 = new MockBuilder(Result.SUCCESS);
        trigger2 = new BuildTrigger(proj2, true);
        project3 = createFreeStyleProject(proj3);
        builder3 = new MockBuilder(Result.SUCCESS);
        trigger3 = new BuildTrigger(proj3, true);
        project4 = createFreeStyleProject(proj4);
        builder4 = new MockBuilder(Result.SUCCESS);
        trigger4 = new BuildTrigger(proj4, true);
        project5 = createFreeStyleProject(proj5);
        builder5 = new MockBuilder(Result.SUCCESS);
        trigger5 = new BuildTrigger(proj5, true);

        // Add the builders to the respective project's builder lists
        project1.getBuildersList().add(builder1);
        project2.getBuildersList().add(builder2);
        project3.getBuildersList().add(builder3);
        project4.getBuildersList().add(builder4);
        project5.getBuildersList().add(builder5);

        // Project 1 -> Project 2 -> Project 4
        // -> Project 3
        project1.getPublishersList().add(trigger2);
        project1.getPublishersList().add(trigger3);
        project2.getPublishersList().add(trigger4);
        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();
        PipelineBuild pb1 = new PipelineBuild(build1, null, null);
        StringBuffer result = new StringBuffer();
        printDownstreamPipeline("", pb1, result);
        assertEquals(RESULT1, result.toString());

        // Project 1 -> Project 2
        // -> Project 3 -> Project 4
        project1.getPublishersList().add(trigger2);
        project1.getPublishersList().add(trigger3);
        project2.getPublishersList().remove(trigger4);
        project3.getPublishersList().add(trigger4);
        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();
        pb1 = new PipelineBuild(build1, null, null);
        result.delete(0, result.length());
        printDownstreamPipeline("", pb1, result);
        assertEquals(RESULT2, result.toString());

        // Project 1 -> Project 2
        // -> Project 3 -> Project 4
        // -> Project 5
        project1.getPublishersList().add(trigger2);
        project1.getPublishersList().add(trigger3);
        project3.getPublishersList().add(trigger4);
        project3.getPublishersList().add(trigger5);
        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();
        pb1 = new PipelineBuild(build1, null, null);
        result.delete(0, result.length());
        printDownstreamPipeline("", pb1, result);
        assertEquals(RESULT3, result.toString());
    }

    private void printDownstreamPipeline(final String prefix, PipelineBuild pb, StringBuffer result) {
        String newPrefix = prefix + "-";

        result.append(newPrefix + pb.toString() + "\n");
        for (PipelineBuild child : pb.getDownstreamPipeline()) {
            printDownstreamPipeline(newPrefix, child, result);
        }
    }

    @Test
    public void testGetCurrentBuildResult() throws Exception {
        String proj1 = "Proj1";
        String proj2 = "Proj2";
        FreeStyleProject project1;
        FreeStyleBuild build1 = null;
        BuildPipelineTrigger trigger2;

        project1 = createFreeStyleProject(proj1);
        trigger2 = new BuildPipelineTrigger(proj2);

        project1.getPublishersList().add(trigger2);
        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();

        PipelineBuild pb1 = new PipelineBuild(build1, null, null);
        assertEquals(build1 + " should have been " + HudsonResult.SUCCESS, HudsonResult.SUCCESS.toString(), pb1.getCurrentBuildResult());
    }

    @Test
    public void testGetUpstreamPipelineBuild() throws Exception {
        String proj1 = "Proj1";
        String proj2 = "Proj2";
        FreeStyleProject project1, project2;
        FreeStyleBuild build1, build2 = null;

        project1 = createFreeStyleProject(proj1);
        project2 = createFreeStyleProject(proj2);

        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();
        build2 = project2.getLastBuild();

        PipelineBuild pb1 = new PipelineBuild(build1, null, null);
        PipelineBuild pb2 = new PipelineBuild(build2, null, build1);
        assertEquals("Upstream PipelineBuild should have been " + pb1.toString(), pb1.toString(), pb2.getUpstreamPipelineBuild().toString());
    }

    @Test
    public void testGetUpstreamBuildResult() throws Exception {
        String proj1 = "Proj1";
        String proj2 = "Proj2";
        FreeStyleProject project1, project2;
        FreeStyleBuild build1 = null;
        FreeStyleBuild build2 = null;
        BuildPipelineTrigger trigger2;

        project1 = createFreeStyleProject(proj1);
        project2 = createFreeStyleProject(proj2);
        trigger2 = new BuildPipelineTrigger(proj2);

        project1.getPublishersList().add(trigger2);
        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();
        build2 = project2.getLastBuild();

        PipelineBuild pb1 = new PipelineBuild(build2, null, build1);
        assertEquals(build2 + " should have been " + HudsonResult.SUCCESS, HudsonResult.SUCCESS.toString(), pb1.getUpstreamBuildResult());
    }

    @Test
    public void testGetBuildResultURL() throws Exception {
        String proj1 = "Proj 1";
        String proj1Url = "/job/Proj%201/1/";
        FreeStyleProject project1;
        FreeStyleBuild build1;
        project1 = createFreeStyleProject(proj1);
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();

        PipelineBuild pb = new PipelineBuild(build1, null, null);

        assertEquals("The build URL should have been " + proj1Url, proj1Url, pb.getBuildResultURL());
    }

    @Test
    public void testToString() throws Exception {
        String proj1 = "Proj1";
        String proj1ToString = "Project: " + proj1 + " : Build: 1";
        FreeStyleProject project1;
        FreeStyleBuild build1;
        project1 = createFreeStyleProject(proj1);
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();

        PipelineBuild pb = new PipelineBuild(build1, null, null);

        assertEquals("The toString should have been " + proj1ToString, proj1ToString, pb.toString());
    }

    @Test
    public void testGetBuildDescription() throws Exception {
        String proj1 = "Proj1";
        String proj1BuildDescFail = "Pending build of project: " + proj1;
        String proj1BuildDescSuccess = proj1 + " #1";
        FreeStyleProject project1;
        FreeStyleBuild build1;
        project1 = createFreeStyleProject(proj1);
        PipelineBuild pb = new PipelineBuild(null, project1, null);

        assertEquals("The build description should have been " + proj1BuildDescFail, proj1BuildDescFail, pb.getBuildDescription());

        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();
        pb.setCurrentBuild(build1);

        assertEquals("The build description should have been " + proj1BuildDescSuccess, proj1BuildDescSuccess, pb.getBuildDescription());
    }

    @Test
    public void testGetSVNRevisionNo() throws Exception {
        String proj1 = "Proj1";
        String proj1GetSVN = "No Revision";
        FreeStyleProject project1;
        FreeStyleBuild build1;
        project1 = createFreeStyleProject(proj1);
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();

        PipelineBuild pb = new PipelineBuild(build1, null, null);
        assertEquals("The SVN Revision text should have been " + proj1GetSVN, proj1GetSVN, pb.getSVNRevisionNo());
    }
}
