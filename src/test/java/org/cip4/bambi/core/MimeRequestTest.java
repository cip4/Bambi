/*
 * The CIP4 Software License, Version 1.0
 */
package org.cip4.bambi.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.mime.MimeReader;
import org.junit.Test;

import jakarta.mail.Multipart;

public class MimeRequestTest extends BambiTestCaseBase
{
	@Test
	public void testConstructorWithMimeReaderSetsMultipartRelatedContentType()
	{
		final MimeReader reader = mock(MimeReader.class);
		final MimeRequest request = new MimeRequest(reader);

		assertSame(reader, request.getReader());
		assertEquals(MimeUtil.MULTIPART_RELATED, request.getContentType(true));
	}

	@Test
	public void testConstructorWithMultipartCreatesReader()
	{
		final Multipart multipart = createMinimalMultipart();
		final MimeRequest request = new MimeRequest(multipart);

		assertNotNull(request.getReader());
		assertEquals(MimeUtil.MULTIPART_RELATED, request.getContentType(true));
	}

	@Test
	public void testToStringContainsMimePartCount()
	{
		final MimeReader reader = mock(MimeReader.class);
		when(reader.getCount()).thenReturn(7);
		final MimeRequest request = new MimeRequest(reader);

		final String text = request.toString();
		assertTrue(text.contains("MimeRequest"));
		assertTrue(text.endsWith("\n7"));
	}

	@Test
	public void testToStringWithZeroParts()
	{
		final MimeReader reader = mock(MimeReader.class);
		when(reader.getCount()).thenReturn(0);
		final MimeRequest request = new MimeRequest(reader);

		assertTrue(request.toString().endsWith("\n0"));
	}

	private Multipart createMinimalMultipart()
	{
		final JDFDoc jmfDoc = new JDFDoc("JMF");
		final JDFJMF jmf = jmfDoc.getJMFRoot();
		jmf.appendCommand().setType("KnownMessages");

		final JDFDoc jdfDoc = new JDFDoc("JDF");
		jdfDoc.getJDFRoot().setType("ConventionalPrinting", true);
		return MimeUtil.buildMimePackage(jmfDoc, jdfDoc, false);
	}
}