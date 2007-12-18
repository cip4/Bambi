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

package org.cip4.bambi.core;

import java.io.File;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.queues.IQueueProcessor;
import org.cip4.bambi.core.queues.QueueFacade;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.resource.JDFDevice;
import org.cip4.jdflib.resource.JDFDeviceList;
import org.cip4.jdflib.util.QueueHotFolder;
import org.cip4.jdflib.util.QueueHotFolderListener;
import org.cip4.jdflib.util.UrlUtil;

/**
 * basis for JDF devices. <br>
 * Devices are defined in /WebContent/config/devices.xml<br>
 * Derived classes should be final: if they were ever subclassed, the DeviceProcessor thread 
 * would be started before the constructor from the subclass has a chance to fire.
 * 
 * @author boegerni
 * 
 */
public abstract class AbstractDevice implements IDevice, IJMFHandler
{
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

    protected class HFListner implements QueueHotFolderListener
    {
        public void submitted(JDFJMF submissionJMF)
        {
            log.info("HFListner:submitted");
            JDFCommand command=submissionJMF.getCommand(0);
            JDFQueueSubmissionParams qsp=command.getQueueSubmissionParams(0);

            JDFDoc doc=qsp.getURLDoc();
            if(doc==null)
            {
                log.warn("could not process JDF File");
            }
            else
            {
                JDFResponse r=_theQueueProcessor.addEntry(command, doc, qsp.getHold());
                if (r == null)
                    log.warn("_theQueue.addEntry returned null");
                final String tmpURL=qsp.getURL();
                final File tmpFile=UrlUtil.urlToFile(tmpURL);
                if(tmpFile!=null) {
                	if (!tmpFile.delete())
                		log.warn( "failed to delete temporary file "+tmpFile.getAbsolutePath() );
                }    
            }        
        }
    }
    protected static final Log log = LogFactory.getLog(AbstractDevice.class.getName());
    protected IQueueProcessor _theQueueProcessor=null;
    protected Vector<AbstractDeviceProcessor> _deviceProcessors=null;
    protected ISignalDispatcher _theSignalDispatcher=null;
    protected JMFHandler _jmfHandler = null ;
    protected IDeviceProperties _devProperties=null;
    protected QueueHotFolder _submitHotFolder=null;

    /**
     * creates a new device instance
     * @param prop the properties for the device
     */
    public AbstractDevice(IDeviceProperties prop) {
        super();
        init(prop);
    }

    protected void init(IDeviceProperties prop) {
        _devProperties = prop;
        _jmfHandler = new JMFHandler();

        _theSignalDispatcher=new SignalDispatcher(_jmfHandler, _devProperties.getDeviceID());
        _theSignalDispatcher.addHandlers(_jmfHandler);

        _theQueueProcessor = buildQueueProcessor( );
        _theQueueProcessor.addHandlers(_jmfHandler);
        

        String deviceID=_devProperties.getDeviceID();
        _deviceProcessors=new Vector<AbstractDeviceProcessor>();
        AbstractDeviceProcessor newDevProc= buildDeviceProcessor();
        if (newDevProc!=null) {
        	IStatusListener theStatusListener=new StatusListener(_theSignalDispatcher, getDeviceID());
            theStatusListener.addHandlers(_jmfHandler);
            newDevProc.init(_theQueueProcessor, theStatusListener, _devProperties);
            String deviceProcessorClass=newDevProc.getClass().getSimpleName();
            new Thread(newDevProc,deviceProcessorClass+"_"+deviceID).start();
            log.info("device processor thread started: "+deviceProcessorClass+"_"+deviceID);
            _deviceProcessors.add( newDevProc );
        }

        final String hfURL=prop.getHotFolderURL();
        createHotFolder(hfURL);

        addHandlers();
    }

    /**
	 * creates the hotfolder on the file system
     * @param hfURL the URL of the hotfolder to create. If hfURL is null, no hotfolder will be created.
     */
    protected void createHotFolder(String hfURL)
    {
        if(hfURL==null)
            return;
        log.info("enabling input hot folder: "+hfURL);
        File hfStorage=new File(_devProperties.getAppDir()+File.separator+"HFTmpStorage"+File.separator+_devProperties.getDeviceID());
        hfStorage.mkdirs(); // just in case
        if(hfStorage.isDirectory())
        {
            _submitHotFolder=new QueueHotFolder(UrlUtil.urlToFile(hfURL),hfStorage,null,new HFListner(),null);
        }
        else
        {
            log.error("input hot folder could not be created "+hfURL);
        }

    }

    protected void addHandlers() {
        _jmfHandler.addHandler( this.new KnownDevicesHandler() );
    }

