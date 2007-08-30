/*
*
* The CIP4 Software License, Version 1.0
*
*
* Copyright (c) 2001-2007 The International Cooperation for the Integration of 
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

package org.cip4.bambi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.resource.JDFDevice;
import org.cip4.jdflib.resource.JDFDeviceList;

/**
 * a JDF device. <br>
 * class should remain final: if it is ever subclassed, the DeviceProcessor thread would be started 
 * before the constructor from the subclass has a chance to fire off.
 * 
 * @author boegerni
 * 
 
 * 
 */
public final class Device implements IJMFHandler  {
	/**
	 * 
	 * handler for the knowndevices query
	 */
	protected class KnownDevicesHandler implements IMessageHandler
	{
	
		/* (non-Javadoc)
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		public boolean handleMessage(JDFMessage m, JDFResponse resp)
		{
			// "I am the known device"
			if(m==null || resp==null)
			{
				return false;
			}
			log.debug("Handling"+m.getType());
			EnumType typ=m.getEnumType();
			if(EnumType.KnownDevices.equals(typ))
			{
				JDFDeviceList dl = resp.appendDeviceList();
				appendDeviceInfo(dl);
				return true;
			}
	
			return false;
		}
	
	
		/* (non-Javadoc)
		 * @see org.cip4.bambi.IMessageHandler#getFamilies()
		 */
		public EnumFamily[] getFamilies()
		{
			return new EnumFamily[]{EnumFamily.Query};
		}
	
		/* (non-Javadoc)
		 * @see org.cip4.bambi.IMessageHandler#getMessageType()
		 */
		public EnumType getMessageType()
		{
			return EnumType.KnownDevices;
		}
	}

	private static Log log = LogFactory.getLog(Device.class.getName());
	private String _deviceType = "";
	private String _deviceID = "";
	private IQueueProcessor _theQueue=null;
	private IDeviceProcessor _theDeviceProcessor=null;
	private IStatusListener _theStatusListener=null;
	private ISignalDispatcher _theSignalDispatcher=null;
	private JMFHandler _jmfHandler = null ;

	/**
	 * constructor
	 * @param deviceType
	 * @param deviceID
	 */
	public Device(String deviceType, String deviceID, String deviceClass)
	{
		log.info("creating device with type='" + deviceType + "', deviceID='"+deviceID+"'");
		_deviceType = deviceType;
		_deviceID = deviceID;
		_jmfHandler = new JMFHandler();

        _theSignalDispatcher=new SignalDispatcher(_jmfHandler);
        _theSignalDispatcher.addHandlers(_jmfHandler);

		_theQueue=new QueueProcessor(deviceID);
        _theQueue.addHandlers(_jmfHandler);
        _theStatusListener=new StatusListener(_theSignalDispatcher, getDeviceID());
        _theStatusListener.addHandlers(_jmfHandler);
        
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class configClass;
        boolean clFailed = false;
        Exception caughtEx = null;
        try {
        	// warning: ClassNotFoundException might not be caught sometimes
        	configClass = classLoader.loadClass(deviceClass);
        	_theDeviceProcessor= (IDeviceProcessor) configClass.newInstance();
        } catch (ClassNotFoundException e) {
        	clFailed = true;
        	caughtEx = e;
        } catch (InstantiationException e) {
        	clFailed = true;
        	caughtEx = e;
        } catch (IllegalAccessException e) {
        	clFailed = true;
        	caughtEx = e;
        }
        if (clFailed)
        {
        	log.error("failed to create device from class name "+deviceClass+":\r\n"+caughtEx);
        	return;
        }
        
        _theDeviceProcessor.init(_theQueue, _theStatusListener, _deviceID);
        log.debug("created device from class name "+deviceClass);

        log.info("Starting device thread");
		new Thread(_theDeviceProcessor).start();
		log.info("device thread started");
		
		addHandlers();
	}

	private void addHandlers() {
		_jmfHandler.addHandler( this.new KnownDevicesHandler() );
	}

	/* (non-Javadoc)
	 * @see org.cip4.bambi.IDevice#getDeviceName()
	 */
	public String getDeviceType()
	{
		return _deviceType;
	}

	/* (non-Javadoc)
	 * @see org.cip4.bambi.IDevice#getDeviceID()
	 */
	public String getDeviceID()
	{
		return _deviceID;
	}

	/* (non-Javadoc)
	 * @see org.cip4.bambi.IDevice#processJMF(org.cip4.jdflib.core.JDFDoc)
	 */
	public JDFDoc processJMF(JDFDoc doc)
	{
		log.debug("JMF processed by "+_deviceID);
		return _jmfHandler.processJMF(doc);
	}

	/* (non-Javadoc)
	 * @see org.cip4.bambi.IDevice#toString()
	 */
	public String toString()
	{
		return ("[org.cip4.bambi.Device: DeviceID=" + _deviceID + ", DeviceType=" + _deviceType + ", " +
				"Queue: " + _theQueue + "]");
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.IDevice#getDeviceInfo(org.cip4.jdflib.resource.JDFDeviceList)
	 */
	public boolean appendDeviceInfo(JDFDeviceList dl)
	{
		JDFDeviceInfo info = dl.appendDeviceInfo();
		JDFDevice dev = info.appendDevice();
		dev.setDeviceID(_deviceID);
		dev.setDeviceType(_deviceType);
		dev.setJDFVersions( EnumVersion.Version_1_3.getName() );
		return true;
	}

	public void addHandler(IMessageHandler handler) {
		_jmfHandler.addHandler(handler);
		
	}

	public IJMFHandler getHandler() {
		return _jmfHandler;
	}
	
	public QueueFacade getQueueFacade()
	{
		return (new QueueFacade(_theQueue.getQueue()) );
	}
	
    public JDFQueue getQueue()
    {
        return _theQueue.getQueue();
    }
    
    /**
     * get the queprocessor
     * @return
     */
    public IQueueProcessor getQueueProcessor()
    {
        return _theQueue;
    }
	
	
	public boolean suspend()
	{
		return false;
	}
	
	public boolean resume()
	{
		return false;	
	}
}