package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.model.AbstractProject;

public class BuildPipelineForm {

    private final ProjectForm projectForm;

    public BuildPipelineForm(AbstractProject<?, ?> project) {
        projectForm = new ProjectForm(project);
    }

    public ProjectForm getProjectForm() {
        return projectForm;
    }

}
