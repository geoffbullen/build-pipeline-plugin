package au.com.centrumsystems.hudson.plugin.buildpipeline;

import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.PipelineHeaderExtension;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.ParametersDefinitionProperty;
import hudson.util.AdaptedIterator;
import hudson.util.HttpResponses;
import hudson.util.ListBoxModel;

import jenkins.model.Jenkins;
import jenkins.util.TimeDuration;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import org.acegisecurity.AccessDeniedException;

/**
 * {@link ProjectGridBuilder} based on the upstream/downstream relationship.
 *
 * @author Kohsuke Kawaguchi
 */
public class DownstreamProjectGridBuilder extends ProjectGridBuilder {
    /**
     * Name of the first job in the grid, relative to the owner view.
     */
    private String firstJob;

    /**
     * Url to the first job in the grid, relative to the owner view
     */
    private String firstJobLink;

    /**
     * @param firstJob Name of the job to lead the piepline.
     */
    @DataBoundConstructor
    public DownstreamProjectGridBuilder(String firstJob) {
        this.firstJob = firstJob;
    }

    /**
     * {@link ProjectGrid} that lays out things via upstream/downstream.
     */
    private static final class GridImpl extends DefaultProjectGridImpl {
        /**
         * Project at the top-left corner. Initiator of the pipeline.
         */
        private final AbstractProject<?, ?> start;

        /**
         * The item group pipeline view belongs to
         */
        private final ItemGroup context;

        /**
         * @param columnHeaders
         *            column headers to get build parameters from
         * @param context
         *            item group pipeline view belongs to, used to compute relative item names
         * @param start The first project to lead the pipeline.
         */
        private GridImpl(PipelineHeaderExtension columnHeaders, ItemGroup context, AbstractProject<?, ?> start) {
            this.context = context;
            this.start = start;
            placeProjectInGrid(0, 0, ProjectForm.as(start, columnHeaders));
        }

        /**
         * Function called recursively to place a project form in a grid
         *
         * @param startingRow    project will be placed in the starting row and 1st child as well. Each subsequent
         *                       child will be placed in a row below the previous.
         * @param startingColumn project will be placed in starting column. All children will be placed in next column.
         * @param projectForm    project to be placed
         */
        private void placeProjectInGrid(final int startingRow, final int startingColumn, final ProjectForm projectForm) {
            if (projectForm == null) {
                return;
            }

            int row = getNextAvailableRow(startingRow, startingColumn);
            set(row, startingColumn, projectForm);

            final int childrensColumn = startingColumn + 1;
            for (final ProjectForm downstreamProject : projectForm.getDependencies()) {
                placeProjectInGrid(row, childrensColumn, downstreamProject);
                row++;
            }
        }

        /**
         * Factory for {@link Iterator}.
         */
        private final Iterable<BuildGrid> builds = new Iterable<BuildGrid>() {
            @Override
            public Iterator<BuildGrid> iterator() {
                if (start == null) {
                    return Collections.<BuildGrid>emptyList().iterator(); // no dat
                }

                final Iterator<? extends AbstractBuild<?, ?>> base = start.getBuilds().iterator();
                return new AdaptedIterator<AbstractBuild<?, ?>, BuildGrid>(base) {
                    @Override
                    protected BuildGrid adapt(AbstractBuild<?, ?> item) {
                        return new BuildGridImpl(new BuildForm(context, new PipelineBuild(item)));
                    }
                };
            }
        };

        @Override
        public Iterable<BuildGrid> builds() {
            return builds;
        }
    }

    /**
     * {@link BuildGrid} implementation that lays things out via its upstream/downstream relationship.
     */
    private static final class BuildGridImpl extends DefaultBuildGridImpl {
        /**
         * @param start The first build to lead the pipeline instance.
         */
        private BuildGridImpl(final BuildForm start) {
            placeBuildInGrid(0, 0, start);
        }

