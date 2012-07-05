////////////////////////////////////////////////////////////////////////////////
//
//  ADOBE SYSTEMS INCORPORATED
//  Copyright 2012 Adobe Systems Incorporated
//  All Rights Reserved.
//
//  NOTICE: Adobe permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////
package au.com.centrumsystems.hudson.plugin.buildpipeline.dashboard;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import hudson.model.AbstractProject;
import hudson.security.Permission;

/**
 * This class provides a read-only view for the existing build-pipeline view. All calls checking permissions return false. The other reason
 * for this class is that it's used in a different context and not as a child of the view tab.
 * 
 * @author Ingo Richter (irichter@adobe.com)
 * @since 04/01/2012
 */
public class ReadOnlyBuildPipelineView extends BuildPipelineView {
    /**
     * 
     * @param displayName
     *            display name of build pipeline view
     * @param description
     *            description of build pipeline view
     * @param selectedJob
     *            selected job of build pipeline view
     * @param noOfDisplayedBuilds
     *            number of displayed build of build pipeline view
     * @param triggerOnlyLatestJob
     *            is trigger only latest job?
     */
    public ReadOnlyBuildPipelineView(final String displayName, final String description, final String selectedJob,
            final String noOfDisplayedBuilds, final boolean triggerOnlyLatestJob) {
        super(displayName, displayName, selectedJob, noOfDisplayedBuilds, triggerOnlyLatestJob);
        // this is ugly, but there is no other way to set the description of the view
        super.description = description;
    }

    @Override
    public boolean hasBuildPermission(final AbstractProject<?, ?> currentProject) {
        // we are not a 'real view' in this case and we don't care in R/O mode
        return false;
    }

    @Override
    public boolean hasPermission(final Permission p) {
        return false;
    }

    @Override
    public AbstractProject<?, ?> getSelectedProject() {
        AbstractProject<?, ?> selectedProject = null;
        if (getSelectedJob() != null) {
            selectedProject = (AbstractProject<?, ?>) jenkins.model.Jenkins.getInstance().getItem(getSelectedJob());
        }

        return selectedProject;
    }
}
