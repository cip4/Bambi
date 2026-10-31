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

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.UrlUtil;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class XMLResponse extends HTTPResponse
{
	private final static Log log = LogFactory.getLog(XMLResponse.class);

	/**
	 * @param theXML the xml to write - may be null
	 */
	public XMLResponse(final KElement theXML)
	{
		super();
		if (theXML != null)
		{
			this.theXML = theXML;
			setContentType(UrlUtil.TEXT_XML);
		}
		this.errorRC = true;
	}

	/**
	 * @param r
	 * @param contentType
	 */
	public XMLResponse(final XMLResponse r, final String contentType)
	{
		super(r);
		this.theXML = r.theXML;
		setContentType(getContentType());

	}

	private KElement theXML;
	private XMLDoc xslt;

	/**
	 * @return
	 */
	public KElement getXML()
	{
		return theXML;
	}

	/**
	 * @return the xslt used for server-side transformation
	 */
	public XMLDoc getXSLT()
	{
		return xslt;
	}

	/**
	 * @param xslt the optional xslt used for server-side transformation
	 */
	public void setXSLT(final XMLDoc xslt)
	{
		this.xslt = xslt;
	}

	public void ensureJDF()
	{
		if (theXML != null && !(theXML instanceof JDFElement))
		{
			theXML = new JDFDoc(theXML.getOwnerDocument()).getRoot();
		}
	}

	/**
	 * returns the OutputStream for this if theXML==null
	 *
	 * @return an OutputStream to write to, if xml==null, else null
	 */
	@Override
	public OutputStream getOutputStream()
	{
		if (theXML != null)
		{
			return null;
		}
		if (theBuffer == null)
		{
			theBuffer = new ByteArrayIOStream();
		}
		return theBuffer;
	}

	/**
	 * @return
	 */
	public XMLDoc getXMLDoc()
	{
		return theXML == null ? null
				: (theXML instanceof JDFElement) ? ((JDFElement) theXML).getOwnerDocument_JDFElement() : theXML.getOwnerDocument_KElement();
	}

	/**
	 * sets this xml to e, also removes any output stream that may be associated to this
	 *
	 * @param e the xml to set
	 */
	public void setXML(final KElement e)
	{
		theXML = e;
		theBuffer = null;
	}

	/**
	 * @return
	 */
	@Override
	public InputStream getInputStream()
	{
		if (theBuffer == null)
		{
			final XMLDoc d = getXMLDoc();
			if (d != null)
			{
				theBuffer = serializeDoc(d);
				theXML = null;
			}
		}
		return theBuffer == null ? null : theBuffer.getInputStream();
	}

	@Override
	protected void fillbuffer()
	{
		final XMLDoc d = getXMLDoc();
		if (d != null)
		{
			theBuffer = serializeDoc(d);
			theXML = null;
		}
	}

	private ByteArrayIOStream serializeDoc(final XMLDoc d)
	{
		if (d == null)
		{
			return null;
		}

		if (xslt != null)
		{
			final ByteArrayIOStream transformedBuffer = new ByteArrayIOStream();
			try
			{
				final TransformerFactory factory = TransformerFactory.newInstance();
				factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
				final DOMSource styleSource = new DOMSource(xslt.getMemberDocument());
				final String styleSystemID = xslt.getOriginalFileName();
				if (styleSystemID != null && !styleSystemID.isEmpty())
				{
					// systemId lets xsl:include resolve relative to the stylesheet location
					styleSource.setSystemId(styleSystemID);
				}
				final Transformer transformer = factory.newTransformer(styleSource);
				transformer.transform(new DOMSource(d.getMemberDocument()), new StreamResult(transformedBuffer));
				if (transformedBuffer.size() > 0)
				{
					setContentType(UrlUtil.TEXT_HTML);
					return transformedBuffer;
				}
				log.warn("XSLT transform returned empty output - falling back to raw XML serialization");
			}
			catch (final TransformerException x)
			{
				log.warn("error applying XSLT transform - falling back to raw XML serialization", x);
			}
			catch (final RuntimeException x)
			{
				log.warn("unexpected XSLT transform error - falling back to raw XML serialization", x);
			}
			setContentType(UrlUtil.TEXT_XML);
		}

		final ByteArrayIOStream rawBuffer = new ByteArrayIOStream();
		try
		{
			d.write2Stream(rawBuffer, 2, false);
			return rawBuffer;
		}
		catch (final IOException x)
		{
			return null;
		}
	}
}
