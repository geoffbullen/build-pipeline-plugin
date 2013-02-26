package au.com.centrumsystems.hudson.plugin.buildpipeline;

import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class DownstreamProjectGridBuilderTest extends HudsonTestCase {
    /**
     * Makes sure that the config form will keep the settings intact.
     */
    public void testConfigRoundtrip() throws Exception {
        DownstreamProjectGridBuilder gridBuilder = new DownstreamProjectGridBuilder("something");
        BuildPipelineView v = new BuildPipelineView("foo","Title", gridBuilder, "5", true);
        jenkins.addView(v);
        configRoundtrip(v);
        BuildPipelineView av = (BuildPipelineView)jenkins.getView(v.getViewName());
        assertNotSame(v,av);
        assertEqualDataBoundBeans(v,av);
        assertNotSame(gridBuilder,av.getGridBuilder());
        assertEqualDataBoundBeans(gridBuilder,av.getGridBuilder());
    }
}
