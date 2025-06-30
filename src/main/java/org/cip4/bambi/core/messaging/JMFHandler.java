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

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.BambiException;
import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.jdflib.auto.JDFAutoMessageService.EnumJMFRole;
import org.cip4.jdflib.auto.JDFAutoNotification.EnumClass;
import org.cip4.jdflib.auto.JDFAutoSignal.EnumChannelMode;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFMessageService;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.resource.JDFNotification;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.EnumUtil;

/**
 * TBD: Add description.
 */
public class JMFHandler implements IMessageHandler, IJMFHandler
{

	private final static Log log = BambiLogFactory.getLog(JMFHandler.class);
	/**
	 * Attribute to ignore warnings for unhandled messages
	 */
	public final static String subscribed = "IgnoreSubscribe";

	/**
	 * Sub class for message types handling.
	 */
	protected class MessageType
	{
		public final String type;
		public final EnumFamily family;

		/**
		 * Custom constructor. Accepting multipe parameters for initiaizing.
		 */
		public MessageType(final EnumType type, final EnumFamily family)
		{
			this.type = type.getName();
			this.family = family;
		}

		/**
		 * Custom constructor. Accepting multipe parameters for initiaizing.
		 */
		public MessageType(final String type, final EnumFamily family)
		{
			this.type = type;
			this.family = family;
		}

		@Override
		public boolean equals(final Object arg0)
		{
			if (!(arg0 instanceof MessageType))
			{
				return false;
			}
			final MessageType messageType = (MessageType) arg0;
			return ContainerUtil.equals(family, messageType.family) && ContainerUtil.equals(type, messageType.type);
		}

		@Override
		public int hashCode()
		{
			return (type == null ? 0 : type.hashCode()) + (family == null ? 0 : family.hashCode());
		}

		@Override
		public String toString()
		{
			return "MessageType :" + type + " " + family;
		}
	}

	protected HashMap<MessageType, IMessageHandler> messageMap; // key = type ,
	protected SignalDispatcher signalDispatcher;
	protected boolean filterOnDeviceID;
	protected AbstractDevice device;
	private long messageCount;

	/**
	 * handler for the knownmessages query
	 */
	protected class KnownMessagesHandler extends AbstractHandler
	{

		/**
		 *
		 */
		public KnownMessagesHandler()
		{
			super(EnumType.KnownMessages, new EnumFamily[] { EnumFamily.Query });
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public boolean handleMessage(final JDFMessage jmfMessage, final JDFResponse resp)
		{
			if (jmfMessage == null || resp == null)
			{
				return false;
			}

			log.debug("Handling" + jmfMessage.getType());
			return handleKnownMessages(jmfMessage, resp);
		}

		/**
		 * Create the KnownMessages Response from the internal hashMap
		 *
		 * @return true if handling was successful.
		 */
		private boolean handleKnownMessages(final JDFMessage jmfMessage, final JDFMessage jmfResponse)
		{
			/*
			 * Helper class to collect data for filling into message service elements
			 */
			class MessageStuff
			{
				@Override
				public String toString()
				{
					return "MessageStuff [families=" + families + ", subscribe=" + subscribe + ", acknowledge=" + acknowledge + "]";
				}

				protected final Vector<EnumFamily> families;
				protected boolean subscribe;
				protected boolean acknowledge;

				protected MessageStuff()
				{
					families = new Vector<>();
					subscribe = false;
					acknowledge = false;
				}
			}

			if (jmfMessage == null)
			{
				return false;
			}
			if (!EnumFamily.Query.equals(jmfMessage.getFamily()))
			{
				return false;
			}

			final Vector<MessageType> messageTypes = ContainerUtil.getKeyVector(messageMap);
			final HashMap<String, MessageStuff> familyTypesMap = new HashMap<>();

			for (final MessageType messageType : messageTypes)
			{
				final IMessageHandler messageHandler = messageMap.get(messageType);
				if (familyTypesMap.get(messageType.type) == null)
				{
					final MessageStuff messageStuff = new MessageStuff();

					messageStuff.families.add(messageType.family);
					messageStuff.acknowledge = messageHandler.isAcknowledge();
					messageStuff.subscribe = messageHandler.isSubScribable();
					familyTypesMap.put(messageType.type, messageStuff);
				}
				else
				{
					final MessageStuff messageStuff = familyTypesMap.get(messageType.type);
					messageStuff.families.add(messageType.family);
					messageStuff.acknowledge = messageStuff.acknowledge || messageHandler.isAcknowledge();
					messageStuff.subscribe = messageStuff.subscribe || messageHandler.isSubScribable();
				}
			}

			for (final Entry<String, MessageStuff> stringMessageStuffEntry : familyTypesMap.entrySet())
			{
				final String familyType = stringMessageStuffEntry.getKey();

				if (KElement.isWildCard(familyType))
				{
					continue; // skip "*"
				}

				final JDFMessageService jdfMessageService = jmfResponse.appendMessageService();
				jdfMessageService.setType(familyType);

				final MessageStuff messageStuff = familyTypesMap.get(familyType);
				jdfMessageService.setFamilies(messageStuff.families);
				jdfMessageService.setJMFRole(EnumJMFRole.Receiver);
				jdfMessageService.setURLSchemes(new VString("http", null));
				jdfMessageService.setPersistent(messageStuff.subscribe);
				jdfMessageService.setAcknowledge(messageStuff.acknowledge);
			}
			return true;
		}
	}

