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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.core.BambiNSExtension.EPackageType;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.UrlUtil.URLProtocol;
import org.junit.Test;
import org.mockito.Mockito;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 *         16.11.2009
 */
public class ContainerRequestTest extends BambiTestCaseBase
{
	private static class ExposedContainerRequest extends ContainerRequest
	{
		void applyRequest(final HttpServletRequest request)
		{
			apply(request);
		}
	}

	/**
	 *
	 */
	public ContainerRequestTest()
	{
		super();
	}

	/**
	 *
	 */
	@Test
	public void testBearer1()
	{
		final ContainerRequest req = new ContainerRequest();
		final JDFAttributeMap m1 = new JDFAttributeMap(UrlUtil.AUTHORIZATION, "Bearer a");
		req.setHeaderMap(m1);
		assertEquals("a", req.getBearerToken());
	}

	/**
	 *
	 */
	@Test
	public void testGetDumpHeader()
	{
		final ContainerRequest req = new ContainerRequest();
		assertNotNull(req.getDumpHeader());
		final JDFAttributeMap m1 = new JDFAttributeMap(UrlUtil.AUTHORIZATION, "Bearer a");
		req.setHeaderMap(m1);
		assertEquals("a", req.getBearerToken());
		assertNotNull(req.getDumpHeader());
	}

	/**
	 *
	 */
	@Test
	public void testGetDumpHeaderAuth()
	{
		final ContainerRequest req = new ContainerRequest();
		assertNotNull(req.getDumpHeader());
		final JDFAttributeMap m1 = new JDFAttributeMap(UrlUtil.AUTHORIZATION, "Bearer a");
		req.setHeaderMap(m1);
		assertEquals(-1, req.getDumpHeader().indexOf("Bearer a"));
		assertNotEquals(-1, req.getDumpHeader().indexOf("Bearer **removed**"));
	}

	/**
	 *
	 */
	@Test
	public void testBearer()
	{
		final ContainerRequest req = new ContainerRequest();
		final JDFAttributeMap m1 = new JDFAttributeMap("access_token", "abc");
		req.setParameterMap(m1);
		assertEquals("abc", req.getBearerToken());
	}

	/**
	 *
	 */
	@Test
	public void testgetContext()
	{
		final ContainerRequest req = new ContainerRequest();
		req.setRequestURI("http://host/foo/bar/dev");
		assertEquals("bar", req.getContext());
	}

	/**
	 *
	 */
	@Test
	public void testSetparameter()
	{
		final ContainerRequest req = new ContainerRequest();
		req.setRequestURI("http://host/foo/bar/dev");
		req.setParameter("a", "b");
		req.setParameter("c", "d");
		assertEquals("b", req.getParameter("a"));
		assertEquals("d", req.getParameter("c"));
		req.setParameter("c", "e");
		assertEquals("e", req.getParameter("c"));
	}

	/**
	 *
	 */
	@Test
	public void testgetDevice()
	{
		final ContainerRequest req = new ContainerRequest();
		req.setRequestURI("http://host/foo/bar/dev");
		assertEquals("dev", req.getDeviceID());
		req.setRequestURI("http://host/foo/bar/dev?k");
		assertEquals("dev", req.getDeviceID());
	}

	/**
	 *
	 */
	@Test
	public void testisDevice()
	{
		final ContainerRequest req = new ContainerRequest();
		req.setRequestURI("http://host/foo/bar/dev");
		assertTrue(req.isMyRequest("DEV"));
		assertTrue(req.isMyRequest("dev"));
		assertFalse(req.isMyRequest("bar"));
		req.setRequestURI("http://host/foo/bar/dev?BLUB");
		assertTrue(req.isMyRequest("DEV"));
		assertTrue(req.isMyRequest("dev"));
		assertFalse(req.isMyRequest("bar"));
	}

	/**
	*
	*/
	@Test
	public void testisContext()
	{
		final ContainerRequest req = new ContainerRequest();
		req.setRequestURI("http://host/foo/bar/dev");
		assertTrue(req.isMyContext("bar"));
		assertTrue(req.isMyContext("BAR"));
	}

	/**
	*
	*/
	@Test
	public void testgetParameter()
	{
		final ContainerRequest req = new ContainerRequest();
		req.setRequestURI("http://host/foo/bar/dev");
		req.setParameterMap(new JDFAttributeMap("A", "B"));
		assertEquals("B", req.getParameter("a"));
		assertEquals("B", req.getParameter("A"));
	}

	/**
	*
	*/
	@Test
	public void testgetParameterMap()
	{
		final ContainerRequest req = new ContainerRequest();
		req.setRequestURI("http://host/foo/bar/dev");
		req.setParameterMap(new JDFAttributeMap("A", "B"));
		assertEquals("B", req.getParameterMap().get("A"));
	}

	/**
	*
	*/
	@Test
	public void testgetLongParameter()
	{
		final ContainerRequest req = new ContainerRequest();
		req.setRequestURI("http://host/foo/bar/dev");
		final long currentTimeMillis = System.currentTimeMillis();
		req.setParameterMap(new JDFAttributeMap("A", "" + currentTimeMillis));
		assertEquals(currentTimeMillis, req.getLongParam("a"));
	}

	/**
	 *
	 */
	@Test
	public void testToString()
	{
		final ContainerRequest req = new ContainerRequest();
		final String requestURI = "http://host/foo/bar/dev";
		req.setRequestURI(requestURI);
		assertTrue(req.toString().contains(requestURI));
		assertTrue(req.shortString().contains(requestURI));
	}

