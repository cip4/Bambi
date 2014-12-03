package org.cip4.bambi;

import java.awt.EventQueue;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Entrance point of the Bambi application.
 */
public class App
{
	/**
	 * 
	 */
	public App()
	{
		super();
	}

	private class AppRunner implements Runnable
	{
		private final boolean auto;
		private final String context;
		private final String port;

		private AppRunner(boolean auto, String context, String port)
		{
			this.auto = auto;
			this.context = context;
			this.port = port;
		}

		/**
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			try
			{
				ExecutorForm window = new ExecutorForm(context, port, auto);
				window.display();
			}
			catch (Exception e)
			{
				throw new AssertionError(e);
			}
		}
	}

	private static final String OPT_AUTO = "auto";

	private static final String OPT_PORT = "port";

	// private static final String OPT_CONTEXT = "context";

	/**
	 * Application main entrance point.
	 * @param args Application parameter
	 */
	public static void main(String[] args)
	{
		new App().start(args);
	}

	/**
	 * Application main entrance point.
	 * @param args Application parameter
	 */
	public void start(String[] args)
	{

		CommandLineParser parser = new BasicParser();
		CommandLine cmd;

		try
		{
			cmd = parser.parse(createOptions(), args);
		}
		catch (ParseException e)
		{
			throw new AssertionError(e);
		}

		final String port = cmd.getOptionValue(OPT_PORT, "8080");
		// final String context = cmd.getOptionValue(OPT_CONTEXT, "SimWorker");
		final String context = "SimWorker";
		final boolean auto = cmd.hasOption(OPT_AUTO);

		// start application
		EventQueue.invokeLater(new AppRunner(auto, context, port));
	}

	/**
	 * Create the cli options.
	 * @return List of all cli options.
	 */
	private static Options createOptions()
	{

		Options options = new Options();

		options.addOption("a", OPT_AUTO, false, "Start bambi automatically");
		options.addOption("p", OPT_PORT, true, "Port setting");
		// options.addOption("c", OPT_CONTEXT, true, "URL Context");

		return options;
	}
}
