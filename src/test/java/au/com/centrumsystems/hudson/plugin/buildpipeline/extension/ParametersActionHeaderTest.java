/*
 * The MIT License
 *
 * Copyright (c) 2016 the Jenkins project
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
package au.com.centrumsystems.hudson.plugin.buildpipeline.extension;

import hudson.model.AbstractBuild;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.PasswordParameterValue;
import hudson.model.StringParameterValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author dalvizu
 */
public class ParametersActionHeaderTest {

    private ParametersActionHeader provider = new ParametersActionHeader();

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void setup() {

    }

    @Test
    public void testNoParameterAction() {
        AbstractBuild build = mock(AbstractBuild.class);
        assertTrue(provider.getParameters(build).isEmpty());
    }

    @Test
    public void testGetParameters() {
        List<ParameterValue> parameterList = new ArrayList<ParameterValue>();
        ParameterValue sensitiveParameter = new PasswordParameterValue("password", "123");
        ParameterValue nonsensitiveParameter = new StringParameterValue("foo", "bar");
        parameterList.add(sensitiveParameter);
        parameterList.add(nonsensitiveParameter);
        ParametersAction action = mock(ParametersAction.class);
        when(action.getParameters()).thenReturn(parameterList);
        AbstractBuild build = mock(AbstractBuild.class);
        when(build.getAction(ParametersAction.class)).thenReturn(action);
        Map<String, String> result = provider.getParameters(build);
        assertEquals(result.size(), 2);
        assertEquals(result.get("password"), "********");
        assertEquals(result.get("foo"), "bar");
    }
}
