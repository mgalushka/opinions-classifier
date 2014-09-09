classifier.remote = function () {

	var URL = "http://ec2-54-68-39-246.us-west-2.compute.amazonaws.com:8090";
	var REFRESH_PERIOD = 5000;
  
	// this function calls remote service to retrieve current state of clusters
	var retrieveClusters = function(callback){
		console.log("retrieveClusters");
		$.ajax({ url: URL, 
			success: function(data){
				callback(data);
			}, 
			error: function(data){
				console.log("Error during remote call to: " + URL);
			}, 
			dataType: "json"});
	}
	
	// this function schedules any function to be called
	var schedulePolling = function(callback){
		console.log("schedulePolling in " + REFRESH_PERIOD + "ms.");
		setTimeout(function(){
			retrieveClusters(callback);
		}, REFRESH_PERIOD);		
	}
 
	return {
		retrieveClusters: retrieveClusters,
		schedulePolling: schedulePolling
	}; 
}();

