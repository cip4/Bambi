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

package org.cip4.bambi.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractBambiServlet;
import org.cip4.bambi.core.IDevice;
import org.cip4.bambi.core.ISignalDispatcher;
import org.cip4.bambi.core.IStatusListener;
import org.cip4.bambi.core.SignalDispatcher;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.queues.IQueueProcessor;
import org.cip4.bambi.core.queues.QueueFacade;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.resource.JDFDevice;
import org.cip4.jdflib.resource.JDFDeviceList;
import org.cip4.jdflib.util.MimeUtil;


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
public class ProxyServlet extends AbstractBambiServlet implements IDevice 
{
	private static final long serialVersionUID = -8902151736245089036L;
	private static Log log = LogFactory.getLog(ProxyServlet.class.getName());
	private ISignalDispatcher _theSignalDispatcher=null;
	private IQueueProcessor _theQueueProcessor=null;
	private IStatusListener _theStatusListener=null;
	
	
	/** Initializes the servlet.
	 */
	@Override
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);
		ServletContext context = config.getServletContext();
		log.info( "Initializing servlet for "+context.getServletContextName() );
		
		_theSignalDispatcher=new SignalDispatcher(_jmfHandler, _deviceID);
		_theSignalDispatcher.addHandlers(_jmfHandler);

        _theStatusListener=new StatusListener(_theSignalDispatcher,_deviceID);
        _theStatusListener.addHandlers(_jmfHandler);
		
        _theQueueProcessor = new ProxyQueueProcessor(_deviceID, _appDir);
        _theQueueProcessor.addHandlers(_jmfHandler);
	}

	/** Destroys the servlet.
	 */
	@Override
	public void destroy() {
		_theSignalDispatcher.shutdown();
	}


	/** Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		log.info("Processing get request...");
		String command = request.getParameter("cmd");
		
		if ( command==null||command.length()==0 )  {
			QueueFacade qf = new QueueFacade( _theQueueProcessor.getQueue() );
			request.setAttribute("qf", qf);
			try {
				request.getRequestDispatcher("/overview.jsp").forward(request, response);
			} catch (Exception e) {
				log.error(e);
			} 
// TODO allow proxy to send AbortQE to workers via web interface?		
//		} else if ( command.endsWith("QueueEntry") ) 
//		{
//			IDevice dev=getDeviceFromRequest(request);
//			if (dev!=null)
//			{
//				request.setAttribute("device", dev);
//				try {
//					request.getRequestDispatcher("QueueEntry").forward(request, response);
//				} catch (Exception e) {
//					log.error(e);
//				}
//			} else {
//				log.error("can't get device, device ID is missing or unknown");
//			}
		} else if ( command.equals("showQueue") )  {	
			QueueFacade bqu = new QueueFacade( _theQueueProcessor.getQueue() );
			String quStr = bqu.toHTML();
			writeRawResponse(request, response, quStr);
		} else if ( command.equals("showJDFDoc") ) {
			String qeid=request.getParameter("qeid");
			if ( (qeid!=null&&qeid.length()>0) ) {
				String filePath=_jdfDir+qeid+".jdf";
				JDFDoc theDoc=JDFDoc.parseFile(filePath);
				if (theDoc!=null) {
					writeRawResponse( request,response,theDoc.toXML() );
				} else {
					log.error( "cannot parse '"+filePath+"'" );
					return;
				}
			}
		}
	}

	private void writeRawResponse(HttpServletRequest request,
			HttpServletResponse response, String theStr) {
		PrintWriter out=null;
		try {
			out = response.getWriter();
			out.println(theStr);
		    out.flush();
		    out.close();
		} catch (IOException e) {
			showErrorPage("failed to response", e.getMessage(), request, response);
			log.error("failed to write response: "+e.getMessage());
		}
	}

	@Override
	protected void processJMFDoc(HttpServletRequest request,
			HttpServletResponse response, JDFDoc jmfDoc) {
		if(jmfDoc==null) {
			processError(request, response, null, 3, "Error Parsing JMF");
		} else {
			// switch: sends the jmfDoc to correct device
			JDFDoc responseJMF = null;
			IJMFHandler handler = _jmfHandler;
			if (handler != null) {
				responseJMF=handler.processJMF(jmfDoc);
			} 
			
			if (responseJMF!=null) {
				response.setContentType(MimeUtil.VND_JMF);
				try {
					responseJMF.write2Stream(response.getOutputStream(), 0, true);
				} catch (IOException e) {
					log.error("cannot write to stream: ",e);
				}
			} else {
				processError(request, response, null, 3, "Error Parsing JMF");               
			}
		}
	}

	/** 
	 * Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo() {
		return "Bambi Proxy Servlet";
	}
	
	@Override
	public String toString() {
		String ret ="[ BambiProxy - DeviceID="+_deviceID+", DeviceURL="+_deviceURL+", AppDir="
			+_appDir+" ]";
		return ret;
	}
	
	@Override
	protected boolean handleKnownDevices(JDFMessage m, JDFResponse resp) {
		if(m==null || resp==null)
		{
			return false;
		}
//		log.info("Handling "+m.getType());
		EnumType typ=m.getEnumType();
		if(EnumType.KnownDevices.equals(typ)) {
			// I am the known device
			JDFDeviceList dl = resp.appendDeviceList();
			JDFDeviceInfo info = dl.appendDeviceInfo();
			JDFDevice dev = info.appendDevice();
			dev.setDeviceID( getDeviceID() );
			dev.setDeviceType( getDeviceType() );
			dev.setJDFVersions( EnumVersion.Version_1_3.getName() );
			return true;
		}

		return false;
	}
	
	@Override
	protected void processJDFRequest(HttpServletRequest request, HttpServletResponse response, InputStream inStream) throws IOException
	{
		log.info("processJDFRequest");
		JDFParser p=new JDFParser();
		if(inStream==null) {
			inStream=request.getInputStream();
		}
		JDFDoc doc=p.parseStream(inStream);
		if(doc==null) {
			processError(request, response, null, 3, "Error Parsing JDF");
		} else {
			JDFJMF jmf=JDFJMF.createJMF(EnumFamily.Command, EnumType.SubmitQueueEntry);
			final JDFCommand command = jmf.getCommand(0);
			// create a simple dummy sqe and submit to myself
			JDFQueueSubmissionParams qsp=command.getCreateQueueSubmissionParams(0);
			qsp.setPriority(50);
			JDFResponse r=_theQueueProcessor.addEntry(command, doc, false);
			if (r == null) {
				log.warn("_theQueue.addEntry returned null");
			}
		}
	}
}
