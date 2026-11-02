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
package org.cip4.bambi.core.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.BambiTestDevice;
import org.cip4.jdflib.auto.JDFAutoNotification.EnumClass;
import org.cip4.jdflib.core.JDFComment;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFResponse;
import org.junit.Test;

public class JMFHandlerTest extends BambiTestCaseBase
{

	private static class DummyHandler extends JMFHandler.AbstractHandler
	{
		private final int returnCode;
		private boolean handled;

		DummyHandler(final String messageType, final EnumFamily[] families, final int returnCode)
		{
			super(messageType, families);
			this.returnCode = returnCode;
			handled = false;
		}

		@Override
		public boolean handleMessage(final JDFMessage jmfMessage, final JDFResponse jmfResponse)
		{
			handled = true;
			if (jmfResponse != null)
			{
				jmfResponse.setReturnCode(returnCode);
			}
			return true;
		}
	}

	@Test
	public void testErrorResponse()
	{
		JMFHandler.errorResponse(null, "foo", 42, EnumClass.Error);
		final JDFResponse r = new JDFDoc("JMF").getJMFRoot().appendResponse(EnumType.FlushQueue);
		JMFHandler.errorResponse(r, "foo", 42, EnumClass.Warning);
		assertEquals(42, r.getReturnCode());
	}

	@Test
	public void testErrorResponseException()
	{
		JMFHandler.errorResponse(null, "foo", 42, EnumClass.Error);
		final JDFResponse r = new JDFDoc("JMF").getJMFRoot().appendResponse(EnumType.FlushQueue);
		JMFHandler.errorResponse(r, "foo", 42, EnumClass.Warning, new JDFException("foo"));
		assertEquals(42, r.getReturnCode());
		final JDFComment comment = r.getNotification().getComment("DeviceText", 0);
		assertNotNull(comment);
	}

	@Test
	public void testErrorResponseNotification()
	{
		JMFHandler.errorResponse(null, "foo", 42, EnumClass.Error);
		final JDFResponse r = new JDFDoc("JMF").getJMFRoot().appendResponse(EnumType.FlushQueue);
		JMFHandler.errorResponse(r, "foo", 42, EnumClass.Warning);
		assertNotNull(r.getNotification().getCommentText());
	}

	@Test
	public void testUnhandled()
	{
		final JMFHandler h = new JMFHandler(new BambiTestDevice(false));
		final JDFResponse r = new JDFDoc("JMF").getJMFRoot().appendResponse(EnumType.FlushQueue);
		h.unhandledMessage(r, r);
		assertEquals(5, r.getReturnCode());
	}

	@Test
	public void testAddGetAndDispatchSpecificHandler()
	{
		final JMFHandler handler = new JMFHandler(new BambiTestDevice(false));
		final DummyHandler queryStatus = new DummyHandler(EnumType.Status.getName(), new EnumFamily[] { EnumFamily.Query }, 17);
		handler.addHandler(queryStatus);
		assertSame(queryStatus, handler.getMessageHandler(EnumType.Status.getName(), EnumFamily.Query));

		final JDFJMF queryJmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.Status);
		final JDFJMF responseJmf = JDFJMF.createJMF(EnumFamily.Response, EnumType.Status);
		assertTrue(handler.handleMessage(queryJmf.getQuery(0), responseJmf.getResponse(0)));
		assertTrue(queryStatus.handled);
		assertEquals(17, responseJmf.getResponse(0).getReturnCode());
	}

	@Test
	public void testDispatchFallsBackToWildcardHandler()
	{
		final JMFHandler handler = new JMFHandler(new BambiTestDevice(false));
		final DummyHandler wildcardQuery = new DummyHandler("*", new EnumFamily[] { EnumFamily.Query }, 23);
		handler.addHandler(wildcardQuery);

		final JDFJMF queryJmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.QueueStatus);
		final JDFJMF responseJmf = JDFJMF.createJMF(EnumFamily.Response, EnumType.QueueStatus);
		assertTrue(handler.handleMessage(queryJmf.getQuery(0), responseJmf.getResponse(0)));
		assertTrue(wildcardQuery.handled);
		assertEquals(23, responseJmf.getResponse(0).getReturnCode());
	}

	@Test
	public void testMessageTypeEqualsAndHashCode()
	{
		final JMFHandler handler = new JMFHandler(new BambiTestDevice(false));
		final JMFHandler.MessageType key1 = handler.new MessageType(EnumType.Status, EnumFamily.Query);
		final JMFHandler.MessageType key2 = handler.new MessageType(EnumType.Status.getName(), EnumFamily.Query);
		final JMFHandler.MessageType key3 = handler.new MessageType(EnumType.Status, EnumFamily.Signal);
		assertEquals(key1, key2);
		assertEquals(key1.hashCode(), key2.hashCode());
		assertNotEquals(key1, key3);
		assertNotNull(key1.toString());
	}

	@Test
	public void testCallURLEqualityAndNormalization()
	{
		final JMFFactory.CallURL c1 = new JMFFactory.CallURL("http://example.com/a/b/c?x=1");
		final JMFFactory.CallURL c2 = new JMFFactory.CallURL("http://example.com/a");
		assertEquals(c1, c2);
		assertEquals(c1.hashCode(), c2.hashCode());
		assertNotNull(c1.getURL());
	}

	@Test
	public void testTriggerEqualityAndWaitQueuedNullArray()
	{
		final Trigger t1 = new Trigger("channelA", 1);
		final Trigger t2 = new Trigger("channelA", 9);
		final Trigger t3 = new Trigger("channelB", 1);
		assertEquals(t1, t2);
		assertNotEquals(t1, t3);
		assertTrue(Trigger.waitQueued(null, 5));
		assertNotNull(t1.toString());
	}
}
