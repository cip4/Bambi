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

package org.cip4.bambi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.XMLDoc;
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
 *
 * @author  rainer
 *
 *
 * @web:servlet-init-param	name="" 
 *									value=""
 *									description=""
 *
 * @web:servlet-mapping url-pattern="/FixJDFServlet"
 */
public class DeviceServlet extends HttpServlet 
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
		public boolean handleMessage(JDFMessage m, JDFResponse resp, String queueEntryID, String workstepID)
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
					((Device)_devices.get(key)).appendDeviceInfo(dl);
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
    private static Log log = LogFactory.getLog(DeviceServlet.class.getName());
	public static final String baseDir=System.getProperty("catalina.base")+"/webapps/Bambi/jmb"+File.separator;
	public static final String configDir=System.getProperty("catalina.base")+"/webapps/Bambi/config"+File.separator;
	public static final String jdfDir=baseDir+"JDFDir"+File.separator;
	


	/**
	 * 
	 */
	private static final long serialVersionUID = -8902151736245089036L;
	private JMFHandler _jmfHandler=null;
	private HashMap _devices = null;
	private ISignalDispatcher _theSignalDispatcher=null;
	private IQueueProcessor _theQueueProcessor=null;
	private IStatusListener _theStatusListener=null;
	public static String bambiRootDeviceID = "BambiRootDevice";


	/** Initializes the servlet.
	 */
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);
		new File(baseDir).mkdirs();
		_devices = new HashMap();
		// TODO make configurable
		_jmfHandler=new JMFHandler();
		addHandlers();
		
		_theSignalDispatcher=new SignalDispatcher(_jmfHandler);
		_theSignalDispatcher.addHandlers(_jmfHandler);

        _theStatusListener=new StatusListener(_theSignalDispatcher,bambiRootDeviceID);
        _theStatusListener.addHandlers(_jmfHandler);
		
        _theQueueProcessor = new QueueProcessor(_theSignalDispatcher,bambiRootDeviceID);
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

    //TODO device liste, queues,  und stati über get seiten darstellen
    // technologie nach gusto - xslt des jdf, jsp, xmldoc to html, ... whatever
	/** Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		log.debug("Processing get request...");
		XMLDoc d=new XMLDoc("html",null);
		KElement root=d.getRoot();
		root.appendElement("head").appendElement("title").appendText("DeviceServlet generic page");
		root.appendElement("h1").setText("Unknown URL:"+request.getPathInfo());
		response.setContentType("text/html;charset=utf-8");
		try
		{
			response.getOutputStream().print(root.toString());
		}
		catch (IOException x)
		{
			log.error(x);
		}

	}

	/** Handles the HTTP <code>POST</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{
		log.info("Processing post request for: "+request.getPathInfo());
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
                //TODO device manipulation über post von html seiten
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
		Device device = (Device)_devices.get(deviceID);
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
			processError(request, response, EnumType.Notification, 2,"proccessMultipleDocuments- not enough parts, bailing out ");
			return;
		}
		JDFDoc docJDF[]=MimeUtil.getJMFSubmission(bp[0].getParent());
		if(docJDF==null)
		{
			processError(request, response, EnumType.Notification, 2,"proccessMultipleDocuments- not enough parts, bailing out ");
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
	 * @return true, if device has been created. 
	 * False, if not (maybe device with deviceID is already present)
	 */
	public boolean createDevice(String deviceID, String deviceType, String deviceClass)
	{
		log.debug("created device");
		if (_devices == null)
		{
			log.warn("map of devices is null, re-initialising map...");
			_devices = new HashMap();
		}
		
		if (_devices.get(deviceID) == null)
		{	
			Device dev = new Device(deviceType, deviceID, deviceClass);
			_devices.put(deviceID,dev);
			return true;
		}
		else
		{
			log.warn("device "+deviceID+" already existing");
			return false;
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

	public Device getDevice(String deviceID)
	{
		if (_devices == null)
		{
			log.debug("list of devices is null");
			return null;
		}

		return (Device)_devices.get(deviceID);
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
	    	String deviceID = device.getXPathAttribute("@DeviceID", "");
	    	String deviceType = device.getXPathAttribute("@DeviceType", "");
	    	String deviceClass = device.getXPathAttribute("@DeviceClass", "org.cip.bambi.DeviceServlet");
	    	if (deviceID != "")
	    		createDevice(deviceID,deviceType,deviceClass);
	    	else
	    		log.warn("cannot create device without device ID");
	    }
		
		return true;
	}
	
	private boolean loadBambiProperties()
	{
		log.debug("loading Bambi properties");
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(configDir+"Bambi.properties"));
			JDFJMF.setTheSenderID(properties.getProperty("SenderID"));
			
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
}
