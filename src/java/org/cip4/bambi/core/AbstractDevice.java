/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2007 The International Cooperation for the Integration of 
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.queues.IQueueProcessor;
import org.cip4.bambi.core.queues.QueueFacade;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.resource.JDFDevice;
import org.cip4.jdflib.resource.JDFDeviceList;
import org.cip4.jdflib.util.MimeUtil;

/**
 * basis for JDF devices. <br>
 * Devices are defined in /WebContent/config/devices.xml<br>
 * Derived classes should be final: if they were ever subclassed, the DeviceProcessor thread 
 * would be started before the constructor from the subclass has a chance to fire.
 * 
 * @author boegerni
 * 
 */
public abstract class AbstractDevice extends HttpServlet implements IDevice, IJMFHandler
{
	/**
	 * 
	 * handler for the KnownDevices query
	 */
	protected class KnownDevicesHandler implements IMessageHandler
	{
	
		/* (non-Javadoc)
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		public boolean handleMessage(JDFMessage m, JDFResponse resp)
		{
			// "I am the known device"
			if(m==null || resp==null)
			{
				return false;
			}
			log.debug("Handling "+m.getType());
			EnumType typ=m.getEnumType();
			if(EnumType.KnownDevices.equals(typ))
			{
				JDFDeviceList dl = resp.appendDeviceList();
				appendDeviceInfo(dl);
				return true;
			}
	
			return false;
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
	private static Log log = LogFactory.getLog(AbstractDevice.class.getName());
	protected IQueueProcessor _theQueueProcessor=null;
	protected AbstractDeviceProcessor _theDeviceProcessor=null;
	protected IStatusListener _theStatusListener=null;
	protected ISignalDispatcher _theSignalDispatcher=null;
	protected JMFHandler _jmfHandler = null ;
	protected IDeviceProperties _devProperties=null;

	/**
	 * creates a new device instance
	 */
	public AbstractDevice() {
		super();
	}
	
	/**
	 * creates a new device instance
	 * @param prop the properties for the device
	 */
	public AbstractDevice(IDeviceProperties prop) {
		super();
		init(prop);
	}

	protected void init(IDeviceProperties prop) {
		_devProperties = prop;
		_jmfHandler = new JMFHandler();

        _theSignalDispatcher=new SignalDispatcher(_jmfHandler, _devProperties.getDeviceID());
        _theSignalDispatcher.addHandlers(_jmfHandler);

        _theQueueProcessor = buildQueueProcessor( );
        _theQueueProcessor.addHandlers(_jmfHandler);
        _theStatusListener=new StatusListener(_theSignalDispatcher, getDeviceID());
        _theStatusListener.addHandlers(_jmfHandler);
        
        String deviceID=_devProperties.getDeviceID();
        _theDeviceProcessor = buildDeviceProcessor();
        _theDeviceProcessor.init(_theQueueProcessor, _theStatusListener, deviceID, prop.getAppDir());
        String deviceProcessorClass=_theDeviceProcessor.getClass().getSimpleName();
		new Thread(_theDeviceProcessor,deviceProcessorClass+"_"+deviceID).start();
		log.info("device thread started: "+deviceProcessorClass+"_"+deviceID);
		
		_devProperties.setDeviceURL( createDeviceURL(deviceID) );
		
		addHandlers();
	}
	
	private void addHandlers() {
		_jmfHandler.addHandler( this.new KnownDevicesHandler() );
	}

	/**
	 * get the device type of this device
	 * @return
	 */
	public String getDeviceType()
	{
		return _devProperties.getDeviceType();
	}

	/* (non-Javadoc)
	 * @see org.cip4.bambi.IDevice#getDeviceID()
	 */
	public String getDeviceID() {
		return _devProperties.getDeviceID();
	}

	public JDFDoc processJMF(JDFDoc doc) {
		log.info("JMF processed by "+_devProperties.getDeviceID());
		return _jmfHandler.processJMF(doc);
	}

	/**
	 * get a String representation of this device
	 */
	public String toString() {
		return ("["+this.getClass().getName()+" Properties="+ _devProperties.toString() +"]");
	}
	
