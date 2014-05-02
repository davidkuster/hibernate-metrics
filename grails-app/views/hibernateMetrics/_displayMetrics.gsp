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

			<g:each var="metric" in="${section.value}">

				<div class="metricsUnit">

					${metric.key} =

					<hibernateMetrics:metricDisplay
                        name="${metric.key}"
                        value="${metric.value}" />

				</div>

			</g:each>

		</div>

	</g:each>

</div>