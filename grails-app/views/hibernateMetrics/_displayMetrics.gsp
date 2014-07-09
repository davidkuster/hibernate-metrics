<link rel="stylesheet" href="${resource(dir:'css', file:'hibernate-metrics.css', plugin:'hibernate-metrics')}" type="text/css">

<div>

    <div class="metricsSection">
        Before/After AJAX calls:
        <span id="clearStatsLink" class="metricsLink">Clear Metrics</span>
        <span id="refreshMetricsLink" class="metricsLink">Refresh Metrics</span>
    </div>

	<g:each var="section" in="${session.metrics}">

		<div class="metricsSection metricsData">

			<u>${section.key}</u>

            <g:each var="metricGrouping" in="${section.value}">

                <div class="metricsGrouping">

                    [

                    <label class="metricsGroupingTitle">${metricGrouping.key}:</label>

        			<g:each var="metric" in="${metricGrouping.value}" status="i">

                        <g:if test="${i}"> | </g:if>

    					${metric.key} =

    					<hibernateMetrics:metricDisplay
                            name="${metric.key}"
                            value="${metric.value}" />

                    </g:each>

                    ]

                </div>

			</g:each>

		</div>

	</g:each>

</div>