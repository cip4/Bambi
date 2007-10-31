package org.cip4.bambi.core;

public interface IDeviceProperties {

	/**
	 * @param deviceURL the deviceURL to set
	 */
	public void setDeviceURL(String deviceURL);

	/**
	 * @return the deviceURL
	 */
	public String getDeviceURL();

	/**
	 * @return the deviceID
	 */
	public String getDeviceID();

	/**
	 * @return the controllerURL
	 */
	public String getProxyURL();

	/**
	 * @return the deviceType
	 */
	public String getDeviceType();

	/**
	 * @param appDir the location of the web application on the hard disk
	 */
	public void setAppDir(String appDir);

	/**
	 * @return the appURL
	 */
	public String getAppDir();

	/**
	 * get the String representation of this DeviceProperty
	 */
	public String toString();

}