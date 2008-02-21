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
package org.cip4.bambi.core.messaging;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.ISignalDispatcher;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.ifaces.IJMFSubscribable;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessageService;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;


/**
 *
 * @author  rainer
 */
public class JMFHandler implements IMessageHandler, IJMFHandler
{

	protected static final Log log = LogFactory.getLog(JMFHandler.class.getName());
	protected class MessageType
    {
	    public EnumType type;
        public EnumFamily family;
        /**
         * @param typ
         * @param family2
         */
        public MessageType(EnumType typ, EnumFamily _family)
        {
            type=typ;
            family=_family;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object arg0)
        {
            if(!(arg0 instanceof MessageType))
                return false;
            MessageType mt=(MessageType)arg0;
            if(type==null && mt.type!=null)
                return false;
            if(family==null && mt.family!=null)
                return false;
            if(family==null&&type==null)
                return true;
            return family.equals(mt.family) && type.equals(mt.type);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            return (type==null?0: type.hashCode()) + (family==null ? 0 : family.hashCode());
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "MessageType :"+type+" "+family;
        }
    }
	/**
	 * 
	 */
	private static final long serialVersionUID = -8902151736245089033L;
	protected HashMap<MessageType,IMessageHandler> messageMap; // key = type , value = IMessageHandler
//TODO handle subscriptions
    protected HashMap<EnumType,IMessageHandler> subscriptionMap; // key = type , value = subscriptions handled
    ISignalDispatcher _signalDispatcher;
	/**
	 * 
	 * handler for the knownmessages query
	 */
	protected class KnownMessagesHandler implements IMessageHandler
	{

		/* (non-Javadoc)
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		public boolean handleMessage(JDFMessage m, JDFResponse resp)
		{
			if(m==null || resp==null)
			{
				return false;
			}
			log.debug("Handling"+m.getType());
			EnumType typ=m.getEnumType();
			if(EnumType.KnownMessages.equals(typ))
				return handleKnownMessages(m, resp);

			return false;
		}
		/**
		 * @return
		 */
		private boolean handleKnownMessages(JDFMessage m, JDFMessage resp)
		{
			if(m==null)
				return false;
			if(!EnumFamily.Query.equals(m.getFamily()))
				return false;

			Iterator<MessageType> it=messageMap.keySet().iterator();
            HashMap<EnumType,Vector<EnumFamily>> htf=new  HashMap<EnumType,Vector<EnumFamily>>();
			while(it.hasNext())
			{
				MessageType typ=it.next();
                if(htf.get(typ.type)==null)
                {
                    Vector<EnumFamily> v=new Vector<EnumFamily>();
                    v.add(typ.family);
                    htf.put(typ.type, v);
                }
                else
                {
                    Vector<EnumFamily> v=htf.get(typ.type);
                    v.add(typ.family);                   
                }
            }
            
            Iterator<Entry<EnumType, Vector<EnumFamily>>> iTyp=htf.entrySet().iterator();
            while(iTyp.hasNext())
            {
                EnumType typ=iTyp.next().getKey();
				log.debug("Known Message: "+typ.getName());
				JDFMessageService ms=resp.appendMessageService();
				ms.setType(typ);
				ms.setFamilies(htf.get(typ));     
				if(subscriptionMap.get(typ)!=null)
					ms.setPersistent(true);
			}
			return true;
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
			return EnumType.KnownMessages;
		}
	}

	public JMFHandler()
	{
		super();
		messageMap=new HashMap<MessageType, IMessageHandler>();
		subscriptionMap=new HashMap<EnumType, IMessageHandler>();
		addHandler( this.new KnownMessagesHandler() );
        _signalDispatcher=null;
	}

	public JMFHandler(JMFHandler oldHandler)
	{
		super();
		messageMap=new HashMap<MessageType, IMessageHandler>(oldHandler.messageMap);
		subscriptionMap=new HashMap<EnumType, IMessageHandler>(oldHandler.subscriptionMap);
		addHandler( this.new KnownMessagesHandler());
        _signalDispatcher=oldHandler._signalDispatcher;        
	}

