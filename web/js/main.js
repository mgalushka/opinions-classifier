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
	
});

(function poll(){
	setTimeout(function(){
	  $.ajax({ url: "http://localhost", 
		success: function(data){
			poll();
		}, 
		error: function(data){
			poll();
		}, 
		dataType: "json"});
	}, 10000);
})();
