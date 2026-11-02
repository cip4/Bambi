/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2026 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.util.UrlUtil;
import org.junit.Test;
import org.mockito.Mockito;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;

public class HTTPResponseTest extends BambiTestCaseBase
{
	private static class BufferingServletOutputStream extends ServletOutputStream
	{
		private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

		@Override
		public void write(final int b)
		{
			bos.write(b);
		}

		@Override
		public boolean isReady()
		{
			return true;
		}

		@Override
		public void setWriteListener(final WriteListener writeListener)
		{
			// no async writes in tests
		}

		String getText()
		{
			return new String(bos.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	private static class SimpleHTTPResponse extends HTTPResponse
	{
		private final String body;

		SimpleHTTPResponse(final String body, final String contentType)
		{
			super();
			this.body = body;
			setContentType(contentType);
		}

		SimpleHTTPResponse(final HTTPResponse response, final String body)
		{
			super(response);
			this.body = body;
		}

		@Override
		protected void fillbuffer()
		{
			if (body == null)
			{
				return;
			}
			try
			{
				getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
			}
			catch (final IOException x)
			{
				throw new IllegalStateException(x);
			}
		}
	}

	@Test
	public void testWriteResponseWithHeadersAndBody() throws IOException
	{
		final SimpleHTTPResponse response = new SimpleHTTPResponse("abc", "text/plain");
		response.setHeader("X-Test", "123");
		response.setHttpRC(202, "accepted");

		final HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);
		final BufferingServletOutputStream out = new BufferingServletOutputStream();
		Mockito.when(servletResponse.getOutputStream()).thenReturn(out);

		response.writeResponse(servletResponse);

		Mockito.verify(servletResponse).setHeader("X-Test", "123");
		Mockito.verify(servletResponse).setStatus(202);
		Mockito.verify(servletResponse).setContentType("text/plain");
		Mockito.verify(servletResponse).setContentLength(3);
		assertEquals("abc", out.getText());
		assertTrue(response.hasContent());
	}

	@Test
	public void testWriteResponseSendsHttpErrorForNonOK() throws IOException
	{
		final SimpleHTTPResponse response = new SimpleHTTPResponse("ignored", "text/plain");
		response.setHttpRC(503, "service unavailable");
		final HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);

		response.writeResponse(servletResponse);

		Mockito.verify(servletResponse).sendError(503, "service unavailable");
		Mockito.verify(servletResponse, Mockito.never()).getOutputStream();
	}

	@Test
	public void testCopyConstructorKeepsStatusAndHeaders() throws IOException
	{
		final SimpleHTTPResponse source = new SimpleHTTPResponse("src", "text/plain");
		source.setHeader("X-Copy", "yes");
		source.setHttpRC(207, "multi");
		final SimpleHTTPResponse copy = new SimpleHTTPResponse(source, "copy");

		final HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);
		final BufferingServletOutputStream out = new BufferingServletOutputStream();
		Mockito.when(servletResponse.getOutputStream()).thenReturn(out);
		copy.writeResponse(servletResponse);

		Mockito.verify(servletResponse).setHeader("X-Copy", "yes");
		Mockito.verify(servletResponse).setStatus(207);
		assertEquals("copy", out.getText());
	}

	@Test
	public void testXMLResponseGetXMLAndSerialization()
	{
		final XMLResponse xmlResponse = new XMLResponse(KElement.createRoot("JMF"));
		assertNotNull(xmlResponse.getXML());
		assertNotNull(xmlResponse.getInputStream());
		assertNull(xmlResponse.getXML());
		assertEquals(UrlUtil.TEXT_XML, xmlResponse.getContentType());
	}
}
