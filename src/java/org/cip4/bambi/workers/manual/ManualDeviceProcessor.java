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

package org.cip4.bambi.workers.manual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.IStatusListener;
import org.cip4.bambi.core.queues.IQueueProcessor;
import org.cip4.bambi.workers.core.AbstractBambiDeviceProcessor;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.jmf.JDFQueueEntry;

/**
 * simulates processing of manually and on the fly designed job phases
 * ("now run this phase, stop, and now do that")
 * 
 * @author boegerni
 *
 */
public class ManualDeviceProcessor extends AbstractBambiDeviceProcessor
{
	private static Log log = LogFactory.getLog(ManualDeviceProcessor.class.getName());
	private static final long serialVersionUID = -384123589645081254L;
	private boolean doFinalizeQE = false;
	private boolean doNextPhase = false;
	private JobPhase firstPhase=null;
	
	public ManualDeviceProcessor(IQueueProcessor queueProcessor, 
			IStatusListener statusListener, String deviceID, String appDir) {
		super(queueProcessor, statusListener, deviceID, appDir);
		initManualDeviceProcessor();
	}
	
	private void initManualDeviceProcessor() {
		firstPhase = new JobPhase();
		firstPhase.deviceStatus=EnumDeviceStatus.Idle;
		firstPhase.deviceStatusDetails="Waiting";
		firstPhase.nodeStatus=EnumNodeStatus.Waiting;
		firstPhase.nodeStatusDetails="Waiting";
	}
		
	@Override
	public EnumQueueEntryStatus processDoc(JDFDoc doc, JDFQueueEntry qe) {
		super.processDoc(doc, qe);
		
        doFinalizeQE = false;
		while ( !doFinalizeQE ) {
			if ( _jobPhases==null || _jobPhases.isEmpty() ) {
				_jobPhases.add(firstPhase);
			}
			
			if (doNextPhase) {
				_jobPhases.remove(0);
				doNextPhase = false;
			}
			JobPhase phase = _jobPhases.get(0);
			
			if (phase == null) {
				log.fatal("job phase is null");
				return EnumQueueEntryStatus.Aborted;
			}
			
			log.info("processing new job phase: "+phase.toString());
			_statusListener.signalStatus(phase.deviceStatus, phase.deviceStatusDetails, 
					phase.nodeStatus,phase.nodeStatusDetails);
			
			while ( !doNextPhase ) {
				if (doFinalizeQE) {
					break;
				}
					
				try {
					int reqSize=_updateStatusReqs.size();
					if (reqSize>0) {
						for (int reqNo=0;reqNo<reqSize;reqNo++) {
							ChangeQueueEntryStatusRequest request=_updateStatusReqs.get(reqNo);
							if ( !request.queueEntryID.equals(qe.getQueueEntryID()) ) {	
								_updateStatusReqs.remove(reqNo);
								log.error("failed to change status of QueueEntry, it is not running");
							} else if (request.newStatus.equals(EnumQueueEntryStatus.Suspended)) {
								_updateStatusReqs.remove(reqNo);
								return suspendQueueEntry(qe,0, 0);
							} else if (request.newStatus.equals(EnumQueueEntryStatus.Aborted)) {
								_updateStatusReqs.remove(reqNo);
								doNextPhase(firstPhase);
								return abortQueueEntry();
							}
						}					
					} else {
						Thread.sleep(1000);
					}
					_statusListener.updateAmount(_trackResourceID, phase.Output_Good, phase.Output_Waste);
					
				} catch (InterruptedException e) {
					log.warn("interrupted while sleeping");
				}
			}

		}
		
		// processing has finished
		doNextPhase(firstPhase);
		return finalizeProcessDoc();
	}
	
	/**
	 * proceed to the next job phase
	 * @param newPhase the next job phase to process.<br>
	 * Phase duration is ignored in this class, it is advancing to the next phase 
	 * solely by doNextPhase().
	 */
	public void doNextPhase(JobPhase nextPhase) {
		_jobPhases.add(nextPhase);
		doNextPhase = true;
	}
	
	/**
	 * finish processing the current QueueEntry
	 */
	public void finalizeQueueEntry() {
		doFinalizeQE = true;
	}
	
	@Override
	public JobPhase getCurrentJobPhase() {
		if ( _jobPhases != null && _jobPhases.size() > 0)
			return _jobPhases.get(0);
		return null;
	}

}