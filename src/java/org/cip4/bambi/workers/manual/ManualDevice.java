/*
*
* The CIP4 Software License, Version 1.0
*
*
* Copyright (c) 2001-2008 The International Cooperation for the Integration of 
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

package org.cip4.bambi.workers.manual;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.enums.ValuedEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractBambiServlet;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.IGetHandler;
import org.cip4.bambi.workers.core.AbstractWorkerDevice;
import org.cip4.bambi.workers.core.AbstractWorkerDeviceProcessor.JobPhase;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StatusCounter;

/**
 * a simple JDF device.<br>
 * A ManualDevice does not contain a fixed list of JobPhases. After a QueueEntry has been 
 * submitted, it starts with an idle job phase. Additional Job phases may be added via 
 * the web interface. Processing of the QueueEntry finishes when ordered by the user. <br>
 * This class should remain final: if it is ever subclassed, the DeviceProcessor thread 
 * would be started before the constructor from the subclass has a chance to fire.
 * 
 * 
 * @author boegerni
 * 
 */
public final class ManualDevice extends AbstractWorkerDevice  implements IGetHandler {
	

	private class XMLSimDevice extends XMLDoc
    {
    
        private JobPhase currentJobPhase;
    
        /**
         * XML representation of this simDevice
         * fore use as html display using an XSLT
         * @param dev
         */
        public XMLSimDevice(ManualDevice dev)
        {
            super("SimDevice",null);
            setXSLTURL("showManualDevice.xsl");
            KElement root=getRoot();
            root.setAttribute(AttributeName.DEVICEID, dev.getDeviceID());
            root.setAttribute(AttributeName.DEVICETYPE, dev.getDeviceType());
            root.setAttribute("DeviceURL", dev.getDeviceURL());
            root.setAttribute(AttributeName.DEVICESTATUS, dev.getDeviceStatus().getName());
            currentJobPhase = dev.getCurrentJobPhase();
            KElement phase=addPhase();
            // TODO Auto-generated constructor stub
        }
    
        /**
         * @param currentJobPhase
         * @return
         */
        private KElement addPhase()
        {
            KElement root=getRoot();
            KElement phase=root.appendElement("Phase");
            
            final EnumDeviceStatus deviceStatus = currentJobPhase.getDeviceStatus();
            final EnumNodeStatus nodeStatus = currentJobPhase.getNodeStatus();
            if(deviceStatus!=null  && nodeStatus!=null)
            {
                phase.setAttribute("DeviceStatus",deviceStatus.getName(),null);
                phase.setAttribute("DeviceStatusDetails",currentJobPhase.getDeviceStatusDetails());
                phase.setAttribute("NodeStatus",nodeStatus.getName(),null);
                phase.setAttribute("NodeStatusDetails",currentJobPhase.getNodeStatusDetails());
                phase.setAttribute(AttributeName.DURATION,(double)currentJobPhase.getTimeToGo()/1000.,null);  
                VString v=currentJobPhase.getAmountResourceNames();
                int vSiz=v==null ? 0 : v.size();
                for(int i=0;i<vSiz;i++)
                {
                    addAmount(v.stringAt(i), phase);
                }
                addOptionList(deviceStatus,EnumDeviceStatus.iterator(),phase,"DeviceStatus");
                addOptionList(nodeStatus,EnumNodeStatus.iterator(),phase,"NodeStatus");
            }
            else
            {
                log.error("null status - bailing out");
            }
            return null;
        }
    
        /**
         * @param deviceStatus
         */
        private void addOptionList(ValuedEnum e, Iterator<ValuedEnum>it,KElement parent, String name)
        {
            if(e==null || parent==null)
                return;
            KElement list=parent.appendElement("OptionList");
            list.setAttribute("name", name);
            list.setAttribute("default", e.getName());
            while(it.hasNext())
            {
                ValuedEnum ve=it.next();
                KElement option=list.appendElement("Option");
                option.setAttribute("name", ve.getName());
                option.setAttribute("selected", ve.equals(e)?"selected":null,null);
            }
            
        }
    
