/*
 * The CIP4 Software License, Version 1.0
 */
package org.cip4.bambi.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.core.BambiNSExtension.EPackageType;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.UrlUtil.URLProtocol;
import org.junit.Test;

public class BambiNSExtensionResidualTest extends BambiTestCaseBase
{
	@Test
	public void testMyNSElementAndStatusContainerHelpers()
	{
		final JDFQueueEntry qe = buildQueueEntry();

		assertNull(BambiNSExtension.getStatusContainer(qe));
		assertNull(BambiNSExtension.getCreateMyNSElement(null, "Foo", 0));
		assertNull(BambiNSExtension.getMyNSElement(null, "Foo", 0));
		assertNull(BambiNSExtension.removeMyNSElement(null, "Foo", 0));

		assertNotNull(BambiNSExtension.getCreateStatusContainer(qe));
		assertNotNull(BambiNSExtension.getStatusContainer(qe));
		assertNotNull(BambiNSExtension.removeMyNSElement(qe, "StatusContainer", 0));
		assertNull(BambiNSExtension.getStatusContainer(qe));
	}

	@Test
	public void testNullGuardsForAppendAndRemoveAttribute()
	{
		assertThrows(JDFException.class, () -> BambiNSExtension.appendMyNSAttribute(null, "foo", "bar"));
		assertThrows(JDFException.class, () -> BambiNSExtension.removeMyNSAttribute(null, "foo"));
	}

	@Test
	public void testSlaveFolderAndSubmissionTimeHelpers()
	{
		final JDFQueueEntry qe = buildQueueEntry();
		final KElement root = KElement.createRoot("Root", null);

		BambiNSExtension.setSlaveInputHF(root, "input-dir");
		BambiNSExtension.setSlaveOutputHF(root, "output-dir");
		BambiNSExtension.setSlaveErrorHF(root, "error-dir");

		assertEquals("input-dir", BambiNSExtension.getMyNSAttribute(root, "SlaveInputHF"));
		assertEquals("output-dir", BambiNSExtension.getMyNSAttribute(root, "SlaveOutputHF"));
		assertEquals("error-dir", BambiNSExtension.getMyNSAttribute(root, "SlaveErrorHF"));

		BambiNSExtension.setSlaveSubmissionTime(qe, new JDFDate());
		assertNotNull(BambiNSExtension.getSlaveSubmissionTime(qe));

		BambiNSExtension.setMyNSAttribute(qe, "SlaveSubmissionTime", "not-a-date");
		assertNull(BambiNSExtension.getSlaveSubmissionTime(qe));

		BambiNSExtension.setSlaveSubmissionTime(qe, null);
		assertNull(BambiNSExtension.getSlaveSubmissionTime(qe));
	}

	@Test
	public void testSlaveQueueEntryLookup()
	{
		final JDFDoc doc = new JDFDoc("JMF");
		final JDFJMF root = doc.getJMFRoot();
		final JDFResponse response = root.appendResponse();
		response.setType("Status");
		final JDFQueue queue = response.getCreateQueue(0);

		final JDFQueueEntry qe1 = queue.getCreateQueueEntry(0);
		final JDFQueueEntry qe2 = queue.getCreateQueueEntry(1);
		BambiNSExtension.setSlaveQueueEntryID(qe1, "slave-1");
		BambiNSExtension.setSlaveQueueEntryID(qe2, "slave-2");

		assertNull(BambiNSExtension.getSlaveQueueEntry(queue, "*"));
		assertEquals(qe1, BambiNSExtension.getSlaveQueueEntry(queue, "slave-1"));
		assertEquals(qe2, BambiNSExtension.getSlaveQueueEntry(queue, "slave-2"));
		assertNull(BambiNSExtension.getSlaveQueueEntry(queue, "slave-missing"));
	}

	@Test
	public void testDocModifiedPackageAndProtocolHelpers()
	{
		final KElement root = KElement.createRoot("Root", null);

		assertEquals(-1, BambiNSExtension.getDocModified(root));
		BambiNSExtension.setDocModified(root, 123456789L);
		assertEquals(123456789L, BambiNSExtension.getDocModified(root));

		BambiNSExtension.setPackageType(root, EPackageType.XJMF);
		assertEquals(EPackageType.XJMF, BambiNSExtension.getPackageType(root));

		BambiNSExtension.setProtocolType(root, URLProtocol.https);
		assertEquals(URLProtocol.https, BambiNSExtension.getProtocolType(root));
	}

	private JDFQueueEntry buildQueueEntry()
	{
		final JDFDoc doc = new JDFDoc("JMF");
		final JDFJMF root = doc.getJMFRoot();
		final JDFResponse response = root.appendResponse();
		response.setType("Status");
		return response.getCreateQueue(0).getCreateQueueEntry(0);
	}
}