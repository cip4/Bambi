/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2016 The International Cooperation for the Integration of 
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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

import javax.mail.BodyPart;
import javax.mail.MessagingException;

import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.jdflib.core.JDFConstants;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.extensions.XJDFConstants;
import org.cip4.jdflib.extensions.XJDFHelper;
import org.cip4.jdflib.extensions.XJDFZipReader;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.mime.MimeReader;
import org.cip4.jdflib.util.zip.ZipReader;

/**
 * class that handles all bambi JDF/JMF requests - regardless of the servlet context
 * previously part of {@link BambiServlet}
 * it is implemented as a Singleton so that you always have static access
 * 
 * note that the get handling routines still assume a servlet context - only the actual JDF / JMF post does not
 * @author Rainer Prosi, Heidelberger Druckmaschinen 
 */
public abstract class ServletContainer extends BambiLogFactory
{

	/**
	 * use getCreateInstance from outside
	 */
	protected ServletContainer()
	{
		super();
		log.info("Creating Servlet Container: " + toString());
		nLogGet = 0;
	}

	/**
	 * @param url
	 * @return the deviceID
	 */
	public static String getDeviceIDFromURL(String url)
	{
		String devID = StringUtil.token(url, -1, "/");
		devID = StringUtil.token(devID, 0, "?&");
		devID = StringUtil.trim(devID, null);
		return devID;
	}

	protected int nLogGet;
	protected boolean bWantDump = true;

	/**
	 * Getter for wantDump attribute.
	 * @return the wantDump
	 */
	public boolean wantDump()
	{
		return bWantDump;
	}

	/**
	 * Setter for wantDump attribute.
	 * @param wantDump the wantDump to set
	 */
	public void setWantDump(boolean wantDump)
	{
		bWantDump = wantDump;
	}

	/**
	 * @param requestURI
	 * @param messageType
	 * @param returnCode
	 * @param notification
	 * @return 
	 */
	public XMLResponse processError(final String requestURI, final EnumType messageType, final int returnCode, final String notification)
	{
		log.warn("processError- rc: " + returnCode + " " + notification == null ? "" : notification);
		final JDFJMF error = JDFJMF.createJMF(EnumFamily.Response, messageType);
		final JDFResponse r = error.getResponse(0);
		r.setReturnCode(returnCode);
		r.setErrorText(notification, null);
		final IConverterCallback _callBack = getCallback(requestURI);
		if (_callBack != null)
		{
			_callBack.updateJMFForExtern(error.getOwnerDocument_JDFElement());
		}
		final XMLResponse response = new XMLResponse(error);
		response.setContentType(UrlUtil.VND_JMF);
		return response;
	}

	/**
	 * 
	 * @param requestURI
	 * @return
	 */
	protected abstract IConverterCallback getCallback(String requestURI);

	/**
	 * 
	 */
	public void shutDown()
	{
		log.info("Shutting down Container " + toString());
		JMFFactory.shutdown();
	}

	/**
	 * 
	 */
	public void reset()
	{
		log.info("resetting Container " + toString());
		JMFFactory.shutdown();
	}

	/**
	 * process an incoming stream 
	 * dispatch to the appropriate processors based on the content type
	 * 
	 * @param request 
	 * @return 
	 * @throws IOException 
	 */
	public XMLResponse processStream(final StreamRequest request) throws IOException
	{
		final XMLResponse r;
		startTimer(request);

		if (request.isPost()) // post request
		{
			final String contentType = request.getContentType(true);
			if (UrlUtil.VND_JMF.equals(contentType))
			{
				XMLRequest req = new XMLRequest(request);
				r = processJMFDoc(req);
			}
			else if (UrlUtil.isXMLType(contentType))
			{
				XMLRequest req = new XMLRequest(request);
				r = processXMLDoc(req);
			}
			else if (UrlUtil.isZIPType(contentType))
			{
				r = processZip(request);
			}
			else
			{
				final boolean isMultipart = MimeUtil.isMimeMultiPart(contentType);
				if (isMultipart)
				{
					log.info("Processing multipart request... (ContentType: " + contentType + ")");
					r = processMultiPart(request);
				}
				else
				{
					String ctWarn = "Unknown HTTP ContentType: " + contentType;
					log.error(ctWarn);
					ctWarn += "\nFor JMF , please use: " + UrlUtil.VND_JMF;
					ctWarn += "\nFor JDF , please use: " + UrlUtil.VND_JDF;
					ctWarn += "\nFor MIME, please use: " + MimeUtil.MULTIPART_RELATED;
					ctWarn += "\n\n Input Message:\n\n";
					r = processError(request.getRequestURI(), EnumType.Notification, 9, ctWarn);
				}
			}
		}
		else
		// get request
		{
			r = handleGet(request);
		}
		stopTimer(request);
		return r;
	}

	private XMLResponse processZip(final StreamRequest request)
	{
		log.info("Processing zip request:  " + toString());
		final InputStream is = request.getInputStream();
		ZipReader zipReader = new ZipReader(is);
		zipReader.setCaseSensitive(false);
		return processZip(request, zipReader);
	}

