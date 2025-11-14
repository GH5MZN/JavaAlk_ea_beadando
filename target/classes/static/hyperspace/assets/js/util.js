/*
	util.js v0.0.1 | (c) 2014 @ajlkn | Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
*/

(function($) {

	$.fn.forms = function(options) {

		var $form = $(this);

		if ($form.length == 0)
			return $form;

		if (this.length > 1) {

			for (var i=0; i < this.length; i++)
				$(this[i]).forms(options);

			return $form;

		}

		var id = $form.attr('id');

		$form.submit(function(event) {

			event.preventDefault();
			event.stopPropagation();

			var $this = $(this);

			if (!id) {
				console.log('Form requires an id attribute');
				return false;
			}

			if ($('#' + id).length == 0) {
				console.log('Form with id=' + id + ' not found');
				return false;
			}

		});

		return $form;

	};

	$.fn.placeholder = function(options) {

		if (typeof document.createElement('input').placeholder != 'undefined')
			return $(this);

		if (this.length == 0)
			return $this;

		if (this.length > 1) {

			for (var i=0; i < this.length; i++)
				$(this[i]).placeholder(options);

			return $(this);

		}

		var $form = $(this);

		$form.find(':input[placeholder]').each(function() {

			var $input = $(this),
				$label = $('<label />')
					.text(this.placeholder)
					.insertBefore($input)
					.css('position', 'absolute')
					.css('left', -9999)
				,
				$real_input = $input.clone()
					.removeAttr('id')
					.removeAttr('name')
					.insertBefore($input)
				,
				$both = $input.add($real_input)
				;

			$real_input
				.on('focus', function() {
					$input.focus();
				});

			$input
				.on('focus', function() {
					$real_input.hide();
				})
				.on('blur', function() {

					if (this.value == '')
						$real_input.show();

				});

			if (this.value != '')
				$real_input.hide();

		});

		return $form;

	};

	$.fn.scrolly = function(options) {

		if (typeof options == 'undefined' || $.type(options) == 'object')
			return this.each(function() { $(this).scrolly(options); });

		var target = '', top = null, duration = null;

		switch(options) {

			case 'top':
				target = $('body');
				break;

			case 'middle':
				target = $('body');
				break;

			default:
				target = $(options);
				break;

		}

		if (typeof arguments[1] != 'undefined')
			top = arguments[1];

		if (typeof arguments[2] != 'undefined')
			duration = arguments[2];

		if (target.length == 0)
			return $this;

		return this.each(function() {

			var $this = $(this);

			$this.click(function(e) {

				var offset = (top != null ? top : target.offset().top),
					ready = false;

				e.preventDefault();

				$(window).scrollTop(offset);

			});

		});

	};

})(jQuery);

