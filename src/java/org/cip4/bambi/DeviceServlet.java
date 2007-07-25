
package org.cip4.bambi;

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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.util.MimeUtil;


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
    private static Log log = LogFactory.getLog(DeviceServlet.class.getName());
    public static final String baseDir=System.getProperty("catalina.base")+"/webapps/Bambi/"+"jmb"+File.separator;


    /**
     * 
     */
    private static final long serialVersionUID = -8902151736245089036L;
    private JMFHandler jmfHandler=null;
    private IQueueProcessor theQueue=null;
    private IDeviceProcessor theDevice=null;
    private IStatusListener theStatusListener=null;
    private ISignalDispatcher theSignalDispatcher=null;

    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException 
    {
        super.init(config);
        new File(baseDir).mkdirs();
        // TODO make configurable
        jmfHandler=new JMFHandler();
        JDFJMF.setTheSenderID("bambi");
        
        SignalDispatcher tmpDisp=new SignalDispatcher(jmfHandler);
        theSignalDispatcher=tmpDisp;
        tmpDisp.addHandlers(jmfHandler);
        
        log.info("Initializing DeviceServlet");
        theQueue=new QueueProcessor(theStatusListener, theSignalDispatcher);
        StatusListener statusListener=new StatusListener(theSignalDispatcher);
        theStatusListener=statusListener;
        statusListener.addHandlers(jmfHandler);
         
        theDevice=new DeviceProcessor(theQueue, theStatusListener);
        log.info("Starting device thread");
        new Thread(theDevice).start();
        log.info("device thread started");
    }

    /** Destroys the servlet.
     */
    public void destroy() {
//      foo		
    }

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
           boolean isMultipart = FileUploadBase.isMultipartContent(request);
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
        System.out.println("processJMFRequest");
        JDFParser p=new JDFParser();
        if(inStream==null)
            inStream=request.getInputStream();
        JDFDoc jmfDoc=p.parseStream(inStream);
        if(jmfDoc==null)
        {
            processError(request, response, null, 3, "Error Parsing JMF");
        }
        else
        {
            JDFDoc responseJMF=jmfHandler.processJMF(jmfDoc);
            if(responseJMF!=null)
            {
                responseJMF.write2Stream(response.getOutputStream(), 0, true);
            }
            else
            {
                processError(request, response, null, 3, "Error Parsing JMF");               
            }
        }     
        response.setContentType(MimeUtil.VND_JMF);
    }
    /**
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
            // create a simple dummy sqe and subit to myself
            JDFQueueSubmissionParams qsp=command.getCreateQueueSubmissionParams(0);
            qsp.setPriority(50);
            JDFResponse r=theQueue.addEntry(command, doc);
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
        final JDFCommand command = docJDF[0].getJMFRoot().getCommand(0);
        JDFResponse r=theQueue.addEntry(command, docJDF[1]);
        if(r==null)
        {
            processError(request, response, EnumType.Notification, 2,"proccessMultipleDocuments- queue rejected submission");
            return;
        }
        
        try
        {
            r.getOwnerDocument_KElement().write2Stream(response.getOutputStream(),2,true);
        }
        catch (IOException x)
        {
            processError(request, response, EnumType.Notification, 2,"proccessMultipleDocuments- error writing\n"+x.getMessage());
        }
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

}
