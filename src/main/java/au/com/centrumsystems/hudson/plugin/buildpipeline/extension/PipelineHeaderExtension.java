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

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractBuild;
import hudson.model.AbstractDescribableImpl;
import jenkins.model.Jenkins;

import java.util.Map;

/**
 * An extension point to provide column and/or row headers for the build pipeline
 *
 * @author dalvizu
 */
public abstract class PipelineHeaderExtension
        extends AbstractDescribableImpl<PipelineHeaderExtension>
        implements ExtensionPoint,
        Comparable<PipelineHeaderExtension> {

    /**
     * @return all known <code>PipelineHeaderExtension</code>s
     */
    public static ExtensionList<PipelineHeaderExtension> all() {
        return Jenkins.getInstance().getExtensionList(PipelineHeaderExtension.class);
    }

    /**
     * @param build - the build to find parameters for
     * @return parameters to display in the pipeline
     */
    public abstract Map<String, String> getParameters(AbstractBuild<?, ?> build);

    /**
     * @return whether this extension can be a Row header
     */
    public boolean appliesToRows() {
        return true;
    }

    /**
     * @return whether this extension can be a Column header
     */
    public boolean appliesToColumns() {
        return true;
    }

    /**
     * Return an index to where this should be displayed, relative to other options
     *
     * @return
     *  the index - lower appears first in the list
     */
    public abstract long getIndex();

    @Override
    public int compareTo(PipelineHeaderExtension o) {
        return Long.compare(getIndex(), o.getIndex());
    }

}
