/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2022 The International Cooperation for the Integration of 
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
package org.cip4.bambi.proxy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cip4.bambi.proxy.AbstractProxyDevice.KnownMessagesResponseHandler;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFMessageService;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.util.StringUtil;

/**
 * class to check against knownmessages prior to sending
 * 
 * 
 * @author rainer prosi
 * @date Sep 28, 2010
 */
class MessageChecker
{
	private Map<String, KnownMessageDetails> theMessages;
	private final AbstractProxyDevice abstractProxyDevice;

	/**
	 * @param abstractProxyDevice
	 * 
	 * 
	 */
	public MessageChecker(AbstractProxyDevice abstractProxyDevice)
	{
		super();
		this.abstractProxyDevice = abstractProxyDevice;
		setMessages(null);
	}

	/**
	 * update the knownmessages list
	 */
	public void updateKnownMessages()
	{
		final JMFBuilder builder = abstractProxyDevice.getBuilderForSlave();
		final JDFJMF knownMessages = builder.buildKnownMessagesQuery();
		KnownMessagesResponseHandler handler = abstractProxyDevice.new KnownMessagesResponseHandler(knownMessages);
		boolean sent = abstractProxyDevice.sendJMFToSlave(knownMessages, handler);
		if (sent)
		{
			handler.waitHandled(20000, 30000, true);
			setMessages(handler.completeHandling());
		}
	}

	/**
	 * Setter for theMessages attribute.
	 * 
	 * @param messages the {@link KnownMessageDetails} to set
	 */
	private void setMessages(Collection<KnownMessageDetails> messages)
	{
		if (messages == null)
		{
			theMessages = null;
		}
		else
		{
			theMessages = new HashMap<String, MessageChecker.KnownMessageDetails>();
			for (KnownMessageDetails det : messages)
			{
				theMessages.put(det.getType(), det);
			}
		}
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "MessageChecker: " + theMessages;
	}

	public static class KnownMessageDetails
	{
		/**
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "MessageDetails: Type=" + type;
		}

		private final String type;

		/**
		 * @param ms
		 */
		protected KnownMessageDetails(JDFMessageService ms)
		{
			type = StringUtil.getNonEmpty(ms == null ? null : ms.getType());
		}

		/**
		 * 
		 * get the message type attribute
		 * 
		 * @return the message type attribute
		 */
		public String getType()
		{
			return type;
		}

	}

	/**
	 * do I know this message type?
	 * 
	 * @param type
	 * @return true if known OR KnownMessages is unknown
	 */
	public boolean knows(EnumType type)
	{
		return type == null ? false : knows(type.getName());
	}

	/**
	 * 
	 * return true if we are initialized
	 * 
	 * @return
	 */
	public boolean isInitialized()
	{
		return theMessages != null;
	}

	/**
	 * do I know this message
	 * 
	 * @param name
	 * @return true if known OR KnownMessages is unknown
	 */
	boolean knows(String name)
	{
		if (theMessages == null)
			return true;

		return theMessages.get(name) != null;
	}

}