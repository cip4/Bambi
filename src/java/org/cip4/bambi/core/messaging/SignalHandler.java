/**
 * 
 */
package org.cip4.bambi.core.messaging;

import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.bambi.proxy.AbstractProxyDevice;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.util.StringUtil;

/**
  * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class SignalHandler extends AbstractHandler
{
	/**
	 * 
	 */
	private final AbstractDevice abstractDevice;

	/**
	 * @param _type
	 * @param abstractDevice TODO
	 * @param families 
	 */
	public SignalHandler(AbstractDevice abstractDevice, EnumType _type, EnumFamily[] families)
	{
		super(_type, families);
		this.abstractDevice = abstractDevice;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
	 * @param inputMessage
	 * @param response
	 * @return
	*/
	@Override
	public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
	{
		if (inputMessage == null || !(inputMessage instanceof JDFSignal))
		{
			return false;
		}
		String refID = inputMessage.getrefID();
		if (StringUtil.getNonEmpty(refID) == null)
		{
			refID = inputMessage.getType();
		}
		if (!KElement.isWildCard(refID))
		{
			AbstractProxyDevice abstractProxyDevice = getAbstractProxyDevice();
			if (abstractProxyDevice != null)
				abstractProxyDevice.getMySubscriptions().incrementHandled(refID);
		}
		return true;
	}

	/**
	 * @return
	 */
	private AbstractProxyDevice getAbstractProxyDevice()
	{
		return (abstractDevice instanceof AbstractProxyDevice) ? (AbstractProxyDevice) abstractDevice : null;
	}
}