$( document ).ready(function() {
	console.log( "ready!" );
	$('.masonry').masonry({
		columnWidth: 60,
		itemSelector: '.item'
	});
	
	var msnry = $('.masonry').data('masonry');
	
	$( ".item" ).on( "click", function() {
		console.log( "click" );
		$(this).toggleClass('gigante');
		msnry.layout();
	});
	
});
