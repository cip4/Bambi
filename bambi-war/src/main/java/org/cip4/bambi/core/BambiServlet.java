/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2018 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This product includes software developed by the The International Cooperation for
 * the Integration of Processes in Prepress, Press and Postpress (www.cip4.org)" Alternately, this acknowledgment may appear in the software itself, if and wherever such third-party acknowledgments
 * normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of Processes in Prepress, Press and Postpress" must not be used to endorse or promote products derived from this software
 * without prior written permission. For written permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4", nor may "CIP4" appear in their name, without prior written permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE. ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals on behalf of the The International Cooperation for the Integration of Processes in Prepress, Press and Postpress and was
 * originally based on software copyright (c) 1999-2001, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */

package org.cip4.bambi.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.server.BambiServer;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 * Entrance point for Bambi servlets note that most processing has been moved to the servlet independent class @see BambiContainer
 *
 * @author boegerni
 *
 */
public final class BambiServlet extends HttpServlet
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1926504814491033980L;

	/**
	 *
	 * @param bambiServer
	 */
	public BambiServlet(final BambiServer bambiServer)
	{
		super();
		log = LogFactory.getLog(getClass());
		BambiContainer.getCreateInstance();
		theServer = bambiServer;
	}

	final Log log;
	private boolean dumpGet = false;
	private boolean dumpEmpty = false;
	private DumpDir bambiDumpIn = null;
	private DumpDir bambiDumpOut = null;
	private final BambiServer theServer;

	/**
	 * Initializes the servlet.
	 *
	 * @throws ServletException
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException
	{
		super.init(config);
		final String baseURL = getContextPath();

		final String realPath = theServer.getToolPath();
		final File baseDir = new File(realPath);
		log.info("Initializing Bambi servlet for " + baseURL + " at " + realPath);
		final String dump = initializeDumps(config, baseDir);
		final BambiContainer container = BambiContainer.getCreateInstance();
		container.loadProperties(baseDir, baseURL, dump);
	}

	/**
	 *
	 * getContextPath is new in servlet api 2.5
	 *
	 * @return the context
	 */
	protected String getContextPath()
	{
		final ServletContext context = getServletConfig().getServletContext();
		String baseURL;
		if (context.getMajorVersion() <= 2 && context.getMinorVersion() < 6)
		{
			baseURL = context.getServletContextName();
		}
		else
		{
			baseURL = context.getContextPath();
		}
		if (baseURL.startsWith("/"))
		{
			baseURL = baseURL.substring(1);
		}
		return baseURL;
	}

	private String initializeDumps(final ServletConfig config, final File baseDir)
	{
		String dump = System.getProperty("bambiDump");
		if (dump == null)
		{
			dump = config.getInitParameter("bambiDump");
			log.info("retrieving bambidump from servlet config: " + dump);
		}
		else
		{
			log.info("retrieving bambidump from java property: " + dump);
		}
		dump = parseEnv(dump);
		if (dump == null)
		{
			log.info("not initializing http dump directory: ");
		}
		else
		{
			final File dumpFile;
			if (UrlUtil.isRelativeURL(dump) && dump.indexOf(":\\") < 0)
				dumpFile = FileUtil.getFileInDirectory(baseDir, new File(dump));
			else
				dumpFile = new File(dump);
			dump = dumpFile.getAbsolutePath();

			log.info("initializing http dump directory: " + dump);
			bambiDumpIn = new DumpDir(FileUtil.getFileInDirectory(dumpFile, new File("in")));
			bambiDumpOut = new DumpDir(FileUtil.getFileInDirectory(dumpFile, new File("out")));
			final String iniDumpGet = config.getInitParameter("bambiDumpGet");
			dumpGet = StringUtil.parseBoolean(iniDumpGet, false);
			final String iniDumpEmpty = config.getInitParameter("bambiDumpEmpty");
			dumpEmpty = StringUtil.parseBoolean(iniDumpEmpty, false);
			log.info("initializing http dump directory: " + dump + " get=" + dumpGet + " empty=" + dumpEmpty);
		}
		return dump;
	}

	/**
	 * parse a string for environment variables
	 *
	 * @param dump
	 * @return
	 */
	public static String parseEnv(String dump)
	{
		dump = StringUtil.getNonEmpty(dump);
		if (dump == null || !dump.startsWith("%"))
			return dump;
		int posS = dump.indexOf('/');
		int posB = dump.indexOf('\\');
		if (posS < 0)
			posS = 999999;
		if (posB < 0)
			posB = 999999;
		if (posS > posB)
			posS = posB;
		if (posS == 999999)
			return dump;
		final String env = dump.substring(1, posS);
		String newBase = System.getProperty(env);
		if (newBase == null)
			newBase = System.getenv(env);
		if (newBase == null)
		{
			LogFactory.getLog(BambiServlet.class).warn("could not evaluate environment variable, keeping literal : " + env);
			return dump.substring(1);
		}
		LogFactory.getLog(BambiServlet.class).info("evaluated environment variable " + env + " to: " + newBase);
		return newBase + dump.substring(posS);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws IOException
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
		doGetPost(request, response, true);
	}

	/**
	 * @param request
	 * @param response
	 * @param bPost
	 * @throws IOException
	 */
	public void doGetPost(final HttpServletRequest request, final HttpServletResponse response, final boolean bPost) throws IOException
	{
		try
		{
			final BambiContainer theContainer = BambiContainer.getInstance();
			if (theContainer == null)
			{
				log.error("No container Running");
			}
			else
			{
				final AbstractDevice rootDev = theContainer.getRootDev();
				rootDev.startWork();
				final String getPost = bPost ? "post" : "get";
				log.debug("Processing " + getPost + " request for: " + request.getPathInfo());

				final StreamRequest sr = StreamRequest.createStreamRequest(request);
				sr.setPost(bPost);

				XMLResponse xr = null;
				try
				{
					xr = theContainer.processStream(sr);
					if (xr != null)
					{
						xr.writeResponse(response);
					}
				}
				catch (final Throwable x)
				{
					log.error("Snafu processing " + getPost + " request for: " + request.getPathInfo(), x);
				}
				final boolean bBuf = theContainer.wantDump() && (dumpGet || bPost) && bambiDumpIn != null;
				if (bBuf)
				{
					dumpIncoming(request, bBuf, sr);
					dumpOutGoing(getPost, sr, xr);
				}
				request.getInputStream().close(); // avoid mem leaks
				rootDev.endWork();
			}
		}
		catch (final IOException x)
		{
			log.warn("whazzap???", x);
		}
	}

	/**
	 * dump the outgoing stuff
	 *
	 * @param getPost
	 * @param sr
	 * @param xr
	 */
	protected void dumpOutGoing(final String getPost, final StreamRequest sr, final XMLResponse xr)
	{
		if (bambiDumpOut != null && (dumpEmpty || (xr != null && xr.hasContent())))
		{
			final String header = sr.getDumpHeader();
			final InputStream buf = xr == null ? null : xr.getInputStream();

			final File in = bambiDumpOut.newFileFromStream(header, buf, sr.getName());
			if (in != null)
			{
				in.renameTo(new File(UrlUtil.newExtension(in.getPath(), ("." + getPost + ".resp.tmp"))));
			}
		}
	}

	/**
	 *
	 * dump the incoming stuff
	 *
	 * @param request
	 * @param bBuf
	 * @param sr
	 */
	protected void dumpIncoming(final HttpServletRequest request, final boolean bBuf, final StreamRequest sr)
	{
		if (bBuf)
		{
			final String header = sr.getDumpHeader();
			final String h2 = header + "\nContext Length: " + request.getContentLength();
			bambiDumpIn.newFileFromStream(h2, sr.getInputStream(), sr.getName());
		}
	}

	/**
	 * Destroys the servlet.
	 */
	@Override
	public void destroy()
	{
		log.info("shutting down servlet: ");
		final BambiContainer container = BambiContainer.getInstance();
		if (container != null)
		{
			container.shutDown();
		}
		else
		{
			log.warn("shutting down null container - ignore");
		}
		super.destroy();
	}

	/**
	 *
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
		doGetPost(request, response, false);
	}
}
