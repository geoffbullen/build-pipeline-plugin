package au.com.centrumsystems.hudson.plugin.buildpipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * @author Centrum Systems
 * 
 *         Representation of the projects and their related builds making up the build pipeline view
 * 
 */
public class BuildPipelineForm {
	private static final Logger LOGGER = Logger.getLogger(BuildPipelineForm.class.getName());
	
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
		int row = getNextAvailableRow(projectGrid, startingRow, startingColumn);
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
		int row = getNextAvailableRow(buildGrid, startingRow, startingColumn);
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
	 * Determines the next row of the grid that should be populated
     * @param grid - The grid of objects to be analysed
     * @param currentRow - The current row of the grid being used
     * @param currentColumn - The current column of the grid being used
	 * @return - The row number to be used
	 */
	private int getNextAvailableRow(Map<Integer, ? extends Map<Integer, ?>> grid, int currentRow, int currentColumn) {
		int nextRow = currentRow;
		boolean nextRoundFound = false;
		if (grid != null) {
			// For each row of the grid
			while (!nextRoundFound) {
				final Map<Integer, ?> gridRow = grid.get(nextRow);
				if (gridRow != null) {
					if (rowAlreadyContainsData(gridRow, currentColumn)) {
						nextRow++;
					} else {
						nextRoundFound = true;
					}
				} else {
					nextRoundFound = true;
				}
			}
		}

		return nextRow;
	}

	/**
     * Tests if the row of the grid already contains entries in the columns greater than 
     * the entered column.
     * @param rowOfGrid - The row of the grid
     * @param col - The current column of the grid 
     * @return - true: The row does contain data in the columns greater than col,
     *          false: The row does not contain data in the columns greater than col
	 */
	private boolean rowAlreadyContainsData(Map<Integer, ?> rowOfGrid, int col) {
		if (rowOfGrid != null) {
			for (Entry<Integer, ?> entry : rowOfGrid.entrySet()) {
				if (entry.getKey() >= col) {
					if (entry.getValue() != null) {
						return true;
					}
				}
			}
			return false;
		} else {
			return false;
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

	/**
     * Gets a display value to determine whether a manual jobs 'trigger' button 
     * will be shown.  This is used along with isTriggerOnlyLatestJob property 
     * allow only the latest version of a job to run.  
	 * 
     * Works by:
     * Initially always defaulted to true.
     * If isTriggerOnlyLatestJob is set to true then as the html code is rendered the first
     * job which should show the trigger button will render and then a call will be made
     * to 'setDisplayTrigger' to change the value to both so all future jobs will 
     * not display the trigger.  see main.jelly
	 * 
     * @param row the row of the job
     * @param height the height of the job
	 * @return boolean whether to display or not
	 */
	public Boolean getDisplayTrigger(int row, int height) {
		return projectGrid.get(row).get(height).getDisplayTrigger();
	}

	/**
     * Sets a display value to determine whether a manual jobs 'trigger' button 
     * will be shown.  This is used along with isTriggerOnlyLatestJob property 
     * allow only the latest version of a job to run.  
	 * 
     * Works by:
     * Initially always defaulted to true.
     * If isTriggerOnlyLatestJob is set to true then as the html code is rendered the first
     * job which should show the trigger button will render and then a call will be made
     * to 'setDisplayTrigger' to change the value to both so all future jobs will 
     * not display the trigger.  see main.jelly

     * @param row the row of the job
     * @param height the height of the job
     * @param display - boolean to indicate whether the trigger button should be shown
	 */
	public void setDisplayTrigger(int row, int height, boolean display) {
		projectGrid.get(row).get(height).setDisplayTrigger(display);
	}

	public List<Map<Integer, Map<Integer, BuildForm>>> getBuildGrids() {
		return buildGrids;
	}

}
