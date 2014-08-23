$( document ).ready(function() {
	console.log( "ready!" );
	$('.masonry').masonry({
		columnWidth: 60,
		itemSelector: '.item'
	});
	
	var msnry = $('.masonry').data('masonry');
	
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
		elem.background = '#de3';
		$(elem).on("click", clickHandler);
		return elem;
	}

	function prepend(){
		var container = $('.masonry');
		//var msnry = $('.masonry').data('masonry');
		// create new item elements
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
		  $.ajax({ url: "http://localhost", 
			success: function(data){
				prepend();
				poll();
			}, 
			error: function(data){
				prepend();
				poll();
			}, 
			dataType: "json"});
		}, 5000);
	};
	
	poll();
	
});




