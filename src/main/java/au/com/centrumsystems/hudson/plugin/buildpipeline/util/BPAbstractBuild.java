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
package au.com.centrumsystems.hudson.plugin.buildpipeline.util;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.CauseAction;

import java.util.ArrayList;
import java.util.List;

/**
 * CentrumAbstractBuild class is a util class which is used to build the build pipeline
 * 
 * @author Centrum Systems
 * 
 */
public final class BPAbstractBuild {
    
    /**
     * get the project build pipeline for a particular build
     * 
     * @param topAbstractBuild top abstract build
     * @return List of AbstractBuild 
     */
    public static List<AbstractBuild<?, ?>> getProjectBuildPipeline(final AbstractBuild<?, ?> topAbstractBuild) {
        final List<AbstractBuild<?, ?>> pipeline = new ArrayList<AbstractBuild<?, ?>>();
        pipeline.add(topAbstractBuild);

        pipeline.addAll(getDownstreamBuilds(topAbstractBuild));
        return pipeline;
    }

    /**
     * Get the down stream build
     * 
     * @param upstreamBuild
     *            Upstream Build
     * @return List of AbstractBuild
     */
    @SuppressWarnings("unchecked")
    private static List<AbstractBuild<?, ?>> getDownstreamBuilds(final AbstractBuild<?, ?> upstreamBuild) {
        final List<AbstractBuild<?, ?>> pipeline = new ArrayList<AbstractBuild<?, ?>>();
        final AbstractProject<?, ?> project = upstreamBuild.getProject();

        for (final AbstractProject<?, ?> proj : project.getDownstreamProjects()) {
            for (final AbstractBuild<?, ?> innerBuild : (List<AbstractBuild<?, ?>>) proj.getBuilds()) {
                for (final CauseAction action : innerBuild.getActions(CauseAction.class)) {
                    for (final Cause cause : action.getCauses()) {
                        if (cause instanceof UpstreamCause) {
                            final UpstreamCause upstreamCause = (UpstreamCause) cause;                            
                            if (upstreamCause.getUpstreamProject().equals(project.getName()) 
                                    && (upstreamCause.getUpstreamBuild() == upstreamBuild.getNumber())) {
                                pipeline.add(innerBuild);
                                pipeline.addAll(getDownstreamBuilds(innerBuild));
                                break;
                            }
                        }
                    }
                }
            }
        }
        return pipeline;
    }

    /**
     * Build a URL of a particular build
     * 
     * @param build
     *            the build
     * @return URL of the build
     */
    public static String getBuildResultURL(final AbstractBuild<?, ?> build) {
        final StringBuffer resultURL = new StringBuffer();
        resultURL.append("/job/");
        resultURL.append(build.getProject().getName());
        resultURL.append('/');
        resultURL.append(build.getNumber());
        resultURL.append('/');
        return resultURL.toString();
    }
}
