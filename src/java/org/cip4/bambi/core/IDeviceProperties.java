package org.cip4.bambi.core;

public interface IDeviceProperties {

	/**
	 * set the URL to communicate with this device (in other words: send JMFs to this URL)
	 * @param deviceURL the deviceURL to set
	 */
	public void setDeviceURL(String deviceURL);

    /**
     * get the URL to communicate with this device
     * @return the device URL. Send JMFs to this URL, if you want to communicate with this device. 
     */
    public String getDeviceURL();
    
    /**
     * get the URL of the device hotfolder, if null the device does not support a JDF input hot folder
     * @return the device hotfolder URL. Drop JDFs to this URL, if you want to submit to the device without JMF. 
     */
    public String getHotFolderURL();

	/**
	 * get the DeviceID of this device
	 * @return the deviceID
	 */
	public String getDeviceID();

	/**
	 * get the URL of the proxy this device is using.
	 * @return the proxy URL
	 */
	public String getProxyURL();

	/**
	 * get the DeviceType of this device
	 * @return the DeviceType of this device
	 */
	public String getDeviceType();

	/**
	 * set the location of the web application on the hard disk
	 * @param appDir the location of the web application on the hard disk
	 */
	public void setAppDir(String appDir);

	/**
	 * get the location of the web application on the hard disk
	 * @return the location of the web application on the hard disk
	 */
	public String getAppDir();

	/**
	 * get a String representation of this DeviceProperty
	 */
	public String toString();

}