/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2011 The International Cooperation for the Integration of 
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

package org.cip4.bambi.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 * Entrance point for Bambi servlets
 * note that most processing has been moved to the servlet independent class @see BambiContainer
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
	 */
	public BambiServlet()
	{
		super();
		initLogging();
		theContainer = getBambiContainer();
	}

	/**
	 * 
	 *this should be overwritten for nice logging features
	 */
	protected void initLogging()
	{
		log = LogFactory.getLog(getClass());
	}

	protected BambiContainer getBambiContainer()
	{
		BambiContainer container = new BambiContainer();
		return container;
	}

	protected Log log = null;
	protected boolean dumpGet = false;
	protected boolean dumpEmpty = false;
	private final BambiContainer theContainer;
	protected DumpDir bambiDumpIn = null;
	protected DumpDir bambiDumpOut = null;
	/**
	 * cludge to get port number
	 */
	public static int port = 0;

	/**
	 * Initializes the servlet.
	 * @throws ServletException
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException
	{
		super.init(config);
		final String dump = initializeDumps(config);
		String baseURL = getContextPath();
		log.info("Initializing servlet for " + baseURL);

		final ServletContext context = config.getServletContext();
		String realPath = context.getRealPath("/");
		if (realPath == null)
			realPath = ".";
		final File baseDir = new File(realPath);
		theContainer.loadProperties(baseDir, baseURL, new File("config/devices.xml"), dump);
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
		if (context.getMajorVersion() <= 2 && context.getMinorVersion() < 5)
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

	/**
	 * @return
	 * @deprecated - setPropertiesName in the config file
	 */
	@Deprecated
	protected String getPropsName()
	{
		return null;
	}

	private String initializeDumps(final ServletConfig config)
	{
		final String dump = StringUtil.getNonEmpty(config.getInitParameter("bambiDump"));
		if (dump == null)
		{
			log.info("initializing http dump directory: " + dump);
		}
		else
		{
			log.info("initializing http dump directory: " + dump);
			bambiDumpIn = new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("in")));
			bambiDumpOut = new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("out")));
			final String iniDumpGet = config.getInitParameter("bambiDumpGet");
			dumpGet = iniDumpGet == null ? false : "true".compareToIgnoreCase(iniDumpGet) == 0;
			final String iniDumpEmpty = config.getInitParameter("bambiDumpEmpty");
			dumpEmpty = iniDumpEmpty == null ? false : "true".compareToIgnoreCase(iniDumpEmpty) == 0;
			log.info("initializing http dump directory: " + dump + " get=" + dumpGet + " empty=" + dumpEmpty);
		}
		return dump;
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
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
	private void doGetPost(final HttpServletRequest request, final HttpServletResponse response, boolean bPost) throws IOException
	{
		AbstractDevice rootDev = theContainer.getRootDev();
		rootDev.startWork();
		String getPost = bPost ? "post" : "get";
		log.debug("Processing " + getPost + " request for: " + request.getPathInfo());

		StreamRequest sr = createStreamRequest(request);
		sr.setPost(bPost);

		XMLResponse xr = null;
		try
		{
			xr = theContainer.processStream(sr);
			writeResponse(xr, response);
		}
		catch (Exception x)
		{
			log.error("Snafu processing get / post");
		}
		final boolean bBuf = (dumpGet || bPost) && bambiDumpIn != null;
		dumpIncoming(request, bBuf, sr);
		dumpOutGoing(getPost, sr, xr);
		request.getInputStream().close(); // avoid mem leaks
		rootDev.endWork();
	}

	/**
	 * dump the outgoing stuff
	 *  
	 * @param getPost
	 * @param sr
	 * @param xr
	 */
	protected void dumpOutGoing(String getPost, StreamRequest sr, XMLResponse xr)
	{
		if (bambiDumpOut != null && (dumpEmpty || (xr != null && xr.hasContent())))
		{
			final String header = getDumpHeader(sr);
			final InputStream buf = xr.getInputStream();

			final File in = bambiDumpOut.newFileFromStream(header, buf, sr.getName());
			if (in != null)
			{
				in.renameTo(new File(UrlUtil.newExtension(in.getPath(), ("." + getPost + ".resp.txt"))));
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
	protected void dumpIncoming(final HttpServletRequest request, final boolean bBuf, StreamRequest sr)
	{
		if (bBuf)
		{
			final String header = getDumpHeader(sr);
			final String h2 = header + "\nContext Length: " + request.getContentLength();
			bambiDumpIn.newFileFromStream(h2, sr.getInputStream(), sr.getName());
		}
	}

	/**
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private StreamRequest createStreamRequest(final HttpServletRequest request) throws IOException
	{
		StreamRequest sr = new StreamRequest(request.getInputStream());
		final String contentType = request.getContentType();
		sr.setContentType(contentType);
		sr.setRequestURI(request.getRequestURL().toString());
		sr.setHeaderMap(getHeaderMap(request));
		sr.setParameterMap(new JDFAttributeMap(getParameterMap(request)));
		sr.setRemoteHost(request.getRemoteHost());
		return sr;
	}

	/**
	 * @param sr
	 * @return
	 */
	private String getDumpHeader(final ContainerRequest sr)
	{
		String header = "Context Path: " + sr.getRequestURI();
		header += "\nMethod: Post\nContext Type: " + sr.getContentType(false);
		header += "\nRemote host: " + sr.getRemoteHost();
		return header;
	}

	/**
	 * Destroys the servlet.
	 */
	@Override
	public void destroy()
	{
		theContainer.shutDown();
		super.destroy();
	}

	/**
	 * @param r the XMLResponse to serialize
	 * @param sr the servlet response to serialize into
	 */
	private void writeResponse(XMLResponse r, HttpServletResponse sr)
	{
		if (r == null)
		{
			return; // don't write empty stuff
		}
		try
		{
			sr.setContentType(r.getContentType());
			ServletOutputStream outputStream = sr.getOutputStream();
			InputStream inputStream = r.getInputStream(); // note that getInputStream optionally serializes the XMLResponse xml document
			sr.setContentLength(r.getContentLength());
			if (inputStream != null)
			{
				IOUtils.copy(inputStream, outputStream);
			}
			outputStream.flush();
			outputStream.close();
		}
		catch (final IOException e)
		{
			log.error("cannot write to stream: ", e);
		}
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

	@Override
	protected void service(final HttpServletRequest arg0, final HttpServletResponse arg1) throws ServletException, IOException
	{
		// TODO find correct server port at startup
		if (port == 0)
		{
			port = arg0.getServerPort();
		}
		super.service(arg0, arg1);
	}

	/**
	 * returns the headers as an attributemap
	 * @return map of headers, null if no headers exist
	 */
	private JDFAttributeMap getHeaderMap(HttpServletRequest request)
	{
		@SuppressWarnings("unchecked")
		Enumeration<String> headers = request.getHeaderNames();
		if (!headers.hasMoreElements())
		{
			return null;
		}
		JDFAttributeMap map = new JDFAttributeMap();
		while (headers.hasMoreElements())
		{
			String header = headers.nextElement();
			@SuppressWarnings("unchecked")
			Enumeration<String> e = request.getHeaders(header);
			VString v = new VString(e);
			if (v.size() > 0)
			{
				map.put(header, StringUtil.setvString(v, ",", null, null));
			}
		}
		if (map.size() == 0)
			map = null;
		return map;
	}

	/**
	 *  
	 */
	private Map<String, String> getParameterMap(HttpServletRequest request)
	{
		@SuppressWarnings("unchecked")
		Map<String, String[]> pm = request.getParameterMap();
		Map<String, String> retMap = new JDFAttributeMap();
		Set<String> keyset = pm.keySet();
		for (String key : keyset)
		{
			String[] strings = pm.get(key);
			if (strings != null && strings.length > 0)
			{
				String s = strings[0];
				for (int i = 1; i < strings.length; i++)
				{
					s += "," + strings[i];
				}
				s = StringUtil.getNonEmpty(s);
				if (s != null)
					retMap.put(key, s);
			}
		}
		return retMap.size() == 0 ? null : retMap;
	}
}
