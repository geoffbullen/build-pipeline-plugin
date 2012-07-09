package au.com.centrumsystems.hudson.plugin.buildpipeline

import au.com.centrumsystems.hudson.plugin.buildpipeline.PipelineBuild;
import groovy.json.JsonBuilder
import hudson.model.Cause;
import hudson.model.Cause.UserIdCause;
import hudson.model.Item;

class BuildJSONBuilder {

    static String asJSON(PipelineBuild pipelineBuild, Integer formId, List<Integer> buildDependencyIds) {
        def builder = new JsonBuilder()
		def buildStatus = pipelineBuild.currentBuildResult
        def root = builder {
			id(formId)			
            build {		
                dependencyIds(buildDependencyIds)
                displayName(pipelineBuild.currentBuild?.displayName)
				duration(pipelineBuild.buildDuration)
                extId(pipelineBuild.currentBuild?.externalizableId)
				hasPermission(pipelineBuild.project?.hasPermission(Item.BUILD));
                hasUpstreamBuild(null != pipelineBuild.upstreamBuild)
				isBuilding(buildStatus == 'BUILDING')
				isComplete(buildStatus != 'BUILDING' && buildStatus != 'PENDING' && buildStatus != 'MANUAL')
				isPending(buildStatus == 'PENDING')
				isReadyToBeManuallyBuilt(pipelineBuild.isReadyToBeManuallyBuilt())
				isManualTrigger(pipelineBuild.isManualTrigger())
				number(pipelineBuild.currentBuild?.number)                
				progress(pipelineBuild.buildProgress)
				progressLeft(100 - pipelineBuild.buildProgress)
				startDate(pipelineBuild.formattedStartDate)
				startTime(pipelineBuild.formattedStartTime)
				status(buildStatus)								
				url(pipelineBuild.buildResultURL)					
				userId(pipelineBuild.currentBuild?.getCause(Cause.UserIdCause.class)?.getUserId())
                estimatedRemainingTime(pipelineBuild.currentBuild?.executor?.estimatedRemainingTime)
			}
			project {                
                disabled(pipelineBuild.projectDisabled)
				name(pipelineBuild.project.name)
				url(pipelineBuild.projectURL)                
			}
			upstream {
				projectName(pipelineBuild.upstreamPipelineBuild?.project?.name)
				buildNumber(pipelineBuild.upstreamBuild?.number)
			}           
        }
        return builder.toString()
    }
}