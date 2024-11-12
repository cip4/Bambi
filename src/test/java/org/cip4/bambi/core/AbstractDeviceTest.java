/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2024 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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

package org.cip4.bambi.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.BambiTestDevice;
import org.cip4.bambi.core.IDeviceProperties.EWatchFormat;
import org.cip4.bambi.core.queues.QueueEntry;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.extensions.XJDFHelper;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JMFBuilderFactory;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.ThreadUtil;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractDeviceTest extends BambiTestCaseBase
{

	/**
	 * @throws Exception
	 */
	@Test
	public void testUpdatePriority() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry(null, "http://foo/bar/*.jdf");
		final JDFQueue q = (JDFQueue) new JDFDoc(ElementName.QUEUE).getRoot();
		final JDFQueueEntry qe = q.appendQueueEntry();
		final JDFNode n = JDFNode.createRoot();
		n.setJobID("j1");
		final JDFNodeInfo ni = n.getCreateNodeInfo();
		ni.removeAttribute(AttributeName.JOBPRIORITY);
		final String s = device.fixEntry(qe, n.getOwnerDocument_JDFElement());
		assertNull(ni.getNonEmpty(AttributeName.JOBPRIORITY));
		assertNull(qe.getNonEmpty(AttributeName.PRIORITY));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testConfig() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		device.setSim(true);
		final File dir = device.getCachedConfigDir();
		assertEquals(new File(sm_dirTestDataTemp, "config"), dir);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testUpdateWatchURL() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		device.setSim(true);
		device.getSignalDispatcher().removeSubScriptions(null, null, null);
		device.updateWatchURL(null, null);

		assertEquals(EWatchFormat.JMF, device.getProperties().getWatchFormat());
		device.updateWatchURL("http://dummy.com", EWatchFormat.JSON.name());
		assertEquals(EWatchFormat.JSON, device.getProperties().getWatchFormat());
		assertEquals("http://dummy.com", device.getProperties().getWatchURL());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEWatchFormat() throws Exception
	{
		assertEquals(EWatchFormat.JMF, EWatchFormat.getEnum(null));
		assertEquals(EWatchFormat.XJMF, EWatchFormat.getEnum("xJmF"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testExtractURL() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		device.setSim(true);
		final JDFQueueEntry qe = (JDFQueueEntry) JDFElement.createRoot(ElementName.QUEUEENTRY);
		qe.setQueueEntryID("a");
		final File dir = device.getExtractDirectory(qe, true);
		assertEquals("a", dir.getName());
	}

	/**
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testExtractURLBad1() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		device.setSim(true);
		final JDFQueueEntry qe = (JDFQueueEntry) JDFElement.createRoot(ElementName.QUEUEENTRY);
		qe.setQueueEntryID("..");
		device.getExtractDirectory(qe, true);
	}

	/**
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testExtractURLBad2() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		device.setSim(true);
		final JDFQueueEntry qe = (JDFQueueEntry) JDFElement.createRoot(ElementName.QUEUEENTRY);
		qe.setQueueEntryID("a/..");
		device.getExtractDirectory(qe, true);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testUpdateWatchURLXML() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		device.setSim(true);
		device.getSignalDispatcher().removeSubScriptions(null, null, null);
		device.updateWatchURL(null, null);

		assertEquals(EWatchFormat.JMF, device.getProperties().getWatchFormat());
		device.updateWatchURL("http://dummy.com", EWatchFormat.JSON.name());
		assertEquals(EWatchFormat.JSON, device.getProperties().getWatchFormat());
		assertEquals("http://dummy.com", device.getProperties().getWatchURL());
		final XMLDevice xd = device.getXMLDevice(false, new ContainerRequest());
		assertEquals(EWatchFormat.JSON.name(), xd.getRoot().getAttribute("WatchFormat"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testCreateSubmissionJMF() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		device.setSim(true);
		final XJDFHelper h = new XJDFHelper("j1", "p1");
		final JDFJMF jmf = device.createSubmissionJMF(h.getRoot(), null);
		assertEquals(2, jmf.getMaxVersion().getMajorVersion());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testCreateSubmitFromJDF() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		device.setSim(true);
		final XJDFHelper h = new XJDFHelper("j1", "p1");
		final XMLRequest xml = device.createSubmitFromJDF(h.getRoot(), new XMLRequest(h.getRoot()));
		assertEquals(2, ((JDFJMF) xml.getXML()).getMaxVersion().getMajorVersion());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testToString() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		assertNotNull(device.toString());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testCopyToCache() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		device.setSim(true);
		device.copyToCache();
		final File dir = device.getCachedConfigDir();
		assertEquals(new File(sm_dirTestDataTemp, "config"), dir);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDoSynch() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		device.setSynchronous(true);
		final QueueEntry qe = new QueueEntry(JDFNode.createRoot(), device.getQueueProcessor().getQueue().appendQueueEntry());
		assertTrue(device.doSynchronous(qe));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDoSynchMulti() throws Exception
	{
		final BambiTestDevice device = Mockito.spy(new BambiTestDevice());
		device.setSynchronous(true);
		when(device.getParallelSynch()).thenReturn(1);
		final QueueProcessor queueProcessor = device.getQueueProcessor();
		for (int i = 0; i < 42; i++)
		{
			final JDFQueueEntry qe = queueProcessor.getQueue().appendQueueEntry();
			qe.setQueueEntryID("qe" + i);
			qe.setQueueEntryStatus(EnumQueueEntryStatus.Waiting);
			final JDFNode root = JDFNode.createRoot();
			root.setJobID("J" + i % 7);
			final QueueEntry qee = new QueueEntry(root, qe);
			assertTrue(device.doSynchronous(qee));
		}
		for (int i = 0; i < 1234; i++)
		{
			if (queueProcessor.getQueue().numEntries(EnumQueueEntryStatus.Waiting) > 0)
			{
				ThreadUtil.sleep(123);
			}
		}
		assertNotEquals(0, queueProcessor.getQueue().numEntries(EnumQueueEntryStatus.Completed));
		assertEquals(0, queueProcessor.getQueue().numEntries(EnumQueueEntryStatus.Waiting));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDoSynchMulti4() throws Exception
	{
		final BambiTestDevice device = Mockito.spy(new BambiTestDevice());
		device.setSynchronous(true);
		when(device.getParallelSynch()).thenReturn(4);
		final QueueProcessor queueProcessor = device.getQueueProcessor();
		for (int i = 0; i < 42; i++)
		{
			final JDFQueueEntry qe = queueProcessor.getQueue().appendQueueEntry();
			qe.setQueueEntryID("qe" + i);
			qe.setQueueEntryStatus(EnumQueueEntryStatus.Waiting);
			final JDFNode root = JDFNode.createRoot();
			root.setJobID("J" + i % 7);
			final QueueEntry qee = new QueueEntry(root, qe);
			assertTrue(device.doSynchronous(qee));
		}
		for (int i = 0; i < 1234; i++)
		{
			if (queueProcessor.getQueue().numEntries(EnumQueueEntryStatus.Waiting) > 0)
			{
				ThreadUtil.sleep(123);
			}
		}
		assertNotEquals(0, queueProcessor.getQueue().numEntries(EnumQueueEntryStatus.Completed));
		assertEquals(0, queueProcessor.getQueue().numEntries(EnumQueueEntryStatus.Waiting));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDoAsynchMulti() throws Exception
	{
		final BambiTestDevice device = Mockito.spy(new BambiTestDevice());
		device.setSynchronous(false);
		when(device.getParallelSynch()).thenReturn(4);
		final QueueProcessor queueProcessor = device.getQueueProcessor();
		for (int i = 0; i < 42; i++)
		{
			final JDFQueueEntry qe = queueProcessor.getQueue().appendQueueEntry();
			qe.setQueueEntryID("qe" + i);
			qe.setQueueEntryStatus(EnumQueueEntryStatus.Waiting);
			final JDFNode root = JDFNode.createRoot();
			root.setJobID("J" + i % 7);
			final QueueEntry qee = new QueueEntry(root, qe);
			assertTrue(device.doSynchronous(qee));
		}
		for (int i = 0; i < 1234; i++)
		{
			if (queueProcessor.getQueue().numEntries(EnumQueueEntryStatus.Waiting) > 0)
			{
				ThreadUtil.sleep(123);
			}
		}
		assertNotEquals(0, queueProcessor.getQueue().numEntries(EnumQueueEntryStatus.Completed));
		assertEquals(0, queueProcessor.getQueue().numEntries(EnumQueueEntryStatus.Waiting));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testForceFile() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		assertFalse(device.forceCopy(null));
		assertFalse(device.forceCopy(new File("foo.xml")));
		assertTrue(device.forceCopy(new File("foo.xsl")));
		assertTrue(device.forceCopy(new File("foo.xsd")));
	}

}
