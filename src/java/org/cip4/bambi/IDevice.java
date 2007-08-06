package org.cip4.bambi;

import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.resource.JDFDeviceList;

/**
 * interface for Bambi devices
 * @author boegerni
 *
 */
public interface IDevice extends IJMFHandler {

	public abstract String getDeviceType();

	public abstract String getDeviceID();

	public abstract boolean appendDeviceInfo(JDFDeviceList dl);
	
	public abstract JDFDoc processJMF(JDFDoc doc);

}