package org.cip4.bambi.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.cip4.bambi.actions.beans.DeviceList;
import org.cip4.bambi.actions.beans.XMLDevice;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.BambiContainer;
import org.cip4.bambi.core.BambiServlet;
import org.cip4.bambi.core.StreamRequest;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.jdflib.jmf.JDFQueueEntry;

import com.opensymphony.xwork2.ActionSupport;

public class HomeAction extends ActionSupport implements ServletRequestAware {
	private final static Logger log = Logger.getLogger(HomeAction.class);
	
	private final static BambiContainer theContainer = BambiContainer.getInstance();
	
	private HttpServletRequest request;
	
	private List<XMLDevice> deviceList;
	private String deviceId;
	
	public String execute() throws Exception {
		log.info("theContainer action: " + theContainer);
		
//		AbstractDevice device = theContainer.getDeviceFromID("simIDP");
//		log.info("simIDP queue size: " + device.getQueueProcessor().getQueue().getAllQueueEntry().size());
		
		String pageName = SUCCESS;
		
		if (theContainer == null) {
			pageName = ERROR;
		} else {
			log.info("... some work occured here");
			
			AbstractDevice rootDev = theContainer.getRootDev();
			rootDev.startWork();
			
			boolean isPost = request.getMethod().equalsIgnoreCase("POST") ? true : false;
			
			StreamRequest sr = ActionUtils.createStreamRequest(request);
			sr.setPost(isPost);
			
			XMLResponse xr = null;
			try {
				xr = theContainer.processStream(sr);
//				writeResponse(xr, response);
				parseResponse(xr);
				fillDevicesQueue();
			}
			catch (Throwable t) {
				log.error("Error processing request for: " + request.getPathInfo(), t);
				pageName = ERROR;
			}
			
			rootDev.endWork();
		}
		
		return pageName;
	}
	
	private void parseResponse(XMLResponse xr) {
//		log.debug("xr: " + xr);
		
		String xmlResp = xr.getXML().toDisplayXML(0);
		
		InputStream is = new ByteArrayInputStream(xmlResp.getBytes());
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(DeviceList.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			final DeviceList deviceListLocal = (DeviceList) jaxbUnmarshaller.unmarshal(is);
			
			deviceList = deviceListLocal.getXMLDevice();
		} catch (JAXBException e) {
			log.error("Error:", e);
		}
		
	}
	
	private void fillDevicesQueue() {
		for (XMLDevice device : deviceList) {
			final Collection<JDFQueueEntry> queueCurrent = theContainer.getDeviceFromID(device.getDeviceId()).getQueueProcessor().getQueue().getAllQueueEntry();
			log.info("Device: '" + device.getDeviceId() + "' has queue size: " + queueCurrent.size());
			
//			this code just to see API to be used in .jsp
			final List<JDFQueueEntry> queue = new ArrayList<JDFQueueEntry>();
			Iterator<JDFQueueEntry> it = queueCurrent.iterator();
			while (it.hasNext()) {
				JDFQueueEntry job = it.next();
//				job.getJobID();
//				job.getQueueEntryID();
//				job.getSubmissionTime().getDateTimeISO();
//				job.getPriority();
				
				queue.add(job);
			}
			
			Collections.reverse(queue);
			device.setJobsQueue(queue);
		}
	}
	
	public List<XMLDevice> getDevices() {
		return deviceList;
	}

	public void setServletRequest(HttpServletRequest httpServletRequest) {
		request = httpServletRequest;
	}
}
