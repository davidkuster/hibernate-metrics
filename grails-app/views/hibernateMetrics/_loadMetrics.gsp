<script type='text/javascript'>
    $(document).ready( function() {
        /*if ( $().jquery >= 1.7 ) {
            $("#clearStatsLink").on( 'click', function() { clearStats(); } );
            $("#refreshMetricsLink").on( 'click', function() { getMetrics(); } );
        }
        else {*/
            $("#clearStatsLink").live( 'click', function() { clearStats(); } );
            $("#refreshMetricsLink").live( 'click', function() { getMetrics(); } );
        //}

        getMetrics();
    });

    function getMetrics() {
        $.ajax({
            url: "${g.createLink(controller:'hibernateMetrics', action:'ajaxDisplayMetrics')}",
            type: "POST",
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
</script>

<div id="metrics">

</div>