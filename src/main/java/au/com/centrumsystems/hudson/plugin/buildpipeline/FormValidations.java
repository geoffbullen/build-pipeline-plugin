package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.util.FormValidation;

/**
 * Form validation class for validation errors.
 * @author Mathieu Mitchell
 */
public final class FormValidations {
    /** Intentionally private constructor. */
    private FormValidations() {
    }

    /**
     * Returns form validation error when no project is specified.
     * @return form validation error when no project is specified.
     */
    public static FormValidation noProjectSpecified() {
        return FormValidation.error("No project specified");
    }

    /**
     * Returns form validation error when project is not buildable.
     * @param projectName name of project that is not buildable
     * @return form validation error when project is not buildable.
     */
    public static FormValidation notBuildable(String projectName) {
        return FormValidation.error("‘" + projectName + "‘ not buildable");
    }

    /**
     * Returns form validation error when project does not exist.
     * @param projectName name of project that does not exist
     * @param nearestProjectName name of a project that might be near the non-existent project
     * @return form validation error when project does not exist.
     */
    public static FormValidation noSuchProject(String projectName, String nearestProjectName) {
        return FormValidation.error(
                "No such project ‘" + projectName + "‘. Did you mean ‘" + nearestProjectName + "‘?");
    }
}
