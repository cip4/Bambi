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

package org.cip4.bambi.core.messaging;

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFRequestQueueEntryParams;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;

/**
 * factory for creating JMF messages
 * 
 * @author boegerni
 * 
 */
public class JMFFactory {
	
    private static Log log = LogFactory.getLog(JMFFactory.class.getName());
    private static HashMap<String,MessageSender> senders=new HashMap<String, MessageSender>();
    private static IConverterCallback callback=null;
    private static int nThreads=0;

    /**
	 * build a JMF SuspendQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to suspend
	 * @return the message
	 */
	public static JDFJMF buildSuspendQueueEntry(String queueEntryId)
	{
		JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Command, EnumType.SuspendQueueEntry);
		jmf.getCommand(0).appendQueueEntryDef().setQueueEntryID(queueEntryId);
		return jmf;
	}
	
	/**
	 * build a JMF HoldQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to hold
	 * @return the message
	 */
	public static JDFJMF buildHoldQueueEntry(String queueEntryId)
	{
		JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Command, EnumType.HoldQueueEntry);
		jmf.getCommand(0).appendQueueEntryDef().setQueueEntryID(queueEntryId);
		return jmf;
	}
	
	/**
	 * build a JMF ResumeQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to resume
	 * @return the message
	 */
	public static JDFJMF buildResumeQueueEntry(String queueEntryId)
	{
		JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Command, EnumType.ResumeQueueEntry);
		jmf.getCommand(0).appendQueueEntryDef().setQueueEntryID(queueEntryId);
		return jmf;
	}

	/**
	 * build a JMF AbortQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to abort
	 * @return the message
	 */
	public static JDFJMF buildAbortQueueEntry(String queueEntryId)
	{
		JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Command, EnumType.AbortQueueEntry);
		jmf.getCommand(0).appendQueueEntryDef().setQueueEntryID(queueEntryId);
		return jmf;
	}
	
	/**
	 * build a JMF RemoveQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to remove
	 * @return the message
	 */
	public static JDFJMF buildRemoveQueueEntry(String queueEntryId)
	{
		JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Command, EnumType.RemoveQueueEntry);
		jmf.getCommand(0).appendQueueEntryDef().setQueueEntryID(queueEntryId);
		return jmf;
	}
	
    /**
     * build a JMF Status query
     * @return the message
     */
    public static JDFJMF buildStatus()
    {
        JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.Status);
        return jmf;
    }
    /**
     * build a JMF Status subscription
     * @return the message
     */
    public static JDFJMF buildStatusSubscription(String subscriberURL, double repeatTime, int repeatStep)
    {
        final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.Status);
        final JDFQuery q=jmf.getQuery(0);
        final JDFSubscription s=q.appendSubscription();
        s.setURL(subscriberURL);
        if(repeatTime>0)
            s.setRepeatTime(repeatTime);
        if(repeatStep>0)
            s.setRepeatStep(repeatStep);
        s.appendObservationTarget().setObservationPath("*");

        return jmf;
    }
	
	/**
	 * build a JMF QueueStatus query
	 * @return the message
	 */
	public static JDFJMF buildQueueStatus()
	{
		JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.QueueStatus);
		return jmf;
	}
	
	/**
	 * build a JMF RequestQueueEntry command <br/>
	 *    default: JMFFactory.buildRequestQueueEntry(theQueueURL,null)
	 * @param queueURL the queue URL of the device sending the command
	 * 				   ("where do you want your SubmitQE's delivered to?")
	 * @param deviceID the DeviceID of the worker requesting the QE (default=null)
	 * @return the message
	 */
	public static JDFJMF buildRequestQueueEntry(String queueURL, String deviceID)
	{
		// maybe replace DeviceID with DeviceType, just to be able to decrease the 
		// Proxy's knowledge about querying devices?
		JDFJMF jmf=JDFJMF.createJMF(EnumFamily.Command, EnumType.RequestQueueEntry);
		JDFRequestQueueEntryParams qep=jmf.getCommand(0).appendRequestQueueEntryParams();
		qep.setQueueURL(queueURL);
		BambiNSExtension.setDeviceID(qep, deviceID);
		return jmf;
	}
	
	/**
	 * sends a JMF message to a given URL
	 * @param jmf the message to send
	 * @param url the URL to send the JMF to
	 * @return the response if successful, otherwise null
	 */
	public static void send2URL(JDFJMF jmf, String url, IMessageHandler handler, String senderID) {
		
		if (jmf==null || url==null) {
			if (log!=null) {
                // this method is prone for crashing on shutdown, thus checking for 
                // log!=null is important
				log.error("failed to send JDFMessage, message and/or URL is null");
			}
			return;
		}
        
        MessageSender ms = getCreateMessageSender(url,callback); 
        if(senderID!=null)
            jmf.setSenderID(senderID);
        ms.queueMessage(jmf,handler);
	}

    /**
     * 
     * @param url
     */
    public static void shutDown(String url, boolean graceFully)
	{
        if(url==null) // null = all
        {           
            final Set<String> keySet = senders.keySet();
            String[]as=keySet.toArray(new String[keySet.size()]);
            for(int i=0;i<as.length;i++)
            {
                String s=as[i];
                if(s!=null)
                {
                    shutDown(s,graceFully);
                }
                senders.remove(s);                                   
            }
        }
        else // individual url
        {
            MessageSender ms=senders.get(url);
            if(ms!=null)
            {
                ms.shutDown(graceFully);
            }
            senders.remove(url);                                               
        }
	}

    public static MessageSender getCreateMessageSender(String url, IConverterCallback callBack)
    {
        if(url==null)
            return null;

        MessageSender ms=senders.get(url);
        if(ms==null)
        {
            ms=new MessageSender(url,callBack);
            senders.put(url, ms);
            new Thread(ms,"MessageSender_"+nThreads++).start();
        }
        return ms;
    }

    /**
     * @param callBackClass the standard callback class for all sent jmf messages
     */
    public static void setCallBack(IConverterCallback callBackClass)
    {
        callback=callBackClass;       
    }
	
}