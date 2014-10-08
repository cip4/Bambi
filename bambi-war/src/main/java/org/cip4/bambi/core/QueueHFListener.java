/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2014 The International Cooperation for the Integration of 
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
/**
 * 
 */
package org.cip4.bambi.core;

import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.hotfolder.QueueHotFolderListener;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen
 * 
 */
public class QueueHFListener extends BambiLogFactory implements QueueHotFolderListener
{
	/**
	 * 
	 */
	private IConverterCallback _callBack = null;
	private final QueueProcessor queueProc;

	/**
	 * @param qProc
	 * @param callBackClass
	 */
	public QueueHFListener(final QueueProcessor qProc, final IConverterCallback callBackClass)
	{
		queueProc = qProc;
		_callBack = callBackClass;
	}

	/**
	 * @see org.cip4.jdflib.util.hotfolder.QueueHotFolderListener#submitted(org.cip4.jdflib.jmf.JDFJMF)
	 * @param submissionJMF
	 */
	@Override
	public boolean submitted(final JDFJMF submissionJMF)
	{
		log.info("HFListner:submitted");
		final JDFCommand command = submissionJMF.getCommand(0);

		if (_callBack != null)
		{
			_callBack.prepareJMFForBambi(submissionJMF.getOwnerDocument_JDFElement());
		}

		final JDFQueueSubmissionParams qsp = command.getQueueSubmissionParams(0);

		JDFDoc doc = qsp.getURLDoc();
		if (doc == null)
		{
			log.warn("could not process JDF File at URL: " + qsp.getURL());
			return false;
		}
		else
		{
			if (_callBack != null)
			{
				doc = _callBack.prepareJDFForBambi(doc);
			}

			final JDFQueueEntry qe = queueProc.addEntry(command, null, doc);
			if (qe == null)
			{
				log.warn("_theQueue.addEntry returned null");
				return false;
			}
			queueProc.updateCache(qe, qe.getQueueEntryID());
			return waitForSubmission(qe);
		}
	}

	/**
	 * @param qe
	 * @return
	 */
	private boolean waitForSubmission(final JDFQueueEntry qe)
	{
		int iLoop = 1;
		long t0 = System.currentTimeMillis();
		while (iLoop++ < 42)
		{
			IQueueEntry iqeNew = queueProc.getIQueueEntry(qe, true);
			JDFQueueEntry qeNew = iqeNew == null ? null : iqeNew.getQueueEntry();
			if (queueProc.wasSubmitted(qeNew))
			{
				if (iLoop > 10)
				{
					t0 = System.currentTimeMillis() - t0;
					t0 /= 1000;
					log.info("waited " + t0 + " seconds for response queue submission response. qeID=" + iqeNew.getQueueEntryID());
				}
				return true;
			}
			if (!ThreadUtil.sleep(iLoop * iLoop * 42))
			{
				return false;
			}
		}
		log.warn("no queueentry response in reasonable time: " + qe.getQueueEntryID());
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "QueueHFListener [queueProc=" + queueProc + "]";
	}
}