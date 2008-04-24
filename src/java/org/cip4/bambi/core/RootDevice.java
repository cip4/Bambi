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

package org.cip4.bambi.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFDeviceList;

/**
 * the dispatcher / rootDev controller device
 * 
 */
public class RootDevice extends AbstractDevice  
{
    protected HashMap<String,IDevice> _devices = null;

    /**
     * 
     */
    private static final long serialVersionUID = -4412710163767830461L;
    private static Log log = LogFactory.getLog(RootDevice.class.getName());


    public RootDevice(IDeviceProperties prop)
    {
        super(prop);
        _devices=new HashMap<String, IDevice>();
        _jmfHandler.setFilterOnDeviceID(false); // accept all

        log.info("created RootDevice '"+prop.getDeviceID()+"'");
    }

    protected void addHandlers() {
        super.addHandlers();
        _jmfHandler.addHandler(this.new KnownDevicesHandler() );
        _jmfHandler.addHandler(this.new StatusHandler());
        _jmfHandler.addHandler(this.new RootDispatchHandler(EnumType.Resource,new EnumFamily[]{EnumFamily.Query,EnumFamily.Command}));
        _jmfHandler.addHandler(this.new QueueDispatchHandler(EnumType.AbortQueueEntry,new EnumFamily[]{EnumFamily.Command}));
        _jmfHandler.addHandler(this.new QueueDispatchHandler(EnumType.HoldQueueEntry,new EnumFamily[]{EnumFamily.Command}));
        _jmfHandler.addHandler(this.new QueueDispatchHandler(EnumType.RemoveQueueEntry,new EnumFamily[]{EnumFamily.Command}));
        _jmfHandler.addHandler(this.new QueueDispatchHandler(EnumType.ResumeQueueEntry,new EnumFamily[]{EnumFamily.Command}));
    }

