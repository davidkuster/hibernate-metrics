<link rel="stylesheet" href="${resource(dir:'css', file:'hibernate-metrics.css', plugin:'hibernate-metrics')}" type="text/css">

<div>

    <div class="metricsSection">
        Before/After AJAX calls:
        <a id="clearStatsLink" class="metricsLink">Clear Metrics</a>
        <a id="refreshMetricsLink" class="metricsLink">Refresh Metrics</a>
    </div>

	<g:each var="section" in="${session.metrics}">

		<div class="metricsSection">

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