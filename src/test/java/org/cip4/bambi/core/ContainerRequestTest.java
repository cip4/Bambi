/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2020 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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
import static org.junit.Assert.assertTrue;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.util.UrlUtil;
import org.junit.Test;

/**
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 *
 *         16.11.2009
 */
public class ContainerRequestTest extends BambiTestCaseBase
{

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
	public void tesSetparameter()
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

}
