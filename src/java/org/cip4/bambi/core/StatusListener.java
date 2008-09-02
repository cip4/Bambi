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
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
package org.cip4.bambi.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.SignalDispatcher.Trigger;
import org.cip4.bambi.core.queues.QueueEntry;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFResourceQuParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.NodeIdentifier;
import org.cip4.jdflib.util.StatusCounter;

/**
 * @author Rainer Prosi
 * 
 */
public class StatusListener
{

	private static Log log = LogFactory.getLog(StatusListener.class.getName());
	private final SignalDispatcher dispatcher;
	private SignalDispatcher rootDispatcher = null;
	protected StatusCounter theCounter;
	private JDFNode currentNode = null;
	private long lastSave = 0;

	/**
	 * 
	 * @param dispatch
	 * @param deviceID
	 */
	public StatusListener(SignalDispatcher dispatch, String deviceID)
	{
		dispatcher = dispatch;
		theCounter = new StatusCounter(null, null, null);
		theCounter.setDeviceID(deviceID);
	}

	/**
	 * @param msgType the type of messages to flush our, null if any
	 */
	public void flush(String msgType)
	{
		Trigger[] t = dispatcher.triggerQueueEntry(theCounter.getQueueEntryID(), theCounter.getNodeIDentifier(), -1, msgType);
		dispatcher.flush();
		if (rootDispatcher != null)
		{
			rootDispatcher.triggerQueueEntry(theCounter.getQueueEntryID(), theCounter.getNodeIDentifier(), -1, msgType);
			rootDispatcher.flush();
		}
		Trigger.waitQueued(t, 2000);
	}

	/**
	 * update the status information by starting a new phase all amounts that
	 * have been accumulated are linked to the prior phase should be called
	 * after all amounts have been appropriately set
	 * 
	 * @param deviceStatus
	 * @param deviceStatusDetails
	 * @param nodeStatus
	 * @param nodeStatusDetails
	 * @param forceOut
	 *            forces writing by any generator, even if the status remains
	 *            the same and the trigger would not call for a write
	 */
	public void signalStatus(EnumDeviceStatus deviceStatus, String deviceStatusDetails, EnumNodeStatus nodeStatus, String nodeStatusDetails, boolean forceOut)
	{
		if (theCounter == null)
		{
			log.error("updating null status tracker");
			return;
		}
		boolean bMod = theCounter.setPhase(nodeStatus, nodeStatusDetails, deviceStatus, deviceStatusDetails);
		if (bMod || forceOut)
		{
			flush("Status");
		}
	}

	/**
	 * set event, append the Event element and optionally the comment<br/>
	 * overwrites existing values
	 * 
	 * @param eventID
	 *            Event/@EventID to set
	 * @param eventValue
	 *            Event/@EventValue to set
	 * @param comment
	 *            the comment text, if null no comment is set
	 */
	public void setEvent(String eventID, String eventValue, String comment)
	{
		if (theCounter == null)
		{
			log.error("updating null status tracker");
			return;
		}
		theCounter.setEvent(eventID, eventValue, comment);
		flush("Notification");
	}

	/**
	 * updates the amount for a given resource the amounts are collected but not
	 * signaled until @see signalStatus() is called
	 * 
	 * @param resID the resource id of the tracked resource
	 * @param good the number of good copies
	 * @param waste the number of waste copies, 0  specifies that waste should be ignored
	 */
	public void updateAmount(String resID, double good, double waste)
	{
		if (theCounter == null)
			return;
		theCounter.addPhase(resID, good, waste, true);
		if (good + waste > 0)
		{
			dispatcher.triggerQueueEntry(theCounter.getQueueEntryID(), theCounter.getNodeIDentifier(), (int) (good + waste), null);
		}
		saveJDF(12345);
	}

	/**
	 * set the total amount of a given resource by the value specified
	 * @param percent the percent completed
	 * 
	 * 
	 */
	public void setPercentComplete(double percent)
	{
		if (theCounter == null)
			return;
		theCounter.setPercentComplete(percent);
		saveJDF(12345);
	}

	/**
	 * incrementally update the total amount of a given resource by the value specified
	 * @param percent the percent completed
	 * 
	 * 
	 */
	public void updatePercentComplete(double percent)
	{
		if (theCounter == null)
			return;
		theCounter.updatePercentComplete(percent);
		saveJDF(12345);
	}

	/**
	 * update the total amount of a given resource to the value specified
	 * 
	 * @param resID
	 *            the resource id
	 * @param amount
	 *            the total amount top set
	 * @param waste
	 *            if true, this is waste, else it is good
	 * 
	 */
	public void updateTotal(String resID, double amount, boolean waste)
	{
		if (theCounter == null)
			return;
		theCounter.setTotal(resID, amount, waste);
		if (amount > 0)
		{
			dispatcher.triggerQueueEntry(theCounter.getQueueEntryID(), theCounter.getNodeIDentifier(), (int) amount, null);
		}
		saveJDF(12345);
	}

	/**
	 * replace the currently tracked node with node used to overwrite the
	 * current node with a returned node, e.g from a proxy device
	 * 
	 * @param node
	 *            the JDFNode used to overwrite the local JDF node
	 */
	public void replaceNode(JDFNode node)
	{
		if (node != null)
		{
			String location = currentNode == null ? null : currentNode.getOwnerDocument_JDFElement().getOriginalFileName();
			currentNode = node;
			if (location != null)
				currentNode.getOwnerDocument_JDFElement().setOriginalFileName(location);
			saveJDF(-1);
		}
	}

