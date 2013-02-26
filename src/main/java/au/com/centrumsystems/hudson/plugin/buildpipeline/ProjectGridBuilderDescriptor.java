package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import hudson.slaves.Cloud;
import jenkins.model.Jenkins;

/**
 * {@link Descriptor} for {@link ProjectGridBuilder}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ProjectGridBuilderDescriptor extends Descriptor<ProjectGridBuilder> {
    public ProjectGridBuilderDescriptor(Class<? extends ProjectGridBuilder> clazz) {
        super(clazz);
    }

    public ProjectGridBuilderDescriptor() {
    }

    /**
     * Returns all the registered {@link Cloud} descriptors.
     */
    public static DescriptorExtensionList<ProjectGridBuilder,ProjectGridBuilderDescriptor> all() {
        return Jenkins.getInstance().<ProjectGridBuilder,ProjectGridBuilderDescriptor>getDescriptorList(ProjectGridBuilder.class);
    }

}
