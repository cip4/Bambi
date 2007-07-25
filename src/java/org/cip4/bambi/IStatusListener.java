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
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
package org.cip4.bambi;

import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.node.JDFNode;


/**
 * @author prosirai
 *
 */
public interface IStatusListener
{
    /**
     * updates the amount for a given resource
     * the amounts are collected but not signalled until setstatus is called
     * 
     * @param queueEntryID the queuentry id of the process stepp being processed
     * @param workstepID the workstep id of the process stepp being processed, 
     * set to null if the all partitions of the root are being processed
     * @param resID the resource id of the tracked resource
     * @param good the number of good copies
     * @param waste the number of waste copies, negative values specify that waste should be ignored
     */
    void updateAmount(String queueEntryID,String workstepID, String resID, double good, double waste);
    
    /**
     * update the status information by starting a new phase
     * all amounts that have been accumulated are linked to the prior phase
     * should be called after all amounts have been appropriately set
     * @param queueEntryID the queuentry id of the process stepp being processed
     * @param workstepID the workstep id of the process stepp being processed, 
     * set to null if the all partitions of the root are being processed
     * @param deviceStatus
     * @param deviceStatusDetails
     * @param nodeStatus
     * @param nodeStatusDetails
     */
    void signalStatus(String queueEntryID, String workstepID, EnumDeviceStatus deviceStatus, String deviceStatusDetails, EnumNodeStatus nodeStatus, String nodeStatusDetails);

    /**
     * setup the map of queueentryid and node
     * 
     * @param queueEntryID the queueentryid is associated to the node
     * if {@link QueueEntry}==null, the entire list is cleared
     * @param workStepID the workstep id that is being tracked
     * @param vPartMap the vector of partitions taht are being tracked
     * @param trackResourceID the id of the "major" resource to be counted for phasetimes
     * @param node the jfd node that will be processed. this may be a group node with 
     * additional sub nodes
     * if node==null the queuentryid is removed from the map
     */
     public void setNode(String queueEntryID, String workStepID, JDFNode node, VJDFAttributeMap vPartMap, String trackResourceID);
}
