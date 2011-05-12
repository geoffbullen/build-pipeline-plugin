/*
 * The MIT License
 *
 * Copyright (c) 2011, Centrumsystems Pty Ltd
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

import hudson.model.Item;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;

import au.com.centrumsystems.hudson.plugin.util.BuildUtil;
import au.com.centrumsystems.hudson.plugin.util.HudsonResult;
import au.com.centrumsystems.hudson.plugin.util.ProjectUtil;

/**
 * @author KevinV
 * 
 */
public class PipelineBuild {
    /** Represents the current build */
    private AbstractBuild<?, ?> currentBuild;
    /** Represents the current project */
    private AbstractProject<?, ?> project;
    /** Represents the upstream build */
    private AbstractBuild<?, ?> upstreamBuild;
    /**
     * Contains the upstreamBuild result. Can be one of the following: - BUILDING - SUCCESS - FAILURE - UNSTABLE - NOT_BUILT - ABORT -
     * PENDING - MANUAL
     */
    private String upstreamBuildResult;
    /**
     * Contains the currentBuild result. Can be one of the following: - BUILDING - SUCCESS - FAILURE - UNSTABLE - NOT_BUILT - ABORT -
     * PENDING - MANUAL
     */
    private String currentBuildResult;

    /** A Logger object is used to log messages */
    private static final Logger LOGGER = Logger.getLogger(PipelineBuild.class.getName());

    /**
     * Default constructor
     */
    public PipelineBuild() {
    }

    /**
     * Creates a new PipelineBuild with currentBuild, project and upstreamBuild set.
     * 
     * @param build
     *            - current build
     * @param project
     *            - current project
     * @param previousBuild
     *            - upstream build
     */
    public PipelineBuild(final AbstractBuild<?, ?> build, final AbstractProject<?, ?> project, final AbstractBuild<?, ?> previousBuild) {
        this.currentBuild = build;
        this.project = project;
        this.upstreamBuild = previousBuild;
        this.currentBuildResult = "";
        this.upstreamBuildResult = "";
    }

    /**
     * @param project
     *            project
     */
    public PipelineBuild(final FreeStyleProject project) {
        this(null, project, null);
    }

    public AbstractBuild<?, ?> getCurrentBuild() {
        return currentBuild;
    }

    public void setCurrentBuild(final AbstractBuild<?, ?> currentBuild) {
        this.currentBuild = currentBuild;
    }

    public AbstractBuild<?, ?> getUpstreamBuild() {
        return upstreamBuild;
    }

    public void setUpstreamBuild(final AbstractBuild<?, ?> upstreamBuild) {
        this.upstreamBuild = upstreamBuild;
    }

    public void setProject(final AbstractProject<?, ?> currentProject) {
        this.project = currentProject;
    }

    /**
     * Returns the project name. If the current project is null the project name is determined using the current build.
     * 
     * @return - Project name
     */
    public AbstractProject<?, ?> getProject() {
        final AbstractProject<?, ?> currentProject;
        if (this.project == null && this.currentBuild != null) {
            currentProject = this.currentBuild.getProject();
        } else {
            currentProject = this.project;
        }
        return currentProject;
    }

    /**
     * Returns the current build number.
     * 
     * @return - Current build number or empty String is the current build is null.
     */
    private String getCurrentBuildNumber() {
        if (this.currentBuild != null) {
            return Integer.toString(currentBuild.getNumber());
        } else {
            return "";
        }
    }

    /**
     * Constructs a List of downstream PipelineBuild objects that make up the current pipeline.
     * 
     * @return - List of downstream PipelineBuild objects that make up the current pipeline.
     */
    public List<PipelineBuild> getDownstreamPipeline() {
        final List<PipelineBuild> pbList = new ArrayList<PipelineBuild>();

        final AbstractProject<?, ?> currentProject;
        currentProject = getProject();

        final List<AbstractProject<?, ?>> downstreamProjects = ProjectUtil.getDownstreamProjects(currentProject);
        for (final AbstractProject<?, ?> proj : downstreamProjects) {
            AbstractBuild<?, ?> returnedBuild = null;
            if (this.currentBuild != null) {
                returnedBuild = BuildUtil.getDownstreamBuild(proj, currentBuild);
            }
            final PipelineBuild newPB = new PipelineBuild(returnedBuild, proj, this.currentBuild);
            pbList.add(newPB);
        }

        return pbList;
    }

    /**
     * Build a URL of the currentBuild
     * 
     * @return URL of the currentBuild
     */
    public String getBuildResultURL() {
        return currentBuild != null ? currentBuild.getUrl() : "";
    }

