<#ftl ns_prefixes={ "D":"http://www.faustedition.net/ns" }>
<#import "snippets.ftl" as snippets />
<#assign title = message("menu.archives")>
<#assign css>
	#archives_map { text-align: center; margin: 2em auto; width: 100%; height: 400px }
	#archives { margin: 2em 0em; border-collapse: collapse; }
	.archive-row {border-bottom: 1px solid #ccc }
	.archive-container { padding: 1em 0em }
</#assign>
<#assign header>
	<script type="text/javascript" src="https://maps.google.com/maps/api/js?sensor=false"></script>
	<script type="text/javascript" src="${cp}/static/js/archive.js"></script>
</#assign>
<@faust.page title=title css=css header=header menuhighlight="archives">
	<div id="archives">
		<#list archives.archive as a>
			<#if (a_index % 3) == 0><div class="yui3-g archive-row"></#if>
			<div class="yui3-u-1-3">
			<div class="archive-container">
				<p>${a_index + 1}.<br><a href="${cp}/archive/${(a.@id)?url}">${a.name?html}</a></p>
				<@snippets.archiveData a false />
			</div>
			</div>
			<#if ((a_index % 3) == 2) || !a_has_next></div></#if>
		</#list>		
	</div>
</@faust.page>