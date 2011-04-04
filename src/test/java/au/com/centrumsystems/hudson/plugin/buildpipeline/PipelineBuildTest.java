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

    private static final String TEST_PROJECT1 = "Main Project1";
    private static final String TEST_PROJECT2 = "Downstream Project2";
    private static final String TEST_PROJECT3 = "Downstream Project3";
    private static final String TEST_PROJECT4 = "Downstream Project4";
    private static final String TEST_PROJECT5 = "Downstream Project5";
    private static final String TEST_PROJECT1_BUILD_URL = "/job/Main%20Project1/1/";
    private static final String TEST_TO_STRING_RESULT = "Project: " + TEST_PROJECT1 + " : Build: 1";
    private static final String TEST_BUILD_DESCRIPTION_FAIL = "Pending build of project: " + TEST_PROJECT1;
    private static final String TEST_BUILD_DESCRIPTION_SUCCESS = TEST_PROJECT1 + " #1";
    private static final String TEST_GET_SVN = "No Revision";

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
    public void testGetDownstreamPipeline() {
        FreeStyleProject project1, project2, project3, project4, project5;
        MockBuilder builder1, builder2, builder3, builder4, builder5;
        FreeStyleBuild build1, build2, build3, build4 = null;
        BuildTrigger trigger2, trigger3, trigger4, trigger5;
        final String RESULT1 = "-Project: Main Project1 : Build: 1\n" + "--Project: Downstream Project2 : Build: 1\n"
                + "---Project: Downstream Project4 : Build: 1\n" + "--Project: Downstream Project3 : Build: 1\n";
        final String RESULT2 = "-Project: Main Project1 : Build: 2\n" + "--Project: Downstream Project2 : Build: 2\n"
                + "--Project: Downstream Project3 : Build: 2\n" + "---Project: Downstream Project4 : Build: 2\n";
        final String RESULT3 = "-Project: Main Project1 : Build: 3\n" + "--Project: Downstream Project2 : Build: 3\n"
                + "--Project: Downstream Project3 : Build: 3\n" + "---Project: Downstream Project4 : Build: 3\n"
                + "---Project: Downstream Project5 : Build: 1\n";

        // Create test projects and associated builders
        try {
            project1 = createFreeStyleProject(TEST_PROJECT1);
            builder1 = new MockBuilder(Result.SUCCESS);
            project2 = createFreeStyleProject(TEST_PROJECT2);
            builder2 = new MockBuilder(Result.SUCCESS);
            trigger2 = new BuildTrigger(TEST_PROJECT2, true);
            project3 = createFreeStyleProject(TEST_PROJECT3);
            builder3 = new MockBuilder(Result.SUCCESS);
            trigger3 = new BuildTrigger(TEST_PROJECT3, true);
            project4 = createFreeStyleProject(TEST_PROJECT4);
            builder4 = new MockBuilder(Result.SUCCESS);
            trigger4 = new BuildTrigger(TEST_PROJECT4, true);
            project5 = createFreeStyleProject(TEST_PROJECT5);
            builder5 = new MockBuilder(Result.SUCCESS);
            trigger5 = new BuildTrigger(TEST_PROJECT5, true);

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
        } catch (Exception e) {
            e.toString();
        }
    }

    private void printDownstreamPipeline(final String prefix, PipelineBuild pb, StringBuffer result) {
        String newPrefix = prefix + "-";

        result.append(newPrefix + pb.toString() + "\n");
        for (PipelineBuild child : pb.getDownstreamPipeline()) {
            printDownstreamPipeline(newPrefix, child, result);
        }
    }

    @Test
    public void testGetCurrentBuildResult() {
        FreeStyleProject project1, project2;
        MockBuilder builder1, builder2;
        FreeStyleBuild build1 = null;
        BuildPipelineTrigger trigger2;

        try {
            project1 = createFreeStyleProject(TEST_PROJECT1);
            builder1 = new MockBuilder(Result.SUCCESS);
            project2 = createFreeStyleProject(TEST_PROJECT2);
            builder2 = new MockBuilder(Result.SUCCESS);
            trigger2 = new BuildPipelineTrigger(TEST_PROJECT2);

            project1.getPublishersList().add(trigger2);
            // Important; we must do this step to ensure that the dependency graphs are updated
            Hudson.getInstance().rebuildDependencyGraph();

            // Build project1
            build1 = buildAndAssertSuccess(project1);
            // When all building is complete retrieve the last builds
            waitUntilNoActivity();

            PipelineBuild pb1 = new PipelineBuild(build1, null, null);
            assertEquals(build1 + " should have been " + HudsonResult.SUCCESS, HudsonResult.SUCCESS.toString(), pb1.getCurrentBuildResult());
        } catch (Exception e) {
            e.toString();
        }
    }

    @Test
    public void testGetUpstreamPipelineBuild() {
        FreeStyleProject project1, project2;
        MockBuilder builder1, builder2;
        FreeStyleBuild build1, build2 = null;
        BuildPipelineTrigger trigger2;

        try {
            project1 = createFreeStyleProject(TEST_PROJECT1);
            builder1 = new MockBuilder(Result.SUCCESS);
            project2 = createFreeStyleProject(TEST_PROJECT2);
            builder2 = new MockBuilder(Result.SUCCESS);
            trigger2 = new BuildPipelineTrigger(TEST_PROJECT2);

            project1.getPublishersList().add(trigger2);
            // Important; we must do this step to ensure that the dependency graphs are updated
            Hudson.getInstance().rebuildDependencyGraph();

            // Build project1
            build1 = buildAndAssertSuccess(project1);
            // When all building is complete retrieve the last builds
            waitUntilNoActivity();
            build2 = project2.getLastBuild();

            PipelineBuild pb1 = new PipelineBuild(build1, null, null);
            PipelineBuild pb2 = new PipelineBuild(build2, null, null);
            assertEquals("Upstream PipelineBuild should have been " + pb1.toString(), pb1, pb2.getUpstreamPipelineBuild());
        } catch (Exception e) {
            e.toString();
        }
    }

    @Test
    public void testGetUpstreamBuildResult() {
        FreeStyleProject project1, project2;
        MockBuilder builder1, builder2;
        FreeStyleBuild build1, build2 = null;
        BuildPipelineTrigger trigger2;

        try {
            project1 = createFreeStyleProject(TEST_PROJECT1);
            builder1 = new MockBuilder(Result.SUCCESS);
            project2 = createFreeStyleProject(TEST_PROJECT2);
            builder2 = new MockBuilder(Result.SUCCESS);
            trigger2 = new BuildPipelineTrigger(TEST_PROJECT2);

            project1.getPublishersList().add(trigger2);
            // Important; we must do this step to ensure that the dependency graphs are updated
            Hudson.getInstance().rebuildDependencyGraph();

            // Build project1
            build1 = buildAndAssertSuccess(project1);
            // When all building is complete retrieve the last builds
            waitUntilNoActivity();
            build2 = project2.getLastBuild();

            PipelineBuild pb1 = new PipelineBuild(build2, null, null);
            assertEquals(build2 + " should have been " + HudsonResult.SUCCESS, HudsonResult.SUCCESS.toString(),
                    pb1.getUpstreamBuildResult());
        } catch (Exception e) {
            e.toString();
        }
    }

    @Test
    public void testGetBuildResultURL() {
        FreeStyleProject project1;
        FreeStyleBuild build1;
        try {
            project1 = createFreeStyleProject(TEST_PROJECT1);
            build1 = buildAndAssertSuccess(project1);
            // When all building is complete retrieve the last builds
            waitUntilNoActivity();

            PipelineBuild pb = new PipelineBuild(build1, null, null);

            assertEquals("The build URL should have been " + TEST_PROJECT1_BUILD_URL, TEST_PROJECT1_BUILD_URL, pb.getBuildResultURL());
        } catch (Exception e) {
            e.toString();
        }
    }

    @Test
    public void testToString() {
        FreeStyleProject project1;
        FreeStyleBuild build1;
        try {
            project1 = createFreeStyleProject(TEST_PROJECT1);
            build1 = buildAndAssertSuccess(project1);
            // When all building is complete retrieve the last builds
            waitUntilNoActivity();

            PipelineBuild pb = new PipelineBuild(build1, null, null);

            assertEquals("The toString should have been " + TEST_TO_STRING_RESULT, TEST_TO_STRING_RESULT, pb.toString());
        } catch (Exception e) {
            e.toString();
        }
    }

    @Test
    public void testGetBuildDescription() {
        FreeStyleProject project1;
        FreeStyleBuild build1;
        try {
            project1 = createFreeStyleProject(TEST_PROJECT1);
            PipelineBuild pb = new PipelineBuild(null, project1, null);

            assertEquals("The build description should have been " + TEST_BUILD_DESCRIPTION_FAIL, TEST_BUILD_DESCRIPTION_FAIL,
                    pb.getBuildDescription());

            build1 = buildAndAssertSuccess(project1);
            // When all building is complete retrieve the last builds
            waitUntilNoActivity();
            pb.setCurrentBuild(build1);

            assertEquals("The build description should have been " + TEST_BUILD_DESCRIPTION_SUCCESS, TEST_BUILD_DESCRIPTION_SUCCESS,
                    pb.getBuildDescription());
        } catch (Exception e) {
            e.toString();
        }
    }

    @Test
    public void testGetSVNRevisionNo() {
        FreeStyleProject project1;
        FreeStyleBuild build1;
        try {
            project1 = createFreeStyleProject(TEST_PROJECT1);
            build1 = buildAndAssertSuccess(project1);
            // When all building is complete retrieve the last builds
            waitUntilNoActivity();

            PipelineBuild pb = new PipelineBuild(build1, null, null);
            assertEquals("The SVN Revision text should have been " + TEST_GET_SVN, TEST_GET_SVN, pb.getSVNRevisionNo());
        } catch (Exception e) {
            e.toString();
        }
    }
}
