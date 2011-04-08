package au.com.centrumsystems.hudson.plugin.buildpipeline;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class ProjectFormTest extends HudsonTestCase {
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
        ProjectForm pf = new ProjectForm(project1);
        assertEquals(project1.getName(), pf.getName());
        assertEquals(pb.getCurrentBuildResult(), pf.getResult());
        assertEquals(pb.getProjectURL(), pf.getUrl());
        assertEquals(pb.getUpstreamBuildResult(), pf.getHealth());
        assertThat(pf.getDependencies().get(0).getName(), is(project2.getName()));
    }

    @Test
    public void testEquals() throws IOException {
        String proj1 = "Project1";
        String proj2 = "Project2";
        FreeStyleProject project1 = createFreeStyleProject(proj1);
        FreeStyleProject project2 = createFreeStyleProject(proj2);
        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        hudson.rebuildDependencyGraph();

        ProjectForm pf = new ProjectForm(project1);
        ProjectForm pf1 = new ProjectForm(project1);
        ProjectForm pf2 = new ProjectForm(project2);

        assertTrue(pf.equals(pf1));
        assertFalse(pf.equals(pf2));
    }
}
