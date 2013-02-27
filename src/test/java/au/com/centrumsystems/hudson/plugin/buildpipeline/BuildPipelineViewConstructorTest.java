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
    final DownstreamProjectGridBuilder gridBuilder = new DownstreamProjectGridBuilder(proj1);
    final DownstreamProjectGridBuilder nullGridBuilder = new DownstreamProjectGridBuilder("");
    final String noOfBuilds = "5";

    @Test
    public void testAlwaysAllowManualTrigger() throws IOException {

        // True
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, gridBuilder, noOfBuilds, true, true, false, false, false, 2);
        assertTrue(testView.isAlwaysAllowManualTrigger());

        // False
        testView = new BuildPipelineView(bpViewName, bpViewTitle, nullGridBuilder, noOfBuilds, true, false, false, false, false, 2);
        assertFalse(testView.isAlwaysAllowManualTrigger());
    }

    @Test
    public void testShowPipelineParameters() throws IOException {

        // True
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, gridBuilder, noOfBuilds, true, false, true, false, false, 2);
        assertTrue(testView.isShowPipelineParameters());

        // False
        testView = new BuildPipelineView(bpViewName, bpViewTitle, nullGridBuilder, noOfBuilds, true, false, false, false, false, 2);
        assertFalse(testView.isShowPipelineParameters());
    }

    @Test
    public void testShowPipelineParametersInHeaders() throws IOException {

        // True
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, gridBuilder, noOfBuilds, true, false, true, true, false, 2);
        assertTrue(testView.isShowPipelineParametersInHeaders());

        // False
        testView = new BuildPipelineView(bpViewName, bpViewTitle, nullGridBuilder, noOfBuilds, true, false, false, false, false, 2);
        assertFalse(testView.isShowPipelineParametersInHeaders());
    }

    @Test
    public void testRefreshFrequency() throws IOException {

        // False
        final BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, nullGridBuilder, noOfBuilds, true, false, false, false, false, 2);
        assertThat(testView.getRefreshFrequency(), is(2));
        assertThat(testView.getRefreshFrequencyInMillis(), is(2000));
    }

}