        /**
         * @param string
         */
        private void addAmount(String resString, KElement jp)
        {
            if(jp==null)
                return;
            KElement amount=jp.appendElement("ResourceAmount");
            amount.setAttribute("ResourceName", resString);
            amount.setAttribute("ResourceIndex", jp.getParentNode_KElement().numChildElements("ResourceIndex", null)-1,null);
            amount.setAttribute("Good", currentJobPhase.getOutput_Good(resString,-1),null);
            amount.setAttribute("Waste", currentJobPhase.getOutput_Waste(resString,-1),null);            
            amount.setAttribute("Speed", currentJobPhase.getOutput_Speed(resString),null);            
        }        
    }

    /**
	 * 
	 */
	private static final long serialVersionUID = -2337883311731643911L;
	private static Log log = LogFactory.getLog(ManualDevice.class.getName());
	
	public ManualDevice(IDeviceProperties prop)
	{
		super(prop);
 		log.info("created ManualDevice '"+prop.getDeviceID()+"'");
	}

	public JobPhase getCurrentJobPhase()
	{
		return _theDeviceProcessor.getCurrentJobPhase();
	}
	
	public void doNextJobPhase(JobPhase nextPhase)
	{
		((ManualDeviceProcessor)_theDeviceProcessor).doNextPhase(nextPhase);
	}
	
	@Override
	protected AbstractDeviceProcessor buildDeviceProcessor() {
		return new ManualDeviceProcessor();
	}

    public String getTrackResource()
    {
        return _trackResource;
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.core.IGetHandler#handleGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    public boolean handleGet(HttpServletRequest request, HttpServletResponse response, String context)
    {
        final String reqDeviceID=AbstractBambiServlet.getDeviceIDFromRequest(request);
        if(reqDeviceID==null)
            return false;
        if(!reqDeviceID.equals(getDeviceID()))
            return false;
        if("showDevice".equals(context))
        {
            return showDevice(request,response);
        }
        else if("processNextPhase".equals(context))
        {
            return processNextPhase(request,response);
        }
        return false;
    }

    private boolean showDevice(HttpServletRequest request,HttpServletResponse response)
    {
        XMLSimDevice simDevice=new XMLSimDevice(this);
        try
        {
            simDevice.write2Stream(response.getOutputStream(), 0,true);
        }
        catch (IOException x)
        {
            return false;
        }
        response.setContentType(MimeUtil.TEXT_XML);
        return true;
    }

    private boolean processNextPhase(HttpServletRequest request, HttpServletResponse response) {

        JobPhase nextPhase = buildJobPhaseFromRequest(request);
        doNextJobPhase(nextPhase);
        StatusCounter.sleep(1000); // allow device to switch phases before displaying page
        showDevice(request, response);
        return true;
    }

    /**
     * build a new job phase with info from a given request. 
     * JobPhase parameter 'timeToGo' will remain with its default value 0, since it
     * is not used in the context of ManualDevice.doNextJobPhase()
     * @param request request to get the job phase info from
     * @return the new JobPhase
     */
    private JobPhase buildJobPhaseFromRequest(HttpServletRequest request) {
        JobPhase newPhase = new JobPhase();

        String status = request.getParameter("DeviceStatus");
        if (status != null) {
            newPhase.deviceStatus = EnumDeviceStatus.getEnum( status );
        }
        newPhase.deviceStatusDetails = request.getParameter("DeviceStatusDetails");

        status = request.getParameter("NodeStatus");
        if (status != null) {
            newPhase.nodeStatus = EnumNodeStatus.getEnum(status);
        }
        newPhase.nodeStatusDetails = request.getParameter("NodeStatusDetails");

        newPhase.setAmount(getTrackResource(), 
                AbstractBambiServlet.getDoubleFromRequest(request, "Speed"),
                !AbstractBambiServlet.getBooleanFromRequest(request, "Waste") );
        newPhase.timeToGo=Integer.MAX_VALUE; // until modified...
        return newPhase;
    }
    
}