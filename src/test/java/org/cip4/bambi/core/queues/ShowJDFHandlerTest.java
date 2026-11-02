/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2026 The International Cooperation for the Integration of
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
package org.cip4.bambi.core.queues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.bambi.workers.sim.SimDevice;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.util.UrlUtil;
import org.junit.Test;

public class ShowJDFHandlerTest extends BambiTestCaseBase
{

	private ContainerRequest createShowJDFRequest()
	{
		final ContainerRequest req = new ContainerRequest();
		req.setRequestURI("http://host/bambi/showJDF");
		return req;
	}

	@Test
	public void testProcessFileDefault()
	{
		final ShowJDFHandler h = new ShowJDFHandler(mock(SimDevice.class));
		final File f = new File(sm_dirTestData + "IDPSimplex.jdf");
		final ContainerRequest req = createShowJDFRequest();
		final XMLResponse xml = h.processFile(req, f);

		assertNotNull(xml);
		assertNotNull(xml.getXML());
		assertEquals(UrlUtil.TEXT_XML, xml.getContentType());
	}

	@Test
	public void testProcessFileRaw()
	{
		final ShowJDFHandler h = new ShowJDFHandler(mock(SimDevice.class));
		final File f = new File(sm_dirTestData + "IDPSimplex.jdf");
		final ContainerRequest req = createShowJDFRequest();
		req.setParameter("raw", "true");

		final XMLResponse xml = h.processFile(req, f);

		assertNotNull(xml);
		assertNull(xml.getXML());
		assertEquals(UrlUtil.TEXT_XML, xml.getContentType());
		assertTrue(xml.hasContent());
	}

	@Test
	public void testProcessFileJDF()
	{
		final ShowJDFHandler h = new ShowJDFHandler(mock(SimDevice.class));
		final File f = new File(sm_dirTestData + "IDPSimplex.jdf");
		final ContainerRequest req = createShowJDFRequest();
		req.setParameter(QueueProcessor.isJDF, "true");

		final XMLResponse xml = h.processFile(req, f);

		assertNotNull(xml);
		assertNotNull(xml.getXML());
		assertEquals(UrlUtil.VND_JDF, xml.getContentType());
	}

	@Test
	public void testProcessFileRepair()
	{
		final ShowJDFHandler h = new ShowJDFHandler(mock(SimDevice.class));
		final File f = new File(sm_dirTestData + "Elk_ConventionalPrinting.jdf");
		final ContainerRequest req = createShowJDFRequest();
		req.setParameter("repair", "true");

		final XMLResponse xml = h.processFile(req, f);

		assertNotNull(xml);
		assertNotNull(xml.getXML());
		assertEquals(UrlUtil.TEXT_XML, xml.getContentType());
	}

	@Test
	public void testIsMyRequestJobPart()
	{
		final ShowJDFHandler h = new ShowJDFHandler(mock(SimDevice.class));

		final ContainerRequest reqShowJDF = createShowJDFRequest();
		assertTrue(h.isMyRequest(reqShowJDF));

		final ContainerRequest reqJobPart = createShowJDFRequest();
		reqJobPart.setParameter(AttributeName.JOBPARTID, "SB__000");
		assertFalse(h.isMyRequest(reqJobPart));
	}

	@Test
	public void testHandleGetUnknownQE()
	{
		final SimDevice device = mock(SimDevice.class);
		when(device.getUpdatedFile(anyString())).thenReturn(null);

		final ShowJDFHandler h = new ShowJDFHandler(device);
		final ContainerRequest req = createShowJDFRequest();
		req.setParameter(QueueProcessor.QE_ID, "unknownQE");

		final XMLResponse xml = h.handleGet(req);
		final KElement root = xml == null ? null : xml.getXML();

		assertNotNull(xml);
		assertNotNull(root);
		assertEquals(404, xml.getHttpRC());
		assertEquals("BambiError", root.getLocalName());
	}

	@Test
	public void testHandleGetReadsFile()
	{
		final SimDevice device = mock(SimDevice.class);
		when(device.getUpdatedFile(anyString())).thenReturn(sm_dirTestData + "IDPSimplex.jdf");

		final ShowJDFHandler h = new ShowJDFHandler(device);
		final ContainerRequest req = createShowJDFRequest();
		req.setParameter(QueueProcessor.QE_ID, "qe1");

		final XMLResponse xml = h.handleGet(req);

		assertNotNull(xml);
		assertNotNull(xml.getXML());
		assertEquals(UrlUtil.TEXT_XML, xml.getContentType());
	}
}