package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.model.AbstractBuild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.kohsuke.stapler.bind.JavaScriptMethod;

/**
 * @author Centrum Systems
 * 
 *         Representation of a build results pipeline
 * 
 */
public class BuildForm {
	private static final Logger LOGGER = Logger.getLogger(BuildForm.class.getName());
	
	/**
	 * status
	 */
	private String status = "";

	private PipelineBuild pipelineBuild;

	private final Integer id;

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
		status = pipelineBuild.getCurrentBuildResult();
		dependencies = new ArrayList<BuildForm>();
		for (final PipelineBuild downstream : pipelineBuild.getDownstreamPipeline()) {
			dependencies.add(new BuildForm(downstream));
		}
		id = hashCode();
	}

	public String getStatus() {
		return status;
	}

	public List<BuildForm> getDependencies() {
		return dependencies;
	}

	public List<Integer> getDependencyIds() {
		final List<Integer> ids = new ArrayList<Integer>();
		for (final BuildForm dependency : dependencies) {
			ids.add(dependency.getId());
		}
		return ids;
	}

	@JavaScriptMethod
	public String asJSON() {
		return BuildJSONBuilder.asJSON(pipelineBuild, id, getDependencyIds());
	}

	public int getId() {
		return id;
	}

	@JavaScriptMethod
	public boolean updatePipelineBuild(final int nextBuildNumber) {
		boolean updated = false;
		final AbstractBuild<?, ?> newBuild = pipelineBuild.getProject().getBuildByNumber(nextBuildNumber);
		if (newBuild != null) {
			updated = true;
			pipelineBuild = new PipelineBuild(newBuild, newBuild.getProject(), pipelineBuild.getUpstreamBuild());
		}
		return updated;
	}

	public int getNextBuildNumber() {
		return pipelineBuild.getProject().getNextBuildNumber();
	}

	public String getRevision() {
		return pipelineBuild.getScmRevision();
	}

	@JavaScriptMethod
	public boolean isManualTrigger() {
		return pipelineBuild.isManualTrigger();
	}
	
	public Map<String, String> getParameters() {
		return pipelineBuild.getBuildParameters();
	}
}
