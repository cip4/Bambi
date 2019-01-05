/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2019 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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
package org.cip4.bambi.core.messaging;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StringUtil;

/**
 * class that has a head and a tail and also serializes intermediate messages
 *
 * @author rainer prosi
 * @date Mar 18, 2014
 */
class MessageFiFo
{
	/**
	 * @param dumpDir
	 */
	MessageFiFo(final File dumpDir)
	{
		super();
		log = LogFactory.getLog(getClass());
		this.dumpDir = dumpDir;
		dumps = readDump();
		head = readFirstDump();
		tail = createTail();
	}

	private ArrayList<MessageDetails> createTail()
	{
		final File oldTail = getDumpFile("tail");
		final ArrayList<MessageDetails> v = readSingleFile(oldTail);
		if (!v.isEmpty())
			return v;

		if (dumps.isEmpty())
		{
			return head;
		}
		else
		{
			return v;
		}
	}

	private File getDumpFile(final String headTail)
	{
		final File localFile = new File("MsgDump." + headTail + ".xml");
		final File oldTail = FileUtil.getFileInDirectory(dumpDir, localFile);
		return oldTail;
	}

	/**
	 *
	 * @return
	 */
	private synchronized ArrayList<MessageDetails> readFirstDump()
	{
		final File oldHead = getDumpFile("head");
		final ArrayList<MessageDetails> v = readSingleFile(oldHead);
		if (!v.isEmpty())
		{
			oldHead.delete();
			return v;
		}
		final ArrayList<MessageDetails> vMD = removeFromDump();
		return vMD;
	}

	private ArrayList<MessageDetails> removeFromDump()
	{
		if (dumps.isEmpty())
			return new ArrayList<>();
		final File remove = dumps.remove(0);
		final ArrayList<MessageDetails> v = readSingleFile(remove);
		remove.delete();
		if (size() > 0)
		{
			log.info("reactivating file from disk: " + remove.getAbsolutePath() + " messages pending: " + size());
		}
		return v;
	}

	private ArrayList<MessageDetails> readSingleFile(final File inputFile)
	{
		final ArrayList<MessageDetails> vMD = new ArrayList<>();
		if (inputFile != null && inputFile.canRead())
		{
			final JDFDoc d = JDFDoc.parseFile(inputFile);
			// adding existing messages prior to vTmp - they must be sent first
			if (d != null)
			{
				final KElement root = d.getRoot();

				final VElement v = root.getChildElementVector("Message", null);
				final int zapp = 0;
				for (final KElement e : v)
				{
					final MessageDetails messageDetails = new MessageDetails(e);
					vMD.add(messageDetails);
				}
				log.info(" read " + v.size() + " messages from " + inputFile.getAbsolutePath() + " and removed messages: " + zapp);
			}
			inputFile.delete();
		}
		return vMD;
	}

	/**
	 * read all files from dumpdir
	 *
	 * @return
	 */
	private ArrayList<File> readDump()
	{
		ArrayList<File> vDumps = ContainerUtil.toArrayList(FileUtil.listFilesWithExpression(dumpDir, "*.msg.xml"));
		if (vDumps == null)
			vDumps = new ArrayList<>();
		Collections.sort(vDumps);
		if (!vDumps.isEmpty())
		{
			log.info("read pending messages: in " + dumpDir.getAbsolutePath() + " #files= " + vDumps.size());
		}
		return vDumps;
	}

	final Log log;
	ArrayList<MessageDetails> head;
	ArrayList<MessageDetails> tail;
	final ArrayList<File> dumps;
	final File dumpDir;
	static final int messPerDump = 421;

	/**
	 *
	 * @param i
	 * @return
	 */
	public MessageDetails get(final int i)
	{
		if (i < head.size())
			return head.get(0);
		else if (i >= tailStart() && i < size())
			return tail.get(i - tailStart());
		return null;
	}

	/**
	 *
	 * return a clone of tail so that we can optimize without blocking the head
	 *
	 * @return
	 */
	public synchronized Vector<MessageDetails> getTailClone()
	{
		if (tail == null)
			return null;
		final Vector<MessageDetails> v = new Vector<>();
		v.addAll(tail);
		return v;
	}

