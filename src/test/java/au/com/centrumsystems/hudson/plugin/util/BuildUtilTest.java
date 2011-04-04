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
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.tasks.BuildTrigger;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.MockBuilder;

public class BuildUtilTest extends HudsonTestCase {

    private static final String TEST_PROJECT1 = "Main Project1";
    private static final String TEST_PROJECT2 = "Downstream Project2";
    private static final String TEST_PROJECT3 = "Downstream Project3";

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
    public void testGetDownstreamBuild() {
        FreeStyleProject project1, project2, project3;
        MockBuilder builder1, builder2, builder3;
        FreeStyleBuild build1, build2, build3;

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

            AbstractBuild<?, ?> nextBuild = BuildUtil.getDownstreamBuild(project2, build1);
            assertEquals("The next build should be " + TEST_PROJECT1 + build2.number, build2, nextBuild);

            nextBuild = BuildUtil.getDownstreamBuild(project3, nextBuild);
            assertEquals("The next build should be " + TEST_PROJECT1 + build3.number, build3, nextBuild);
        } catch (Exception e) {
            e.toString();
        }
    }
}
