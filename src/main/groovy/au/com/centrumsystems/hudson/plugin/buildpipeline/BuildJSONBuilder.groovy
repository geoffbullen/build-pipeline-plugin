package au.com.centrumsystems.hudson.plugin.buildpipeline

import au.com.centrumsystems.hudson.plugin.buildpipeline.PipelineBuild;
import groovy.json.JsonBuilder
import hudson.model.Item;

class BuildJSONBuilder {

    static String asJSON(PipelineBuild pipelineBuild, Integer formId) {
        def builder = new JsonBuilder()
		def buildStatus = pipelineBuild.currentBuildResult
        def root = builder {
			id(formId)			
            build {				
				duration(pipelineBuild.buildDuration)				
				hasPermission(pipelineBuild.project?.hasPermission(Item.BUILD));
				isBuilding(buildStatus == 'BUILDING')
				isComplete(buildStatus != 'BUILDING' && buildStatus != 'PENDING')
				isPending(buildStatus == 'PENDING' && !pipelineBuild.isManualTrigger())
				isManual(pipelineBuild.isManual())
				isManualTrigger(pipelineBuild.isManualTrigger())
				number(pipelineBuild.currentBuild?.number)
				progress(pipelineBuild.buildProgress)
				progressLeft(100 - pipelineBuild.buildProgress)
				startDate(pipelineBuild.formattedStartDate)
				startTime(pipelineBuild.formattedStartTime)
				status(buildStatus)								
				url(pipelineBuild.buildResultURL)	
			}
			project {
				name(pipelineBuild.project.name)
			}
			upstream {
				projectName(pipelineBuild.upstreamPipelineBuild?.project?.name)
				buildNumber(pipelineBuild.upstreamBuild?.number)
			}           
        }
        return builder.toString()
    }
}