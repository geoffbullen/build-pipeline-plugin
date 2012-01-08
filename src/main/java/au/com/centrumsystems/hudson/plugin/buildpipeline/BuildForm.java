package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.model.Item;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kohsuke.stapler.bind.JavaScriptMethod;

import au.com.centrumsystems.hudson.plugin.util.HudsonResult;

/**
 * @author Centrum Systems
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
	 * build number
	 */
	private Integer buildNumber;

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
	 * build progress
	 */
	private long buildProgress;

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
	 * does user have build permission
	 */
	private boolean hasBuildPermission;

	/**
	 * build start time
	 */
	private Date startTime;

	private PipelineBuild pipelineBuild;

	private int id;

	/**
	 * downstream builds
	 */
	private List<BuildForm> dependencies = new ArrayList<BuildForm>();

	/**
	 * @param pipelineBuild
	 *            pipeline build domain used to see the form
	 */
	public BuildForm(final PipelineBuild pipelineBuild) {
		this.pipelineBuild = pipelineBuild;
		name = pipelineBuild.getBuildDescription();
		buildNumber = pipelineBuild.getCurrentBuild() != null ? pipelineBuild.getCurrentBuild().getNumber() : null;
		status = pipelineBuild.getCurrentBuildResult();
		revision = pipelineBuild.getScmRevision();
		url = pipelineBuild.getBuildResultURL();
		duration = pipelineBuild.getBuildDuration();
		manual = pipelineBuild.isManual();
		hasBuildPermission = pipelineBuild.getProject().hasPermission(Item.BUILD);
		projectName = pipelineBuild.getProject().getName();

		if (pipelineBuild.getUpstreamPipelineBuild() != null) {
			if (pipelineBuild.getUpstreamPipelineBuild().getProject() != null) {
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
		buildProgress = pipelineBuild.getBuildProgress();
		id = pipelineBuild.hashCode();
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

	/**
	 * Accessor for whether the user is permitted to perform a build
	 * 
	 * @return hasPermission true\false
	 * */
	public boolean hasBuildPermission() {
		return hasBuildPermission;
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
			formattedStartTime = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(startTime);
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
	 * @return estimated build progress
	 */
	public long getBuildProgress() {
		return buildProgress;
	}

	public Integer getBuildNumber() {
		return buildNumber;
	}

	public List<Integer> getDependencyIds() {
		List<Integer> hashes = new ArrayList<Integer>();
		for (final BuildForm dependency : dependencies) {
			hashes.add(dependency.getId());
		}
		return hashes;
	}

	@JavaScriptMethod
	public String asJSON() {
		return new BuildJSONBuilder(new BuildForm(pipelineBuild)).asJSON();
	}

	@JavaScriptMethod
	public long getBuildProgressUpdate() {
		long buildProgress = 0L;
		// if the build is done building, send a negative value
		if (HudsonResult.BUILDING.toString().equals(pipelineBuild.getCurrentBuildResult())) {
			buildProgress = pipelineBuild.getBuildProgress();
		} else {
			buildProgress = -1L;
		}
		return buildProgress;
	}

	public int getId() {
		return id;
	}
}
