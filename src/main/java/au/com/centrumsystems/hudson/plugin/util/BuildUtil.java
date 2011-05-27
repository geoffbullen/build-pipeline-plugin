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
package au.com.centrumsystems.hudson.plugin.util;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.CauseAction;

import java.util.List;

/**
 * Provides helper methods for #hudson.model.AbstractBuild
 * 
 * @author Centrum Systems
 * 
 */
public final class BuildUtil {

    /**
     * Gets the next downstream build based on the upstream build and downstream project.
     * 
     * @param downstreamProject
     *            - The downstream project
     * @param upstreamBuild
     *            - The upstream build
     * @return - The next downstream build based on the upstream build and downstream project, or null if there is no downstream project.
     */
    public static AbstractBuild<?, ?> getDownstreamBuild(final AbstractProject<?, ?> downstreamProject,
            final AbstractBuild<?, ?> upstreamBuild) {

        for (final AbstractBuild<?, ?> innerBuild : (List<AbstractBuild<?, ?>>) downstreamProject.getBuilds()) {
            for (final CauseAction action : innerBuild.getActions(CauseAction.class)) {
                for (final Cause cause : action.getCauses()) {
                    if (cause instanceof UpstreamCause) {
                        final UpstreamCause upstreamCause = (UpstreamCause) cause;
                        if (upstreamCause.getUpstreamProject().equals(upstreamBuild.getProject().getName())
                                && (upstreamCause.getUpstreamBuild() == upstreamBuild.getNumber())) {
                            return innerBuild;
                        }
                    }
                }
            }
        }
        return null;
    }

}