	/**
	 * append the JDFDeviceInfo of this device to a given JDFDeviceList
	 * @param dl the JDFDeviceList, where the JDFDeviceInfo will be appended
	 * @return true, if successful
	 */
	public boolean appendDeviceInfo(JDFDeviceList dl) {
		JDFDeviceInfo info = dl.appendDeviceInfo();
		JDFDevice dev = info.appendDevice();
		dev.setDeviceID(_devProperties.getDeviceID());
		dev.setDeviceType( _devProperties.getDeviceType() );
		dev.setJDFVersions( EnumVersion.Version_1_3.getName() );
		info.setDeviceStatus( getDeviceStatus() );
		return true;
	}

	/**
	 * add a MessageHandler to this devices JMFHandler
	 * @param handler the MessageHandler to add
	 */
	public void addHandler(IMessageHandler handler) {
		_jmfHandler.addHandler(handler);
	}

	/**
	 * get the JMFHandler of this device
	 * @return
	 */
	public IJMFHandler getHandler() {
		return _jmfHandler;
	}
	
	/**
	 * get a facade of this devices Queue
	 * @return
	 */
	public QueueFacade getQueueFacade()
	{
		return (new QueueFacade(_theQueueProcessor.getQueue()) );
	}
	
	/**
	 * get the JDFQueue
	 * @return JDFQueue
	 */
	public JDFQueue getQueue()
	{
		return _theQueueProcessor.getQueue();
	}
	
	/**
	 * get the class name of the device processor
	 * @return
	 */
	public String getDeviceProcessorClass()
	{
		if (_theDeviceProcessor != null)
			return _theDeviceProcessor.getClass().getName();
		else
			return "";
	}
	
	/**
     * get the queprocessor
     * @return
     */
    public IQueueProcessor getQueueProcessor()
    {
        return _theQueueProcessor;
    }
    
    /**
     * get the DeviceStatus of this device
     * @return
     */
    public EnumDeviceStatus getDeviceStatus()
    {
    	EnumDeviceStatus status = _theStatusListener.getDeviceStatus();
    	if (status == null) {
    		log.error("StatusListener returned a null device status");
    		status = EnumDeviceStatus.Unknown;
    	}
    	return status;
    }
    
    /**
     * stop processing the given QueueEntry
     * @param queueEntryID the ID of the QueueEntry to stop
     * @param status target status of the QueueEntry (Suspended,Aborted,Held)
     * @return the updated QueueEntry
     */
    public JDFQueueEntry stopProcessing(String queueEntryID, EnumQueueEntryStatus status)
    {
    	JDFQueue q=_theQueueProcessor.getQueue();
    	if (q==null) {
    		log.fatal("queue of device "+_devProperties.getDeviceID()+"is null");
    		return null;
    	}
    	JDFQueueEntry qe=q.getQueueEntry(queueEntryID);
    	if (qe==null) {
    		log.fatal("QueueEntry with ID="+queueEntryID+" is null on device "+_devProperties.getDeviceID());
    		return null;
    	}
    	
    	_theDeviceProcessor.stopProcessing(qe, status);
    	return qe;
    }

