/*
 * Copyright (c) 2015 Faust Edition development team.
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

package de.faustedition.transcript.simple;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.ui.ModelMap;
import org.xml.sax.InputSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.faustedition.document.MaterialUnit;
import de.faustedition.json.CompactTextModule;
import de.faustedition.transcript.TranscriptTransformerConfiguration;
import de.faustedition.transcript.input.HandsXMLTransformerModule;
import de.faustedition.transcript.input.StageXMLTransformerModule;
import de.faustedition.transcript.input.WhitespaceXMLTransformerModule;
import eu.interedition.text.Anchor;
import eu.interedition.text.Layer;
import eu.interedition.text.Name;
import eu.interedition.text.TextConstants;
import eu.interedition.text.TextRepository;
import eu.interedition.text.simple.SimpleLayer;
import eu.interedition.text.simple.SimpleTextRepository;
import eu.interedition.text.xml.XMLTransformer;
import eu.interedition.text.xml.XMLTransformerConfigurationBase;
import eu.interedition.text.xml.XMLTransformerModule;
import eu.interedition.text.xml.module.TEIAwareAnnotationXMLTransformerModule;


/**
 * User: moz
 * Date: 27/04/15
 * Time: 9:36 PM
 */
public class SimpleTransform {

	protected static XMLTransformerConfigurationBase<JsonNode> configure(XMLTransformerConfigurationBase<JsonNode> conf, MaterialUnit.Type type) {


		TranscriptTransformerConfiguration.configure(conf);

		List<XMLTransformerModule<JsonNode>> modules = conf.getModules();

		switch (type) {
			case ARCHIVALDOCUMENT:
			case DOCUMENT:
				modules.add(new StageXMLTransformerModule(conf));
				break;
			case PAGE:
				modules.add(new HandsXMLTransformerModule(conf));
				modules.add(new WhitespaceXMLTransformerModule(conf));
				// modules.add(new FacsimilePathXMLTransformerModule(materialUnit));
				break;
			default: break;
		}

		modules.add(new TEIAwareAnnotationXMLTransformerModule<JsonNode>());

		return conf;
	}


	public static void main (String[] args) throws TransformerException, IOException, XMLStreamException {


		StringWriter writer = new StringWriter();
		InputStream input = System.in;
		simpleTransform(input, writer);

		System.out.println (writer);


	}

	public static void simpleTransform(InputStream xmlInput, StringWriter outputWriter) throws TransformerException, IOException, XMLStreamException {
		final TextRepository<JsonNode> textRepository = new SimpleTextRepository<JsonNode>();

		final StringWriter xmlString = new StringWriter();

		TransformerFactory.newInstance().newTransformer().transform(
				new SAXSource(new InputSource(new InputStreamReader(xmlInput, "UTF-8"))),
				new StreamResult(xmlString)
		);

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new CompactTextModule());

		final Layer<JsonNode> sourceLayer = textRepository.add(TextConstants.XML_TARGET_NAME, new StringReader(xmlString.toString()), null, Collections.<Anchor<JsonNode>>emptySet());

		final XMLTransformerConfigurationBase<JsonNode> conf = configure(new XMLTransformerConfigurationBase<JsonNode>(textRepository) {
			@Override
			protected Layer<JsonNode> translate(Name name, Map<Name, String> attributes, Set<Anchor<JsonNode>> anchors) {
				return new SimpleLayer<JsonNode>(name, "", objectMapper.valueToTree(attributes), anchors, null);
			}
		}, MaterialUnit.Type.PAGE);


		final SimpleLayer<JsonNode> transcriptLayer = (SimpleLayer<JsonNode>) new XMLTransformer<JsonNode>(conf).transform(sourceLayer);

		final Map<String, Name> names = Maps.newHashMap();
		final ArrayList<Layer<JsonNode>> annotations = Lists.newArrayList();


		for (Layer<JsonNode> annotation : transcriptLayer.getPorts()) {
			final Name name = annotation.getName();
			names.put(Long.toString(name.hashCode()), name);
			annotations.add(annotation);
		}


		final JsonGenerator jg = objectMapper.getJsonFactory().createJsonGenerator(outputWriter);

		jg.writeObject(new ModelMap()
				.addAttribute("text", transcriptLayer)
				.addAttribute("textContent", transcriptLayer.read())
				.addAttribute("names", names)
				.addAttribute("annotations", annotations));
		jg.flush();
	}
}