	/**
	 *
	 * @return
	 */
	public int size()
	{
		int s = tailStart();
		if (s == 0 || head != tail)
			s += tail.size();
		return s;
	}

	private int tailStart()
	{
		int s = dumps.size() * messPerDump;
		if (head != tail)
			s += head.size();
		return s;
	}

	/**
	 *
	 * @param i
	 * @return
	 */
	public synchronized MessageDetails remove(final int i)
	{
		if (i < head.size())
		{
			final MessageDetails remove = head.remove(i);
			if (head.isEmpty())
			{
				if (!dumps.isEmpty())
				{
					head = removeFromDump();
				}
				else if (head != tail)
				{
					head = tail;
					dumpHeadTail();
				}
			}
			return remove;
		}
		else if (i >= tailStart() && i < size())
		{
			return tail.remove(i - tailStart());
		}
		else if (head.isEmpty() && !dumps.isEmpty())
		{
			head = removeFromDump();
			return remove(i);
		}
		return null;

	}

	/**
	 *
	 * @return
	 */
	public boolean isEmpty()
	{
		return get(0) == null;
	}

	/**
	 * cleans up the metadata - note the files still need to be deleted
	 *
	 */
	public void clear()
	{
		head = new ArrayList<>();
		tail = head;
		dumps.clear();
	}

	/**
	 *
	 * @param messageDetails
	 */
	public synchronized void add(final MessageDetails messageDetails)
	{
		if (tail.size() >= messPerDump)
		{
			final ArrayList<MessageDetails> newTail = new ArrayList<>();
			while (tail.size() > messPerDump)
			{
				newTail.add(tail.get(messPerDump));
			}
			if (head != tail)
				persistTail();
			tail = newTail;
		}
		tail.add(messageDetails);
	}

	/**
	 * persist the current tail and create a new vector
	 */
	private void persistTail()
	{
		final File f = getNewTail();
		persistFile(f, tail);
		dumps.add(f);
		log.info(" dumping full file to disk: " + f.getAbsolutePath() + " current size: " + size());
	}

	/**
	 *
	 * @return
	 */
	private File getNewTail()
	{
		int iPos = 0;
		for (final File f : dumps)
		{
			int ii = getPos(f);
			if (ii >= iPos)
				iPos = ++ii;
		}
		final String dumpName = StringUtil.sprintf("%06i.msg", "" + iPos);
		return getDumpFile(dumpName);
	}

	/**
	 *
	 * @param f
	 * @return
	 */
	private int getPos(final File f)
	{
		final String name = f.getName();
		final String token = StringUtil.token(name, -3, ".");
		return StringUtil.parseInt(token, -1);
	}

	private void persistFile(final File f, final ArrayList<MessageDetails> vM)
	{
		final XMLDoc xmlDoc = new XMLDoc("MessageSender", null);
		final KElement meassagesRoot = xmlDoc.getRoot();
		if (vM.size() > 0)
		{
			log.info("writing " + vM.size() + " pending messages to: " + f.getAbsolutePath());
		}
		for (final MessageDetails md : vM)
		{
			md.appendToXML(meassagesRoot, 1, false);
		}
		xmlDoc.write2File(f, 2, false);
	}

	/**
	 *
	 */
	public synchronized void dumpHeadTail()
	{
		final File headFile = getDumpFile("head");
		headFile.delete();
		final File tailFile = getDumpFile("tail");
		tailFile.delete();
		if (!head.isEmpty())
			persistFile(headFile, head);
		if (head != tail && !tail.isEmpty())
			persistFile(tailFile, tail);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "MessageFiFo [size=" + size() + " head=" + head.size() + ", tail=" + tail.size() + ", dumps=" + dumps.size() + ", dumpDir=" + dumpDir + "]";
	}

	/**
	 *
	 * @param messageDetails
	 * @return
	 */
	public synchronized boolean remove(final MessageDetails messageDetails)
	{
		boolean zapped = false;
		if (tail != null)
			zapped = tail.remove(messageDetails);
		if (!zapped && head != null && head != tail)
			head.remove(messageDetails);
		return zapped;
	}
}
