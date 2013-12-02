package au.com.centrumsystems.hudson.plugin.buildpipeline

import groovy.json.JsonBuilder
import hudson.model.Cause
import hudson.model.Item

class BuildJSONBuilder {

	static String asJSON(PipelineBuild pipelineBuild, Integer formId, Integer projectId, List<Integer> buildDependencyIds) {
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
				isSuccess(buildStatus == 'SUCCESS')
				isReadyToBeManuallyBuilt(pipelineBuild.isReadyToBeManuallyBuilt())
				isManualTrigger(pipelineBuild.isManualTrigger())
				isRerunable(pipelineBuild.isRerunnable())
				isLatestBuild(null != pipelineBuild.currentBuild?.number && pipelineBuild.currentBuild?.number == pipelineBuild.project.getLastBuild()?.number)
				isUpstreamBuildLatest(null != pipelineBuild.upstreamBuild?.number && pipelineBuild.upstreamBuild?.number == pipelineBuild.upstreamPipelineBuild?.project?.getLastBuild()?.number)
				isUpstreamBuildLatestSuccess(null != pipelineBuild.upstreamBuild?.number && pipelineBuild.upstreamBuild?.number == pipelineBuild.upstreamPipelineBuild?.project?.lastSuccessfulBuild?.number)
				number(pipelineBuild.currentBuild?.number)
				progress(pipelineBuild.buildProgress)
				progressLeft(100 - pipelineBuild.buildProgress)
				startDate(pipelineBuild.formattedStartDate)
				startTime(pipelineBuild.formattedStartTime)
				status(buildStatus)
				url(pipelineBuild.buildResultURL ? pipelineBuild.buildResultURL : pipelineBuild.projectURL)
				userId(pipelineBuild.currentBuild?.getCause(Cause.UserIdCause.class)?.getUserId())
				estimatedRemainingTime(pipelineBuild.currentBuild?.executor?.estimatedRemainingTime)
			}
			project {
				disabled(pipelineBuild.projectDisabled)
				name(pipelineBuild.project.name)
				url(pipelineBuild.projectURL)
				health(pipelineBuild.projectHealth)
				id(projectId)
			}
			upstream {
				projectName(pipelineBuild.upstreamPipelineBuild?.project?.name)
				buildNumber(pipelineBuild.upstreamBuild?.number)
			}
		}
		return builder.toString()
	}
}