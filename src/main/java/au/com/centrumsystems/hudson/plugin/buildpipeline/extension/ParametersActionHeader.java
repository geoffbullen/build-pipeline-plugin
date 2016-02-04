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

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Provide parameters only from the ParametersAction and nothing else
 *
 * See issue JENKINS-14591
 *
 * @see <a href="https://github.com/jenkinsci/build-pipeline-plugin/pull/74">Pull request #74</a>
 * @author dalvizu
 */
@Extension(optional = true)
public class ParametersActionHeader
    extends AbstractNameValueHeader {

    /**
     * Default constructor
     */
    @DataBoundConstructor
    public ParametersActionHeader() {
        super();
    }

    @Override
    public Map<String, String> getParameters(AbstractBuild<?, ?> build) {
        final Map<String, String> retval = new HashMap<String, String>();
        if (build != null) {
            final ParametersAction action = build.getAction(ParametersAction.class);
            if (action != null) {
                for (ParameterValue value : action.getParameters()) {
                    if (value.isSensitive()) {
                        // We have the choice to hide the parameter or to replace it with special characters
                        retval.put(value.getName(), "********");
                    } else {
                        retval.put(value.getName(), value.createVariableResolver(build).resolve(value.getName()));
                    }
                }
            }
        }
        return retval;
    }

    @Override
    public long getIndex() {
        return 1000;
    }

    /**
     * Descriptor, since we show it in UI
     */
    @Extension
    public static class DescriptorImpl
            extends Descriptor<PipelineHeaderExtension> {

        @Override
        public String getDisplayName() {
            return "Parameters only";
        }
    }
}
