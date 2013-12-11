<style type="text/css">
	.ui-autocomplete.ui-menu {
		padding: 0;
		overflow: hidden;
	}
	.ui-autocomplete li {
		list-style: none;
	}

	.ui-autocomplete .ui-menu-item a {
		border-bottom: 1px solid #efefef;
		padding: .5em;
	}

	.ui-autocomplete .ui-menu-item a:hover {
		cursor: pointer;
	}
	.ui-autocomplete .ui-menu-item a:hover .autocomplete-header {
		color: #fff;
	}

	.ui-autocomplete a.ui-state-focus {
		border: 0;
		background: rgb(62, 70, 79);
		color: #fff;
	}
	.ui-autocomplete a.ui-corner-all {
		-webkit-border-radius: 0 0 0 0;
		border-radius: 0 0 0 0;
	}
	.ui-autocomplete .autocomplete-header {
		font-weight: 800;
		color: rgb(123, 34, 64);;
	}

</style>
<script type="text/javascript">

(function($) {
	var initSearchAuto = function() {

		var searchField = $('form input.searchInput');
		var actionUrl = searchField.closest('form').attr('action');
		var searchUrl = $('input.autocompleteUrl')[0].value;

		/**
		 * Helper method for making it a little easier to format the output in the menu
		 * @param  {object} item A JavaScript Object containing the item values
		 * @return {string}      Returns a formatted string that will be injected into the menu
		 */
		var formatOutput = function(item) {
			var output = '<a><span class="autocomplete-header">' + item.label + '</span><br>' + item.desc + '</a>';
			return output;
		}

		searchField.autocomplete({
			minLength: 3,
			source: function(request, response) {
				$.get( actionUrl, { query: request.term, ajax: "true" } )
				.done(function( data ) {
					/**
					 * Make a second AJAX request to get the actual values.
					 */
					$.getJSON( searchUrl, { query: request.term, ajax: true } )
					.done(function ( data ) {
						response($.map(data.results, function (value, key) {
							return {
								label: value.title,
								desc: (value.description === null) ? '' : value.description,
								url: value.url
							};
						}));
					});
				});
			},
			select: function( event, ui ) {
				window.location.href = ui.item.url;
				return false;
			}
		})
		.data( "ui-autocomplete" )
		._renderItem = function( ul, item ) {
			return $( "<li>" )
			.append( formatOutput(item) )
			.appendTo( ul );
		};
	};

	$(document).ready(function() {
		initSearchAuto();
	});
})(searchjQ);

</script>