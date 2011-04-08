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
        String proj1 = "Project1";
        String proj2 = "Project2";
        FreeStyleProject project1 = createFreeStyleProject(proj1);
        FreeStyleProject project2 = createFreeStyleProject(proj2);
        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        hudson.rebuildDependencyGraph();
        FreeStyleBuild build1 = buildAndAssertSuccess(project1);
        waitUntilNoActivity();

        PipelineBuild pb = new PipelineBuild(build1, project1, null);
        BuildForm bf = new BuildForm(pb);

        assertEquals(pb.getBuildDescription(), bf.getName());
        assertEquals(pb.getCurrentBuildResult(), bf.getStatus());
        assertEquals(pb.getBuildResultURL(), bf.getUrl());
        assertEquals("No Revision", bf.getRevision());
        assertThat(bf.getDependencies().get(0).getName(), is(project2.getLastBuild().toString()));
    }
}
