package au.com.centrumsystems.hudson.plugin.buildpipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author marcinp
 * 
 *         Representation of the projects and their related builds making up the build pipeline view
 * 
 */
public class BuildPipelineForm {

    /**
     * projects laid out in a grid using maps to ease accessing (or maybe I made it way too complicated by not using a 2-dimensional array)
     * Outside map holds rows and inner map has ProjectForm at a particular position (defined with key)
     */
    private final Map<Integer, Map<Integer, ProjectForm>> projectGrid;
    /**
     * a list of maps of map represents build pipelines laid out in grids, similar to projectGrid, but we have many of these grids
     */
    private final List<Map<Integer, Map<Integer, BuildForm>>> buildGrids;

    /**
     * 
     * @param projectForm
     *            Project to be laid out in a grid
     * @param buildForms
     *            builds to be laid out in a grid
     */
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

    /**
     * 
     * @param projectForm
     *            Project to be laid out in a grid
     * @param buildForms
     *            builds to be laid out in a grid
     */
    public BuildPipelineForm(final ProjectForm projectForm, final List<BuildForm> buildForms) {
        this(projectForm, buildForms.toArray(new BuildForm[buildForms.size()]));
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
        int row = startingRow;
        if (!doesRowContainPreviousEntries(projectGrid.get(row), startingColumn)) {
            row++;
        }
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
     * @param buildGrid
     *            build grid to place build in
     */
    private void placeBuildInGrid(final int startingRow, final int startingColumn, final BuildForm buildForm,
            final Map<Integer, Map<Integer, BuildForm>> buildGrid) {
        int row = startingRow;
        if (!doesRowContainPreviousEntries(buildGrid.get(row), startingColumn)) {
            row++;
        }
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

    /**
     * Tests if the row of the grid already contains entries in the columns greater than 
     * the entered column.
     * @param rowOfGrid - The row of the grid
     * @param col - The current column of the grid 
     * @return - true: The row does contain data in the columns greater than col,
     *          false: The row does not contain data in the columns greater than col
     */
    private boolean doesRowContainPreviousEntries(Map<Integer, ?> rowOfGrid, int col) {
        if (rowOfGrid != null) {
            for (Entry<Integer, ?> entry : rowOfGrid.entrySet()) {
                if (entry.getKey() >= col) {
                    if (entry.getValue() != null) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return true;
        }
    }
    
    public Map<Integer, Map<Integer, ProjectForm>> getProjectGrid() {
        return projectGrid;
    }

    /**
     * grid width is the longest column map counting empties (keys represent position, so they are used to determine width)
     * 
     * @return width
     */
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
