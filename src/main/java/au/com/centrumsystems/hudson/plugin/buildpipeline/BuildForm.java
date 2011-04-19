package au.com.centrumsystems.hudson.plugin.buildpipeline;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author marcinp
 * 
 *         Representation of a build results pipeline
 * 
 */
public class BuildForm {
    /**
     * scn revision
     */
    private String revision = "";
    /**
     * build name
     */
    private String name = "";
    /**
     * status
     */
    private String status = "";

    /**
     * url
     */
    private String url = "";
    /**
     * duration
     */
    private String duration = "";

    /**
     * upstream project
     */
    private String upstreamProjectName = "";

    /**
     * upstream build number
     */
    private String upstreamBuildNumber = "";

    /**
     * project name
     */
    private String projectName = "";

    /**
     * indicates if it is a build that needs to be triggered manually
     */
    private boolean manual;

    /**
     * build start time
     */
    private Date startTime;

    /**
     * downstream builds
     */
    private List<BuildForm> dependencies = new ArrayList<BuildForm>();

    /**
     * @param pipelineBuild
     *            pipeline build domain used to see the form
     */
    public BuildForm(final PipelineBuild pipelineBuild) {
        name = pipelineBuild.getBuildDescription();
        status = pipelineBuild.getCurrentBuildResult();
        revision = pipelineBuild.getScmRevision();
        url = pipelineBuild.getBuildResultURL();
        duration = pipelineBuild.getBuildDuration();
        manual = pipelineBuild.isManual();

        if (pipelineBuild.getUpstreamPipelineBuild() != null) {
            if (pipelineBuild.getUpstreamPipelineBuild().getProject() != null) {
                projectName = pipelineBuild.getProject().getName();
                upstreamProjectName = pipelineBuild.getUpstreamPipelineBuild().getProject().getName();
            }
            if (pipelineBuild.getUpstreamBuild() != null) {
                upstreamBuildNumber = String.valueOf(pipelineBuild.getUpstreamBuild().getNumber());
            }
        }

        dependencies = new ArrayList<BuildForm>();
        for (final PipelineBuild downstream : pipelineBuild.getDownstreamPipeline()) {
            dependencies.add(new BuildForm(downstream));
        }
        startTime = pipelineBuild.getStartTime();
    }

    /**
     * @param name
     *            build name
     */
    public BuildForm(final String name) {
        this.name = name;
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

    public boolean isManual() {
        return manual;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getUpstreamBuildNumber() {
        return upstreamBuildNumber;
    }

    public String getUpstreamProjectName() {
        return upstreamProjectName;
    }

    /**
     * @return Formatted start time
     */
    public String getStartTime() {
        String formattedStartTime = "";
        if (startTime != null) {
            formattedStartTime = DateFormat.getTimeInstance(DateFormat.FULL).format(startTime);
        }
        return formattedStartTime;
    }

    /**
     * @return Formatted start date
     */
    public String getStartDate() {
        String formattedStartTime = "";
        if (startTime != null) {
            formattedStartTime = DateFormat.getDateInstance(DateFormat.MEDIUM).format(startTime);
        }
        return formattedStartTime;
    }

    public List<BuildForm> getDependencies() {
        return dependencies;
    }

    /**
     * Shortened revision (git SHA1 can get long
     * 
     * @return shortened revision
     */
    public String getShortRevision() {
        String shortRevision;
        if (revision.length() > 22) {
            shortRevision = revision.substring(0, 22) + "...";
        } else {
            shortRevision = revision;
        }
        return shortRevision;
    }
}
