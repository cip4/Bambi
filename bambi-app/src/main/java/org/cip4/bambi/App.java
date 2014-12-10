package org.cip4.bambi;

import java.awt.EventQueue;

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

		final String port = "8080";
		// final String context = cmd.getOptionValue(OPT_CONTEXT, "SimWorker");
		final String context = "SimWorker";
		final boolean auto = false;

		// start application
		EventQueue.invokeLater(new AppRunner(auto, context, port));
	}

}
