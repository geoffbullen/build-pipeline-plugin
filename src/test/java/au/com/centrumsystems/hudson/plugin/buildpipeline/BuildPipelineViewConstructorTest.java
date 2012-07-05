package au.com.centrumsystems.hudson.plugin.buildpipeline;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class BuildPipelineViewConstructorTest {

    final String bpViewName = "MyTestView";
    final String bpViewTitle = "MyTestViewTitle";
    final String proj1 = "Proj1";
    final String noOfBuilds = "5";

    @Test
    public void testAlwaysAllowManualTrigger() throws IOException {

        // True
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, proj1, noOfBuilds, true, true, false, false, false, 2);
        assertTrue(testView.isAlwaysAllowManualTrigger());

        // False
        testView = new BuildPipelineView(bpViewName, bpViewTitle, "", noOfBuilds, true, false, false, false, false, 2);
        assertFalse(testView.isAlwaysAllowManualTrigger());
    }

    @Test
    public void testShowPipelineParameters() throws IOException {

        // True
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, proj1, noOfBuilds, true, false, true, false, false, 2);
        assertTrue(testView.isShowPipelineParameters());

        // False
        testView = new BuildPipelineView(bpViewName, bpViewTitle, "", noOfBuilds, true, false, false, false, false, 2);
        assertFalse(testView.isShowPipelineParameters());
    }

    @Test
    public void testRefreshFrequency() throws IOException {

        // False
        final BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, "", noOfBuilds, true, false, false, false, false, 2);
        assertThat(testView.getRefreshFrequency(), is(2));
        assertThat(testView.getRefreshFrequencyInMillis(), is(2000));
    }

}
