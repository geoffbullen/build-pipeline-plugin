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
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Just show the pipeline number
 *
 * @author dalvizu
 */
public class SimpleRowHeader
    extends PipelineHeaderExtension {

    /**
     * Default constructor
     */
    @DataBoundConstructor
    public SimpleRowHeader() {
        super();
    }

    @Override
    public Map<String, String> getParameters(AbstractBuild<?, ?> build) {
        return new HashMap<String, String>();
    }

   /**
     * Descriptor since we're in the UI
     */
    @Extension
    public static class DescriptorImpl
            extends PipelineHeaderExtensionDescriptor {

       @Override
       public boolean appliesToColumns() {
           return false;
       }

       @Override
       public long getIndex() {
           return 100;
       }


       @Override
        public String getDisplayName() {
            return "Just the pipeline number";
        }
    }
}
