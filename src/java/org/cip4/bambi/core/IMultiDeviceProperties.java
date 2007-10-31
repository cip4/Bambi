package org.cip4.bambi.core;

import java.util.Set;


public interface IMultiDeviceProperties {

	/**
	 * get the number of the devices
	 * @return the number of devices, zero if no devices have been found
	 */
	public int count();

	/**
	 * get the a Set with the device IDs of all device properties stored
	 * @return a Set of device IDs, an empty set of nothing has been found
	 */
	public Set<String> getDeviceIDs();

	/**
	 * get the device properties for a single device
	 * @param deviceID the device ID of the device to look for
	 * @return the device properties, null if not found
	 */
	public IDeviceProperties getDevice(String deviceID);

	/**
	 * get the String representation of this MultiDeviceProperties
	 */
	public String toString();

}