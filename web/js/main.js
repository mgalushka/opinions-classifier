$( document ).ready(function() {
	console.log( "ready!" );
	$('.masonry').masonry({
		columnWidth: 60,
		itemSelector: '.item'
	});
	
	var autorefresh = true;
	
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
	
	//var clusters = {};
	
	var msnry = $('.masonry').data('masonry');
	
	// TODO: open associated link with cluster in a new tab
	var clickHandler = function() {
		//var w = $(this).width();
		//$(this).width(w + 60);
		//msnry.layout();
		var url = $(this).data("url");
		if(url !== ""){
			console.log("Opening: " + url);		
			window.open(url);
		}
	}
	
	$(".item").on("click", clickHandler);
	
	$("#stopBtn").on("click", function() {
		console.log("pause/resume");
		autorefresh = !autorefresh;
		console.log("refreshing = " + autorefresh);
		poll();
	});
	
	function createClusterElement(id, text, score, url, image) {
		var elem = document.createElement('div');
		elem['id'] = id;
		
		//var documentFragment = $(document.createDocumentFragment());
		
		// insert text
		if(image === ""){
			var t = document.createTextNode(text);
			$(elem).append(t); 
		}
		
		// TODO: here size should depend on cluster overall score
		var widthClass = score > 0.01 ? 'w4' : score > 0.005 ? 'w3' : score > 0.001 ? 'w2' : 'w2';
		//var heightClass = score > 0.01 ? 'h4' : score > 0.005 ? 'h3' : score > 0.001 ? 'h2' : '';
		
		if(image !== ""){
			// insert image
			$(elem).append($('<div style="display:block;"><div>' + text + '</div><img src="' + image + '" class="img ' + widthClass + '" style="display:block;"/></div>'));
		};
		
		//console.info(documentFragment);
		//$(elem).append(documentFragment);
		
		// assign corresponding class
		elem.className = 'item ' + widthClass;// + ' ' + heightClass;
		if(url !== ""){
			$(elem).data("url", url);
			$(elem).on("click", clickHandler);
		}
		return elem;
	}

	function refresh(data){
		clusters.print();
		var container = $('.masonry');
		// iterate over elements and if cluster exists - refresh with updated relative weight
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
			// TODO: cleanup clusters which disappeared!
			if(existing){
				// TODO: resize depending on score
				console.log("Update existing cluster: " + existing.id + " " + existing.label + " to [" + cluster.id + "] " + cluster.label);
				var relative_score = (cluster.score/total).toFixed(2);
				//var updated_cluster = createClusterElement(cluster.id, cluster.message, relative_score, cluster.url, cluster.image);
				//$('#' + cluster.id).html("");
				//$('#' + cluster.id).append(updated_cluster);
				//added_elems.push(new_cluster);
				//removed_elems.push($('#' + cluster.id));
				msnry.remove($('#' + cluster.id));
				$('#' + cluster.id).remove();
				var updated_cluster = createClusterElement(cluster.id, cluster.message, relative_score, cluster.url, cluster.image);
				$('.item').first().before(updated_cluster);
				added_elems.push(updated_cluster);
				existing.marked = true;
			}
			else{
				// create new
				console.log("Create new cluster: " + cluster.label);
				var relative_score = (cluster.score/total).toFixed(2);
				var new_cluster = createClusterElement(cluster.id, cluster.message, relative_score, cluster.url, cluster.image);
				// prepend new cluster element to container
				$('.item').first().before(new_cluster);
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
	
	function poll(){
		setTimeout(function(){
		  $.ajax({ url: "http://localhost:8090", 
			success: function(data){
				console.log(data.clusters.length);
				refresh(data);
				if(autorefresh){
					poll();
				}
			}, 
			error: function(data){
				console.log(data.clusters.length);
				refresh(data);
				if(autorefresh){
					poll();
				}
			}, 
			dataType: "json"});
		}, 5000);
	};
	
	poll();
	
});




