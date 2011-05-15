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
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.DependecyDeclarer;
import hudson.model.DependencyGraph;
import hudson.model.Item;
import hudson.model.Items;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.model.listeners.ItemListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Messages;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class BuildPipelineTrigger extends Notifier implements DependecyDeclarer {

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
     * @param downstreamProjectNames
     *            - the job name of the downstream build
     */
    @DataBoundConstructor
    public BuildPipelineTrigger(final String downstreamProjectNames) {
        if (downstreamProjectNames == null) {
            throw new IllegalArgumentException();
        }

        setDownstreamProjectNames(downstreamProjectNames);
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
    public void buildDependencyGraph(final AbstractProject owner, final DependencyGraph graph) {
        if (downstreamProjectNames != null && downstreamProjectNames.length() > 0) {
            for (final Object o : Items.fromNameList(downstreamProjectNames, AbstractProject.class)) {
                final AbstractProject downstream = (AbstractProject) o;

                graph.addDependency(createDownstreamDependency(owner, downstream));
            }
        }
    }

    /**
     * Create a new DownstreamDependency
     * 
     * @param owner
     *            - upstream project
     * @param downstream
     *            - downstream project
     * @return downstream dependency
     */
    private DownstreamDependency createDownstreamDependency(final AbstractProject<?, ?> owner, final AbstractProject<?, ?> downstream) {
        return new DownstreamDependency(owner, downstream);
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
        throws InterruptedException, IOException {
        return true;
    }

    /**
     * Renames a project contained in downstreamProjectNames
     * 
     * @param oldName
     *            - The old name of the project
     * @param newName
     *            - The new name of the project
     * @return - true: A downstream project has been renamed; false No downstream projects were renamed
     */
    public boolean onDownstreamProjectRenamed(final String oldName, final String newName) {
        boolean changed = false;
        final String[] existingDownstreamProjects = this.getDownstreamProjectNames().split(",");
        for (int i = 0; i < existingDownstreamProjects.length; i++) {
            if (existingDownstreamProjects[i].trim().equals(oldName)) {
                existingDownstreamProjects[i] = newName;
                changed = true;
            }
        }
        if (changed) {
            final StringBuilder newDownstreamProjects = new StringBuilder();
            for (int i = 0; i < existingDownstreamProjects.length; i++) {
                if (existingDownstreamProjects[i] == null) {
                    continue;
                }
                if (newDownstreamProjects.length() > 0) {
                    newDownstreamProjects.append(',');
                }
                newDownstreamProjects.append(existingDownstreamProjects[i].trim());
            }
            this.setDownstreamProjectNames(newDownstreamProjects.toString());
        }
        return changed;
    }

    /**
     * Deletes a project from downstreamProjectNames.
     * 
     * @param oldName
     *            - Project to be deleted
     * @return - true; project deleted: false; project not deleted {@link #onDownstreamProjectRenamed(String, String)}
     */
    public boolean onDownstreamProjectDeleted(final String oldName) {
        return onDownstreamProjectRenamed(oldName, null);
    }

    /**
     * Set the descriptor for build pipeline trigger class This descriptor is only attached to Build Trigger Post Build action in JOB
     * configuration page
     * 
     * @author Centrum Systems
     * 
     */
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
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

        @Override
        public Publisher newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
            return new BuildPipelineTrigger(formData.getString("downstreamProjectNames"));
        }

        /**
         * Validates that the downstream project names entered are valid projects.
         * 
         * @param value
         *            - The entered project names
         * @return hudson.util.FormValidation
         */
        public FormValidation doCheckDownstreamProjectNames(@QueryParameter("downstreamProjectNames") final String value) {
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

        /**
         * If a job is renamed, update all BuildPipelineTriggers with the new name.
         */
        @Extension
        public static final class ItemListenerImpl extends ItemListener {
            @Override
            public void onRenamed(final Item item, final String oldName, final String newName) {
                for (final Project<?, ?> p : Hudson.getInstance().getProjects()) {
                    final BuildPipelineTrigger bpTrigger = p.getPublishersList().get(BuildPipelineTrigger.class);
                    if (bpTrigger != null) {
                        boolean changed = false;
                        changed = bpTrigger.onDownstreamProjectRenamed(oldName, newName);

                        if (changed) {
                            try {
                                p.save();
                            } catch (final IOException e) {
                                Logger.getLogger(ItemListenerImpl.class.getName()).log(
                                    Level.WARNING,
                                    "Failed to persist project BuildPipelineTrigger setting during rename from " + oldName + " to "
                                        + newName, e);
                            }
                        }
                    }
                }
            }

            @Override
            public void onDeleted(final Item item) {
                for (final Project<?, ?> p : Hudson.getInstance().getProjects()) {
                    final String oldName = item.getName();
                    final BuildPipelineTrigger bpTrigger = p.getPublishersList().get(BuildPipelineTrigger.class);
                    if (bpTrigger != null) {
                        boolean changed = false;

                        if (bpTrigger.onDownstreamProjectDeleted(oldName)) {
                            changed = true;
                        }

                        if (changed) {
                            try {
                                if (bpTrigger.getDownstreamProjectNames().length() == 0) {
                                    p.getPublishersList().remove(bpTrigger);
                                }
                                p.save();
                            } catch (final IOException e) {
                                Logger.getLogger(ItemListenerImpl.class.getName()).log(Level.WARNING,
                                    "Failed to persist project BuildPipelineTrigger setting during remove of " + oldName, e);
                            }
                        }
                    }
                }
            }
        }
    }
}
