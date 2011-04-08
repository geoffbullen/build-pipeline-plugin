package au.com.centrumsystems.hudson.plugin.buildpipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * @author marcin
 * 
 *         Representation of a build results pipeline
 * 
 */
public class BuildForm {
    /**
     * scn revision
     */
    private final String revision;
    /**
     * build name
     */
    private final String name;
    /**
     * status
     */
    private final String status;
    /**
     * url
     */
    private final String url;
    /**
     * duration
     */
    private final String duration;
    /**
     * downstream builds
     */
    private final List<BuildForm> dependencies;

    /**
     * @param pipelineBuild
     *            pipeline build domain used to see the form
     */
    public BuildForm(final PipelineBuild pipelineBuild) {
        name = pipelineBuild.getBuildDescription();
        status = pipelineBuild.getCurrentBuildResult();
        revision = pipelineBuild.getSVNRevisionNo();
        url = pipelineBuild.getBuildResultURL();
        duration = pipelineBuild.getBuildDuration();
        dependencies = new ArrayList<BuildForm>();
        for (final PipelineBuild downstream : pipelineBuild.getDownstreamPipeline()) {
            dependencies.add(new BuildForm(downstream));
        }
    }

    /**
     * @param name
     *            build name
     */
    public BuildForm(final String name) {
        this.name = name;
        status = "";
        revision = "";
        url = "";
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

    public String getUrl() {
        return url;
    }

    public String getDuration() {
        return duration;
    }

    public List<BuildForm> getDependencies() {
        return dependencies;
    }
}
