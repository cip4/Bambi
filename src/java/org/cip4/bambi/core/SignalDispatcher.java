/**
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
package org.cip4.bambi.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.ifaces.IJMFSubscribable;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFResourceQuParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFStopPersChParams;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.VectorMap;

/**
 * 
 * this class handles subscriptions <br>
 * class should remain final, because if it is ever subclassed the dispactcher thread would be started 
 * before the constructor from the subclass has a chance to fire off.
 * 
 * @author prosirai
 * 
 */
public final class SignalDispatcher implements ISignalDispatcher 
{
    protected static final Log log = LogFactory.getLog(SignalDispatcher.class.getName());
    protected HashMap<String, MsgSubscription> subscriptionMap=null; // map of channelID / Subscription
//    protected VectorMap<String,String> queueEntryMap=null; // map of queueEntryID / vector of channelIDS
    protected IMessageHandler messageHandler=null;
    protected Vector<Trigger> triggers=null;
    protected Object mutex=null;
    protected boolean doShutdown=false;
    protected IDevice device=null;
    private JMFFactory jmfFactory;
    private Dispatcher theDispatcher;

    /////////////////////////////////////////////////////////////
    protected static class Trigger
    {
        protected String queueEntryID;
        protected String workStepID;
        protected String channelID;
        protected int amount;

        public Trigger(String _queueEntryID, String _workStepID, String _channelID, int _amount)
        {
            super();
            queueEntryID = _queueEntryID;
            workStepID = _workStepID;
            channelID=_channelID;
            amount = _amount;
        }

        /**
         * equals ignores the value of Amount!
         */
        @Override
         public boolean equals(Object t1)
        {
            if(!(t1 instanceof Trigger))
                return false;
            Trigger t=(Trigger) t1;
            boolean b=ContainerUtil.equals(channelID, t.channelID);
            b=b && ContainerUtil.equals(queueEntryID, t.queueEntryID) ;
            b=b &&  ContainerUtil.equals(workStepID, t.workStepID) ;   
            return b;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "Trigger: queueEntryID: "+queueEntryID+" workStepID: "+workStepID+ " amount: "+amount+workStepID+ " channelID: "+channelID;
        }

        @Override
        public int hashCode()
        {
            // TODO Auto-generated method stub
            return super.hashCode()+channelID==null ? 0 : channelID.hashCode() + queueEntryID==null ? 0 : queueEntryID.hashCode() + workStepID==null ? 0 : workStepID.hashCode();
        }

    }
    /////////////////////////////////////////////////////////////
    protected class Dispatcher implements Runnable
    {
        public Dispatcher()
        {
            super();
        }

