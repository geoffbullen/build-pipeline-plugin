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
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Pull values from build variables, hiding sensitive ones
 *
 * @author dalvizu
 */
@Extension
public class BuildVariablesHeader
    extends AbstractNameValueHeader {

    /**
     * Default constructor
     */
    @DataBoundConstructor
    public BuildVariablesHeader() {
        super();
    }

    @Override
    public Map<String, String> getParameters(AbstractBuild<?, ?> build) {
        final Map<String, String> retval = new HashMap<String, String>();
        if (build != null) {
            retval.putAll(build.getBuildVariables());
            final Set<String> sensitiveBuildVariables = build.getSensitiveBuildVariables();
            for (String paramName : sensitiveBuildVariables) {
                if (retval.containsKey(paramName)) {
                    // We have the choice to hide the parameter or to replace it with special characters
                    retval.put(paramName, "********");
                    //retval.remove(paramName);
                }
            }
        }

        return retval;
    }

    @Override
    public long getIndex() {
        return 2000;
    }

    /**
     * Descriptor, since we're in the UI
     */
    @Extension
    public static class DescriptorImpl
            extends Descriptor<PipelineHeaderExtension> {

        @Override
        public String getDisplayName() {
            return "Build variables";
        }
    }
}
