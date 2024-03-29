/*
 *
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
package org.cip4.bambi.core.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.junit.Test;

public class MessageIdentifierTest
{

	@Test
	public void testMessageIdentifier()
	{
		JMFBuilder jmfBuilder = new JMFBuilder();
		jmfBuilder.setSenderID("sender");
		JDFJMF jmf = jmfBuilder.buildQueueStatusSubscription("url");
		jmf.getMessageElement(null, null, 0).setSenderID("s3");
		MessageIdentifier mi = new MessageIdentifier(jmf.getMessageElement(null, null, 0), jmf.getDeviceID());
		assertNull(mi.deviceID);
	}

	@Test
	public void testToString()
	{
		JMFBuilder jmfBuilder = new JMFBuilder();
		jmfBuilder.setSenderID("sender");
		JDFJMF jmf = jmfBuilder.buildQueueStatusSubscription("url");
		jmf.getMessageElement(null, null, 0).setSenderID("s3");
		MessageIdentifier mi = new MessageIdentifier(jmf.getMessageElement(null, null, 0), jmf.getDeviceID());
		assertNotNull(mi.toString());
	}

	@Test
	public void testMatches()
	{
		JMFBuilder jmfBuilder = new JMFBuilder();
		jmfBuilder.setSenderID("sender");
		JDFJMF jmf = jmfBuilder.buildQueueStatusSubscription("url");
		jmf.getMessageElement(null, null, 0).setSenderID("s3");
		MessageIdentifier mi = new MessageIdentifier(jmf.getMessageElement(null, null, 0), jmf.getDeviceID());
		MessageIdentifier mi2 = new MessageIdentifier(jmf.getMessageElement(null, null, 0), jmf.getDeviceID());
		assertTrue(mi.matches(mi2));
		MessageIdentifier mi3 = mi.clone();
		mi3.msgType = "foo";
		assertFalse(mi.matches(mi3));
		assertFalse(mi.equals(mi3));
		assertFalse(mi.hashCode() == mi3.hashCode());
	}

	@Test
	public void testClone()
	{
		JMFBuilder jmfBuilder = new JMFBuilder();
		jmfBuilder.setSenderID("sender");
		JDFJMF jmf = jmfBuilder.buildQueueStatusSubscription("url");
		jmf.getMessageElement(null, null, 0).setSenderID("s3");
		MessageIdentifier mi = new MessageIdentifier(jmf.getMessageElement(null, null, 0), jmf.getDeviceID());
		MessageIdentifier mi2 = mi.clone();
		assertTrue(mi.matches(mi2));
		assertEquals(mi, mi2);
		assertEquals(mi.hashCode(), mi2.hashCode());
	}

	@Test
	public void testCloneChannel()
	{
		JMFBuilder jmfBuilder = new JMFBuilder();
		jmfBuilder.setSenderID("sender");
		JDFJMF jmf = jmfBuilder.buildQueueStatusSubscription("url");
		jmf.getMessageElement(null, null, 0).setSenderID("s3");
		MessageIdentifier mi = new MessageIdentifier(jmf.getMessageElement(null, null, 0), jmf.getDeviceID());
		assertNull(mi.cloneChannels(null));
		MessageIdentifier[] mi2 = mi.cloneChannels(new VString("a b c"));
		assertEquals("a", mi2[0].getMisChannelID());
		assertEquals("b", mi2[1].getMisChannelID());

	}

}
