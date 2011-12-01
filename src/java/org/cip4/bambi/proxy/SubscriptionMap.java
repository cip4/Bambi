package org.cip4.bambi.proxy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;

/**
  * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class SubscriptionMap extends HashMap<EnumType, ProxySubscription>
{

	/**
	 * 
	 */
	protected SubscriptionMap()
	{
		super();
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
			ps.incrementHandled();

	}

	/**
	 * @param refID the refID or type of the message
	 * @return
	 */
	private ProxySubscription getSubscription(String refID)
	{
		if (refID == null)
			return null;

		Collection<ProxySubscription> v = values();
		Iterator<ProxySubscription> it = v.iterator();
		while (it.hasNext())
		{
			ProxySubscription ps = it.next();
			if (refID.equals(ps.channelID) || refID.equals(ps.type))
				return ps;
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
}