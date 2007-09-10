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

package org.cip4.bambi.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.AbstractDevice;
import org.cip4.bambi.CustomDevice;
import org.cip4.bambi.SimDevice;
import org.cip4.bambi.IJMFHandler;
import org.cip4.bambi.IMessageHandler;
import org.cip4.bambi.IQueueProcessor;
import org.cip4.bambi.ISignalDispatcher;
import org.cip4.bambi.IStatusListener;
import org.cip4.bambi.JMFHandler;
import org.cip4.bambi.QueueProcessor;
import org.cip4.bambi.SignalDispatcher;
import org.cip4.bambi.StatusListener;
import org.cip4.bambi.QueueFacade;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.resource.JDFDeviceList;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StringUtil;


/**
 * This is Bambi main entrance point and "root device". <br>
 * It does not process QueueEntries, it just collects them. Processing is done by
 * the worker devices defined in <code>/WebContent/config/devices.xml</code>.
 * @author  rainer
 *
 *
 * @web:servlet-init-param	name="" 
 *									value=""
 *									description=""
 *
 * @web:servlet-mapping url-pattern="/BambiRootDevice"
 */
public class DeviceServlet extends AbstractBambiServlet 
{
	/**
	 * 
	 * handler for the knowndevices query
	 */
	protected class KnownDevicesHandler implements IMessageHandler
	{
	
		/* (non-Javadoc)
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		public boolean handleMessage(JDFMessage m, JDFResponse resp)
		{
			if(m==null || resp==null)
			{
				return false;
			}
			log.debug("Handling"+m.getType());
			EnumType typ=m.getEnumType();
			if(EnumType.KnownDevices.equals(typ))
			{
				JDFDeviceList dl = resp.appendDeviceList();
				Set keys = _devices.keySet();
				Object[] strKeys = keys.toArray();
				for (int i=0; i<keys.size();i++)
				{
					String key = (String)strKeys[i];
					AbstractDevice dev = getDeviceFromObject(_devices.get(key));
					if (dev == null)
						log.error("device with key '"+key+"'not found");
					else
						dev.appendDeviceInfo(dl);
				}
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
	private static final long serialVersionUID = -8902151736245089036L;
	private static Log log = LogFactory.getLog(DeviceServlet.class.getName());
	public static final String baseDir=System.getProperty("catalina.base")+"/webapps/Bambi/jmb"+File.separator;
	public static String configDir=System.getProperty("catalina.base")+"/webapps/Bambi/config"+File.separator;
	public static final String xslDir="./xslt/";
	public static String jdfDir=baseDir+"JDFDir"+File.separator;
	private JMFHandler _jmfHandler=null;
	private static HashMap _devices = null;
	private ISignalDispatcher _theSignalDispatcher=null;
	private IQueueProcessor _theQueueProcessor=null;
	private IStatusListener _theStatusListener=null;
	public static final String bambiRootDeviceID = "BambiRootDevice";


	/** Initializes the servlet.
	 */
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);
		
		/*** uncomment to set the log level to debug: ***/
		//System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		//System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");

		new File(baseDir).mkdirs();
		_devices = new HashMap();
		_jmfHandler=new JMFHandler();
		addHandlers();
		
		_theSignalDispatcher=new SignalDispatcher(_jmfHandler);
		_theSignalDispatcher.addHandlers(_jmfHandler);

        _theStatusListener=new StatusListener(_theSignalDispatcher,bambiRootDeviceID);
        _theStatusListener.addHandlers(_jmfHandler);
		
        _theQueueProcessor = new QueueProcessor(bambiRootDeviceID);
        _theQueueProcessor.addHandlers(_jmfHandler);
        
