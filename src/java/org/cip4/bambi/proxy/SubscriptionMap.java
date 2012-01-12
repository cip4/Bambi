package org.cip4.bambi.proxy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.MessageResponseHandler;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JMFBuilder;

/**
  * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class SubscriptionMap extends HashMap<EnumType, ProxySubscription>
{
	private final Log log;
	private boolean wantShutDown;

	/**
	 * 
	 */
	protected SubscriptionMap()
	{
		super();
		log = LogFactory.getLog(getClass());
		wantShutDown = true;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param refID
	 */
	public void incrementHandled(String refID)
	{
		ProxySubscription ps = getSubscription(refID);
		if (ps != null)
		{
			ps.incrementHandled();
		}
	}

	/**
	 * 
	 * send StopPersistantChannel messages to url
	 * @param dev 
	 *  
	 */
	public void shutdown(AbstractProxyDevice dev)
	{
		if (!wantShutDown)
		{
			log.info("skipping shutdown because wantshutdown=false");
			return;
		}
		log.info("retrieving stoppersistantchannel messages; n=" + size());
		long t0 = System.currentTimeMillis();
		Collection<ProxySubscription> v = values();
		for (ProxySubscription ps : v)
		{
			JDFJMF stopper = ps.getStopper();
			dev.sendJMFToSlave(stopper, null);
		}
		// and - just in case - a global cleanup
		final JMFBuilder builder = dev.getBuilderForSlave();
		final JDFJMF stopPersistant = builder.buildStopPersistentChannel(null, null, dev.getDeviceURLForSlave());
		final MessageResponseHandler waitHandler = dev.new StopPersistantHandler(stopPersistant);
		dev.sendJMFToSlave(stopPersistant, waitHandler);
		waitHandler.waitHandled(10000, 30000, true);

		log.info("sent all messages: t=" + ((System.currentTimeMillis() - t0) * 0.001));
	}

	/**
	 * @param refID the refID or type of the message
	 * @return
	 */
	private ProxySubscription getSubscription(String refID)
	{
		if (refID == null)
		{
			return null;
		}
		Collection<ProxySubscription> v = values();
		for (ProxySubscription ps : v)
		{
			if (refID.equals(ps.channelID) || refID.equals(ps.type))
			{
				return ps;
			}
		}
		return null;
	}

	/**
	 * @param deviceRoot
	 */
	protected void copyToXML(KElement deviceRoot)
	{
		Collection<ProxySubscription> v = values();
		Iterator<ProxySubscription> it = v.iterator();
		KElement subs = deviceRoot.appendElement("ProxySubscriptions");
		while (it.hasNext())
			it.next().copyToXML(subs);
	}

	public void setWantShutDown(boolean wantShutDown)
	{
		this.wantShutDown = wantShutDown;
	}
}