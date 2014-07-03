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

package de.faustedition;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import net.middell.combo.*;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class ComboResourceFinder extends Finder implements InitializingBean {

	@Autowired
	private Environment environment;

	private TextResourceResolver resolver;

	@Override
	public void afterPropertiesSet() throws Exception {
		final String contextPath = environment.getRequiredProperty("ctx.path");
		final File staticHome = environment.getRequiredProperty("static.home", File.class);

		resolver = new TextResourceResolver();
		FileBasedTextResourceCollection.register(resolver, "yui3/", new File(staticHome, "yui3"), contextPath + "/static/yui3", Charset.forName("UTF-8"), 86400);
		FileBasedTextResourceCollection.register(resolver, "css/", new File(staticHome, "css"), contextPath + "/static/css/", Charset.forName("UTF-8"), 0);
		FileBasedTextResourceCollection.register(resolver, "js/", new File(staticHome, "js"), contextPath + "/static/js/", Charset.forName("UTF-8"), 0);
	}

	@Override
	public ServerResource find(Request request, Response response) {
		return new ComboResource();
	}

	private class ComboResource extends ServerResource {

		@Override
		protected Representation get() throws ResourceException {
			return new ComboRepresentation(resolver.resolve(extractPaths()));
		}
		protected Iterable<String> extractPaths() {
			final List<String> parameters = Arrays.asList(Objects.firstNonNull(getOriginalRef().getQuery(true), "").split("\\&"));
			return Iterables.filter(Iterables.transform(parameters, new Function<String, String>() {

				@Override
				public String apply(String input) {
					final int equalsIndex = input.indexOf('=');
					final String parameterName = (equalsIndex < 0 ? input : input.substring(0, equalsIndex));
					return Strings.emptyToNull(parameterName.trim());
				}
			}), Predicates.notNull());
		}

	}
	private class ComboRepresentation extends WriterRepresentation {

		private final TextResourceCombo combo;

		private ComboRepresentation(TextResourceCombo combo) {
			super(new MediaType(combo.getMediaType()));
			setModificationDate(new Date(combo.lastModified()));
			setExpirationDate(new Date(System.currentTimeMillis() + combo.maxAge()));
			this.combo = combo;
		}
		@Override
		public void write(Writer writer) throws IOException {
			CharStreams.copy(combo, writer);
		}

	}

	private class ReferenceBasedTextResourceCollection extends TextResourceCollection {
		private final Reference base;

		public ReferenceBasedTextResourceCollection(Reference base, String sourceURI, Charset charset, long maxAge) {
			super(sourceURI, charset, maxAge);
			this.base = base;
		}

		@Override
		public TextResource resolve(String relativePath) throws IOException, IllegalArgumentException {
			final ClientResource resource = new ClientResource(getContext(), new Reference(base, relativePath).getTargetRef());
			return new TextResource(new InputSupplier<InputStream>() {
				@Override
				public InputStream getInput() throws IOException {
					return resource.get().getStream();
				}
			}, source.resolve(relativePath), charset, resource.head().getModificationDate().getTime(), maxAge);
		}
	}
}
