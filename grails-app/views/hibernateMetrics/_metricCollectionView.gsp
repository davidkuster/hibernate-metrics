

<g:set var="linkName" value="link_${name?.replace(' ','_')}" />
<g:set var="divName" value="div_${name?.replace(' ','_')}" />


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

	<span class="metricsLink" id="${linkName}" title="${output.join(' \n')}">Results (${value.size()})</span>


	<div id="${divName}" style="display:none; max-height:500px;">
		<g:each in="${output}">
			<g:if test="${it instanceof Map.Entry}">
				<pre>${it.key}</pre>
				<pre>${it.value}</pre>
			</g:if>
			<g:else>
				<pre>${it}</pre>
			</g:else>
			<br/>
		</g:each>
	</div>


	<script type="text/javascript">
		$(document).ready( function() {

			$("#${divName}").dialog({
				autoOpen: false,
				height: "auto",
				width: 500,
				maxHeight: 500,
				modal: false,
				title: "${name}",
				buttons: {
					/*"Copy IE" : function() {
						//for IE ONLY!
	    				window.clipboardData.setData('Text','Copied Text');
					},*/
					"Close": function() {
						$("#${divName}").dialog('close');
					}
				}
			});

			$("#${linkName}").click(function() {
				if ( $("#${divName}").is(":visible") )
					$("#${divName}").dialog('close');
				else {
					$("#${divName}").dialog('open');
					$('#${divName}').dialog( 'option', 'position', ['middle', 60] );

				}
			});

		});

	</script>

</g:else>