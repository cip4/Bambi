/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2013 The International Cooperation for the Integration of 
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

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.MessageResponseHandler;
import org.cip4.bambi.proxy.AbstractProxyDevice.KnownSubscriptionsHandler;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.ThreadUtil;

/**
 * class to asynchronously subscribe to messages at the slaves
 * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class SlaveSubscriber extends Thread
{
	protected final AbstractProxyDevice abstractProxyDevice;
	protected final Log log;

	/**
	 * @param b
	 */
	public void setReset(final boolean b)
	{
		reset = b;
	}

	/**
	 * @param waitBefore the time to wait prior to subscribing
	 * @param slaveQEID
	 * @param abstractProxyDevice the device that this subscriber works on
	 */
	public SlaveSubscriber(AbstractProxyDevice abstractProxyDevice, final int waitBefore, final String slaveQEID)
	{
		super("SlaveSubscriber_" + abstractProxyDevice.getDeviceID() + "_" + AbstractProxyDevice.slaveThreadCount++);
		this.abstractProxyDevice = abstractProxyDevice;
		this.waitBefore = waitBefore;
		this.slaveQEID = slaveQEID;
		log = LogFactory.getLog(getClass());
		log.info("creating slave subscriber for slave qeid=" + slaveQEID);
		reset = false;
		abstractProxyDevice.waitingSubscribers.put(abstractProxyDevice.getKey(slaveQEID), this);
	}

	private final int waitBefore;
	private final String slaveQEID;
	private boolean reset;

	/**
	 * add global subscriptions at startup
	 */
	@Override
	public void run()
	{
		final String slaveURL = abstractProxyDevice.getProperties().getSlaveURL();
		log.info("Updating global subscriptions to :" + slaveURL);
		final String deviceURL = abstractProxyDevice.getDeviceURLForSlave();
		if (deviceURL == null)
		{
			log.error("Device feedback url is not specified in subscription, proxy device " + abstractProxyDevice.getDeviceID() + " is not subscribing at slave device: "
					+ abstractProxyDevice.getSlaveDeviceID());
			return;
		}

		ThreadUtil.sleep(waitBefore); // wait for other devices to start prior to subscribing
		if (!abstractProxyDevice.knownSlaveMessages.isInitialized())
		{
			abstractProxyDevice.knownSlaveMessages.updateKnownMessages();
		}
		final Vector<JDFJMF> vJMFS = prepare(deviceURL);

		// reduce currently known subscriptions
		sendSubscriptions(vJMFS);

		cleanup();
	}

	/**
	 * @param deviceURL
	 * @return
	 */
	protected Vector<JDFJMF> prepare(final String deviceURL)
	{
		resetSubscriptions();
		final JMFBuilder builder = abstractProxyDevice.getBuilderForSlave();
		final JDFJMF knownSubscriptions = builder.buildKnownSubscriptionsQuery(deviceURL, slaveQEID);
		final Vector<JDFJMF> createSubscriptions = createSubscriptions(deviceURL);
		if (createSubscriptions != null)
		{
			// remove duplicates
			if (abstractProxyDevice.knownSlaveMessages.knows(EnumType.KnownSubscriptions))
			{
				final KnownSubscriptionsHandler handler = abstractProxyDevice.new KnownSubscriptionsHandler(knownSubscriptions, createSubscriptions);
				abstractProxyDevice.sendJMFToSlave(knownSubscriptions, handler);
				handler.waitHandled(20000, 30000, true);
				return handler.completeHandling();
			}
			else
			{
				return createSubscriptions;
			}
		}
		else
		{
			return null;
		}
	}

	/**
	 * @param vJMFS
	 */
	protected void sendSubscriptions(final Vector<JDFJMF> vJMFS)
	{
		if (vJMFS != null)
		{
			for (JDFJMF jmf : vJMFS)
			{
				createNewSubscription(jmf);
			}
		}
	}

	/**
	 * 
	 */
	protected void cleanup()
	{
		if (!ThreadUtil.sleep(30000))
			return;

		synchronized (abstractProxyDevice.waitingSubscribers)
		{
			abstractProxyDevice.waitingSubscribers.remove(abstractProxyDevice.getKey(slaveQEID));
		}
	}

	/**
	 * 
	 */
	protected void resetSubscriptions()
	{
		if (abstractProxyDevice.knownSlaveMessages.knows(EnumType.StopPersistentChannel))
		{
			if (reset)
			{
				final JMFBuilder builder = abstractProxyDevice.getBuilderForSlave();
				final JDFJMF stopPersistant = builder.buildStopPersistentChannel(null, null, abstractProxyDevice.getDeviceURLForSlave());
				final MessageResponseHandler waitHandler = abstractProxyDevice.new StopPersistantHandler(stopPersistant);
				abstractProxyDevice.sendJMFToSlave(stopPersistant, waitHandler);
				waitHandler.waitHandled(10000, 30000, true);
			}
		}
		else
		{
			log.warn("slave does not handle StopPersistantChannel");
		}
	}

	/**
	 * @param deviceURL
	 * 
	 * @return
	 */
	protected Vector<JDFJMF> createSubscriptions(final String deviceURL)
	{
		final JMFBuilder builder = abstractProxyDevice.getBuilderForSlave();
		final JDFJMF[] createSubscriptions = builder.createSubscriptions(deviceURL, slaveQEID, 10, 0);
		Vector<JDFJMF> vRet = ContainerUtil.toVector(createSubscriptions);
		vRet = removeUnknown(vRet);

		return vRet.size() > 0 ? vRet : null;
	}

	protected Vector<JDFJMF> removeUnknown(Vector<JDFJMF> vRet)
	{
		if (vRet == null)
			return vRet;
		for (int i = vRet.size() - 1; i >= 0; i--)
		{
			JDFJMF jmf = vRet.elementAt(i);
			JDFMessage m = jmf.getMessageElement(null, null, 0);
			if (!abstractProxyDevice.knownSlaveMessages.knows(m.getType()))
				vRet.remove(jmf);
		}
		return vRet;
	}

	/**
	 * @param jmf the subscribeable jmf
	 */
	protected void createNewSubscription(final JDFJMF jmf)
	{
		if (jmf == null)
		{
			return;
		}
		final ProxySubscription proxySub = new ProxySubscription(jmf);
		final EnumType messageType = jmf.getQuery(0).getEnumType();
		final ProxySubscription proxySubOld = abstractProxyDevice.mySubscriptions.get(messageType);
		if (proxySubOld != null)
		{
			log.warn("updating dropped subscription; type: " + messageType.getName());
		}
		final int rc = sendSubscriptionToSlave(jmf);
		if (rc == 0)
		{
			abstractProxyDevice.mySubscriptions.remove(messageType);
			abstractProxyDevice.mySubscriptions.put(messageType, proxySub);
		}
		else
		{
			log.warn("error updating subscription; type: " + messageType.getName() + " rc=" + rc);
		}
	}

	protected int sendSubscriptionToSlave(final JDFJMF jmf)
	{
		final MessageResponseHandler respHandler = new MessageResponseHandler(jmf);
		abstractProxyDevice.sendJMFToSlave(jmf, respHandler);
		respHandler.waitHandled(10000, 30000, false);
		final int rc = respHandler.getJMFReturnCode();
		return rc;
	}

	/**
	 * 
	 * @see java.lang.Thread#toString()
	 */
	@Override
	public String toString()
	{
		return "SlaveSubscriber: qeid=" + slaveQEID + " " + abstractProxyDevice.knownSlaveMessages;
	}
}