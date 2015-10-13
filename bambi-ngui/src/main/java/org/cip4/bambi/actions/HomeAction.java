/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2015 The International Cooperation for the Integration of 
 * Processes in  Prepress, Press and Postpress (CIP4).  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        The International Cooperation for the Integration of 
 *        Processes in  Prepress, Press and Postpress (www.cip4.org)"
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of 
 *    Processes in  Prepress, Press and Postpress" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4",
 *    nor may "CIP4" appear in their name, without prior written
 *    permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For
 * details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR
 * THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the The International Cooperation for the Integration 
 * of Processes in Prepress, Press and Postpress and was
 * originally based on software 
 * copyright (c) 1999-2001, Heidelberger Druckmaschinen AG 
 * copyright (c) 1999-2001, Agfa-Gevaert N.V. 
 *  
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
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
import org.cip4.bambi.actions.beans.DeviceJob;
import org.cip4.bambi.actions.beans.DeviceList;
import org.cip4.bambi.actions.beans.XMLDevice;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.BambiContainer;
import org.cip4.bambi.core.BambiServlet;
import org.cip4.bambi.core.StreamRequest;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.bambi.settings.BambiServerUtils;
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
			
			final List<DeviceJob> queue = new ArrayList<DeviceJob>();
			Iterator<JDFQueueEntry> it = queueCurrent.iterator();
			while (it.hasNext()) {
				JDFQueueEntry jdfJob = it.next();
				
				DeviceJob job = new DeviceJob();
				job.setJobId(jdfJob.getQueueEntryID());
				job.setPriority("" + jdfJob.getPriority());
				job.setStatus(jdfJob.getQueueEntryStatus().getName());
				job.setSubmitted(BambiServerUtils.convertTime(jdfJob.getSubmissionTime().getTimeInMillis()));
				job.setStarted(BambiServerUtils.convertTime(getStartTime(jdfJob)));
				job.setEnded(BambiServerUtils.convertTime(getEndTime(jdfJob)));
				
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
	
	private long getStartTime(final JDFQueueEntry qe)
	{
		if (qe.getStartTime() == null)
		{
			return 0;
		}
		return qe.getStartTime().getTimeInMillis();
	}

	private long getEndTime(final JDFQueueEntry qe)
	{
		if (qe.getEndTime() == null)
		{
			return 0;
		}
		return qe.getEndTime().getTimeInMillis();
	}
}
