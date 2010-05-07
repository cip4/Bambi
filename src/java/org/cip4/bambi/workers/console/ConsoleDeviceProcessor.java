/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2009 The International Cooperation for the Integration of 
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
package org.cip4.bambi.workers.console;

import java.util.Iterator;
import java.util.Vector;

import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.bambi.workers.JobPhase;
import org.cip4.bambi.workers.UIModifiableDeviceProcessor;
import org.cip4.bambi.workers.console.ConsoleDevice.PhaseAction;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.ThreadUtil.MyMutex;

/**
 * parent class for console device processors,
 * 
 * @author Rainer Prosi
 * 
 */
public class ConsoleDeviceProcessor extends UIModifiableDeviceProcessor
{
	private final MyMutex completeMutex;
	private EnumNodeStatus endStatus = null;
	private JobPhase currentPhase;
	protected int currentAction;
	private boolean bStopJob = false;

	/**
	 * added to ensure a consistent hierarchy
	 * 
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * Oct 1, 2009
	 */
	protected class XMLConsoleProcessor extends XMLWorkerProcessor
	{
		/**
		 * @param _root
		 */
		public XMLConsoleProcessor(final KElement _root)
		{
			super(_root);
		}

		/**
		 * @see org.cip4.bambi.core.AbstractDeviceProcessor.XMLDeviceProcessor#fill()
		 */
		@Override
		public KElement fill()
		{
			final KElement proc = super.fill();
			addActions(proc);
			return proc;
		}

		/**
		 * 
		 */
		private void addActions(final KElement processor)
		{
			final Vector<PhaseAction> vActions = getParent().getActions(currentAction);
			if (vActions != null)
			{
				for (final Iterator<PhaseAction> iterator = vActions.iterator(); iterator.hasNext();)
				{
					final PhaseAction phaseAction = iterator.next();
					phaseAction.addToRoot(processor);
				}
			}
		}
	}

	/**
	 * constructor
	 */
	public ConsoleDeviceProcessor()
	{
		super();
		completeMutex = new MyMutex();
		resetPhase();
	}

	/**
	 * 
	 */
	private void resetPhase()
	{
		currentAction = 0;
		currentPhase = null;
		endStatus = null;
	}

	/**
	 * initialize the IDeviceProcessor
	 * @param queueProcessor
	 * @param statusListener
	 * @param devProperties
	 */
	@Override
	public void init(final QueueProcessor queueProcessor, final StatusListener statusListener, final IDeviceProperties devProperties)
	{
		super.init(queueProcessor, statusListener, devProperties);
		_statusListener.getStatusCounter().setPhaseTimeAmounts(false);
	}

	/**
	 * process a queue entry
	 * 
	 * @param n the JDF node to process
	 * @param qe the JDF queueentry that corresponds to this
	 * @return EnumQueueEntryStatus the final status of the queuentry
	 */
	@Override
	public EnumQueueEntryStatus processDoc(final JDFNode n, final JDFQueueEntry qe)
	{
		// reqularly update the counter to avoid retaining double phasetimes too long
		while (!bStopJob)
		{
			ThreadUtil.wait(completeMutex, 10000);
			if (currentPhase != null)
			{
				setNextPhase(currentPhase, null);
			}
		}
		return EnumNodeStatus.getQueueEntryStatus(endStatus);
	}

	/**
	 * proceed to the next job phase
	 * 
	 * @param nextPhase the next job phase to process.<br>
	 * Phase timeToGo is ignored in this class, it is advancing to the next phase solely by doNextPhase().
	 * @param request
	 */
	public void doNextPhase(final JobPhase nextPhase, final ContainerRequest request)
	{
		final JobPhase lastPhase = getCurrentJobPhase();
		setNextPhase(nextPhase, request);
		applyAmounts(lastPhase, request);
	}

	/**
	 * @param phase
	 * @param request
	 */
	private void setNextPhase(final JobPhase phase, final ContainerRequest request)
	{
		final int nextAction = request == null ? currentAction : request.getIntegerParam("pos");
		_statusListener.signalStatus(phase.getDeviceStatus(), phase.getDeviceStatusDetails(), phase.getNodeStatus(), phase.getNodeStatusDetails(), request != null);
		currentPhase = phase;
		endStatus = phase.getNodeStatus();
		currentAction = nextAction;
		// finalize the job
		final Vector<PhaseAction> actions = getParent().getActions(currentAction);
		if (actions == null || actions.isEmpty())
		{
			bStopJob = true;
			ThreadUtil.notifyAll(completeMutex);
		}
	}

	/**
	 * @param lastPhase
	 * @param request
	 */
	private void applyAmounts(final JobPhase lastPhase, final ContainerRequest request)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean finalizeProcessDoc(final EnumQueueEntryStatus qes)
	{
		final boolean b = super.finalizeProcessDoc(qes);
		resetPhase();
		return b;
	}

	/**
	 * 
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#initializeProcessDoc(org.cip4.jdflib.node.JDFNode, org.cip4.jdflib.jmf.JDFQueueEntry)
	 * @param node
	 * @param qe
	 * @return true if successful
	 */
	@Override
	protected boolean initializeProcessDoc(final JDFNode node, final JDFQueueEntry qe)
	{
		final boolean bOK = super.initializeProcessDoc(node, qe);
		endStatus = bOK ? node.getStatus() : EnumNodeStatus.Aborted;
		bStopJob = !bOK;
		if (bOK)
		{
			currentAction = 0;
			currentPhase = new JobPhase();
			currentPhase.setDeviceStatus(EnumDeviceStatus.Idle);
			currentPhase.setNodeStatus(EnumNodeStatus.Waiting);
		}
		return bOK;
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#toString()
	 * @return the string
	 */
	@Override
	public String toString()
	{
		return "Console Worker Device Processor: " + super.toString() + "]";
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#stopProcessing(org.cip4.jdflib.core.JDFElement.EnumNodeStatus)
	 */
	@Override
	public EnumNodeStatus stopProcessing(final EnumNodeStatus newStatus)
	{
		bStopJob = true;
		endStatus = newStatus;
		ThreadUtil.notifyAll(completeMutex);

		return endStatus;
	}

	/**
	 * @see org.cip4.bambi.workers.UIModifiableDeviceProcessor#getCurrentJobPhase()
	 */
	@Override
	public JobPhase getCurrentJobPhase()
	{
		return currentPhase;
	}

	/**
	 * @param root
	 * @return
	 */
	@Override
	protected XMLDeviceProcessor getXMLDeviceProcessor(final KElement root)
	{
		return this.new XMLConsoleProcessor(root);
	}

	/**
	 * @return the _parent
	 */
	@Override
	public ConsoleDevice getParent()
	{
		return (ConsoleDevice) _parent;
	}

}
