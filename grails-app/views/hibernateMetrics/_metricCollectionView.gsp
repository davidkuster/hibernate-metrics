
<g:if test="${value instanceof Map && value?.size()}">
	<g:set var="output" value="${value.entrySet()}" />
</g:if>
<g:elseif test="${value instanceof List && value?.size()}">
	<g:set var="output" value="${value.flatten().sort()}" />
</g:elseif>
<g:elseif test="${value instanceof String[] && value?.length}">
	<g:set var="output" value="${value.flatten().sort()}" />
</g:elseif>


<g:if test="${! output}">

	N/A

</g:if>
<g:else>

	<g:set var="linkName" value="link_${name?.replace(' ','_')}" />
	<g:set var="divName" value="div_${name?.replace(' ','_')}" />


	<span class="metricsLink"
		id="${linkName}"
		divid="${divName}"
		divtitle="${name}"
		title="${output.join(' \n')}">
			Results (${value.size()})
	</span>

	<div id="${divName}" style="display:none;" class="collectionDialog">
		<g:each in="${output}" var="data" status="i">
			<div class="metricsCollection metricsCollection${i % 2 == 0 ? 'Even' : 'Odd'}">
				<g:if test="${data instanceof Map.Entry}">
					<pre>${data.key}</pre>
					<pre>${data.value}</pre>
				</g:if>
				<g:else>
					<pre>${data}</pre>
				</g:else>
			</div>
		</g:each>
	</div>

</g:else>