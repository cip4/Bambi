/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2025 The International Cooperation for the Integration of
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.server.BambiServer;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.UrlUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 *         16.02.2009
 */
public class BambiServletTest extends BambiTestCase
{
	@Before
	public void setUpServletState()
	{
		final BambiContainer container = BambiContainer.getInstance();
		if (container != null)
		{
			container.shutDown();
		}
		BambiContainer.removeInstance();
		bambiContainer = null;
	}

	@After
	public void tearDownServletState()
	{
		final BambiContainer container = BambiContainer.getInstance();
		if (container != null)
		{
			container.shutDown();
		}
		BambiContainer.removeInstance();
		bambiContainer = null;
	}

	private BambiServer createAuthenticatedServer()
	{
		final BambiServer server = mock(BambiServer.class);
		when(server.isAuthenticated(any(), any())).thenReturn(true);
		return server;
	}

	private static class ByteArrayServletInputStream extends ServletInputStream
	{
		private final ByteArrayInputStream bis;

		ByteArrayServletInputStream(final byte[] body)
		{
			bis = new ByteArrayInputStream(body == null ? new byte[0] : body);
		}

		@Override
		public int read()
		{
			return bis.read();
		}

		@Override
		public boolean isFinished()
		{
			return bis.available() <= 0;
		}

		@Override
		public boolean isReady()
		{
			return true;
		}

		@Override
		public void setReadListener(final ReadListener readListener)
		{
			// no async reads in unit tests
		}
	}