    /**
	 * build the URL of this device
	 * @param deviceID the ID of the device to get the URL for. Use "" for the Bambi Root Device.
	 * @return
	 */
	private String createDeviceURL(String deviceID) {
		Properties properties = new Properties();
		FileInputStream in=null;
		String deviceURL=null;
		try {
			in = new FileInputStream(_devProperties.getAppDir()+"config/device.properties");
			properties.load(in);
			JDFJMF.setTheSenderID(properties.getProperty("SenderID"));
			deviceURL= properties.getProperty("DeviceURL");
			if (deviceID!=null && deviceID.length()>0)
				deviceURL += "/"+deviceID;
			in.close();
		} catch (IOException e) {
			log.error("failed to load properties: \r\n"+e.getMessage());
			return null;
		}
		return deviceURL;
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.IDevice#getDeviceURL()
	 */
	public String getDeviceURL() {
		return _devProperties.getDeviceURL();
	}
	
	public void setDeviceURL(String theURL) {
		_devProperties.setDeviceURL(theURL);
	}
	
	/**
	 * get the URL of the proxy device for this device
	 * @return
	 */
	public String getProxyURL() {
		return _devProperties.getProxyURL();
	}
	
	/**
	 * stop the signal dispatcher and device processor
	 */
	public void shutdown() {
		_theSignalDispatcher.shutdown();
		_theDeviceProcessor.shutdown();
	}

	/**
	 * @param request
	 * @param response
	 */
	private void processError(HttpServletRequest request, HttpServletResponse response, EnumType messageType, int returnCode, String notification)
	{
		log.warn("processError- rc: "+returnCode+" "+notification==null ? "" : notification);
		JDFJMF error=JDFJMF.createJMF(EnumFamily.Response, messageType);
		JDFResponse r=error.getResponse(0);
		r.setReturnCode(returnCode);
		r.setErrorText(notification);
		response.setContentType(MimeUtil.VND_JMF);
		try
		{
			error.getOwnerDocument_KElement().write2Stream(response.getOutputStream(), 0, true);
		}
		catch (IOException x)
		{
			log.error("processError: cannot write response\n"+x.getMessage());
		}
	
	}

	/**
	 * http hotfolder processor
	 * 
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	private void processJDFRequest(HttpServletRequest request, HttpServletResponse response, InputStream inStream) throws IOException
	{
		log.info("processJDFRequest");
		JDFParser p=new JDFParser();
		if(inStream==null)
			inStream=request.getInputStream();
		JDFDoc doc=p.parseStream(inStream);
		if(doc==null)
		{
			processError(request, response, null, 3, "Error Parsing JDF");
		}
		else
		{
			JDFJMF jmf=JDFJMF.createJMF(EnumFamily.Command, EnumType.SubmitQueueEntry);
			final JDFCommand command = jmf.getCommand(0);
			// create a simple dummy sqe and submit to myself
			JDFQueueSubmissionParams qsp=command.getCreateQueueSubmissionParams(0);
			qsp.setPriority(50);
			JDFResponse r=_theQueueProcessor.addEntry(command, doc, false);
			if (r == null)
				log.warn("_theQueue.addEntry returned null");
		}
	}

	/**
	 * @param request
	 * @param response
	 * @param jmfDoc
	 * @throws IOException
	 */
	private void processJMFDoc(HttpServletRequest request,
			HttpServletResponse response, JDFDoc jmfDoc) {
		if(jmfDoc==null)
		{
			processError(request, response, null, 3, "Error Parsing JMF");
		}
		else
		{
			// switch: sends the jmfDoc to correct device
			JDFDoc responseJMF = null;
			if (_jmfHandler != null) {
				responseJMF=_jmfHandler.processJMF(jmfDoc);
			} 
			
			if(responseJMF!=null)
			{
				response.setContentType(MimeUtil.VND_JMF);
				try {
					responseJMF.write2Stream(response.getOutputStream(), 0, true);
				} catch (IOException e) {
					log.error("cannot write to stream: ",e);
				}
			}
			else
			{
				processError(request, response, null, 3, "Error Parsing JMF");               
			}
		}
	}

	/**
	 * @param request
	 * @param response
	 */
	private void processJMFRequest(HttpServletRequest request, HttpServletResponse response,InputStream inStream) throws IOException
	{
		log.debug("processJMFRequest");
		JDFParser p=new JDFParser();
		if(inStream==null)
			inStream=request.getInputStream();
		JDFDoc jmfDoc=p.parseStream(inStream);
		processJMFDoc(request, response, jmfDoc);
	}

	/**
	 * Parses a multipart request.
	 */
	private void processMultipartRequest(HttpServletRequest request, HttpServletResponse response)
	throws IOException
	{
		InputStream inStream=request.getInputStream();
		BodyPart bp[]=MimeUtil.extractMultipartMime(inStream);
		log.info("Body Parts: "+((bp==null) ? 0 : bp.length));
		if(bp==null || bp.length==0)
		{
			processError(request,response,null,9,"No body parts in mime package");
			return;
		}
		try // messaging exceptions
		{
			if(bp.length>1)
			{
				processMultipleDocuments(request,response,bp);
			}
			else
			{
				String s=bp[0].getContentType();
				if(MimeUtil.VND_JDF.equalsIgnoreCase(s))
				{
					processJDFRequest(request, response, bp[0].getInputStream());            
				}
				if(MimeUtil.VND_JMF.equalsIgnoreCase(s))
				{
					processJMFRequest(request, response, bp[0].getInputStream());            
				}
			}
		}
		catch (MessagingException x)
		{
			processError(request, response, null, 9, "Messaging exception\n"+x.getLocalizedMessage());
		}
	
	}

	/**
	 * process a multipart request - including job submission
	 * @param request
	 * @param response
	 */
	private void processMultipleDocuments(HttpServletRequest request, HttpServletResponse response,BodyPart[] bp)
	{
		log.info("processMultipleDocuments- parts: "+(bp==null ? 0 : bp.length));
		if(bp==null || bp.length<2)
		{
			processError(request, response, EnumType.Notification, 2,"processMultipleDocuments- not enough parts, bailing out");
			return;
		}
		JDFDoc docJDF[]=MimeUtil.getJMFSubmission(bp[0].getParent());
		if(docJDF==null)
		{
			processError(request, response, EnumType.Notification, 2,"proccessMultipleDocuments- incorrect jmf/jdf parts, bailing out!");
			return;
		}
		processJMFDoc(request, response, docJDF[0]);
	}
	
	/** Handles the HTTP <code>POST</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{
		log.debug("Processing post request for: "+request.getPathInfo());
		String contentType=request.getContentType();
		if(MimeUtil.VND_JMF.equals(contentType))
		{
			processJMFRequest(request,response,null);
		}
		else if(MimeUtil.VND_JDF.equals(contentType))
		{
			processJDFRequest(request,response,null);
		}
		else 
		{
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart)
			{
				log.info("Processing multipart request..."+contentType);
				processMultipartRequest(request, response);
			}
			else
			{
				log.warn("Unknown ContentType:"+contentType);
				response.setContentType("text/plain");
				OutputStream os=response.getOutputStream();
				InputStream is=request.getInputStream();
				byte[] b=new byte[1000];
				while(true)
				{
					int l=is.read(b);
					if(l<=0)
						break;
					os.write(b,0,l);
				}
			}
		}
	}
	
	/** Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		log.info("Processing get request...");
		String command = request.getParameter("cmd");

		if ( command==null || command.length()==0 || command.equals("showDevice") || 
				command.equals("processNextPhase") || command.equals("finalizeCurrentQE") )
		{
			request.setAttribute("device", this);
			try {
				request.getRequestDispatcher("DeviceInfo").forward(request, response);
			} catch (Exception e) {
				log.error(e);
			}
		} else if ( command.endsWith("QueueEntry") ) {
			request.setAttribute("device", this);
			try {
				request.getRequestDispatcher("QueueEntry").forward(request, response);
			} catch (Exception e) {
				log.error(e);
			}
		} else if ( command.equals("showQueue") ) {	
			QueueFacade bqu = new QueueFacade( _theQueueProcessor.getQueue() );
			String quStr = bqu.toHTML();
			PrintWriter out=null;
			try {
				out = response.getWriter();
				out.println(quStr);
				out.flush();
				out.close();
			} catch (IOException e) {
				showErrorPage("failed to show queue", e.getMessage(), request, response);
				log.error("failed to show Queue: "+e.getMessage());
			}
		}
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
	 * get the directory of the web application this device belongs to
	 * @return the path of the app dir on the filesystem
	 */
	public String getAppDir() {
		return _devProperties.getAppDir();
	}
	/**
	 * build a new QueueProcessor
	 * @return
	 */
	protected abstract IQueueProcessor buildQueueProcessor();
	
	/**
	 * build a new DeviceProcessor
	 * @return
	 */
	protected abstract AbstractDeviceProcessor buildDeviceProcessor();
}