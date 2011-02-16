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
package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Descriptor.FormException;
import hudson.model.ViewDescriptor;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.AllView;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.Run;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import au.com.centrumsystems.hudson.plugin.buildpipeline.util.BPAbstractBuild;
import au.com.centrumsystems.hudson.plugin.buildpipeline.util.BPAbstractProject;
import au.com.centrumsystems.hudson.plugin.buildpipeline.util.HudsonResult;

/**
 * This view displays the set of jobs that are related based on their upstream\downstream relationships as a pipeline.  
 * Each build pipeline becomes a row on the view.
 * 
 * @author Centrum Systems
 * 
 */
public class BuildPipelineView extends AllView {
    
    /** selectedJob. */
    private String selectedJob;

    /** noOfDisplayedBuilds. */
    private String noOfDisplayedBuilds;

    /** buildViewTitle. */
    private String buildViewTitle = "";

    /** buildViewTitle. */
    private AbstractBuild<?, ?> lastBuildOfAPipeline;

    /** A Logger object is used to log messages */
    private static final Logger LOGGER = Logger.getLogger(BuildPipelineView.class.getName());

    /**
     * 
     * @param name
     *            the name of the pipeline build view.
     * @param buildViewTitle
     *            the build view title.
     * @param selectedJob
     *            the first job in the build pipeline.  
     * @param noOfDisplayedBuilds
     *            a count of the number of builds displayed on the view
     */
    @DataBoundConstructor
    public BuildPipelineView(final String name, final String buildViewTitle, final String selectedJob, final String noOfDisplayedBuilds) {
        super(name);
        this.buildViewTitle = buildViewTitle;
        this.selectedJob = selectedJob;
        this.noOfDisplayedBuilds = noOfDisplayedBuilds;
    }

    /**
     * Handles the configuration submission
     * 
     * @param req
     *            Stapler Request
     * @throws FormException
     *             Form Exception
     * @throws IOException
     *             IO Exception
     * @throws ServletException
     *             Servlet Exception
     */
    @Override
    protected void submit(final StaplerRequest req) throws IOException, ServletException, FormException {
        super.submit(req);
        this.selectedJob = req.getParameter("selectedJob");
        this.noOfDisplayedBuilds = req.getParameter("noOfDisplayedBuilds");
        this.buildViewTitle = req.getParameter("buildViewTitle");
    }

    /**
     * get the selected job
     * 
     * @return JOB
     */
    public Job<?, ?> getJob() {
        Job<?, ?> job = null;
        if (selectedJob != null) {
            job = (Job<?, ?>) super.getJob(selectedJob);
        }
        return job;
    }

    /**
     * Get Project Pipeline
     * 
     * @return list of project name
     */
    public List<AbstractProject<?, ?>> getProjectPipeline() {
        final List<AbstractProject<?, ?>> pipeline = new ArrayList<AbstractProject<?, ?>>();
        final AbstractProject<?, ?> project = (AbstractProject<?, ?>) getJob();
        if (project != null) {
            pipeline.add(project);
            pipeline.addAll(BPAbstractProject.getDownstreamProjectsList(project));
        }
        return pipeline;
    }

    /**
     * Check whether the next Build is required in a build pipeline
     * 
     * @return True : if the next build is require
     */
    public boolean isNextBuildRequire() {
        final List<AbstractProject<?, ?>> downProjects = BPAbstractProject.getDownstreamProjectsList(lastBuildOfAPipeline.getProject());
        return (!downProjects.isEmpty()) && (getHudsonResult(lastBuildOfAPipeline) == HudsonResult.SUCCESS);
    }

    /**
     * Get the downstream project. This assume that we only have one downstream project
     * 
     * @param currentBuild
     *            current build
     * @return abstract project
     */
    public AbstractProject<?, ?> getNextProject(final AbstractBuild<?, ?> currentBuild) {
        return BPAbstractProject.getDownstreamProjectsList(currentBuild.getProject()).get(0);
    }

    /**
     * Get the SVN revision no of a particular build
     * 
     * @param builds
     *            build
     * @return the revision number
     */
    @SuppressWarnings("deprecation")
    public String getSVNRefvisionNo(final List<AbstractBuild<?, ?>> builds) {
        String revNo = "noRevison";
        try {
            final EnvVars environmentVars = builds.get(0).getEnvironment();
            revNo = environmentVars.get("SVN_REVISION");
        } catch (final IOException e) {
            LOGGER.info(e.toString());
        } catch (final InterruptedException e) {
            LOGGER.info(e.toString());
        }
        return revNo;
    }

    /**
     *  Gets the collection of pipelines (rows in the view).  
     *  The number returned is dependant on the number configured to be displayed on that view.
     * 
     * @return List of build pipe lines
     */
    @SuppressWarnings("rawtypes")
    public List<List> getBuildsHistory() {
        final int tmpNoOfBuilds = (noOfDisplayedBuilds == null ? 0 : Integer.parseInt(noOfDisplayedBuilds));
        int rows = 0;
        final ArrayList<List> builds = new ArrayList<List>();
        final AbstractProject<?, ?> project = (AbstractProject<?, ?>) getJob();

        if (project.getLastBuild() != null) {
            AbstractBuild<?, ?> currentBuild = project.getLastBuild();
            while (rows < tmpNoOfBuilds) {
                if (currentBuild != null) {
                    builds.add(BPAbstractBuild.getProjectBuildPipeline(currentBuild));
                    if (currentBuild.getPreviousBuild() != null) {
                        currentBuild = currentBuild.getPreviousBuild();
                    }
                }
                rows++;
            }
        }
        return builds;
    }