	private static class ByteArrayServletOutputStream extends ServletOutputStream
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
			// no async writes in unit tests
		}

		String asString()
		{
			return new String(bos.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	private HttpServletRequest createRequest(final String method, final String requestURL, final String contentType, final byte[] body) throws IOException
	{
		final HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getMethod()).thenReturn(method);
		when(request.getRequestURL()).thenReturn(new StringBuffer(requestURL));
		when(request.getRequestURI()).thenReturn(requestURL);
		when(request.getPathInfo()).thenReturn(requestURL);
		when(request.getContentType()).thenReturn(contentType);
		when(request.getRemoteHost()).thenReturn("localhost");
		when(request.getScheme()).thenReturn("http");
		when(request.getLocalPort()).thenReturn(8080);
		when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
		when(request.getParameterMap()).thenReturn(new HashMap<>());
		when(request.getInputStream()).thenReturn(new ByteArrayServletInputStream(body));
		return request;
	}

	private HttpServletResponse createResponse(final ByteArrayServletOutputStream out) throws IOException
	{
		final HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getOutputStream()).thenReturn(out);
		return response;
	}

	private byte[] getJMFBody() throws IOException
	{
		final JDFJMF jmf = new JMFBuilder().buildKnownMessagesQuery();
		final ByteArrayIOStream ios = new ByteArrayIOStream();
		jmf.getOwnerDocument_JDFElement().write2Stream(ios, 0, true);
		return ios.getBuf();
	}

	public BambiServletTest()
	{
		super();
		wantContainer = true;
	}

	/**
	 * @throws Exception
	 * @throws IOException
	 * @throws Exception
	 */
	@Test
	public void testCreate() throws Exception
	{
		final BambiServer s = createAuthenticatedServer();
		assertNotNull(new BambiServlet(s));
	}

	@Test
	public void testDoGetOverviewWritesResponse() throws Exception
	{
		startContainer();
		final BambiServer s = createAuthenticatedServer();
		final BambiServlet bs = new BambiServlet(s);
		final HttpServletRequest req = createRequest("GET", "http://dummy:8080/war/overview/" + deviceID, null, null);
		final ByteArrayServletOutputStream out = new ByteArrayServletOutputStream();
		final HttpServletResponse response = createResponse(out);

		bs.doGet(req, response);

		verify(response).setStatus(200);
		verify(response).setContentType(anyString());
		verify(response).setContentLength(anyInt());
		assertTrue(out.asString().length() > 0);
	}

	@Test
	public void testDoGetUnknownContextWrites404ErrorBody() throws Exception
	{
		startContainer();
		final BambiServer s = createAuthenticatedServer();
		final BambiServlet bs = new BambiServlet(s);
		final HttpServletRequest req = createRequest("GET", "http://dummy:8080/war/noSuchContext/" + deviceID, null, null);
		final ByteArrayServletOutputStream out = new ByteArrayServletOutputStream();
		final HttpServletResponse response = createResponse(out);

		bs.doGet(req, response);

		verify(response).setStatus(404);
		assertTrue(out.asString().contains("BambiError"));
	}

	@Test
	public void testDoGetShowQueueRouteWritesResponse() throws Exception
	{
		startContainer();
		final BambiServer s = createAuthenticatedServer();
		final BambiServlet bs = new BambiServlet(s);
		final HttpServletRequest req = createRequest("GET", "http://dummy:8080/war/showQueue/" + deviceID, null, null);
		final ByteArrayServletOutputStream out = new ByteArrayServletOutputStream();
		final HttpServletResponse response = createResponse(out);

		bs.doGet(req, response);

		verify(response).setStatus(200);
		assertTrue(out.asString().length() > 0);
	}

	@Test
	public void testDoPostJMFUsesProcessStream() throws Exception
	{
		startContainer();
		final BambiServer s = createAuthenticatedServer();
		final BambiServlet bs = new BambiServlet(s);
		final HttpServletRequest req = createRequest("POST", "http://dummy:8080/war/jmf/" + deviceID, UrlUtil.VND_JMF, getJMFBody());
		final ByteArrayServletOutputStream out = new ByteArrayServletOutputStream();
		final HttpServletResponse response = createResponse(out);

		bs.doPost(req, response);

		verify(response).setStatus(200);
		verify(response).setContentType(UrlUtil.VND_JMF);
		assertTrue(out.asString().contains("JMF"));
	}

	@Test
	public void testDoPostUnknownContentTypeReturnsJMFError() throws Exception
	{
		startContainer();
		final BambiServer s = createAuthenticatedServer();
		final BambiServlet bs = new BambiServlet(s);
		final HttpServletRequest req = createRequest("POST", "http://dummy:8080/war/jmf/" + deviceID, "text/plain", "junk".getBytes(StandardCharsets.UTF_8));
		final ByteArrayServletOutputStream out = new ByteArrayServletOutputStream();
		final HttpServletResponse response = createResponse(out);

		bs.doPost(req, response);

		verify(response).setStatus(200);
		verify(response).setContentType(UrlUtil.VND_JMF);
		assertTrue(out.asString().contains("Unknown HTTP ContentType"));
	}

	@Test
	public void testAuthMessage() throws Exception
	{
		startContainer();
		final BambiServer s = createAuthenticatedServer();
		final BambiServlet bs = new BambiServlet(s);
		final HttpServletRequest req = mock(HttpServletRequest.class);
		assertEquals("BASIC", bs.getAuthMessage(req).substring(0, 5));
		when(req.getMethod()).thenReturn("Post");
		assertEquals("Not authenticated", bs.getAuthMessage(req));
	}

	@Test
	public void testDoGetPostReturnsFalseWithoutContainer() throws Exception
	{
		final BambiServer s = createAuthenticatedServer();
		final BambiServlet bs = new BambiServlet(s);
		BambiContainer.removeInstance();
		final HttpServletRequest req = createRequest("GET", "http://dummy:8080/war/overview/" + deviceID, null, null);
		final ByteArrayServletOutputStream out = new ByteArrayServletOutputStream();
		final HttpServletResponse response = createResponse(out);

		assertFalse(bs.doGetPost(req, response, false));
		assertEquals(0, out.asString().length());
	}

	@Test
	public void testAuthResp() throws Exception
	{
		startContainer();
		final BambiServer s = createAuthenticatedServer();
		when(s.isAuthenticated(any(), any())).thenReturn(false);
		final BambiServlet bs = new BambiServlet(s);
		final HttpServletRequest getReq = createRequest("GET", "http://dummy:8080/war/overview/" + deviceID, null, null);
		final HttpServletResponse getResp = mock(HttpServletResponse.class);
		assertTrue(bs.doGetPost(getReq, getResp, false));
		verify(getResp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		verify(getResp).setHeader(anyString(), anyString());

		final HttpServletRequest postReq = createRequest("POST", "http://dummy:8080/war/jmf/" + deviceID, UrlUtil.VND_JMF, getJMFBody());
		final HttpServletResponse postResp = mock(HttpServletResponse.class);
		assertTrue(bs.doGetPost(postReq, postResp, true));
		verify(postResp).sendError(401, "Not authenticated");
	}

	/**
	 * @throws BambiException
	 */
	@Test
	public void testParseEnv() throws BambiException
	{
		System.setProperty("foo", "/a/b/c");
		assertEquals(BambiServlet.parseEnv("%foo/bar"), "/a/b/c/bar");
		assertEquals("/bar", BambiServlet.parseEnv("/bar"));
		assertNull(BambiServlet.parseEnv(null));
	}
}
