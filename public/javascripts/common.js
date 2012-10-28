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
    $('#adminNavBar').find(".adminMenuItem").removeClass("active");

    //set given one
    $("#adminMenu"+id).addClass("active");
}

/**
Sets the values for the modal on admin pages
- modalId: id fo the modal, used to show/hide
- modalTitle: the title to display
- valuesMap: a map of {key:value} where key is the name of an input control in the modal and the value the value to set it to
- onSuccess: an optional method to execute when the ajax call returns successfully, usually to update the UI with the new data
- isRemoval will be true if we are doing a destructive operation
*/
function setModal(modalId, modalTitle, valuesMap, onSuccess, isRemoval){
    // Check the optional parameter
    onSuccess = typeof onSuccess !== 'undefined' ? onSuccess : function(data, textStatus, jqXHR){ };
    isRemoval = typeof onSuccess !== 'undefined' ? isRemoval : false;

    //Set modal values
    $('#'+modalId).find('#modalTitle').text(modalTitle);

    //Set values from map
    for (var key in valuesMap) {
        if($("#"+modalId+" input[name='"+key+"']").length > 0){
            $("#"+modalId+" input[name='"+key+"']").val(valuesMap[key]);
        } else if($("#"+modalId+" textarea[name='"+key+"']").length > 0){
            $("#"+modalId+" textarea[name='"+key+"']").text(valuesMap[key]);
        }
    }

    //set the post call for the modal
    var form = $('#'+modalId+' form');
    $('#'+modalId).find('#submitModalForm').on("click", function(event){
        $.post(form.attr('action'), form.serialize())
            .success( function(data, textStatus, jqXHR) {
                for (var key in data) {
                    $('#' + key + '-' + data['id']).text(data[key]);
                }
                onSuccess(data, textStatus, jqXHR);
                $('#'+modalId).modal('hide');
            })
            .error( function(jqXHR, textStatus, errorThrown) {
                var messageJson = JSON.parse(jqXHR.responseText);
                var message = '<strong>Errors:</strong><br><ul>';
                for (var key in messageJson) {
                    message += ('<li><strong>'+key + ':</strong> ' + messageJson[key] + '</li>');
                }
                message += '</ul>';
                $('#messageArea').html(message);
            });
    });

    if(isRemoval){
        var button = $('#'+modalId).find('#submitModalForm');
        button.addClass('btn-danger');
        button.text('Delete');
    }

    //Modal properly set, show data
    $('#'+modalId).modal('show');
}


/**
Sets the values for the modal to delete elements on admin pages
- modalId: id fo the modal, used to show/hide
- modalTitle: the title to display
- valuesMap: a map of {key:value} where key is the name of an input control in the modal and the value the value to set it to
- onSuccess: an optional method to execute when the ajax call returns successfully, usually to update the UI with the new data
*/
function setDeleteModal(modalId, modalTitle, valuesMap, onSuccess){
    onSuccess = typeof onSuccess !== 'undefined' ? onSuccess : function(data, textStatus, jqXHR){ };
    setModal(modalId, modalTitle, valuesMap, onSuccess, true);
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