        /**
         * Function called recursively to place a build form in a grid
         *
         * @param startingRow    build will be placed in the starting row and 1st child as well. Each subsequent child
         *                       will be placed in a row below the previous.
         * @param startingColumn build will be placed in starting column. All children will be placed in next column.
         * @param buildForm      build to be placed
         */
        private void placeBuildInGrid(final int startingRow, final int startingColumn, final BuildForm buildForm) {
            int row = getNextAvailableRow(startingRow, startingColumn);
            set(row, startingColumn, buildForm);

            final int childrensColumn = startingColumn + 1;
            for (final BuildForm downstreamProject : buildForm.getDependencies()) {
                placeBuildInGrid(row, childrensColumn, downstreamProject);
                row++;
            }
        }
    }

    public String getFirstJob() {
        return firstJob;
    }

    public String getFirstJobLink() {        
        return firstJobLink;
    }

    /**
     * The job that's configured as the head of the pipeline.
     *
     * @param owner View that this builder is operating under.
     * @return possibly null
     */
    public AbstractProject<?, ?> getFirstJob(BuildPipelineView owner) {
        try {
            return Jenkins.getInstance().getItem(firstJob, owner.getOwnerItemGroup(), AbstractProject.class);
        } catch (final AccessDeniedException ex) {
            return null;
        }
    }

    @Override
    public boolean hasBuildPermission(BuildPipelineView owner) {
        final AbstractProject<?, ?> job = getFirstJob(owner);
        return job != null && job.hasPermission(Item.BUILD);
    }

    @Override
    public boolean startsWithParameters(BuildPipelineView owner) {
        final AbstractProject<?, ?> firstJob = this.getFirstJob(owner);
        final ParametersDefinitionProperty pdp = firstJob.getProperty(ParametersDefinitionProperty.class);
        return pdp != null;
    }

    @Override
    @RequirePOST
    public HttpResponse doBuild(StaplerRequest req, @AncestorInPath BuildPipelineView owner) throws IOException {
        final AbstractProject<?, ?> p = getFirstJob(owner);
        if (p == null) {
            return HttpResponses.error(StaplerResponse.SC_BAD_REQUEST, "No such project: " + getFirstJob());
        }
        return new HttpResponse() {
            @Override
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.sendRedirect("..");
                rsp.setStatus(200);
                try {
                    p.doBuild(req, rsp, new TimeDuration(0));
                } catch (IllegalStateException e) {
                    ;
                    // Ignore because sendRedirect(String) gets called twice. We do not want to hit the top
                    // level of the project but instead we want to be redirected back 1 directory.
                }
            }
        };
    }

    @Override
    public ProjectGrid build(BuildPipelineView owner) {
        final AbstractProject<?, ?> project = Jenkins.getInstance().getItem(firstJob, 
            owner.getOwnerItemGroup(), AbstractProject.class);            
        if (project != null) {
            this.firstJobLink = project.getUrl();
        } else {
            this.firstJobLink = "";
        }
        return new GridImpl(owner.getColumnHeaders(), owner.getOwnerItemGroup(), getFirstJob(owner));
    }

    @Override
    public void onJobRenamed(BuildPipelineView owner, Item item, String oldName, String newName) throws IOException {
        if (item instanceof AbstractProject) {
            if ((oldName != null) && (oldName.equals(this.firstJob))) {
                this.firstJob = newName;
                owner.save();
            }
        }
    }

    /**
     * Descriptor.
     */
    @Extension(ordinal = 1000) // historical default behavior, so give it a higher priority
    public static class DescriptorImpl extends ProjectGridBuilderDescriptor {
        @Override
        public String getDisplayName() {
            return "Based on upstream/downstream relationship";
        }

        /**
         * Display Job List Item in the Edit View Page
         *
         * @param context What to resolve relative job names against?
         * @return ListBoxModel
         */
        public ListBoxModel doFillFirstJobItems(@AncestorInPath ItemGroup<?> context) {
            final hudson.util.ListBoxModel options = new hudson.util.ListBoxModel();
            for (final AbstractProject<?, ?> p : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
                options.add(/* TODO 1.515: p.getRelativeDisplayNameFrom(context) */p.getFullDisplayName(),
                        p.getRelativeNameFrom(context));
            }
            return options;
        }
    }
}
