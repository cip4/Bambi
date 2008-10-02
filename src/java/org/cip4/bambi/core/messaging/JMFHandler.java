/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2008 The International Cooperation for the Integration of 
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
package org.cip4.bambi.core.messaging;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.SignalDispatcher;
import org.cip4.jdflib.auto.JDFAutoMessageService.EnumJMFRole;
import org.cip4.jdflib.auto.JDFAutoNotification.EnumClass;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessageService;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.util.ContainerUtil;

/**
 * 
 * @author rainer
 */
public class JMFHandler implements IMessageHandler, IJMFHandler
{
	protected static final Log log = LogFactory.getLog(JMFHandler.class.getName());
	/**
	 * attribute to ignore warnings for unhandled messages
	 */
	public final static String subscribed = "IgnoreSubscribe";

	protected class MessageType
	{
		public String type;
		public EnumFamily family;

		/**
		 * @param typ
		 * @param _family
		 */
		public MessageType(EnumType typ, EnumFamily _family)
		{
			type = typ.getName();
			family = _family;
		}

		public MessageType(String typ, EnumFamily _family)
		{
			type = typ;
			family = _family;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object arg0)
		{
			if (!(arg0 instanceof MessageType))
				return false;
			MessageType mt = (MessageType) arg0;
			return ContainerUtil.equals(family, mt.family) && ContainerUtil.equals(type, mt.type);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return (type == null ? 0 : type.hashCode()) + (family == null ? 0 : family.hashCode());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "MessageType :" + type + " " + family;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8902151736245089033L;
	protected HashMap<MessageType, IMessageHandler> messageMap; // key = type ,
	// value =
	// IMessageHandler
	// TODO handle subscriptions
	protected HashMap<EnumType, IMessageHandler> subscriptionMap; // key = type
	// , value =
	// subscriptions
	// handled
	protected SignalDispatcher _signalDispatcher;
	protected boolean bFilterOnDeviceID = false;
	protected AbstractDevice _parentDevice = null;

	/**
	 * 
	 * handler for the knownmessages query
	 */
	protected class KnownMessagesHandler extends AbstractHandler
	{

		/**
		 */
		public KnownMessagesHandler()
		{
			super(EnumType.KnownMessages, new EnumFamily[] { EnumFamily.Query });
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.
		 * JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(JDFMessage m, JDFResponse resp)
		{
			if (m == null || resp == null)
			{
				return false;
			}
			log.debug("Handling" + m.getType());
			return handleKnownMessages(m, resp);
		}

		/**
		 * create the KnownMessages Response from the internal hashMap
		 * @param m 
		 * @param resp 
		 * 
		 * @return true if handled correctly
		 */
		private boolean handleKnownMessages(JDFMessage m, JDFMessage resp)
		{
			if (m == null)
				return false;
			if (!EnumFamily.Query.equals(m.getFamily()))
				return false;

			Iterator<MessageType> it = messageMap.keySet().iterator();
			HashMap<String, Vector<EnumFamily>> hTypeFamily = new HashMap<String, Vector<EnumFamily>>();
			while (it.hasNext())
			{
				MessageType typ = it.next();
				if (hTypeFamily.get(typ.type) == null)
				{
					Vector<EnumFamily> v = new Vector<EnumFamily>();
					v.add(typ.family);
					hTypeFamily.put(typ.type, v);
				}
				else
				{
					Vector<EnumFamily> v = hTypeFamily.get(typ.type);
					v.add(typ.family);
				}
			}

			Iterator<Entry<String, Vector<EnumFamily>>> iTyp = hTypeFamily.entrySet().iterator();
			while (iTyp.hasNext())
			{
				String typ = iTyp.next().getKey();
				log.debug("Known Message: " + typ);
				if (KElement.isWildCard(typ))
					continue; // skip "*"

				JDFMessageService ms = resp.appendMessageService();
				ms.setType(typ);
				ms.setFamilies(hTypeFamily.get(typ));
				ms.setJMFRole(EnumJMFRole.Receiver);
				ms.setURLSchemes(new VString("http", null));
				if (subscriptionMap.get(typ) != null)
					ms.setPersistent(true);
			}
			return true;
		}
	}

	/**
	 * 
	 * handler for the StopPersistentChannel command
	 */
	public static abstract class AbstractHandler implements IMessageHandler
	{
		protected String type = null;
		protected EnumFamily[] families = null;

		/**
		 * @param _type the message type
		 * @param _families the array of families
		 */
		public AbstractHandler(EnumType _type, EnumFamily[] _families)
		{
			type = _type.getName();
			families = _families;
		}

		/**
		 * @param _type the message type
		 * @param _families the array of families
		 */
		public AbstractHandler(String _type, EnumFamily[] _families)
		{
			type = _type;
			families = _families;
		}

		/**
		 * @see org.cip4.bambi.core.messaging.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 * @param inputMessage
		 * @param response
		 * @return true if handled
		 */
		public abstract boolean handleMessage(JDFMessage inputMessage, JDFResponse response);

		/**
		 * @see org.cip4.bambi.core.messaging.IMessageHandler#getFamilies()
		 * @return the array of families
		 */
		final public EnumFamily[] getFamilies()
		{
			return families;
		}

		/**
		 * @see org.cip4.bambi.core.messaging.IMessageHandler#getMessageType()
		 * @return the message type string
		 */
		final public String getMessageType()
		{
			return type;
		}
	}

	/**
	 * @param dev
	 */
	public JMFHandler(AbstractDevice dev)
	{
		super();
		messageMap = new HashMap<MessageType, IMessageHandler>();
		subscriptionMap = new HashMap<EnumType, IMessageHandler>();
		addHandler(this.new KnownMessagesHandler());
		_signalDispatcher = null;
		_parentDevice = dev;

	}

	/**
	 * add a message handler
	 * 
	 * @param handler
	 *            the handler associated with the event
	 */
	public void addHandler(IMessageHandler handler)
	{
		String typ = handler.getMessageType();
		EnumFamily[] families = handler.getFamilies();
		if (typ == null || families == null)
		{
			log.error("Unknown message type or family in addhandler - bailing out ");
			return;
		}
		for (int i = 0; i < families.length; i++)
			messageMap.put(new MessageType(typ, families[i]), handler);
	}

	/**
	 * return a handler for a given type and family.
	 * 
	 * @param typ
	 *            the message type, "*" is a wildcard that will be called in
	 *            case no individual handler exists
	 * @param family
	 *            the family
	 * @return the handler, null if none exists
	 */
	public IMessageHandler getHandler(String typ, EnumFamily family)
	{
		IMessageHandler h = messageMap.get(new MessageType(typ, family));
		if (h == null)
			h = messageMap.get(new MessageType("*", family));
		return h;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IJMFHandler#addSubscriptionHandler(org.cip4.jdflib.jmf.JDFMessage.EnumType, org.cip4.bambi.core.messaging.IMessageHandler)
	 * @param typ
	 * @param handler
	 */
	public void addSubscriptionHandler(EnumType typ, IMessageHandler handler)
	{
		subscriptionMap.put(typ, handler);
	}

	/**
	 * the big processing dispatcher
	 * 
	 * @param doc
	 *            the JDFDoc holding the JMF which is to be processed
	 * @return the JDFDoc holding the JMF response
	 */
	public JDFDoc processJMF(JDFDoc doc)
	{
		JDFJMF jmf = doc.getJMFRoot();
		JDFJMF jmfResp = jmf.createResponse();
		VElement vMess = jmf.getMessageVector(null, null);
		final int messSize = vMess.size();
		for (int i = 0; i < messSize; i++)
		{
			JDFMessage m = (JDFMessage) vMess.elementAt(i);
			String id = m.getID();

			JDFResponse mResp = (JDFResponse) (id == null ? null : jmfResp.getChildWithAttribute(ElementName.RESPONSE, AttributeName.REFID, null, id, 0, true));
			if (mResp == null)
			{
				log.error("no response provided ??? " + id + " " + jmf);
			}
			if (_signalDispatcher != null)
				_signalDispatcher.findSubscription(m, mResp);
			handleMessage(m, mResp);

			if (m instanceof JDFSignal && mResp != null)
			{
				int retCode = mResp.getReturnCode();
				if (retCode == 0)
				{
					mResp.deleteNode();
				}
			}
		}
		vMess = jmfResp.getMessageVector(null, null);
		if (vMess != null && vMess.size() > 0)
		{
			jmfResp.setSenderID(getSenderID());
			jmfResp.setICSVersions((_parentDevice).getICSVersions());
			jmfResp.collectICSVersions();
			return jmfResp.getOwnerDocument_JDFElement();
		}
		else
			return null;
	}

	/**
	 * standard handler for unimplemented messages
	 * 
	 * @param m
	 * @param resp
	 */
	private void unhandledMessage(JDFMessage m, JDFResponse resp)
	{
		errorResponse(resp, "Message not handled: " + m.getType() + "; Family: " + m.getFamily().getName() + " id="
				+ m.getID(), 5, EnumClass.Error);
	}

	/**
	 * standard error message creator
	 * 
	 * @param resp the response to make an error
	 * @param text the explicit error text
	 * @param rc  the jmf response returncode
	 * @param errorClass TODO
	 */
	public static void errorResponse(JDFResponse resp, String text, int rc, EnumClass errorClass)
	{
		if (resp != null)
		{
			resp.setReturnCode(rc);
			resp.setErrorText(text, errorClass);
		}
		log.warn("JMF error: rc=" + rc + " " + text);
	}

	/**
	 * we do not call these for ourselves...
	 * @return the list of families
	 */
	public EnumFamily[] getFamilies()
	{
		return null;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#getMessageType()
	 * @return the type
	 */
	public String getMessageType()
	{
		return null;
	}

	/**
	 * the handler implements itself as a generic handler
	 * @param inputMessage 
	 * @param response 
	 * @return true if handled
	 */
	public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
	{
		if (inputMessage == null)
			return false;
		try
		{
			final EnumFamily family = inputMessage.getFamily();
			final String typ = inputMessage.getType();
			if (bFilterOnDeviceID)
			{
				String deviceID = inputMessage.getJMFRoot().getDeviceID();
				if (!KElement.isWildCard(deviceID) && !ContainerUtil.equals(deviceID, getSenderID()))
					return false;
			}
			final IMessageHandler handler = getHandler(typ, family);
			boolean handled = handler != null;

			if (handler != null)
				handled = handler.handleMessage(inputMessage, response);
			if (!inputMessage.hasAttribute(subscribed) && !handled)
			{
				unhandledMessage(inputMessage, response);
			}
			VString icsVersions = _parentDevice.getICSVersions();
			if (icsVersions != null)
			{
				VString respVersions = response.getICSVersions();
				if (respVersions != null)
				{
					respVersions.appendUnique(icsVersions);
					response.setICSVersions(respVersions);
				}
				else
				{
					response.setICSVersions(icsVersions);
				}
			}
			return handled;
		}
		catch (Exception x)
		{
			log.error("Unhandled Exception in handleMessage", x);
			return false;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return the string representation
	 */
	@Override
	public String toString()
	{
		String msgMap = "MessageMap (size=" + messageMap.size() + ")=[" + messageMap.toString() + "]";
		String subMap = "SubsriptionMap (size=" + subscriptionMap.size() + ")=[" + subscriptionMap.toString() + "]";
		return "JMFHandler: " + msgMap + ", " + subMap;
	}

	/**
	 * @param signalDispatcher
	 */
	public void setDispatcher(SignalDispatcher signalDispatcher)
	{
		_signalDispatcher = signalDispatcher;

	}

	/**
	 * @return the senderid
	 */
	public String getSenderID()
	{
		return _parentDevice == null ? null : _parentDevice.getDeviceID();
	}

	/**
	 * @return true if we filter on deviceid
	 */
	public boolean isFilterOnDeviceID()
	{
		return bFilterOnDeviceID;
	}

	/**
	 * @param filterOnDeviceID
	 */
	public void setFilterOnDeviceID(boolean filterOnDeviceID)
	{
		bFilterOnDeviceID = filterOnDeviceID;
	}

}
