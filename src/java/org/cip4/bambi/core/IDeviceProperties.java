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
     * get the Name, ProcessUsage or Usage of the major resource to track
     * @return the deviceID
     */
    public String getTrackResource();

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
	 * set the base dir of the web application
	 * @param baseDir the base dir of the web application
	 */
	public void setBaseDir(String baseDir);

	/**
	 * get the base dir of the web application
	 * @return the base dir of the web application
	 */
	public String getBaseDir();
	
	/**
	 * set the directory containing the JDF documents
	 * @param jdfDir the directory containing the JDF documents
	 */
	public void setJDFDir(String jdfDir);

	/**
	 * get the directory containing the JDF documents
	 * @return the directory containing the JDF documents
	 */
	public String getJDFDir();
	
	/**
	 * set the directory containing the config files of the application
	 * @param jdfDir the directory containing the config files of the application
	 */
	public void setConfigDir(String configDir);

	/**
	 * get the directory containing the config files of the application
	 * @return the directory containing the config files of the application
	 */
	public String getConfigDir();
	
    /**
     * returns the name of the IConverterCallback that specifies the converter name
     * @return {@link IConverterCallback} the callback to use, null if none is specified
     */
	public IConverterCallback getCallBackClass();

	/**
	 * get a String representation of this DeviceProperty
	 */
	public String toString();

}