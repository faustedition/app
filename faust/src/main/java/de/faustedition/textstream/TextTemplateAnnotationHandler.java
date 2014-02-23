package de.faustedition.textstream;

import eu.interedition.text.stream.TextAnnotationStart;

import java.util.Set;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public interface TextTemplateAnnotationHandler {

    boolean start(TextAnnotationStart start, Set<String> classes);

    boolean end(TextAnnotationStart start, Set<String> classes);

}
