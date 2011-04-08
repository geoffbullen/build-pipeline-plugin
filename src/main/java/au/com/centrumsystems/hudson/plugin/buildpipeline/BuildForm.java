package au.com.centrumsystems.hudson.plugin.buildpipeline;

import java.util.ArrayList;
import java.util.List;

public class BuildForm {
    private final String revision;
    private final String name;
    private final String status;
    private final String URL;
    private final String duration;
    private final List<BuildForm> dependencies;

    public BuildForm(final PipelineBuild pipelineBuild) {
        name = pipelineBuild.getBuildDescription();
        status = pipelineBuild.getCurrentBuildResult();
        revision = pipelineBuild.getSVNRevisionNo();
        URL = pipelineBuild.getBuildResultURL();
        duration = pipelineBuild.getBuildDuration();
        dependencies = new ArrayList<BuildForm>();
        for (final PipelineBuild downstream : pipelineBuild.getDownstreamPipeline()) {
            dependencies.add(new BuildForm(downstream));
        }
    }

    public BuildForm(final String name) {
        this.name = name;
        status = "";
        revision = "";
        URL = "";
        duration = "";
        dependencies = new ArrayList<BuildForm>();
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getRevision() {
        return revision;
    }

    public String getURL() {
        return URL;
    }

    public String getDuration() {
        return duration;
    }

    public List<BuildForm> getDependencies() {
        return dependencies;
    }
}
