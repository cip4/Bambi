package org.cip4.bambi.actions.beans;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "XMLDevice")
public class XMLDevice {
	private String deviceId;
	private String deviceStatus;
	private int queueAll;
	private int queueCompleted;
	private int queueRunning;
	private int queueWaiting;
	private String queueStatus;

	public String getDeviceId() {
		return deviceId;
	}

	@XmlAttribute(name = "DeviceID")
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceStatus() {
		return deviceStatus;
	}

	@XmlAttribute(name = "DeviceStatus")
	public void setDeviceStatus(String deviceStatus) {
		this.deviceStatus = deviceStatus;
	}

	public int getQueueAll() {
		return queueAll;
	}

	@XmlAttribute(name = "QueueAll")
	public void setQueueAll(int queueAll) {
		this.queueAll = queueAll;
	}

	public int getQueueCompleted() {
		return queueCompleted;
	}

	@XmlAttribute(name = "QueueCompleted")
	public void setQueueCompleted(int queueCompleted) {
		this.queueCompleted = queueCompleted;
	}

	public int getQueueRunning() {
		return queueRunning;
	}

	@XmlAttribute(name = "QueueRunning")
	public void setQueueRunning(int queueRunning) {
		this.queueRunning = queueRunning;
	}

	public int getQueueWaiting() {
		return queueWaiting;
	}

	@XmlAttribute(name = "QueueWaiting")
	public void setQueueWaiting(int queueWaiting) {
		this.queueWaiting = queueWaiting;
	}

	public String getQueueStatus() {
		return queueStatus;
	}

	@XmlAttribute(name = "QueueStatus")
	public void setQueueStatus(String queueStatus) {
		this.queueStatus = queueStatus;
	}

}
