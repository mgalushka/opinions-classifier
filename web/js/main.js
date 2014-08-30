$( document ).ready(function() {
	console.log( "ready!" );
	$('.masonry').masonry({
		columnWidth: 60,
		itemSelector: '.item'
	});
	
	var clusters = {};
	
	var msnry = $('.masonry').data('masonry');
	
	// TODO: open associated link with cluster in a new tab
	var clickHandler = function() {
		console.log( "click" );
		//$(this).toggleClass('gigante');
		var w = $(this).width();
		$(this).width(w + 60);
		msnry.layout();
	}
	
	$(".item").on("click", clickHandler);
	
	function getItemElement() {
		var elem = document.createElement('div');
		var wRand = Math.random();
		var hRand = Math.random();
		var widthClass = wRand > 0.92 ? 'w4' : wRand > 0.84 ? 'w3' : wRand > 0.65 ? 'w2' : '';
		var heightClass = hRand > 0.85 ? 'h4' : hRand > 0.6 ? 'h3' : hRand > 0.35 ? 'h2' : '';
		elem.className = 'item ' + widthClass + ' ' + heightClass;
		$(elem).on("click", clickHandler);
		return elem;
	}

	function refresh(data){
		var container = $('.masonry');
		// iterate over elements and if cluster exists - refresh with updated relative weight
		// if missed - create new
		// TODO: all existing clusters which are not in passed object should be removed from screen
		var elems = [];
		var fragment = document.createDocumentFragment();
		for ( var i = 0; i < 3; i++ ) {
		  var elem = getItemElement();
		  fragment.appendChild( elem );
		  elems.push( elem );
		}
		// prepend elements to container
		$('.item').first().before(fragment);
		// add and lay out newly prepended elements
		msnry.prepended( elems );
	    //msnry.layout();
		console.log( "prepended" );
	}
	
	function poll(){
		setTimeout(function(){
		  $.ajax({ url: "http://localhost:8090", 
			success: function(data){
				console.log(data.clusters.length);
				refresh(data);
				poll();
			}, 
			error: function(data){
				console.log(data.clusters.length);
				refresh(data);
				poll();
			}, 
			dataType: "json"});
		}, 60000);
	};
	
	poll();
	
});




