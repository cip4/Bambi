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
import java.util.Hashtable;
import java.util.List;

import org.cip4.bambi.richclient.model.Queue;
import org.cip4.bambi.richclient.model.QueueEntry;

/**
 * Lazy loading functionality for Queue objects..
 * @author smeissner
 * @date 04.10.2009
 */
class LazyQueue {
	private final static String STATUS_REMOVED = "Removed";

	private Queue queue;

	/**
	 * Contains all QueueEntry objects by primary key.
	 */
	private final Hashtable<String, QueueEntry> queueEntries;

	/**
	 * Contains last modified time for a QueueEntry object.
	 */
	private final Hashtable<String, Long> updateLog;

	/**
	 * Default constructor. Initialize object.
	 */
	LazyQueue() {
		queueEntries = new Hashtable<String, QueueEntry>();
		updateLog = new Hashtable<String, Long>();
	}

	/**
	 * Put an QueueEntry object to queue.
	 * @param queue QueueEntry object to put
	 */
	void put(QueueEntry queueEntry) {

		// check whether queueEntry already exists
		if (queueEntries.containsKey(queueEntry.getQueueEntryId())) {
			// get it
			QueueEntry obj = queueEntries.get(queueEntry.getQueueEntryId());

			// don't update removed queue entries
			if (obj.getStatus().equals(STATUS_REMOVED)) {
				return;
			}
		} else if (queueEntry.getStatus().equals(STATUS_REMOVED)) {
			// don't cache removed queue entries
			return;
		}

		// store current time
		long time = new Date().getTime();
		updateLog.put(queueEntry.getQueueEntryId(), Long.valueOf(time));

		// put object to hashtable
		queueEntries.put(queueEntry.getQueueEntryId(), queueEntry);
	}

	/**
	 * Removes an QueueEntry object from queue.
	 * @param queue QueueEntry object to remove
	 */
	void remove(QueueEntry queueEntry) {
		// remove from
	}

	/**
	 * Put an Queue object to lazy queue
	 * @param queue Queue to put
	 */
	void put(Queue queue) {
		// store queue WITHOUT entries
		this.queue = new Queue.Builder(queue).queueEntries(null).build();

		// store entries separately
		for (QueueEntry entry : queue.getQueueEntries()) {
			put(entry);
		}
	}

	/**
	 * Returns Queue object including all QueueEntry objects.
	 * @return Queue including all QueueEntry objects.
	 */
	Queue getQueue() {
		// generate list
		List<QueueEntry> lst = new ArrayList<QueueEntry>(queueEntries.values());

		// build Queue object
		Queue queue = new Queue.Builder(this.queue).queueEntries(lst).build();

		// return result
		return queue;
	}

	/**
	 * Returns Queue object including all QueueEntry objects modified since lastUpdate time.
	 * @param lastUpdate Time of last update as long
	 * @return Queue including modified QueueEntry objects.
	 */
	Queue getQueue(long lastUpdate) {
		List<QueueEntry> lst = new ArrayList<QueueEntry>();

		// filter QueueEntry objects modified since last update
		for (String key : updateLog.keySet()) {

			long modifyTimeEntity = updateLog.get(key).longValue();

			if (modifyTimeEntity >= lastUpdate) {
				// add to result
				lst.add(queueEntries.get(key));
			}
		}

		// build Queue object
		Queue queue = new Queue.Builder(this.queue).queueEntries(lst).build();

		// return result
		return queue;
	}
}