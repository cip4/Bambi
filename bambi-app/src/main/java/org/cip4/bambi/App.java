package org.cip4.bambi;

import org.apache.commons.cli.*;

import java.awt.*;

/**
 * Entrance point of the Bambi application.
 */
public class App {

    private static final String OPT_AUTO = "auto";

    private static final String OPT_PORT = "port";

    // private static final String OPT_CONTEXT = "context";


    /**
     * Application main entrance point.
     * @param args Application parameter
     */
    public static void main(String[] args) {

        CommandLineParser parser = new BasicParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(createOptions(), args);
        } catch (ParseException e) {
            throw new AssertionError(e);
        }



        final String port = cmd.getOptionValue(OPT_PORT, "8080");
        // final String context = cmd.getOptionValue(OPT_CONTEXT, "SimWorker");
        final String context = "SimWorker";
        final boolean auto = cmd.hasOption(OPT_AUTO);

        // start application
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ExecutorForm window = new ExecutorForm(context, port, auto);
                    window.display();
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
        });
    }

    /**
     * Create the cli options.
     * @return List of all cli options.
     */
    private static Options createOptions() {

        Options options = new Options();

        options.addOption("a", OPT_AUTO, false, "Start bambi automatically");
        options.addOption("p", OPT_PORT, true, "Port setting");
        // options.addOption("c", OPT_CONTEXT, true, "URL Context");

        return options;
    }
}
