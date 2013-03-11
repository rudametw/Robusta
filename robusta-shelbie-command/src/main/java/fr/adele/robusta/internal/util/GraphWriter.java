package fr.adele.robusta.internal.util;

import java.io.BufferedWriter;
import java.io.IOException;

import com.google.common.base.Throwables;

public class GraphWriter {

    BufferedWriter bw;

    final AnsiPrintToolkit toolkit;

    final String fileName;

    public GraphWriter(String file, AnsiPrintToolkit kit) {
        super();
        toolkit = kit;
        fileName = file;
    }

    public boolean open(boolean forceOverwrite) {
        bw = FileUtils.openFile(fileName, toolkit, forceOverwrite);
        if (bw == null) {
            return false;
        } else {
            start();
            return true;
        }
    }

    public void close() {
        end();
        FileUtils.closeWriter(bw, toolkit);
    }

    private void writeln(String text) {
        write(text + "\n");
    }

    private void write(String text) {
        // toolkit.white("Going to write: "+text+"   to: " + bw);
        // toolkit.eol();

        try {
            bw.write(text);
        } catch (IOException e) {
            toolkit.urgent("Cannot write");
            toolkit.urgent(Throwables.getStackTraceAsString(e));
        }
    }

    public void start() {
        writeln("digraph G {");
    }

    public void end() {
        writeln("}");
    }

    public void writeEdge(final String source, final String dest) {
        writeln("    " + "\"" + source + "\"" + " -> " + "\"" + dest + "\"");
    }

    public void writeNode(final String node) {
        writeln("    " + "\"" + node + "\"");
    }

}
