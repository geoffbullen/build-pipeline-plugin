package au.com.centrumsystems.hudson.plugin.buildpipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildPipelineForm {

    private final Map<Integer, Map<Integer, ProjectForm>> projectGrid;
    private final List<Map<Integer, Map<Integer, BuildForm>>> buildGrids;

    public BuildPipelineForm(final ProjectForm projectForm, final BuildForm... buildForms) {
        projectGrid = new HashMap<Integer, Map<Integer, ProjectForm>>();
        // recurse through projects and dependencies and place them in grid
        // root project in position 0:0 in grid
        // root project's 1st dependency will go in 0:1
        placeProjectInGrid(0, 0, projectForm);

        buildGrids = new ArrayList<Map<Integer, Map<Integer, BuildForm>>>();
        for (final BuildForm buildForm : buildForms) {
            final Map<Integer, Map<Integer, BuildForm>> buildGrid = new HashMap<Integer, Map<Integer, BuildForm>>();
            // recurse through projects and dependencies and place them in grid
            // root project in position 0:0 in grid
            // root project's 1st dependency will go in 0:1
            placeBuildInGrid(0, 0, buildForm, buildGrid);
            buildGrids.add(buildGrid);
        }
    }

    public BuildPipelineForm(final ProjectForm projectForm, final List<BuildForm> buildPipeForms) {
        this(projectForm, buildPipeForms.toArray(new BuildForm[buildPipeForms.size()]));
    }

    private void placeProjectInGrid(final int startingRow, final int startingColumn, final ProjectForm projectForm) {
        int row = startingRow;
        if (projectGrid.get(row) == null) {
            projectGrid.put(row, new HashMap<Integer, ProjectForm>());
        }
        projectGrid.get(row).put(startingColumn, projectForm);
        final int childrensColumn = startingColumn + 1;
        for (final ProjectForm downstreamProject : projectForm.getDependencies()) {
            placeProjectInGrid(row, childrensColumn, downstreamProject);
            row++;
        }
    }

    private void placeBuildInGrid(final int startingRow, final int startingColumn, final BuildForm buildForm,
            final Map<Integer, Map<Integer, BuildForm>> buildGrid) {
        int row = startingRow;
        if (buildGrid.get(row) == null) {
            buildGrid.put(row, new HashMap<Integer, BuildForm>());
        }
        buildGrid.get(row).put(startingColumn, buildForm);
        final int childrensColumn = startingColumn + 1;
        for (final BuildForm downstreamProject : buildForm.getDependencies()) {
            placeBuildInGrid(row, childrensColumn, downstreamProject, buildGrid);
            row++;
        }
    }

    public Map<Integer, Map<Integer, ProjectForm>> getProjectGrid() {
        return projectGrid;
    }

    public Integer getGridWidth() {
        int maxHeight = 0;
        for (final Integer key : projectGrid.keySet()) {
            for (final Integer innerKey : projectGrid.get(key).keySet()) {
                if (maxHeight < innerKey) {
                    maxHeight = innerKey;
                }
            }
        }
        // keys were 0 based
        return maxHeight + 1;
    }

    public Integer getGridHeight() {
        return projectGrid.keySet().size();
    }

    public List<Map<Integer, Map<Integer, BuildForm>>> getBuildGrids() {
        return buildGrids;
    }

}