	/**
	 * Handler for the StopPersistentChannel command
	 */
	public static abstract class AbstractHandler implements IMessageHandler
	{
		protected String messageType;
		protected EnumFamily[] families;
		protected final Log log;

		/**
		 * Custom constructor. Accepting multiple params for initializing.
		 *
		 * @param messageType The message type
		 * @param families The array of families
		 */
		public AbstractHandler(final EnumType messageType, final EnumFamily[] families)
		{
			log = BambiLogFactory.getLog(getClass());
			this.messageType = messageType == null ? "*" : messageType.getName();
			this.families = families;
		}

		/**
		 * Custom constructor. Accepting multiple params for initializing.
		 *
		 * @param messageType The message type
		 * @param families The array of families
		 */
		public AbstractHandler(final String messageType, final EnumFamily[] families)
		{
			log = BambiLogFactory.getLog(getClass());
			this.messageType = messageType;
			this.families = families;
		}

		/**
		 * @see org.cip4.bambi.core.messaging.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public abstract boolean handleMessage(JDFMessage jmfMessage, JDFResponse jmfResponse);

		/**
		 * @see org.cip4.bambi.core.messaging.IMessageHandler#getFamilies()
		 */
		@Override
		final public EnumFamily[] getFamilies()
		{
			return families;
		}

		/**
		 * @see org.cip4.bambi.core.messaging.IMessageHandler#getMessageType()
		 */
		@Override
		final public String getMessageType()
		{
			return messageType;
		}

		/**
		 * @see org.cip4.bambi.core.messaging.IMessageHandler#isSubScribable()
		 */
		@Override
		public boolean isSubScribable()
		{
			return false;
		}

		/**
		 * @see org.cip4.bambi.core.messaging.IMessageHandler#isAcknowledge()
		 */
		@Override
		public boolean isAcknowledge()
		{
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " : " + getMessageType() + ", " + ArrayUtils.toString(getFamilies());
		}
	}

	/**
	 * Custom constructor. Accepting a device for initializing.
	 *
	 * @param device The device.
	 */
	public JMFHandler(final AbstractDevice device)
	{
		super();
		messageMap = new HashMap<>();
		signalDispatcher = null;
		this.device = device;
		addHandler(this.new KnownMessagesHandler());
		messageCount = 0;
		filterOnDeviceID = false;
	}

	/**
	 * Add a message handler
	 *
	 * @param messageHandler The message handler associated with the event
	 */
	@Override
	public void addHandler(final IMessageHandler messageHandler)
	{
		final String messageType = messageHandler.getMessageType();
		final EnumFamily[] messageFamilies = messageHandler.getFamilies();

		if (messageType == null || messageFamilies == null)
		{
			log.error("Unknown message type or family in addhandler - bailing out! type=" + messageType + " families=" + ArrayUtils.toString(messageFamilies));
			return;
		}

		for (final EnumFamily messageFamily : messageFamilies)
		{
			addHandler(messageHandler, messageType, messageFamily);
		}
	}

