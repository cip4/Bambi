/*
 *
 * The CIP4 Software License, Version 1.0
 *
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

package org.cip4.bambi.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.enums.ValuedEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.MessageSender;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 * Entrance point for Bambi servlets
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
		 * @param d the error details string
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
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IGetHandler#handleGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
		 */
		public boolean handleGet(final BambiServletRequest request, final BambiServletResponse response)
		{
			final String context = getContext(request);
			if (KElement.isWildCard(context) || context.equalsIgnoreCase("overview"))
			{
				return rootDev.showDevice(request, response, false);
			}
			else
			{
				return false;
			}
		}
	}

	// protected IConverterCallback _callBack = null;
	private static Log log = LogFactory.getLog(BambiServlet.class.getName());
	protected AbstractDevice rootDev = null;
	protected DumpDir bambiDumpIn = null;
	protected DumpDir bambiDumpOut = null;
	protected boolean dumpGet = false;
	protected boolean dumpEmpty = false;
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
		loadProperties(context, new File("/config/devices.xml"), dump);
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
	 * create the specified directories, if the do not exist
	 * @param dirs the directories to create
	 */
	private void createDirs(final Vector<File> dirs)
	{
		for (int i = 0; i < dirs.size(); i++)
		{
			final File f = dirs.get(i);
			if (f != null && !f.exists())
			{
				if (!f.mkdirs())
				{
					log.error("failed to create directory " + f);
				}
			}
		}
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
		log.warn("processError- rc: " + returnCode + " " + notification == null ? "" : notification);
		final JDFJMF error = JDFJMF.createJMF(EnumFamily.Response, messageType);
		final JDFResponse r = error.getResponse(0);
		r.setReturnCode(returnCode);
		r.setErrorText(notification, null);
		response.setContentType(UrlUtil.VND_JMF);
		final IConverterCallback _callBack = getCallBack(request);
		if (_callBack != null)
		{
			_callBack.updateJMFForExtern(error.getOwnerDocument_JDFElement());
		}

		try
		{
			error.getOwnerDocument_KElement().write2Stream(response.getBufferedOutputStream(), 0, true);
		}
		catch (final IOException x)
		{
			log.error("processError: cannot write response\n" + x.getMessage());
		}
	}

	/**
	 * process a multipart request - including job submission
	 * @param request
	 * @param response
	 * @param bp the mime body parts
	 */
	protected void processMultipleDocuments(final BambiServletRequest request, final BambiServletResponse response, final BodyPart[] bp)
	{
		log.info("processMultipleDocuments- parts: " + (bp == null ? 0 : bp.length));
		if (bp == null || bp.length < 2)
		{
			processError(request, response, EnumType.Notification, 2, "processMultipleDocuments- not enough parts, bailing out");
			return;
		}
		final JDFDoc docJDF[] = MimeUtil.getJMFSubmission(bp[0].getParent());
		if (docJDF == null || docJDF.length == 0)
		{
			processError(request, response, EnumType.Notification, 2, "proccessMultipleDocuments- incorrect jmf/jdf parts, bailing out!");
			return;
		}
		else if (docJDF.length == 1)
		{
			final JDFMessage messageElement = docJDF[0].getJMFRoot().getMessageElement(null, null, 0);
			EnumType typ = messageElement == null ? EnumType.Notification : messageElement.getEnumType();
			if (typ == null)
			{
				typ = EnumType.Notification;
			}
			processError(request, response, typ, 2, "proccessMultipleDocuments- incorrect jmf/jdf parts, bailing out!");
			return;

		}

		// callbacks must be handled individually
		processJMFDoc(request, response, docJDF[0]);
	}

	/**
	 * process zhe main, i.e. doc #0 JMF document
	 * 
	 * @param request the http request to service
	 * @param response the http response to fill
	 * @param jmfDoc the extracted first jmf bodypart or raw jmf
	 */
	protected void processJMFDoc(final BambiServletRequest request, final BambiServletResponse response, final JDFDoc jmfDoc)
	{
		if (jmfDoc == null)
		{
			processError(request, response, null, 3, "Error Parsing JMF");
		}
		else
		{
			final IConverterCallback _callBack = getCallBack(request);

			if (_callBack != null)
			{
				_callBack.prepareJMFForBambi(jmfDoc);
			}

			// switch: sends the jmfDoc to correct device
			JDFDoc responseJMF = null;
			final IJMFHandler handler = getTargetHandler(request);
			if (handler != null)
			{
				responseJMF = handler.processJMF(jmfDoc);
			}

			if (responseJMF != null)
			{
				response.setContentType(UrlUtil.VND_JMF);
				if (_callBack != null)
				{
					_callBack.updateJMFForExtern(responseJMF);
				}

				try
				{
					responseJMF.write2Stream(response.getBufferedOutputStream(), 0, true);
				}
				catch (final IOException e)
				{
					log.error("cannot write to stream: ", e);
				}
			}
			else
			{
				final JDFJMF jmf = jmfDoc.getJMFRoot();
				if (jmf != null)
				{
					VElement v = jmf.getMessageVector(null, null);
					final int nMess = v == null ? 0 : v.size();
					v = jmf.getMessageVector(EnumFamily.Signal, null);
					final int nSigs = v.size();
					if (nMess > nSigs || nMess == 0)
					{
						processError(request, response, null, 1, "General Error Handling JMF");
					}
				}
				else
				{
					processError(request, response, null, 3, "Error Parsing JMF");
				}
			}
		}
	}

	/**
	 * @param request
	 * @return the call back for a given request
	 */
	private IConverterCallback getCallBack(final BambiServletRequest request)
	{
		return rootDev.getCallback(request.getRequestURI());
	}

	protected IJMFHandler getTargetHandler(final BambiServletRequest request)
	{
		final AbstractDevice device = getDeviceFromRequest(request);
		if (device == null)
		{
			return rootDev.getHandler(); // device not found
		}
		return (device.getHandler());
	}

	/**
	 * Parses a multipart request.
	 */
	protected void processMultipartRequest(final BambiServletRequest request, final BambiServletResponse response) throws IOException
	{
		final InputStream inStream = request.getBufferedInputStream();
		final BodyPart bp[] = MimeUtil.extractMultipartMime(inStream);
		log.info("Body Parts: " + ((bp == null) ? 0 : bp.length));
		if (bp == null || bp.length == 0)
		{
			processError(request, response, null, 9, "No body parts in mime package");
			return;
		}
		try
		{// messaging exceptions
			if (bp.length > 1)
			{
				processMultipleDocuments(request, response, bp);
			}
			else
			{
				final String s = bp[0].getContentType();
				if (UrlUtil.VND_JMF.equalsIgnoreCase(s))
				{
					processJMFRequest(request, response, bp[0].getInputStream());
				}
			}
		}
		catch (final MessagingException x)
		{
			processError(request, response, null, 9, "Messaging exception\n" + x.getLocalizedMessage());
		}
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

		final String contentType = request.getContentType();
		if (UrlUtil.VND_JMF.equals(contentType))
		{
			processJMFRequest(bufRequest, bufResponse, null);
		}
		else if (UrlUtil.TEXT_XML.equals(contentType))
		{
			processXMLRequest(bufRequest, bufResponse);
		}
		else
		{
			final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart)
			{
				log.info("Processing multipart request... (ContentType: " + contentType + ")");
				processMultipartRequest(bufRequest, bufResponse);
			}
			else
			{
				String ctWarn = "Unknown HTTP ContentType: " + contentType;
				log.error(ctWarn);
				response.setContentType("text/plain");

				final OutputStream os = bufResponse.getBufferedOutputStream();
				final InputStream is = bufRequest.getBufferedInputStream();
				ctWarn += "\nFor JMF , please use: " + UrlUtil.VND_JMF;
				ctWarn += "\nFor JDF , please use: " + UrlUtil.VND_JDF;
				ctWarn += "\nFor MIME, please use: " + MimeUtil.MULTIPART_RELATED;
				ctWarn += "\n\n Input Message:\n\n";
				os.write(ctWarn.getBytes());
				IOUtils.copy(is, os);
			}
		}
		if (bambiDumpOut != null && (dumpEmpty || bufResponse.getBufferedCount() > 0))
		{
			final InputStream buf = bufResponse.getBufferedInputStream();

			final File in = bambiDumpOut.newFileFromStream(header, buf);
			if (in != null)
			{
				in.renameTo(new File(StringUtil.newExtension(in.getName(), ".post.resp.txt")));
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
	 * @param bufRequest
	 * @param bufResponse
	 */
	private void processXMLRequest(final BambiServletRequest bufRequest, final BambiServletResponse bufResponse)
	{
		// TODO some smarts whether JDF or JMF
		log.info("Processing text/xml");
		processJMFRequest(bufRequest, bufResponse, null);
	}

	/**
	 * @param request
	 * @param response
	 */
	private void processJMFRequest(final BambiServletRequest request, final BambiServletResponse response, InputStream inStream)
	{
		log.debug("processJMFRequest");
		final JDFParser p = new JDFParser();
		if (inStream == null)
		{
			inStream = request.getBufferedInputStream();
		}
		final JDFDoc jmfDoc = p.parseStream(inStream);
		processJMFDoc(request, response, jmfDoc);
	}

	/**
	 * loads properties
	 * @param context the servlet context information
	 * @param config the name of the Java config xml file
	 * @param dump the file where to dump debug requests
	 */
	protected void loadProperties(final ServletContext context, final File config, final String dump)
	{
		final MultiDeviceProperties props = new MultiDeviceProperties(context, config);
		createDevices(props, dump);
	}

	/**
	 * Destroys the servlet.
	 */
	@Override
	public void destroy()
	{
		rootDev.shutdown();
		JMFFactory.shutDown(null, true);
		super.destroy();
		ThreadUtil.sleep(5234); // leave some time for cleanup
		JMFFactory.shutDown(null, false);
	}

	/**
	 * @param request
	 * @return the device to process this request
	 */
	protected AbstractDevice getDeviceFromRequest(final BambiServletRequest request)
	{
		final String deviceID = request.getDeviceID();
		final RootDevice root = getRootDevice();
		final AbstractDevice dev = root == null ? rootDev : root.getDevice(deviceID);
		if (dev == null)
		{
			log.info("invalid request: device with id=" + deviceID == null ? "null" : deviceID + " not found");
			return null;
		}
		return dev;
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
	public static void addOptionList(final ValuedEnum e, final List<EnumQueueEntryStatus> l, final KElement parent, final String name)
	{
		if (e == null || parent == null)
		{
			return;
		}
		final KElement list = parent.appendElement(BambiNSExtension.MY_NS_PREFIX + "OptionList", BambiNSExtension.MY_NS);
		list.setAttribute("name", name);
		list.setAttribute("default", e.getName());
		final Iterator<EnumQueueEntryStatus> it = l.iterator();
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
			bHandled = rootDev.getGetDispatchHandler().handleGet(bufRequest, bufResponse);
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
	 * @return
	 */
	private RootDevice getRootDevice()
	{
		return (rootDev instanceof RootDevice) ? (RootDevice) rootDev : null;
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

	/**
	 * create devices based on the list of devices given in a file
	 * @param props
	 * @param dump the file where to dump debug requests
	 * @return true if successful, otherwise false
	 */
	protected boolean createDevices(final MultiDeviceProperties props, final String dump)
	{
		MessageSender.setBaseLocation(props.getJMFDir());
		final Vector<File> dirs = new Vector<File>();
		dirs.add(props.getBaseDir());
		dirs.add(props.getJDFDir());
		createDirs(dirs);
		final EnumVersion version = props.getJDFVersion();
		JDFElement.setDefaultJDFVersion(version);
		final VElement v = props.getDevices();
		final Iterator<KElement> iter = v.iterator();
		final boolean needController = v.size() > 1;
		while (iter.hasNext())
		{
			final KElement next = iter.next();
			log.info("Creating Device " + next.getAttribute("DeviceID"));
			final IDeviceProperties prop = props.createDevice(next);
			AbstractDevice d = null;
			if (rootDev == null)
			{
				if (needController)
				{
					d = prop.getDeviceInstance();
					if (!(d instanceof RootDevice))
					{
						log.info("Updating Root Device " + next.getAttribute("DeviceID"));
						d.shutdown();
						d = new RootDevice(prop);
					}
				}
				else
				{
					d = prop.getDeviceInstance();
				}
				rootDev = d;
			}
			else
			// we already have a root / dispatcher device - use it as base
			{
				final RootDevice rd = getRootDevice();
				d = rd.createDevice(prop);

			}
			if (dump != null)
			{
				bambiDumpIn = new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("inServer")));
				bambiDumpOut = new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("outServer")));
				final String senderID = d != null ? d.getDeviceID() : "Bambi";
				final DumpDir dumpSendIn = new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("inMessage." + senderID)));
				final DumpDir dumpSendOut = new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("outMessage." + senderID)));
				MessageSender.addDumps(senderID, dumpSendIn, dumpSendOut);
			}
		}
		return true;
	}

	@Override
	protected void service(final HttpServletRequest arg0, final HttpServletResponse arg1) throws ServletException, IOException
	{
		// TODO find correct server port at startup
		if (port == 0)
		{
			port = arg0.getServerPort();
		}
		rootDev.incNumRequests();
		super.service(arg0, arg1);
	}
}
