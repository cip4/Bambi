/*--------------------------------------------------------------------------------------------------
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2010 The International Cooperation for the Integration of
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
 */
package org.cip4.bambi.core.messaging;

import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.jmf.JDFAcknowledge;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.ThreadUtil.MyMutex;

/**
 * Handler class for Acknowledges - checks a message for an acknowledgeURL and makes any appropriate message handlers asynchronous <br/>
 * this class handles incoming asynchronous requests i.e. outgoing acknowledge messages (extern sent an AcknowledgeURL)
 * 
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * June 28, 2009
 */
public class AcknowledgeThread extends BambiLogFactory implements IMessageHandler
{
	private int waitForSych;
	protected static int runThreadCounter = 0; // used by RunThread for naming

	/**
	 * number of milliseconds to wait before going asynch (acknowledge) if<=0, ALWAYS go asynch
	 * @param waitForSych the waitForSych to set
	 */
	public void setWaitForSych(final int waitForSych)
	{
		this.waitForSych = waitForSych;
	}

	/**
	 * @param _baseHandler
	 * @param _device MUST NOT be null !
	 * @throws NullPointerException we ALWAYS need a base handler
	 */
	public AcknowledgeThread(final IMessageHandler _baseHandler, final AbstractDevice _device)
	{
		super();
		baseHandler = _baseHandler;
		device = _device;
		waitForSych = 1000; // 1 second wait prior to going asynch
		if (device == null)
		{
			throw new NullPointerException("device must not be null!");
		}
	}

	/**
	 * this is the thread that waits for the handler to finish and then sends the acknowledge
	 * 
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * June 28, 2009
	 */
	protected class RunThread extends Thread
	{
		/**
		 * @param inputMessage
		 * @param response
		 * @param ackURL
		 */
		public RunThread(final JDFMessage inputMessage, final JDFResponse response, final String ackURL)
		{
			super("RunThread_" + runThreadCounter++);
			this.inputMessage = inputMessage;
			this.response = response;
			this.ackURL = ackURL;
			waitMutex = new MyMutex();
		}

		private final JDFMessage inputMessage;
		private final JDFResponse response;
		private final String ackURL;
		private MyMutex waitMutex;

		/**
		 * let the base handler do its thing...<br/>
		 * note that any changes here must be reflected in @see handleWithAcknowledge
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			// due to the signature of handlemessage, we do some weird copying between resp and ack. messy but efficient.
			baseHandler.handleMessage(inputMessage, response);
			if (waitMutex != null) // someone is still waiting
			{
				synchronized (waitMutex)
				{
					final MyMutex copy = waitMutex;
					waitMutex = null;
					ThreadUtil.notifyAll(copy);
					// someone is still waiting for the response - go for synch handling

				}
			}
			else if (response != null)
			{
				final JDFJMF jmf = response.getJMFRoot();
				final JDFAcknowledge ack = jmf.getAcknowledge(0);
				ack.mergeElement(response, true); // make sure any previous stuff is still alive
				response.deleteNode();
				ack.setType(ack.getType());
				device.sendJMF(jmf, ackURL, null);
			}
		}

		/**
		 * @param millis
		 * @return true if the waited process has completed
		 */
		protected boolean waitHandled(final int millis)
		{
			ThreadUtil.wait(waitMutex, millis);
			final boolean b = waitMutex == null;
			waitMutex = null;
			return b;
		}
	}

	/**
	 * handles acknowledges - also waits max 1 second for synchronous handling, if possible
	 * @param response
	 * @param inputMessage
	 * @return 
	 * 
	 */
	private boolean handleWithAcknowledge(final JDFMessage inputMessage, final JDFResponse response)
	{
		log.info("handling message asynchronously: " + inputMessage.getType());
		JDFResponse r2 = null;
		if (response != null)
		{
			final JDFAcknowledge ack = response.splitAcknowledge();
			// hack to adhere to IMessageHandler Interface
			r2 = ack.getJMFRoot().appendResponse();
			r2.mergeElement(ack, false);
		}
		final String ackURL = inputMessage.getAttribute(AttributeName.ACKNOWLEDGEURL, null, null);

		final RunThread thread = new RunThread(inputMessage, r2, ackURL);
		thread.start();

		// if we complete within the synch time, we undo everything and send back a synch response
		if (waitForSych > 0)
		{
			final boolean b = thread.waitHandled(waitForSych);
			if (b && response != null && r2 != null)
			{
				response.removeAttribute(AttributeName.ACKNOWLEDGED);
				r2.removeAttribute(AttributeName.XSITYPE);
				r2.removeAttribute(AttributeName.ACKNOWLEDGETYPE);
				response.mergeElement(r2, true);
			}
		}
		return true;
	}

	protected final IMessageHandler baseHandler;
	protected AbstractDevice device;

	/**
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#getFamilies()
	 */
	public EnumFamily[] getFamilies()
	{
		return baseHandler.getFamilies();
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#getMessageType()
	 */
	public String getMessageType()
	{
		return baseHandler.getMessageType();
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
	 */
	public boolean handleMessage(final JDFMessage inputMessage, final JDFResponse response)
	{
		if (baseHandler == null || inputMessage == null)
		{
			log.error("null handler or inputmessage - bailing out");
			return false;
		}
		// we handle any synchronous requests in this thread
		final String ackURL = inputMessage.getAttribute(AttributeName.ACKNOWLEDGEURL, null, null);
		if (ackURL == null)
		{
			log.info("handling message synchronously: " + inputMessage.getType());
			return baseHandler.handleMessage(inputMessage, response);
		}
		else
		{
			return handleWithAcknowledge(inputMessage, response);
		}

	}

	/**
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#isSubScribable()
	 */
	public boolean isSubScribable()
	{
		return false;
	}

	/**
	 * this one abviously handles acknowledges...
	 * @return true if acknowledges are handled
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#isSubScribable()
	 */
	public boolean isAcknowledge()
	{
		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "AcknowledgeThread: base=" + baseHandler.toString();
	}

}
