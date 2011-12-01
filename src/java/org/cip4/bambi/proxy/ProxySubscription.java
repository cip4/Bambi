package org.cip4.bambi.proxy;

import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.util.StringUtil;

/**
 * class to manage subscriptions to the slave device
  * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
class ProxySubscription
{
	/**
	 * 
	 */
	private final AbstractProxyDevice abstractProxyDevice;
	long lastReceived;
	long created;
	int numReceived;
	String channelID;
	String url;
	JDFJMF subscribedJMF;
	String type;

	/**
	 * 
	 * @param jmf
	 * @param abstractProxyDevice TODO
	 * @throws IllegalArgumentException
	 */
	public ProxySubscription(AbstractProxyDevice abstractProxyDevice, JDFJMF jmf) throws IllegalArgumentException
	{
		this.abstractProxyDevice = abstractProxyDevice;
		subscribedJMF = (JDFJMF) jmf.clone();
		type = subscribedJMF.getMessageElement(null, null, 0).getType();
		channelID = StringUtil.getNonEmpty(jmf.getQuery(0).getID());
		if (channelID == null)
		{
			this.abstractProxyDevice.getLog().error("Subscription with no channelID");
			throw new IllegalArgumentException("Subscription with no channelID");
		}
		lastReceived = 0;
		numReceived = StringUtil.parseInt(BambiNSExtension.getMyNSAttribute(jmf, "numReceived"), 0);
		created = StringUtil.parseLong(BambiNSExtension.getMyNSAttribute(jmf, AttributeName.CREATIONDATE), System.currentTimeMillis());
		url = BambiNSExtension.getMyNSAttribute(jmf, AttributeName.URL);
	}

	/**
	 * @param channelID
	 */
	public void setChannelID(String channelID)
	{
		if (this.channelID.equals(channelID))
			return; //nop

		this.abstractProxyDevice.getLog().info("updating proxy subscription channelID to: " + channelID);
		this.channelID = channelID;
		subscribedJMF.getMessageElement(null, null, 0).setID(channelID);
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return
	*/
	@Override
	public String toString()
	{
		return "ProxySubscription: " + subscribedJMF;
	}

	/**
	 * 
	 */
	public void incrementHandled()
	{
		lastReceived = System.currentTimeMillis();
		numReceived++;
	}

	/**
	 * @param subs
	 */
	public void copyToXML(KElement subs)
	{
		subs = subs.appendElement("ProxySubscription");
		subs.copyElement(subscribedJMF, null);
		subs.setAttribute(AttributeName.CHANNELID, channelID);
		subs.setAttribute(AttributeName.URL, url);
		subs.setAttribute(AttributeName.TYPE, subscribedJMF.getMessageElement(null, null, 0).getType());
		subs.setAttribute(AttributeName.CREATIONDATE, XMLResponse.formatLong(created));
		subs.setAttribute("LastReceived", XMLResponse.formatLong(lastReceived));
		subs.setAttribute("NumReceived", StringUtil.formatInteger(numReceived));
	}
}