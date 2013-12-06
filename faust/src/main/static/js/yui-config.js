YUI.GlobalConfig = {
    debug: false,
    combine: false,
    comboBase: cp + '/resources?',
    root: 'yui3/build/',
    groups: {
        faust: {
            base: cp + '/static/js/',
            combine: false,
            comboBase: cp + '/resources?',
            filter: "raw",
            root: 'js/',
            modules: {
                'adhoc-tree': { path: 'faust/text-annotation/adhoc-tree.js'},
                'facsimile': { path: 'faust/facsimile/facsimile.js'},
                'facsimile-svgpane': { path: 'faust/facsimile/facsimile-svgpane.js' },
                'facsimile-highlightpane': { path: 'faust/facsimile/facsimile-highlightpane.js' },
                'util': { path: "faust/util/util.js"},
				'document-adhoc-tree' : { path: 'faust/document/document-adhoc-tree.js' },
	            'document-app' : { path: 'faust/document/document-app.js' },
				'document-configuration-faust' : { path: 'faust/document/document-configuration-faust.js' },
				'document-yui-view' : { path: 'faust/document/document-yui-view.js' },
				'document-structure-view' : { path: 'faust/document/document-structure-view.js' },
				"transcript-view-svg" : { path: 'faust/transcript/transcript-view-svg.js' },
				"transcript-model" : { path: 'faust/transcript/transcript-model.js' },
				'document-text' : { path: 'faust/document/document-text.js' },
				'materialunit' : { path: 'faust/document/materialunit.js' },
                'protovis': { path: "protovis-r3.2.js"},
                'search': { path: "faust/search/search.js"},
				'svg-utils' : { path: "faust/svg-utils/svg-utils.js"},
                'text-annotation': { path: "faust/text-annotation/text-annotation.js"},
				'text-display': { path: "faust/text-display/text-display.js"},
                'text-index': { path: 'faust/text-annotation/text-index.js'}
            }
        }
    }
};
