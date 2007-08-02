package org.cip4.bambi;

import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.resource.JDFDeviceList;

public interface IDevice {

	public abstract String getDeviceType();

	public abstract String getDeviceID();

	public abstract JDFDoc processJMF(JDFDoc doc);

	public abstract String toString();

	public abstract boolean getDeviceInfo(JDFDeviceList dl);

}