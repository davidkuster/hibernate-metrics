<script type='text/javascript'>
    $(document).ready( function() {

        console.log( "jquery version = " + $().jquery );

        if ( $().jquery == "1.8.3" ) {
            $("#clearStatsLink").on( 'click', function() { clearStats(); } );
            $("#refreshMetricsLink").on( 'click', function() { getMetrics(); } );
        }
        else {
            $("#clearStatsLink").live( 'click', function() { clearStats(); } );
            $("#refreshMetricsLink").live( 'click', function() { getMetrics(1); } );
        }

        getMetrics(0);
    });

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
</script>

<div id="metrics">

</div>