    /**
     * Builds a URL of the current project
     * 
     * @return URL - of the project
     */
    public String getProjectURL() {
        return project != null ? project.getUrl() : "";
    }

    /**
     * Determines the result of the current build.
     * 
     * @return - String representing the build result
     * @see PipelineBuild#getBuildResult(AbstractBuild)
     */
    public String getCurrentBuildResult() {
        if (this.currentBuildResult.isEmpty()) {
            this.currentBuildResult = getBuildResult(this.currentBuild);
        }
        return this.currentBuildResult;
    }

    /**
     * Determines the result of the upstream build.
     * 
     * @return - String representing the build result
     * @see PipelineBuild#getBuildResult(AbstractBuild)
     */
    public String getUpstreamBuildResult() {
        if (this.upstreamBuildResult.isEmpty()) {
            this.upstreamBuildResult = getBuildResult(this.upstreamBuild);
        }
        return this.upstreamBuildResult;
    }

    /**
     * Determines the result for a particular build. Can be one of the following: - BUILDING - SUCCESS - FAILURE - UNSTABLE - NOT_BUILT -
     * ABORT - PENDING - MANUAL
     * 
     * @param build
     *            - The build for which a result is requested.
     * @return - String representing the build result
     */
    private String getBuildResult(final AbstractBuild<?, ?> build) {
        String buildResult;
        // If AbstractBuild exists determine its current status
        if (build != null) {
            if (build.isBuilding()) {
                buildResult = HudsonResult.BUILDING.toString();
            } else {
                buildResult = HudsonResult.values()[build.getResult().ordinal].toString();
            }
        } else {
            // Otherwise determine its pending status
            buildResult = getPendingStatus();
        }

        return buildResult;
    }

    /**
     * Determines the pending currentBuild status of a currentBuild in the pipeline that has not been completed. (i.e. the currentBuild is
     * null)
     * 
     * @return - PENDING: Current currentBuild is pending the execution of upstream builds. MANUAL: Current currentBuild requires a manual
     *         trigger
     */
    private String getPendingStatus() {
        String pendingStatus = HudsonResult.PENDING.toString();
        final PipelineBuild upstreamPB = getUpstreamPipelineBuild();

        if (upstreamPB != null) {
            if (this.getUpstreamBuild() != null) {
                if (getUpstreamBuildResult().equals(HudsonResult.SUCCESS.toString())) {
                    if (ProjectUtil.isManualTrigger(this.upstreamBuild.getProject(), this.project)) {
                        pendingStatus = HudsonResult.MANUAL.toString();
                    }
                }
            }
        }
        return pendingStatus;
    }

    /**
     * Returns the upstream PipelineBuild object from the current PipelineBuild object.
     * 
     * @return - Upstream PipelineBuild object from the current PipelineBuild object
     */
    public PipelineBuild getUpstreamPipelineBuild() {
        final List<AbstractProject> upstreamProjects = getProject().getUpstreamProjects();
        final AbstractProject<?, ?> previousProject;
        final PipelineBuild previousPB = new PipelineBuild();
        if (upstreamProjects.size() > 0) {
            previousProject = upstreamProjects.get(0);
            previousPB.setCurrentBuild(this.getUpstreamBuild());
            previousPB.setProject(previousProject);
        }
        return previousPB;
    }

