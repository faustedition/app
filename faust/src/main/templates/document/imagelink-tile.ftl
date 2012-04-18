<#assign tilePath=(cp + "/static/TILE")>
<#assign path=(cp + "/document/imagelink/" + document.source?replace('faust://xml/document/', ''))>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>TILE: Text-Image Linking Environment</title>
	
	<link rel="stylesheet" href="${tilePath}/skins/columns/css/floatingDiv.css" type="text/css" media="screen, projection" charset="utf-8">
	<link rel="stylesheet" href="${tilePath}/skins/columns/css/style.css" type="text/css" media="screen, projection" charset="utf-8">
	<link rel="stylesheet" href="${tilePath}/skins/columns/css/dialog.css" type="text/css" media="screen, projection" charset="utf-8">
	<link rel="stylesheet" href="${tilePath}/lib/jquery/jquery-ui-1.8.5.custom/css/ui-lightness/jquery-ui-1.8.5.custom.css" type="text/css" media="screen, projection" charset="utf-8">
	<link rel="stylesheet" href="${tilePath}/skins/columns/css/autorec.css" type="text/css" media="screen, projection" charset="utf-8">
	<link rel="stylesheet" href="${tilePath}/lib/jquery/plugins/colorpicker/css/colorpicker.css" type="text/css" />
	
	<script type="text/javascript" src="${tilePath}/lib/jquery/jquery-1.5.1.min.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/jquery/jquery-ui-1.8.5.custom/js/jquery-ui-1.8.5.custom.min.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/jquery/jquery-ui-1.8.5.custom/development-bundle/ui/jquery.ui.mouse.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/jquery/plugins/DataTables-1.7.6/media/js/jquery.dataTables.min.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/jquery/plugins/jgcharts/jgcharts.pack.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/jquery/plugins/jquery.pngFix.pack.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/jquery/jquery.xmlns.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/jquery/plugins/raphael.js" charset="utf-8"></script>
	<script type="text/javascript" src="${tilePath}/lib/rangy/plugins/rangy.googleCode/rangy.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/jquery/plugins/underscore.js"></script> 
	<script type="text/javascript" src="${tilePath}/lib/VectorDrawer_1.0/VectorDrawer.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/main/tile1.0.js"></script> 
	<script type="text/javascript" src="${tilePath}/lib/Plugins/ImageTagger/image_tagger.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/Plugins/Transcript/Transcript.js"></script> 
	<script type="text/javascript" src="${tilePath}/lib/Plugins/Labels/Labels.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/Plugins/TextSelection/textSelection.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/Plugins/AutoLineRecognizer/autorecognizer_plugin.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/jquery/plugins/colorpicker/js/colorpicker.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/jquery/plugins/colorpicker/js/eye.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/jquery/plugins/colorpicker/js/utils.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/rangy/getPath.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/Plugins/Views/views.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/Plugins/LoadTags/loadJSON1.0.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/Plugins/WelcomeDialog/welcomedialog.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/Plugins/ExportDialog/export1.0.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/Plugins/TutorialPlugin/HelloWorld.js"></script>
	<script type="text/javascript" src="${tilePath}/importWidgets/exportJSONXML.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/Plugins/AutoLoad/autoload.js"></script>
	<script type="text/javascript" src="${tilePath}/lib/Plugins/Dashboard/dashboard.js"></script>
</head>
<body>
	<div class="az header"><div class="az logo"><img src="skins/columns/images/tile.gif" alt="TILE: Text-Image Linking Environment" /></div>
	<div id="azglobalmenu" class="az globalmenu">
		<div class="globalbuttons">
			<div class="modeitems"></div>
			<div class="dataitems">
				<div class="menuitem"><a class="button" id="restartall" href="" title="Discard all changes and start over">Start Over</a></div>
			</div>
			<div class="misc">
				<div class="menuitem"><a id="tilehelp" title="Go to the TILE documentation page" href="http://mith.umd.edu/tile/documentation">Documentation</a></div>
				<div class="menuitem"><span class="version">version 0.9</span></div></div>
			</div>
	</div>
	</div>
	<!-- Submit Form for save progress-->
	<form id="inv_SaveProgress_Form" class="submitFormHidden" method="POST" action="${cp}">
		<input id="uploadData" name="uploadData" class="submitFormHidden" type="text"/>
	</form>
	<div class="az main twocol">
		<div id="az_log" class="az log"></div>
		<div id="az_activeBox" class="az activeBox"></div>
		<div id="azcontentarea" class="az content"></div>
	<script>
		// TODO: generate global instance of TILE_ENGINE - needs to be the same name each
		// time so plugins can inherit the variable
		var engine=null;
		//adding AR from autorecognizer_plugin.js
		// set verbose mode either true (on) or false (off)
		__v=false;
		// array of tools to use in engine
		var tools=[];
		
		// automatically set to true if in localhost
		if(/^http:\/\/localhost/.test(document.URL)){
			 __v=true;
			// ADD NEW PLUGIN WRAPPERS FOR LOCAL VERSION HERE
			// adding everything except for Welcome Dialog
			tools=[];
		} else {
			// ADD NEW PLUGIN WRAPPERS HERE
			// adding Image tagger, Labels, Transcript area, Auto-Recognizer, 
			// AutoLoad, Welcome Screen, Loading Dialog, and Export Dialog
			// tools=[IT,Trans,LB,TS,AR,WD,AutoLoad,LoadJSONTILE,ExportTile];
		}
		// security test
		var reg=/.js|javascript:|.php|.JS|.PHP/;
		
		$(function(){
			// Initialize the core functions and objects:
			// (Metadata dialog, TILE_ENGINE, Save dialog, Toolbar)
			engine=new TILE_ENGINE({urls:{
				state: "${path?js_string}",
				remoteState: "${path?js_string}",
				remoteImg: "${path?js_string}"
			}});
			// add plugins
			
			// Image tagger
			engine.insertPlugin(IT);
			// Auto recognizer
			engine.insertPlugin(AR);
			// transcript lines
			engine.insertPlugin(Trans);
			// text selection
			engine.insertPlugin(TS);
			// labels
			engine.insertPlugin(LB);
			// autoload
			engine.insertPlugin(AutoLoad);
			// load dialog
			engine.insertPlugin(LoadDialog);
			// Export dialog
			 engine.insertPlugin(ExportDialog);
			// dashboard
			engine.insertPlugin(Dashboard);
			
			
			if(!(/^http:\/\/localhost/.test(document.URL))){
				// welcome dialog
				engine.insertPlugin(WelcomeDialog);
			}
			// Done adding plugins
		});
	
	</script>
	
</body>
</html>