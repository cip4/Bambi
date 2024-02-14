/**
 * The CIP4 Software License, Version 1.0
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.HttpURLConnection;

import org.cip4.jdflib.util.ThreadUtil;
import org.junit.Test;

public class MessageResponseHandlerTest
{

	class WaitThread implements Runnable
	{

		private final int w;
		private final MessageResponseHandler mrh;

		WaitThread(final int w, final MessageResponseHandler mrh)
		{
			this.w = w;
			this.mrh = mrh;
		}

		@Override
		public void run()
		{
			ThreadUtil.sleep(w);
			mrh.finalizeHandling();

		}

	}

	@Test
	public void testWaitHandled()
	{
		final MessageResponseHandler mrh = new MessageResponseHandler("32");
		mrh.waitHandled(3, 3, false);
		assertFalse(mrh.isAborted());
	}

	@Test
	public void testGetBuffered()
	{
		final MessageResponseHandler mrh = new MessageResponseHandler("32");
		assertNull(mrh.getBufferedStream());
		mrh.setConnection(mock(HttpURLConnection.class));
		assertNotNull(mrh.getBufferedStream());
	}

	@Test
	public void testWaitHandledC()
	{
		final MessageResponseHandler mrh = new MessageResponseHandler("32");
		mrh.setConnection(mock(HttpURLConnection.class));
		mrh.waitHandled(3, 3, false);
		assertFalse(mrh.isAborted());
	}

	@Test
	public void testWaitHandled0()
	{
		final MessageResponseHandler mrh = new MessageResponseHandler("32");
		mrh.waitHandled(0, 0, false);
		assertFalse(mrh.isAborted());
	}

	@Test
	public void testToString()
	{
		final MessageResponseHandler mrh = new MessageResponseHandler("32");
		assertNotNull(mrh.toString());
	}

	@Test
	public void testSetConnect()
	{
		final MessageResponseHandler mrh = new MessageResponseHandler("32");
		assertEquals(null, mrh.getConnection());
		final HttpURLConnection c = mock(HttpURLConnection.class);
		mrh.setConnection(c);
		assertEquals(c, mrh.getConnection());
	}

	@Test
	public void testFinalizeHandling()
	{
		final MessageResponseHandler mrh = new MessageResponseHandler("32");
		mrh.finalizeHandling();
		mrh.finalizeHandling();
	}

	@Test
	public void testWaitHandledAbort()
	{
		final MessageResponseHandler mrh = new MessageResponseHandler("32");
		mrh.waitHandled(3, 3, true);
		assertTrue(mrh.isAborted());
	}

	@Test
	public void testWaitHandled2()
	{
		final MessageResponseHandler mrh = new MessageResponseHandler("32");
		final long t = System.currentTimeMillis();
		new Thread(new WaitThread(42, mrh)).start();
		assertFalse(mrh.isAborted());
		mrh.waitHandled(3000, 3000, false);
		assertEquals(1234l, System.currentTimeMillis() - t, 1234l);
		assertFalse(mrh.isAborted());
	}

}