    @Override
    protected AbstractDeviceProcessor buildDeviceProcessor() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.core.AbstractDevice#canAccept(org.cip4.jdflib.core.JDFDoc)
     */
    @Override
    public boolean canAccept(JDFDoc doc)
    {
       Iterator<String> it=_devices.keySet().iterator();
       while(it.hasNext())
       {
           AbstractDevice ad=(AbstractDevice) _devices.get(it.next());
           if(ad.canAccept(doc))
               return true;
           
       }
       return false;
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.core.AbstractDevice#getNodeFromDoc(org.cip4.jdflib.core.JDFDoc)
     */
    @Override
    public JDFNode getNodeFromDoc(JDFDoc doc)
    {
        Iterator<String> it=_devices.keySet().iterator();
        while(it.hasNext())
        {
            AbstractDevice ad=(AbstractDevice) _devices.get(it.next());
            JDFNode n=ad.getNodeFromDoc(doc);
            if(n!=null)
                return n;
        }
        return null;
    }
    /**
     * create a new device and add it to the map of devices.
     * @param deviceID
     * @param deviceType
     * @return the Device, if device has been created. 
     * null, if not (maybe device with deviceID is already present)
     */
    IDevice createDevice(IDeviceProperties prop,BambiServlet servlet)
    {
        if (_devices == null) {
            log.info("map of devices is null, re-initialising map...");
            _devices = new HashMap<String, IDevice>();
        }

        String devID=prop.getDeviceID();
        if (_devices.get(prop.getDeviceID()) != null) { 
            log.warn("device "+devID+" is already existing");
            return null;
        }
        IDevice dev;
        if(servlet!=null)
        {
            dev= prop.getDeviceClass();
            if(dev instanceof AbstractDevice)
            {
                final AbstractDevice abstractDevice = ((AbstractDevice)dev);
                abstractDevice.setRootDevice(this);
                servlet._getHandlers.add(0,abstractDevice);
            }
 
            _devices.put(devID,dev);

        }
        else
        {
            //TODO is rootDev in this?
            dev=this;
        }
        log.info("created device "+devID);
        return dev;
    }

    protected boolean handleKnownDevices(JDFMessage m, JDFResponse resp) {
        if(m==null || resp==null)
        {
            return false;
        }
//      log.info("Handling "+m.getType());
        EnumType typ=m.getEnumType();
        if(EnumType.KnownDevices.equals(typ)) {
            JDFDeviceList dl = resp.appendDeviceList();
            appendDeviceInfo(dl); // write myself into the list...
            Set<String> keys = _devices.keySet();
            Object[] strKeys = keys.toArray();
            for (int i=0; i<keys.size();i++) {
                String key = (String)strKeys[i];
                IDevice dev = _devices.get(key);
                if (dev == null)
                    log.error("device with key '"+key+"'not found");
                else
                    dev.appendDeviceInfo(dl);
            }
            return true;
        }

        return false;
    }

    /**
     * 
     * handler for the knowndevices query
     */
    protected class KnownDevicesHandler extends AbstractHandler
    {
        public KnownDevicesHandler() {
            super(EnumType.KnownDevices,new EnumFamily[]{EnumFamily.Query});
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage m, JDFResponse resp)
        {
            return handleKnownDevices(m, resp);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    
    public class StatusHandler extends RootDispatchHandler
    {
        public StatusHandler()
        {
            super(EnumType.Status,new EnumFamily[]{EnumFamily.Query,EnumFamily.Command});
        }
        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
        {
            final JDFStatusQuParams statusQuParams = inputMessage.getStatusQuParams();
            boolean bQueue=statusQuParams==null ? false : statusQuParams.getQueueInfo();
            boolean bGood= super.handleMessage(inputMessage, response);
            if(bGood&&bQueue)
            {
                VElement vq=response.getChildElementVector(ElementName.QUEUE, null);
                
                JDFQueue q=_theQueueProcessor.getQueue().copyToResponse(response, null);
                int nQ=vq==null ? 0 : vq.size();
                final JDFQueueEntry qe0 = q.getQueueEntry(0);
                for(int i=0;i<nQ;i++)
                {
                    final JDFQueue qi = (JDFQueue) vq.elementAt(i);
                    VElement vQE=qi.getQueueEntryVector();
                    int nQE=vQE==null ? 0 : vQE.size(); 

                    for(int j=0;j<nQE;j++)
                    {
                        q.copyElement(vQE.elementAt(j), qe0);
                    }
                }
                QueueProcessor.removeBambiNSExtensions(q);
            }
            return bGood;
        }

    }
     /**
     * 
     * handler for the StopPersistentChannel command
     */
    public  class RootDispatchHandler extends DispatchHandler
    {
        public RootDispatchHandler(EnumType _type, EnumFamily[] _families)
        {
            super(_type,_families);
        }
        public RootDispatchHandler(String _type, EnumFamily[] _families)
        {
            super(_type,_families);
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
        {
            IDevice[] devs=getDeviceArray();
            return super.handleMessage(inputMessage, response, devs,false);
        }
    }
    /**
     * 
     * handler for the StopPersistentChannel command
     */
    public  class QueueDispatchHandler extends DispatchHandler
    {
        private IMessageHandler superHandler=null;
        public QueueDispatchHandler(EnumType _type, EnumFamily[] _families)
        {
            super(_type,_families);       
            superHandler=_jmfHandler.getHandler(_type.getName(), _families[0]);
        }
        public QueueDispatchHandler(String _type, EnumFamily[] _families)
        {
            super(_type,_families);       
            superHandler=_jmfHandler.getHandler(_type, _families[0]);
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
        {
            boolean bHandled=false;
            if(superHandler!=null)
                bHandled=superHandler.handleMessage(inputMessage, response);
            if(bHandled)
            {
                int rc=response.getReturnCode();
                bHandled=rc==0;
            }
            if(!bHandled)
            {
                IDevice[] devs=getDeviceArray();
                bHandled= super.handleMessage(inputMessage, response, devs,true);
            }
            return bHandled;
        }
    }
    
    @Override
    public void shutdown()
    {
        Set<String> keys=_devices.keySet();
        Iterator<String> it=keys.iterator();
        while (it.hasNext()) {
            String devID=it.next();
            AbstractDevice dev=(AbstractDevice) _devices.get(devID);
            dev.shutdown();
        }
        _devices.clear();
        super.shutdown();
    }
    
    /**
     * get a device
     * @param deviceID ID of the device to get
     * @return
     */
    public IDevice getDevice(String deviceID)
    {
        if (_devices == null) {
            log.warn("list of devices is null");
            return this;
        }
        else if(deviceID==null)   
        {
            log.warn("attempting to retrieve null device");
            return this;           
        }
        return _devices.get(deviceID);
    }

    public HashMap<String, IDevice> getDevices()
    {
        return _devices;
    }
    
    /**
     * get an array of all child devices
     * @return
     */
    public IDevice[] getDeviceArray()
    {
        if( _devices==null )
            return null;
        Set<Entry<String, IDevice>> s=_devices.entrySet();
        if(s.size()==0)
            return null;
        IDevice[] array=new IDevice[s.size()];
        Iterator<Entry<String, IDevice>> it=s.iterator();
        int i=0;
        while(it.hasNext())
        {
            array[i++]=it.next().getValue();
        }
        return array;
    }

    /**
     * remove device
     * @param deviceID ID of the device to be removed
     * @return
     */
    public boolean removeDevice(String deviceID)
    {
        if (_devices == null) {
            log.error("list of devices is null");
            return false;
        }
        if (_devices.get(deviceID) == null) {
            log.warn("tried to removing non-existing device");
            return false;
        }
        _devices.remove(deviceID);
        return true;
    }
    protected boolean isMyRequest(HttpServletRequest request)
    {
        return BambiServlet.isMyRequest(request, null);
    }

    @Override
    protected boolean showDevice(HttpServletRequest request,HttpServletResponse response, boolean refresh)
    {
        request.setAttribute("devices", getDevices());
        try {
            request.getContextPath();
            final RequestDispatcher requestDispatcher = request.getRequestDispatcher("/overview.jsp");
            requestDispatcher.forward(request, response);
        } catch (Exception e) {
            log.error(e);
        } 
        return true;

    }
}