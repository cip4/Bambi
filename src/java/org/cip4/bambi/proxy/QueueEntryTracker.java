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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QueueEntryTracker implements IQueueEntryTracker {
	// TODO persist (SOAP?)
	protected static class OutgoingQE {
		private String _qeid=null;
		private String _deviceID=null;
		private String _deviceURL=null;
		private String _returnURL=null;
		
		public OutgoingQE(String qeid, String deviceID,String deviceURL, String returnURL) {
			_qeid=qeid;
			_deviceID=deviceID;
			_deviceURL=deviceURL;
			_returnURL=returnURL;
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
		
		public String getReturnURL() {
			return _returnURL;
		}
		
		public String toString() {
			return "[ outgoing QueueEntryID="+_qeid+", device ID="+_deviceID
				+", device URL="+", returnURL="+_returnURL+" ]";
		}
	}
	
	Map<String, OutgoingQE> _tracker = null;
	
	/**
	 * constructor
	 */
	public QueueEntryTracker() {
		_tracker = new HashMap<String, OutgoingQE>();
	}
	
	/**
	 * get QueueEntry by input QueueEntry ID
	 * @param qeid incoming QueueEntry ID
	 * @return the {@link OutgoingQE}
	 */
	private OutgoingQE getOutputQE(String qeid) {
		Object value = _tracker.get(qeid);
		if (value!=null) {
			return (OutgoingQE)value;
		} else {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#addEntry(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addEntry(String inputQEID, String outputQEID, String deviceID, String deviceURL, String returnURL) {
		OutgoingQE qe = new OutgoingQE(outputQEID,deviceID,deviceURL,returnURL);
		_tracker.put(inputQEID, qe);
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
		if (qe!=null) {
			return qe.getQueueEntryID();
		} else {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#removeEntry(java.lang.String)
	 */
	public void removeEntry(String qeid) {
		_tracker.remove(qeid);
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#count()
	 */
	public int count() {
		return _tracker.size();
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#getDeviceID(java.lang.String)
	 */
	public String getDeviceID(String qeid) {
		OutgoingQE qe = getOutputQE(qeid);
		if (qe!=null) {
			return qe.getDeviceID();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#getDeviceURL(java.lang.String)
	 */
	public String getDeviceURL(String qeid) {
		OutgoingQE qe = _tracker.get(qeid);
		if (qe!=null) {
			return qe.getDeviceURL();
		} else {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.proxy.IQueueEntryTracker#getReturnURL(java.lang.String)
	 */
	public String getReturnURL(String qeid) {
		OutgoingQE qe = _tracker.get(qeid);
		if (qe!=null) {
			return qe.getReturnURL();
		} else {
			return null;
		}
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
	
}