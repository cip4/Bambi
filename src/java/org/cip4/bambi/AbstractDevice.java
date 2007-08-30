package org.cip4.bambi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
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
 * basis for JDF devices in Bambi. <br>
 * derived classes should be final: if they were ever subclassed, the DeviceProcessor thread 
 * would be started before the constructor from the subclass has a chance to fire.
 * 
 * @author boegerni
 * 
 */
public class AbstractDevice implements IJMFHandler{
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
	public AbstractDevice() {
		super();
	}
	
	private static Log log = LogFactory.getLog(AbstractDevice.class.getName());
	protected String _deviceType = "";
	protected String _deviceID = "";
	protected IQueueProcessor _theQueue=null;
	protected AbstractDeviceProcessor _theDeviceProcessor=null;
	protected IStatusListener _theStatusListener=null;
	protected ISignalDispatcher _theSignalDispatcher=null;
	protected JMFHandler _jmfHandler = null ;
	
	/**
	 * constructor
	 * @param deviceType
	 * @param deviceID
	 */
	public AbstractDevice(String deviceType, String deviceID, String deviceClass)
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
        	configClass = classLoader.loadClass(deviceClass+"Processor");
        	_theDeviceProcessor= (AbstractDeviceProcessor) configClass.newInstance();
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

	public String getDeviceType()
	{
		return _deviceType;
	}

	public String getDeviceID()
	{
		return _deviceID;
	}

	public JDFDoc processJMF(JDFDoc doc)
	{
		log.debug("JMF processed by "+_deviceID);
		return _jmfHandler.processJMF(doc);
	}

	public String toString()
	{
		return ("["+this.getClass().getName()+" DeviceID=" + _deviceID + ", DeviceType=" + _deviceType + ", " +
				"Queue: " + _theQueue + "]");
	}
	
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
	
	/**
	 * get the JDFQueue
	 * @return JDFQueue
	 */
	public JDFQueue getQueue()
	{
		return _theQueue.getQueue();
	}
	
	/**
	 * get the class name of the device processor
	 * @return
	 */
	public String getDeviceProcessorClass()
	{
		if (_theDeviceProcessor != null)
			return _theDeviceProcessor.getClass().getName();
		else
			return "";
	}
	
	/**
     * get the queprocessor
     * @return
     */
    public IQueueProcessor getQueueProcessor()
    {
        return _theQueue;
    }
    
    public EnumDeviceStatus getDeviceStatus()
    {
    	EnumDeviceStatus status = _theStatusListener.getDeviceStatus();
    	if (status == null) {
    		log.error("StatusListener returned a null device status");
    		status = EnumDeviceStatus.Unknown;
    	}
    	return status;
    }

}