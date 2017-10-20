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

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

/**
 * @author dalvizu
 */
public abstract class PipelineHeaderExtensionDescriptor
    extends Descriptor<PipelineHeaderExtension>
    implements ExtensionPoint, Comparable<PipelineHeaderExtensionDescriptor> {

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

    /**
     * @return all known <code>PipelineHeaderExtensionDescriptor</code>
     */
    public static DescriptorExtensionList<PipelineHeaderExtension, PipelineHeaderExtensionDescriptor> all() {
        return Jenkins.getInstance().getDescriptorList(PipelineHeaderExtension.class);
    }

    @Override
    public int compareTo(PipelineHeaderExtensionDescriptor other) {
        return Long.compare(getIndex(), other.getIndex());
    }
}
