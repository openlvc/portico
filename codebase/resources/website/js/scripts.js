(function($){

	// Declare global variables
	var map,
		mapLatLng;
	
	$(window).on('load', function(){

		// Remove loading indicator
		setTimeout(function(){
			$('#preload-content').fadeOut(400, function(){
				$('#preload').fadeOut(800);				
				$('.fadeInLeft, .fadeInRight').addClass('animate');
			});
		}, 400);

	});

	$(document).ready( function(){

		// Create a countdown instance. Change the launchDay according to your needs.
		// The month ranges from 0 to 11. I specify the month from 1 to 12 and manually subtract the 1.
		// Thus the launchDay below denotes 7 December, 2014.
		var launchDay = new Date(2014, 6, 14);
		$('#countdown-timer').countdown({
			until: launchDay,
			format: 'DHMS'
		});

		// Add background image
		$('.left-wrap .bg').backstretch('images/bg.jpg');

		// Invoke the Placeholder plugin
		$('input, textarea').placeholder();

		// Validate newsletter form
		$('<div class="spinner"><div class="square"></div><div class="square"></div><div class="square"></div><div class="square"></div></div>').hide().appendTo('.newsletter');
		$('<div class="success"></div>').hide().appendTo('.newsletter');
		$('#newsletter-form').validate({
			rules: {
				newsletter_email: { required: true, email: true }
			},
			messages: {
				newsletter_email: {
					required: 'Email address is required',
					email: 'Email address is not valid'
				}
			},
			errorElement: 'span',
			errorPlacement: function(error, element){
				error.appendTo(element.parent());
			},
			submitHandler: function(form){
				$(form).hide();
				$('.newsletter').find('.spinner').css({ opacity: 0 }).show().animate({ opacity: 1 });
				$.post($(form).attr('action'), $(form).serialize(), function(data){
					$('.newsletter').find('.spinner').animate({opacity: 0}, function(){
						$(this).hide();
						$('.newsletter').find('.success').show().html('<i class="icon ion-ios7-checkmark-outline"></i> Thank you for subscribing!').animate({opacity: 1});
					});
				});
				return false;
			}
		});

		// Add tabs functionality to the right side content
		jgtContentTabs();

		// Load the Google Map object
		if ( $('#map-canvas').length > 0 ) {
			jgtGoogleMap();
		}
		
		// Set the minimum height for the right side and bind on resize or orientation change
		jgtMinHeight();
		$(window).bind('resize orientationchange', function(){
			jgtMinHeight();
		});

	});

	// Set the minimum height for the right side
	function jgtMinHeight(){
		var leftWrap = $('.left-wrap'),
			rightWrap = $('.right-wrap');

		if ( Modernizr.mq('only screen and (max-width: 1200px)') == true ) {
			rightWrap.css({ 'min-height': $(window).height() - leftWrap.height() });
		} else {
			rightWrap.removeAttr('style');
		}
	}

	// Add tabs functionality to the right side content
	function jgtContentTabs(){
		var tabsNav = $('#menu'),
			tabsWrap = $('#main');

		tabsWrap.find('.main-section:gt(0)').hide();
		tabsNav.find('li:first').addClass('active');
		tabsNav.find('a').click(function(e){
			tabsWrap.find('.main-section').hide();
			tabsWrap.find($($(this).attr('href'))).fadeIn(800);
			tabsNav.find('li').removeClass('active');
			$(this).parent().addClass('active');
			e.preventDefault();

			// Fix the map in the hidden div
			if ( $('#map-canvas').length > 0 ) {
				google.maps.event.trigger(map, 'resize');
				map.setCenter(mapLatLng);
			}

			// Fix background
			$('.left-wrap .bg').backstretch('resize');

		});
	}

	// Create and initialize the Google Map object
	function jgtGoogleMap(){

		// Change the default values according to your needs.
		// mapLat and mapLong represents the latitude and longitude coordinates of your location.
		// mapInfo represents the info which is displayed when the marker is clicked
		var mapLat = -31.952707,
			mapLong = 115.85766,
			mapInfo = '<div id="content">Portico HQ @ Calytrix Technologies</div>';

		// Create a LatLng object
		mapLatLng = new google.maps.LatLng(mapLat, mapLong);

		// Create a Map options object
		var mapOptions = {
			zoom: 14,
			center: mapLatLng,
			mapTypeId: google.maps.MapTypeId.ROADMAP
		};
		
		// Create a new map instance
		map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);

		// Center the map when the window resizes 
		google.maps.event.addDomListener(window, 'resize', function() {
			map.setCenter(mapLatLng);
		});

		// Add an info window and a marker. Open info window when the marker is clicked
		var infowindow = new google.maps.InfoWindow({
	   		content: mapInfo
		});
		var marker = new google.maps.Marker({
			position: mapLatLng,
			map: map
		});
		google.maps.event.addListener(marker, 'click', function() {
			infowindow.open(map, marker);
		});

	}

})(jQuery);
