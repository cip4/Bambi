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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QueueEntryTracker implements IQueueEntryTracker {
	protected static class OutgoingQE implements Serializable {
		private static final long serialVersionUID = 4586978931L;
		private String _qeid=null;
		private String _deviceID=null;
		private String _deviceURL=null;
		
		public OutgoingQE(String qeid, String deviceID,String deviceURL) {
			_qeid=qeid;
			_deviceID=deviceID;
			_deviceURL=deviceURL;
		}
		
		public String getQueueEntryID() {
			return _qeid;
		}
		
		public String getDeviceID() {
			return _deviceID;
		}
		
		public String getDeviceURL() {
			return _deviceURL;
		}
		
		@Override
		public String toString() {
			return "[ outgoing QueueEntryID="+_qeid+", device ID="+_deviceID
				+", device URL="+_deviceURL+" ]";
		}
	}
	
	Map<String, OutgoingQE> _tracker = null;
	protected static final Log log = LogFactory.getLog(QueueEntryTracker.class.getName());
	private String _configFile=null;
	
	/**
	 * constructor
	 * @param configPath the path to the config folder of the web application
	 * @param deviceID TODO
	 */
	public QueueEntryTracker(String configPath, String deviceID) {
		_configFile=configPath+"tracker"+deviceID+".bin";
		if (loadTracker()==true) {
			log.info("loaded QueueEntryTracker from "+_configFile);
		} else {
			_tracker = new HashMap<String, OutgoingQE>();
			log.info("initialised new QueueEntryTracker");
		}
	}
	
	/**
	 * get QueueEntry by input QueueEntry ID
	 * @param qeid incoming QueueEntry ID
	 * @return the {@link OutgoingQE}
	 */
	private OutgoingQE getOutputQE(String qeid) {
		Object value = _tracker.get(qeid);
		if (value==null) {
			return null;
		}
		return (OutgoingQE)value;
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#addEntry(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addEntry(String inputQEID, String outputQEID, String deviceID, String deviceURL) {
		OutgoingQE qe = new OutgoingQE(outputQEID,deviceID,deviceURL);
		_tracker.put(inputQEID, qe);
		persist();
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#hasIncomingQE(java.lang.String)
	 */
	public boolean hasIncomingQE(String qeid) {
		return _tracker.containsKey(qeid);
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#getIncomingQEID(java.lang.String)
	 */
	public String getIncomingQEID(String qeid) {
		Collection<String> keys = _tracker.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String inQEID = it.next().toString();
			OutgoingQE out = _tracker.get(inQEID);
			if (out.getQueueEntryID().equals(qeid)) {
				return inQEID;
			}
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#getOutgoingQEID(java.lang.String)
	 */
	public String getOutgoingQEID(String qeid) {
		OutgoingQE qe = getOutputQE(qeid);
		if (qe==null) {
			return null;	
		}
		return qe.getQueueEntryID();
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#removeEntry(java.lang.String)
	 */
	public void removeEntry(String qeid) {
		_tracker.remove(qeid);
		persist();
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#count()
	 */
	public int countTracked() {
		return _tracker.size();
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#getDeviceID(java.lang.String)
	 */
	public String getDeviceID(String qeid) {
		OutgoingQE qe = getOutputQE(qeid);
		return qe!=null ? qe.getDeviceID() : null;
	}

	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#getDeviceURL(java.lang.String)
	 */
	public String getDeviceURL(String qeid) {
		OutgoingQE qe = _tracker.get(qeid);
		return qe==null ? null : qe.getDeviceURL();
	}
	
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#getQueueEntryString(java.lang.String)
	 */
	public String getQueueEntryString(String qeid) {
		OutgoingQE qe = _tracker.get(qeid);
		String ret="[ key="+qeid+", value=";
		if (qe!=null) {
			ret += qe.toString();
		}
		ret += " ]";
		return ret;
	}
	
	/**
	 * persist the QueueEntrytracker to a file
	 * @return true is successful, false if not
	 */
	private boolean persist() {
		boolean succeeded=true;
		OutputStream fos = null; 
		try { 
		  fos = new FileOutputStream( _configFile ); 
		  ObjectOutputStream o = new ObjectOutputStream( fos ); 
		  o.writeObject( _tracker );  
		} catch ( IOException e ) { 
			log.error("failed to persist QueueEntryTracker to "+_configFile);
			succeeded=false;
		} finally { 
			try { 
				if (fos!=null)
					fos.close(); 
			} catch ( Exception e ) { 
				log.error("failed to persist QueueEntryTracker to "+_configFile);
				succeeded=false;
			} 
		}

		 return succeeded;
	}
	
	/**
	 * load the tracker from the config file
	 * @return true if successful, false if not
	 */
	@SuppressWarnings("unchecked")
	private boolean loadTracker() {
		if ( !new File(_configFile).canRead() ) {
			return false;
		}
		
		boolean succeeded=true;
		InputStream fis = null; 
		try { 
		  fis = new FileInputStream( _configFile ); 
		  ObjectInputStream o = new ObjectInputStream( fis ); 
		  _tracker = (Map<String, OutgoingQE>) o.readObject();  
		}  catch ( IOException e ) { 
			log.error("failed to load QueueEntryTracker from "+_configFile); 
			succeeded=false;
		}  catch ( ClassNotFoundException e ) { 
			log.error( "failed to import file '"+_configFile+"'" );
			succeeded=false;
		} finally { 
			try { 
				if (fis!=null)
					fis.close(); 
			} catch ( Exception e ) {
				log.error("failed to load QueueEntryTracker from "+_configFile);
				succeeded=false;
			} 
		}
		
		return succeeded;
	}
	
}