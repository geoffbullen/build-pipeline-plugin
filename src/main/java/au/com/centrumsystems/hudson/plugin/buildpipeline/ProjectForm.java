package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.model.AbstractProject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author marcin
 * 
 *         Representation of a set of projects
 * 
 */
public class ProjectForm {
    /**
     * project name
     */
    private final String name;
    /**
     * last build result
     */
    private final String result;
    /**
     * overall health
     */
    private final String health;
    /**
     * project url
     */
    private final String url;
    /**
     * downstream projects
     */
    private final List<ProjectForm> dependencies;

    /**
     * @param name
     *            project name
     */
    public ProjectForm(final String name) {
        this.name = name;
        result = "";
        health = "";
        url = "";
        dependencies = new ArrayList<ProjectForm>();
    }

    /**
     * @param project
     *            project
     */
    public ProjectForm(final AbstractProject<?, ?> project) {

        final PipelineBuild pipelineBuild = new PipelineBuild(project.getLastBuild(), project, null);

        name = pipelineBuild.getProject().getName();
        result = pipelineBuild.getCurrentBuildResult();
        health = pipelineBuild.getUpstreamBuildResult();
        url = pipelineBuild.getProjectURL();
        dependencies = new ArrayList<ProjectForm>();
        for (final AbstractProject<?, ?> dependency : project.getDownstreamProjects()) {
            dependencies.add(new ProjectForm(dependency));
        }

    }

    public String getName() {
        return name;
    }

    public String getHealth() {
        return health;
    }

    public String getResult() {
        return result;
    }

    public String getUrl() {
        return url;
    }

    public List<ProjectForm> getDependencies() {
        return dependencies;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProjectForm other = (ProjectForm) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
