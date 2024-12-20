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
package org.cip4.bambi.core.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.zip.ZipEntry;

import javax.mail.BodyPart;
import javax.mail.MessagingException;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.ConverterCallback;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumDeviceDetails;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumJobDetails;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.extensions.MessageHelper;
import org.cip4.jdflib.extensions.XJMFHelper;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JMFBuilderFactory;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.net.HTTPDetails;
import org.cip4.jdflib.util.zip.ZipReader;
import org.cip4.lib.jdf.jsonutil.JSONObjHelper;
import org.junit.Test;

public class MessageDetailsTest extends BambiTestCaseBase
{

	/**
	 * @throws Throwable
	 *
	 */
	@Test
	public void testCBDetails() throws Throwable
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		final MessageDetails md = new MessageDetails(jmf, null, new MyTestCallback(), null, "abc");
		final KElement root = new JDFDoc("Root").getRoot();
		md.appendToXML(root, 2, false);
		assertEquals("b", root.getXPathAttribute("Message/CBDetails/@a", null));
		final MessageDetails md2 = new MessageDetails(root.getElement("Message"));
		assertEquals("b", md2.callback.getCallbackDetails().get("a"));
	}

	/**
	 *
	 */
	@Test
	public void testGetContentType()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://foo");
		assertEquals(UrlUtil.VND_JMF, md.getContentType());
	}

	/**
	 *
	 */
	@Test
	public void testGetContentTypeJSON()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final ConverterCallback cb = new ConverterCallback();
		cb.setJSON(true);
		cb.setFixToExtern(EnumVersion.Version_2_1);
		final MessageDetails md = new MessageDetails(jmf, null, cb, null, "http://foo");
		assertEquals(UrlUtil.VND_XJMF_J, md.getContentType());
	}

	/**
	 *
	 */
	@Test
	public void testGetXML()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final ConverterCallback cb = new ConverterCallback();
		cb.setJSON(true);
		cb.setFixToExtern(EnumVersion.Version_2_1);
		final MessageDetails md = new MessageDetails(jmf, null, cb, null, "http://foo");
		final KElement list = JDFElement.createRoot("L");
		md.appendToXML(list, 0, false);
		final MessageDetails md2 = new MessageDetails(list.getElement(null));
		assertEquals(md.callback.getCallbackDetails(), md2.callback.getCallbackDetails());
	}

	/**
	 *
	 */
	@Test
	public void testGetJSON()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final ConverterCallback cb = new ConverterCallback();
		cb.setJSON(true);
		cb.setFixToExtern(EnumVersion.Version_2_1);
		final MessageDetails md = new MessageDetails(jmf, null, cb, null, "http://foo");
		final KElement list = JDFElement.createRoot("L");
		md.appendToXML(list, 0, false);
		final MessageDetails md2 = new MessageDetails(list.getElement(null));
		final InputStream is = md2.getInputStream();
		final JSONObjHelper oh = new JSONObjHelper(is);
		assertNotNull(oh.getRootObject());
	}

	/**
	 *
	 */
	@Test
	public void testNameJMF()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://foo");
		assertEquals("Status." + jmf.getSenderID(), md.getName());
	}

	/**
	 *
	 */
	@Test
	public void testNameMilestone()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildMilestone("grunz", "j1");
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://foo");
		assertTrue(md.getName().startsWith("Milestone_grunz"));
	}

	/**
	 *
	 */
	@Test
	public void testNameResource()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildResourceSignal(true, null);
		jmf.getSignal(0).getCreateResourceInfo(0).appendResource(ElementName.EXPOSEDMEDIA);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://foo");
		assertTrue(md.getName().startsWith("Resource_ExposedMedia"));
	}

	/**
	 *
	 */
	@Test
	public void testNameElem()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final KElement message = new JDFDoc("Message").getRoot();
		message.copyElement(jmf, null);
		final MessageDetails md = new MessageDetails(message);
		assertTrue(md.getName().startsWith("Status"));
	}

	/**
	 *
	 */
	@Test
	public void testEncoding()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final KElement message = new JDFDoc("Message").getRoot();
		message.appendElement(MessageDetails.MIME).setAttribute(AttributeName.ENCODING, "foo");
		message.copyElement(jmf, null);
		final MessageDetails md = new MessageDetails(message);
		assertEquals("foo", md.mimeDet.transferEncoding);
	}

	/**
	 *
	 */
	@Test
	public void testHTTPHeader()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final KElement message = new JDFDoc("Message").getRoot();
		message.appendElement(MessageDetails.MIME).appendElement(MessageDetails.HTTP).setAttribute("bar", "foo");
		message.copyElement(jmf, null);
		final MessageDetails md = new MessageDetails(message);
		assertEquals("foo", md.mimeDet.httpDetails.getHeader("bar"));
	}

	/**
	 *
	 */
	@Test
	public void testEncodingRound()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final KElement message = new JDFDoc("Message").getRoot();
		message.copyElement(jmf, null);
		final MessageDetails md = new MessageDetails(message);
		md.mimeDet = new MIMEDetails();
		md.mimeDet.transferEncoding = "fnarf";
		final KElement msgs = new JDFDoc("r").getRoot();
		md.appendToXML(msgs, 0, false);
		final MessageDetails md2 = new MessageDetails(msgs.getElement("Message"));

		assertEquals("fnarf", md2.mimeDet.transferEncoding);
	}

	/**
	 *
	 */
	@Test
	public void testHTTPRound()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final KElement message = new JDFDoc("Message").getRoot();
		message.copyElement(jmf, null);
		final MessageDetails md = new MessageDetails(message);
		md.mimeDet = new MIMEDetails();
		md.mimeDet.httpDetails = new HTTPDetails();
		md.mimeDet.httpDetails.setHeader("a", "c");
		final KElement msgs = new JDFDoc("r").getRoot();
		md.appendToXML(msgs, 0, false);
		final MessageDetails md2 = new MessageDetails(msgs.getElement("Message"));

		assertEquals("c", md2.mimeDet.httpDetails.getHeader("a"));
	}

	/**
	 *
	 */
	@Test
	public void testGetStream()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://foo");
		final InputStream is = md.getInputStream();
		assertNotNull(JDFDoc.parseStream(is).getJMFRoot());
	}

	/**
	 *
	 */
	@Test
	public void testContentTypeZip()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		final JDFNode jdf = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf").getJDFRoot();
		final ConverterCallback cb = new ConverterCallback();
		cb.setFixToExtern(EnumVersion.Version_2_0);
		final MessageDetails md = new MessageDetails(jmf, jdf, null, cb, null, "http://foo");
		assertEquals(UrlUtil.APPLICATION_ZIP, md.getContentType());
	}

	/**
	 *
	 */
	@Test
	public void testContentTypeMime()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		final JDFNode jdf = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf").getJDFRoot();
		final ConverterCallback cb = new ConverterCallback();
		final MessageDetails md = new MessageDetails(jmf, jdf, null, cb, null, "http://foo");
		assertEquals(MimeUtil.MULTIPART_RELATED, md.getContentType());
	}

	/**
	 *
	 */
	@Test
	public void testContentTypeXJMF()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		final ConverterCallback cb = new ConverterCallback();
		cb.setFixToExtern(EnumVersion.Version_2_0);
		final MessageDetails md = new MessageDetails(jmf, null, cb, null, "http://foo");
		assertEquals(UrlUtil.VND_XJMF, md.getContentType());
	}

	/**
	 *
	 */
	@Test
	public void testContentTypeXJMFMax()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		jmf.setMaxVersion(EnumVersion.Version_2_1);
		final ConverterCallback cb = new ConverterCallback();
		final MessageDetails md = new MessageDetails(jmf, null, cb, null, "http://foo");
		assertEquals(UrlUtil.VND_XJMF, md.getContentType());
	}

	/**
	 *
	 */
	@Test
	public void testContentTypeXJMFJSON()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		jmf.setMaxVersion(EnumVersion.Version_2_1);
		BambiNSExtension.setJSON(jmf, true);
		final ConverterCallback cb = new ConverterCallback();
		final MessageDetails md = new MessageDetails(jmf, null, cb, null, "http://foo");
		assertEquals(UrlUtil.VND_XJMF_J, md.getContentType());
	}

	/**
	 *
	 */
	@Test
	public void testGetStreamJSON()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final ConverterCallback cb = new ConverterCallback();
		jmf.setMaxVersion(EnumVersion.Version_2_1);
		BambiNSExtension.setJSON(jmf, true);
		final MessageDetails md = new MessageDetails(jmf, null, cb, null, "http://foo");
		assertEquals(UrlUtil.VND_XJMF_J, md.getContentType());
		final JSONObjHelper h = new JSONObjHelper(md.getInputStream());
		assertNotNull(h.getRoot());
		assertNull(h.getString("XJMF/bambi:json"));
		assertEquals(-1, h.toJSONString().indexOf("bambi"));
	}

	/**
	 * @throws IOException
	 *
	 */
	@Test
	public void testStreamZip() throws IOException
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		jmf.getCommand(0).getQueueSubmissionParams(0).setURL("dummy");
		final JDFNode jdf = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf").getJDFRoot();
		final ConverterCallback cb = new ConverterCallback();
		cb.setFixToExtern(EnumVersion.Version_2_0);
		final MessageDetails md = new MessageDetails(jmf, jdf, null, cb, null, "http://foo");
		final InputStream is = md.getInputStream();
		final ZipReader zr = ZipReader.getZipReader(is);
		zr.buffer();
		assertNotNull(zr);
		final Vector<ZipEntry> entries = zr.getEntries();
		assertEquals(entries.size(), 2);
		assertNotNull(zr.getMatchingEntry("root.xjmf", 0));
		final XMLDoc xjmf = zr.getXMLDoc();
		final XJMFHelper h = XJMFHelper.getHelper(xjmf);
		assertNotNull(h);

		final MessageHelper mh = h.getMessageHelper(0);
		assertEquals("xjdf/2893.00.1.xjdf", mh.getXPathValue("QueueSubmissionParams/@URL"));
		assertNotNull(zr.getMatchingEntry("*.xjdf", 0));
		assertEquals('P', is.read());

	}

	/**
	 * @throws IOException
	 *
	 */
	@Test
	public void testStreamZip2() throws IOException
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		jmf.getCommand(0).getQueueSubmissionParams(0).setURL("dummy");
		final JDFNode jdf = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf").getJDFRoot();
		jdf.setMaxVersion(EnumVersion.Version_2_1);
		jmf.setMaxVersion(EnumVersion.Version_2_1);
		final ConverterCallback cb = new ConverterCallback();
		final MessageDetails md = new MessageDetails(jmf, jdf, null, cb, null, "http://foo");
		final InputStream is = md.getInputStream();
		final ZipReader zr = ZipReader.getZipReader(is);
		zr.buffer();
		assertNotNull(zr);
		final Vector<ZipEntry> entries = zr.getEntries();
		assertEquals(entries.size(), 2);
		assertNotNull(zr.getMatchingEntry("*.xjmf", 0));
		final XMLDoc xjmf = zr.getXMLDoc();
		final XJMFHelper h = XJMFHelper.getHelper(xjmf);
		assertNotNull(h);

		final MessageHelper mh = h.getMessageHelper(0);
		assertEquals("xjdf/2893.00.1.xjdf", mh.getXPathValue("QueueSubmissionParams/@URL"));
		assertNotNull(zr.getMatchingEntry("*.xjdf", 0));
		assertEquals('P', is.read());

	}

	/**
	 * @throws IOException
	 *
	 */
	@Test
	public void testStreamZipReturn() throws IOException
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildReturnQueueEntry("q1");
		jmf.getCommand(0).getReturnQueueEntryParams(0).setURL("dummy");
		final JDFNode jdf = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf").getJDFRoot();
		jdf.setMaxVersion(EnumVersion.Version_2_1);
		jmf.setMaxVersion(EnumVersion.Version_2_1);
		final ConverterCallback cb = new ConverterCallback();
		final MessageDetails md = new MessageDetails(jmf, jdf, null, cb, null, "http://foo");
		final InputStream is = md.getInputStream();
		final ByteArrayIOStream bos = new ByteArrayIOStream(is);

		final ZipReader zr = ZipReader.getZipReader(bos.getInputStream());
		zr.buffer();
		assertNotNull(zr);
		FileUtil.streamToFile(bos.getInputStream(), sm_dirTestDataTemp + "ret.zip");
		zr.buffer();
		final Vector<ZipEntry> entries = zr.getEntries();
		assertEquals(entries.size(), 2);
		assertNotNull(zr.getMatchingEntry("*.xjmf", 0));
		final XMLDoc xjmf = zr.getXMLDoc();
		final XJMFHelper h = XJMFHelper.getHelper(xjmf);
		assertNotNull(h);

		final MessageHelper mh = h.getMessageHelper(0);
		assertEquals("xjdf/2893.00.1.xjdf", mh.getXPathValue("ReturnQueueEntryParams/@URL"));
		assertNotNull(zr.getMatchingEntry("*.xjdf", 0));
		assertEquals('P', is.read());

	}

	/**
	 * @throws Throwable
	 *
	 */
	@Test
	public void testStreamNoZip() throws Throwable
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		final JDFNode jdf = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf").getJDFRoot();
		final ConverterCallback cb = new MyTestCallback();
		cb.setFixToExtern(EnumVersion.Version_2_0);
		final MessageDetails md = new MessageDetails(jmf, jdf, null, cb, null, "http://foo");
		assertEquals(MimeUtil.MULTIPART_RELATED, md.getContentType());
		final InputStream is = md.getInputStream();
		assertNotSame('P', is.read());
	}

	/**
	 *
	 */
	@Test
	public void testStreamMime()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		final JDFNode jdf = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf").getJDFRoot();
		final ConverterCallback cb = new ConverterCallback();
		final MessageDetails md = new MessageDetails(jmf, jdf, null, cb, null, "http://foo");
		final InputStream is = md.getInputStream();
		final BodyPart[] bp = MimeUtil.extractMultipartMime(is);

		assertNotNull(bp);
		assertEquals(bp.length, 2);
	}

	/**
	 * @throws MessagingException
	 *
	 */
	@Test
	public void testStreamMimeNastyName() throws MessagingException
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		final JDFDoc doc = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf");
		doc.setOriginalFileName("\\\\/%#@123€");
		final JDFNode jdf = doc.getJDFRoot();

		final ConverterCallback cb = new ConverterCallback();
		final MessageDetails md = new MessageDetails(jmf, jdf, null, cb, null, "http://foo");
		final InputStream is = md.getInputStream();
		final BodyPart[] bp = MimeUtil.extractMultipartMime(is);

		assertNotNull(bp);
		assertEquals(bp.length, 2);
		final BodyPart bpJDF = bp[1];
		assertEquals("<TheJDF.jdf>", bpJDF.getHeader("Content-ID")[0]);
		assertEquals("attachment; filename=TheJDF.jdf", bpJDF.getHeader("Content-Disposition")[0]);
	}

	class ExtendCallback extends ConverterCallback
	{

		@Override
		public boolean isExtendReferenced()
		{
			return true;
		}

	}

	/**
	 *
	 */
	@Test
	public void testStreamMimeExtend()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		final JDFNode jdf = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf").getJDFRoot();
		final ConverterCallback cb = new ExtendCallback();

		final MessageDetails md = new MessageDetails(jmf, jdf, null, cb, null, "http://foo");
		final InputStream is = md.getInputStream();
		final BodyPart[] bp = MimeUtil.extractMultipartMime(is);

		assertNotNull(bp);
		assertEquals(bp.length, 2);
	}
}
