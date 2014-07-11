
<div>

    <div class="metricsSection">
        <label class="metricsTitle">Before/After AJAX calls</label>
        <span id="clearStatsLink" class="metricsLink">Clear Metrics</span>
        <span id="refreshMetricsLink" class="metricsLink">Refresh Metrics</span>
    </div>

	<g:each var="section" in="${session.metrics}">

		<div class="metricsSection metricsData">

			<label class="metricsTitle">${section.key}</label>

            <g:each var="metricGrouping" in="${section.value}">

                <div class="metricsGrouping">

                    <label class="metricsGroupingTitle">${metricGrouping.key}</label>

        			<g:each var="metric" in="${metricGrouping.value}" status="i">

                        <g:if test="${i}"> | </g:if>

    					${metric.key} =

    					<hibernateMetrics:metricDisplay
                            name="${metric.key}"
                            value="${metric.value}" />

                    </g:each>

                </div>

			</g:each>

		</div>

	</g:each>

</div>