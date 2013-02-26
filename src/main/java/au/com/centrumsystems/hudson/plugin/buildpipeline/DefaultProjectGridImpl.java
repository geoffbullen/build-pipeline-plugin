package au.com.centrumsystems.hudson.plugin.buildpipeline;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ProjectGrid} backed by map.
 *
 * @author Kohsuke Kawaguchi
 * @author Centrum Systems
 */
public abstract class DefaultProjectGridImpl extends ProjectGrid {
    private final Map<Integer/*row*/, Map<Integer/*height*/, ProjectForm>> data = new HashMap<Integer, Map<Integer, ProjectForm>>();

    /**
     * Dimension of the {@link #data}
     */
    private int rows,cols;

    /**
     * Mutable, but only for {@link ProjectGridBuilder}
     */
    public void set(int row, int col, ProjectForm p) {
        Map<Integer, ProjectForm> c = data.get(row);
        if (c==null)
            data.put(row,c=new HashMap<Integer, ProjectForm>());
        c.put(col,p);

        rows = Math.max(rows,row+1);
        cols = Math.max(cols,col+1);
    }

    @Override
    public ProjectForm get(int row, int col) {
        Map<Integer, ProjectForm> cols = data.get(row);
        if (cols==null) return null;
        return cols.get(col);
    }

    @Override
    public int getColumns() {
        return cols;
    }

    @Override
    public int getRows() {
        return rows;
    }

}
