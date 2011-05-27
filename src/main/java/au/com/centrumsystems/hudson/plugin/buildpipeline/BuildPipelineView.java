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

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.model.ViewDescriptor;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause.UserCause;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.View;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import au.com.centrumsystems.hudson.plugin.util.ProjectUtil;

/**
 * This view displays the set of jobs that are related based on their upstream\downstream relationships as a pipeline. Each build pipeline
 * becomes a row on the view.
 * 
 * @author Centrum Systems
 * 
 */
public class BuildPipelineView extends View {

    /** selectedJob. */
    private String selectedJob;

    /** noOfDisplayedBuilds. */
    private String noOfDisplayedBuilds;

    /** buildViewTitle. */
    private String buildViewTitle = "";

    /** A Logger object is used to log messages */
    private static final Logger LOGGER = Logger.getLogger(BuildPipelineView.class.getName());
    /** Constant that represents the Stapler Request upstream build number. */
    private static final String REQ_UPSTREAM_BUILD_NUMBER = "upstreamBuildNumber";
    /** Constant that represents the Stapler Request trigger project name. */
    private static final String REQ_TRIGGER_PROJECT_NAME = "triggerProjectName";
    /** Constant that represents the Stapler Request upstream project name. */
    private static final String REQ_UPSTREAM_PROJECT_NAME = "upstreamProjectName";

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
        setBuildViewTitle(buildViewTitle);
        setSelectedJob(selectedJob);
        setNoOfDisplayedBuilds(noOfDisplayedBuilds);
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
        this.selectedJob = req.getParameter("selectedJob");
        this.noOfDisplayedBuilds = req.getParameter("noOfDisplayedBuilds");
        this.buildViewTitle = req.getParameter("buildViewTitle");
    }

    /**
     * Gets the selected project
     * 
     * @return - The selected project in the current view
     */
    public AbstractProject<?, ?> getSelectedProject() {
        AbstractProject<?, ?> selectedProject = null;
        if (getSelectedJob() != null) {
            selectedProject = (AbstractProject<?, ?>) super.getJob(getSelectedJob());
        }
        return selectedProject;
    }

    /**
     * Tests if the selected project exists.
     * 
     * @return - true: Selected project exists; false: Selected project does not exist.
     */
    public boolean hasSelectedProject() {
        boolean result = false;
        final AbstractProject<?, ?> testProject = getSelectedProject();
        if (testProject != null) {
            result = true;
        }
        return result;
    }

    /**
     * Checks whether the user has Build permission for the current project.
     * 
     * @param currentProject
     *            - The project being viewed.
     * @return - true: Has Build permission; false: Does not have Build permission
     * @see hudson.model.Item
     */
    public boolean hasBuildPermission(final AbstractProject<?, ?> currentProject) {
        return currentProject.hasPermission(Item.BUILD);
    }

    /**
     * Checks whether the user has Configure permission for the current project.
     * 
     * @return - true: Has Configure permission; false: Does not have Configure permission
     */
    public boolean hasConfigurePermission() {
        return this.hasPermission(CONFIGURE);
    }

    /**
     * Checks whether the user has Configure permission for the current project.
     * 
     * @return - true: Has Configure permission; false: Does not have Configure permission
     */
    public boolean hasBuildPermission() {
        return this.hasPermission(CONFIGURE);
    }

    /**
     * Get a List of downstream projects.
     * 
     * @param currentProject
     *            - The project from which we want the downstream projects
     * @return - A List of downstream projects
     */
    public List<AbstractProject<?, ?>> getDownstreamProjects(final AbstractProject<?, ?> currentProject) {
        return ProjectUtil.getDownstreamProjects(currentProject);
    }

    /**
     * Determines if the current project has any downstream projects
     * 
     * @param currentProject
     *            - The project from wwhich we are testing.
     * @return - true; has downstream projects; false: does not have downstream projects
     */
    public boolean hasDownstreamProjects(final AbstractProject<?, ?> currentProject) {
        return (getDownstreamProjects(currentProject).size() > 0);
    }

    /**
     * Returns the HTML containing the build pipeline to display.
     * 
     * @return A STring containing the HTML code for the project and build pipelines.
     * @throws URISyntaxException
     *             {@link URISyntaxException}
     */
    public BuildPipelineForm getBuildPipelineForm() throws URISyntaxException {
        final AbstractProject<?, ?> project = getSelectedProject();
        BuildPipelineForm buildPipelineForm = null;
        if (project != null) {
            final int maxNoOfDisplayBuilds = Integer.valueOf(noOfDisplayedBuilds);
            int rowsAppended = 0;
            final List<BuildForm> buildForms = new ArrayList<BuildForm>();
            for (final AbstractBuild<?, ?> currentBuild : project.getBuilds()) {
                buildForms.add(new BuildForm(new PipelineBuild(currentBuild, project, null)));
                rowsAppended++;
                if (rowsAppended >= maxNoOfDisplayBuilds) {
                    break;
                }
            }
            buildPipelineForm = new BuildPipelineForm(new ProjectForm(project), buildForms);
        }
        return buildPipelineForm;
    }

    /**
     * Retrieves the project URL
     * 
     * @param project
     *            - The project
     * @return URL - of the project
     * @throws URISyntaxException
     * @see {@link ProjectUtil#getProjectURL(AbstractProject)}
     * @throws URISyntaxException
     *             {@link URISyntaxException}
     */
    public String getProjectURL(final AbstractProject<?, ?> project) throws URISyntaxException {
        return project.getUrl();
    }

    /**
     * Invoke this method when the URL(/manualExecution/) is called
     * 
     * @param req
     *            - Stapler Request
     * @param rsp
     *            - Stapler Response
     */
    @SuppressWarnings("unchecked")
    public void doManualExecution(final StaplerRequest req, final StaplerResponse rsp) {
        final List<Action> buildActions = new ArrayList<Action>();

        int upstreamBuildNo;
        if (req.getParameter(REQ_UPSTREAM_BUILD_NUMBER) == null) {
            upstreamBuildNo = 0;
        } else {
            upstreamBuildNo = Integer.parseInt(req.getParameter(REQ_UPSTREAM_BUILD_NUMBER));
        }
        final AbstractProject<?, ?> triggerProject = (AbstractProject<?, ?>) super.getJob(req.getParameter(REQ_TRIGGER_PROJECT_NAME));
        final AbstractProject<?, ?> upstreamProject = (AbstractProject<?, ?>) super.getJob(req.getParameter(REQ_UPSTREAM_PROJECT_NAME));
        AbstractBuild<?, ?> upstreamBuild = null;

        for (final AbstractBuild<?, ?> tmpUpBuild : (List<AbstractBuild<?, ?>>) upstreamProject.getBuilds()) {
            if (tmpUpBuild.getNumber() == upstreamBuildNo) {
                upstreamBuild = tmpUpBuild;
                break;
            }
        }

        // Retrieve the List of Actions from the upstream build
        Action buildParametersAction = null;
        if (upstreamBuild != null) {
            // If a ParametersAction is found, save the parameters to pass to the downstream project
            for (Action nextAction : upstreamBuild.getActions()) {
                if (nextAction instanceof ParametersAction) {
                    buildParametersAction = nextAction;
                }
            }
        }
        
        final hudson.model.Cause.UpstreamCause upstreamCause = new hudson.model.Cause.UpstreamCause((Run<?, ?>) upstreamBuild);
        if (buildParametersAction == null) {
            triggerProject.scheduleBuild(triggerProject.getQuietPeriod(), upstreamCause, 
                    buildActions.toArray(new Action[buildActions.size()]));
        } else {
            triggerProject.scheduleBuild(triggerProject.getQuietPeriod(), upstreamCause, buildParametersAction);
        }

        // redirect to the view page.
        try {
            rsp.sendRedirect2(".");
        } catch (final IOException e) {
            LOGGER.info(e.toString());
        }
    }

    /**
     * Request to invoke a build of the current pipeline base project
     */
    public void invokeBuild() {
        final List<Action> buildActions = new ArrayList<Action>();
        final UserCause userInvokedCause = new UserCause();

        getSelectedProject().scheduleBuild(getSelectedProject().getQuietPeriod(), userInvokedCause,
            buildActions.toArray(new Action[buildActions.size()]));

    }

    /**
     * This descriptor class is required to configure the View Page
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

    @Override
    public Collection<TopLevelItem> getItems() {
        return Hudson.getInstance().getItems();
    }

    @Override
    public boolean contains(final TopLevelItem item) {
        return this.getItems().contains(item);
        // return false;
    }

    @Override
    public void onJobRenamed(final Item item, final String oldName, final String newName) {
    }

    @Override
    public Item doCreateItem(final StaplerRequest req, final StaplerResponse rsp) throws IOException, ServletException {
        return Hudson.getInstance().doCreateItem(req, rsp);
    }

}
