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

    final ProjectForm pInit = new ProjectForm("Init");
    final ProjectForm pA = new ProjectForm("A");
    final ProjectForm pA1 = new ProjectForm("A1");
    final ProjectForm pA2 = new ProjectForm("A2");
    final ProjectForm pB = new ProjectForm("B");
    final ProjectForm pB1 = new ProjectForm("B1");
    final ProjectForm pB2 = new ProjectForm("B2");
    
    // builds
    final BuildForm b1 = new BuildForm("1");
    final BuildForm b2 = new BuildForm("2");
    final BuildForm b3 = new BuildForm("3");
    final BuildForm b4 = new BuildForm("4");
    final BuildForm b5 = new BuildForm("5");
    final BuildForm b6 = new BuildForm("6");

    final BuildForm bInit = new BuildForm("Init");
    final BuildForm bA = new BuildForm("A");
    final BuildForm bA1 = new BuildForm("A1");
    final BuildForm bA2 = new BuildForm("A2");
    final BuildForm bB = new BuildForm("B");
    final BuildForm bB1 = new BuildForm("B1");
    final BuildForm bB2 = new BuildForm("B2");

    @Before
    public void setup() {
        // projects
        p1.getDependencies().add(p2);
        p1.getDependencies().add(p3);
        p3.getDependencies().add(p4);
        p3.getDependencies().add(p5);
        p5.getDependencies().add(p6);
        
        pInit.getDependencies().add(pA);
        pInit.getDependencies().add(pB);
        pA.getDependencies().add(pA1);
        pA.getDependencies().add(pA2);
        pB.getDependencies().add(pB1);
        pB.getDependencies().add(pB2);
        
        // builds
        b1.getDependencies().add(b2);
        b1.getDependencies().add(b3);
        b3.getDependencies().add(b4);
        b3.getDependencies().add(b5);
        b5.getDependencies().add(b6);

        bInit.getDependencies().add(bA);
        bInit.getDependencies().add(bB);
        bA.getDependencies().add(bA1);
        bA.getDependencies().add(bA2);
        bB.getDependencies().add(bB1);
        bB.getDependencies().add(bB2);
    }

    @Test
    public void constructProjectGrid1() {
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
    public void constructBuildGrid1() {
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
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(0).get(0), is(b1));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(0).get(1), is(b2));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(0).get(2), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(1).get(0), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(1).get(1), is(b3));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(1).get(2), is(b4));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(2).get(1), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(2).get(2), is(b5));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(2).get(3), is(b6));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(0).get(0), is(b1));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(0).get(1), is(b2));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(0).get(2), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(1).get(0), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(1).get(1), is(b3));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(1).get(2), is(b4));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(2).get(1), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(2).get(2), is(b5));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(2).get(3), is(b6));
    }

    @Test
    public void gridDimensions() {
        // SUT
        final BuildPipelineForm buildPipelineForm = new BuildPipelineForm(p1, new BuildForm("nothing"));

        // verify
        assertThat(buildPipelineForm.getGridWidth(), is(4));
        assertThat(buildPipelineForm.getGridHeight(), is(3));
    }

    @Test
    public void constructProjectGrid2() {
        // SUT
        final BuildPipelineForm buildPipelineForm = new BuildPipelineForm(pInit, new BuildForm("nothing"));

        // verify
        // expecting
        // |Init|A|A1|
        // |    | |A2|
        // |    |B|B1|
        // |    | |B2|
        assertThat(buildPipelineForm.getProjectGrid().keySet().size(), is(4));
        assertThat(buildPipelineForm.getProjectGrid().get(0).get(0), is(pInit));
        assertThat(buildPipelineForm.getProjectGrid().get(0).get(1), is(pA));
        assertThat(buildPipelineForm.getProjectGrid().get(0).get(2), is(pA1));
        assertThat(buildPipelineForm.getProjectGrid().get(1).get(0), is(nullValue()));
        assertThat(buildPipelineForm.getProjectGrid().get(1).get(1), is(nullValue()));
        assertThat(buildPipelineForm.getProjectGrid().get(1).get(2), is(pA2));
        assertThat(buildPipelineForm.getProjectGrid().get(2).get(0), is(nullValue()));
        assertThat(buildPipelineForm.getProjectGrid().get(2).get(1), is(pB));
        assertThat(buildPipelineForm.getProjectGrid().get(2).get(2), is(pB1));
        assertThat(buildPipelineForm.getProjectGrid().get(3).get(0), is(nullValue()));
        assertThat(buildPipelineForm.getProjectGrid().get(3).get(1), is(nullValue()));
        assertThat(buildPipelineForm.getProjectGrid().get(3).get(2), is(pB2));
    }

    @Test
    public void constructBuildGrid2() {
        // SUT
        final BuildPipelineForm buildPipelineForm = new BuildPipelineForm(new ProjectForm("nothing"), bInit, bInit);

        // verify
        // expecting
        // |Init|A|A1|
        // |    | |A2|
        // |    |B|B1|
        // |    | |B2|
        // |Init|A|A1|
        // |    | |A2|
        // |    |B|B1|
        // |    | |B2|
        assertThat(buildPipelineForm.getBuildGrids().size(), is(2));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(0).get(0), is(bInit));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(0).get(1), is(bA));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(0).get(2), is(bA1));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(1).get(0), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(1).get(1), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(1).get(2), is(bA2));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(2).get(0), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(2).get(1), is(bB));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(2).get(2), is(bB1));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(3).get(0), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(3).get(1), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(0).get(3).get(2), is(bB2));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(0).get(0), is(bInit));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(0).get(1), is(bA));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(0).get(2), is(bA1));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(1).get(0), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(1).get(1), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(1).get(2), is(bA2));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(2).get(0), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(2).get(1), is(bB));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(2).get(2), is(bB1));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(3).get(0), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(3).get(1), is(nullValue()));
        assertThat(buildPipelineForm.getBuildGrids().get(1).get(3).get(2), is(bB2));
    }
}
