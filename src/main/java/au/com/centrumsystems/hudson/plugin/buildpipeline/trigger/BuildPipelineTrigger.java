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
package au.com.centrumsystems.hudson.plugin.buildpipeline.trigger;

import hudson.Extension;
import hudson.Util;
import hudson.model.DependecyDeclarer;
import hudson.model.DependencyGraph;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.tasks.Messages;
import hudson.tasks.BuildTrigger;
import hudson.util.FormValidation;

import java.util.StringTokenizer;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * The build pipeline trigger allows the creation of downstream jobs which aren't triggered automatically. This allows us to have manual
 * "approval" steps in the process where jobs are manually promoted along the pipeline by a user pressing a button on the view.
 *
 * @author Centrum Systems
 *
 */
@SuppressWarnings("unchecked")
public class BuildPipelineTrigger extends BuildTrigger implements DependecyDeclarer {

    /** downstream project name */
    private String downstreamProjectNames;

    public String getDownstreamProjectNames() {
        return downstreamProjectNames;
    }

    public void setDownstreamProjectNames(final String downstreamProjectNames) {
        this.downstreamProjectNames = downstreamProjectNames;
    }

    /**
     * Construct the trigger setting the project name and manual build promotion option
     *
     * @param downstreamProjectNames - the job name of the downstream build
     */
    @DataBoundConstructor
    public BuildPipelineTrigger(final String downstreamProjectNames) {
        super(downstreamProjectNames, Result.SUCCESS);
        if (downstreamProjectNames == null) {
            throw new IllegalArgumentException();
        }

        setDownstreamProjectNames(downstreamProjectNames);
    }

    /**
     * Set the descriptor for build pipeline trigger class This descriptor is only attached to Build Trigger Post Build action in JOB
     * configuration page
     *
     * @author Centrum Systems
     *
     */
    @Extension
    @SuppressWarnings("unchecked")
    public static class DescriptorImpl extends hudson.tasks.BuildTrigger.DescriptorImpl {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * set the display name in post build action section of the job configuration page
         *
         * @return display name
         */
        @Override
        public String getDisplayName() {
            return "Build Pipeline Plugin -> Manually Execute Downstream Project";
        }

        /**
         * Set help text to "Build Pipeline Plugin -> Manually Execute Downstream Project" Post Build action in JOB configuration page
         *
         * @return location of the help file
         */
        @Override
        public String getHelpFile() {
            return "/descriptor/au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger/help/buildPipeline.html";
        }

        /**
         * This method is called when downstream build pipeline is created
         *
         * @param req - stapler request
         * @param formData JSONObject
         * @return BuildTrigger build trigger
         * @throws FormException e
         */
        @Override
        public BuildTrigger newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
            final String downstreamProjectNames = formData.getString("downstreamProjectNames");
            String cleanProjectNames = downstreamProjectNames.replace("[", "");
            cleanProjectNames = cleanProjectNames.replace("]", "");
            cleanProjectNames = cleanProjectNames.replace("\"", "");

            return new BuildPipelineTrigger(cleanProjectNames);
        }

        /**
         * Validates that the downstream project names entered are valid projects.
         * @param value - The entered project names
         * @return hudson.util.FormValidation
         */
        public FormValidation doCheckDownstreamProjectNames(@QueryParameter("downstreamProjectNames") String value) {
            final StringTokenizer tokens = new StringTokenizer(Util.fixNull(value), ",");
            while (tokens.hasMoreTokens()) {
                final String projectName = tokens.nextToken().trim();
                final Item item = Hudson.getInstance().getItemByFullName(projectName, Item.class);
                if (item == null) {
                    return FormValidation.error(Messages.BuildTrigger_NoSuchProject(projectName, AbstractProject.findNearest(projectName)
                            .getName()));
                }
                if (!(item instanceof AbstractProject)) {
                    return FormValidation.error(Messages.BuildTrigger_NotBuildable(projectName));
                }
            }

            return FormValidation.ok();
        }
    }

    /**
     * this method is required to rebuild the dependency graph of the downstream project
     *
     * @param owner
     *            owner
     * @param graph
     *            graph
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void buildDependencyGraph(final AbstractProject owner, final DependencyGraph graph) {
        for (final AbstractProject<?, ?> downstream : getChildProjects()) {
            graph.addDependency(createDownstreamDependency(owner, downstream));
        }
    }

    /**
     * Create a new DownstreamDependency
     *
     * @param owner - upstream project
     * @param downstream - downstream project
     * @return downstream dependency
     */
    private DownstreamDependency createDownstreamDependency(final AbstractProject<?, ?> owner, final AbstractProject<?, ?> downstream) {
        return new DownstreamDependency(owner, downstream);
    }
}
