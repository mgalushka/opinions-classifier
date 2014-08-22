$( document ).ready(function() {
	console.log( "ready!" );
	$('.masonry').masonry({
		columnWidth: 60,
		itemSelector: '.item'
	});
	
	var msnry = $('.masonry').data('masonry');
	
	$( ".item" ).on( "click", function() {
		console.log( "click" );
		//$(this).toggleClass('gigante');
		var w = $(this).width();
		$(this).width(w + 60);
		msnry.layout();
	});
	
	
	(function poll(){
		setTimeout(function(){
		  $.ajax({ url: "http://localhost:8080", success: function(data){
			//Update your dashboard gauge
			//salesGauge.setValue(data.value);

			//Setup the next poll recursively
			poll();
		  }, dataType: "json"});
		}, 30000);
	})();
});
