/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2024 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.enums.ValuedEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public abstract class HTTPResponse extends BambiLogFactory
{
	private final static Log log = LogFactory.getLog(HTTPResponse.class);

	/**
	 * @param theXML the xml to write - may be null
	 */
	public HTTPResponse()
	{
		super();
		httpRC = 200;
		headers = new JDFAttributeMap();
		theBuffer = null;
		notification = null;
		this.errorRC = true;
	}

	/**
	 *
	 * @param r
	 * @param contentType
	 */
	public HTTPResponse(final HTTPResponse r)
	{
		super();
		httpRC = r.httpRC;
		theBuffer = null;
		notification = r.notification;
		contentType = r.getContentType();
		this.errorRC = r.errorRC;
		headers = r.headers.clone();

	}

	protected ByteArrayIOStream theBuffer;
	private String contentType;
	int httpRC;
	private String notification;
	boolean errorRC;
	private final JDFAttributeMap headers;

	/**
	 * @return the errorRC
	 */
	boolean isErrorRC()
	{
		return errorRC;
	}

	/**
	 * if true (the default) e a specific error message is sent for error rcs, else the standard message is sent
	 *
	 * @param errorRC the errorRC to set
	 */
	public void setErrorRC(final boolean errorRC)
	{
		this.errorRC = errorRC;
	}

	/**
	 * returns the OutputStream for this if theXML==null
	 *
	 * @return an OutputStream to write to, if xml==null, else null
	 */
	public OutputStream getOutputStream()
	{
		if (theBuffer == null)
			theBuffer = new ByteArrayIOStream();
		return theBuffer;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType()
	{
		return contentType;
	}

	/**
	 * @return the content Length
	 */
	public int getContentLength()
	{
		getInputStream(); // ensure that we have a valid stream length
		return theBuffer == null ? 0 : theBuffer.size();
	}

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(final String contentType)
	{
		this.contentType = contentType;
	}

	/**
	 * add an http header for the response stream
	 *
	 * @param key
	 * @param value
	 */
	public void setHeader(final String key, final String value)
	{
		if (StringUtil.isEmpty(value))
			headers.remove(key);
		else
			headers.put(key, value);
	}

	/**
	 * @return
	 */
	public InputStream getInputStream()
	{
		if (theBuffer == null)
			fillbuffer();
		return theBuffer == null ? null : theBuffer.getInputStream();
	}

	protected abstract void fillbuffer();

	/**
	 * @return
	 */
	public boolean hasContent()
	{
		final InputStream is = getInputStream();
		try
		{
			return is != null && is.read() >= 0;
		}
		catch (final IOException x)
		{
			return false;
		}
	}

	/**
	 * format currentTimeMillis() to mmm dd -HHH:mm:ss
	 *
	 * @param milliSeconds
	 * @return A String that formats a milliseconds (currentTimeMillis()) to a date
	 */
	public static String formatLong(final long milliSeconds)
	{

		return milliSeconds <= 0 ? " - " : new JDFDate(milliSeconds).getFormattedDateTime("MMM dd - HH:mm:ss");
	}

	/**
	 * add a set of options to an xml file
	 *
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

	/**
	 *
	 * @param sr the servlet response to serialize into
	 */
	public void writeResponse(final HttpServletResponse sr)
	{
		try
		{
			if (errorRC && !UrlUtil.isReturnCodeOK(httpRC))
			{
				sr.sendError(httpRC, notification);
				log.warn("sending rc: " + httpRC + " " + notification);
			}
			else
			{
				for (final Entry<String, String> e : headers.entrySet())
				{
					sr.setHeader(e.getKey(), e.getValue());
				}
				sr.setStatus(httpRC);
				sr.setContentType(getContentType());
				final ServletOutputStream outputStream = sr.getOutputStream();
				final InputStream inputStream = getInputStream(); // note that getInputStream optionally serializes the XMLResponse xml document
				sr.setContentLength(getContentLength());
				if (inputStream != null)
				{
					IOUtils.copy(inputStream, outputStream);
				}
				outputStream.flush();
				outputStream.close();
			}
		}
		catch (final IOException e)
		{
			log.error("cannot write to stream: ", e);
		}
	}

	/**
	 * @return the httpRC
	 */
	public int getHttpRC()
	{
		return httpRC;
	}

	/**
	 * @param httpRC the httpRC to set
	 */
	public void setHttpRC(final int httpRC, final String notification)
	{
		this.httpRC = httpRC;
		this.notification = notification;
	}

	/**
	 * @return the notification
	 */
	public String getNotification()
	{
		return notification;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [" + (theBuffer != null ? "theBuffer=" + theBuffer + ", " : "") + (contentType != null ? "contentType=" + contentType + ", " : "")
				+ "httpRC=" + httpRC + ", " + (notification != null ? "notification=" + notification + ", " : "") + "errorRC=" + errorRC + "]";
	}
}
