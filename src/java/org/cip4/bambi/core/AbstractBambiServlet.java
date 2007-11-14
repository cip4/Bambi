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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.MultiDeviceProperties.DeviceProperties;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.util.MimeUtil;

/**
 * Entrance point for 
 * @see AbstractWorkerServlet
 * @see ProxyServlet
 * @author boegerni
 *
 */
public abstract class AbstractBambiServlet extends HttpServlet {
	
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

	protected IDeviceProperties _devProperties=null;
	protected JMFHandler _jmfHandler=null;
	protected HashMap<String,IDevice> _devices = null;
	private static Log log = LogFactory.getLog(AbstractBambiServlet.class.getName());
	
	/** Initializes the servlet.
	 * @throws MalformedURLException 
	 */
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		ServletContext context = config.getServletContext();
		log.info( "Initializing servlet for "+context.getServletContextName() );
		String appDir=context.getRealPath("")+"/";
		_devProperties=loadProperties(appDir, appDir+"config/application.properties");
		
		List<File> dirs=new ArrayList<File>();
		final String baseDir = _devProperties.getBaseDir();
        if(baseDir!=null)
            dirs.add( new File(baseDir) );
		final String jdfDir = _devProperties.getJDFDir();
        if(jdfDir!=null)
            dirs.add( new File(jdfDir) );
		createDirs(dirs);
		
		_jmfHandler=new JMFHandler();
		addHandlers();
	}

	/**
	 * create the specified directories, if the do not exist
	 * @param dirs the directories to create
	 */
	protected void createDirs(List<File> dirs) {
		for (int i=0;i<dirs.size();i++) {
			File dir=dirs.get(i);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
	}
	
	protected abstract boolean handleKnownDevices(JDFMessage m, JDFResponse resp);

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
	protected void processError(HttpServletRequest request, HttpServletResponse response, EnumType messageType, int returnCode, String notification)
	{
		log.warn("processError- rc: "+returnCode+" "+notification==null ? "" : notification);
		JDFJMF error=JDFJMF.createJMF(EnumFamily.Response, messageType);
		JDFResponse r=error.getResponse(0);
		r.setReturnCode(returnCode);
		r.setErrorText(notification);
		response.setContentType(MimeUtil.VND_JMF);
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
	protected void processMultipleDocuments(HttpServletRequest request, HttpServletResponse response,BodyPart[] bp)
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
		processJMFDoc(request, response, docJDF[0]);
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param jmfDoc
	 */
	protected abstract void processJMFDoc(HttpServletRequest request,
			HttpServletResponse response, JDFDoc jmfDoc);
	
	/**
	 * Parses a multipart request.
	 */
	protected void processMultipartRequest(HttpServletRequest request, HttpServletResponse response)
	throws IOException {
		InputStream inStream=request.getInputStream();
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
		String contentType=request.getContentType();
		if(MimeUtil.VND_JMF.equals(contentType)) {
			processJMFRequest(request,response,null);
		} else {
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart) {
				log.info("Processing multipart request... (ContentType: "+contentType+")");
				processMultipartRequest(request, response);
			} else {
				log.warn("Unknown ContentType: "+contentType);
				response.setContentType("text/plain");
				OutputStream os=response.getOutputStream();
				InputStream is=request.getInputStream();
				byte[] b=new byte[1000];
				while(true) {
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
	 * loads properties
	 * @param appDir   the directory of the web application
	 * @param fileName the name of the Java .propert file
	 * @return true, if the properties have been loaded successfully
	 */
	protected IDeviceProperties loadProperties(String appDir, String fileName)
	{
		IDeviceProperties prop=new DeviceProperties();
		log.info("loading properties from "+fileName);
		try {
			Properties properties = new Properties();
			FileInputStream in = new FileInputStream(fileName);
			properties.load(in);
			JDFJMF.setTheSenderID(properties.getProperty("SenderID"));
			String property=properties.getProperty("BaseDir");
			if (property!=null && property.startsWith("./")) {
				property=property.substring(2);
				property=appDir+property;
			}
			prop.setBaseDir(property);
			
			property=properties.getProperty("ConfigDir");
			if (property!=null && property.startsWith("./")) {
				property=property.substring(2);
				property=appDir+property;
			}
			prop.setConfigDir(property);
			
			property=properties.getProperty("JDFDir");
			if (property!=null && property.startsWith("./")) {
				property=property.substring(2);
				property=appDir+property;
			}
			prop.setJDFDir(property);
			
			in.close();
		} catch (FileNotFoundException e) {
			log.fatal(fileName+" not found");
			return null;
		} catch (IOException e) {
			log.fatal("Error while applying properties from "+fileName);
			return null;
		}
		return prop;
	}
	
	protected void addHandlers() {
		_jmfHandler.addHandler( new AbstractBambiServlet.KnownDevicesHandler() );
	}
	
	public String getDeviceID() {
		return _devProperties.getDeviceID();
	}

	public String getDeviceURL() {
		return _devProperties.getDeviceURL();
	}
	
	public String getDeviceType() {
        return _devProperties.getDeviceType();
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

}
