/*
 * The MIT License
 * 
 * Copyright (c) 2011, Centrum Systems Pty Ltd
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import au.com.centrumsystems.hudson.plugin.buildpipeline.util.HudsonResult;

/**
 * Test Hudson Result ENUM class
 * 
 * @author rayc
 * 
 */
public class TestHudsonResult {
    @Test
    public void testHudsonResult() {
        final HudsonResult hudsonResult = HudsonResult.SUCCESS;
        assertNotNull("Hudson result should not be null", hudsonResult);
    }

    @Test
    public void testCompletedStatus() {
        final HudsonResult completed = HudsonResult.SUCCESS;
        assertEquals("Hudson Result values is completed", completed, HudsonResult.values()[0]);
    }

    @Test
    public void testUnstableStatus() {
        final HudsonResult UNSTABLE = HudsonResult.UNSTABLE;
        assertEquals("Hudson Result values is UNSTABLE", UNSTABLE, HudsonResult.values()[1]);
    }

    @Test
    public void testFailureStatus() {
        final HudsonResult FAILURE = HudsonResult.FAILURE;
        assertEquals("Hudson Result values is FAILURE", FAILURE, HudsonResult.values()[2]);
    }

    @Test
    public void testNotBuiltStatus() {
        final HudsonResult NOT_BUILT = HudsonResult.NOT_BUILT;
        assertEquals("Hudson Result values is NOT_BUILT", NOT_BUILT, HudsonResult.values()[3]);
    }

    @Test
    public void testAbortStatus() {
        final HudsonResult ABORT = HudsonResult.ABORT;
        assertEquals("Hudson Result values is ABORT", ABORT, HudsonResult.values()[4]);
    }
}
