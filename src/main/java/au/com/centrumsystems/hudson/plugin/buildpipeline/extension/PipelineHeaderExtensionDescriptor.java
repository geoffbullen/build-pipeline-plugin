/***************************************************************************
 * Copyright (C) 2014 Ping Identity Corporation
 * All rights reserved.
 * <p/>
 * The contents of this file are the property of Ping Identity Corporation.
 * You may not copy or use this file, in either source code or executable
 * form, except in compliance with terms set by Ping Identity Corporation.
 * For further information please contact:
 * <p/>
 * Ping Identity Corporation
 * 1001 17th Street Suite 100
 * Denver, CO 80202
 * 303.468.2900
 * http://www.pingidentity.com
 **************************************************************************/
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
