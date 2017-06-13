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

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import au.com.centrumsystems.hudson.plugin.util.BuildUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractBuild;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Descriptor;
import hudson.model.ItemGroup;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.plugins.parameterizedtrigger.BuildTrigger;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import hudson.util.LogTaskListener;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 *   This class is an extension point for a plugin to provide their own behavior for the 'build cards'
 *   that show up in the build pipeline plugin.
 * </p>
 *
 * <p>
 *   This base class encapsulates the logic for how builds can be re-run and how the upstream build is
 *   found, allowing subclasses to override this behavior.
 * </p>
 *
 * <p>
 *   In addition, this class also defines the look-and-feel of the build cards so that they can be overridden.
 *   These are defined in the following .jelly files:
 * </p>
 *
 * <ul>
 *    <li>buildCardTemplate.jelly</li>
 *    <li>buildCardHelpers.jelly</li>
 * </ul>
 *
 * @author dalvizu
 */
public abstract class BuildCardExtension
        extends AbstractDescribableImpl<BuildCardExtension>
        implements ExtensionPoint,
        Comparable<BuildCardExtension> {


    /** A Logger object is used to log messages */
    private static final Logger LOGGER = Logger.getLogger(BuildCardExtension.class.getName());

    /**
     * @return all known <code>BuildCardExtension</code>s
     */
    public static ExtensionList<BuildCardExtension> all() {
        return Jenkins.getInstance().getExtensionList(BuildCardExtension.class);
    }

    /**
     * Return an index to where this should be displayed, relative to other options
     *
     * @return
     *  the index - lower appears first in the list
     */
    public abstract long getIndex();

    @Override
    public int compareTo(BuildCardExtension o) {
        return Long.compare(getIndex(), o.getIndex());
    }

    /**
     * Re-run the build known by the given Run externalizeableId
     * @param externalizableId - a Run externalizableId
     * @return the integer of the next Run
     */
    public int rerunBuild(String externalizableId) {
        final AbstractBuild<?, ?> triggerBuild = (AbstractBuild<?, ?>) Run.fromExternalizableId(externalizableId);
        final AbstractProject<?, ?> triggerProject = triggerBuild.getProject();
        final Future<?> future = triggerProject.scheduleBuild2(triggerProject.getQuietPeriod(), new Cause.UserIdCause(),
                filterActions(triggerBuild.getActions()));

        AbstractBuild<?, ?> result = triggerBuild;
        try {
            result = (AbstractBuild<?, ?>) future.get();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }

        return result.getNumber();
    }

    /**
     * Filter out the list of actions so that it only includes {@link ParametersAction} and
     * CauseActions, removing the UserIdAction from the CauseAction's list of Causes.
     *
     * We want to include CauseAction because that includes upstream cause actions, which
     * are inherited in downstream builds.
     *
     * We do not want to inherit the UserId cause, because the user initiating a retry may
     * be different than the user who originated the upstream build, and so should be
     * re-identified.
     *
     * We do not want to inherit any other CauseAction because that will result in duplicating
     * actions from publishers, and builders from previous builds corrupting the retriggered build.
     *
     * @param actions
     *            a collection of build actions.
     * @return a collection of build actions with all UserId causes removed.
     */
    protected List<Action> filterActions(final List<Action> actions) {
        final List<Action> retval = new ArrayList<Action>();
        for (final Action action : actions) {
            if (action instanceof CauseAction) {
                final CauseAction causeAction  = filterOutUserIdCause((CauseAction) action);
                if (!causeAction.getCauses().isEmpty()) {
                    retval.add(causeAction);
                }
            } else if (action instanceof ParametersAction) {
                retval.add(action);
            } else if ("hudson.plugins.git.RevisionParameterAction".equals(action.getClass().getName())) {
                retval.add(action);
            }
        }
        return retval;
    }

    /**
     * Filter out {@link Cause.UserIdCause} from the given {@link CauseAction}.
     *
     * We want to do this because re-run will want to contribute its own
     * {@link Cause.UserIdCause}, not copy it from the previous run.
     *
     * @param causeAction
     *  the causeAction to remove UserIdCause from
     * @return a causeAction with UserIdCause removed
     */
    protected CauseAction filterOutUserIdCause(CauseAction causeAction) {
        final List<Cause> causes = new ArrayList<Cause>();
        final Iterator<Cause> it = causeAction.getCauses().iterator();
        while (it.hasNext()) {
            final Cause cause = it.next();
            if (!(cause instanceof Cause.UserIdCause)) {
                causes.add(cause);
            }
        }
        return new CauseAction(causes);
    }

    /**
     * Trigger a manual build
     * @param pipelineContext - the context of the calling pipeline, used to find projects in the pipeline
     * @param upstreamBuildNumber - the build  number of the upstream build
     * @param triggerProjectName - the name of the project being manually triggered
     * @param upstreamProjectName - the name of the project in the upstream build
     * @return the build number of the triggered manual build
     */
    public int triggerManualBuild(ItemGroup pipelineContext, Integer upstreamBuildNumber, String triggerProjectName,
                                  String upstreamProjectName) {
        final AbstractProject<?, ?> triggerProject =
                (AbstractProject<?, ?>) Jenkins.getInstance().getItem(triggerProjectName, pipelineContext);
        final AbstractProject<?, ?> upstreamProject =
                (AbstractProject<?, ?>) Jenkins.getInstance().getItem(upstreamProjectName, pipelineContext);

        final AbstractBuild<?, ?> upstreamBuild = retrieveBuild(upstreamBuildNumber, upstreamProject);

        // Get parameters from upstream build
        if (upstreamBuild != null) {
            LOGGER.fine("Getting parameters from upstream build " + upstreamBuild.getExternalizableId()); //$NON-NLS-1$
        }
        Action buildParametersAction = null;
        if (upstreamBuild != null) {
            buildParametersAction = BuildUtil.getAllBuildParametersAction(upstreamBuild, triggerProject);
        }

        return triggerBuild(triggerProject, upstreamBuild, buildParametersAction);
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
    protected int triggerBuild(final AbstractProject<?, ?> triggerProject, final AbstractBuild<?, ?> upstreamBuild,
                             final Action buildParametersAction) {
        LOGGER.fine("Triggering build for project: " + triggerProject.getFullDisplayName()); //$NON-NLS-1$
        final List<Action> buildActions = new ArrayList<Action>();
        final List<Cause> causes = new ArrayList<Cause>();
        causes.add(new Cause.UserIdCause());
        if (upstreamBuild != null) {
            causes.add(new Cause.UpstreamCause((Run<?, ?>) upstreamBuild));
        }
        final CauseAction causeAction = new CauseAction(causes);
        buildActions.add(causeAction);
        ParametersAction parametersAction =
                buildParametersAction instanceof ParametersAction
                        ? (ParametersAction) buildParametersAction : new ParametersAction();

        if (upstreamBuild != null) {


            final List<AbstractBuildParameters> configs = retrieveUpstreamProjectTriggerConfig(triggerProject, upstreamBuild);

            if (configs == null) {
                LOGGER.log(Level.SEVERE, "No upstream trigger found for this project: " + triggerProject.getFullDisplayName());
                throw new IllegalStateException("No upstream trigger found for this project: " + triggerProject.getFullDisplayName());
            }

            for (final AbstractBuildParameters config : configs) {
                try {
                    final Action action = config.getAction(upstreamBuild, new LogTaskListener(LOGGER, Level.INFO));
                    if (action instanceof ParametersAction) {
                        parametersAction = mergeParameters(parametersAction, (ParametersAction) action);
                    } else {
                        buildActions.add(action);
                    }
                } catch (final IOException e) {
                    LOGGER.log(Level.SEVERE, "I/O exception while adding build parameter", e); //$NON-NLS-1$
                } catch (final InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Adding build parameter was interrupted", e); //$NON-NLS-1$
                } catch (final AbstractBuildParameters.DontTriggerException e) {
                    LOGGER.log(Level.FINE, "Not triggering : " + config); //$NON-NLS-1$
                }
            }
        }

        buildActions.add(parametersAction);

        triggerProject.scheduleBuild(triggerProject.getQuietPeriod(), null, buildActions.toArray(new Action[buildActions.size()]));
        return triggerProject.getNextBuildNumber();
    }

    /**
     * Used to retrieve the parameters from the upstream project build trigger relative to the given downstream project
     * @param project the downstream project
     * @param upstreamBuild the upstream project build
     * @return the trigger config relative to the given downstream project
     */
    protected List<AbstractBuildParameters> retrieveUpstreamProjectTriggerConfig(final AbstractProject<?, ?> project,
                                                                               final AbstractBuild<?, ?> upstreamBuild) {
        LOGGER.fine("Looking for triggers in the upstream project: " + upstreamBuild.getProject().getFullName());
        final DescribableList<Publisher, Descriptor<Publisher>> upstreamProjectPublishersList =
                upstreamBuild.getProject().getPublishersList();

        List<AbstractBuildParameters> configs = null;

        final BuildPipelineTrigger manualTrigger = upstreamProjectPublishersList.get(BuildPipelineTrigger.class);
        if (manualTrigger != null) {
            LOGGER.fine("Found Manual Trigger (BuildPipelineTrigger) found in upstream project publisher list ");
            final Set<String> downstreamProjectsNames =
                    Sets.newHashSet(Splitter.on(",").trimResults().split(manualTrigger.getDownstreamProjectNames()));
            LOGGER.fine("Downstream project names: " + downstreamProjectsNames);
            // defect: requires full name in the trigger. But downstream is just fine!
            if (downstreamProjectsNames.contains(project.getFullName())) {
                configs = manualTrigger.getConfigs();
            } else {
                LOGGER.warning("Upstream project had a Manual Trigger for projects [" + downstreamProjectsNames
                        + "], but that did not include our project [" + project.getFullName() + "]");
            }
        } else {
            LOGGER.fine("No manual trigger found");
        }

        final BuildTrigger autoTrigger = upstreamProjectPublishersList.get(BuildTrigger.class);
        if (autoTrigger != null) {
            for (BuildTriggerConfig config : autoTrigger.getConfigs()) {
                final Set<String> downstreamProjectsNames = Sets.newHashSet(Splitter.on(",").trimResults().split(config.getProjects()));
                if (downstreamProjectsNames.contains(project.getFullName())) {
                    configs = config.getConfigs();
                }
            }
        }

        return configs;
    }

    /**
     * From parameterized trigger plugin src/main/java/hudson/plugins/parameterizedtrigger/BuildTriggerConfig.java
     *
     * @param base
     *      One of the two parameters to merge.
     * @param overlay
     *      The other parameters to merge
     * @return
     *      Result of the merge.
     */
    protected static ParametersAction mergeParameters(final ParametersAction base, final ParametersAction overlay) {
        final LinkedHashMap<String, ParameterValue> params = new LinkedHashMap<String, ParameterValue>();
        for (final ParameterValue param : base.getParameters()) {
            params.put(param.getName(), param);
        }
        for (final ParameterValue param : overlay.getParameters()) {
            params.put(param.getName(), param);
        }
        return new ParametersAction(params.values().toArray(new ParameterValue[params.size()]));
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
    @SuppressWarnings("unchecked")
    protected AbstractBuild<?, ?> retrieveBuild(final int buildNo, final AbstractProject<?, ?> project) {
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

}
