var BuildPipeline = function(viewProxy, buildCardTemplate){
	this.buildCardTemplate = buildCardTemplate;
	this.buildProxies = {};
	this.viewProxy = viewProxy;				
};

BuildPipeline.prototype = {
	showProgress : function(id, dependencies) {
		var buildPipeline = this;
		var intervalId = setInterval(function(){		    	
			buildPipeline.buildProxies[id].asJSON(function(data){
	    		var buildData = jQuery.parseJSON(data.responseObject());
	    		if (buildData.build.progress > 0) {
	    			buildPipeline.updateBuildCardFromJSON(buildData, false);
	    		} else {
		    		buildPipeline.updateBuildCardFromJSON(buildData, true);
	    			clearInterval(intervalId);
	    			//kick off status tracking for all dependencies
	    			jQuery.each(dependencies, function(){
		    			jQuery("#pipelines").trigger("show-status-" + this);
	    			});
	    		}
	    	});
		}, 2000);
	},
	updateBuildCard : function(id) {
		var buildPipeline = this;
		buildPipeline.buildProxies[id].asJSON(function(data){
			buildPipeline.updateBuildCardFromJSON(jQuery.parseJSON(data.responseObject()), true);
		});
	},
	fetchLatestBuildNumber : function(id) {
		var buildPipeline = this;
		console.log(buildPipeline.buildProxies[id])
		buildPipeline.buildProxies[id].asJSON(function(data){
			console.log(jQuery.parseJSON(data.responseObject()).build.number)
		});
	},
	updateBuildCardFromJSON : function(buildAsJSON, fadeIn) {
		var buildPipeline = this;
		jQuery("#build-" + buildAsJSON.id).empty();
		jQuery(buildPipeline.buildCardTemplate(buildAsJSON)).hide().appendTo("#build-" + buildAsJSON.id).fadeIn(fadeIn ? 1000 : 0);
	},
	updateNextBuildAndShowProgress : function(id, nextBuildNumber, dependencies) {
		var buildPipeline = this;
		//try to get the updated build, that's not pending 
		var intervalId = setInterval(function(){
			buildPipeline.buildProxies[id].updatePipelineBuild(nextBuildNumber, function(updated){
				if (updated.responseObject()) {
					buildPipeline.showProgress(id, dependencies);
					clearInterval(intervalId);
				}
			});
		}, 2000);				
	},
	triggerBuild : function(id, upstreamProjectName, upstreamBuildNumber, triggerProjectName, dependencyIds) {
		var buildPipeline = this;
		buildPipeline.viewProxy.triggerManualBuild(upstreamBuildNumber, triggerProjectName, upstreamProjectName, function(data){
			buildPipeline.updateNextBuildAndShowProgress(id, data.responseObject(), dependencyIds);
		});
	},
	rerunSuccessfulBuild : function(id, buildExternalizableId, dependencyIds) {
		var buildPipeline = this;
		buildPipeline.viewProxy.rerunSuccessfulBuild(buildExternalizableId, function(data){
			buildPipeline.updateNextBuildAndShowProgress(id, data.responseObject(), dependencyIds);
		});
	},
	showSpinner : function(id){
		$("#status-bar-" + id).html('<table class="progress-bar" align="center"><tbody><tr class="unknown"><td></td></tr></tbody></table>');
		$("#icons-" + id).empty();
	},
	fillDialog : function(href, title) {
		$.fancybox({
			type: 'iframe',
			title: title,
			titlePosition: 'over',
			href: href,
			transitionIn : 'elastic',
			transitionOut : 'elastic',
			width: '90%',
			height: '80%'
		});
	},
	closeDialog : function() {
		$.fancybox.close();
	},
	showModalSpinner : function() {
		$.fancybox.showActivity();
	},
	hideModalSpinner : function() {
		$.fancybox.hideActivity();
	}
	
}