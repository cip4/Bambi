package org.cip4.bambi;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.servlets.DeviceServlet;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.resource.JDFDevice;
import org.cip4.jdflib.resource.JDFDeviceList;

/**
 * basis for JDF devices in Bambi. <br>
 * Bambis devices are defined in /WebContent/config/devices.xml<br>
 * Derived classes should be final: if they were ever subclassed, the DeviceProcessor thread 
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
	protected String _deviceURL=null;
	
	/**
	 * creates a new Bambi device instance from a given class
	 * @param deviceType the device type as defined in the JDF spec, e.g. "Generic Bambi Stitcher"
	 * @param deviceID the individual device ID, as defined in the JDF spec
	 * @param deviceClass the name of the Java class of the instance, e.g. "org.cip4.bambi.SimDevice"
	 */
	public AbstractDevice(String deviceType, String deviceID, String deviceClass)
	{
		log.info("creating device with type='" + deviceType + "', deviceID='"+deviceID+"'");
		_deviceType = deviceType;
		_deviceID = deviceID;
		_jmfHandler = new JMFHandler();

        _theSignalDispatcher=new SignalDispatcher(_jmfHandler, deviceID);
        _theSignalDispatcher.addHandlers(_jmfHandler);

		_theQueue=new SubdeviceQueueProcessor(deviceID, this);
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
        log.info("created device from class name "+deviceClass);

		new Thread(_theDeviceProcessor,"DeviceProcessor_"+_deviceID).start();
		log.info("device thread started: DeviceProcessor_"+_deviceID);
		
		_deviceURL = createDeviceURL(_deviceID);
		
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
    
    /**
     * stop processing the given QueueEntry
     * @param queueEntryID the ID of the QueueEntry to stop
     * @param status target status of the QueueEntry (Suspended,Aborted,Held)
     * @return the updated QueueEntry
     */
    public JDFQueueEntry stopProcessing(String queueEntryID,EnumQueueEntryStatus status)
    {
    	JDFQueue q=_theQueue.getQueue();
    	if (q==null) {
    		log.fatal("queue of device "+_deviceID+"is null");
    		return null;
    	}
    	JDFQueueEntry qe=q.getQueueEntry(queueEntryID);
    	if (qe==null) {
    		log.fatal("QueueEntry with ID="+queueEntryID+" is null on device "+_deviceID);
    		return null;
    	}
    	
    	_theDeviceProcessor.stopProcessing(qe, status);
    	return qe;
    }

    /**
	 * build the URL of this device
	 * @param deviceID the ID of the device to get the URL for. Use "" for the Bambi Root Device.
	 * @return
	 */
	public static String createDeviceURL(String deviceID) {
		Properties properties = new Properties();
		FileInputStream in=null;
		String deviceURL=null;
		try {
			in = new FileInputStream(DeviceServlet.configDir+"Bambi.properties");
			properties.load(in);
			JDFJMF.setTheSenderID(properties.getProperty("SenderID"));
			deviceURL= properties.getProperty("BambiURL")+"/"+properties.getProperty("RootDeviceID");
			if (deviceID!=null && deviceID.length()>0)
				deviceURL += "/"+deviceID;
			in.close();
		} catch (IOException e) {
			log.error("failed to load Bambi properties: \r\n"+e.getMessage());
			return null;
		}
		return deviceURL;
	}
	
	/**
	 * return the URL of this device
	 * @return
	 */
	public String getDeviceURL() {
		return _deviceURL;
	}
	
}