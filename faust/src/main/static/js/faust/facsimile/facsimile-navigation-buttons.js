/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

YUI.add('facsimile-navigation-buttons', function (Y) {

	var SVG_NS = "http://www.w3.org/2000/svg";

    var FacsimileNavigationButtons = Y.Base.create("facsimile-navigation-buttons", Y.Base, [], {

        initializer: function(config) {
			var navigationPanel = Y.Node.create(
				'<div>' +
					'<button class="pure-button button-opaque" id="button_zoom_in"><i class="icon-zoom-in"></i></button>' +
					'<button class="pure-button button-opaque" id="button_zoom_out"><i class="icon-zoom-out"></i></button>' +
					'<button class="pure-button button-opaque" id="button_left"><i class="icon-arrow-left"></i></button>' +
					'<button class="pure-button button-opaque" id="button_up"><i class="icon-arrow-up"></i></button>' +
					'<button class="pure-button button-opaque" id="button_down"><i class="icon-arrow-down"></i></button>' +
					'<button class="pure-button button-opaque" id="button_right"><i class="icon-arrow-right"></i></button>' +
					'</div>');

			config.host.get('contentBox').append(navigationPanel);

			navigationPanel.one('#button_zoom_in').on('click', function(e){
				config.host.model.zoom(-1);
				e.stopPropagation();
			});

			navigationPanel.one('#button_zoom_out').on('click', function(e){
				config.host.model.zoom(1);
				e.stopPropagation();
			});

			navigationPanel.one('#button_left').on('click', function(e){
				var moveX = Math.floor(config.host.model.get('view').width / 4);
				config.host.model.pan(-moveX, 0);
				e.stopPropagation();
			});

			navigationPanel.one('#button_right').on('click', function(e){
				var moveX = Math.floor(config.host.model.get('view').width / 4);
				config.host.model.pan(moveX, 0);
				e.stopPropagation();
			});

			navigationPanel.one('#button_up').on('click', function(e){
				var moveY = Math.floor(config.host.model.get('view').height / 4);
				config.host.model.pan(0, -moveY);
				e.stopPropagation();
			});

			navigationPanel.one('#button_down').on('click', function(e){
				var moveY = Math.floor(config.host.model.get('view').height / 4);
				config.host.model.pan(0, moveY);
				e.stopPropagation();
			});


			navigationPanel.setStyles({
				top: '1em',
				right: '1em',
				position: 'absolute'
			});

		},
        destructor: function() {
        }
    }, {
		NAME : 'facsimileNavigationButtons',
		NS : 'navigationButtons',
        ATTRS: {
            view: {},
            model: {}
        }
    });

    Y.mix(Y.namespace("Faust"), { FacsimileNavigationButtons: FacsimileNavigationButtons });

}, '0.0', {
	requires: ['facsimile', 'plugin', 'svg-utils']
});