package de.faustedition.tei;

import com.google.inject.Guice;
import de.faustedition.ConfigurationModule;
import de.faustedition.DataStoreModule;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.NodeListWrapper;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import de.faustedition.xml.XPathUtil;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeiTemplater implements Runnable {
	private static final FaustURI TEMPLATE_SOURCE = new FaustURI(FaustAuthority.XML, "/template/tei.xml");

	private final XMLStorage xml;
	private final Logger logger;

    @Inject
    public TeiTemplater(XMLStorage xml, Logger logger) {
        this.xml = xml;
        this.logger = logger;
    }

    @Override
	public void run() {
		try {
			final XPathExpression piXP = XPathUtil.xpath("/processing-instruction()");
			final XPathExpression handNotesXP = XPathUtil.xpath("//tei:teiHeader/tei:profileDesc/tei:handNotes");
			final XPathExpression charDeclXP = XPathUtil.xpath("//tei:teiHeader/tei:encodingDesc/tei:charDecl");

			final Document template = XMLUtil.parse(xml.getInputSource(TEMPLATE_SOURCE));

			final ProcessingInstruction testPi = new NodeListWrapper<ProcessingInstruction>(piXP, template)
					.singleResult(ProcessingInstruction.class);
			final Element testHandNotes = new NodeListWrapper<Element>(handNotesXP, template)
					.singleResult(Element.class);
			final Element testCharDecl = new NodeListWrapper<Element>(charDeclXP, template).singleResult(Element.class);
			if (testPi == null || testHandNotes == null || testCharDecl == null) {
				logger.severe(String.format("Template is incomplete: [%s, %s, %s]", testPi, testHandNotes,
                        testCharDecl));
				return;
			}

			for (FaustURI source : xml.iterate(new FaustURI(FaustAuthority.XML, "/transcript"))) {
				if (!source.getPath().endsWith(".xml")) {
					continue;
				}

				logger.fine("Updating templated sections in " + source);

				try {
					final Document current = XMLUtil.parse(xml.getInputSource(source));
					Element docElement = current.getDocumentElement();
					if (!"TEI".equals(docElement.getLocalName())
							|| !Namespaces.TEI_NS_URI.equals(docElement.getNamespaceURI())) {
						continue;
					}

					for (ProcessingInstruction pi : new NodeListWrapper<ProcessingInstruction>(piXP, current)) {
						pi.getParentNode().removeChild(pi);
					}
					for (ProcessingInstruction pi : new NodeListWrapper<ProcessingInstruction>(piXP, template)) {
						current.insertBefore(current.adoptNode(pi.cloneNode(true)), docElement);
					}
					replaceElements(handNotesXP, current, template);
					replaceElements(charDeclXP, current, template);

					xml.put(source, current);
				} catch (SAXException e) {
					logger.log(Level.FINE, "XML error while templating " + source, e);
				} catch (IOException e) {
                    logger.log(Level.FINE, "I/O error while templating " + source, e);
				} catch (TransformerException e) {
                    logger.log(Level.SEVERE, "XML serialization error while templating " + source, e);
				}
			}
		} catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error while initializing TEI template " + TEMPLATE_SOURCE, e);
		} catch (SAXException e) {
            logger.log(Level.SEVERE, "XML error while initializing TEI template " + TEMPLATE_SOURCE, e);
		} catch (XPathExpressionException e) {
            logger.log(Level.SEVERE, "XPath error while templating TEI documents", e);
		}

	}

	private void replaceElements(XPathExpression xp, Document in, Document from) throws XPathExpressionException {
		final NodeListWrapper<Element> substitutes = new NodeListWrapper<Element>(xp, from);
		for (Element element : new NodeListWrapper<Element>(xp, in)) {
			final Node parent = element.getParentNode();
			for (Element substitute : substitutes) {
				parent.insertBefore(in.adoptNode(substitute.cloneNode(true)), element);
			}
			parent.removeChild(element);
		}
	}

	public static void main(String[] args) {
        Guice.createInjector(new ConfigurationModule(), new DataStoreModule()).getInstance(TeiTemplater.class).run();
    }

}
