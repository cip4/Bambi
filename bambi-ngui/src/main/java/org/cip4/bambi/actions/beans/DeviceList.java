package org.cip4.bambi.actions.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DeviceList")
public class DeviceList {
	@XmlElement(name = "XMLDevice")
    private List<XMLDevice> xmlDevice;
	
	public List<XMLDevice> getXMLDevice() {
        return xmlDevice;
    }
	
	public void setXMLDevice(List<XMLDevice> d) {
        this.xmlDevice = d;
    }

}
