package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Item;
import hudson.model.ItemGroup;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

/**
 * Encapsulates the definition of how to layout projects into a {@link ProjectGrid}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ProjectGridBuilder extends AbstractDescribableImpl<ProjectGridBuilder> {

    /**
     * Builds the grid.
     *
     * @param owner
     *      The view for which this builder is working. Never null.
     *      If the {@link ProjectGridBuilder} takes user-supplied job name,
     *      this parameter should be used as a context to resolve relative names.
     *      See {@link Jenkins#getItem(String, ItemGroup)} (where you obtain
     *      {@link ItemGroup} by {@link BuildPipelineView#getOwnerItemGroup()}.
     */
    public abstract ProjectGrid build(BuildPipelineView owner);

    /**
     * Called by {@link BuildPipelineView} when one of its members are renamed.
     */
    public void onJobRenamed(BuildPipelineView owner, Item item, String oldName, String newName) throws IOException {}

    /**
     * If the grid produced by this builder supports the notion of "starting a new pipeline instance",
     * and if the current user has a permission to do so, then return true.
      */
    public abstract boolean hasBuildPermission(BuildPipelineView owner);

    /**
     * Called to start a new pipeline instance
     * (normally by triggering some job.)
     */
    public abstract HttpResponse doBuild(StaplerRequest req, @AncestorInPath BuildPipelineView owner) throws IOException;

    @Override
    public ProjectGridBuilderDescriptor getDescriptor() {
        return (ProjectGridBuilderDescriptor)super.getDescriptor();
    }
}
