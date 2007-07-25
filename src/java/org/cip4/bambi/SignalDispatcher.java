/**
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
 * copyright (c) 1999-2006, Heidelberger Druckmaschinen AG 
 * copyright (c) 1999-2001, Agfa-Gevaert N.V. 
 * 
 *  
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
package org.cip4.bambi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.ifaces.IJMFSubscribable;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.jmf.JDFStopPersChParams;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.VectorMap;

/**
 * 
 * this class handles subscriptions
 * @author prosirai
 *
 */
public class SignalDispatcher implements ISignalDispatcher 
{

    protected static Log log = LogFactory.getLog(SignalDispatcher.class.getName());
    protected HashMap subscriptionMap;
    protected VectorMap queueEntryMap;
    protected IMessageHandler messageHandler;
    protected VectorMap triggers;
    protected Object mutex;

    
    /////////////////////////////////////////////////////////////
    protected class Trigger
    {
        protected String queueEntryID;
        protected String workStepID;
        protected int amount;
        
        public Trigger(String _queueEntryID, String _workStepID, int _amount)
        {
            super();
            this.queueEntryID = _queueEntryID;
            this.workStepID = _workStepID;
            this.amount = _amount;
        }

        /**
         * equals ignores the value of Amount!
         */
        public boolean equals(Object arg0)
        {
            if(!(arg0 instanceof Trigger))
                return false;
            Trigger t=(Trigger) arg0;
            
            boolean bQE=queueEntryID==null && t.queueEntryID==null || queueEntryID!=null && queueEntryID.equals(t.queueEntryID);
            if(!bQE)
                return false;
            return workStepID==null && t.workStepID==null || workStepID!=null && workStepID.equals(t.workStepID);            
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            return queueEntryID==null ? 0 : queueEntryID.hashCode() + workStepID==null ? 0 : workStepID.hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return "Trigger: queueEntryID: "+queueEntryID+" workStepID: "+workStepID+ "amount: "+amount;
        }
        
        
        
    }
    /////////////////////////////////////////////////////////////
    protected class Dispatcher implements Runnable
    {
        /**
         * this is the time clocked thread
         */
        public void run()
        {
            while(true)
            {
                final Vector triggerVector = getTriggerSubscriptions();
                // spam them out
                for(int i=0;i<triggerVector.size();i++)
                {
                    final MsgSubscription sub=(MsgSubscription) triggerVector.elementAt(i);
                    log.debug("Trigger Signalling :"+i+" channelID="+sub.channelID);
                    // TODO think about new threads here
                    sub.signalJMF();
                }
               // select pending time subscriptions
                final Vector subVector = getTimeSubscriptions();
                // spam them out
                for(int i=0;i<subVector.size();i++)
                {
                    final MsgSubscription sub=(MsgSubscription) subVector.elementAt(i);
                    log.debug("Time Signalling :"+i+" channelID="+sub.channelID);
                    // TODO think about new threads here
                    sub.signalJMF();
                }
 
                try
                {
                    synchronized (mutex)
                    {
                        mutex.wait(1000);                       
                    }
                 }
                catch (InterruptedException x)
                {
                    //
                }
            }
        }

        /**
         * get the triggered subscriptions, either forced (amount=-1) or by amount
         * @return the vector of triggered subscriptions
         */
        private Vector getTriggerSubscriptions()
        {
            Vector v = new Vector();
            Iterator it=subscriptionMap.entrySet().iterator(); // subscriptions
            while(it.hasNext())
            {
                MsgSubscription sub=(MsgSubscription)it.next();
                String channelID=sub.channelID;
                int siz=triggers.size(channelID);
                for(int i=0;i<siz;i++)
                {


                    Trigger t= (Trigger) triggers.getOne(channelID,i);
                    MsgSubscription subClone=(MsgSubscription) sub.clone();
                    subClone.trigger=t;

                    if(t.amount<0)
                    {
                        v.add(subClone);
                    }
                    else if(t.amount>0)
                    {
                        if(subClone.repeatAmount>0)
                        {
                            int last=subClone.lastAmount;
                            int next=last+t.amount;
                            if(next/sub.repeatAmount > last/sub.repeatAmount)
                            {
                                sub.lastAmount=next; // not a typo - modify of nthe original subscription
                                v.add(subClone);
                            }
                        }                    
                    }
                }
            }
            for(int j=0;j<v.size();j++)
            {
                MsgSubscription sub=(MsgSubscription)v.elementAt(j);                
                triggers.remove(sub.trigger);
            }
            return v;                
        }

