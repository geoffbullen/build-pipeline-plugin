package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.util.AdaptedIterator;
import hudson.util.HttpResponses;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

/**
 * {@link ProjectGridBuilder} based on the upstream/downstream relationship.
 *
 * @author Kohsuke Kawaguchi
 */
public class DownstreamProjectGridBuilder extends ProjectGridBuilder {
    /**
     * Full name of the first job in the grid.
     *
     * @see AbstractProject#getFullName()
     */
    private String firstJob;

    @DataBoundConstructor
    public DownstreamProjectGridBuilder(String firstJob) {
        this.firstJob = firstJob;
    }

    private class GridImpl extends DefaultProjectGridImpl {
        /**
         * Project at the top-left corner. Initiator of the pipeline.
         */
        private final AbstractProject<?,?> start;

        private GridImpl(AbstractProject<?,?> start) {
            this.start = start;
            placeProjectInGrid(0,0,ProjectForm.as(start));
        }

        /**
         * Function called recursively to place a project form in a grid
         *
         * @param startingRow
         *            project will be placed in the starting row and 1st child as well. Each subsequent child will be placed in a row below the
         *            previous.
         * @param startingColumn
         *            project will be placed in starting column. All children will be placed in next column.
         * @param projectForm
         *            project to be placed
         */
        private void placeProjectInGrid(final int startingRow, final int startingColumn, final ProjectForm projectForm) {
            if (projectForm==null)  return;

            int row = getNextAvailableRow(startingRow, startingColumn);
            set(row, startingColumn, projectForm);

            final int childrensColumn = startingColumn + 1;
            for (final ProjectForm downstreamProject : projectForm.getDependencies()) {
                placeProjectInGrid(row, childrensColumn, downstreamProject);
                row++;
            }
        }

        private final Iterable<BuildGrid> builds = new Iterable<BuildGrid>() {
            @Override
            public Iterator<BuildGrid> iterator() {
                if (start==null)        return Collections.<BuildGrid>emptyList().iterator(); // no dat

                final Iterator<? extends AbstractBuild<?,?>> base = start.getBuilds().iterator();
                return new AdaptedIterator<AbstractBuild<?,?>,BuildGrid>(base) {
                    @Override
                    protected BuildGrid adapt(AbstractBuild<?, ?> item) {
                        return new BuildGridImpl(new BuildForm(new PipelineBuild(item)));
                    }
                };
            }
        };

        @Override
        public Iterable<BuildGrid> builds() {
            return builds;
        }
    }

    private class BuildGridImpl extends DefaultBuildGridImpl {
        private BuildGridImpl(final BuildForm start) {
            placeBuildInGrid(0,0,start);
        }

        /**
         * Function called recursively to place a build form in a grid
         *
         * @param startingRow
         *            build will be placed in the starting row and 1st child as well. Each subsequent child will be placed in a row below the
         *            previous.
         * @param startingColumn
         *            build will be placed in starting column. All children will be placed in next column.
         * @param buildForm
         *            build to be placed
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

    public AbstractProject<?,?> getFirstJob(BuildPipelineView owner) {
        return Jenkins.getInstance().getItem(firstJob,owner.getOwnerItemGroup(),AbstractProject.class);
    }

    @Override
    public boolean hasBuildPermission(BuildPipelineView owner) {
        AbstractProject<?, ?> job = getFirstJob(owner);
        return job!=null && job.hasPermission(Item.BUILD);
    }

    @Override
    public HttpResponse doBuild(StaplerRequest req, @AncestorInPath BuildPipelineView owner) throws IOException {
        final AbstractProject<?, ?> p = getFirstJob(owner);
        if (p==null)    return HttpResponses.error(StaplerResponse.SC_BAD_REQUEST,"No such project: "+getFirstJob());

        return new HttpResponse() {
            @Override
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                p.doBuild(req,rsp);
            }
        };
    }

    @Override
    public ProjectGrid build(BuildPipelineView owner) {
        return new GridImpl(getFirstJob(owner));
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

    @Extension(ordinal=1000) // historical default behavior, so give it a higher priority
    public static class DescriptorImpl extends ProjectGridBuilderDescriptor {
        @Override
        public String getDisplayName() {
            return "Based on upstream/downstream relationship";
        }

        /**
         * Display Job List Item in the Edit View Page
         *
         * @return ListBoxModel
         */
        // TODO: this does not handle relative path in the current context correctly
        public ListBoxModel doFillFirstJobItems(@AncestorInPath ItemGroup<?> context) {
            final hudson.util.ListBoxModel options = new hudson.util.ListBoxModel();
            for (final String jobName : Hudson.getInstance().getJobNames()) {
                options.add(jobName);
            }
            return options;
        }
    }
}
