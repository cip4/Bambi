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

package org.cip4.bambi.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.IDevice;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.ISignalDispatcher;
import org.cip4.bambi.core.IStatusListener;
import org.cip4.bambi.core.SignalDispatcher;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.queues.IQueueProcessor;
import org.cip4.bambi.core.queues.QueueFacade;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.resource.JDFDevice;
import org.cip4.jdflib.resource.JDFDeviceList;

public class ProxyDevice implements IDevice, IJMFHandler {
	
	/**
	 * 
	 * handler for the KnownDevices query
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
			log.debug("Handling "+m.getType());
			EnumType typ=m.getEnumType();
			if(EnumType.KnownDevices.equals(typ))
			{
				JDFDeviceList dl = resp.appendDeviceList();
				JDFDeviceInfo info = dl.appendDeviceInfo();
				JDFDevice dev = info.appendDevice();
				dev.setDeviceID(getDeviceID());
				dev.setDeviceType( getDeviceType() );
				dev.setJDFVersions( EnumVersion.Version_1_3.getName() );
				info.setDeviceStatus( getDeviceStatus() );
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
	
	private static final Log log = LogFactory.getLog(ProxyDevice.class.getName());
	private ISignalDispatcher _theSignalDispatcher=null;
	private IQueueProcessor _theQueueProcessor=null;
	private IStatusListener _theStatusListener=null;
	private IDeviceProperties _devProperties=null;
	private JMFHandler _jmfHandler=null;
	
	public ProxyDevice(IDeviceProperties properties) {
		log.info( "initializing "+properties.getDeviceID() );
		_devProperties=properties;
		
		_jmfHandler = new JMFHandler();
        _theSignalDispatcher=new SignalDispatcher(_jmfHandler, _devProperties.getDeviceID());
        _theSignalDispatcher.addHandlers(_jmfHandler);

        _theQueueProcessor = new ProxyQueueProcessor(_devProperties.getDeviceID(), _devProperties.getAppDir());
        _theQueueProcessor.addHandlers(_jmfHandler);
        _theStatusListener=new StatusListener(_theSignalDispatcher, getDeviceID());
        _theStatusListener.addHandlers(_jmfHandler);
        
        addHandlers();
	}


	public String getDeviceID() {
		return _devProperties.getDeviceID();
	}

	public String getDeviceType() {
		return _devProperties.getDeviceType();
	}

	public String getDeviceURL() {
		return _devProperties.getDeviceURL();
	}
	
	/**
     * get the DeviceStatus of this device
     * @return
     */
    public EnumDeviceStatus getDeviceStatus()
    {
    	EnumDeviceStatus status = _theStatusListener.getDeviceStatus();
    	if (status == null) {
    		log.error("StatusListener returned a null device status");
    		status = EnumDeviceStatus.Unknown;
    	}
    	return status;
    }
    
    /**
	 * get a facade of this devices Queue
	 * @return
	 */
	public QueueFacade getQueueFacade() {
		return (new QueueFacade(_theQueueProcessor.getQueue()) );
	}
	
	/**
	 * append the JDFDeviceInfo of this device to a given JDFDeviceList
	 * @param dl the JDFDeviceList, where the JDFDeviceInfo will be appended
	 * @return true, if successful
	 */
	public boolean appendDeviceInfo(JDFDeviceList dl) {
		JDFDeviceInfo info = dl.appendDeviceInfo();
		JDFDevice dev = info.appendDevice();
		dev.setDeviceID(getDeviceID());
		dev.setDeviceType( getDeviceType() );
		dev.setJDFVersions( EnumVersion.Version_1_3.getName() );
		info.setDeviceStatus( getDeviceStatus() );
		return true;
	}
	
	public JMFHandler getHandler() {
		return _jmfHandler;
	}
	
	/**
	 * stop the signal dispatcher
	 */
	public void shutdown() {
		_theSignalDispatcher.shutdown();
	}
	
    private void addHandlers() {
		_jmfHandler.addHandler( this.new KnownDevicesHandler() );
	}


	/**
	 * add a MessageHandler to this devices JMFHandler
	 * @param handler the MessageHandler to add
	 */
	public void addHandler(IMessageHandler handler) {
		_jmfHandler.addHandler(handler);
	}

	public JDFDoc processJMF(JDFDoc doc) {
		log.info("JMF processed by "+_devProperties.getDeviceID());
		return _jmfHandler.processJMF(doc);
	}
	
}