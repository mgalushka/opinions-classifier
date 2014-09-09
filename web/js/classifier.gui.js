var classifier = classifier || {};

classifier.gui = function () {

	var container;
	var msnry;
	var autorefresh = true;
	
	// internal storage for clusters on a screen
	var clusters = {
		all: {},
		add: function(cluster) {
			var cl = {
				id: cluster.id, 			
				label: cluster.label, 			
				message: cluster.message, 			
				url: cluster.url, 			
				image: cluster.image, 		
				marked: true
			};
			clusters.all[cluster.id] = cl;
		},
		remove: function(id) {
			delete clusters.all[id];
		},
		get: function(){
			return all;
		},
		getById: function(id){
			return clusters.all[id];
		},
		reset: function(){
			for (var clusterId in clusters.all) {
				if (clusters.all.hasOwnProperty(clusterId)) {
					var cluster = clusters.all[clusterId];
					cluster.marked = false;
				}
			}			
		},
		clean: function(){
			for (var clusterId in clusters.all) {
				if (clusters.all.hasOwnProperty(clusterId)) {
					var cluster = clusters.all[clusterId];
					if(!cluster.marked){
						console.log("Delete cluster: " + cluster.id);
						delete clusters.all[clusterId];
						msnry.remove($('#' + cluster.id));
						$('#' + cluster.id).remove();
					}	
				}
			}	
		},
		print: function(){
			console.log(clusters.all);
		}
	};
  
	var init = function(){
		console.log("initializing");
		//container = $('.masonry');
		//msnry = container.data('masonry');
		
		var WIDTH = window.innerWidth;
		var colsWidth = Math.max(WIDTH/4, 240);
		container = $('.masonry');
		container.masonry({
			columnWidth: 60,
			itemSelector: '.item'
		});		
		
		msnry = container.data('masonry');
		
		$(".item").on("click", clickHandler);
		
		// assign handler to pause button
		$("#stopBtn").on("click", function() {
			console.log("pause/resume");
			autorefresh = !autorefresh;
			if(autorefresh){
				classifier.remote.retrieveClusters(refreshCallback);
			}
		});	
		
		// init immediate first call to remote function
		classifier.remote.retrieveClusters(refreshCallback);
	}	
	
	// TODO: open associated link with cluster in a new tab
	// TODO: open image if clicked on image
	var clickHandler = function() {
		var url = $(this).data("url");
		if(url !== ""){
			console.log("Opening: " + url);		
			window.open(url);
		}
	}
	
	var createClusterElement = function(id, text, score, url, image) {
		var elem = document.createElement('div');
		elem['id'] = id;
		
		// insert text
		if(image === ""){
			var t = document.createTextNode(text);
			$(elem).append(t); 
		}
		
		// TODO: here size should depend on cluster overall score
		// TODO: this is bad idea, revertign to default size for big screent
		//var widthClass = score > 0.01 ? 'w4' : score > 0.005 ? 'w3' : score > 0.001 ? 'w2' : 'w2';
		var widthClass = 'w4';
		//var heightClass = score > 0.01 ? 'h4' : score > 0.005 ? 'h3' : score > 0.001 ? 'h2' : '';
		
		if(image !== ""){
			// insert image
			$(elem).append($('<div style="display:block;"><div>' + text + '</div><img src="' + image + '" class="img ' + widthClass + '" style="display:block;"/></div>'));
		};

		// assign corresponding class
		elem.className = 'item ' + widthClass;// + ' ' + heightClass;
		if(url !== ""){
			$(elem).data("url", url);
			$(elem).on("click", clickHandler);
		}
		return elem;
	}
	
	var insertIntoContainer = function(clusterObj){
		if($('.item').length > 0){
			$('.item').first().before(clusterObj);
		}
		else{
			container.append(clusterObj);
		}	
	}
	
	// this refreshes screen presentation based on data received
	var refresh = function(data){
		// we skip if update is empty - maybe error on server?
		if(data && data.clusters && data.clusters.length === 0) {
			console.warn("Received empty clusters list. Check server side. No refresh.");
			return;
		}
		console.log("Received = "  + data.clusters.length + " clusters");
		clusters.print();
		//var container = $('.masonry');
		
		// iterate over elements and if cluster exists - refresh with updated relative weight? TODO: this is not good idea.
		// TODO: Need to think how we will work on different devices with resizing issues, etc...
		// if missed - create new
		// TODO: all existing clusters which are not in passed object should be removed from screen
		var added_elems = [];
		var removed_elems = [];
		var updated_clusters = data.clusters;
		var total = data.size;
		
		clusters.reset();
		
		for (var i = 0; i < updated_clusters.length; i++) {
			var cluster = updated_clusters[i];
			var existing = clusters.getById(cluster.id);
			// TODO: we want to keep disappeared clusters for history.
			// TODO: we need to keep disappeared clusters at the end
			if(existing){
				// TODO: resize depending on score???
				console.log("Update existing cluster: " + existing.id + " " + existing.label + " to [" + cluster.id + "] " + cluster.label);
				var relative_score = (cluster.score/total).toFixed(2);
				// we only delete in case if messages are really different
				if(existing.message !== cluster.message){
					msnry.remove($('#' + cluster.id));
					$('#' + cluster.id).remove();
					var updated_cluster = createClusterElement(cluster.id, cluster.message, relative_score, cluster.url, cluster.image);
					
					// insert updated cluster to container
					insertIntoContainer(updated_cluster);
					added_elems.push(updated_cluster);
				}
				existing.marked = true;
			}
			else{
				// create new
				console.log("Create new cluster: " + cluster.label);
				var relative_score = (cluster.score/total).toFixed(2);
				var new_cluster = createClusterElement(cluster.id, cluster.message, relative_score, cluster.url, cluster.image);
				
				// insert new cluster to container
				insertIntoContainer(new_cluster);
				added_elems.push(new_cluster);
				clusters.add(cluster);
			}
		}
		
		clusters.clean();
		
		// add and lay out newly prepended elements
		msnry.prepended(added_elems);
		msnry.layout();
		//msnry.remove(removed_elems);
		clusters.print();
		console.log("completed layout processing");
	}
	
	var refreshCallback = function(data){
		console.log("refreshCallback");
		if(data && data.clusters){
			console.log("Received = "  + data.clusters.length + " clusters");
			refresh(data);
			if(autorefresh){
				classifier.remote.schedulePolling(refreshCallback);
			}
		}
	}
 
	return {
		init: init
	}; 
}();

