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

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.tasks.BuildTrigger;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.MockBuilder;

import au.com.centrumsystems.hudson.plugin.buildpipeline.PipelineBuild;

public class PipelineViewUITest extends HudsonTestCase {
    private static final String TEST_PROJECT1 = "Main Project1";
    private static final String TEST_PROJECT2 = "Downstream Project2";
    private static final String TEST_PROJECT3 = "Downstream Project3";
    private static final String TEST_PROJECT4 = "Downstream Project4";
    private static final String TEST_EMPTY_CELL = "<table><tr><td align=\"center\" valign=\"middle\"><div class=\"EMPTY rounded\" style=\"height=40px\"></div></td><td align=\"center\" valign=\"middle\">";
    private static final String TEST_SVN_REVISION_CELL = "<table><tr><td align=\"center\" valign=\"middle\"><div class=\"RevisionNo rounded\" style=\"height=40px\">No Revision</div></td><td align=\"center\" valign=\"middle\">";

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
    public void testGetProjectBuildPipeline() {
        FreeStyleProject project1, project2, project3, project4;
        MockBuilder builder1, builder2, builder3, builder4;
        FreeStyleBuild build1, build2, build3, build4 = null;
        final String BUILD1_TABLE = "<table><tr><td class=\"SUCCESS rounded\"><a href=/job/Main Project1/1/ target=\"_blank\">Main Project1 #1<br />Duration:";
        final String BUILD2_TABLE = "<table><tr><td class=\"SUCCESS rounded\"><a href=/job/Downstream Project2/1/ target=\"_blank\">Downstream Project2 #1<br />Duration:";
        final String BUILD3_TABLE = "<tr><td class=\"SUCCESS rounded\"><a href=/job/Downstream Project3/1/ target=\"_blank\">Downstream Project3 #1<br />Duration:";
        final String BUILD4_TABLE = "<table><tr><td class=\"SUCCESS rounded\"><a href=/job/Downstream Project4/1/ target=\"_blank\">Downstream Project4 #1<br />Duration:";
        // Create test projects and associated builders
        try {
            project1 = createFreeStyleProject(TEST_PROJECT1);
            builder1 = new MockBuilder(Result.SUCCESS);
            project2 = createFreeStyleProject(TEST_PROJECT2);
            builder2 = new MockBuilder(Result.SUCCESS);
            project3 = createFreeStyleProject(TEST_PROJECT3);
            builder3 = new MockBuilder(Result.SUCCESS);
            project4 = createFreeStyleProject(TEST_PROJECT4);
            builder4 = new MockBuilder(Result.SUCCESS);

            // Project1 -> Project2 -> Project4
            // -> Project3
            project1.getPublishersList().add(new BuildTrigger(TEST_PROJECT2, true));
            project1.getPublishersList().add(new BuildTrigger(TEST_PROJECT3, true));
            project2.getPublishersList().add(new BuildTrigger(TEST_PROJECT4, true));
            // Important; we must do this step to ensure that the dependency graphs are updated
            Hudson.getInstance().rebuildDependencyGraph();

            // Add the builders to the respective project's builder lists
            project1.getBuildersList().add(builder1);
            project2.getBuildersList().add(builder2);
            project3.getBuildersList().add(builder3);
            project4.getBuildersList().add(builder4);

            build1 = buildAndAssertSuccess(project1);
            // When all building is complete retrieve the last build from project2
            waitUntilNoActivity();
            // build2 = project2.getLastBuild();
            // build3 = project3.getLastBuild();
            // build4 = project4.getLastBuild();

            // Test the method
            PipelineBuild pb = new PipelineBuild(build1, null, null);
            StringBuffer result = new StringBuffer();
            // final List<PipelineBuild> builds = BuildUtil.getProjectBuildPipeline(build1);
            PipelineViewUI.getBuildPipeline("", pb, result);
            // assertStringContains("Should contain the HTML table for " + build1, result.toString());
            // assertStringContains("Should contain the HTML table for " + build2, PipelineViewUI.getBuildPipeline(builds.get(1)),
            // BUILD2_TABLE);
            // assertStringContains("Should contain the HTML table for " + build3, PipelineViewUI.getBuildPipeline(builds.get(1)),
            // BUILD3_TABLE);
            // assertStringContains("Should contain the HTML table for " + build4, PipelineViewUI.getBuildPipeline(builds.get(2)),
            // BUILD4_TABLE);
        } catch (Exception e) {
            e.toString();
        }
    }

    @Test
    public void testAddRevisionCell() {
        StringBuffer outputBuffer = new StringBuffer();
        FreeStyleProject project1;
        FreeStyleBuild build1;
        try {
            project1 = createFreeStyleProject(TEST_PROJECT1);
            build1 = buildAndAssertSuccess(project1);
            // When all building is complete retrieve the last builds
            waitUntilNoActivity();

            PipelineBuild pb = new PipelineBuild(build1, null, null);
            PipelineViewUI.addRevisionCell(pb, outputBuffer);

            assertEquals(TEST_SVN_REVISION_CELL, outputBuffer.toString());

        } catch (Exception e) {
            e.toString();
        }
    }

    @Test
    public void testAddEmptyCell() {
        StringBuffer outputBuffer = new StringBuffer();

        PipelineViewUI.addEmptyCell(outputBuffer);

        assertEquals(TEST_EMPTY_CELL, outputBuffer.toString());
    }
}
