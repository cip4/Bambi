/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2020 The International Cooperation for the Integration of
 * Processes in  Prepress, Press and Postpress (CIP4).  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        The International Cooperation for the Integration of
 *        Processes in  Prepress, Press and Postpress (www.cip4.org)"
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of
 *    Processes in  Prepress, Press and Postpress" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4",
 *    nor may "CIP4" appear in their name, without prior written
 *    permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For
 * details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR
 * THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the The International Cooperation for the Integration
 * of Processes in Prepress, Press and Postpress and was
 * originally based on software
 * copyright (c) 1999-2001, Heidelberger Druckmaschinen AG
 * copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.bambi.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.BambiException;
import org.cip4.bambi.core.BambiServlet;
import org.cip4.bambi.core.MultiDeviceProperties;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MyArgs;
import org.cip4.jdflib.util.file.UserDir;
import org.cip4.jdflib.util.logging.LogConfigurator;
import org.cip4.jdfutility.server.JettyServer;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * standalone app for bambi using an embedded jetty server
 * @author rainer prosi
 * @date Dec 9, 2010
 */
public class BambiServer extends JettyServer
{
	public static final String BAMBI = "bambi";
	public static final String RESOURCES_FILE = "/list.txt";

	final MultiDeviceProperties mp;
	protected BambiServlet myServlet;

	/**
	 * @return the myServlet
	 */
	public BambiServlet getServlet()
	{
		return myServlet;
	}

	/**
	 *
	 */
	public static BambiServer getBambiServer()
	{
		if (theServer == null)
		{
			try
			{
				theServer = new BambiServer();
			}
			catch (final BambiException e)
			{
				LogFactory.getLog(BambiServer.class).fatal("Cannot create bambi server", e);
			}
		}
		return (BambiServer) theServer;
	}

	/**
	 * @throws BambiException if config file is not readable
	 */
	public BambiServer() throws BambiException
	{
		super();
		final UserDir userDir = getUserDir();
		final String toolPath = getToolPath();
		final File toolDir = new File(toolPath);
		final MultiDeviceProperties mpTmp;
		if (MultiDeviceProperties.getXMLDoc(toolDir) != null)
		{
			log.info("loading local file from: " + toolDir.getAbsolutePath());
			mpTmp = MultiDeviceProperties.getProperties(toolDir, null);
		}
		else
		{
			log.info("loading resource file from class: " + getClass().getSimpleName());
			final InputStream resourceAsStream = getClass().getResourceAsStream("/config/devices.xml");
			if (resourceAsStream == null)
			{
				log.fatal("invalid resource stream ");
			}
			mpTmp = MultiDeviceProperties.getProperties(resourceAsStream);
		}
		mp = mpTmp;

		unpackResourceList(userDir);

		final KElement root = mp.getRoot();
		if (root == null)
		{
			final String logString;
			final File configFile = MultiDeviceProperties.getConfigFile(toolDir);
			if (configFile.exists())
			{
				logString = "corrupt config file at :" + configFile.getAbsolutePath();
			}
			else
			{
				logString = "cannot find config file at :" + configFile.getAbsolutePath();
			}
			log.fatal(logString);
			throw new BambiException(logString);
		}
		final int iport = mp.getPort();
		setPort(iport);
		final int sslPort = mp.getSSLPort();
		if (sslPort > 0)
		{
			setSSLPort(sslPort, null);
		}

		setContext(root.getAttribute("Context", null, null));
		if (context == null || "".equals(context))
		{
			final String logString = "no context specified for servlet, bailing out";
			log.fatal(logString);
			throw new BambiException(logString);
		}
		log.info("starting BambiServer at context: " + context + " port: " + getPort());
	}

	/**
	 *
	 *
	 * @return
	 */
	public String getToolPath()
	{
		final UserDir userDir = getUserDir();
		return userDir == null ? "." : userDir.getToolPath();
	}

	/**
	 *
	 * @return
	 */
	protected UserDir getUserDir()
	{
		return new UserDir(BAMBI);
	}

