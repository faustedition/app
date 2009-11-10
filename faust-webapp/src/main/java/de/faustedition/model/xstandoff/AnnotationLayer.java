package de.faustedition.model.xstandoff;

import java.util.Map;

import javax.xml.XMLConstants;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AnnotationLayer extends AnnotationNode
{
	private String id;

	public AnnotationLayer(CorpusData corpusData, AnnotationNode parent)
	{
		super(corpusData, parent, CorpusData.XSTANDOFF_NS_URI, "layer");
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	@Override
	public Node createNode(Node parent, Map<AnnotationSegment, String> segmentIds, Map<String, String> namespacePrefixes)
	{
		Element layerElement = parent.getOwnerDocument().createElementNS(CorpusData.XSTANDOFF_NS_URI, "xsf:layer");
		if (id != null)
		{
			layerElement.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", id);
		}
		return layerElement;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && id != null && obj instanceof AnnotationLayer)
		{
			return id.equals(((AnnotationLayer) obj).id);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return (id == null ? super.hashCode() : id.hashCode());
	}

	@Override
	public AnnotationNode copy()
	{
		return new AnnotationLayer(corpusData, null);
	}
}