	/**
	 * Add a message handler.
	 *
	 * @param messageHandler The message handler to add.
	 * @param messageType The message type.
	 * @param messageFamily The message family.
	 */
	public void addHandler(final IMessageHandler messageHandler, final String messageType, final EnumFamily messageFamily)
	{
		final IMessageHandler previousMessageHandler = messageMap.put(new MessageType(messageType, messageFamily), messageHandler);

		if (previousMessageHandler != null)
		{
			log.info(device.getDeviceID() + ": removing old IMessageHandler: " + previousMessageHandler.getClass().getSimpleName());
			log.info(device.getDeviceID() + ": size: " + messageMap.size() + ": replacing with new IMessageHandler: " + messageHandler.getClass().getSimpleName());
		}
		else
		{
			log.info(device.getDeviceID() + ": size: " + messageMap.size() + ": adding new IMessageHandler: " + messageHandler.getClass().getSimpleName());
		}
	}

	/**
	 *
	 * @param messageType
	 * @param messageFamily
	 * @return
	 * @deprecated
	 */
	@Deprecated
	public IMessageHandler getHandler(final String messageType, final EnumFamily messageFamily)
	{
		return getMessageHandler(messageType, messageFamily);
	}

	/**
	 * Return a handler for a given type and family.
	 *
	 * @param messageType The message type, "*" is a wildcard that will be called in case no individual handler exists
	 * @param messageFamily The message family
	 * @return The message handler, null if none exist.
	 */
	@Override
	public IMessageHandler getMessageHandler(final String messageType, final EnumFamily messageFamily)
	{
		IMessageHandler messageHandler = messageMap.get(new MessageType(messageType, messageFamily));

		if (messageHandler == null)
		{
			messageHandler = messageMap.get(new MessageType("*", messageFamily));
		}

		return messageHandler;
	}

