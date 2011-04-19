package au.com.centrumsystems.hudson.plugin.buildpipeline;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;

import java.util.TimeZone;

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
        final FreeStyleProject project2 = createFreeStyleProject(proj2);
        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        hudson.rebuildDependencyGraph();
        final FreeStyleBuild build1 = buildAndAssertSuccess(project1);
        waitUntilNoActivity();

        final PipelineBuild pb = new PipelineBuild(build1, project1, null);
        final BuildForm bf = new BuildForm(pb);

        assertThat(bf.getName(), is(pb.getBuildDescription()));
        assertThat(bf.getStatus(), is(pb.getCurrentBuildResult()));
        assertThat(bf.getUrl(), is(pb.getBuildResultURL()));
        assertThat(bf.getRevision(), is("No Revision"));
        assertThat(bf.getDependencies().get(0).getName(), is(project2.getLastBuild().toString()));
        assertThat(bf.getStartTime(), containsString(TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT)));
    }
}
