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
import hudson.model.TaskListener;
import hudson.model.TopLevelItem;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.model.Run;
import hudson.model.User;
import hudson.model.View;
import hudson.model.ViewDescriptor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import au.com.centrumsystems.hudson.plugin.util.BuildUtil;
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
	private String buildViewTitle = ""; //$NON-NLS-1$

	/** Indicates whether only the latest job will be triggered. **/
	private boolean triggerOnlyLatestJob;

	/** alwaysAllowManualTrigger. */
	private boolean alwaysAllowManualTrigger;

	/** showPipelineParameters. */
	private boolean showPipelineParameters;

	/*
	 * Keep feature flag properties in one place so that it is easy to refactor them out later.
	 */
	/* Feature flags - START */

	/** Indicates whether the progress bar should be displayed */
	private boolean displayProgressBar;

	/* Feature flags - END */

	/** A Logger object is used to log messages */
	private static final Logger LOGGER = Logger.getLogger(BuildPipelineView.class.getName());
	/** Constant that represents the Stapler Request upstream build number. */
	private static final String REQ_UPSTREAM_BUILD_NUMBER = "upstreamBuildNumber"; //$NON-NLS-1$
	/** Constant that represents the Stapler Request trigger project name. */
	private static final String REQ_TRIGGER_PROJECT_NAME = "triggerProjectName"; //$NON-NLS-1$
	/** Constant that represents the Stapler Request upstream project name. */
	private static final String REQ_UPSTREAM_PROJECT_NAME = "upstreamProjectName"; //$NON-NLS-1$

	/**
	 * An instance of {@link Cause.UserIdCause} related to the current user. Must be transient, or xstream will include it in the
	 * serialization
	 */
	private class MyUserIdCause extends Cause.UserIdCause {
		private User user = null;

		public MyUserIdCause() {
			try {
				// this block can generate a CyclicGraphDetector.CycleDetectedException
				// in cases that I haven't quite figured out yet
				// also an org.acegisecurity.AccessDeniedException when the user
				// is not logged in
				user = Hudson.getInstance().getMe();
			} catch (final Exception e) {
				// do nothing
				LOGGER.fine(e.getMessage());
			}
		}

		@Override
		public String getUserId() {
			return (null == user) ? null : user.getId();
		}

		@Override
		public String getUserName() {
			return (null == user) ? null : user.getDisplayName();
		}

		@Override
		public String toString() {
			return getUserName();
		}

		@Override
		public int hashCode() {
			if (getUserId() == null) {
				return super.hashCode();
			} else {
				return getUserId().hashCode();
			}
		}

		@Override
		public boolean equals(Object o) {
			if (null == o) {
				return false;
			}
			if (!(o instanceof Cause.UserIdCause)) {
				return false;
			}

			return hashCode() == o.hashCode();
		}

		@Override
		public void print(TaskListener listener) {
			// do nothing
		}
	}

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
	 * @param triggerOnlyLatestJob
	 *            Indicates whether only the latest job will be triggered.
	 */
	@DataBoundConstructor
	public BuildPipelineView(final String name, final String buildViewTitle, final String selectedJob, final String noOfDisplayedBuilds,
			final boolean triggerOnlyLatestJob) {
		super(name, Hudson.getInstance());
		setBuildViewTitle(buildViewTitle);
		setSelectedJob(selectedJob);
		setNoOfDisplayedBuilds(noOfDisplayedBuilds);
		setTriggerOnlyLatestJob(triggerOnlyLatestJob);
	}

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
	 * @param triggerOnlyLatestJob
	 *            Indicates whether only the latest job will be triggered.
	 * @param alwaysAllowManualTrigger
	 *            Indicates whether manual trigger will always be available.
	 * @param showPipelineParameters
	 *            Indicates whether pipeline parameter values should be shown.
	 */
	@DataBoundConstructor
	public BuildPipelineView(final String name, final String buildViewTitle, final String selectedJob, final String noOfDisplayedBuilds,
			final boolean triggerOnlyLatestJob, final boolean alwaysAllowManualTrigger, final boolean showPipelineParameters) {
		this(name, buildViewTitle, selectedJob, noOfDisplayedBuilds, triggerOnlyLatestJob);
		setAlwaysAllowManualTrigger(alwaysAllowManualTrigger);
		setShowPipelineParameters(showPipelineParameters);
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
		this.selectedJob = req.getParameter("selectedJob"); //$NON-NLS-1$
		this.noOfDisplayedBuilds = req.getParameter("noOfDisplayedBuilds"); //$NON-NLS-1$
		this.buildViewTitle = req.getParameter("buildViewTitle"); //$NON-NLS-1$
		this.triggerOnlyLatestJob = Boolean.valueOf(req.getParameter("_.triggerOnlyLatestJob")); //$NON-NLS-1$
		this.alwaysAllowManualTrigger = Boolean.valueOf(req.getParameter("_.alwaysAllowManualTrigger")); //$NON-NLS-1$
		this.showPipelineParameters = Boolean.valueOf(req.getParameter("_.showPipelineParameters")); //$NON-NLS-1$
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
	 *            - The project from which we are testing.
	 * @return - true; has downstream projects; false: does not have downstream projects
	 */
	public boolean hasDownstreamProjects(final AbstractProject<?, ?> currentProject) {
		return (getDownstreamProjects(currentProject).size() > 0);
	}

	/**
	 * Returns BuildPipelineForm containing the build pipeline to display.
	 * 
	 * @return - Representation of the projects and their related builds making up the build pipeline view
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
	 * Trigger a manual build
	 * 
	 * @param upstreamBuildNumber
	 *            upstream build number
	 * @param triggerProjectName
	 *            project that is triggered
	 * @param upstreamProjectName
	 *            upstream project
	 * @return next build number that has been scheduled
	 */
	@JavaScriptMethod
	public int triggerManualBuild(final Integer upstreamBuildNumber, final String triggerProjectName, final String upstreamProjectName) {
		final AbstractProject<?, ?> triggerProject = (AbstractProject<?, ?>) super.getJob(triggerProjectName);
		final AbstractProject<?, ?> upstreamProject = (AbstractProject<?, ?>) super.getJob(upstreamProjectName);

		final AbstractBuild<?, ?> upstreamBuild = retrieveBuild(upstreamBuildNumber, upstreamProject);

		// Get parameters from upstream build
		Action buildParametersAction = null;
		if (upstreamBuild != null) {
			buildParametersAction = BuildUtil.getAllBuildParametersAction(upstreamBuild, triggerProject);
		}

		return triggerBuild(triggerProject, upstreamBuild, buildParametersAction);
	}

	@JavaScriptMethod
	public int rerunSuccessfulBuild(final String externalizableId) {
		final AbstractBuild<?, ?> triggerBuild = (AbstractBuild<?, ?>) Run.fromExternalizableId(externalizableId);
		final AbstractProject<?, ?> triggerProject = (AbstractProject<?, ?>) triggerBuild.getProject();
		final Future<?> future = triggerProject.scheduleBuild2(triggerProject.getQuietPeriod(), new MyUserIdCause(),
				triggerBuild.getActions());

		AbstractBuild<?, ?> result = triggerBuild;
		try {
			result = (AbstractBuild<?, ?>) future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return result.getNumber();
	}

	/**
	 * Given an AbstractProject and a build number the associated AbstractBuild will be retrieved.
	 * 
	 * @param buildNo
	 *            - Build number
	 * @param project
	 *            - AbstractProject
	 * @return The AbstractBuild associated with the AbstractProject and build number.
	 */
	private AbstractBuild<?, ?> retrieveBuild(final int buildNo, final AbstractProject<?, ?> project) {
		AbstractBuild<?, ?> build = null;

		if (project != null) {
			for (final AbstractBuild<?, ?> tmpUpBuild : (List<AbstractBuild<?, ?>>) project.getBuilds()) {
				if (tmpUpBuild.getNumber() == buildNo) {
					build = tmpUpBuild;
					break;
				}
			}
		}

		return build;
	}

	/**
	 * Schedules a build to start.
	 * 
	 * The build will take an upstream build as its Cause and a set of ParametersAction from the upstream build.
	 * 
	 * @param triggerProject
	 *            - Schedule a build to start on this AbstractProject
	 * @param upstreamBuild
	 *            - The upstream AbstractBuild that will be used as a Cause for the triggerProject's build.
	 * @param buildParametersAction
	 *            - The upstream ParametersAction that will be used as an Action for the triggerProject's build.
	 * @return next build number
	 */
	private int triggerBuild(final AbstractProject<?, ?> triggerProject, final AbstractBuild<?, ?> upstreamBuild,
			final Action buildParametersAction) {
		final Cause.UpstreamCause upstreamCause = (null == upstreamBuild) ? null : new hudson.model.Cause.UpstreamCause(
				(Run<?, ?>) upstreamBuild);
		final List<Action> buildActions = new ArrayList<Action>();
		buildActions.add(new CauseAction(new MyUserIdCause()));

		if (buildParametersAction != null) {
			buildActions.add(buildParametersAction);
		}

		triggerProject.scheduleBuild(triggerProject.getQuietPeriod(), upstreamCause, buildActions.toArray(new Action[buildActions.size()]));
		return triggerProject.getNextBuildNumber();
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
			return Strings.getString("BuildPipelineView.DisplayText"); //$NON-NLS-1$
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
			noOfBuilds.add("1"); //$NON-NLS-1$
			noOfBuilds.add("2"); //$NON-NLS-1$
			noOfBuilds.add("3"); //$NON-NLS-1$
			noOfBuilds.add("5"); //$NON-NLS-1$
			noOfBuilds.add("10"); //$NON-NLS-1$
			noOfBuilds.add("20"); //$NON-NLS-1$
			noOfBuilds.add("50"); //$NON-NLS-1$
			noOfBuilds.add("100"); //$NON-NLS-1$
			noOfBuilds.add("200"); //$NON-NLS-1$
			noOfBuilds.add("500"); //$NON-NLS-1$

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

	public boolean isTriggerOnlyLatestJob() {
		return triggerOnlyLatestJob;
	}

	public String getTriggerOnlyLatestJob() {
		return Boolean.toString(triggerOnlyLatestJob);
	}

	public void setTriggerOnlyLatestJob(final boolean triggerOnlyLatestJob) {
		this.triggerOnlyLatestJob = triggerOnlyLatestJob;
	}

	public boolean isAlwaysAllowManualTrigger() {
		return alwaysAllowManualTrigger;
	}

	public String getAlwaysAllowManualTrigger() {
		return Boolean.toString(alwaysAllowManualTrigger);
	}

	public void setAlwaysAllowManualTrigger(final boolean alwaysAllowManualTrigger) {
		this.alwaysAllowManualTrigger = alwaysAllowManualTrigger;
	}

	public boolean isShowPipelineParameters() {
		return showPipelineParameters;
	}

	public String getShowPipelineParameters() {
		return Boolean.toString(showPipelineParameters);
	}

	public void setShowPipelineParameters(final boolean showPipelineParameters) {
		this.showPipelineParameters = showPipelineParameters;
	}

	@Override
	public Collection<TopLevelItem> getItems() {
		return Hudson.getInstance().getItems();
	}

	@Override
	public boolean contains(final TopLevelItem item) {
		return this.getItems().contains(item);
	}

	/**
	 * If a project name is changed we check if the selected job for this view also needs to be changed.
	 * 
	 * @param item
	 *            - The Item that has been renamed
	 * @param oldName
	 *            - The old name of the Item
	 * @param newName
	 *            - The new name of the Item
	 * 
	 */
	@Override
	public void onJobRenamed(final Item item, final String oldName, final String newName) {
		if (item instanceof AbstractProject) {
			if ((oldName != null) && (oldName.equals(this.selectedJob))) {
				setSelectedJob(newName);
			}
		}
	}

	@Override
	public Item doCreateItem(final StaplerRequest req, final StaplerResponse rsp) throws IOException, ServletException {
		return Hudson.getInstance().doCreateItem(req, rsp);
	}

}