	/**
	 * 
	 * @param request
	 * @param zipReader
	 * @return
	 */
	protected XMLResponse processZip(final StreamRequest request, ZipReader zipReader)
	{
		ZipEntry e = getXMLFromZip(zipReader);
		String name = e == null ? null : e.getName();
		JDFDoc d;
		if (XJDFHelper.XJDF.equalsIgnoreCase(UrlUtil.extension(name)))
		{
			log.info("Processing XJDF zip request: " + request);
			XJDFZipReader xjdfZipReader = new XJDFZipReader(zipReader);
			xjdfZipReader.convertXJDF();
			JDFNode jdfRoot = xjdfZipReader.getJDFRoot();
			d = jdfRoot == null ? null : jdfRoot.getOwnerDocument_JDFElement();
		}
		else
		{
			log.info("Processing XML zip request:  " + toString());
			d = zipReader.getJDFDoc();
			zipReader.buffer();
			ZipEntry e2 = zipReader.getNextEntry();
			if (e2 != null)
			{
				String rootName = e2.getName();
				if (rootName.endsWith(JDFConstants.SLASH) && name.startsWith(rootName))
				{
					zipReader.setRootEntry(rootName);
				}
			}
		}
		final XMLResponse r;
		if (d != null)
		{
			XMLRequest req = new XMLRequest(d);
			req.setContainer(request);
			r = processXMLDoc(req);
		}
		else
		{
			String ctWarn = "Cannot extract zip from: " + request.getRequestURI();
			log.error(ctWarn);
			r = processError(request.getRequestURI(), EnumType.Notification, 9, ctWarn);
		}
		return r;
	}

	/**
	 * retrieve the first xml like entry from a zip
	 * 
	 * @param zipReader
	 * @return the matching entry, null if no matching entry
	 */
	private ZipEntry getXMLFromZip(ZipReader zipReader)
	{
		ZipEntry e = zipReader.getMatchingEntry("*.ptk", 0);
		if (e == null)
		{
			e = zipReader.getMatchingEntry("*.xjmf", 0);
		}
		if (e == null)
		{
			e = zipReader.getMatchingEntry("*.jmf", 0);
		}
		if (e == null)
		{
			e = zipReader.getMatchingEntry("*.xjdf", 0);
		}
		if (e == null)
		{
			e = zipReader.getMatchingEntry("*.jdf", 0);
		}
		if (e == null)
		{
			e = zipReader.getMatchingEntry("*.xml", 0);
		}
		return e;
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	protected abstract XMLResponse handleGet(final StreamRequest request);

	/**
	 * @param request
	 * @return
	 */
	public XMLResponse processXMLDoc(XMLRequest request)
	{
		log.info("Processing xml document: content type=" + request.getContentType(true));
		XMLRequest newRequest = convertToJMF(request);
		if (newRequest != null)
		{
			KElement e = newRequest.getXML();
			// jmf with incorrect mime type or something that the device could translate to jmf
			if (e instanceof JDFJMF || XJDFConstants.XJMF.equals(e.getLocalName()))
			{
				return processJMFDoc(newRequest);
			}
		}

		KElement e = request.getXML();
		String notification = "cannot process xml of type root = " + ((e == null) ? "null" : e.getLocalName()) + "; Content-Type: " + request.getContentType(false);
		return processError(request.getRequestURI(), EnumType.Notification, 3, notification);
	}

	protected abstract XMLRequest convertToJMF(XMLRequest request);

	/**
	 * Parses a multipart request.
	 * @param request 
	 * @return 
	 * @throws IOException 
	 */
	public XMLResponse processMultiPart(final StreamRequest request) throws IOException
	{
		startTimer(request);
		final InputStream inStream = request.getInputStream();
		final MimeReader mr = new MimeReader(inStream);
		final BodyPart bp[] = mr.getBodyParts();
		log.info("Body Parts: " + ((bp == null) ? 0 : bp.length));
		XMLResponse r = null;
		if (bp == null || bp.length == 0)
		{
			r = processError(request.getRequestURI(), EnumType.Notification, 9, "No body parts in mime package");
		}
		else
		{
			try
			{// messaging exceptions
				if (bp.length > 1)
				{
					MimeRequest req = new MimeRequest(mr);
					req.setContainer(request);
					r = processMultipleDocuments(req);
				}
				else
				// unpack the only body part and throw it at the processor again
				{
					StreamRequest sr = new StreamRequest(bp[0].getInputStream());
					sr.setContainer(request);
					r = processStream(sr);
				}
			}
			catch (final MessagingException x)
			{
				r = processError(request.getRequestURI(), null, 9, "Messaging exception\n" + x.getLocalizedMessage());
			}
		}
		stopTimer(request);
		return r;
	}

	/**
	 * process a multipart request - including job submission
	 * @param request
	 * @return the generated response
	 */
	public XMLResponse processMultipleDocuments(final MimeRequest request)
	{
		startTimer(request);
		final XMLResponse r;
		MimeReader reader = request.getReader();
		BodyPart[] bp = reader == null ? null : reader.getBodyParts();
		log.info("processMultipleDocuments- parts: " + (bp == null ? 0 : bp.length));
		if (bp == null || bp.length == 0)
		{
			r = processError(request.getRequestURI(), EnumType.Notification, 2, "processMultipleDocuments- not enough parts, bailing out");
		}
		else
		{
			final JDFDoc docJDF[] = MimeUtil.getJMFSubmission(bp[0].getParent());
			if (docJDF == null || docJDF.length == 0)
			{
				r = processError(request.getRequestURI(), EnumType.Notification, 2, "proccessMultipleDocuments- no body parts, bailing out!");
			}
			else
			{
				XMLRequest r2 = new XMLRequest(docJDF[0].getJMFRoot());
				r2.setContainer(request);
				r = processXMLDoc(r2);
				request.setName(r2.getName());
			}
		}
		stopTimer(request);

		return r;
	}

	/**
	 * process the main, i.e. doc #0 JMF document
	 * 
	 * @param request the http request to service
	 * @return
	 */
	public abstract XMLResponse processJMFDoc(final XMLRequest request);

	/**
	 * @see java.lang.Object#toString()
	 * @return
	*/
	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	/**
	 * @param request
	 */
	protected abstract void startTimer(ContainerRequest request);

	/**
	 * @param request
	 */
	protected abstract void stopTimer(ContainerRequest request);
}