        private Vector getTimeSubscriptions()
        {
            Vector subVector=new Vector();
            synchronized(subscriptionMap)
            {
                Iterator it=subscriptionMap.entrySet().iterator();
                long now=System.currentTimeMillis()/1000;
                while(it.hasNext())
                {
                    MsgSubscription sub=(MsgSubscription) it.next();
                    if(sub.bActive && sub.repeatTime>0)
                    {
                        if(sub.lastTime-now>sub.repeatTime)
                        {
                            sub.lastTime=now;
                            sub=(MsgSubscription) sub.clone();
                            subVector.add(sub);
                        }
                    }
                }
            } // end synch map
            return subVector;
        }
        
    }
    
    private class MsgSubscription implements Cloneable
    {
        protected boolean bActive;
        protected String channelID;
        protected String url;
        protected int repeatAmount, lastAmount;
        protected long repeatTime, lastTime;
        protected JDFMessage theMessage;
        protected Trigger trigger;
        
        MsgSubscription(IJMFSubscribable m)
        {
            JDFSubscription sub=m.getSubscription();
            if(sub==null)
            {
                log.error("Subscribing to non subscription ");
                channelID=null;
                return;
            }
             channelID=m.getID(); 
             url=sub.getURL();
             lastAmount=0;
             repeatAmount=sub.getRepeatStep();
             lastTime=0;
             repeatTime=(long)sub.getRepeatTime();
             theMessage=(m instanceof JDFMessage) ? theMessage : null;
             bActive=false;
             trigger=new Trigger(null,null,0);
             //TODO observation targets
        }
        /**
         * 
         */
        protected void signalJMF()
        {
            if(!(theMessage instanceof JDFQuery))
            {
                //TODO guess what...
                log.error("registrations not supported");
                return;
            }
            JDFQuery q=(JDFQuery)theMessage;
            JDFJMF jmf=q.createResponse();
            JDFResponse r=jmf.getResponse(0);
            q=(JDFQuery) jmf.copyElement(q, null);
            q.removeChild(ElementName.SUBSCRIPTION, null, 0);
            
            boolean b=messageHandler.handleMessage(q, r, trigger.queueEntryID, trigger.workStepID);
            if(!b)
            {
                log.error("Unhandled message: "+q.getType());
                return;
            }
            jmf=JDFJMF.createJMF(EnumFamily.Signal, q.getEnumType());
            JDFSignal s=jmf.getSignal(0);
            s.convertResponse(r, q);
            JDFDoc resp=new JDFDoc(jmf.getOwnerDocument()).write2URL(url);
            // TODO error handling
            
        }
        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        public Object clone()
        {
            // TODO Auto-generated method stub
            MsgSubscription c;
            try
            {
                c = (MsgSubscription) super.clone();
            }
            catch (CloneNotSupportedException x)
            {
                return null;
            }
            c.bActive=bActive;
            c.channelID=channelID;
            c.lastAmount=lastAmount;
            c.repeatAmount=repeatAmount;
            c.repeatTime=repeatTime;
            c.theMessage=theMessage; // ref only NOT Cloned (!!!)
            c.url=url;
            return c;
        }
    }
    