	/**
	 * The main processing dispatcher
	 *
	 * @param jmf The JMF which needs to be processed
	 * @return the JDFDoc holding the JMF response
	 */
	@Override
	public JDFDoc processJMF(final JDFDoc jmf)
	{
		final JDFJMF jmfMessage = jmf.getJMFRoot();
		final JDFJMF jmfResponse = jmfMessage.createResponse();

		VElement messages = jmfMessage.getMessageVector(null, null);

		if (log.isDebugEnabled())
		{
			log.debug("handling jmf from " + jmfMessage.getSenderID() + " id=" + jmfMessage.getID() + " with " + messages.size() + " messages; total=" + messageCount);
		}

		for (final KElement m : messages)
		{
			final JDFMessage message = (JDFMessage) m;
			final String id = message.getID();

			final JDFResponse response = (JDFResponse) (id == null ? null : jmfResponse.getChildWithAttribute(ElementName.RESPONSE, AttributeName.REFID, null, id, 0, true));
			if (response == null)
			{
				log.warn("no response provided ??? " + id + " " + message.getFamily() + " " + message.getType());
			}

			final boolean hasSubscription = signalDispatcher != null && signalDispatcher.findSubscription(message, response);
			final EnumVersion v = message.getVersion(true);
			if (EnumUtil.aLessThanB(v, EnumVersion.Version_1_5) || !hasSubscription)
			{
				handleMessage(message, response);
			}

			if ((message instanceof JDFSignal) && response != null && response.getReturnCode() == 0 && !EnumChannelMode.Reliable.equals(((JDFSignal) message).getChannelMode()))
			{
				response.deleteNode();
			}
		}

		messages = jmfResponse.getMessageVector(null, null);
		if (messages != null && messages.size() > 0)
		{
			jmfResponse.setSenderID(getSenderID());
			jmfResponse.setICSVersions((device).getICSVersions());
			jmfResponse.collectICSVersions();
			return jmfResponse.getOwnerDocument_JDFElement();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Standard handler for unimplemented messages.
	 */
	protected void unhandledMessage(final JDFMessage jmfMessage, final JDFResponse jmfResponse)
	{
		errorResponse(jmfResponse, "Message not handled: " + jmfMessage.getType() + "; Family: " + jmfMessage.getFamily().getName() + " id=" + jmfMessage.getID(), 5,
				EnumClass.Warning);
	}

	/**
	 * Standard error message creator.
	 *
	 * @param jmfResponse the response to make an error
	 * @param errorText the explicit error text
	 * @param returnCode the jmf response returncode
	 * @param errorClass the error class of the message
	 */
	public static void errorResponse(final JDFResponse jmfResponse, final String errorText, final int returnCode, final EnumClass errorClass)
	{
		errorResponse(jmfResponse, errorText, returnCode, errorClass, null);
	}

	/**
	 * Standard error message creator.
	 *
	 * @param jmfResponse the response to make an error
	 * @param errorText the explicit error text
	 * @param returnCode the jmf response returncode
	 * @param errorClass the error class of the message
	 */
	public static void errorResponse(final JDFResponse jmfResponse, final String errorText, final int returnCode, final EnumClass errorClass, final Throwable t)
	{
		if (jmfResponse != null)
		{
			jmfResponse.setReturnCode(returnCode);
			final String stackTrace = BambiLogFactory.printStackTrace(t == null ? new BambiException(errorText) : t, 10);
			final JDFNotification notification = jmfResponse.setErrorText(errorText + "\nStack Trace:\n" + stackTrace, errorClass);
			notification.getComment(0).setName("DeviceText");
		}

		if (EnumClass.Error.equals(errorClass))
		{
			log.error("JMF error: returnCode=" + returnCode + " " + errorText, t);
		}
		else
		{
			log.warn("JMF warning: returnCode=" + returnCode + " " + errorText, t);
		}
	}

	/**
	 * We do not call these for ourselves...
	 *
	 * @return the list of families
	 */
	@Override
	public EnumFamily[] getFamilies()
	{
		return null;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#getMessageType()
	 */
	@Override
	public String getMessageType()
	{
		return null;
	}

	/**
	 * The handler implements itself as a generic handler.
	 */
	@Override
	public boolean handleMessage(final JDFMessage jmfMessage, final JDFResponse jmfResponse)
	{
		if (jmfMessage == null)
		{
			log.error("handling null message");
			return false;
		}

		messageCount++;

		try
		{
			final EnumFamily messageFamily = jmfMessage.getFamily();
			final String messageType = jmfMessage.getType();

			if (filterOnDeviceID)
			{
				final String deviceID = jmfMessage.getJMFRoot().getDeviceID();
				if (!KElement.isWildCard(deviceID) && !ContainerUtil.equals(deviceID, getSenderID()))
				{
					return false;
				}
			}
			final IMessageHandler messageHandler = getMessageHandler(messageType, messageFamily);
			boolean messageIsHandled = messageHandler != null;

			if (messageIsHandled)
			{
				if (messageCount < 10 || messageCount % 1000 == 0)
				{
					final String stringBuffer = messageCount + "; family= " + jmfMessage.getLocalName() + " type=" + jmfMessage.getType() + " Sender= " + jmfMessage.getSenderID();
					log.info("handling message #" + stringBuffer);
				}
				messageIsHandled = messageHandler.handleMessage(jmfMessage, jmfResponse);
			}
			else
			{
				unhandledMessage(jmfMessage, jmfResponse);
			}
			final VString icsVersions = device.getICSVersions();

			if (jmfResponse != null)
			{
				if (icsVersions != null && EnumUtil.aLessEqualsThanB(EnumVersion.Version_1_4, jmfMessage.getMaxVersion(true))
						&& EnumUtil.aLessEqualsThanB(EnumVersion.Version_1_4, jmfResponse.getMaxVersion(true)))
				{
					final VString responseICSVersions = jmfResponse.getICSVersions();
					if (!ContainerUtil.isEmpty(responseICSVersions))
					{
						responseICSVersions.appendUnique(icsVersions);
						jmfResponse.setICSVersions(responseICSVersions);
					}
					else
					{
						jmfResponse.setICSVersions(icsVersions);
					}
				}
			}
			return messageIsHandled;
		}
		catch (final Throwable ex)
		{
			log.error("Unhandled Exception in handleMessage", ex);
			return false;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final String msgMap = "MessageMap (size=" + messageMap.size() + ")=[" + messageMap.toString() + "]";
		return "JMFHandler: " + msgMap;
	}

	/**
	 * Set the signal dispatcher.
	 */
	public void setDispatcher(final SignalDispatcher signalDispatcher)
	{
		this.signalDispatcher = signalDispatcher;

	}

	/**
	 * Returns the sender's identifier.
	 *
	 * @return the senderid
	 */
	public String getSenderID()
	{
		return device == null ? null : device.getDeviceID();
	}

	/**
	 * Set the state of device id filter.
	 *
	 * @param filterOnDeviceID The new state of the device id filter
	 */
	public void setFilterOnDeviceID(final boolean filterOnDeviceID)
	{
		this.filterOnDeviceID = filterOnDeviceID;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#isSubScribable()
	 */
	@Override
	public boolean isSubScribable()
	{
		return false;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#isAcknowledge()
	 */
	@Override
	public boolean isAcknowledge()
	{
		return false;
	}
}
