/**
 * Allows $(function() {}); to be used even without Jquery loaded - This code fragments runs the stored calls.
 * This is useful so we can push all the $(function() { ...}); calls in templates without having to load jquery at head
 */
window.$.noConflict();window.$ = window.$.attachReady(jQuery);


/** CUSTOM SCRIPTS HERE */
/**
* Sets the proper selected option in the navbar
*/
function setNavbar(id) {
    //remove all selectors
    $('#mpoNavBar').find('.menuItem').removeClass('active');

    //set given one
    $("#menu"+id).addClass('active');
}

/**
* Sets the proper selected option in the Admin bar
*/
function setAdminNavbar(id) {
    //remove all selectors
    $("#adminMenuVersion").removeClass("active");
    $("#adminMenuCategories").removeClass("active");
    $("#adminMenuFeatured").removeClass("active");

    //set given one
    $("#adminMenu"+id).addClass("active");
}


/** END OF CUSTOM SCRIPTS */

/**
 * Prevention of window hijack, run after all jquery scripts
 */
$('html').css('display', 'none');
if( self == top ) {
    document.documentElement.style.display = 'block';
} else {
    top.location = self.location;
}


// Bootstrap CSS Fallback, uses a div with bootstrap modal styles that should not be visible if bootstrap has loaded properly.
if ($('.modal.hide').is(':visible') === true) {
    $('<link rel="stylesheet" type="text/css" href="/assets/stylesheets/bootstrap.min.css" />').prependTo('head');
    $('<link rel="stylesheet" type="text/css" href="/assets/stylesheets/bootstrap-responsive.min.css" />').prependTo('head');
}

