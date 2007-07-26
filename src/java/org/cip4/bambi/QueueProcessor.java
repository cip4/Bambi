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
import java.net.MalformedURLException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.auto.JDFAutoQueue.EnumQueueStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.UrlUtil;


/**
 *
 * @author  rainer
 *
 *
 */
public class QueueProcessor implements IQueueProcessor
{

    private static Log log = LogFactory.getLog(QueueProcessor.class.getName());
    private File queueFile;
    private static final long serialVersionUID = -876551736245089033L;
    private JDFQueue myQueue;
    private Vector listeners;
    private IStatusListener statusListener;
    private ISignalDispatcher signalDispatcher;
    private static final String jdfDir=DeviceServlet.baseDir+"JDFDir"+File.separator;
    /**
     * 
     *
     */
    public QueueProcessor(IStatusListener _statusListener, ISignalDispatcher _signalDispatcher)
    {
        super();
        statusListener=_statusListener;
        signalDispatcher=_signalDispatcher;
        log.info("QueueProcessor construct");
        queueFile=new File(DeviceServlet.baseDir+File.separator+"theQueue.xml");
        queueFile.getParentFile().mkdirs();
        new File(jdfDir).mkdirs();
        JDFDoc d=JDFDoc.parseFile(queueFile.getAbsolutePath());
        if(d!=null)
        {
            log.info("refreshing queue");
            myQueue=(JDFQueue) d.getRoot();
        }
        else
        {
            d=new JDFDoc(ElementName.QUEUE);
            log.info("creating new queue");
            myQueue=(JDFQueue) d.getRoot();
            myQueue.setQueueStatus(EnumQueueStatus.Waiting);
        }
        myQueue.setAutomated(true);
        listeners=new Vector();
    }

    public IQueueEntry getNextEntry()
    {
        log.debug("getNextEntry");
        JDFQueueEntry qe=myQueue.getNextExecutableQueueEntry();
        if(qe==null)
            return null;
        String docURL=BambiNSExtension.getDocURL(qe);
        docURL=UrlUtil.urlToFile(docURL).getAbsolutePath();
        JDFDoc doc=JDFDoc.parseFile(docURL);
        return new QueueEntry(doc,qe);        
    }


    /* (non-Javadoc)
     * @see org.cip4.bambi.IQueueProcessor#addListener(java.lang.Object)
     */
    public void addListener(Object o)
    {
        log.info("adding new listener");
        listeners.add(o);        
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.IQueueProcessor#addEntry(org.cip4.jdflib.jmf.JDFCommand, org.cip4.jdflib.core.JDFDoc)
     */
    public JDFResponse addEntry(JDFCommand submitQueueEntry, JDFDoc theJDF)
    {
        if(submitQueueEntry==null || theJDF==null)
        {
            log.error("error submitting new queueentry");
            return null;
        }
        if(!myQueue.canAccept())
            return null;
        
        JDFQueueSubmissionParams qsp=submitQueueEntry.getQueueSubmissionParams(0);
        if(qsp==null)
        {
            log.error("error submitting new queueentry");
            return null;
        }
        
        JDFResponse r=qsp.addEntry(myQueue, null);
        JDFQueueEntry newQE=r.getQueueEntry(0);
        if(r.getReturnCode()!=0 || newQE==null)
        {
            log.error("error submitting queueentry: "+r.getReturnCode());
            return r;
        }
        //       myQueue.replaceElement(r.getQueue(0));   // copy the updated queue  
        if(!storeDoc(newQE,theJDF))
        {
            log.error("error storing queueentry: "+r.getReturnCode());
        }
        persist();

        VJDFAttributeMap vPartMap=newQE.getPartMapVector();
        JDFNode node=theJDF.getJDFRoot();
        JDFAttributeMap partMap=vPartMap==null ? null : vPartMap.elementAt(0);
        final String workStepID = node.getWorkStepID(partMap);
        final String queueEntryID = newQE.getQueueEntryID();
        // TODO move to processor device
        statusListener.setNode(queueEntryID, workStepID, node, vPartMap, null);        
        if(queueEntryID!=null)
        {
            signalDispatcher.addSubscriptions(node,queueEntryID);
        }
        notifyListeners();

        return r;
    }

    /**
     * @param newQE
     * @param theJDF
     */
    private boolean storeDoc(JDFQueueEntry newQE, JDFDoc theJDF)
    {
        if(newQE==null || theJDF==null)
        {
            log.error("error storing queueentry");
            return false;
        }
        String newQEID=newQE.getQueueEntryID();
        newQE=myQueue.getEntry(newQEID);
        if(newQE==null)
        {
            log.error("error fetching queueentry: QueueEntryID="+newQEID);
            return false;
        }
        String theDocFile=jdfDir+newQEID+".jdf";
        theJDF.write2File(theDocFile, 0, true);
        try
        {
            BambiNSExtension.setDocURL(newQE, UrlUtil.fileToUrl(new File(theDocFile), false));
        }
        catch (MalformedURLException x)
        {
            log.error("invalid file name: "+theDocFile);
        }
        return true;
    }

    private void notifyListeners()
    {
        for(int i=0;i<listeners.size();i++)
        {
            final Object elementAt = listeners.elementAt(i);
            synchronized (elementAt)
            {
                elementAt.notifyAll();               
            }
         }
    }

    /**
     * make the memory queue persistant
     *
     */
    private synchronized void persist()
    {
        log.info("persisting queue to"+queueFile.getAbsolutePath());
        myQueue.getOwnerDocument_KElement().write2File(queueFile.getAbsolutePath(), 0, true);
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.IQueueProcessor#getQueue()
     */
    public JDFQueue getQueue()
    {
        return myQueue;
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.IQueueProcessor#updateEntry(java.lang.String, org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus)
     */
    public void updateEntry(String queueEntryID, EnumQueueEntryStatus status)
    {
        if(queueEntryID==null)
            return;
        JDFQueueEntry qe=myQueue.getEntry(queueEntryID);
        qe.setQueueEntryStatus(status);
        persist();
        notifyListeners();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String s="[QueueProcessor: ] Status= "+myQueue.getQueueStatus().getName()+" Num Entries: "+myQueue.numEntries(null)+"\n Queue:\n";
        s+=myQueue.toString();
        return s;
    }
}
