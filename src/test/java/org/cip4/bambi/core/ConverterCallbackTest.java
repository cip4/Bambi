/*
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2023 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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

import java.io.InputStream;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.extensions.XJDFConstants;
import org.cip4.jdflib.extensions.XJDFHelper;
import org.cip4.jdflib.jmf.JMFBuilderFactory;
import org.cip4.jdflib.util.UrlUtil;
import org.junit.Test;

public class ConverterCallbackTest extends BambiTestCaseBase
{

	/**
	 *
	 */
	@Test
	public void testGetJDFInputStream()
	{
		final ConverterCallback cb = new ConverterCallback();
		final JDFDoc d = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf");
		assertNotNull(d);
		final InputStream is = cb.getJDFExternStream(d);
		assertNotNull(is);
		final JDFDoc d2 = JDFDoc.parseStream(is);
		assertNotNull(d2);
	}

	/**
	 *
	 */
	@Test
	public void testCopyCtor()
	{
		final ConverterCallback cb = new ConverterCallback();
		cb.setFixToBambi(EnumVersion.Version_2_0);
		final ConverterCallback cb2 = new ConverterCallback(cb);
		assertEquals(cb.getFixToBambi(), cb2.getFixToBambi());
	}

	/**
	 *
	 */
	@Test
	public void testClone()
	{
		final ConverterCallback cb = new ConverterCallback();
		cb.setFixToBambi(EnumVersion.Version_2_0);
		final ConverterCallback cb2 = cb.clone();
		assertEquals(cb.getFixToBambi(), cb2.getFixToBambi());
	}

	/**
	 *
	 */
	@Test
	public void testCloneJSON()
	{
		final ConverterCallback cb = new ConverterCallback();
		cb.setFixToBambi(EnumVersion.Version_2_0);
		cb.setJSON(true);
		final ConverterCallback cb2 = cb.clone();
		assertEquals(cb.isJSON(), cb2.isJSON());
	}

	/**
	 *
	 */
	@Test
	public void testGetCallbackDetails()
	{
		final ConverterCallback cb = new ConverterCallback();
		cb.setFixToBambi(EnumVersion.Version_2_0);
		cb.setJSON(true);
		assertTrue(cb.getCallbackDetails().getBool(ConverterCallback.IS_JSON, false));
	}

	/**
	 *
	 */
	@Test
	public void testCloneNull()
	{
		final ConverterCallback cb = new ConverterCallback(null);
		assertNotNull(cb);
	}

	/**
	 *
	 */
	@Test
	public void testGetJDFContentType()
	{
		final ConverterCallback cb = new ConverterCallback();
		assertEquals(cb.getJDFContentType(), UrlUtil.VND_JDF);
		cb.setFixToExtern(EnumVersion.Version_2_0);
		assertEquals(cb.getJDFContentType(), UrlUtil.VND_XJDF);
		cb.setJSON(true);
		assertEquals(cb.getJDFContentType(), UrlUtil.VND_XJDF_J);
	}

	/**
	 *
	 */
	@Test
	public void testGetJMFContentType()
	{
		final ConverterCallback cb = new ConverterCallback();
		assertEquals(cb.getJMFContentType(), UrlUtil.VND_JMF);
		cb.setFixToExtern(EnumVersion.Version_2_0);
		assertEquals(cb.getJMFContentType(), UrlUtil.VND_XJMF);
		cb.setFixToExtern(EnumVersion.Version_2_1);
		assertEquals(cb.getJMFContentType(), UrlUtil.VND_XJMF);
		cb.setJSON(true);
		assertEquals(cb.getJMFContentType(), UrlUtil.VND_XJMF_J);
	}

	/**
	 *
	 */
	@Test
	public void testGetJMFInputStream()
	{
		final ConverterCallback cb = new ConverterCallback();
		final JDFDoc d = JMFBuilderFactory.getJMFBuilder(null).buildAbortQueueEntry("q").getOwnerDocument_JDFElement();
		assertNotNull(d);
		final InputStream is = cb.getJMFExternStream(d);
		assertNotNull(is);
		final JDFDoc d2 = JDFDoc.parseStream(is);
		assertNotNull(d2);
	}

	/**
	 *
	 */
	@Test
	public void testGetXJDFInputStream()
	{
		final ConverterCallback cb = new ConverterCallback();
		cb.setFixToExtern(EnumVersion.Version_2_0);
		final JDFDoc d = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf");
		assertNotNull(d);
		final InputStream is = cb.getJDFExternStream(d);
		assertNotNull(is);
		final JDFDoc d2 = JDFDoc.parseStream(is);
		assertNotNull(d2);
		assertEquals(XJDFConstants.XJDF, d2.getRoot().getLocalName());
	}

	/**
	 *
	 */
	@Test
	public void testGetXJMFInputStream()
	{
		final ConverterCallback cb = new ConverterCallback();
		cb.setFixToExtern(EnumVersion.Version_2_0);
		final JDFDoc d = JMFBuilderFactory.getJMFBuilder(null).buildAbortQueueEntry("q").getOwnerDocument_JDFElement();
		final InputStream is = cb.getJMFExternStream(d);
		assertNotNull(is);
		final JDFDoc d2 = JDFDoc.parseStream(is);
		assertNotNull(d2);
		assertEquals(XJDFConstants.XJMF, d2.getRoot().getLocalName());
	}

	/**
	 *
	 */
	@Test
	public void testGetXJMFInputStreamNull()
	{
		final ConverterCallback cb = new ConverterCallback();
		cb.setFixToExtern(EnumVersion.Version_2_0);
		final JDFDoc d = new JDFDoc(ElementName.JMF);
		final InputStream is = cb.getJMFExternStream(d);
		assertNull(is);
	}

	/**
	 *
	 */
	@Test
	public void testExportXJMF()
	{
		final ConverterCallback cb = new ConverterCallback();
		cb.setFixToExtern(EnumVersion.Version_2_0);
		assertNull(cb.exportXJMF(null));
		assertNull(cb.exportXJMF(new JDFDoc(ElementName.JMF)));
	}

	/**
	 *
	 */
	@Test
	public void testRoundTripXJDF()
	{
		final ConverterCallback cb = new ConverterCallback();
		XJDFHelper h = new XJDFHelper("j1", "j2");
		JDFDoc d = new JDFDoc(h.getRoot().getOwnerDocument_KElement());
		JDFDoc d2 = cb.prepareJDFForBambi(d);
		assertEquals("j1", d2.getJDFRoot().getJobID(true));
		JDFDoc d3 = cb.updateJDFForExtern(d2);
		assertEquals("j1", XJDFHelper.getHelper(d3).getJobID());
	}

	/**
	 *
	 */
	@Test
	public void testExportXJDF()
	{
		final ConverterCallback cb = new ConverterCallback();
		cb.setFixToExtern(EnumVersion.Version_2_0);
		assertNull(cb.exportXJDF(null));
		assertEquals(XJDFConstants.XJDF, cb.exportXJDF(new JDFDoc(ElementName.JDF)).getRoot().getNodeName());
	}

}