        /**
         * this is the time clocked thread
         */
        public void run()
        {
            while(!doShutdown)
            {

                flush();

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
         * 
         */
        private void flush()
        {
            while(true)
            {
                final Vector<MsgSubscription> triggerVector = getTriggerSubscriptions();
                final int size = triggerVector.size();
                // spam them out
                for(int i=0;i<size;i++)
                {
                    final MsgSubscription sub=triggerVector.elementAt(i);
                    log.debug("Trigger Signalling :"+i+" channelID="+sub.channelID);
                    queueMessageInSender(sub);
                }
                // select pending time subscriptions
                final Vector<MsgSubscription> subVector = getTimeSubscriptions();
                final int size2 = subVector.size();
                // spam them out
                for(int i=0;i<size2;i++) {
                    final MsgSubscription sub=subVector.elementAt(i);
                    log.debug("Time Signalling: "+i+", channelID="+sub.channelID);
                    queueMessageInSender(sub);
                }
                if(size==0 && size2==0)
                    break; // flushed all
            }                
        }

        /**
         * queue a message in the appropriate sender
         * @param sub
         */
        private void queueMessageInSender(final MsgSubscription sub) {
            String url=sub.getURL();
            final JDFJMF signalJMF = sub.getSignal();
            if(signalJMF!=null)
            {
                signalJMF.setSenderID(getSenderID());
                jmfFactory.send2URL(signalJMF, url, null,getSenderID());
            }
            else
            {
                log.error("bad subscription: "+sub);
            }
        }

        /**
         * get the triggered subscriptions, either forced (amount=-1) or by amount
         * @return the vector of triggered subscriptions
         */
        private Vector<MsgSubscription> getTriggerSubscriptions()
        {
            synchronized (triggers)
            {
                Vector<MsgSubscription> v = new Vector<MsgSubscription>();
                Iterator<Trigger> it = triggers.iterator(); // active triggers
                while(it.hasNext())
                {
                    Trigger t=it.next();
                    String channelID=t.channelID;
                    MsgSubscription sub=subscriptionMap.get(channelID);
                    if(sub==null)
                        continue; // snafu
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
                 // remove active triggers that will be returned
                for(int j=0;j<v.size();j++)
                {
                    MsgSubscription sub=v.elementAt(j);                
                    triggers.remove(sub.trigger);
                }
                return v;
            }
        }

        private Vector<MsgSubscription> getTimeSubscriptions()
        {
            Vector<MsgSubscription> subVector=new Vector<MsgSubscription>();
            synchronized(subscriptionMap)
            {
                Iterator<Entry<String, MsgSubscription>> it=subscriptionMap.entrySet().iterator();
                long now=System.currentTimeMillis()/1000;
                while(it.hasNext())
                {
                    final Entry<String, MsgSubscription> next = it.next();
                    MsgSubscription sub=next.getValue();
                    if( sub.repeatTime>0)
                    {
                        if(now-sub.lastTime>sub.repeatTime)
                        {
                            // todo keine fehlerfortpflanzung
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
        protected String channelID = null;
        protected String queueEntry = null;
        protected String url = null;
        protected int repeatAmount, lastAmount = 0;
        protected long repeatTime, lastTime = 0;
        protected JDFMessage theMessage = null;
        protected Trigger trigger = null;

        MsgSubscription(IJMFSubscribable m, String qeid)
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
            queueEntry=qeid;
           
            lastAmount=0;
            repeatAmount=sub.getRepeatStep();
            lastTime=0;
            repeatTime=(long)sub.getRepeatTime();
            theMessage=(m instanceof JDFMessage) ? theMessage : null;
            trigger=new Trigger(null,null,null,0);
            theMessage=(JDFMessage)m;
            //TODO observation targets
            if(repeatTime==0 && repeatAmount==0) // reasonable default
            {
                repeatAmount=100;
                repeatTime=15; 
            }

        }

        public JDFJMF getSignal() 
        {
            if(!(theMessage instanceof JDFQuery))
            {
                //TODO guess what...
                log.error("registrations not supported");
                return null;
            }
            JDFQuery q=(JDFQuery)theMessage;
            JDFJMF jmf=q.createResponse();
            JDFResponse r=jmf.getResponse(0);
            q=(JDFQuery) jmf.copyElement(q, null);
            q.removeChild(ElementName.SUBSCRIPTION, null, 0);

            // this is the handling of the actual message
            boolean b=messageHandler.handleMessage(q, r);
            if(!b)
            {
                log.error("Unhandled message: "+q.getType());
                return null;
            }
            jmf=JDFJMF.createJMF(EnumFamily.Signal, q.getEnumType());
            JDFSignal s=jmf.getSignal(0);
            s.convertResponse(r, q);
            return jmf;
        }

        public String getURL() {
            return url;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        @Override
        public Object clone()
        {
            MsgSubscription c;
            try
            {
                c = (MsgSubscription) super.clone();
            }
            catch (CloneNotSupportedException x)
            {
                return null;
            }
            c.channelID=channelID;
            c.lastAmount=lastAmount;
            c.repeatAmount=repeatAmount;
            c.repeatTime=repeatTime;
            c.theMessage=theMessage; // ref only NOT Cloned (!!!)
            c.url=url;
            c.trigger=trigger; // ref only NOT Cloned (!!!)
            c.queueEntry=queueEntry;
            return c;
        }

        @Override
        public String toString() {
            return "[MsgSubscription: channelID="+channelID+
            " Type="+theMessage.getType()+
            " QueueEntry="+queueEntry+
            " lastAmount="+lastAmount+
            " repeatAmount="+repeatAmount+
            " repeatTime="+repeatTime+
            " lastTime="+lastTime+
            " URL="+url+"]";
        }

        /**
         * @param queueEntryID
         */
        protected void setQueueEntryID(String queueEntryID)
        {
            if(queueEntryID==null)
                return;
            if(theMessage==null)
                return;
            EnumType typ=theMessage.getEnumType();
            //TODO more message types
            if(EnumType.Status.equals(typ))
            {
                JDFStatusQuParams sqp=theMessage.getCreateStatusQuParams(0);
                sqp.setQueueEntryID(queueEntryID);
            }

        }
    }

    /**
     * 
     * handler for the StopPersistentChannel command
     */
    public class StopPersistentChannelHandler extends AbstractHandler
    {

        /**
         * @param _type
         * @param _families
         */
        public StopPersistentChannelHandler()
        {
            super(EnumType.StopPersistentChannel, new EnumFamily[]{EnumFamily.Command});
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
        {
            if(!EnumType.StopPersistentChannel.equals(inputMessage.getEnumType()))
                return false;
            JDFStopPersChParams spcp=inputMessage.getStopPersChParams(0);
            if(spcp==null)
                return true;
            final String channel=spcp.getChannelID();
            if(!KElement.isWildCard(channel))
            {
                removeSubScription(channel);
                return true;
            }
            String url=spcp.getURL();
            if(KElement.isWildCard(url))
            {
                JMFHandler.errorResponse(response, "No URL specified", 7);
                return true;
            }
            String queueEntryID=spcp.getQueueEntryID();
            if(KElement.isWildCard(queueEntryID))
                queueEntryID=null;
            removeSubScriptions(queueEntryID,url);

            return true;
        }

    }

    /**
     * constructor
     * @param _messageHandler message handler
     * @param deviceID ID of the device this SignalHandler is working for. 
     * 			       Required for debugging purposes only. 
     */
    public SignalDispatcher(IMessageHandler _messageHandler, IDevice dev)
    {    
        device=dev;
        subscriptionMap=new HashMap<String, MsgSubscription>();
 //       queueEntryMap=new VectorMap<String, String>();
        messageHandler=_messageHandler;
        triggers=new Vector<Trigger>();
        mutex = new Object();
        theDispatcher = new Dispatcher();
        new Thread(theDispatcher,"SignalDispatcher_"+getSenderID()).start();
        log.info("dispatcher thread 'SignalDispatcher_"+getSenderID()+"' started");
        IConverterCallback cb=null;
        if(dev instanceof AbstractDevice)
            cb=((AbstractDevice)device).getCallback();
        jmfFactory=new JMFFactory(cb);
    }

    /**
     * @param m
     * @param resp
     * @param dispatcher
     */
    public void findSubscription(JDFMessage m, JDFResponse resp)
    {
        if(!(m instanceof IJMFSubscribable))
        {
            return;        
        }
        IJMFSubscribable query=(IJMFSubscribable)m;
        JDFSubscription sub=query.getSubscription();
        if(sub==null)
            return;
        final String channelID=addSubscription(query, findQueueEntryID(m));
        if(resp!=null && channelID!=null)
            resp.setSubscribed(true);
    }

    /**
     * @param m
     * @return
     */
    private String findQueueEntryID(JDFMessage m)
    {
        if(m==null)
            return null;
        try{
            final EnumType messageType = m.getEnumType();
            if(EnumType.Status.equals(messageType))
            {
                JDFStatusQuParams sqp=m.getStatusQuParams();
                String qeid=sqp==null ? null : sqp.getQueueEntryID();
                return qeid;
            }
            else if(EnumType.Resource.equals(messageType))
            {
                JDFResourceQuParams rqp=m.getResourceQuParams();
                String qeid=rqp==null ? null : rqp.getQueueEntryID();
                return qeid;
            }
        }
        catch (JDFException x)
        { /* nop */ }
        return null;
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.ISignalDispatcher#addSubscription(org.cip4.jdflib.ifaces.IJMFSubscribable)
     */
    public String addSubscription(IJMFSubscribable subMess, String queueEntryID)
    {
        if(subMess==null)
        {
            log.error("adding null subscription"+queueEntryID);
            return null;
        }
        log.info("adding subscription ");
        MsgSubscription sub=new MsgSubscription(subMess,queueEntryID);
        sub.setQueueEntryID(queueEntryID);
        if(sub.channelID==null || sub.url==null)
        {
            return null;
        }
        if(getSenderID().equals(BambiServlet.getDeviceIDFromURL(sub.url)))
        {
            log .info("subscribing to self - ignore: "+getSenderID());
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
        sub.trigger.queueEntryID=queueEntryID;
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
            if (vMess!=null) {
                int nMess = vMess.size();
                for (int j = 0; j < nMess; j++) {
                    JDFQuery q = (JDFQuery) vMess.elementAt(j);
                    String channelID = addSubscription(q, queueEntryID);
                    if (channelID != null)
                        vs.add(channelID);
                }
            }            
        }
        return vs;
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.ISignalDispatcher#removeSubScription(java.lang.String)
     */
    public void removeSubScription(String channelID)
    {
        theDispatcher.flush();
        if(channelID==null)
            return;
        synchronized (triggers)
        {
            triggers.remove(channelID);
        }
        synchronized(subscriptionMap)
        {
            subscriptionMap.remove(channelID);
        }
        log.debug("removing subscription for channelid="+channelID);
    }
    /* (non-Javadoc)
     * @see org.cip4.bambi.ISignalDispatcher#removeSubScription(java.lang.String)
     */
    public void removeSubScriptions(String queueEntryID, String url) {
        Iterator<String> it=subscriptionMap.keySet().iterator();            
        boolean allURL = KElement.isWildCard(url);
        boolean allQE = KElement.isWildCard(queueEntryID);
        while(it.hasNext()) {
            final String channelID = it.next();
            if(!allURL || !allQE)
            {
                MsgSubscription sub=subscriptionMap.get(channelID);
                if(!allURL && !url.equals(sub.getURL()))
                {
                    continue; // non-matching URL
                }                    
                if(!allQE && !queueEntryID.equals(sub.queueEntry))
                {
                    continue; // non-matching qeid
                }                    
            }               
            removeSubScription(channelID);                
        }
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.ISignalDispatcher#triggerChannel(java.lang.String)
     */
    public void triggerChannel(String channelID,  String queueEntryID, String workStepID, int amount)
    {
        Trigger tNew=new Trigger(queueEntryID, workStepID, channelID, amount);
        synchronized (triggers)
        {
            Trigger t=getTrigger(tNew); // allways add a trigger if amount<0
            
            if(t==null)
            {
                triggers.add(tNew);
            }
            else if(amount>=0 && t.amount>=0) // -1 always forces
            {
                t.amount+=amount; 
            }
            else if(t.amount>0 && amount<0)
            {
                t.amount=amount;
            }
            else if(t.amount<0 && amount<0)
            {
                triggers.add(tNew);               
            }
        }
        if(amount!=0)
        {
            synchronized (mutex)
            {
                mutex.notifyAll();
            }
        }
    }

    /**
     * @param new1
     * @return
     */
    private Trigger getTrigger(Trigger new1)
    {
        if(triggers==null || triggers.size()==0)
            return null;
        for(int i=0;i<triggers.size();i++)
        {
            if(triggers.get(i).equals(new1))
                return triggers.get(i);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.ISignalDispatcher#triggerQueueEntry(java.lang.String)
     */
    public void triggerQueueEntry(String queueEntryID,  String workStepID, int amount)
    {
        Vector<MsgSubscription> v=ContainerUtil.toValueVector(subscriptionMap);
        if (v!=null) {
            int si = v.size();
            for (int i = 0; i < si; i++) 
            {
                MsgSubscription sub=v.get(i);
                if(KElement.isWildCard(sub.queueEntry) || sub.queueEntry.equals(queueEntryID))
                    triggerChannel(sub.channelID, queueEntryID, workStepID, amount);
            }
        }
     }

    /**
     * @param jmfHandler
     */
    public void addHandlers(IJMFHandler jmfHandler)
    {
        jmfHandler.addHandler(this.new StopPersistentChannelHandler());        
    }

    public void shutdown() {
        doShutdown=true;
    }

    /**
     * @return
     */
    private String getSenderID()
    {
        return device==null ? null : device.getDeviceID();
    }

}
