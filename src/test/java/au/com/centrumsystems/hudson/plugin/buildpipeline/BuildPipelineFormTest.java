package au.com.centrumsystems.hudson.plugin.buildpipeline;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class BuildPipelineFormTest {

    // projects
    final ProjectForm p1 = new ProjectForm("1");
    final ProjectForm p2 = new ProjectForm("2");
    final ProjectForm p3 = new ProjectForm("3");
    final ProjectForm p4 = new ProjectForm("4");
    final ProjectForm p5 = new ProjectForm("5");
    final ProjectForm p6 = new ProjectForm("6");

    // builds
    final BuildForm b1 = new BuildForm("1");
    final BuildForm b2 = new BuildForm("2");
    final BuildForm b3 = new BuildForm("3");
    final BuildForm b4 = new BuildForm("4");
    final BuildForm b5 = new BuildForm("5");
    final BuildForm b6 = new BuildForm("6");

    @Before
    public void setup() {
        // projects
        p1.getDependencies().add(p2);
        p1.getDependencies().add(p3);
        p3.getDependencies().add(p4);
        p3.getDependencies().add(p5);
        p5.getDependencies().add(p6);
        // builds
        b1.getDependencies().add(b2);
        b1.getDependencies().add(b3);
        b3.getDependencies().add(b4);
        b3.getDependencies().add(b5);
        b5.getDependencies().add(b6);
    }

    @Test
    public void constructProjectGrid() {
        // SUT
        final BuildPipelineForm buildPipelineForm = new BuildPipelineForm(p1, new BuildForm("nothing"));

        // verify
        // expecting
        // |1|2| | |
        // | |3|4| |
        // | | |5|6|
        assertThat(buildPipelineForm.getProjectGrid().keySet().size(), is(3));
        assertThat(buildPipelineForm.getProjectGrid().get(0).get(0), is(p1));
        assertThat(buildPipelineForm.getProjectGrid().get(0).get(1), is(p2));
        assertThat(buildPipelineForm.getProjectGrid().get(0).get(2), is(nullValue()));
        assertThat(buildPipelineForm.getProjectGrid().get(1).get(0), is(nullValue()));
        assertThat(buildPipelineForm.getProjectGrid().get(1).get(1), is(p3));
        assertThat(buildPipelineForm.getProjectGrid().get(1).get(2), is(p4));
        assertThat(buildPipelineForm.getProjectGrid().get(2).get(1), is(nullValue()));
        assertThat(buildPipelineForm.getProjectGrid().get(2).get(2), is(p5));
        assertThat(buildPipelineForm.getProjectGrid().get(2).get(3), is(p6));
    }

    @Test
    public void constructBuildGrid() {
        // SUT
        final BuildPipelineForm buildPipelineForm = new BuildPipelineForm(new ProjectForm("nothing"), b1, b1);

        // verify
        // expecting
        // |1|2| | |
        // | |3|4| |
        // | | |5|6|
        // |1|2| | |
        // | |3|4| |
        // | | |5|6|
        assertThat(buildPipelineForm.getBuildGrids().size(), is(2));
    }

    @Test
    public void gridDimensions() {
        // SUT
        final BuildPipelineForm buildPipelineForm = new BuildPipelineForm(p1, new BuildForm("nothing"));

        // verify
        assertThat(buildPipelineForm.getGridWidth(), is(4));
        assertThat(buildPipelineForm.getGridHeight(), is(3));
    }

}
