/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2026 The International Cooperation for the Integration of
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

package org.cip4.bambi.core.queues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collections;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.auto.JDFAutoQueueFilter.EnumUpdateGranularity;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueFilter;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.NodeIdentifier;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.JDFDate;
import org.junit.Before;
import org.junit.Test;

/**
 * Focused tests for queue get/status handlers and queue helper classes.
 */
public class QueueGetHandlerTest extends BambiTestCase
{
	private QueueProcessor qp;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		qp = getDevice(false, false).getQueueProcessor();
		qp.reset();
	}

	@Test
	public void testQueueStatusHandlerNoFilterFullQueue()
	{
		final JDFQueue queue = seedMixedQueue();
		final JDFMessage query = JDFJMF.createJMF(EnumFamily.Query, EnumType.QueueStatus).getMessage(0);
		final JDFResponse resp = JDFJMF.createJMF(EnumFamily.Response, EnumType.QueueStatus).getResponse(0);

		final boolean handled = qp.new QueueStatusHandler().handleMessage(query, resp);

		assertTrue(handled);
		assertEquals(0, resp.getReturnCode());
		final JDFQueue queueResp = resp.getQueue(0);
		assertNotNull(queueResp);
		assertEquals(queue.numEntries(null), queueResp.numEntries(null));
	}

	@Test
	public void testQueueStatusHandlerStatusFilter()
	{
		seedMixedQueue();
		final JDFMessage query = JDFJMF.createJMF(EnumFamily.Query, EnumType.QueueStatus).getMessage(0);
		final JDFQueueFilter qf = query.getCreateQueueFilter(0);
		qf.setStatusList(Collections.singletonList(EnumQueueEntryStatus.Waiting));
		final JDFResponse resp = JDFJMF.createJMF(EnumFamily.Response, EnumType.QueueStatus).getResponse(0);

		final boolean handled = qp.new QueueStatusHandler().handleMessage(query, resp);

		assertTrue(handled);
		assertEquals(0, resp.getReturnCode());
		final JDFQueue queueResp = resp.getQueue(0);
		assertNotNull(queueResp);
		assertEquals(1, queueResp.numEntries(null));
		assertEquals(EnumQueueEntryStatus.Waiting, queueResp.getQueueEntry(0).getQueueEntryStatus());
	}

	@Test
	public void testQueueStatusHandlerQueueEntryIDFilter()
	{
		seedMixedQueue();
		final JDFMessage query = JDFJMF.createJMF(EnumFamily.Query, EnumType.QueueStatus).getMessage(0);
		final JDFQueueFilter qf = query.getCreateQueueFilter(0);
		qf.appendQueueEntryDef("qe-running");
		final JDFResponse resp = JDFJMF.createJMF(EnumFamily.Response, EnumType.QueueStatus).getResponse(0);

		final boolean handled = qp.new QueueStatusHandler().handleMessage(query, resp);

		assertTrue(handled);
		assertEquals(0, resp.getReturnCode());
		final JDFQueue queueResp = resp.getQueue(0);
		assertNotNull(queueResp);
		assertEquals(1, queueResp.numEntries(null));
		assertEquals("qe-running", queueResp.getQueueEntry(0).getQueueEntryID());
	}

	@Test
	public void testQueueStatusHandlerMaxEntriesFilter()
	{
		seedMixedQueue();
		final JDFMessage query = JDFJMF.createJMF(EnumFamily.Query, EnumType.QueueStatus).getMessage(0);
		final JDFQueueFilter qf = query.getCreateQueueFilter(0);
		qf.setMaxEntries(2);
		final JDFResponse resp = JDFJMF.createJMF(EnumFamily.Response, EnumType.QueueStatus).getResponse(0);

		final boolean handled = qp.new QueueStatusHandler().handleMessage(query, resp);

		assertTrue(handled);
		assertEquals(0, resp.getReturnCode());
		final JDFQueue queueResp = resp.getQueue(0);
		assertNotNull(queueResp);
		assertEquals(2, queueResp.numEntries(null));
	}

	@Test
	public void testQueueGetHandlerNoFilterAndXJDFPath()
	{
		final JDFQueueEntry qe = addQueueEntry("qe-xjdf", EnumQueueEntryStatus.Waiting, "job-xjdf", "jp-xjdf", null);
		final JDFNode root = JDFNode.createRoot();
		root.setJobID("job-xjdf");
		root.setJobPartID("jp-xjdf");
		final JDFDoc doc = root.getOwnerDocument_JDFElement();
		assertTrue(qp.storeDoc(qe, doc, null, null));

		final ContainerRequest queueReq = new ContainerRequest();
		queueReq.setRequestURI("http://host/bambi/showQueue/" + qp.getParent().getDeviceID());
		final XMLResponse queueResp = qp.getQueueGetHandler().handleGet(queueReq);
		assertNotNull(queueResp);
		assertNotNull(queueResp.getXML());
		assertTrue(queueResp.getXML() instanceof JDFQueue);

		final ContainerRequest xjdfReq = new ContainerRequest();
		xjdfReq.setRequestURI("http://host/bambi/showJDF/" + qp.getParent().getDeviceID());
		xjdfReq.setParameter(QueueProcessor.QE_ID, qe.getQueueEntryID());
		xjdfReq.setParameter(AttributeName.JOBPARTID, "jp-xjdf");
		final XMLResponse xjdfResp = qp.handleGet(xjdfReq);

		assertNotNull(xjdfResp);
		assertNotNull(xjdfResp.getXML());
		assertEquals("XJDF", xjdfResp.getXML().getLocalName());
	}

	@Test
	public void testQueueDeltaDiffAndOrphanAndToString() throws Exception
	{
		final JDFQueue seededQueue = seedMixedQueue();
		final JDFMessage query1 = JDFJMF.createJMF(EnumFamily.Query, EnumType.QueueStatus).getMessage(0);
		final JDFQueueFilter qf1 = query1.getCreateQueueFilter(0);
		qf1.setUpdateGranularity(EnumUpdateGranularity.ChangesOnly);
		final JDFResponse resp1 = JDFJMF.createJMF(EnumFamily.Response, EnumType.QueueStatus).getResponse(0);
		resp1.setrefID("delta-channel");

		assertTrue(qp.new QueueStatusHandler().handleMessage(query1, resp1));
		assertNotNull(resp1.getQueue(0));

		final JDFMessage query2 = JDFJMF.createJMF(EnumFamily.Query, EnumType.QueueStatus).getMessage(0);
		query2.getCreateQueueFilter(0).setUpdateGranularity(EnumUpdateGranularity.ChangesOnly);
		final JDFResponse resp2 = JDFJMF.createJMF(EnumFamily.Response, EnumType.QueueStatus).getResponse(0);
		resp2.setrefID("delta-channel");

		assertTrue(qp.new QueueStatusHandler().handleMessage(query2, resp2));
		final JDFQueue queueDiffOnly = resp2.getQueue(0);
		assertNotNull(queueDiffOnly);
		assertEquals(0, queueDiffOnly.numEntries(null));
		assertEquals(seededQueue.numEntries(null), queueDiffOnly.getQueueSize());
		assertEquals("Running", queueDiffOnly.getQueueStatus().getName());

		addQueueEntry("qe-delta-new", EnumQueueEntryStatus.Waiting, "job-delta-new", null, null);
		final JDFMessage query3 = JDFJMF.createJMF(EnumFamily.Query, EnumType.QueueStatus).getMessage(0);
		query3.getCreateQueueFilter(0).setUpdateGranularity(EnumUpdateGranularity.ChangesOnly);
		final JDFResponse resp3 = JDFJMF.createJMF(EnumFamily.Response, EnumType.QueueStatus).getResponse(0);
		resp3.setrefID("delta-channel");

		assertTrue(qp.new QueueStatusHandler().handleMessage(query3, resp3));
		final JDFQueue deltaQueue = resp3.getQueue(0);
		assertNotNull(deltaQueue);
		assertNotNull(deltaQueue.getQueueEntry("qe-delta-new"));

		final QueueProcessor.QueueDelta delta = qp.getQueueDelta();
		assertNotNull(delta.toString());
		final Method isOrphan = delta.getClass().getDeclaredMethod("isOrphan");
		isOrphan.setAccessible(true);
		assertFalse(((Boolean) isOrphan.invoke(delta)).booleanValue());
	}

	@Test
	public void testSlaveQueueMapFillAndLookup()
	{
		final JDFQueueEntry qe1 = addQueueEntry("qe-s1", EnumQueueEntryStatus.Waiting, "job-s1", null, "slave-1");
		final JDFQueueEntry qe2 = addQueueEntry("qe-s2", EnumQueueEntryStatus.Running, "job-s2", null, "slave-2");

		final QueueProcessor.SlaveQueueMap sqm = qp.new SlaveQueueMap();
		sqm.fill(qp.getQueue());

		assertEquals(qe1.getQueueEntryID(), sqm.getQEFromSlaveQEID("slave-1").getQueueEntryID());
		assertEquals(qe2.getQueueEntryID(), sqm.getQEFromNI(new NodeIdentifier("job-s2", null, null)).getQueueEntryID());
		assertNull(sqm.getQEFromSlaveQEID("missing-slave"));
		assertNull(sqm.getQEFromNI(new NodeIdentifier("job-missing", null, null)));
		assertTrue(sqm.toString().contains("QueueMap"));
	}

	@Test
	public void testSlaveQueueMapResetAndQueueStatisticAndStatusHandlerResidual()
	{
		addQueueEntry("qe-w", EnumQueueEntryStatus.Waiting, "job-w", null, "slave-w");
		addQueueEntry("qe-r", EnumQueueEntryStatus.Running, "job-r", null, "slave-r");
		addQueueEntry("qe-c", EnumQueueEntryStatus.Completed, "job-c", null, "slave-c");

		final QueueProcessor.SlaveQueueMap sqm = qp.new SlaveQueueMap();
		sqm.fill(qp.getQueue());
		assertTrue(sqm.size() >= 3);
		sqm.reset();
		assertEquals(0, sqm.size());
		assertNull(sqm.getQEFromSlaveQEID("slave-w"));

		final QueueProcessor.QueueStatistic stat = qp.new QueueStatistic();
		assertEquals(1, stat.waiting);
		assertEquals(1, stat.running);
		assertEquals(1, stat.completed);
		assertEquals(3, stat.all);
		assertNotNull(stat.toString());

		final QueueProcessor.QueueStatusHandler h = qp.new QueueStatusHandler();
		assertFalse(h.handleMessage(null, null));
		assertTrue(h.isSubScribable());
	}

	private JDFQueue seedMixedQueue()
	{
		qp.reset();
		addQueueEntry("qe-waiting", EnumQueueEntryStatus.Waiting, "job-waiting", null, null);
		addQueueEntry("qe-running", EnumQueueEntryStatus.Running, "job-running", null, null);
		addQueueEntry("qe-completed", EnumQueueEntryStatus.Completed, "job-completed", null, null);
		addQueueEntry("qe-held", EnumQueueEntryStatus.Held, "job-held", null, null);
		addQueueEntry("qe-suspended", EnumQueueEntryStatus.Suspended, "job-suspended", null, null);
		return qp.getQueue();
	}

	private JDFQueueEntry addQueueEntry(final String qeID, final EnumQueueEntryStatus status, final String jobID, final String jobPartID, final String slaveID)
	{
		final JDFQueueEntry qe = qp.getQueue().appendQueueEntry();
		qe.setQueueEntryID(qeID);
		qe.setQueueEntryStatus(status);
		qe.setJobID(jobID);
		if (jobPartID != null)
		{
			qe.setJobPartID(jobPartID);
		}
		qe.setSubmissionTime(new JDFDate());
		if (slaveID != null)
		{
			qp.updateCache(qe, slaveID);
		}
		return qe;
	}

	@Test
	public void testQueueStatusHandlerQueueEntryDefCountFilter()
	{
		seedMixedQueue();
		final JDFMessage query = JDFJMF.createJMF(EnumFamily.Query, EnumType.QueueStatus).getMessage(0);
		final JDFQueueFilter qf = query.getCreateQueueFilter(0);
		qf.appendQueueEntryDef("qe-waiting");
		qf.appendQueueEntryDef("qe-running");
		qf.appendQueueEntryDef("qe-completed");
		final JDFResponse resp = JDFJMF.createJMF(EnumFamily.Response, EnumType.QueueStatus).getResponse(0);

		final boolean handled = qp.new QueueStatusHandler().handleMessage(query, resp);

		assertTrue(handled);
		final JDFQueue queueResp = resp.getQueue(0);
		assertNotNull(queueResp);
		final VElement v = queueResp.getQueueEntryVector();
		assertEquals(3, ContainerUtil.size(v));
	}
}