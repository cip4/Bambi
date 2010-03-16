/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2010 The International Cooperation for the Integration of 
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
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.enums.ValuedEnum;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.StringUtil;

/**
 * Entrance point for Bambi servlets
 * note that most processing has been moved to the servlet independent class @see BambiContainer
 * @author boegerni
 * 
 */
public class BambiServlet extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1926504814491033980L;

	/**
	 * handler for final handler for any non-handled url
	 * @author prosirai
	 * 
	 */
	public static class UnknownErrorHandler implements IGetHandler
	{
		private String details = null;
		private String message = "No handler for URL";
		private Object parent = null;

		/**
		 * @param _parent
		 */
		public UnknownErrorHandler(final Object _parent)
		{
			super();
			parent = _parent;
		}

		/**
		 * @see org.cip4.bambi.core.IGetHandler#handleGet(org.cip4.bambi.core.BambiServletRequest, org.cip4.bambi.core.BambiServletResponse)
		 */
		public boolean handleGet(final BambiServletRequest request, final BambiServletResponse response)
		{
			showErrorPage(message, details, request, response);
			return true;
		}

		/**
		 * @param d the error details string
		 */
		public void setDetails(final String d)
		{
			details = d;
		}

		/**
		 * @param m the error details string
		 */
		public void setMessage(final String m)
		{
			message = m;
		}

		/**
		 * display an error on error.jsp
		 * @param errorMsg short message describing the error
		 * @param errorDetails detailed error info
		 * @param request required to forward the page
		 * @param response required to forward the page
		 */
		protected void showErrorPage(final String errorMsg, final String errorDetails, final BambiServletRequest request, final BambiServletResponse response)
		{
			final XMLDoc d = new XMLDoc("BambiError", null);
			final KElement err = d.getRoot();
			err.setAttribute("errorOrigin", parent.getClass().getName());
			err.setAttribute("errorMsg", errorMsg);
			err.setAttribute("errorDetails", errorDetails);
			err.setAttribute("Context", request.getContextRoot());
			err.setAttribute("URL", request.getCompleteRequestURL());
			d.setXSLTURL(request.getContextRoot() + "/error.xsl");
			try
			{
				d.write2Stream(response.getBufferedOutputStream(), 2, false);
			}
			catch (final IOException x)
			{
				// nop
			}
		}
	}

	/**
	 * handler for the overview page
	 * @author prosirai
	 * 
	 */
	protected class OverviewHandler implements IGetHandler
	{
		/**
		 * 
		 * @see org.cip4.bambi.core.IGetHandler#handleGet(org.cip4.bambi.core.BambiServletRequest, org.cip4.bambi.core.BambiServletResponse)
		 * @param request
		 * @param response
		 * @return
		 */
		public boolean handleGet(final BambiServletRequest request, final BambiServletResponse response)
		{
			final String context = getContext(request);
			if (KElement.isWildCard(context) || context.equalsIgnoreCase("overview"))
			{
				return theContainer.getRootDev().showDevice(request, response, false);
			}
			else
			{
				return false;
			}
		}
	}

	/**
	 * 
	 */
	public BambiServlet()
	{
		super();
		log = new BambiLogFactory(this.getClass()).getLog();
		theContainer = new BambiContainer();
	}

	private BambiLog log = null;
	protected boolean dumpGet = false;
	protected boolean dumpEmpty = false;
	final BambiContainer theContainer;
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
		final ServletContext context = config.getServletContext();
		final String dump = initializeDumps(config);
		log.info("Initializing servlet for " + context.getServletContextName());
		final File baseDir = new File(context.getRealPath(""));
		String baseURL = null;
		try
		{
			baseURL = StringUtil.token(context.getResource("/").toExternalForm(), -1, "/");
		}
		catch (MalformedURLException x)
		{
			log.fatal("illegal context loading servlet: ", x);
		}
		theContainer.loadProperties(baseDir, baseURL, new File("/config/devices.xml"), dump, getPropsName());
	}

	/**
	 * @return
	 */
	protected String getPropsName()
	{
		return "org.cip4.bambi.core.MultiDeviceProperties";
	}

	private String initializeDumps(final ServletConfig config)
	{
		final String dump = StringUtil.getNonEmpty(config.getInitParameter("bambiDump"));
		if (dump != null)
		{
			bambiDumpIn = new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("in")));
			bambiDumpOut = new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("out")));
			final String iniDumpGet = config.getInitParameter("bambiDumpGet");
			dumpGet = iniDumpGet == null ? false : "true".compareToIgnoreCase(iniDumpGet) == 0;
			final String iniDumpEmpty = config.getInitParameter("bambiDumpEmpty");
			dumpEmpty = iniDumpEmpty == null ? false : "true".compareToIgnoreCase(iniDumpEmpty) == 0;
		}
		return dump;
	}

	/**
	 * @param request
	 * @param response
	 * @param messageType
	 * @param returnCode
	 * @param notification
	 */
	protected void processError(final BambiServletRequest request, final BambiServletResponse response, final EnumType messageType, final int returnCode, final String notification)
	{
		XMLResponse res = theContainer.processError(request.getRequestURI(), messageType, returnCode, notification);
		response.write(res);
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
		log.debug("Processing post request for: " + request.getPathInfo());

		final boolean bBuf = bambiDumpIn != null;
		final BambiServletRequest bufRequest = new BambiServletRequest(request, bBuf);
		final BambiServletResponse bufResponse = new BambiServletResponse(response, bBuf, bufRequest);
		final String header = getDumpHeader(bufRequest);
		if (bBuf)
		{
			final String h2 = header + "\nContext Length: " + request.getContentLength();
			bambiDumpIn.newFileFromStream(h2, bufRequest.getBufferedInputStream());
		}

		final String contentType = bufRequest.getContentType();
		StreamRequest sr = new StreamRequest(bufRequest.getBuffer());
		sr.setContentType(contentType);
		sr.setRequestURI(request.getRequestURI());
		sr.setContext(request.getContextPath());
		sr.setPost(true);
		XMLResponse xr = theContainer.processStream(sr);
		bufResponse.write(xr);
		if (bambiDumpOut != null && (dumpEmpty || bufResponse.getBufferedCount() > 0))
		{
			final InputStream buf = bufResponse.getBufferedInputStream();

			final File in = bambiDumpOut.newFileFromStream(header, buf);
			if (in != null)
			{
				in.renameTo(new File(StringUtil.newExtension(in.getPath(), ".post.resp.txt")));
			}
		}
		bufResponse.flush();
		bufRequest.flush(); // avoid mem leaks
	}

	/**
	 * @param request
	 * @return
	 */
	private String getDumpHeader(final BambiServletRequest request)
	{
		String header = "Context Path: " + request.getCompleteRequestURL();
		header += "\nMethod: Post\nContext Type: " + request.getContentType();
		header += "\nRemote host: " + request.getRemoteHost();
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
	 * @param url
	 * @return the dviceID
	 */
	public static String getDeviceIDFromURL(final String url)
	{
		return BambiServletRequest.getDeviceIDFromURL(url);
	}

	/**
	 * add a set of options to an xml file
	 * @param e the default enum
	 * @param l the list of all enums
	 * @param parent the parent element to add the list to
	 * @param name the name of the option list form
	 */
	public static void addOptionList(final ValuedEnum e, final List<? extends ValuedEnum> l, final KElement parent, final String name)
	{
		if (e == null || parent == null)
		{
			return;
		}
		final KElement list = parent.appendElement(BambiNSExtension.MY_NS_PREFIX + "OptionList", BambiNSExtension.MY_NS);
		list.setAttribute("name", name);
		list.setAttribute("default", e.getName());
		final Iterator<? extends ValuedEnum> it = l.iterator();
		while (it.hasNext())
		{
			final ValuedEnum ve = it.next();
			final KElement option = list.appendElement(BambiNSExtension.MY_NS_PREFIX + "Option", BambiNSExtension.MY_NS);
			option.setAttribute("name", ve.getName());
			option.setAttribute("selected", ve.equals(e) ? "selected" : null, null);
		}
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
	{
		final boolean bBuf = dumpGet && bambiDumpIn != null;
		final BambiServletRequest bufRequest = new BambiServletRequest(request, bBuf);
		final BambiServletResponse bufResponse = new BambiServletResponse(response, bBuf, bufRequest);
		final String header = getDumpHeader(bufRequest);
		if (bBuf)
		{
			final String h2 = header + "\nContext Length: " + request.getContentLength();
			bambiDumpIn.newFileFromStream(h2, bufRequest.getBufferedInputStream());
		}
		boolean bHandled = new OverviewHandler().handleGet(bufRequest, bufResponse);
		if (!bHandled)
		{
			bHandled = theContainer.getRootDev().getGetDispatchHandler().handleGet(bufRequest, bufResponse);
		}

		if (!bHandled)
		{
			final UnknownErrorHandler unknownErrorHandler = new UnknownErrorHandler(this);
			unknownErrorHandler.handleGet(bufRequest, bufResponse);
		}

		if (dumpGet && bambiDumpOut != null)
		{
			final InputStream buf = bufResponse.getBufferedInputStream();
			bambiDumpOut.newFileFromStream(header, buf);
		}
		bufResponse.flush();
		bufRequest.flush(); // avoid mem leaks

	}

	/**
	 * get the static context string
	 * @param request
	 * @return
	 */
	public static String getContext(final BambiServletRequest request)
	{
		return request == null ? null : request.getContext();
	}

	/**
	 * get the static context string
	 * @param request
	 * @return
	 */
	public static String getBaseServletName(final HttpServletRequest request)
	{
		return request == null ? null : StringUtil.token(request.getRequestURI(), 0, "/");
	}

	/**
	 * format currentTimeMillis() to mmm dd -HHH:mm:ss
	 * @param milliSeconds
	 * @return A String that formats a millseconds (currentTimeMillis()) to a date
	 */
	public static String formatLong(final long milliSeconds)
	{

		return milliSeconds <= 0 ? " - " : new JDFDate(milliSeconds).getFormattedDateTime("MMM dd - HH:mm:ss");
	}

	/**
	 * 
	 * @param request
	 * @param context
	 * @return
	 */
	public static boolean isMyContext(final BambiServletRequest request, final String context)
	{
		if (context == null)
		{
			return true;
		}

		final String reqContext = getContext(request);
		return context.equals(StringUtil.token(reqContext, 0, "/"));

	}

	/**
	 * @param request
	 * @param deviceID
	 * @return
	 */
	public static boolean isMyRequest(final BambiServletRequest request, final String deviceID)
	{
		if (deviceID == null)
		{
			return true;
		}
		final String reqDeviceID = request.getDeviceID();
		return reqDeviceID == null || deviceID.equals(reqDeviceID);
	}

	@Override
	protected void service(final HttpServletRequest arg0, final HttpServletResponse arg1) throws ServletException, IOException
	{
		// TODO find correct server port at startup
		if (port == 0)
		{
			port = arg0.getServerPort();
		}
		AbstractDevice rootDev = theContainer.getRootDev();
		rootDev.incNumRequests();
		super.service(arg0, arg1);
	}
}
