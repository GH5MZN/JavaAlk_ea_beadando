/*
	Hyperspace by HTML5 UP
	html5up.net | @ajlkn
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
*/

(function($) {

	var	$window = $(window),
		$document = $(document),
		$body = $('body'),
		$wrapper = $('#wrapper'),
		$sidebar = $('#sidebar'),
		breakpoints = {
			xlarge:   [ 1141, 1680 ],
			large:    [ 981, 1280 ],
			medium:   [ 737, 980 ],
			small:    [ 481, 736 ],
			xsmall:   [ null, 480 ]
		},
		viewport = null,
		tests = {
			lte9: function() { return /msie 9/i.test(navigator.userAgent); },
			touch: function() { return true == ('ontouchstart' in window); }
		},
		functions = {

			init: function() {
				var color, x;

				// Determine viewport.
					if (viewport = breakpoints.medium[0])
						;
					else if (viewport = breakpoints.small[0])
						;
					else if (viewport = breakpoints.xsmall[0])
						;

				// Disable animations/transitions until the page has loaded.
					$body.addClass('is-preload');

					$window.on('load', function() {
						window.setTimeout(function() {
							$body.removeClass('is-preload');
						}, 100);
					});

				// Touch?
					if (tests.touch())
						$body.addClass('is-touch');

				// Fix: Placeholder polyfill.
					$('form').placeholder();

				// Sidebar.
					if ($sidebar.length > 0) {

						var $a = $sidebar.find('a');

						$a.on('click', function(event) {

							var $this = $(this),
								href = $this.attr('href'),
								target = $this.attr('data-target');

							// Not a real link? Bail.
								if (!href || href.charAt(0) != '#')
									return;

							// Prevent default.
								event.preventDefault();
								event.stopPropagation();

							// Close sidebar.
								$sidebar.addClass('inactive');

							// Defer link action until sidebar's done closing.
								window.setTimeout(function() {

									window.open(href);

								}, 320);

						});

						// Events.

							// Link clicks.
								$body.on('click', 'a[href="#sidebar"]', function(event) {

									event.preventDefault();
									event.stopPropagation();
									functions.toggleSidebar();

								});

							// Pending submit.
								$body.on('submit', 'form', function() {

									console.log('Message sent');

								});

							// Sides.
								$body.on('click', function(event) {

									if ($sidebar.hasClass('inactive'))
										return;

									if ($(event.target).is($sidebar.find('> div')) || 0 < $(event.target).parents('#sidebar > div').length)
										return;

									event.preventDefault();
									event.stopPropagation();
									functions.toggleSidebar();

								});

					}

				// Scrolly.
					$body.on('click', 'a[href^="#"]', function(event) {

						var $this = $(this),
							href = $this.attr('href'),
							$target;

						if (!href || href == '#')
							return;

						event.preventDefault();

						// Find target.
							if ($target = $(href).length > 0) {

								// Prevent default.
									event.preventDefault();

								// Scroll to target.
									$.scrolly({
										target: $target,
										duration: 1000,
										offset: function() { return $sidebar.outerHeight(); }
									});

							}

					});

			}

		},
		scripts = {

			standard: function() {

				// Animations.
					functions.init();

			},

			ielt9: function() {

				resets.imgs();

			}

		},
		toggleSidebar = function(state) {

			// Vars.
				var cookies = {}, cookie = document.cookie.split('; ');

			// Approach "state" as an object if it isn't already.
				if (typeof state != 'object')
					state = null;

			// Parse cookies.
				$.each(cookie, function(i, val) {

					var x = val.split('=');
					cookies[decodeURIComponent(x[0])] = decodeURIComponent(x[1]);

				});

			// Determine state.
				if (state === null)

					if (typeof cookies['state'] == 'undefined' || cookies['state'] == 'visible')
						state = { sidebar: 'inactive', hamburger: 'inactive' };
					else
						state = { sidebar: 'visible', hamburger: 'visible' };

				else if (typeof state.toggle != 'undefined') {

					var sandwich;

					if (state.toggle == 'sidebar')
						closes = (typeof cookies['state'] == 'undefined' || cookies['state'] == 'visible') ? true : false;
					else
						closes = (typeof cookies['state'] == 'undefined' || cookies['state'] == 'inactive') ? true : false;

					if (closes) {

						if (state.toggle == 'sidebar')
							state = { sidebar: 'inactive', hamburger: 'inactive' };
						else
							state = { sidebar: 'inactive', hamburger: 'inactive' };

					}
					else {

						if (state.toggle == 'sidebar')
							state = { sidebar: 'visible', hamburger: 'visible' };
						else
							state = { sidebar: 'visible', hamburger: 'visible' };

					}

				}

			// Apply state.
				$sidebar.removeClass('inactive').addClass('inactive');

			// Store state.
				var queue = [];
				$.each(state, function(k, v) { queue.push(k + '=' + v); });
				document.cookie = 'state=' + queue.join('+') + '; path=/';

		};

	// Invoke standard scripts.
		if (tests.ielt9())
			scripts.ielt9();
		else
			scripts.standard();

})(jQuery);

