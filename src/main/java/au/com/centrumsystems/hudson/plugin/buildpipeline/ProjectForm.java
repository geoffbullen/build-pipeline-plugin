package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.util.List;

import au.com.centrumsystems.hudson.plugin.util.HudsonResult;

public class ProjectForm {
    private String name;
    private String revision;
    private HudsonResult hudsonResult;
    private BuildHealth buildHealth;
    private List<ProjectForm> dependencies;

    public ProjectForm(AbstractProject<?, ?> project) {
        if (project.getLastBuild() != null) {
            for (final AbstractBuild<?, ?> currentBuild : project.getBuilds()) {
                final PipelineBuild pipelineBuild = new PipelineBuild(currentBuild, null, null);
                name = pipelineBuild.getBuildDescription();
                revision = pipelineBuild.getSVNRevisionNo();
                // PipelineViewUI.getBuildPipeline("", pipelineBuild, result);
                // result.append(PipelineViewUI.CELL_SUFFIX);
                // rowsAppended++;
                // if (rowsAppended >= maxNoOfDisplayBuilds) {
                // break;
                // }
            }
        }
    }

    public String getRevision() {
        return revision;
    }

    public String getName() {
        return name;
    }
}
