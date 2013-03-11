package fr.adele.robusta.internal.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.common.base.Throwables;

public class FileUtils {

    public static BufferedWriter openFile(String fileName, AnsiPrintToolkit toolkit, boolean forceOverwrite) {
        File file = new File(fileName);

        if (!file.exists()) {
            try {
                file.createNewFile();
                // toolkit.white("Opened file: " + fileName);
                // toolkit.eol();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if(forceOverwrite){
                try {
                    toolkit.red("File exists, overwriting (force)");
                    file.delete();
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
            toolkit.red("File exists, please enter new name or erase file");
            return null;
            }
        }

        FileWriter fw;
        BufferedWriter bw = null;

        try {
            fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);

            // bw.write("Opened file: " + fileName);

        } catch (IOException e) {
            toolkit.urgent("Exceeeeptiooooonnn, cannot write file");
            toolkit.urgent(Throwables.getStackTraceAsString(e));
            closeWriter(bw, toolkit);
        }

        // toolkit.white("Got Buffered writer: " + bw);
        // toolkit.eol();

        return bw;
    }

    public static void closeWriter(BufferedWriter bw, AnsiPrintToolkit toolkit) {
        // toolkit.white("Closing Buffered writer: " + bw);
        // toolkit.eol();

        try {
            bw.close();
        } catch (IOException e) {
            toolkit.urgent("Exceeeeptiooooonnn, cannot close file");
            toolkit.urgent(Throwables.getStackTraceAsString(e));
        }
    }

}
