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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.enums.ValuedEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.messaging.MessageSender;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.resource.JDFDeviceList;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 * Entrance point for Bambi servlets 
 * @author boegerni
 *
 */
public abstract class AbstractBambiServlet extends HttpServlet {

    /**
     * handler for final handler for any non-handled url
     * @author prosirai
     *
     */
    protected class UnknownErrorHandler implements IGetHandler
    {

        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IGetHandler#handleGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
         */
        public boolean handleGet(HttpServletRequest request, HttpServletResponse response, String context)
        {

            showErrorPage("No handler for URL", request.getPathInfo(), request, response);
            return true;
        }
    }

    /**
     * handler for the overview page
     * @author prosirai
     *
     */
    protected class OverviewHandler implements IGetHandler
    {
        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IGetHandler#handleGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
         */
        public boolean handleGet(HttpServletRequest request, HttpServletResponse response, String context)
        {
            if(KElement.isWildCard(context)||context.equalsIgnoreCase("overview"))
            {
                request.setAttribute("devices", getDevices());
                try {
                    request.getRequestDispatcher("/overview.jsp").forward(request, response);
                } catch (Exception e) {
                    log.error(e);
                } 
                return true;
            }
            else
                return false;
        }
    }
    /**
     * 
     * handler for the knowndevices query
     */
    protected class KnownDevicesHandler implements IMessageHandler
    {
        public KnownDevicesHandler() {
            super();
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage m, JDFResponse resp)
        {
            return handleKnownDevices(m, resp);
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#getFamilies()
         */
        public EnumFamily[] getFamilies()
        {
            return new EnumFamily[]{EnumFamily.Query};
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#getMessageType()
         */
        public EnumType getMessageType()
        {
            return EnumType.KnownDevices;
        }
    }

    protected IJMFHandler _jmfHandler=null;
    protected HashMap<String,IDevice> _devices = null;
    protected IConverterCallback _callBack = null;
    private static Log log = LogFactory.getLog(AbstractBambiServlet.class.getName());
    protected List<IGetHandler> _getHandlers=new Vector<IGetHandler>();
    protected DumpDir bambiDumpIn=null;
    protected DumpDir bambiDumpOut=null;
    public static int port=0;



    /** Initializes the servlet.
     * @throws MalformedURLException 
     */
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        _devices=new HashMap<String, IDevice>();
        super.init(config);
        ServletContext context = config.getServletContext();       
        String dump=config.getInitParameter("bambiDump");
        if(dump!=null)
        {
            bambiDumpIn=new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("in")));
            MessageSender.inDump=bambiDumpIn;
            bambiDumpOut=new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("out")));
            MessageSender.outDump=bambiDumpOut;
        }
        log.info( "Initializing servlet for "+context.getServletContextName() );
