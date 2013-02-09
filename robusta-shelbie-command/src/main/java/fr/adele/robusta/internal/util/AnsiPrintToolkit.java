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
 * Simple toolkit to ease manipulation of the ANSI buffer. TODO, maybe this can
 * be refactored as a service (API + Implementation) ...
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

    public AnsiPrintToolkit(final Ansi ansi) {
        buffer = ansi;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    public Ansi getBuffer() {
        return buffer;
    }

    public String getIndenter() {
        return indenter;
    }

    public void setIndenter(final String indenter) {
        this.indenter = indenter;
    }

    public static boolean isEmpty(final String value) {
        return ((value == null) || ("".equals(value)));
    }

    public void indent() {
        indent(1);
    }

    public void indent(final int level) {
        for (int i = 0; i < level; i++) {
            buffer.a(indenter);
        }
    }

    public void indentWithMarker(final int level, final int length) {
        if (level <= 0) return;
        for (int i = 0; i < level - 1; i++) {
            indent(length);
            white("│");
        }
        // indent(1);
        indent(length);
        white("├");
        white("───");
    }

    public void eol() {
        eol(1);
    }

    public void eol(final int level) {
        for (int i = 0; i < level; i++) {
            buffer.a('\n');
        }
    }

    public void red(final String message) {
        color(message, Ansi.Color.RED);
    }

    public void green(final String message) {
        color(message, Ansi.Color.GREEN);
    }

    public void blue(final String message) {
        color(message, Ansi.Color.BLUE);
    }

    public void white(final String message) {
        color(message, Ansi.Color.WHITE);
    }

    public void black(final String message) {
        color(message, Ansi.Color.BLACK);
    }

    public void cyan(final String message) {
        color(message, Ansi.Color.CYAN);
    }

    public void yellow(final String message) {
        color(message, Ansi.Color.YELLOW);
    }

    public void magenta(final String message) {
        color(message, Ansi.Color.MAGENTA);
    }

    public void color(final String message, final Ansi.Color color) {
        buffer.fg(color);
        buffer.a(message);
        buffer.fg(Ansi.Color.DEFAULT);
    }

    public void italic(final String message) {
        buffer.a(Ansi.Attribute.ITALIC);
        buffer.a(message);
        buffer.a(Ansi.Attribute.ITALIC_OFF);
    }

    public void bold(final String message) {
        buffer.a(Ansi.Attribute.INTENSITY_BOLD);
        buffer.a(message);
        buffer.a(Ansi.Attribute.INTENSITY_BOLD_OFF);
    }

    public void underline(final String message) {
        buffer.a(Ansi.Attribute.UNDERLINE);
        buffer.a(message);
        buffer.a(Ansi.Attribute.UNDERLINE_OFF);
    }

    public void print(final String message) {
        buffer.a(message);
    }

    public void separator() {
        buffer.fg(Ansi.Color.YELLOW);
        bold(":");
        buffer.fg(Ansi.Color.DEFAULT);
    }

    public void pad(final String message, final int repetitions) {
        for (int i = 0; i < repetitions; i++) {
            bold("*");
        }
    }

    public void title(final String message) {
        final String padding = "*****";
        final int INDENT_SIZE = 4;
        final int length = (2 * padding.length()) + message.length() + (2 * INDENT_SIZE * DEFAULT_INDENTER.length());

        buffer.fg(Ansi.Color.BLUE);
        eol();
        pad("*", length);
        eol();
        bold(padding);
        indent(INDENT_SIZE);
        bold(message);
        indent(INDENT_SIZE);
        bold(padding);
        eol();
        pad("*", length);
        eol();
        buffer.fg(Ansi.Color.DEFAULT);
    }

    public void subtitle(final String message) {
        final String padding = "***";
        final int INDENT_SIZE = 1;

        buffer.fg(Ansi.Color.BLUE);
        bold(padding);
        indent(INDENT_SIZE);
        bold(message);
        indent(INDENT_SIZE);
        bold(padding);
        eol();
        buffer.fg(Ansi.Color.DEFAULT);
    }

    public void urgent(final String message) {
        buffer.bgBright(Ansi.Color.RED);
        buffer.fg(Ansi.Color.WHITE);
        bold(message);
        buffer.fg(Ansi.Color.DEFAULT);
        buffer.bg(Ansi.Color.DEFAULT);
        eol();
    }

    public void bold(final int message) {
        buffer.a(Ansi.Attribute.INTENSITY_BOLD);
        buffer.a(message);
        buffer.a(Ansi.Attribute.INTENSITY_BOLD_OFF);
    }

    public static String padRight(final String s, final int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(final String s, final int n) {
        return String.format("%1$" + n + "s", s);
    }

    public void debug(final String string) {
        yellow("DEBUG: " + string);
        eol();
    }

}
