package au.com.centrumsystems.hudson.plugin.buildpipeline.dashboard;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

public class BuildPipelineDashboardTest
{
	@Rule public JenkinsRule jenkins = new JenkinsRule();

	BuildPipelineDashboard cut;

	@Before public void setUp()
	{
		cut = new BuildPipelineDashboard(
			"TestProject",
			"Test Description",
			new DownstreamProjectGridBuilder("Job10"),
			"5"
		);
	}

	@Test public void shouldReturnANewBuildPipelineView()
	{
		BuildPipelineView bpv = cut.getBuildPipelineView();


		assertNotNull(bpv);
		assertTrue(bpv instanceof ReadOnlyBuildPipelineView);
		assertEquals("Job10", ((DownstreamProjectGridBuilder)bpv.getGridBuilder()).getFirstJob());
		assertEquals("5", bpv.getNoOfDisplayedBuilds());
		assertEquals("TestProject", bpv.getBuildViewTitle());
	}

	@Test public void shouldNotHaveBuildPermissions() {
		BuildPipelineView bpv = cut.getBuildPipelineView();

		assertFalse(bpv.hasBuildPermission());
	}

	@Test public void shouldNotHaveAnyPermission() {
		BuildPipelineView bpv = cut.getBuildPipelineView();

		assertFalse(bpv.hasPermission(null));
	}
}
