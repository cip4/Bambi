/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2023 The International Cooperation for the Integration of
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
/**
 * (C) 2020-2021 Heidelberger Druckmaschinen AG
 */
package org.cip4.bambi.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.ByteArrayIOStream.ByteArrayIOInputStream;
import org.cip4.lib.jdf.jsonutil.JSONWriter;
import org.junit.Test;

public class NetResponseTest
{

	@Test
	public void testJSon()
	{
		final KElement e = KElement.createRoot("a", null);
		e.setAttribute("b", 1, null);
		final NetResponse r = new NetResponse(e);
		r.setJSON(true);
		final ByteArrayIOInputStream is = ByteArrayIOStream.getBufferedInputStream(r.getInputStream());
		final String actual = new String(is.getBuf());
		assertEquals("{\"a\":{\"b\":1}}", actual);
	}

	@Test
	public void testToString()
	{
		final KElement e = KElement.createRoot("a", null);
		e.setAttribute("b", 1, null);
		final NetResponse r = new NetResponse(e);
		r.toString();
	}

	@Test
	public void testValueList()
	{
		final KElement e = KElement.createRoot("a", null);
		e.setAttribute(AttributeName.VALUELIST, "a1 a2");
		final NetResponse r = new NetResponse(e);
		r.setJSON(true);
		final ByteArrayIOInputStream is = ByteArrayIOStream.getBufferedInputStream(r.getInputStream());
		final String actual = new String(is.getBuf());
		assertEquals("{\"a\":{\"ValueList\":[\"a1\",\"a2\"]}}", actual);
	}

	@Test
	public void testJSonRC()
	{
		final KElement e = KElement.createRoot("a", null);
		e.setAttribute("b", 1, null);
		final NetResponse r = new NetResponse(e);
		r.setJSON(true);
		r.setErrorRC(false);
		r.setHttpRC(400, null);
		final ByteArrayIOInputStream is = ByteArrayIOStream.getBufferedInputStream(r.getInputStream());
		final String actual = new String(is.getBuf());
		assertEquals("{\"a\":{\"b\":1}}", actual);
	}

	@Test
	public void testJSonRC2()
	{
		final KElement e = KElement.createRoot("a", null);
		e.setAttribute("b", 1, null);
		final NetResponse r = new NetResponse(null);
		r.setJSON(true);
		r.setErrorRC(false);
		r.setHttpRC(400, "evil");
		final ByteArrayIOInputStream is = ByteArrayIOStream.getBufferedInputStream(r.getInputStream());
		final String actual = new String(is.getBuf());
		assertEquals("{\"rc\":400,\"error\":\"evil\"}", actual);
	}

	@Test
	public void testJSonWriter()
	{
		final KElement e = KElement.createRoot("a", null);
		e.setAttribute("b", 1, null);
		final NetResponse r = new NetResponse(e);
		JSONWriter jw1 = r.setJSON(true);
		final KElement e2 = KElement.createRoot("a2", null);
		final NetResponse r2 = new NetResponse(e2);
		JSONWriter jw2 = r2.setJSON(true);
		jw2.addArray("foo");
		assertFalse(jw1.equals(jw2));
		assertNull(r2.setJSON(false));
	}

}
