/**
 * 
 */
package org.cip4.bambi.proxy;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.mail.Multipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;

/**
 * @author prosirai
 *
 */
public abstract class AbstractProxyProcessor extends AbstractDeviceProcessor
{

    private static Log log = LogFactory.getLog(AbstractProxyProcessor.class);
    protected IConverterCallback slaveCallBack;
        /**
     * 
     */
    public AbstractProxyProcessor(AbstractProxyDevice parent)
    {
        super();
       _parent=parent;
       slaveCallBack=parent.getSlaveCallback();

    }
    

    protected JDFDoc writeToQueue(JDFDoc docJMF, JDFDoc docJDF, String strUrl, MIMEDetails urlDet, boolean expandMime ) throws IOException
    {
        if(strUrl==null)
        {
            log.error("writeToQueue: attempting to write to null url");
            return null;
        }
        Multipart mp=MimeUtil.buildMimePackage(docJMF, docJDF, expandMime);
        SubmitQueueEntryResponseHandler sqh=new SubmitQueueEntryResponseHandler();
        new JMFFactory(slaveCallBack).send2URL(mp, strUrl, sqh, urlDet,_parent.getDeviceID());
        sqh.waitHandled(30000);
        if(sqh.doc==null)
        {
            JDFCommand c=docJMF.getJMFRoot().getCommand(0);
            final JDFJMF respJMF = c.createResponse();
            JDFResponse r=respJMF.getResponse(0);
            HttpURLConnection connection = sqh.getConnection();
            if(connection==null)
            {
                r.setErrorText("Invalid http connection");
            }
            else
            {
            int responseCode = connection.getResponseCode();
            r.setErrorText("Invalid http response - RC="+responseCode);
            }
            r.setReturnCode(3); // TODO correct rcs
            return respJMF.getOwnerDocument_JDFElement();   
        }

        return sqh.doc;    
    }
    /**
     * @param qe
     * @param node
     * @param qeR
     */
    protected void submitted(JDFQueueEntry qe, JDFNode node,  String devQEID, EnumQueueEntryStatus newStatus, String slaveURL)
    {
        BambiNSExtension.setSlaveQueueEntryID(qe, devQEID);
        BambiNSExtension.setDeviceURL(qe, slaveURL);
        _queueProcessor.updateEntry(qe, newStatus ,null,null);
    }
    /**
     * @param hfURL
     * @param qe
     * @return
     */
    protected EnumQueueEntryStatus submitToHF(File fHF)
    {
        JDFQueueEntry qe=currentQE.getQueueEntry();
        JDFNode nod=currentQE.getJDF();
        if(nod==null)
        {
            // TODO abort!
            log.error("submitToQueue - no JDFDoc at: "+BambiNSExtension.getDocURL(qe));
            _queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted,null,null);
        }
        else
        {
            File fLoc=new File(((ProxyDevice)_parent).getNameFromQE(qe));
            final File fileInHF = FileUtil.getFileInDirectory(fHF, fLoc);
            boolean bWritten=nod.getOwnerDocument_JDFElement().write2File(fileInHF,0,true);
            if(bWritten)
            {
                submitted(qe, nod, "qe"+JDFElement.uniqueID(0), EnumQueueEntryStatus.Running, UrlUtil.fileToUrl(fileInHF, true));            
            }
            else
            {
                log.error("Could not write File: "+fLoc+" to "+fHF);
                _queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted,null,null);
            }
        }
        return qe.getQueueEntryStatus();     
    }

    /**
     * @param qurl
     * @param qe
     * @return
     * TODO mime or not mime...
     */
    protected EnumQueueEntryStatus submitToQueue(JDFNode nod, JDFQueueEntry qe, URL qurl, File deviceOutputHF, MIMEDetails ud, boolean expandMime)
    {
        JDFJMF jmf=JDFJMF.createJMF(JDFMessage.EnumFamily.Command,JDFMessage.EnumType.SubmitQueueEntry);
        JDFCommand com = (JDFCommand)jmf.getCreateMessageElement(JDFMessage.EnumFamily.Command, null, 0);
        JDFQueueSubmissionParams qsp = com.appendQueueSubmissionParams();

        qsp.setReturnJMF(_parent.getDeviceURL());
        if(deviceOutputHF!=null)
        {
            qsp.setReturnURL(deviceOutputHF.getPath());
        }

        qsp.setURL("dummy"); // replaced by mimeutil
        if(nod!=null)
        {
            try
            {
                final String urlString = qurl==null ? null :qurl.toExternalForm();

                JDFDoc d=writeToQueue(jmf.getOwnerDocument_JDFElement(), nod.getOwnerDocument_JDFElement(), urlString,ud, expandMime);
                if(d!=null)
                {
                    JDFJMF jmfResp=d.getJMFRoot();
                    if(jmfResp==null)
                    {
                        d=null;
                    }
                    else
                    {
                        JDFResponse r=jmfResp.getResponse(0);
                        if(r==null)
                        {
                            d=null;
                        }
                        else
                        {
                            if(r.getReturnCode()!=0 || !EnumType.SubmitQueueEntry.equals(r.getEnumType()))
                            {
                                log.error("Device returned rc="+r.getReturnCode());
                                _queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted,null,null);
                            }
                            else
                            {
                                JDFQueueEntry qeR=r.getQueueEntry(0);

                                if(qeR!=null)
                                {
                                    submitted(qe, nod, qeR.getQueueEntryID(),qeR.getQueueEntryStatus(),urlString);
                                }
                                else
                                {
                                    log.error("No QueueEntry in the submitqueuentry response");
                                }
                            }
                        }
                    }
                }
                if(d==null)
                {
                    log.error("submitToQueue - no response at: "+BambiNSExtension.getDocURL(qe));
                    _queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted,null,null);
                }
            }
            catch (IOException x)
            {
                nod=null;
            }
         }
        if(nod==null)
        {
            log.error("submitToQueue - no JDFDoc at: "+BambiNSExtension.getDocURL(qe));
            _queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted,null,null);
        }
        return qe.getQueueEntryStatus();
    }

}