	/**
	 * add a message handler
	 * @param handler the handler associated with the event
	 */
	public void addHandler(IMessageHandler handler)
	{
		EnumType typ=handler.getMessageType();
		EnumFamily[] families = handler.getFamilies();
        for(int i=0; i<families.length;i++)
            messageMap.put(new MessageType(typ,families[i]), handler);
	}

	/**
	 * @param typ the message type
	 * @param family the family
	 * @return the handler, null if none exists
	 */
	private IMessageHandler getHandler(EnumType typ, EnumFamily family)
	{
		return messageMap.get(new MessageType(typ,family));
	}
	
	public void addSubscriptionHandler(EnumType typ, IMessageHandler handler)
	{
		subscriptionMap.put(typ, handler);
	}
	
	/**
	 * the big processing dispatcher
	 * 
	 * @param doc the JDFDoc holding the JMF which is to be processed
	 * @return the JDFDoc holding the JMF response
	 */
	public JDFDoc processJMF(JDFDoc doc)
	{
		JDFJMF jmf=doc.getJMFRoot();
		JDFJMF jmfResp=jmf.createResponse();
		VElement vMess=jmf.getMessageVector(null,null);
		for(int i=0;i<vMess.size();i++)
		{
			JDFMessage m=(JDFMessage) vMess.elementAt(i);
			String id=m.getID();

			JDFResponse mResp=(JDFResponse) (id==null ? null : jmfResp.getChildWithAttribute(ElementName.RESPONSE,AttributeName.REFID, null, id, 0, true));
			if(mResp==null)
            {
				log.error("no response provided ??? "+id+" "+jmfResp);
            }
 			handleMessage(m, mResp);
            if(_signalDispatcher!=null)
                _signalDispatcher.findSubscription(m, mResp);
            
            if(m instanceof JDFSignal && mResp!=null)
            {
                int retCode=mResp.getReturnCode();
                if(retCode==0)
                    mResp.deleteNode();
            }
		}   
		return jmfResp.getOwnerDocument_JDFElement();
	}



    /**
	 * standard handler for unimplemented messages
	 * @param m
	 * @param resp
	 */
	private void unhandledMessage(JDFMessage m, JDFResponse resp)
	{
		log.info("unhandled Message: "+m.getType());
		errorResponse(resp, "Message not implemented: "+m.getType()+"; Family: "+m.getFamily().getName(), 5);        
	}

    /**
     * standard error message creator
     * 
     * @param resp the response to make an error
     * @param text the explicit error text
     * @param rc the jmf response returncode
     */
	public static void errorResponse(JDFResponse resp, String text, int rc)
	{
	    if(resp!=null)
	    {
	        resp.setReturnCode(rc);
	        resp.setErrorText(text);
	    }
	    log.warn("JMF error: rc="+rc+" "+text);
	}

	/** 
	 * we do not call these for ourselves...
	 */
	public EnumFamily[] getFamilies()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.cip4.bambi.IMessageHandler#getMessageType()
	 */
	public EnumType getMessageType()
	{
		return null;
	}

	/**
	 * the handler implements itself as a generic handler
	 */
	public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
	{
		if(inputMessage==null)
			return false;
		final EnumFamily family=inputMessage.getFamily();
		final EnumType typ=inputMessage.getEnumType();
		final IMessageHandler handler=getHandler(typ, family);
		boolean handled=handler!=null;
		
        if(handler!=null)
			handled=handler.handleMessage(inputMessage, response);
		if(!handled)
		{
			unhandledMessage(inputMessage,response);
		}
		return handled;
	}
	
	@Override
	public String toString() {
		String msgMap="MessageMap (size="+messageMap.size()+")=["+messageMap.toString()+"]";
		String subMap="SubsriptionMap (size="+subscriptionMap.size()+")=["+subscriptionMap.toString()+"]";
        return "JMFHandler: "+msgMap+", "+subMap;
    }

    /**
     * @param signalDispatcher
     */
    public void setDispatcher(ISignalDispatcher signalDispatcher)
    {
        _signalDispatcher=signalDispatcher;
        
    }

}