	/**
	 *
	 */
	@Test
	public void testToString2()
	{
		final ContainerRequest req = new ContainerRequest();
		final String requestURI = "http://host/foo/bar/dev";
		req.setRequestURI(requestURI);
		assertFalse(req.toString().contains("}"));
	}

	/**
	 *
	 */
	@Test
	public void testToString3()
	{
		final ContainerRequest req = new ContainerRequest();
		final String requestURI = "http://host/foo/bar/dev";
		req.setParameter("a", "b");
		req.setRequestURI(requestURI);
		assertTrue(req.toString().contains("}"));
		assertFalse(req.shortString().contains("}"));
	}

	/**
	 *
	 */
	@Test
	public void testGetCompleteURI()
	{
		final ContainerRequest req = new ContainerRequest();
		final String requestURI = "http://host/foo/bar/dev";
		req.setParameter("a", "b");
		req.setRequestURI(requestURI);
		assertEquals("http://host/foo/bar/dev?a=b", req.getCompleteURI());
	}

	/**
	 *
	 */
	@Test
	public void testGetProtocol()
	{
		final ContainerRequest req = new ContainerRequest();
		final String requestURI = "http://host/foo/bar/dev";
		req.setParameter("a", "b");
		req.setRequestURI(requestURI);
		assertEquals(URLProtocol.http, req.getURLProtocol());
	}

	/**
	 *
	 */
	@Test
	public void testGetCompleteURIResort()
	{
		final ContainerRequest req = new ContainerRequest();
		final String requestURI = "http://host/foo/bar/dev";
		req.setRequestURI(requestURI);
		assertEquals("http://host/foo/bar/dev", req.getCompleteURI("d"));
		req.setParameter("C", "c0");
		req.setParameter("f", "g");
		req.setParameter("a", "b");
		req.setParameter("d", "e");
		assertEquals("http://host/foo/bar/dev?d=e&a=b&C=c0&f=g", req.getCompleteURI("d"));
	}

	@Test
	public void testBooleanParamAndContentTypeBranches()
	{
		final ContainerRequest req = new ContainerRequest();
		req.setParameter("enabled", "true");
		req.setParameter("disabled", "false");
		assertTrue(req.getBooleanParam("enabled"));
		assertFalse(req.getBooleanParam("disabled", true));
		assertTrue(req.getBooleanParam("missing", true));
		assertFalse(req.getBooleanParam("missing"));

		req.setContentType(" application/xml ; charset=UTF-8 ");
		assertEquals("application/xml", req.getContentType(true));

		req.setOriginalContentType("application/json; charset=UTF-8");
		req.setOriginalContentType("text/plain");
		assertEquals("application/json", req.getOriginalContentType());
	}

	@Test
	public void testApplyParsesHeadersAndParams()
	{
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getContentType()).thenReturn("application/xml; charset=UTF-8");
		Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://host/foo/bar/dev"));
		Mockito.when(request.getRemoteHost()).thenReturn("remote.host");
		Mockito.when(request.getMethod()).thenReturn("post");

		Mockito.when(request.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList("X-Trace", UrlUtil.AUTHORIZATION)));
		Mockito.when(request.getHeaders("X-Trace")).thenReturn(Collections.enumeration(Arrays.asList("a", "b")));
		Mockito.when(request.getHeaders(UrlUtil.AUTHORIZATION)).thenReturn(Collections.enumeration(Arrays.asList("Bearer abc")));

		final Map<String, String[]> parameterMap = new HashMap<>();
		parameterMap.put("alpha", new String[] { "1", "2" });
		parameterMap.put("blank", new String[] { "" });
		parameterMap.put("nullEntry", null);
		Mockito.when(request.getParameterMap()).thenReturn(parameterMap);

		final ExposedContainerRequest req = new ExposedContainerRequest();
		req.applyRequest(request);

		assertEquals("application/xml", req.getContentType(true));
		assertEquals("application/xml", req.getOriginalContentType());
		assertEquals("a,b", req.getHeader("x-trace"));
		assertEquals("Bearer abc", req.getHeader(UrlUtil.AUTHORIZATION));
		assertEquals("1,2", req.getParameter("alpha"));
		assertNull(req.getParameter("blank"));
		assertTrue(req.isPost());
		assertEquals("remote.host", req.getRemoteHost());
	}

	@Test
	public void testSetContainerCopiesMeta()
	{
		final ContainerRequest source = new ContainerRequest();
		source.setRequestURI("http://host/foo/bar/dev");
		source.setContentType("text/plain; charset=UTF-8");
		source.setOriginalContentType("application/xml; charset=UTF-8");
		source.setMethod("GET");
		source.setRemoteHost("hostA");
		source.setName("source");
		source.setHeaderMap(new JDFAttributeMap("X-Test", "value"));
		source.setParameterMap(new JDFAttributeMap("p", "v"));
		source.setPackageType(EPackageType.RAW);

		final ContainerRequest target = new ContainerRequest();
		target.setPackageType(EPackageType.JMF);
		target.setContainer(source);

		assertEquals("http://host/foo/bar/dev", target.getRequestURI());
		assertTrue(target.getContentType(false).toLowerCase().startsWith("text/plain"));
		assertEquals("application/xml", target.getOriginalContentType());
		assertEquals("GET", target.getMethod());
		assertEquals("hostA", target.getRemoteHost());
		assertEquals("source", target.getName());
		assertEquals("value", target.getHeader("x-test"));
		assertEquals("v", target.getParameter("P"));
		assertEquals(EPackageType.JMF, target.getPackageType());
	}

}
