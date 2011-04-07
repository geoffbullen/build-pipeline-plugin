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
import hudson.model.FreeStyleProject;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import au.com.centrumsystems.hudson.plugin.buildpipeline.PipelineBuild;

public class PipelineViewUITest extends HudsonTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testAddRevisionCell() throws Exception {
        String proj1 = "Proj1";
        String svnRevisionCell = "<table><tr><td align=\"center\" valign=\"middle\"><div class=\"RevisionNo rounded\" style=\"height=40px\">No Revision</div></td><td align=\"center\" valign=\"middle\">";
        StringBuffer outputBuffer = new StringBuffer();
        FreeStyleProject project1;
        FreeStyleBuild build1;
        project1 = createFreeStyleProject(proj1);
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();

        PipelineBuild pb = new PipelineBuild(build1, null, null);
        PipelineViewUI.addRevisionCell(pb, outputBuffer);

        assertEquals(svnRevisionCell, outputBuffer.toString());
    }

    @Test
    public void testAddEmptyCell() {
        String emptyCell = "<table><tr><td align=\"center\" valign=\"middle\"><div class=\"EMPTY rounded\" style=\"height=40px\"></div></td><td align=\"center\" valign=\"middle\">";
        StringBuffer outputBuffer = new StringBuffer();

        PipelineViewUI.addEmptyCell(outputBuffer);

        assertEquals(emptyCell, outputBuffer.toString());
    }
}
