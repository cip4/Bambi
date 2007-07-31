package org.cip4.bambi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.resource.JDFDevice;
import org.cip4.jdflib.resource.JDFDeviceList;

/**
 * a JDF device
 * 
 * @author boegerni
 * 
 */
public class Device {
	// TODO create interface IDevice
	private static Log log = LogFactory.getLog(DeviceServlet.class.getName());
	private String _deviceName = "";
	private String _deviceID = "";
	private IQueueProcessor _theQueue=null;
	private IDeviceProcessor _theDevice=null;
	private IStatusListener _theStatusListener=null;
	private ISignalDispatcher _theSignalDispatcher=null;
	private JMFHandler _jmfHandler = null ;

	/**
	 * constructor
	 * @param deviceName
	 * @param deviceID
	 */
	public Device(String deviceName, String deviceID, JMFHandler jmfHandler)
	{
		log.info("creating device with name='" + deviceName + "', deviceID='"+deviceID+"'");
		_deviceName = deviceName;
		_deviceID = deviceID;
		_jmfHandler = jmfHandler;

		SignalDispatcher tmpDisp=new SignalDispatcher(_jmfHandler);
		_theSignalDispatcher=tmpDisp;
		tmpDisp.addHandlers(_jmfHandler);

		_theQueue=new QueueProcessor(_theStatusListener, _theSignalDispatcher,deviceID);
		//TODO        theQueue.addHandlers(jmfHandler);
		StatusListener statusListener=new StatusListener(_theSignalDispatcher);
		_theStatusListener=statusListener;
		statusListener.addHandlers(_jmfHandler);

		_theDevice=new DeviceProcessor(_theQueue, _theStatusListener);
		log.info("Starting device thread");
		new Thread(_theDevice).start();
		log.info("device thread started");
	}

	public String getDeviceName()
	{
		return _deviceName;
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
		return ("[org.cip4.bambi.Device: DeviceID=" + _deviceID + ", DeviceName=" + _deviceName + "]");
	}
	
	public boolean getDeviceInfo(JDFDeviceList dl)
	{
		JDFDeviceInfo info = dl.appendDeviceInfo();
		JDFDevice dev = info.appendDevice();
		dev.setDeviceID(_deviceID);
		dev.setDeviceType(_deviceName);
		return true;
	}
}