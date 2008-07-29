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

import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.VectorMap;


/**
 *
 * @author  rainer
 */
public class JMFBufferHandler extends AbstractHandler implements IMessageHandler
{
    private static class MessageIdentifier implements Cloneable
    {
        protected String channelID = null;
        protected String msgType=null;
        protected String senderID=null;

        MessageIdentifier(JDFMessage m)
        {
            if(m==null)
                return;
            msgType=m.getType();
            channelID=m.getrefID();
            channelID=m.getSenderID();
            if(KElement.isWildCard(channelID))
                channelID=null;
        }


        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        @Override
        public Object clone()
        {
            MessageIdentifier c;
            try
            {
                c = (MessageIdentifier) super.clone();
            }
            catch (CloneNotSupportedException x)
            {
                return null;
            }
            c.channelID=channelID;
            c.msgType=msgType;
            c.senderID=senderID;
            return c;
        }

        @Override
        public String toString() {
            return "[MessageIdentifier: channelID="+channelID+
            " Type="+msgType+
            " SenderID="+senderID+"]";
        }



        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if(!(obj instanceof MessageIdentifier))
                return false;
            MessageIdentifier msg=(MessageIdentifier)obj;

            if(!ContainerUtil.equals(senderID, msg.senderID))
                return false;
            if(!ContainerUtil.equals(channelID, msg.channelID))
                return false;
            if(!ContainerUtil.equals(msgType, msg.msgType))
                return false;
            return true;
        }
        /**
         * if obj matches, i.e. any null element of object is also considered matching
         * @param
         */
        public boolean matches(MessageIdentifier msg)
        {
            if( msg.senderID!=null && !ContainerUtil.equals(senderID, msg.senderID))
                return false;
            if( msg.channelID!=null && !ContainerUtil.equals(channelID, msg.channelID))
                return false;
            if(!ContainerUtil.equals(msgType, msg.msgType))
                return false;
            return true;
        }



        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            int hc=senderID==null ? 0 : senderID.hashCode();
            hc+=msgType==null ? 0 : msgType.hashCode();
            hc+=channelID==null ? 0 : channelID.hashCode();
            return hc;
        }
    }

    protected static final Log log = LogFactory.getLog(JMFBufferHandler.class.getName());
    protected VString ignoreSenderIDs=null;

    protected VectorMap<MessageIdentifier, JDFSignal> messageMap=new VectorMap<MessageIdentifier, JDFSignal>();
    /**
     * @param _type
     * @param _families
     */
    public JMFBufferHandler(String typ, EnumFamily[] _families)
    {
        super(typ, _families);
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
     */
    public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
    { 
        if(inputMessage==null)
            return false;
        EnumFamily family=inputMessage.getFamily();
        if(EnumFamily.Signal.equals(family))
        {
            if(ignore(inputMessage))
                return true;
            return handleSignal( (JDFSignal)inputMessage,response);
        }
        else if(EnumFamily.Query.equals(family))
        {
            JDFJMF jmf= getSignals(inputMessage,response);
            return true;
        }
        return false;
    }

    /**
     * @param inputMessage
     * @return
     */
    protected boolean ignore(JDFMessage inputMessage)
    {
        if(ignoreSenderIDs==null)
            return false;
        if(inputMessage==null)
            return true; 

        String senderID=inputMessage.getSenderID();
        for(int i=0;i<ignoreSenderIDs.size();i++)
        {
            if(senderID.indexOf(ignoreSenderIDs.get(i))>=0)
                return true;
        }
        return false;
    }

    /**
     * @param ignoreSenderIDs
     */
    public void setIgnoreSendersIDs(VString _ignoreSenderIDs)
    {
        if(_ignoreSenderIDs==null || _ignoreSenderIDs.size()==0)
        {
            ignoreSenderIDs=null;
        }
        else
        {
            ignoreSenderIDs=_ignoreSenderIDs;
        }

    }
    /**
     * @param inputMessage
     * @param response
     * @return
     */
    protected JDFJMF getSignals(JDFMessage inputMessage, JDFResponse response)
    {
        synchronized(messageMap)
        {
            MessageIdentifier messageIdentifier = new MessageIdentifier(inputMessage);
            Iterator<MessageIdentifier> it=messageMap.keySet().iterator();
            JDFJMF jmf=response.getJMFRoot();
            Vector<MessageIdentifier> v=new Vector<MessageIdentifier>();

            while(it.hasNext())
            {
                MessageIdentifier mi=it.next();
                if(mi.matches(messageIdentifier))
                {
                    v.add(mi);
                    Vector<JDFSignal> sis=messageMap.get(mi);
                    for(int i=0;i<sis.size();i++)
                    {
                        // copy the potentially inherited senderID
                        JDFSignal signal = sis.get(i);
                        JDFSignal sNew=(JDFSignal) jmf.copyElement(signal, null);
                        sNew.setSenderID(signal.getSenderID());
                    }
                }
            }
            if(v.size()>0)
            {
                for(int i=0;i<v.size();i++)
                {
                    messageMap.remove(v.get(i));
                }
            }
            else
            {
                jmf=null;
            }
            response.deleteNode();// always zapp the dummy response
            inputMessage.deleteNode(); // also zapp the query
            return jmf;
        }
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.core.messaging.JMFBufferHandler#handleSignal(org.cip4.jdflib.jmf.JDFSignal, org.cip4.jdflib.jmf.JDFResponse)
     */
    protected boolean handleSignal(JDFSignal inSignal, JDFResponse response)
    {
        MessageIdentifier mi=new MessageIdentifier(inSignal);
        synchronized (messageMap)
        {
            messageMap.putOne(mi,inSignal);
            return true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////

    public static class StatusBufferHandler extends JMFBufferHandler
    {

        /* (non-Javadoc)
         * @see org.cip4.bambi.core.messaging.JMFBufferHandler#getSignals(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
         */
        @Override
        protected JDFJMF getSignals(JDFMessage inputMessage, JDFResponse response)
        {
            JDFStatusQuParams sqp=inputMessage.getStatusQuParams();
            boolean queueInfo=false;
            if(sqp!=null)
                queueInfo=sqp.getQueueInfo();

            JDFJMF jmf=super.getSignals(inputMessage, response);
            if(jmf==null)
                return jmf;
            VElement sigs=jmf.getMessageVector(EnumFamily.Signal, EnumType.Status);
            int sigSize=sigs==null  ? 0 : sigs.size();
            for(int i=0;i<sigSize;i++)
            {
                JDFSignal s=(JDFSignal) sigs.get(i);
                JDFQueue signalQueue=s.getQueue(0);
                if(signalQueue!=null)
                    signalQueue.deleteNode();
            }
            return jmf;

        }
        public StatusBufferHandler()
        {
            super("Status", new EnumFamily[]{EnumFamily.Signal,EnumFamily.Query});
        }
        /* (non-Javadoc)
         * @see org.cip4.bambi.core.messaging.JMFBufferHandler#handleSignal(org.cip4.jdflib.jmf.JDFSignal, org.cip4.jdflib.jmf.JDFResponse)
         */
        @Override
        protected boolean handleSignal(JDFSignal theSignal, JDFResponse response)
        {
            VElement vSigs=splitSignals(theSignal);
            for(int i=0;i<vSigs.size();i++)
            {
                JDFSignal inSignal=(JDFSignal)vSigs.get(i);
                MessageIdentifier mi=new MessageIdentifier(inSignal);
                synchronized (messageMap)
                {
                    JDFSignal last=messageMap.getOne(mi,-1);
                    if(last==null)
                    {
                        messageMap.putOne(mi,inSignal);
                    }
                    else
                    {
                        boolean bAllSame = isSameStatusSignal(inSignal, last);
                        if(bAllSame)
                        {
                            mergeStatusSignal(inSignal, last);
                            messageMap.setOne(mi,inSignal,last);
                        }
                        else
                        {
                            messageMap.putOne(mi,inSignal);
                        }
                    }
                }
            }
            return true;
        }
        /**
         * @param theSignal
         * @return
         */
        private VElement splitSignals(JDFSignal theSignal)
        {
            VElement devInfos=theSignal.getChildElementVector(ElementName.DEVICEINFO, null);
            VElement sigs=new VElement();
            sigs.add(theSignal);                
            if(devInfos.size()==1)
            {
                theSignal.setSenderID(((JDFDeviceInfo) devInfos.get(0)).getDeviceID());
            }
            else
            {
                String senderID=theSignal.getSenderID();
                for(int i=0;i<devInfos.size();i++)
                {
                    JDFDeviceInfo di=(JDFDeviceInfo) devInfos.get(i);
                    String did=di.getDeviceID();
                    if(!ContainerUtil.equals(did, senderID))
                    {
                        JDFSignal s=null;
                        for(int ii=1;ii<sigs.size();ii++)
                        {
                            JDFSignal s2=(JDFSignal)sigs.get(ii);
                            if(ContainerUtil.equals(s2.getSenderID(), did))
                            {
                                s=s2;
                                break;
                            }
                        }
                        if(s==null)
                        {
                            s=JDFJMF.createJMF(EnumFamily.Signal, EnumType.Status).getSignal(0);
                            s.copyElement(theSignal.getQueue(0), null);
                            sigs.add(s);
                        }
                        s.setSenderID(did);
                        s.moveElement(di, null);
                    }
                }
            }
            if(theSignal.numChildElements(ElementName.DEVICEINFO, null)==0)
                sigs.remove(0);
            return sigs;
        }
        /**
         * @param inSignal
         * @param last
         */
        private void mergeStatusSignal(JDFSignal inSignal, JDFSignal last)
        {
            for(int i=0;true;i++)
            {
                JDFDeviceInfo di=inSignal.getDeviceInfo(i);  
                if(di==null)
                    break;
                boolean bSameDI=false;
                for(int j=0;!bSameDI;j++)
                {
                    JDFDeviceInfo diLast=last.getDeviceInfo(j);
                    if(diLast==null)
                        break;
                    bSameDI=di.mergeLastPhase(diLast);
                }                        
            }
        }
        /**
         * @param inSignal
         * @param last
         * @return
         */
        private boolean isSameStatusSignal(JDFSignal inSignal, JDFSignal last)
        {
            boolean bAllSame=true;
            for(int i=0;bAllSame;i++)
            {
                JDFDeviceInfo di=inSignal.getDeviceInfo(i);  
                if(di==null)
                    break;
                boolean bSameDI=false;
                for(int j=0;!bSameDI;j++)
                {
                    JDFDeviceInfo diLast=last.getDeviceInfo(j);
                    if(diLast==null)
                        break;
                    bSameDI=di.isSamePhase(diLast, false);
                }                        
                bAllSame = bAllSame && bSameDI;
            }
            return bAllSame;
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////

    public static class NotificationBufferHandler extends JMFBufferHandler
    {

        public NotificationBufferHandler()
        {
            super("Notification", new EnumFamily[]{EnumFamily.Signal,EnumFamily.Query});
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////

    public static class ResourceBufferHandler extends JMFBufferHandler
    {

        public ResourceBufferHandler()
        {
            super("Resource", new EnumFamily[]{EnumFamily.Signal,EnumFamily.Query});
        }
    }

}
