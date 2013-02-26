package au.com.centrumsystems.hudson.plugin.buildpipeline;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class DefaultBuildGridImpl extends BuildGrid {
    private final Map<Integer/*row*/, Map<Integer/*height*/, BuildForm>> data = new HashMap<Integer, Map<Integer, BuildForm>>();

    /**
     * Dimension of the {@link #data}
     */
    private int rows,cols;

    /**
     * Mutable, but only for the code that instantiates {@link DefaultBuildGridImpl}.
     */
    public void set(int row, int col, BuildForm p) {
        Map<Integer, BuildForm> c = data.get(row);
        if (c==null)
            data.put(row,c=new HashMap<Integer, BuildForm>());
        c.put(col,p);

        rows = Math.max(rows,row+1);
        cols = Math.max(cols,col+1);
    }

    @Override
    public BuildForm get(int row, int col) {
        Map<Integer, BuildForm> cols = data.get(row);
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
