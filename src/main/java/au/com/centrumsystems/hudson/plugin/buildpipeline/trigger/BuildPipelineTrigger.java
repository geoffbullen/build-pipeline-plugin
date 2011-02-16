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
import hudson.model.DependecyDeclarer;
import hudson.model.DependencyGraph;
import hudson.model.Result;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.tasks.BuildTrigger;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * The build pipeline trigger allows the creation of downstream jobs which aren't triggered automatically.  
 * This allows us to have manual "approval" steps in the process where jobs are manually promoted along the pipeline 
 * by a user pressing a button on the view.
 * 
 * @author Centrum Systems
 * 
 */
@SuppressWarnings("unchecked")
public class BuildPipelineTrigger extends BuildTrigger implements DependecyDeclarer {

    /** is manual build promotion required */
    private transient boolean isManualBuild;
    
    /** downstream project name */
    private String downstreamProjectName;
    
    /** 
     *  getIsManualBuild method is required to retrieve the value of isManualBuild from Hudson stapler framework
     *  Otherwise, Hudson will not be able to set the value of "Require manual build execution" in the build trigger post build actions
     *  
     *  @return is manual build flag
     */
    public boolean getIsManualBuild() {
        return isManualBuild;
    }

    public void setManualBuild(final boolean isManualBuild) {
        this.isManualBuild = isManualBuild;
    }

    public String getDownstreamProjectName() {
        return downstreamProjectName;
    }

    public void setDownstreamProjectName(final String downstreamProjectName) {
        this.downstreamProjectName = downstreamProjectName;
    }

    /**
     * Construct the trigger setting the project name and manual build promotion option
     * 
     * @param downstreamProjectName the job name of the downstream build 
     * @param isManualBuild flag indicating whether it is a manual trigger
     */
    @DataBoundConstructor
    public BuildPipelineTrigger(final String downstreamProjectName, final boolean isManualBuild) {
        super(downstreamProjectName, Result.SUCCESS);
        if (downstreamProjectName == null) {
            throw new IllegalArgumentException();
        }
        this.downstreamProjectName = downstreamProjectName;
        this.isManualBuild = isManualBuild;
    }

    /**
     * Set the descriptor for build pipeline trigger class This descriptor is only attached to Build Trigger Post Build action in JOB
     * configuration page
     * 
     * @author Centrum Systems
     * 
     */
    @Extension
    public static class DescriptorImpl extends hudson.tasks.BuildTrigger.DescriptorImpl {

        /**
         * set the display name in post build action section of the job configuration page
         * @return display name
         */
        @Override
        public String getDisplayName() {
            return "Build Pipeline Plugin -> Specify Downstream Project";
        }

        /**
         * Set help text to "Build Pipeline Plugin -> Specify Downstream Project" Post Build action in JOB configuration page
         * @return location of the help file
         */
        @Override
        public String getHelpFile() {
            return "/descriptor/au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger/help/buildPipeline.html";
        }

        /**
         * Display Job List Item in the Edit Job Configuration Page
         * 
         * @return ListBoxModel
         */
        public hudson.util.ListBoxModel doFillDownstreamProjectNameItems() {
            final hudson.util.ListBoxModel options = new hudson.util.ListBoxModel();
            for (final String jobName : Hudson.getInstance().getJobNames()) {
                options.add(jobName);
            }
            return options;
        }

        /**
         * This method is called when downstream build pipeline is created
         * 
         * @param req stapler request
         * @param formData JSONObject
         * @throws FormException form exception
         * @return BuildTrigger build trigger
         */
        @Override
        public BuildTrigger newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
            return new BuildPipelineTrigger(formData.getString("downstreamProjectName"), formData.getBoolean("isManualBuild"));
        }
    }

    /**
     * this method is required to rebuild the dependency graph of the downstream project
     * @param owner owner
     * @param graph graph
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void buildDependencyGraph(final AbstractProject owner, final DependencyGraph graph) {
        for (final AbstractProject<?, ?> downstream : getChildProjects()) {
            graph.addDependency(createDownstremDependency(owner, downstream));
        }
    }
    
    /**
     * Create a downstream dependency
     * @param owner upstream project
     * @param downstream downstream project
     * @return downstream dependency
     */
    private DownstreamDependency createDownstremDependency(final AbstractProject<?, ?> owner, final AbstractProject<?, ?> downstream) {
        return new DownstreamDependency(owner, downstream, !isManualBuild);
    }
}

