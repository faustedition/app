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

package de.faustedition.xml;

import com.google.common.base.Preconditions;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import org.neo4j.helpers.collection.IterableWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class XMLStorage implements Iterable<FaustURI>, InitializingBean {
	private final Pattern xmlFilenamePattern = Pattern.compile("[^\\.]+\\.[xX][mM][lL]$");

	@Autowired
	private Environment environment;

	private File storageDirectory;
	private String storagePath;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.storageDirectory = environment.getRequiredProperty("xml.home", File.class);
		Preconditions.checkArgument(this.storageDirectory.isDirectory(), storageDirectory.getCanonicalPath() + " is a directory");
		this.storagePath = storageDirectory.getCanonicalPath();
	}

	@Override
	public Iterator<FaustURI> iterator() {
		return iterate(storageDirectory).iterator();
	}

	public Iterable<FaustURI> iterate(FaustURI base) {
		final File baseFile = toFile(base);
		Preconditions.checkArgument(baseFile.isDirectory(), baseFile.getAbsolutePath() + " is not a valid base directory");
		return iterate(baseFile);
	}

	protected Iterable<FaustURI> iterate(File base) {
		Preconditions.checkArgument(isInStore(base), base.getAbsolutePath() + " is not in XML storage");
		final List<File> files = new LinkedList<File>();
		listRecursively(files, base);
		return new FileToUriWrapper(files);
	}

	private void listRecursively(List<File> contents, File base) {
		for (File contained : base.listFiles()) {
			if (contained.isDirectory()) {
				listRecursively(contents, contained);
			} else if (contained.isFile() && xmlFilenamePattern.matcher(contained.getName()).matches()) {
				contents.add(contained);
			}
		}
	}

	public InputSource getInputSource(FaustURI uri) throws IOException {
		InputSource is = new InputSource(uri.toString());
		is.setByteStream(new FileInputStream(toFile(uri)));
		return is;
	}

	public void put(FaustURI uri, Document xml) throws TransformerException {
		XMLUtil.serialize(xml, toFile(uri));
	}

	public FaustURI walk (Deque<String> path) {
		return walk(path, null);
	}
	
	public FaustURI walk(Deque<String> path, Deque<String> walked) throws IllegalArgumentException {
		if (path.size() == 0) {
			return null;
		}

		FaustURI uri = new FaustURI(FaustAuthority.XML, "/");
		while (path.size() > 0) {
			String next = path.pop();
			final FaustURI nextURI = uri.resolve(next);
			if (walked != null)
				walked.add(next);
			if (isDirectory(nextURI)) {
				uri = FaustURI.parse(nextURI.toString() + "/");
			} else if (isResource(nextURI)) {
				uri = nextURI;
				break;
			} else {
				return null;
			}
		}
		return (isResource(uri) ? uri : null);
	}

	public boolean isDirectory(FaustURI uri) {
		final File file = toFile(uri);
		return file.isDirectory() && isInStore(file);
	}

	public boolean isResource(FaustURI uri) {
		final File file = toFile(uri);
		return file.isFile() && isInStore(file) && xmlFilenamePattern.matcher(file.getName()).matches();
	}

	protected File toFile(FaustURI uri) {
		Preconditions.checkArgument(FaustAuthority.XML == uri.getAuthority(), uri + " not valid");
		final File file = new File(storageDirectory, uri.getPath());
		try {
			Preconditions.checkArgument(isInStore(file), file.getCanonicalPath() + " is not in XML storage");
		} catch (IOException e) {
			throw new IllegalArgumentException(file.getAbsolutePath() + " is not in XML storage", e);
		}
		return file;
	}

	protected FaustURI toUri(File file) {
		try {
			final String filePath = file.getCanonicalPath();
			Preconditions.checkArgument(isInStore(file), filePath + " not in XML store");
			return new FaustURI(FaustAuthority.XML, filePath.substring(storagePath.length()));
		} catch (IOException e) {
			throw new IllegalArgumentException(file.getAbsolutePath() + " is not in XML storage", e);
		}
	}

	protected boolean isInStore(File file) {
		try {
			return file.getCanonicalPath().startsWith(storagePath);
		} catch (IOException e) {
			return false;
		}
	}

	private class FileToUriWrapper extends IterableWrapper<FaustURI, File> {

		private FileToUriWrapper(Iterable<File> iterableToWrap) {
			super(iterableToWrap);
		}

		@Override
		protected FaustURI underlyingObjectToObject(File file) {
			return toUri(file);
		}
	}
}
