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
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.tasks.BuildTrigger;
import hudson.util.NullStream;
import hudson.util.StreamTaskListener;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.MockBuilder;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.DownstreamDependency;
import au.com.centrumsystems.hudson.plugin.buildpipeline.util.BPAbstractBuild;

public class TestBPAbstractBuild extends HudsonTestCase {

    private static FreeStyleProject project1, project2, project3;
    private static MockBuilder builder1, builder2, builder3;
    private static FreeStyleBuild build1, build2, build3;

    private static final String TEST_PROJECT1 = "Main Project";
    private static final String TEST_PROJECT2 = "Downstream Project";
    private static final String TEST_PROJECT3 = "Downstream Project3";

    /** A Logger object is used to log messages */
    // private static final Logger LOGGER = Logger.getLogger(TestBPAbstractBuild.class.getName());

    @Before
    public void setUp() {
        try {
            super.setUp();
        } catch (Exception e) {
            e.toString();
        }
    }

    @Test
    public void testGetBuildURL() throws IOException {
        project1 = createFreeStyleProject(TEST_PROJECT1);
        build1 = new FreeStyleBuild(project1);
        assertEquals("Check URL of the build", "/job/" + TEST_PROJECT1 + "/1/", BPAbstractBuild.getBuildResultURL(build1));
    }

    @Test
    public void testGetProjectBuildPipeline() {
        // Create test projects and associated builders
        try {
            project1 = createFreeStyleProject(TEST_PROJECT1);
            builder1 = new MockBuilder(Result.SUCCESS);
            project2 = createFreeStyleProject(TEST_PROJECT2);
            builder2 = new MockBuilder(Result.SUCCESS);
            project3 = createFreeStyleProject(TEST_PROJECT3);
            builder3 = new MockBuilder(Result.SUCCESS);

            // Add project2 as a post build action: build other project
            project1.getPublishersList().add(new BuildTrigger(TEST_PROJECT2, true));
            project2.getPublishersList().add(new BuildTrigger(TEST_PROJECT3, true));

            // Important; we must do this step to ensure that the dependency graphs are updated
            Hudson.getInstance().rebuildDependencyGraph();

            // Add the builders to the respective project's builder lists
            project1.getBuildersList().add(builder1);
            project2.getBuildersList().add(builder2);
            project3.getBuildersList().add(builder3);

            // Build project1, upon completion project2 will be built

            build1 = buildAndAssertSuccess(project1);
            // When all building is complete retrieve the last build from project2
            waitUntilNoActivity();
            build2 = project2.getLastBuild();
            build3 = project3.getLastBuild();

            assertNotNull("Check downstream projects", project1.getDownstreamProjects());
        } catch (Exception e) {
            e.toString();
        }
        // Test the method
        final List<AbstractBuild<?, ?>> builds = BPAbstractBuild.getProjectBuildPipeline(build1);
        assertEquals("Check abstract Build 1", build1, builds.get(0));
        assertEquals("Check abstract Build 2", build2, builds.get(1));

        final DownstreamDependency dd = new DownstreamDependency(project1, project2, true);
        dd.setTriggerBuildFlag(true);
        assertTrue(" Should Trigger the build",
                dd.shouldTriggerBuild(build1, new StreamTaskListener(new NullStream()), Hudson.getInstance().getActions()));

        final List<AbstractBuild<?, ?>> projectThreeBuilds = BPAbstractBuild.getProjectBuildPipeline(build3);
        assertNotNull("Check Project 3 builds", projectThreeBuilds);

        //build3 = buildAndAssertSuccess(project3);
        //buildAndAssertSuccess(project2);
    }
}
