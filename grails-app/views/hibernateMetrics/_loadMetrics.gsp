<link rel="stylesheet" href="${resource(dir:'css', file:'hibernate-metrics.css', plugin:'hibernate-metrics')}" type="text/css">


<script type='text/javascript'>
    $(document).ready( function() {

        // TODO: figure out how this should play nice with different versions of jquery
        // TODO: should probably use js apply method & parameterize "on" vs "live"
        var jqueryVersion = $().jquery;
        //console.log( "jquery version = [" + jqueryVersion + "]" );

        // check old versions of jquery
        if ( startsWith(jqueryVersion, "1.3")
                || startsWith(jqueryVersion, "1.4")
                || startsWith(jqueryVersion, "1.5")
                || startsWith(jqueryVersion, "1.6") ) {
            $("#clearStatsLink").live( 'click', function() { clearStats(); } );
            $("#refreshMetricsLink").live( 'click', function() { getMetrics(1); } );
            $(".metricsLink").live( 'click', function(event) {
                displayMetricsDialog( $(event.target) );
            } );
        }
        // otherwise assume it's 1.7 or newer and supports .on()
        else {
            $("#metrics").on( 'click', '#clearStatsLink', function() { clearStats(); } );
            $("#metrics").on( 'click', '#refreshMetricsLink', function() { getMetrics(1); } );
            $("#metrics").on( 'click', '.metricsLink', function(event) {
                displayMetricsDialog( $(event.target) );
            } );
        }

        getMetrics(0);
    });

    function startsWith(str, s) {
        return str.slice(0, s.length) == s;
    }

    function getMetrics(loadNum) {
        $.ajax({
            url: "${g.createLink(controller:'hibernateMetrics', action:'ajaxDisplayMetrics')}",
            type: "POST",
            data: { loadNum:loadNum },
            success: function(data) {
                $("#metrics").html( data );
            },
            error: function(data) {
                $("#metrics").html( "Failed to load metrics data: " + data );
            },
            dataType: "html",
            async: true
        });
    }

    function clearStats() {
        $.ajax({
            url: "${g.createLink(controller:'hibernateMetrics', action:'ajaxClearStats')}",
            type: "GET",
            success: function(data) {
                //$("#metrics").html( data );
                $(".metricsData").html("");
            },
            error: function(data) {
                //$("#metrics").html( "Failed to clear metrics data: " + data );
                $(".metricsData").html("");
            },
            dataType: "json",
            async: true
        });
    }

    function displayMetricsDialog(item) {
        var divId = item.attr("divid");
        var divTitle = item.attr("divtitle");
        var selector = $("#"+divId);
        //console.log(selector);

        if ( selector.is(":visible") ) {
            selector.dialog('close');
        }
        else {
            initDialog(selector, divTitle);
            selector.dialog('open');
            selector.dialog( 'option', 'position', ['middle', 60] );
        }
    }

    function initDialog(selector, divTitle) {
        if ( ! selector.is(":data(dialog)") ) {
            selector.dialog({
                autoOpen: false,
                height: "auto",
                width: 550,
                maxHeight: 750,
                modal: false,
                title: divTitle,
                buttons: {
                    /*"Copy IE" : function() {
                        //for IE ONLY!
                        window.clipboardData.setData('Text','Copied Text');
                    },*/
                    "Close": function() {
                        selector.dialog('close');
                    }
                }
            });
        }
    }
</script>


<div id="metrics"></div>