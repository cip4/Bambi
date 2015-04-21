package org.cip4.bambi.actions.beans;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "XMLDevice")
public class XMLDevice {
	private String deviceId;

	public String getDeviceId() {
		return deviceId;
	}

	@XmlAttribute(name = "DeviceID")
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
}
