package au.com.centrumsystems.hudson.plugin.buildpipeline;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class BuildFormTest extends HudsonTestCase {
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testConstructor() throws Exception {
        final String proj1 = "Project1";
        final String proj2 = "Project2";
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        hudson.rebuildDependencyGraph();
        final FreeStyleBuild build1 = buildAndAssertSuccess(project1);
        waitUntilNoActivity();

        final PipelineBuild pb = new PipelineBuild(build1, project1, null);
        final BuildForm bf = new BuildForm(pb);

        assertThat(bf.getStatus(), is(pb.getCurrentBuildResult()));
    }
}
