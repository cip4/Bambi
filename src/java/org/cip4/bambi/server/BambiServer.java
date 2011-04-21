package org.cip4.bambi.server;

/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2009 The International Cooperation for the Integration of 
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
import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.cip4.bambi.core.BambiException;
import org.cip4.bambi.core.BambiServlet;
import org.cip4.bambi.core.MultiDeviceProperties;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.util.MyArgs;
import org.cip4.jdfutility.server.JettyServer;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * standalone app for bambi using an embedded jetty server
 * @author rainer prosi
 * @date Dec 9, 2010
 */
public final class BambiServer extends JettyServer
{

	/**
	 * @throws BambiException if config file is not readable
	 */
	public BambiServer() throws BambiException
	{
		super();
		BasicConfigurator.configure();
		File configFile = new File("config/devices.xml");
		MultiDeviceProperties mp = new MultiDeviceProperties(new File("."), null, configFile);
		KElement root = mp.getRoot();
		if (root == null)
		{
			String logString;
			if (configFile.exists())
				logString = "corrupt config file at :" + configFile.getAbsolutePath();
			else
				logString = "cannot find config file at :" + configFile.getAbsolutePath();
			log.fatal(logString);
			throw new BambiException(logString);
		}
		int iport = getJettyPort(root);
		setPort(iport);
		setContext(root.getAttribute("Context", null, null));
		if (context == null || "".equals(context))
		{
			String logString = "no context specified for servlet, bailing out";
			log.fatal(logString);
			throw new BambiException(logString);
		}
		log.info("starting BambiServer at context: " + context + " port: " + getPort());
	}

	private int getJettyPort(KElement root)
	{
		int iport = root.getIntAttribute("JettyPort", null, -1);
		if (iport == -1)
			iport = root.getIntAttribute("Port", null, -1);
		return iport;
	}

	/**
	 * 
	 *  
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		BambiServer bambiServer = new BambiServer();
		MyArgs myArgs = new MyArgs(args, "c", "p", "");
		if (myArgs.boolParameter('c'))
		{
			BambiConsole console = new BambiConsole(bambiServer, myArgs);
		}
		else
		{
			BambiFrame frame = new BambiFrame(bambiServer);
			System.exit(frame.waitCompleted());
		}
	}

	@Override
	protected ServletContextHandler createServletHandler()
	{
		ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		contextHandler.setContextPath(context);
		contextHandler.setWelcomeFiles(new String[] { "index.jsp" });
		BambiServlet myServlet = new BambiServlet();
		ServletHolder servletHolder = new ServletHolder(myServlet);
		servletHolder.setInitParameter("bambiDump", "/bambidump" + context);
		contextHandler.addServlet(servletHolder, "/*");
		return contextHandler;
	}

	/**
	 * @see org.cip4.jdfutility.server.JettyServer#getHome()
	 */
	@Override
	protected String getHome()
	{
		return context + "/overview";
	}

}
