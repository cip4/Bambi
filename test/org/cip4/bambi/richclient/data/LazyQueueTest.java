/**
 * The CIP4 Software License, Version 1.0
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
package org.cip4.bambi.richclient.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.cip4.bambi.richclient.model.Queue;
import org.cip4.bambi.richclient.model.QueueEntry;

/**
 * JUnit test for LazyQueue object.
 * @author smeissner
 * @date 08.10.2009
 */
public class LazyQueueTest extends TestCase {

	private static final String STATUS_REMOVED = "Removed";

	LazyQueue lazyQueue;
	Queue queue;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// build queue
		List<QueueEntry> lst = new ArrayList<QueueEntry>(5);
		lst.add(new QueueEntry.Builder("queueEntry1").status(STATUS_REMOVED).build());
		lst.add(new QueueEntry.Builder("queueEntry2").status("Running").build());
		lst.add(new QueueEntry.Builder("queueEntry3").status("Waiting").build());
		lst.add(new QueueEntry.Builder("queueEntry4").status("Running").build());
		lst.add(new QueueEntry.Builder("queueEntry5").status("Waiting").build());
		queue = new Queue.Builder().deviceId("queueId").queueEntries(lst).build();

		// initialize lazy queue
		lazyQueue = new LazyQueue();
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		// reset queue
		lazyQueue = null;
	}

	/**
	 * Test: Don't add removed queue.
	 * 
	 * Test method for {@link org.cip4.bambi.richclient.data.LazyQueue#put(org.cip4.bambi.richclient.model.Queue)}.
	 */
	public void testPutQueueSize() {
		// set queue
		lazyQueue.put(queue);

		// get queue
		Queue result = lazyQueue.getQueue();

		// check
		assertEquals("Size queueEntries in queue is wrong.", 4, result.getQueueEntries().size());
	}

	/**
	 * Test: Change status to Removed.
	 * 
	 * Test method for {@link org.cip4.bambi.richclient.data.LazyQueue#put(org.cip4.bambi.richclient.model.Queue)}.
	 */
	public void testPutChangeStatus2Removed() {
		// set lazy queue
		lazyQueue.put(queue);

		// modify queue Entry object
		QueueEntry entry = new QueueEntry.Builder(queue.getQueueEntries().get(2)).status(STATUS_REMOVED).build();
		lazyQueue.put(entry);

		// get queue
		Queue result = lazyQueue.getQueue();

		// check
		assertEquals("Size queueEntries in queue is wrong.", 4, result.getQueueEntries().size());
		assertEquals("Entry status is wrong", STATUS_REMOVED, result.getQueueEntries().get(2).getStatus());
	}

	/**
	 * Test: Don't update removed entries.
	 * 
	 * Test method for {@link org.cip4.bambi.richclient.data.LazyQueue#put(org.cip4.bambi.richclient.model.Queue)}.
	 */
	public void testPutUpdateException() {
		// set lazy queue
		lazyQueue.put(queue);

		// modify queue Entry object
		QueueEntry entry1 = new QueueEntry.Builder(queue.getQueueEntries().get(2)).status(STATUS_REMOVED).build();
		lazyQueue.put(entry1);

		// modify queue Entry object twice
		QueueEntry entry2 = new QueueEntry.Builder(queue.getQueueEntries().get(2)).status("Running").build();
		lazyQueue.put(entry2);

		// get queue
		Queue result = lazyQueue.getQueue();

		// check
		assertEquals("Size queueEntries in queue is wrong.", 4, result.getQueueEntries().size());
		assertEquals("Entry status is wrong", STATUS_REMOVED, result.getQueueEntries().get(2).getStatus());
	}

	/**
	 * Test: Only feedback modified / new items since <code>time</code>
	 * 
	 * Test method for {@link org.cip4.bambi.richclient.data.LazyQueue#getQueue(long)}.
	 */
	public void testGetQueueLong() throws InterruptedException {
		// set lazy queue
		lazyQueue.put(queue);

		// wait
		Thread.sleep(500);

		// get time
		long time = new Date().getTime();

		// wait
		Thread.sleep(500);

		// modify QueueEntry object
		QueueEntry entry1 = new QueueEntry.Builder(queue.getQueueEntries().get(2)).status(STATUS_REMOVED).build();
		lazyQueue.put(entry1);

		// add new QueueEntry object
		QueueEntry entry2 = new QueueEntry.Builder("queueEntryNew").status("Running").build();
		lazyQueue.put(entry2);

		// get queue
		Queue result = lazyQueue.getQueue(time);

		// check
		assertEquals("Size queueEntries in queue is wrong.", 2, result.getQueueEntries().size());
		assertEquals("Entry status is wrong", "queueEntry3", result.getQueueEntries().get(0).getQueueEntryId());
		assertEquals("Entry status is wrong", "queueEntryNew", result.getQueueEntries().get(1).getQueueEntryId());
	}

}
