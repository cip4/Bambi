/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2018 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This product includes software developed by the The International Cooperation for
 * the Integration of Processes in Prepress, Press and Postpress (www.cip4.org)" Alternately, this acknowledgment may appear in the software itself, if and wherever such third-party acknowledgments
 * normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of Processes in Prepress, Press and Postpress" must not be used to endorse or promote products derived from this software
 * without prior written permission. For written permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4", nor may "CIP4" appear in their name, without prior written permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE. ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals on behalf of the The International Cooperation for the Integration of Processes in Prepress, Press and Postpress and was
 * originally based on software copyright (c) 1999-2001, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.bambi.core.queues;

import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.node.JDFNode;

/**
 * simple QueueEntry / JDF pair
 *
 * @author Rainer Prosi
 *
 */
public class QueueEntry extends BambiLogFactory implements IQueueEntry
{

	private JDFNode _theNode;
	private JDFQueueEntry _theQueueEntry;

	/**
	 * @param node
	 * @param qe
	 */
	public QueueEntry(final JDFNode node, final JDFQueueEntry qe)
	{
		super();
		_theNode = node;
		_theQueueEntry = qe;
		qe.setIdentifier(node == null ? null : node.getIdentifier());
		if (_theNode == null || _theQueueEntry == null)
		{
			log.error("null elements in QueueEntry");
		}
	}

	/**
	 * @see org.cip4.bambi.core.queues.IQueueEntry#getJDF()
	 * @return the jdf node
	 */
	@Override
	public JDFNode getJDF()
	{
		return _theNode;
	}

	/**
	 * @see org.cip4.bambi.core.queues.IQueueEntry#getQueueEntry()
	 * @return the queue entry
	 */
	@Override
	public JDFQueueEntry getQueueEntry()
	{
		return _theQueueEntry;
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return the string
	 */
	@Override
	public String toString()
	{
		String s = "[QueueEntry: ] \nQueueEntry : ";
		s += (_theQueueEntry == null) ? "null " : _theQueueEntry.getQueueEntryID() + "" + _theQueueEntry.toString();
		s += "\n Doc: " + ((_theNode == null) ? "null " : _theNode.toString());
		return s;
	}

	/**
	 * @see org.cip4.bambi.core.queues.IQueueEntry#getQueueEntryID()
	 * @return the queue entry id, null if none is there
	 */
	@Override
	public String getQueueEntryID()
	{
		return _theQueueEntry == null ? null : _theQueueEntry.getQueueEntryID();
	}

	/**
	 * @see org.cip4.bambi.core.queues.IQueueEntry#setJDF(org.cip4.jdflib.node.JDFNode)
	 * @param node
	 */
	@Override
	public void setJDF(final JDFNode node)
	{
		_theNode = node;
	}

	/**
	 * @see org.cip4.bambi.core.queues.IQueueEntry#setQueueEntry(org.cip4.jdflib.jmf.JDFQueueEntry)
	 * @param qe
	 */
	@Override
	public void setQueueEntry(final JDFQueueEntry qe)
	{
		_theQueueEntry = qe;
	}
}
