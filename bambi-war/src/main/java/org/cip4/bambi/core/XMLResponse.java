/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2017 The International Cooperation for the Integration of
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
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.enums.ValuedEnum;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.UrlUtil;
import org.eclipse.jetty.io.EofException;

/**
  * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class XMLResponse extends BambiLogFactory
{
	/**
	 * @param theXML the xml to write - may be null
	 */
	public XMLResponse(KElement theXML)
	{
		super();
		this.theXML = theXML;
		theBuffer = null;
		if (theXML != null)
		{
			setContentType(UrlUtil.TEXT_XML);
		}
	}

	private KElement theXML;
	private ByteArrayIOStream theBuffer;
	private String contentType;

	/**
	 * @return
	 */
	public KElement getXML()
	{
		return theXML;
	}

	/**
	 * returns the OutputStream for this if theXML==null
	 *
	 * @return an OutputStream to write to, if xml==null, else null
	 */
	public OutputStream getOutputStream()
	{
		if (theXML != null)
			return null;
		if (theBuffer == null)
			theBuffer = new ByteArrayIOStream();
		return theBuffer;
	}

	/**
	 * @return
	 */
	public XMLDoc getXMLDoc()
	{
		return theXML == null ? null : theXML.getOwnerDocument_KElement();
	}

	/**
	 * sets this xml to e, also removes any output stream that may be associated to this
	 *
	 *  @param e the xml to set
	 */
	public void setXML(KElement e)
	{
		theXML = e;
		theBuffer = null;
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
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return
	*/
	@Override
	public String toString()
	{
		return "XMLResponse ContentType=" + contentType + "\n" + theXML;
	}

	/**
	 * @return
	 */
	public InputStream getInputStream()
	{
		XMLDoc d = getXMLDoc();
		if (d != null)
		{
			theBuffer = new ByteArrayIOStream();
			try
			{
				d.write2Stream(theBuffer, 2, false);
			}
			catch (IOException x)
			{
				theBuffer = null;
			}
			theXML = null;
		}
		return theBuffer == null ? null : theBuffer.getInputStream();
	}

	/**
	 * @return
	 */
	public boolean hasContent()
	{
		InputStream is = getInputStream();
		try
		{
			return is != null && is.read() >= 0;
		}
		catch (IOException x)
		{
			return false;
		}
	}

	/**
	 * format currentTimeMillis() to mmm dd -HHH:mm:ss
	 * @param milliSeconds
	 * @return A String that formats a milliseconds (currentTimeMillis()) to a date
	 */
	public static String formatLong(final long milliSeconds)
	{

		return milliSeconds <= 0 ? " - " : new JDFDate(milliSeconds).getFormattedDateTime("MMM dd - HH:mm:ss");
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

	/**
	 *
	 * @param sr the servlet response to serialize into
	 */
	public void writeResponse(HttpServletResponse sr)
	{
		try
		{
			sr.setContentType(getContentType());
			ServletOutputStream outputStream = sr.getOutputStream();
			InputStream inputStream = getInputStream(); // note that getInputStream optionally serializes the XMLResponse xml document
			sr.setContentLength(getContentLength());
			if (inputStream != null)
			{
				IOUtils.copy(inputStream, outputStream);
			}
			outputStream.flush();
			outputStream.close();
		}
		catch (final EofException e)
		{
			log.warn("EOF writing to stream: ");
		}
		catch (final IOException e)
		{
			log.error("cannot write to stream: ", e);
		}
	}
}