	/**
	 * grab list of resources from self
	 * @param userDir
	 */
	protected void unpackResourceList(final UserDir userDir)
	{
		if (userDir == null)
		{
			log.info("no user directory specified, bailing out");
			return;
		}
		final File toolDir = new File(userDir.getToolPath());
		File listTxt = new File(RESOURCES_FILE);
		listTxt = FileUtil.getFileInDirectory(toolDir, listTxt);
		if (!listTxt.canRead())
		{
			final Class<? extends BambiServer> myClass = getClass();
			final InputStream listStream = myClass.getResourceAsStream(RESOURCES_FILE);
			if (listStream == null)
			{
				log.error("No list found - cannot unpack resources");
			}
			else
			{
				final BufferedReader r = new BufferedReader(new InputStreamReader(listStream));
				String line = null;
				try
				{
					while ((line = r.readLine()) != null)
					{
						if (line.isEmpty())
							continue;

						final InputStream nextStream = myClass.getResourceAsStream(line);
						if (nextStream != null)
						{
							File toFile = new File(line.substring(1));
							toFile = FileUtil.getFileInDirectory(toolDir, toFile);
							log.info("Streaming resource file " + toFile.getAbsolutePath());
							final File newFile = FileUtil.streamToFile(nextStream, toFile);
							if (newFile != null)
							{
								log.info("Streamed resource file " + newFile.getAbsolutePath());
							}
							else
							{
								log.warn("Cannot stream resource file " + toFile.getAbsolutePath());
							}
						}
						else
						{
							log.warn("no stream for resource file " + line);
						}
					}
				}
				catch (final IOException e)
				{
					line = null;
				}
			}
		}
		else
		{
			log.info("list.txt already extracted at: " + userDir.getToolPath());
		}
	}

	/**
	 *
	 * @return
	 */
	@Override
	protected int getDefaultPort()
	{
		return 8080;
	}

	/**
	 *
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception
	{
		LogConfigurator.configureLog(new UserDir(BAMBI).getLogPath(), "bambi.log");
		final Log log = LogFactory.getLog(BambiServer.class);
		log.info("BambiServer");
		final BambiServer bambiServer = new BambiServer();
		bambiServer.getProp().setBaseDir(new File(new UserDir(BAMBI).toString()));
		LogConfigurator.configureLog(bambiServer.getProp().getBaseDir().getAbsolutePath(), "bambi.log");
		final MyArgs myArgs = new MyArgs(args, "c", "ps", "");
		if (myArgs.boolParameter('c'))
		{
			BambiService.main(args);
		}
		else
		{
			final BambiFrame frame = new BambiFrame(bambiServer);
			System.exit(frame.waitCompleted());
		}
	}

	/**
	 *
	 * @see org.cip4.jdfutility.server.JettyServer#createServletHandler()
	 */
	@Override
	protected ServletContextHandler createServletHandler()
	{
		final ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		contextHandler.setContextPath(context);
		contextHandler.setWelcomeFiles(new String[] { "index.jsp" });
		myServlet = new BambiServlet(this);
		final ServletHolder servletHolder = new ServletHolder(myServlet);
		setInitParams(servletHolder);
		contextHandler.addServlet(servletHolder, "/*");
		return contextHandler;
	}

	/**
	 *
	 * overwrite this to set some more params
	 * @param servletHolder
	 */
	protected void setInitParams(final ServletHolder servletHolder)
	{
		servletHolder.setInitParameter("bambiDump", "bambidump" + context);
	}

	/**
	 * @see org.cip4.jdfutility.server.JettyServer#getHome()
	 */
	@Override
	protected String getHome()
	{
		return context + "/overview";
	}

	/**
	 *
	 * @return
	 */
	public MultiDeviceProperties getProp()
	{
		return mp;
	}

	@Override
	protected ResourceHandler createResourceHandler()
	{
		final ResourceHandler resourceHandler = new MyResourceHandler(context);
		resourceHandler.setResourceBase(getToolPath());
		return resourceHandler;
	}

	@Override
	public void setPort(final int port)
	{
		super.setPort(port);
		if (mp != null)
			mp.setPort(port);
	}

}
