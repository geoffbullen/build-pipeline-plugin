package au.com.centrumsystems.hudson.plugin.buildpipeline

import au.com.centrumsystems.hudson.plugin.buildpipeline.PipelineBuild;
import groovy.json.JsonBuilder

class BuildJSONBuilder {

    static String asJSON(PipelineBuild pipelineBuild, Integer formId) {
        def builder = new JsonBuilder()
		def buildStatus = pipelineBuild.currentBuildResult
        def root = builder {
            title(pipelineBuild.project.name)
            status(buildStatus)
            buildNumber(pipelineBuild.currentBuild?.number)
            startDate(pipelineBuild.formattedStartDate)
            startTime(pipelineBuild.formattedStartTime)
            duration(pipelineBuild.buildDuration)
            buildUrl(pipelineBuild.buildResultURL)
            building(buildStatus == 'BUILDING')
			pending(buildStatus == 'PENDING')
            progress(pipelineBuild.buildProgress)
            progressLeft(100 - pipelineBuild.buildProgress)
            id(formId)
        }
        return builder.toString()
    }
}