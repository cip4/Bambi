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

/**
 * maps the "incoming" QueueEntries to the "outgoing" QueueEntries <br/>
 * incoming - the MIS who submitted the QueueEntry to the proxy <br/>
 * outgoing - the worker who is processing the QueueEntry
 * @author boegerni
 *
 */
public interface IQueueEntryTracker {

	/**
	 * add a new pair
	 * @param inputQEID  ID of the incoming QueueEntry
	 * @param outputQEID ID of the outgoing QueueEntry
	 * @param deviceID   ID of the device where the output QueueEntry is being processed
	 * @param deviceURL  URL of the device where the output QueueEntry is being processed
	 * @param returnURL  URL of the orginal sender of the QueueEntry
	 */
	public abstract void addEntry(String inputQEID, String outputQEID,
			String deviceID, String deviceURL, String returnURL);

	/**
	 * check whether the incomong QueueEntry has been forwarded to a worker  
	 * @param qeid ID of the incoming QueueEntry
	 * @return true, if the QueueEntry has been forwarded
	 */
	public abstract boolean hasIncomingQE(String qeid);

	/**
	 * get the ID of the incoming QueueEntry matchingthe given outgoing QueueEntry ID  
	 * @param qeid ID of the outgoing QueueEntry
	 * @return the ID of the incoming QueueEntry, null if not found
	 */
	public abstract String getIncomingQEID(String qeid);

	/**
	 * get the output QueueEntryID
	 * @param qeid the ID of the input QueueEntry to look for
	 * @return the ID of the output QueueEntry
	 */
	public abstract String getOutgoingQEID(String qeid);

	/**
	 * remove a QueueEntry by ID
	 * @param qeid the incoming QueueEntry ID
	 */
	public abstract void removeEntry(String qeid);

	/**
	 * the number of QueueEntries forwarded
	 * @return
	 */
	public abstract int count();

	/**
	 * get the ID of the device where the given QueueEntry is being processed 
	 * @param qeid the incoming QueueEntry ID
	 * @return the ID of the device where the given QueueEntry is being processed
	 */
	public abstract String getDeviceID(String qeid);

	/**
	 * get the URL of the device where the given QueueEntry is being processed 
	 * @param qeid the incoming QueueEntry ID
	 * @return the URL of the device where the given QueueEntry is being processed
	 */
	public abstract String getDeviceURL(String qeid);

	/**
	 * get the URL of the of the originator of the given QueueEntry (e.g. the MIS) 
	 * @param qeid the incoming QueueEntry ID
	 * @return the URL of the of the originator of the given QueueEntry (e.g. the MIS) 
	 */
	public abstract String getReturnURL(String qeid);

	/**
	 * get the String representation of a tracked entry
	 * @param qeid the ID of the incoming QueueEntry
	 * @return
	 */
	public abstract String getQueueEntryString(String qeid);

}