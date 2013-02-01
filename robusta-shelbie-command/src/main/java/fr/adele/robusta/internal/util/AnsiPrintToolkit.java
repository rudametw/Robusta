/**
 * Copyright 2010 OW2 Shelbie
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.adele.robusta.internal.util;

import org.fusesource.jansi.Ansi;

/**
 * Simple toolkit to ease manipulation of the ANSI buffer. TODO, maybe this can be refactored as a service (API +
 * Implementation) ...
 */
public class AnsiPrintToolkit {

	/**
	 * Default indentation.
	 */
	private static final String DEFAULT_INDENTER = "  ";

	/**
	 * Ansi buffer.
	 */
	private Ansi buffer;

	/**
	 * Verbosity mode.
	 */
	private boolean verbose = false;

	/**
	 * Indentation value.
	 */
	private String indenter = DEFAULT_INDENTER;

	public AnsiPrintToolkit() {
		this(Ansi.ansi());
	}

	public AnsiPrintToolkit(Ansi ansi) {
		this.buffer = ansi;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public Ansi getBuffer() {
		return buffer;
	}

	public String getIndenter() {
		return indenter;
	}

	public void setIndenter(String indenter) {
		this.indenter = indenter;
	}

	//
	// public void printElement(int level, Element element) {
	// indent(level);
	// // element ns:name in bold
	// buffer.a(Ansi.Attribute.INTENSITY_BOLD);
	// String ns = element.getNameSpace();
	// if (verbose && !isEmpty(ns)) {
	// buffer.a(ns);
	// buffer.a(":");
	// }
	// buffer.a(element.getName());
	// buffer.a(Ansi.Attribute.INTENSITY_BOLD_OFF);
	//
	// // Then print attributes
	// if (element.getAttributes() != null) {
	// for (Attribute attribute : element.getAttributes()) {
	// printAttribute(attribute);
	// }
	// }
	//
	// // Now print childs Element (incrementing the indentation counter)
	// if (element.getElements() != null) {
	// for (Element child : element.getElements()) {
	// // EOL
	// eol();
	// printElement((level + 1), child);
	// }
	// }
	//
	// }
	//
	// public void printAttribute(Attribute attribute) {
	//
	// // First, a separator
	// buffer.a(" ");
	//
	// // Then the namespace (if verbose)
	// String ns = attribute.getNameSpace();
	// if (verbose && !isEmpty(ns)) {
	// buffer.a(ns);
	// buffer.a(":");
	// }
	//
	// // The print the key/value pair
	// buffer.a(attribute.getName());
	// buffer.a("=\"");
	// buffer.a(Ansi.Attribute.ITALIC);
	// buffer.a(attribute.getValue());
	// buffer.a(Ansi.Attribute.ITALIC_OFF);
	// buffer.a("\"");
	//
	// }

	public static boolean isEmpty(String value) {
		return ((value == null) || ("".equals(value)));
	}

	public void indent() {
		indent(1);
	}

	public void indent(int level) {
		for (int i = 0; i < level; i++) {
			buffer.a(indenter);
		}
	}

	public void eol() {
		eol(1);
	}

	public void eol(int level) {
		for (int i = 0; i < level; i++) {
			buffer.a('\n');
		}
	}

	public void red(String message) {
		color(message, Ansi.Color.RED);
	}

	public void green(String message) {
		color(message, Ansi.Color.GREEN);
	}

	public void blue(String message) {
		color(message, Ansi.Color.BLUE);
	}

	public void white(String message) {
		color(message, Ansi.Color.WHITE);
	}

	public void black(String message) {
		color(message, Ansi.Color.BLACK);
	}

	public void cyan(String message) {
		color(message, Ansi.Color.CYAN);
	}

	public void yellow(String message) {
		color(message, Ansi.Color.YELLOW);
	}

	public void magenta(String message) {
		color(message, Ansi.Color.MAGENTA);
	}

	public void color(String message, Ansi.Color color) {
		buffer.fg(color);
		buffer.a(message);
		buffer.fg(Ansi.Color.DEFAULT);
	}

	public void italic(String message) {
		buffer.a(Ansi.Attribute.ITALIC);
		buffer.a(message);
		buffer.a(Ansi.Attribute.ITALIC_OFF);
	}

	public void bold(String message) {
		buffer.a(Ansi.Attribute.INTENSITY_BOLD);
		buffer.a(message);
		buffer.a(Ansi.Attribute.INTENSITY_BOLD_OFF);
	}

	public void underline(String message) {
		buffer.a(Ansi.Attribute.UNDERLINE);
		buffer.a(message);
		buffer.a(Ansi.Attribute.UNDERLINE_OFF);
	}

	public void print(String message) {
		buffer.a(message);
	}

	public void separator() {
		buffer.fg(Ansi.Color.YELLOW);
		bold(":");
		buffer.fg(Ansi.Color.DEFAULT);
	}
	public void title(String message) {
		buffer.fg(Ansi.Color.BLUE);
		bold(message);
		buffer.fg(Ansi.Color.DEFAULT);
		eol();
	}


	public void bold(int message) {
		buffer.a(Ansi.Attribute.INTENSITY_BOLD);
		buffer.a(message);
		buffer.a(Ansi.Attribute.INTENSITY_BOLD_OFF);
	}
}
