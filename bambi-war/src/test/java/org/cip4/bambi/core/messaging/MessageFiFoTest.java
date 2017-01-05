/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2017 The International Cooperation for the Integration of
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
package org.cip4.bambi.core.messaging;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StringUtil;
import org.junit.Test;

/**
 *
 * @author rainer prosi
 *
 */
public class MessageFiFoTest extends BambiTestCaseBase
{
	/**
	 *
	 */
	@Test
	public void testFill()
	{
		final File dumpDir = new File(sm_dirTestDataTemp + "/fifo");
		FileUtil.deleteAll(dumpDir);
		MessageFiFo fifo = new MessageFiFo(dumpDir);
		long nIn = 0;
		long nOut = 0;
		for (int i = 0; i < 333; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				final KElement e = new JDFDoc("elem").getRoot();
				KElement jmf = e.appendElement(ElementName.JMF);
				jmf.setAttribute(AttributeName.DESCRIPTIVENAME, "" + nIn);
				final MessageDetails messageDetails = new MessageDetails(e);
				fifo.add(messageDetails);
				nIn++;
			}
			for (int j = 0; j < 2; j++)
			{
				final MessageDetails messageDetails = fifo.get(0);
				assertEquals(fifo.remove(0), messageDetails);
				final JDFJMF jmf = messageDetails.jmf;
				final long nJMF = StringUtil.parseLong(jmf.getDescriptiveName(), -1);
				assertEquals(nJMF, nOut);
				nOut++;
			}
		}
		log.info("dumping");
		fifo.dumpHeadTail();
		log.info("reactivating " + dumpDir);
		fifo = new MessageFiFo(dumpDir);
		for (int i = 0; i < 424; i++)
		{
			for (int j = 0; j < 2; j++)
			{
				final KElement e = new JDFDoc("elem").getRoot();
				e.appendElement(ElementName.JMF).setAttribute(AttributeName.DESCRIPTIVENAME, "" + nIn);
				final MessageDetails messageDetails = new MessageDetails(e);
				fifo.add(messageDetails);
				nIn++;
			}
			boolean breakit = false;
			for (int j = 0; j < 10; j++)
			{
				final MessageDetails messageDetails = fifo.get(0);
				assertEquals(fifo.remove(0), messageDetails);
				if (messageDetails == null)
				{
					breakit = true;
					break;
				}
				final JDFJMF jmf = messageDetails.jmf;
				final long nJMF = StringUtil.parseLong(jmf.getDescriptiveName(), -1);
				assertEquals(nJMF, nOut);
				nOut++;
			}
			if (breakit)
			{
				fifo.dumpHeadTail();
				break;
			}
		}
	}

	/**
	 *
	 */
	@Test
	public void testName()
	{
		final File dumpDir = new File(sm_dirTestDataTemp + "/fifo");
		FileUtil.deleteAll(dumpDir);
		MessageFiFo fifo = new MessageFiFo(dumpDir);
		final KElement e = new JDFDoc("elem").getRoot();
		JDFJMF jmf = (JDFJMF) e.appendElement(ElementName.JMF);
		jmf.appendSignal(EnumType.Resource);
		final MessageDetails messageDetails = new MessageDetails(e);
		fifo.add(messageDetails);
		MessageDetails d2 = fifo.get(0);
		assertEquals("Resource", d2.getName());

	}
}
