package au.com.centrumsystems.hudson.plugin.buildpipeline;

/**
 * Build not found
 * 
 * @author marcin
 * 
 */
public class BuildNotFoundException extends RuntimeException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * constructor
     * 
     * @param message
     *            message
     */
    public BuildNotFoundException(String message) {
        super(message);
    }

}
