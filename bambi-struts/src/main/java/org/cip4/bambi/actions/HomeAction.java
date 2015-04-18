package org.cip4.bambi.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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

import com.opensymphony.xwork2.ActionSupport;

public class HomeAction extends ActionSupport implements ServletRequestAware {
	final static Logger log = Logger.getLogger(HomeAction.class);
	private HttpServletRequest request;
//	private StreamRequest sr;
	
	private List<XMLDevice> device;
	
	public String execute() throws Exception {
//		BambiContainer.getCreateInstance();
		BambiContainer theContainer = BambiContainer.getInstance();
		log.info("theContainer action: " + theContainer);
		
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
		log.info("xr: " + xr);
		log.info("xr.getXMLDoc().getRoot().getChildElementArray.length: " + xr.getXMLDoc().getRoot().getChildElementArray().length);
		
		String xmlResp = xr.getXML().toDisplayXML(0);
		log.debug("xmlResp: " + xmlResp);
		
		InputStream is = new ByteArrayInputStream(xmlResp.getBytes());
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(DeviceList.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			DeviceList deviceList = (DeviceList) jaxbUnmarshaller.unmarshal(is);
			
			log.debug("0, deviceId: " + deviceList.getXMLDevice().get(0).getDeviceId());
			log.debug("1, deviceId: " + deviceList.getXMLDevice().get(1).getDeviceId());
			
			device = deviceList.getXMLDevice();
		} catch (JAXBException e) {
			log.error("Error:", e);
		}
		
	}
	
	public List<XMLDevice> getDevices() {
		return device;
	}

	public void setServletRequest(HttpServletRequest httpServletRequest) {
		request = httpServletRequest;
	}
}
