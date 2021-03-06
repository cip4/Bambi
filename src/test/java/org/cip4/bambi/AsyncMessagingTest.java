/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2008 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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

package org.cip4.bambi;

import static org.junit.Assert.assertEquals;

import java.net.HttpURLConnection;

import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.messaging.IResponseHandler;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.MessageSender;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumDeviceDetails;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumJobDetails;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.util.ByteArrayIOStream;

public class AsyncMessagingTest extends BambiTestCase implements IResponseHandler
{
	protected VString messageIDs = null;
	HttpURLConnection c;

	/**
	 *
	 * @see org.cip4.bambi.BambiTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		messageIDs = new VString();
	}

	/**
	 *
	 * 
	 * @throws InterruptedException
	 */
	public void testSendQueueStatus() throws InterruptedException
	{
		final MessageSender messageSender = JMFFactory.getInstance().getCreateMessageSender(getWorkerURL());
		for (int i = 0; i < 10; i++)
		{
			final JDFJMF stat = new JMFBuilder().buildStatus(EnumDeviceDetails.Brief, EnumJobDetails.Brief);
			final String msgID = stat.getMessageElement(null, null, 0).getID();
			messageSender.queueMessage(stat, this, getWorkerURL(), null, null);
			messageIDs.add(msgID);
		}

		// now wait until all responses have been received
		final int counter = 0;
		while (messageIDs.size() > 0 && counter < 10)
		{
			Thread.sleep(1000);
		}
		assertEquals(0, messageIDs.size());
	}

	/**
	 *
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#handleMessage()
	 */
	@Override
	public boolean handleMessage()
	{
		return true;
	}

	/**
	 * 
	 *
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#getFamilies()
	 */
	public EnumFamily[] getFamilies()
	{
		return new EnumFamily[] { EnumFamily.Response };
	}

	/**
	 * 
	 *
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#getMessageType()
	 */
	public EnumType getMessageType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 *
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#getConnection()
	 */
	@Override
	public HttpURLConnection getConnection()
	{
		return c;
	}

	/**
	 * 
	 *
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#setConnection(java.net.HttpURLConnection)
	 */
	@Override
	public void setConnection(final HttpURLConnection con)
	{
		c = con;

	}

	/**
	 *
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#setBufferedStream(org.cip4.jdflib.util.ByteArrayIOStream)
	 */
	@Override
	public void setBufferedStream(final ByteArrayIOStream bis)
	{
		// TODO Auto-generated method stub

	}

	/**
	 *
	 * TODO Please insert comment!
	 * 
	 * @param response
	 */
	public void setResponse(final JDFMessage response)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#setCallBack(org.cip4.bambi.core.IConverterCallback)
	 */
	@Override
	public void setCallBack(final IConverterCallback back)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#getFinalMessage()
	 */
	@Override
	public JDFMessage getFinalMessage()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#getResponse()
	 */
	@Override
	public JDFResponse getResponse()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#isAborted()
	 */
	@Override
	public boolean isAborted()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#setMessage(org.cip4.jdflib.jmf.JDFMessage)
	 */
	@Override
	public void setMessage(final JDFMessage response)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#waitHandled(int, boolean)
	 */
	@Override
	public void waitHandled(final int milliSeconds, final int milliSeconds2, final boolean abortTimeOut)
	{
		// TODO Auto-generated method stub

	}

}