    /**
     * 
     * handler for the StopPersistentChannel command
     */
    public class StopPersistentChannelHandler implements IMessageHandler
    {
    
        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage inputMessage, JDFResponse response, String s1, String s2)
        {
            if(!EnumType.StopPersistentChannel.equals(inputMessage.getEnumType()))
                return false;
            JDFStopPersChParams spcp=inputMessage.getStopPersChParams(0);
            if(spcp==null)
                return false;
            String channel=spcp.getChannelID();
            boolean bHandled=false;
            if(!KElement.isWildCard(channel))
            {
                removeSubScription(channel);
                bHandled=true;
            }
            String queueEntryID=spcp.getQueueEntryID();
            if(!KElement.isWildCard(queueEntryID))
            {
                removeSubScription(channel);
                bHandled=true;
            }
            
            return bHandled;
        }
        

    
        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#getFamilies()
         */
        public EnumFamily[] getFamilies()
        {
            return new EnumFamily[]{EnumFamily.Command};
        }
    
        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#getMessageType()
         */
        public EnumType getMessageType()
        {
             return EnumType.StopPersistentChannel;
        }
    }

    public SignalDispatcher(IMessageHandler _messageHandler)
    {        
        subscriptionMap=new HashMap();
        queueEntryMap=new VectorMap();
        messageHandler=_messageHandler;
        triggers=new VectorMap();
        mutex = new Object();
         
        new Thread(new Dispatcher()).start();
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.ISignalDispatcher#addSubscription(org.cip4.jdflib.ifaces.IJMFSubscribable)
     */
    public String addSubscription(IJMFSubscribable subMess, String queueEntryID)
    {
        MsgSubscription sub=new MsgSubscription(subMess);
        if(sub.channelID==null)
        {
            return null;
        }
        
        if(subscriptionMap.containsKey(sub.channelID))
        {
            log.error("subscription already exists for:"+sub.channelID);
            return null;
        }
        synchronized (subscriptionMap)
        {
            subscriptionMap.put(sub.channelID, sub);
        }
        if(queueEntryID!=null)
            queueEntryMap.putOne(queueEntryID, sub.channelID);
        else
            queueEntryMap.putOne("*", sub.channelID);
        
        return sub.channelID;
    }
    
    /* (non-Javadoc)
     * @see org.cip4.bambi.ISignalDispatcher#addSubscription(org.cip4.jdflib.ifaces.IJMFSubscribable)
     */
    public VString addSubscriptions(JDFNode n, String queueEntryID)
    {
        final JDFNodeInfo nodeInfo = n.getNodeInfo();
        if(nodeInfo==null)
            return null;
        VElement vJMF=nodeInfo.getChildElementVector(ElementName.JMF, null, null, true, 0,true);
        int siz=vJMF==null ? 0 : vJMF.size();
        if(siz==0)
            return null;
        VString vs=new VString();
        for(int i=0;i<siz;i++)
        {
            JDFJMF jmf=nodeInfo.getJMF(i);
            // TODO regs
            VElement vMess=jmf.getMessageVector(EnumFamily.Query, null);
            int nMess= vMess==null ? 0 : vMess.size();
            for(int j=0;j<nMess;j++)
            {
                JDFQuery q=(JDFQuery)vMess.elementAt(j);
                String channelID=addSubscription(q, queueEntryID);
                if(channelID!=null)
                   vs.add(channelID); 
            }            
        }
        return vs;
     }
    
    /* (non-Javadoc)
     * @see org.cip4.bambi.ISignalDispatcher#removeSubScription(java.lang.String)
     */
    public void removeSubScription(String channelID)
    {
        subscriptionMap.remove(channelID);
        triggers.remove(channelID);
    }
    /* (non-Javadoc)
     * @see org.cip4.bambi.ISignalDispatcher#removeSubScription(java.lang.String)
     */
    public void removeSubScriptions(String queueEntryID)
    {
        Vector v=(Vector)queueEntryMap.get(queueEntryID);
        int siz= v==null ? 0 : v.size();
        for(int i=0;i<siz;i++)
            removeSubScription((String)v.get(i));
        queueEntryMap.remove(queueEntryID);
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.ISignalDispatcher#triggerChannel(java.lang.String)
     */
    public void triggerChannel(String channelID,  String queueEntryID, String workStepID, int amount)
    {
        Trigger tNew=new Trigger(queueEntryID, workStepID, amount);
        Trigger t=(Trigger) triggers.getOne(channelID, tNew);
        if(t==null)
        {
            triggers.putOne(channelID, tNew);
        }
        else if(amount>0 && t.amount>=0) // -1 always forces
        {
            t.amount+=amount; 
        }
        if(amount<0)
        {
            synchronized (mutex)
            {
                mutex.notifyAll();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.ISignalDispatcher#triggerQueueEntry(java.lang.String)
     */
    public void triggerQueueEntry(String queueEntryID,  String workStepID, int amount)
    {
        Vector v=(Vector) queueEntryMap.get(queueEntryID);
        int si=v==null ? 0 : v.size();
        for(int i=0;i<si;i++)
        {
            triggerChannel((String)v.get(i),queueEntryID, workStepID, amount);
        }
        // now the global queries
        v=(Vector) queueEntryMap.get("*");
        si=v==null ? 0 : v.size();
        for(int i=0;i<si;i++)
        {
            triggerChannel((String)v.get(i),queueEntryID, workStepID, amount);
        }
    }

    /**
     * @param jmfHandler
     */
    public void addHandlers(IJMFHandler jmfHandler)
    {
        jmfHandler.addHandler(this.new StopPersistentChannelHandler());        
    }
}