//      String appDir=context.getRealPath("")+"/";
        loadProperties(context,new File("/config/devices.xml"));

        // jmf handlers
        _jmfHandler=new JMFHandler((IDeviceProperties)null);
        addHandlers();

        // doGet handlers
        _getHandlers.add(this.new OverviewHandler());
        _getHandlers.add(this.new UnknownErrorHandler());
    }

    /**
     * create the specified directories, if the do not exist
     * @param dirs the directories to create
     */
    private void createDirs(Vector<File> dirs) {
        for (int i=0;i<dirs.size();i++) {
            File f=dirs.get(i);
            if (f!=null && !f.exists()) {
                if (!f.mkdirs())
                    log.error("failed to create directory "+f);
            }
        }
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
     * display an error on error.jsp
     * @param errorMsg short message describing the error
     * @param errorDetails detailed error info
     * @param request required to forward the page
     * @param response required to forward the page
     */
    protected void showErrorPage(String errorMsg, String errorDetails, HttpServletRequest request, HttpServletResponse response)
    {
        request.setAttribute("errorOrigin", this.getClass().getName());
        request.setAttribute("errorMsg", errorMsg);
        request.setAttribute("errorDetails", errorDetails);

        try {
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        } catch (ServletException e) {
            System.err.println("failed to show error.jsp");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("failed to show error.jsp");
            e.printStackTrace();
        }
    }

    /**
     * @param request
     * @param response
     */
    protected void processError(BambiServletRequest request, BambiServletResponse response, EnumType messageType, int returnCode, String notification)
    {
        log.warn("processError- rc: "+returnCode+" "+notification==null ? "" : notification);
        JDFJMF error=JDFJMF.createJMF(EnumFamily.Response, messageType);
        JDFResponse r=error.getResponse(0);
        r.setReturnCode(returnCode);
        r.setErrorText(notification);
        response.setContentType(MimeUtil.VND_JMF);
        if(_callBack!=null)
            _callBack.updateJMFForExtern(error.getOwnerDocument_JDFElement());

        try {
            error.getOwnerDocument_KElement().write2Stream(response.getOutputStream(), 0, true);
        } catch (IOException x) {
            log.error("processError: cannot write response\n"+x.getMessage());
        }
    }

    /**
     * process a multipart request - including job submission
     * @param request
     * @param response
     */
    protected void processMultipleDocuments(BambiServletRequest request, BambiServletResponse response,BodyPart[] bp)
    {
        log.info("processMultipleDocuments- parts: "+(bp==null ? 0 : bp.length));
        if(bp==null || bp.length<2) {
            processError(request, response, EnumType.Notification, 2,"processMultipleDocuments- not enough parts, bailing out");
            return;
        }
        JDFDoc docJDF[]=MimeUtil.getJMFSubmission(bp[0].getParent());
        if(docJDF==null) {
            processError(request, response, EnumType.Notification, 2,"proccessMultipleDocuments- incorrect jmf/jdf parts, bailing out!");
            return;
        }
        if(_callBack!=null)
        {
            for(int i=0;i<docJDF.length;i++)
            {
                final JDFDoc doc = docJDF[i];
                KElement e=doc.getRoot();
                
                final String localName = e==null ? null : e.getLocalName();
                if(ElementName.JMF.equals(localName))
                {
                    _callBack.prepareJMFForBambi(doc);
                }
                else if(ElementName.JDF.equals(localName))
                {
                    _callBack.prepareJDFForBambi(doc);
                }                
            }
        }
        processJMFDoc(request, response, docJDF[0]);
    }

    /**
     * process zhe main, i.e. doc #0 JMF document
     * 
     * @param request the http request to service
     * @param response the http response to fill
     * @param jmfDoc the extracted first jmf bodypart or raw jmf
     */
    protected void processJMFDoc(BambiServletRequest request, BambiServletResponse response, JDFDoc jmfDoc) {
        if(jmfDoc==null) 
        {
            processError(request, response, null, 3, "Error Parsing JMF");
        } 
        else 
        {
            if(_callBack!=null)
            {
                _callBack.prepareJMFForBambi(jmfDoc);
            }

            // switch: sends the jmfDoc to correct device
            JDFDoc responseJMF = null;
            IJMFHandler handler = getTargetHandler(request);
            if (handler != null) {
                responseJMF=handler.processJMF(jmfDoc);
            } 

            if (responseJMF!=null) {
                response.setContentType(MimeUtil.VND_JMF);
                if(_callBack!=null)
                    _callBack.updateJMFForExtern(responseJMF);

                try 
                {
                    responseJMF.write2Stream(response.getBufferedOutputStream(), 0, true);
                } 
                catch (IOException e) 
                {
                    log.error("cannot write to stream: ",e);
                }
            } else {
                processError(request, response, null, 3, "Error Parsing JMF");               
            }
        }
    }

    protected IJMFHandler getTargetHandler(HttpServletRequest request)
    {
        IDevice device =  getDeviceFromRequest(request);
        if (device == null)
            return _jmfHandler; // device not found
        return( device.getHandler() );
    }

    /**
     * Parses a multipart request.
     */
    protected void processMultipartRequest(BambiServletRequest request, BambiServletResponse response)
    throws IOException {
        InputStream inStream=request.getBufferedInputStream();
        BodyPart bp[]=MimeUtil.extractMultipartMime(inStream);
        log.info("Body Parts: "+((bp==null) ? 0 : bp.length));
        if(bp==null || bp.length==0) {
            processError(request,response,null,9,"No body parts in mime package");
            return;
        }
        try  {// messaging exceptions
            if(bp.length>1) {
                processMultipleDocuments(request,response,bp);
            } else {
                String s=bp[0].getContentType();
                if(MimeUtil.VND_JMF.equalsIgnoreCase(s)) {
                    processJMFRequest(request, response, bp[0].getInputStream());            
                }
            }
        } catch (MessagingException x) {
            processError(request, response, null, 9, "Messaging exception\n"+x.getLocalizedMessage());
        }
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws IOException 
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws IOException	{
        log.debug("Processing post request for: "+request.getPathInfo());
        BambiServletRequest bufRequest=null;
        BambiServletResponse bufResponse=null;

        if(bambiDumpIn!=null)
        {
            try
            {
                bufRequest=new BambiServletRequest(request,true);
                bufResponse=new BambiServletResponse(response,true);

                File f=bambiDumpIn.newFile();
                FileOutputStream fs=new FileOutputStream(f);
                PrintWriter w=new PrintWriter(fs);
//              w.println("Context Type:"+http);
                w.println("Context Path:"+request.getContextPath());

                final String contentType = request.getContentType();
                w.println("Context Type:"+contentType);
                w.println("Context Length:"+request.getContentLength());
                w.print("------ end of http header ------\n");
                w.flush();
                
                IOUtils.copy(bufRequest.getBufferedInputStream(), fs);
                fs.flush();
                fs.close();
            }
            catch (IOException e) {
                bufRequest=new BambiServletRequest(request,false);            
                bufResponse=new BambiServletResponse(response,false);
            }
        }
        else
        {
            bufRequest=new BambiServletRequest(request,false);     
            bufResponse=new BambiServletResponse(response,false);

        }

        String contentType=request.getContentType();
        if(MimeUtil.VND_JMF.equals(contentType)) {
            processJMFRequest(bufRequest,bufResponse,null);
        } else {
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (isMultipart) {
                log.info("Processing multipart request... (ContentType: "+contentType+")");
                processMultipartRequest(bufRequest, bufResponse);
            } else {
                log.warn("Unknown ContentType: "+contentType);
                response.setContentType("text/plain");

                OutputStream os=response.getOutputStream();
                InputStream is=bufRequest.getBufferedInputStream();
                IOUtils.copy(is,os);
            }
        }
        if(bambiDumpOut!=null)
        {
            try
            {
                File f=bambiDumpOut.newFile();
                FileOutputStream fs=new FileOutputStream(f);
                InputStream bufIn=bufResponse.getBufferedInputStream();
                IOUtils.copy(bufIn, fs);
                fs.flush();
                fs.close();
            }
            catch (IOException e) {
                // nop
            }
        }
        bufResponse.flush();
    }

    /**
     * @param request
     * @param response
     */
    private void processJMFRequest(BambiServletRequest request, BambiServletResponse response,InputStream inStream) throws IOException
    {
        log.debug("processJMFRequest");
        JDFParser p=new JDFParser();
        if(inStream==null)
            inStream=request.getBufferedInputStream();
        JDFDoc jmfDoc=p.parseStream(inStream);
        processJMFDoc(request, response, jmfDoc);
    }

    /**
     * loads properties
     * @param appDir   the directory of the web application
     * @param fileName the name of the Java .propert file
     * @return true, if the properties have been loaded successfully
     */
    protected void loadProperties(ServletContext context, File config)
    {
        MultiDeviceProperties props=new MultiDeviceProperties(context,config);
        createDevices(props);
    }

    protected void addHandlers() {
        _jmfHandler.addHandler( new AbstractBambiServlet.KnownDevicesHandler() );
    }

    /**
     * write a String to a writer of a HttpServletResponse, show error.jsp if failed
     * @param request  the request
     * @param response theStr will be written to the PrintWriter of this response
     * @param theStr   the String to write
     */
    protected void writeRawResponse(HttpServletRequest request,
            HttpServletResponse response, String theStr) {
        PrintWriter out=null;
        try {
            out = response.getWriter();
            out.println(theStr);
            out.flush();
            out.close();
        } catch (IOException e) {
            showErrorPage("failed to write response", e.getMessage(), request, response);
            log.error("failed to write response: "+e.getMessage());
        }
    }

    /** 
     * Destroys the servlet.
     */
    @Override
    public void destroy() {
        Set<String> keys=_devices.keySet();
        Iterator<String> it=keys.iterator();
        while (it.hasNext()) {
            String devID=it.next();
            AbstractDevice dev=(AbstractDevice) _devices.get(devID);
            dev.shutdown();
        }
        _devices.clear();
        JMFFactory.shutDown(null, true);
    }

    /**
     * @param request
     */
    protected IDevice getDeviceFromRequest(HttpServletRequest request)
    {
        String deviceID = getDeviceIDFromRequest(request);
        IDevice dev = getDevice(deviceID);
        if (dev == null) {
            log.info("invalid request: device with id="+deviceID==null?"null":deviceID+" not found");
            return null;
        }		
        return dev;
    }

    /**
     * 
     * @param request the request to parse
     * @return
     */
    public static String getDeviceIDFromRequest(HttpServletRequest request)
    {
        String deviceID = request.getParameter("id");
        if (deviceID == null) {
            deviceID = request.getPathInfo();
            deviceID = StringUtil.token(deviceID, 0, "/");
        }
        return deviceID;        
    }
    /**
     * add a set of options to an xml file
     * @param e the default enum
     * @param it the iterator over all enums
     * @param parent the parent element to add the list to
     * @param name the name of the option list form
     */
    public static void addOptionList(ValuedEnum e, List l,KElement parent, String name)
    {
        if(e==null || parent==null)
            return;
        KElement list=parent.appendElement(BambiNSExtension.MY_NS_PREFIX+"OptionList",BambiNSExtension.MY_NS); 
        list.setAttribute("name", name);
        list.setAttribute("default", e.getName());
        Iterator<ValuedEnum> it=l.iterator();
        while(it.hasNext())
        {
            ValuedEnum ve=it.next();
            KElement option=list.appendElement(BambiNSExtension.MY_NS_PREFIX+"Option",BambiNSExtension.MY_NS);
            option.setAttribute("name", ve.getName());
            option.setAttribute("selected", ve.equals(e)?"selected":null,null);
        }
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
            return null;
        }
        else if(deviceID==null)   
        {
            log.warn("attempting to retrieve null device");
            return null;           
        }
        return _devices.get(deviceID);
    }

    public HashMap<String, IDevice> getDevices()
    {
        return _devices;
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

    /**
     * extract a double attribute from a given request
     * @param request
     * @param param
     * @return
     */
    public static double getDoubleFromRequest(HttpServletRequest request, String param)
    {
        final String val = request.getParameter(param);
        return StringUtil.parseDouble(val, 0.0);
    }

    /**
     * extract a boolean attribute from a given request
     * @param request
     * @param param
     * @return true if the parameter is"true", else false
     */
    public static boolean getBooleanFromRequest(HttpServletRequest request, String param)
    {
        final String val = request.getParameter(param);
        return StringUtil.parseBoolean(val, false);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        String context = getContext(request);
        // simply loop over all handlers until you are done
        for(int i=0;i<_getHandlers.size();i++)
        {
            IGetHandler ig=_getHandlers.get(i);
            final boolean bHandled=ig.handleGet(request, response, context);
            if(bHandled)
                return;
        }
    }

    /**
     * get the static context string
     * @param request
     * @return
     */
    public static String getContext(HttpServletRequest request)
    {
        String context = request.getParameter("cmd");
        if(context==null)
        {
            context=UrlUtil.getLocalURL(request.getContextPath(),request.getRequestURI());
        }
        return context;
    }

    public static boolean isMyRequest(HttpServletRequest request,final String deviceID, String context)
    {
        if(deviceID!=null)
        {
            final String reqDeviceID=getDeviceIDFromRequest(request);
            if(!deviceID.equals(reqDeviceID))
                return false;           
        }
        String reqContext=getContext(request);
        if(context.equals(StringUtil.token(reqContext, 0, "/")))
            return true;
        return false;
    }

    /**
     * get the number of devices
     * used only for test purposes 
     * @return
     */
    public int getDeviceQuantity()
    {
        return _devices==null ? 0 : _devices.size();
    }

    /**
     * build a new device instance
     * @param prop
     * @return
     */
    protected abstract IDevice buildDevice(IDeviceProperties prop);

    /**
     * create a new device and add it to the map of devices.
     * @param deviceID
     * @param deviceType
     * @return the Device, if device has been created. 
     * null, if not (maybe device with deviceID is already present)
     */
    private IDevice createDevice(IDeviceProperties prop)
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
        IDevice dev = buildDevice(prop);
        _devices.put(devID,dev);
        log.info("created device "+devID);
        return dev;
    }

    /**
     * create devices based on the list of devices given in a file
     * @param props 
     * @param configFile the file containing the list of devices 
     * @return true if successfull, otherwise false
     */
    private boolean createDevices(MultiDeviceProperties props)
    {

        Vector<File> dirs=new Vector<File>();
        dirs.add( props.getBaseDir() );
        dirs.add( props.getJDFDir() );
        createDirs(dirs);

        VString v=props.getDeviceIDs();
        Iterator<String> iter=v.iterator();
        while (iter.hasNext()) {
            String devID=iter.next();
            IDeviceProperties prop=props.getDevice(devID);
            _callBack=prop.getCallBackClass(); // the last one wins
            createDevice(prop);
        }

        return true;
    }

    @Override
    protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException
    {
        if(port==0) // quick hack
            port=arg0.getServerPort();
        super.service(arg0, arg1);

    }

}