    /**
     * Get the hudson result
     * 
     * @param build
     *            build
     * @return HudsonResult HudsonResult
     */
    public HudsonResult getHudsonResult(final AbstractBuild<?, ?> build) {
        HudsonResult hudsonResult;
        if (build.isBuilding()) {
            hudsonResult = HudsonResult.BUILDING;
        } else {
            hudsonResult = HudsonResult.values()[build.getResult().ordinal];
        }
        return hudsonResult;
    }

    /**
     * Build a URL of a particular build
     * 
     * @param build
     *            the build
     * @return URL of the build
     */
    public String getBuildResultURL(final AbstractBuild<?, ?> build) {
        return BPAbstractBuild.getBuildResultURL(build);
    }

    /**
     * Invoke this method when the URL(/manualExecution/) is called
     * 
     * @param req
     *            Stapler Request
     * @param rsp
     *            Stapler Response
     */
    @SuppressWarnings("unchecked")
    public void doManualExecution(final StaplerRequest req, final StaplerResponse rsp) {
        final List<Action> buildActions = new ArrayList<Action>();

        int upstreamBuildNo;
        if (req.getParameter("upstreamBuildNumber") == null) {
            upstreamBuildNo = 0;
        } else {
            upstreamBuildNo = Integer.parseInt(req.getParameter("upstreamBuildNumber"));
        }
        final AbstractProject<?, ?> triggerProject = (AbstractProject<?, ?>) super.getJob(req.getParameter("triggerProjectName"));
        final AbstractProject<?, ?> upstreamProject = (AbstractProject<?, ?>) super.getJob(req.getParameter("upstreamProjectName"));
        AbstractBuild<?, ?> upstreamBuild = null;

        for (final AbstractBuild<?, ?> tmpUpBuild : (List<AbstractBuild<?, ?>>) upstreamProject.getBuilds()) {
            if (tmpUpBuild.getNumber() == upstreamBuildNo) {
                upstreamBuild = tmpUpBuild;
                break;
            }
        }

        final hudson.model.Cause.UpstreamCause upstreamCause = new hudson.model.Cause.UpstreamCause((Run<?, ?>) upstreamBuild);
        triggerProject.scheduleBuild(triggerProject.getQuietPeriod(), upstreamCause, buildActions.toArray(new Action[buildActions.size()]));

        // redirect to the view page.
        try {
            rsp.sendRedirect2(".");
        } catch (IOException e) {
            LOGGER.info(e.toString());
        }
    }

    /**
     * This descriptor class is required to configure the View Page
     * 
     * @author rayc
     * 
     */
    @Extension
    public static final class DescriptorImpl extends ViewDescriptor {

        /**
         * descriptor impl constructor This empty constructor is required for stapler. If you remove this constructor, text name of
         * "Build Pipeline View" will be not displayed in the "NewView" page
         */
        public DescriptorImpl() {
            super();
        }

        /**
         * get the display name
         * 
         * @return display name
         */
        @Override
        public String getDisplayName() {
            return "Build Pipeline View";
        }

        /**
         * Display Job List Item in the Edit View Page
         * 
         * @return ListBoxModel
         */
        public hudson.util.ListBoxModel doFillSelectedJobItems() {
            final hudson.util.ListBoxModel options = new hudson.util.ListBoxModel();
            for (final String jobName : Hudson.getInstance().getJobNames()) {
                options.add(jobName);
            }
            return options;
        }

        /**
         * Display No Of Builds Items in the Edit View Page
         * 
         * @return ListBoxModel
         */
        public hudson.util.ListBoxModel doFillNoOfDisplayedBuildsItems() {
            final hudson.util.ListBoxModel options = new hudson.util.ListBoxModel();
            final List<String> noOfBuilds = new ArrayList<String>();
            noOfBuilds.add("1");
            noOfBuilds.add("2");
            noOfBuilds.add("3");
            noOfBuilds.add("5");
            noOfBuilds.add("10");
            noOfBuilds.add("20");
            noOfBuilds.add("50");
            noOfBuilds.add("100");
            noOfBuilds.add("200");
            noOfBuilds.add("500");

            for (final String noOfBuild : noOfBuilds) {
                options.add(noOfBuild);
            }
            return options;
        }
    }

    /* -- Setter and Getter -- */
    public AbstractBuild<?, ?> getLastBuildOfAPipeline() {
        return lastBuildOfAPipeline;
    }

    public void setLastBuildOfAPipeline(final AbstractBuild<?, ?> lastBuildOfAPipeline) {
        this.lastBuildOfAPipeline = lastBuildOfAPipeline;
    }

    public String getBuildViewTitle() {
        return buildViewTitle;
    }

    public void setBuildViewTitle(final String buildViewTitle) {
        this.buildViewTitle = buildViewTitle;
    }

    public String getNoOfDisplayedBuilds() {
        return noOfDisplayedBuilds;
    }

    public void setNoOfDisplayedBuilds(final String noOfDisplayedBuilds) {
        this.noOfDisplayedBuilds = noOfDisplayedBuilds;
    }

    public String getSelectedJob() {
        return selectedJob;
    }

    public void setSelectedJob(final String selectedJob) {
        this.selectedJob = selectedJob;
    }

}