    /**
     * Returns the current build duration.
     * 
     * @return - Current build duration or an empty String if the current build is null.
     */
    public String getBuildDuration() {
        if (this.currentBuild != null) {
            return this.currentBuild.getDurationString();
        } else {
            return "";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Project: " + getProject().getName() + " : Build: " + getCurrentBuildNumber();
    }

    /**
     * Returns the current build description.
     * 
     * @return - Current build description or the project name if the current build is null.
     */
    public String getBuildDescription() {
        if (this.currentBuild != null) {
            return this.currentBuild.toString();
        } else {
            return "Pending build of project: " + this.getProject().getName();
        }
    }

    /**
     * Get the SCM revision no of a particular currentBuild
     * 
     * @return The revision number of the currentBuild or "No Revision"
     */
    public String getScmRevision() {
        String revNo = "No Revision";
        try {
            if (currentBuild != null) {
                if ("hudson.scm.SubversionSCM".equals(project.getScm().getType())) {
                    final String svnNo = svnNo();
                    if (svnNo != null) {
                        revNo = svnNo;
                    }
                } else if ("hudson.plugins.git.GitSCM".equals(project.getScm().getType())) {
                    final String gitNo = gitNo();
                    if (gitNo != null) {
                        revNo = gitNo;
                    }
                } else if ("hudson.plugins.mercurial.MercurialSCM".equals(project.getScm().getType())) {
                    final String hgNo = hgNo();
                    if (hgNo != null) {
                        revNo = hgNo;
                    }
                }
            }
        } catch (final Exception e) {
            // if anything goes wrong, ignore and don't show a revision number, no need to bring the whole view down if we cannot see a
            // revision
            LOGGER.warning(e.toString());
        }
        return revNo;
    }

    /**
     * Returns the last hg rev
     * 
     * @return revision number of the current build
     */
    private String hgNo() throws MalformedURLException, IOException {
        InputStream inputStream = null;
        try {
            String revNo = null;
            final URL url = new URL(Hudson.getInstance().getRootUrl() + currentBuild.getUrl() + "api/json?tree=changeSet[items[node,rev]]");
            inputStream = url.openStream();
            final JSONObject json = (JSONObject) JSONSerializer.toJSON(IOUtils.toString(inputStream));
            if (json != null) {
                try {

                    final JSONArray items = json.getJSONObject("changeSet").getJSONArray("items");
                    if (items != null && items.size() >= 1) {
                        revNo = "Hg: " + items.getJSONObject(0).getString("rev") + ":" + items.getJSONObject(0).getString("node");
                    }
                } catch (final JSONException e) {
                    // This is not great, but swallow jquery parsing exceptions assuming that some element did not exist, will have a better
                    // solution one I find a JSON query lib or method
                    LOGGER.finest("did not find svn revision in " + json);
                }
            }
            return revNo;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Get the Git revision no of a particular currentBuild
     * 
     * @return The revision number of the currentBuild
     */
    private String gitNo() throws MalformedURLException, IOException {
        InputStream inputStream = null;
        try {
            String revNo = null;
            final URL url = new URL(Hudson.getInstance().getRootUrl() + currentBuild.getUrl()
                + "api/json?tree=actions[lastBuiltRevision[SHA1]]");
            inputStream = url.openStream();
            final JSONObject json = (JSONObject) JSONSerializer.toJSON(IOUtils.toString(inputStream));
            if (json != null) {
                try {
                    final JSONArray actions = json.getJSONArray("actions");
                    for (final Object object : actions) {
                        if (object instanceof JSONObject) {
                            final JSONObject jsonObject = (JSONObject) object;
                            if (jsonObject.containsKey("lastBuiltRevision")) {
                                revNo = "Git: " + jsonObject.getJSONObject("lastBuiltRevision").getString("SHA1");
                            }
                        }
                    }

                } catch (final JSONException e) {
                    // This is not great, but swallow jquery parsing exceptions assuming that some element did not exist, will have a better
                    // solution one I find a JSON query lib or method
                    LOGGER.finest("did not find git revision in " + json);
                }
            }
            return revNo;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Using the hudson api get the subversion info (yes, it is going the long way around)
     * 
     * @return subversion revision
     */
    private String svnNo() throws MalformedURLException, IOException {
        InputStream inputStream = null;
        try {
            String revNo = null;
            final URL url = new URL(Hudson.getInstance().getRootUrl() + currentBuild.getUrl()
                + "api/json?tree=changeSet[revisions[revision]]");
            inputStream = url.openStream();
            final JSONObject json = (JSONObject) JSONSerializer.toJSON(IOUtils.toString(inputStream));
            if (json != null) {
                try {
                    revNo = "Svn: " + json.getJSONObject("changeSet").getJSONArray("revisions").getJSONObject(0).getString("revision");
                } catch (final JSONException e) {
                    // This is not great, but swallow jquery parsing exceptions assuming that some element did not exist, will have a better
                    // solution one I find a JSON query lib or method
                    LOGGER.finest("did not find svn revision in " + json);
                }
            }
            return revNo;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Checks whether the user has Build permission for the current project.
     * 
     * @return - true: Has Build permission; false: Does not have Build permission
     * @see hudson.model.Item
     */
    public boolean hasBuildPermission() {
        boolean buildPermission = false;
        // If no security is enabled then allow builds
        if (!Hudson.getInstance().isUseSecurity()) {
            buildPermission = true;
        } else if (this.project != null) {
            // If security is enabled check if BUILD is enabled
            buildPermission = this.project.hasPermission(Item.BUILD);
        }
        return buildPermission;
    }

    public boolean isManual() {
        return getCurrentBuildResult().equals(HudsonResult.MANUAL.toString());
    }

    /**
     * Start time of build
     * 
     * @return start time
     */
    public Date getStartTime() {
        return currentBuild != null ? currentBuild.getTime() : null;

    }

}
