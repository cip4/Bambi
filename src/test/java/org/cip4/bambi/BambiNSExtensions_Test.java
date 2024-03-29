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

package org.cip4.bambi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.junit.Test;

public class BambiNSExtensions_Test extends BambiTestCase
{

	private JDFQueueEntry buildQueueEntry()
	{
		final JDFDoc doc = new JDFDoc("JMF");
		final JDFJMF root = doc.getJMFRoot();
		final JDFResponse resp = root.appendResponse();
		resp.setType("Status");
		final JDFQueueEntry qe = resp.getCreateQueue(0).getCreateQueueEntry(0);
		BambiNSExtension.setDeviceID(qe, "someDeviceID");
		BambiNSExtension.setDeviceURL(qe, "someDeviceURL");
		BambiNSExtension.setDocURL(qe, "foo");
		BambiNSExtension.setReturnJMF(qe, "bar");
		BambiNSExtension.setReturnURL(qe, "someReturnURL");

		return qe;
	}

	@Test
	public void testAddBambiExtensions()
	{
		final JDFQueueEntry qe = buildQueueEntry();
		assertEquals("someDeviceID", BambiNSExtension.getDeviceID(qe));
		assertEquals("someDeviceURL", BambiNSExtension.getDeviceURL(qe));
		assertEquals("foo", BambiNSExtension.getDocURL(qe));
		assertEquals("bar", BambiNSExtension.getReturnJMF(qe));
		assertEquals("someReturnURL", BambiNSExtension.getReturnURL(qe));
	}

	@Test
	public void testQueue()
	{
		JDFQueue q = (JDFQueue) JDFQueue.createRoot(ElementName.QUEUE);
		assertEquals(1, BambiNSExtension.incrmentTotal(q));
		assertEquals(2, BambiNSExtension.incrmentTotal(q));
	}

	@Test
	public void testQueue2()
	{
		JDFQueue q = (JDFQueue) JDFQueue.createRoot(ElementName.QUEUE);
		q.appendQueueEntry();
		assertEquals(2, BambiNSExtension.incrmentTotal(q));
	}

	@Test
	public void testJSON()
	{
		KElement e = KElement.createRoot("a", null);
		assertFalse(BambiNSExtension.isJSON(e));
		assertFalse(BambiNSExtension.isJSON(null));
		BambiNSExtension.setJSON(e, true);
		assertTrue(BambiNSExtension.isJSON(e));
		try
		{
			BambiNSExtension.setJSON(null, true);
		}
		catch (JDFException b)
		{
			return;
		}
		fail("no exception");
	}

	@Test
	public void testQueue3()
	{
		assertEquals(0, BambiNSExtension.incrmentTotal(null));
	}

	@Test
	public void testRemoveBambiExtensions()
	{
		final JDFQueueEntry qe = buildQueueEntry();
		BambiNSExtension.removeBambiExtensions(qe);

		assertFalse(qe.hasAttributeNS(BambiNSExtension.MY_NS, BambiNSExtension.deviceID));
		assertFalse(qe.hasAttributeNS(BambiNSExtension.MY_NS, BambiNSExtension.deviceURL));
		assertFalse(qe.hasAttributeNS(BambiNSExtension.MY_NS, BambiNSExtension.docURL));
		assertFalse(qe.hasAttributeNS(BambiNSExtension.MY_NS, BambiNSExtension.returnJMF));
		assertFalse(qe.hasAttributeNS(BambiNSExtension.MY_NS, BambiNSExtension.returnURL));
	}

}
