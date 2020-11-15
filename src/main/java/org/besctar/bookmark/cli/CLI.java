package org.besctar.bookmark.cli;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Scanner;

public class CLI {
    private Scanner in;
    private PrintStream out;

    public CLI(InputStream is, PrintStream out) {
        this.in = new Scanner(is);
        this.out = out;
    }

    public void println(String str, Object... params) {
        if (params.length == 0) {
            out.println(str);
        } else {
            out.println(MessageFormat.format(str, params));
        }
    }

    public int askForNumberInput(String msg, Object... params) {
        println(msg, params);
        while (true) {
            String s = nextLine();
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException ex) {
                println("Entered value is not a number. Please correct.");
            }
        }
    }

    public String askForInput(String msg, Object... params) {
        println(msg, params);
        return nextLine();
    }

    private String nextLine() {
        return in.nextLine();
    }

    public boolean confirm(String message, Object... params) {
        println(message, params);
        return "y".equalsIgnoreCase(in.nextLine());
    }

    private String readPath(boolean ensurePathExists, String message, Object... params) {
        boolean success = false;
        String path = null;
        while (!success) {
            println(message, params);
            path = nextLine();
            try {
                File file = Path.of(path).toFile();
                if (ensurePathExists) {
                    path = file.getAbsolutePath();
                    success = file.exists();
                } else {
                    path = file.getCanonicalPath();
                    success = true;
                }
            } catch (Exception e) {
                success = false;
            }
        }
        return Path.of(path).toString();
    }

    public String initializePathDialog(String initialValue, String name, boolean ensurePathExists) {
        String result = initialValue;
        if (!confirm("Please accept (y) if {0} path is valid for you: {1}", name, initialValue)) {
            result = readPath(ensurePathExists, "Please specify valid path for {0}:", name);
        }
        println("{0} accepted: {1}", name, result);
        return result;
    }
}
