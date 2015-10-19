/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2015 The International Cooperation for the Integration of 
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
package org.cip4.bambi.ngui.server;

import java.io.File;

import org.apache.log4j.Logger;
import org.cip4.bambi.core.BambiException;
import org.cip4.bambi.core.BambiServlet;
import org.cip4.bambi.core.MultiDeviceProperties;
import org.cip4.bambi.ngui.server.mockImpl.BambiNotifyReal;
import org.cip4.bambi.ngui.server.mockImpl.MySessionSocketCreator;
import org.cip4.bambi.ngui.server.mockImpl.MyServiceWebSocket;
import org.cip4.bambi.server.BambiFrame;
import org.cip4.bambi.server.BambiServer;
import org.cip4.bambi.server.BambiService;
import org.cip4.bambi.settings.ConfigurationHandler;
import org.cip4.jdflib.util.MyArgs;
import org.cip4.jdflib.util.file.UserDir;
import org.cip4.jdflib.util.logging.LogConfigurator;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class BambiServerNgui extends BambiServer
{
	private static final Logger log = Logger.getLogger(BambiServerNgui.class);

	public BambiServerNgui() throws BambiException
	{
		super();
	}

	@Override
	protected ServletContextHandler createServletHandler()
	{
		ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		contextHandler.setContextPath(context);
		contextHandler.setWelcomeFiles(new String[] { "index.jsp" });
		BambiServlet myServlet = new BambiServlet(this);
		ServletHolder servletHolder = new ServletHolder(myServlet);
		setInitParams(servletHolder);
		contextHandler.addServlet(servletHolder, "/*");
		
		
		log.info("create servlet for /echo");
//		TODO this shall be moved to Bambi-NGUI
		contextHandler.addServlet(StockServiceSocketServlet.class, "/echo");
		
		return contextHandler;
	}

	@Override
	protected void addMoreHandlers(HandlerList handlers)
	{
		System.out.println("handlers.length: " + handlers.getHandlers().length);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/bambi-ngui");
		File warFile = new File(getToolPath() + "/bambi-ngui-1.0.war");
		webapp.setWar(warFile.getAbsolutePath());

		if (null != webapp) {
			handlers.addHandler(webapp);
		}

		log.debug("handlers.length: " + handlers.getHandlers().length);
		System.out.println("handlers.length: " + handlers.getHandlers().length);
	}
	
	public static void main(String[] args) throws Exception
	{
		LogConfigurator.configureLog(new UserDir(BAMBI).getLogPath(), "bambi.log");
		BambiNotifyReal.getInstance();
		ConfigurationHandler.getInstance();
		
		BambiServerNgui bambiServer = new BambiServerNgui();
		LogConfigurator.configureLog(bambiServer.getProp().getBaseDir().getAbsolutePath(), "bambi.log");
		MyArgs myArgs = new MyArgs(args, "c", "p", "");
		if (myArgs.boolParameter('c'))
		{
			BambiService.main(args);
		}
		else
		{
			BambiFrame frame = new BambiFrame(bambiServer);
			int result = frame.waitCompleted();
			ConfigurationHandler.getInstance().saveProperties();
			
			System.exit(result);
		}
	}

	public static class StockServiceSocketServlet extends WebSocketServlet
	{
		@Override
		public void configure(WebSocketServletFactory factory)
		{
			factory.register(MyServiceWebSocket.class);
			log.debug("registered websocket: " + this);
			System.out.println("registered websocket: " + this);

			factory.setCreator(new MySessionSocketCreator());
		}
	}

}