		log.info("Initializing DeviceServlet");
		loadBambiProperties();
		createDevicesFromFile(configDir+"devices.xml");
		
		
	}

	/** Destroys the servlet.
	 */
	public void destroy() {
//		foo		
	}

	/** Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		log.info("Processing get request...");
		String command = request.getParameter("cmd");
		
		if (command == null || command.length() == 0) {
			request.setAttribute("devices", getDevices());
			try {
				request.getRequestDispatcher("/overview.jsp").forward(request, response);
			} catch (Exception e) {
				log.error(e);
			} 
		} else if ( command.equals("showDevice") || 
				command.equals("processNextPhase") || command.equals("finalizeCurrentQE") )
		{
			AbstractDevice dev=getDeviceFromRequest(request);
			if (dev!=null)
			{
				request.setAttribute("device", dev);
				try {
					request.getRequestDispatcher("DeviceInfo").forward(request, response);
				} catch (Exception e) {
					log.error(e);
				}
			} else {
				showErrorPage("can't get device", "device ID missing or unknown", request, response);
				return;
			}
		} else if ( command.endsWith("QueueEntry") ) 
		{
			AbstractDevice dev=getDeviceFromRequest(request);
			if (dev!=null)
			{
				request.setAttribute("device", dev);
				try {
					request.getRequestDispatcher("QueueEntry").forward(request, response);
				} catch (Exception e) {
					log.error(e);
				}
			} else {
				log.error("can't get device, device ID is missing or unknown");
			}
		} else if ( command.equals("showQueue") ) 
		{	
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
	 * @param request
	 */
	private AbstractDevice getDeviceFromRequest(HttpServletRequest request) {
		String deviceID = request.getParameter("id");
		if (deviceID == null)
		{
			log.error("invalid request: device ID is missing");
			return null;
		}
		AbstractDevice dev = getDevice(deviceID);
		if (dev == null)
		{
			log.error("invalid request: device with id="+deviceID+" not found");
			return null;
		}
		
		return dev;
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
			IJMFHandler handler = getTargetHandler(request);
			if (handler != null) {
				responseJMF=handler.processJMF(jmfDoc);
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

	private IJMFHandler getTargetHandler(HttpServletRequest request) {
		String deviceID = request.getPathInfo();
		if (deviceID == null)
			return _jmfHandler; // root folder
		deviceID = StringUtil.token(deviceID, 0, "/");
		if (deviceID == null)
			return _jmfHandler; // device not found
		AbstractDevice device = getDeviceFromObject( _devices.get(deviceID) );
		if (device == null)
			return _jmfHandler; // device not found
		return( device.getHandler() );
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
			JDFResponse r=_theQueueProcessor.addEntry(command, doc);
			if (r == null)
				log.warn("_theQueue.addEntry returned null");
		}
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
				proccessMultipleDocuments(request,response,bp);
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
	private void proccessMultipleDocuments(HttpServletRequest request, HttpServletResponse response,BodyPart[] bp)
	{
		log.info("proccessMultipleDocuments- parts: "+(bp==null ? 0 : bp.length));
		if(bp==null || bp.length<2)
		{
			processError(request, response, EnumType.Notification, 2,"proccessMultipleDocuments- not enough parts, bailing out:"+bp.length);
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
	 * Returns a short description of the servlet.
	 */
	public String getServletInfo() 
	{
		return "Bambi Device  Servlet";
	}

	/**
	 * create a new device and add it to the map of devices.
	 * @param deviceID
	 * @param deviceType
	 * @return the Device, if device has been created. 
	 * null, if not (maybe device with deviceID is already present)
	 */
	public AbstractDevice createDevice(String deviceID, String deviceType, String deviceClass)
	{
		log.debug("created device");
		if (_devices == null)
		{
			log.warn("map of devices is null, re-initialising map...");
			_devices = new HashMap();
		}
		
		if (_devices.get(deviceID) == null)
		{	
			AbstractDevice dev;
			if (deviceClass.equals("org.cip4.bambi.SimDevice"))
			{
				dev = new SimDevice(deviceType, deviceID, deviceClass);
			} else if (deviceClass.equals("org.cip4.bambi.CustomDevice"))
			{
				dev = new CustomDevice(deviceType, deviceID, deviceClass);
			} else
			{
				log.fatal("unknown device class: "+deviceClass);
				return null;
			}
			_devices.put(deviceID,dev);
			return dev;
		}
		else
		{
			log.warn("device "+deviceID+" already existing");
			return null;
		}
	}
	
	/**
	 * remove device
	 * @param deviceID ID of the device to be removed
	 * @return
	 */
	public boolean removeDevice(String deviceID)
	{
		if (_devices == null)
		{
			log.error("list of devices is null");
			return false;
		}
		if (_devices.get(deviceID) != null)
		{	
			_devices.remove(deviceID);
			return true;
		}
		else
		{
			log.debug("tried to removing non-existing device");
			return false;
		}
	}

	public int getDeviceQuantity()
	{
		if (_devices == null)
			return 0;
		else
			return _devices.size();
	}

	public static AbstractDevice getDevice(String deviceID)
	{
		if (_devices == null)
		{
			log.debug("list of devices is null");
			return null;
		}

		return (AbstractDevice)_devices.get(deviceID);
	}

	private boolean createDevicesFromFile(String fileName)
	{
		JDFParser p = new JDFParser();
	    JDFDoc doc = p.parseFile(fileName);
	    if (doc == null)
	    {
	    	log.error( fileName+" not found, no devices created" );
	    	return false;
	    }
	    
	    KElement e = doc.getRoot();
	    VElement v = e.getXPathElementVector("//devices/*", 99);
	    for (int i = 0; i < v.size(); i++)
	    {
	    	KElement device = (KElement)v.elementAt(i);
	    	String deviceID = device.getXPathAttribute("@DeviceID", null);
	    	String deviceType = device.getXPathAttribute("@DeviceType", null);
	    	String deviceClass = device.getXPathAttribute("@DeviceClass", "org.cip.bambi.DeviceServlet");
	    	if (deviceID != null)
            {
	    		AbstractDevice dev=createDevice(deviceID,deviceType,deviceClass);
                IQueueProcessor qp=dev.getQueueProcessor();
                qp.setFallBackQProcessor(_theQueueProcessor);
              
            }
	    	else
            {
	    		log.warn("cannot create device without device ID");
            }
	    }
		
		return true;
	}
	
	private boolean loadBambiProperties()
	{
		log.debug("loading Bambi properties");
		try 
		{
			Properties properties = new Properties();
			FileInputStream in = new FileInputStream(configDir+"Bambi.properties");
			properties.load(in);
			JDFJMF.setTheSenderID(properties.getProperty("SenderID"));
			in.close();
		} catch (FileNotFoundException e) {
			log.fatal("Bambi.properties not found");
			return false;
		} catch (IOException e) {
			log.fatal("Error while applying Bambi.properties");
			return false;
		}
		return true;
	}
	
	private void addHandlers()
	{
		_jmfHandler.addHandler( new DeviceServlet.KnownDevicesHandler() );
	}
	
	public HashMap getDevices()
	{
		return _devices;
	}
}