    /**
     * get the device type of this device
     * @return
     */
    public String getDeviceType()
    {
        return _devProperties.getDeviceType();
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.IDevice#getDeviceID()
     */
    public String getDeviceID() {
        return _devProperties.getDeviceID();
    }

    public JDFDoc processJMF(JDFDoc doc) {
        log.info("JMF processed by "+_devProperties.getDeviceID());
        return _jmfHandler.processJMF(doc);
    }

    /**
     * get a String representation of this device
     */
    @Override
    public String toString() {
        return ("["+this.getClass().getName()+" Properties="+ _devProperties.toString() +"]");
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

    /**
     * add a MessageHandler to this devices JMFHandler
     * @param handler the MessageHandler to add
     */
    public void addHandler(IMessageHandler handler) {
        _jmfHandler.addHandler(handler);
    }

    /**
     * get the JMFHandler of this device
     * @return
     */
    public IJMFHandler getHandler() {
        return _jmfHandler;
    }

    /**
     * get a facade of this devices Queue
     * @return
     */
    public QueueFacade getQueueFacade()
    {
        return (new QueueFacade(_theQueueProcessor.getQueue()) );
    }

    /**
     * get the JDFQueue
     * @return JDFQueue
     */
    public JDFQueue getQueue()
    {
        return _theQueueProcessor.getQueue();
    }

    /**
     * get the class name of the i'th device processor
     * @param i the index of the device processor to get the name for
     * @return
     */
    public String getDeviceProcessorClass(int i)
    {
        return _deviceProcessors.get(i) != null ? _deviceProcessors.get(i).getClass().getName() : "";
    }

    /**
     * get the queprocessor
     * @return
     */
    public IQueueProcessor getQueueProcessor()
    {
        return _theQueueProcessor;
    }

    /**
     * get the DeviceStatus of this device
     * @return the DeviceStatus. Returns EnumDeviceStatus.Idle, if the StatusListener is null
     */
    public EnumDeviceStatus getDeviceStatus() {
    	IStatusListener listener=getStatusListener(0);
    	if (listener==null) {
    		return EnumDeviceStatus.Idle;
    	}
    	
        EnumDeviceStatus status = listener.getDeviceStatus();
        if (status == null) {
            log.error("StatusListener returned a null device status");
            status = EnumDeviceStatus.Unknown;
        }
        return status;
    }

    /**
     * stop the processing the given QueueEntry
     * @param queueEntryID the ID of the QueueEntry to stop
     * @param status target status of the QueueEntry (Suspended,Aborted,Held)
     * @return the updated QueueEntry
     */
    public JDFQueueEntry stopProcessing(String queueEntryID, EnumQueueEntryStatus status)
    {
    	AbstractDeviceProcessor theDeviceProcessor=_deviceProcessors.get(0);
    	if (theDeviceProcessor==null) {
    		log.error( "DeviceProcessor for device '"+_devProperties.getDeviceID()+"' is null" );
    		return null;
    	}
        JDFQueue q=_theQueueProcessor.getQueue();
        if (q==null) {
            log.fatal("queue of device "+_devProperties.getDeviceID()+"is null");
            return null;
        }
        JDFQueueEntry qe=q.getQueueEntry(queueEntryID);
        if (qe==null) {
            log.fatal("QueueEntry with ID="+queueEntryID+" is null on device "+_devProperties.getDeviceID());
            return null;
        }

        theDeviceProcessor.stopProcessing(qe, status);
        return qe;
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.IDevice#getDeviceURL()
     */
    public String getDeviceURL() {
        return _devProperties.getDeviceURL();
    }

    public void setDeviceURL(String theURL) {
        _devProperties.setDeviceURL(theURL);
    }

    /**
     * get the URL of the proxy device for this device
     * @return
     */
    public String getProxyURL() {
        return _devProperties.getProxyURL();
    }

    /**
	 * stop the signal dispatcher, hot folder and device processor, if they are not null
     */
    public void shutdown() {
		if (_theSignalDispatcher!=null) {
			_theSignalDispatcher.shutdown();
		}
		
		if (_deviceProcessors!=null) {
			for (int i=_deviceProcessors.size()-1;i>=0;i--) {
				_deviceProcessors.get(i).shutdown();
			}
		}
		
        if (_submitHotFolder!=null) {
            _submitHotFolder.stop();
        }
	}

    /**
     * get the directory of the web application this device belongs to
     * @return the path of the app dir on the filesystem
     */
    public String getAppDir() {
        return _devProperties.getAppDir();
    }
    /**
     * build a new QueueProcessor
     * @return
     */
    protected abstract IQueueProcessor buildQueueProcessor();

    /**
     * build a new DeviceProcessor
     * @return
     */
    protected abstract AbstractDeviceProcessor buildDeviceProcessor();
    
    public String getBaseDir() {
    	return _devProperties.getBaseDir();
    }
    
    public String getConfigDir() {
    	return _devProperties.getConfigDir();
    }
    
    public String getJDFDir() {
    	return _devProperties.getJDFDir();
    }

    /**
     * get the StatusListener of the i'th DeviceProcessor
     * @param i the index of the DeviceProcessor to the the StatusListener of
     * @return the StatusListener
     */
    public IStatusListener getStatusListener(int i) {
        return _deviceProcessors.get(i).getStatusListener();
    }
}
