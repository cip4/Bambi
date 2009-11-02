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

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Date;

import junit.framework.TestCase;

import org.cip4.bambi.richclient.value.DeviceListVO;
import org.cip4.bambi.richclient.value.DeviceVO;
import org.cip4.bambi.richclient.value.MsgSubscriptionVO;
import org.cip4.bambi.richclient.value.QueueEntryVO;
import org.cip4.bambi.richclient.value.QueueVO;
import org.cip4.bambi.richclient.value.SubscriptionListVO;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

/**
 * JUnit test case for DevicesContextImpl.
 * @author smeissner
 * @date 30.09.2009
 */
public class DevicesContextImplTest extends TestCase {

	DevicesContextImpl devicesContextImpl;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		devicesContextImpl = new DevicesContextImpl(true);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.data.DevicesContextImpl#deviceOverview()}.
	 */
	public void testDeviceOverview() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.data.DevicesContextImpl#getDevice(java.lang.String, java.lang.String)}.
	 */
	public void testGetDevice() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.data.DevicesContextImpl#getDeviceDiff(java.lang.String, java.lang.String)}.
	 */
	public void testGetDeviceDiff() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.data.DevicesContextImpl#queueClose(java.lang.String)}.
	 */
	public void testQueueClose() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.data.DevicesContextImpl#queueFlush(java.lang.String)}.
	 */
	public void testQueueFlush() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.data.DevicesContextImpl#queueHold(java.lang.String)}.
	 */
	public void testQueueHold() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.data.DevicesContextImpl#queueOpen(java.lang.String)}.
	 */
	public void testQueueOpen() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.data.DevicesContextImpl#queueResume(java.lang.String)}.
	 */
	public void testQueueResume() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.data.DevicesContextImpl#run()}.
	 */
	public void testRun() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.data.DevicesContextImpl#getDeviceList()}.
	 */
	public void testGetDeviceList() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.data.DevicesContextImpl#getDeviceList(long)}.
	 */
	public void testGetDeviceListLong() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.data.DevicesContextImpl#unmarshallDeviceList()}.
	 * @throws MappingException
	 * @throws IOException
	 * @throws ValidationException
	 * @throws MarshalException
	 * @throws IOException
	 * @throws MappingException
	 * @throws ValidationException
	 * @throws MarshalException
	 */
	public void testUnmarsahlDeviceList() throws MarshalException, ValidationException, IOException, MappingException {
		// load sample file
		URL url = DevicesContextImpl.class.getResource("/deviceList.xml");
		Reader reader = new FileReader(url.getFile());

		// unmarshal
		DeviceListVO vo = (DeviceListVO) devicesContextImpl.unmarshal(reader, DeviceListVO.class);

		// check result
		assertEquals("Size devices List is wrong.", 5, vo.getDevices().size());

		assertEquals("Mapping DeviceListVO.Context is wrong!", "context", vo.getContext());
		assertEquals("Mapping DeviceListVO.DeviceType is wrong!", "deviceType", vo.getDeviceType());
		assertEquals("Mapping DeviceListVO.MemFree is wrong!", 1, vo.getMemFree());
		assertEquals("Mapping DeviceListVO.MemTotal is wrong!", 2, vo.getMemTotal());
		assertEquals("Mapping DeviceListVO.NumRequests is wrong!", 3, vo.getNumRequests());

		DeviceVO deviceVO = vo.getDevices().get(0);
		assertEquals("Mapping DeviceVO.Context is wrong!", "context", deviceVO.getContext());
		assertEquals("Mapping DeviceVO.DeviceID is wrong!", "deviceId", deviceVO.getId());
		assertEquals("Mapping DeviceVO.DeviceStatus is wrong!", "status", deviceVO.getStatus());
		assertEquals("Mapping DeviceVO.DeviceType is wrong!", "type", deviceVO.getType());
		assertEquals("Mapping DeviceVO.DeviceURL is wrong!", "deviceUrl", deviceVO.getUrl());
		assertEquals("Mapping DeviceVO.TypeExpression is wrong!", "typeExpression", deviceVO.getTypeExpression());
		assertEquals("Mapping DeviceVO.WatchURL is wrong!", "watchUrl", deviceVO.getWatchUrl());

		assertEquals("Mapping DeviceVO.ErrorHF is wrong!", "errorHF", deviceVO.getErrorFolder());
		assertEquals("Mapping DeviceVO.InputHF is wrong!", "inputHF", deviceVO.getInputFolder());
		assertEquals("Mapping DeviceVO.NumRequests is wrong!", 1, deviceVO.getNumRequests());
		assertEquals("Mapping DeviceVO.OutputHF is wrong!", "outputHF", deviceVO.getOutputFolder());
		assertEquals("Mapping DeviceVO.QueueCompleted is wrong!", 2, deviceVO.getQueueCompleted());

		assertEquals("Mapping DeviceVO.QueueRunning is wrong!", 3, deviceVO.getQueueRunning());
		assertEquals("Mapping DeviceVO.QueueStatus is wrong!", "queueStatus", deviceVO.getQueueStatus());
		assertEquals("Mapping DeviceVO.QueueWaiting is wrong!", 4, deviceVO.getQueueWaiting());
		assertEquals("Mapping DeviceVO.Root is wrong!", true, deviceVO.isRoot());
		assertEquals("Mapping DeviceVO.modify is wrong!", true, deviceVO.isModify());
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.data.DevicesContextImpl#unmarshallDeviceList()}.
	 * @throws MappingException
	 * @throws IOException
	 * @throws ValidationException
	 * @throws MarshalException
	 */
	@SuppressWarnings("deprecation")
	public void testUnmarsahlQueue() throws MarshalException, ValidationException, IOException, MappingException {
		// load sample file
		URL url = DevicesContextImpl.class.getResource("/queue.xml");
		Reader reader = new FileReader(url.getFile());

		// unmarshal
		QueueVO vo = (QueueVO) devicesContextImpl.unmarshal(reader, QueueVO.class);

		// check result
		assertEquals("Size devices List is wrong.", 324, vo.getQueueEntries().size());

		assertEquals("Mapping is wrong!", "context", vo.getContext());
		assertEquals("Mapping is wrong!", "descriptiveName", vo.getDescriptiveName());
		assertEquals("Mapping is wrong!", "deviceId", vo.getDeviceId());
		assertEquals("Mapping is wrong!", true, vo.isPull());
		assertEquals("Mapping is wrong!", true, vo.isRefresh());
		assertEquals("Mapping is wrong!", "status", vo.getStatus());

		QueueEntryVO queueEntryVO = vo.getQueueEntries().get(0);
		assertEquals("Mapping is wrong!", "descriptiveName", queueEntryVO.getDescriptiveName());
		assertEquals("Mapping is wrong!", "jobId", queueEntryVO.getJobId());
		assertEquals("Mapping is wrong!", "jobPartId", queueEntryVO.getJobPartId());
		assertEquals("Mapping is wrong!", 1, queueEntryVO.getPriority());
		assertEquals("Mapping is wrong!", "queueEntryId", queueEntryVO.getQueueEntryId());

		Date startTime = new Date(109, 8, 21, 15, 32, 48); // 2009-09-21 15:32:48
		assertEquals("Mapping is wrong!", startTime, queueEntryVO.getStartTime());

		assertEquals("Mapping is wrong!", "status", queueEntryVO.getStatus());

		Date submissionTime = new Date(109, 8, 21, 14, 31, 6); // 2009-09-21 14:31:06
		assertEquals("Mapping is wrong!", submissionTime, queueEntryVO.getSubmissionTime());
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.data.DevicesContextImpl#unmarshallDeviceList()}.
	 * @throws MappingException
	 * @throws IOException
	 * @throws ValidationException
	 * @throws MarshalException
	 */
	@SuppressWarnings("deprecation")
	public void testUnmarsahlMsgSubscription() throws MarshalException, ValidationException, IOException, MappingException {
		// load sample file
		URL url = DevicesContextImpl.class.getResource("/subscriptionList.xml");
		Reader reader = new FileReader(url.getFile());

		// unmarshal
		SubscriptionListVO lst = (SubscriptionListVO) devicesContextImpl.unmarshal(reader, SubscriptionListVO.class);

		// check result
		assertEquals("Size devices List is wrong.", 4, lst.getMsgSubscriptions().size());

		MsgSubscriptionVO vo = lst.getMsgSubscriptions().get(0);
		assertEquals("Mapping is wrong!", "channelId", vo.getChannelId());
		assertEquals("Mapping is wrong!", "family", vo.getFamily());
		assertEquals("Mapping is wrong!", "lastTime", vo.getLastTime());
		assertEquals("Mapping is wrong!", "messageType", vo.getMessageType());
		assertEquals("Mapping is wrong!", "queueEntryId", vo.getQueueEntryId());
		assertEquals("Mapping is wrong!", 1, vo.getRepeatStep());
		assertEquals("Mapping is wrong!", 2, vo.getRepeatTime());
		assertEquals("Mapping is wrong!", "senderId", vo.getSenderId());
		assertEquals("Mapping is wrong!", 3, vo.getSent());
		assertEquals("Mapping is wrong!", "type", vo.getType());
		assertEquals("Mapping is wrong!", "url", vo.getUrl());
	}
}
