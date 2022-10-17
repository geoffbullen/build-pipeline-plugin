package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.util.FormValidation;

public class FormValidations
{
	private FormValidations()
	{
	}

	public static FormValidation noProjectSpecified()
	{
		return FormValidation.error("No project specified");
	}

	public static FormValidation notBuildable(String projectName)
	{
		return FormValidation.error("‘" + projectName + "‘ not buildable");
	}

	public static FormValidation noSuchProject(String projectName, String nearestProjectName)
	{
		return FormValidation.error(
			"No such project ‘" + projectName + "‘. Did you mean ‘" + nearestProjectName + "‘?");
	}
}