	/**
	 * setup the map of queueentryid and node
	 * 
	 * @param queueEntryID
	 *            the queueentryid is associated to the node if
	 *            {@link QueueEntry}==null, the entire list is cleared
	 * @param vPartMap
	 *            the vector of partitions that are being tracked
	 * @param trackResourceID
	 *            the id of the "major" resource to be counted for phasetimes
	 * @param node
	 *            the jdf node that will be processed. this may be a group node
	 *            with additional sub nodes if node==null the queueentryid is
	 *            removed from the map
	 */
	public void setNode(String queueEntryID, JDFNode node, VJDFAttributeMap vPartMap, String trackResourceID)
	{
		String oldQEID = theCounter.getQueueEntryID();
		theCounter.writeAll(); // write all stuff in the counter to the node
		saveJDF(-1);
		boolean bSame = currentNode == node;
		currentNode = node;
		if (!bSame)
		{
			saveJDF(-1);
		}

		if (!KElement.isWildCard(oldQEID))
		{
			log.info("removing subscription for: " + oldQEID);
			dispatcher.removeSubScriptions(oldQEID, "*");
		}

		theCounter.setActiveNode(node, vPartMap, null);
		theCounter.setFirstRefID(trackResourceID);
		theCounter.setTrackWaste(trackResourceID, true); // always track waste
		theCounter.setQueueEntryID(queueEntryID);
		while (node != null)
		{
			log.info("adding subscription for: " + queueEntryID);
			dispatcher.addSubscriptions(node, queueEntryID);
			node = node.getParentJDF();
		}
	}

	/**
	 * save the currently active jdf
	 * 
	 * @param timeSinceLast
	 *            milliseconds time to leave between saves
	 */
	private void saveJDF(int timeSinceLast)
	{
		if (currentNode == null)
			return;
		if (System.currentTimeMillis() - lastSave > timeSinceLast)
		{
			final JDFDoc ownerDoc = currentNode.getOwnerDocument_JDFElement();
			if (ownerDoc != null && ownerDoc.getOriginalFileName() != null)
			{
				ownerDoc.write2File((String) null, 0, true);
				lastSave = System.currentTimeMillis();
			}
		}
	}

	/**
	 * get the device status
	 * 
	 * @return the device status. <br/> Returns EnumDeviceStatus.Idle if the
	 *         StatusCounter is null. <br/> Returns EnumDeviceStatus.Unknown, if
	 *         the StatusListener was unable to retrieve the status from the
	 *         StatusCounter.
	 */
	public EnumDeviceStatus getDeviceStatus()
	{
		if (theCounter == null)
		{
			return EnumDeviceStatus.Idle;
		}

		JDFDoc docJMF = theCounter.getDocJMFPhaseTime();
		JDFResponse r = docJMF == null ? null : docJMF.getJMFRoot().getResponse(-1);
		JDFDeviceInfo di = r == null ? null : r.getDeviceInfo(0);
		return di == null ? EnumDeviceStatus.Idle : di.getDeviceStatus();
	}

	/**
	 * shut down this StatusListener
	 */
	public void shutdown()
	{
		// not needed right now, retaining method for future compatability
	}

	/**
	 * get the StatusCounter
	 * 
	 * @return the StatusCounter
	 */
	public StatusCounter getStatusCounter()
	{
		return theCounter;
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return String - the string representation
	 */
	@Override
	public String toString()
	{
		return "[StatusListner - counter: " + theCounter + "\n Current Node: " + currentNode;
	}

	/**
	 * @param inputMessage
	 * @return return true if inputMessage applies to this Listener
	 */
	public boolean matchesQuery(JDFMessage inputMessage)
	{
		if (inputMessage == null)
			return false;
		if (!(inputMessage instanceof JDFQuery))
			return false;
		JDFQuery q = (JDFQuery) inputMessage;
		if (EnumType.Status.equals(q.getEnumType()))
		{
			JDFStatusQuParams sqp = q.getStatusQuParams();
			if (sqp == null)
				return true;
			return matchesIDs(sqp.getJobID(), sqp.getJobPartID(), sqp.getQueueEntryID());
		}
		else if (EnumType.Resource.equals(q.getEnumType()))
		{
			JDFResourceQuParams rqp = q.getResourceQuParams();
			if (rqp == null)
				return true;
			return matchesIDs(rqp.getJobID(), rqp.getJobPartID(), rqp.getQueueEntryID());
		}
		return true;
	}

	/**
	 * @param jobID 
	 * @param jobPartID 
	 * @param queueEntryID 
	 * @return
	 */
	private boolean matchesIDs(String jobID, String jobPartID, String queueEntryID)
	{
		NodeIdentifier niIn = new NodeIdentifier(jobID, jobPartID, null);
		NodeIdentifier niCurrent = currentNode == null ? new NodeIdentifier() : currentNode.getIdentifier();
		if (!niIn.matches(niCurrent) && !niCurrent.matches(niIn))
			return false;

		return true;
	}

	/**
	 * @param _rootDispatcher
	 */
	public void setRootDispatcher(SignalDispatcher _rootDispatcher)
	{
		this.rootDispatcher = _rootDispatcher;
	}

	/**
	 * @param resID the resource id of the tracked resource
	 * @param deltaAmount the number of good copies
	 * @param deltaWaste the number of waste copies, 0  specifies that waste should be ignored
	 * @param amount
	 * @param waste
	 */
	public void setAmount(String resID, double deltaAmount, double deltaWaste, double amount, double waste)
	{
		if (theCounter == null)
			return;
		theCounter.setPhase(resID, deltaAmount, deltaWaste);
		if (amount >= deltaAmount)
			theCounter.setTotal(resID, amount, false);
		if (waste >= deltaWaste)
			theCounter.setTotal(resID, waste, true);
	}

}
