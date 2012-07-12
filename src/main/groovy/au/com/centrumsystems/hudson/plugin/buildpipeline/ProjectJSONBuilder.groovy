package au.com.centrumsystems.hudson.plugin.buildpipeline

import au.com.centrumsystems.hudson.plugin.buildpipeline.PipelineBuild;
import groovy.json.JsonBuilder
import hudson.model.Cause;
import hudson.model.Cause.UserIdCause;
import hudson.model.Item;

class ProjectJSONBuilder {

    static String asJSON(ProjectForm projectForm) {
        def builder = new JsonBuilder()
        def root = builder {
			id(projectForm.id)			
            name(projectForm.name)
            health(projectForm.health)
            url(projectForm.url)        
        }
        return builder.toString()
    }
}