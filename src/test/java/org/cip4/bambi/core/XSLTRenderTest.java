/*
 * The CIP4 Software License, Version 1.0
 */
package org.cip4.bambi.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.util.UrlUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XSLTRenderTest extends BambiTestCaseBase
{
	private static final String XHTML_NS = "http://www.w3.org/1999/xhtml";
	private static final String JDF_NS = "http://www.CIP4.org/JDFSchema_1_1";

	private File legacyDir;

	@Before
	public void initLegacyDir() throws URISyntaxException
	{
		legacyDir = new File(getClass().getResource("/legacy").toURI());
	}

	@Test
	public void testDeviceList() throws Exception
	{
		final XMLDoc baseDoc = new XMLDoc("DeviceList", null);
		assertRendersXhtml("deviceList.xsl", baseDoc.getRoot());
	}

	@Test
	public void testShowDevice() throws Exception
	{
		final XMLDoc baseDoc = new XMLDoc("XMLDevice", null);
		assertRendersXhtml("showDevice.xsl", baseDoc.getRoot());
	}

	@Test
	public void testLogin() throws Exception
	{
		final XMLDoc baseDoc = new XMLDoc("XMLDevice", null);
		assertRendersXhtml("login.xsl", baseDoc.getRoot());
	}

	@Test
	public void testSubscriptionList() throws Exception
	{
		final XMLDoc baseDoc = new XMLDoc("SubscriptionList", null);
		assertRendersXhtml("subscriptionList.xsl", baseDoc.getRoot());
	}

	@Test
	public void testSubscriptionDetails() throws Exception
	{
		final XMLDoc baseDoc = new XMLDoc("SubscriptionList", null);
		assertRendersXhtml("subscriptionDetails.xsl", baseDoc.getRoot());
	}

	@Test
	public void testQueue2html() throws Exception
	{
		final XMLDoc baseDoc = new XMLDoc("Queue", JDF_NS);
		assertRendersXhtml("queue2html.xsl", baseDoc.getRoot());
	}

	private void assertRendersXhtml(final String xslName, final KElement baseRoot) throws Exception
	{
		baseRoot.setAttribute("Context", "/bambi");
		baseRoot.setAttribute("DeviceID", "test");

		final File xslFile = new File(legacyDir, xslName);
		final XMLDoc xslDoc = XMLDoc.parseFile(xslFile);
		assertNotNull("Could not parse stylesheet: " + xslFile.getAbsolutePath(), xslDoc);
		xslDoc.setOriginalFileName(xslFile.toURI().toString());

		final XMLResponse response = new XMLResponse(baseRoot);
		response.setXSLT(xslDoc);

		final InputStream responseStream = response.getInputStream();
		assertNotNull("XMLResponse produced no stream for stylesheet: " + xslName, responseStream);
		final String output;
		try (InputStream is = responseStream)
		{
			output = new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}

		assertEquals("Transform fallback detected for stylesheet: " + xslName, UrlUtil.TEXT_HTML, response.getContentType());

		final Document htmlDoc = parseNamespaceAware(output);
		final Element root = htmlDoc.getDocumentElement();
		assertNotNull("No document element in transform output for stylesheet: " + xslName, root);
		assertEquals("Unexpected root local name for stylesheet: " + xslName, "html", root.getLocalName());
		assertEquals("Unexpected root namespace for stylesheet: " + xslName, XHTML_NS, root.getNamespaceURI());

		final List<String> missingNs = new ArrayList<>();
		collectElementsMissingNamespace(root, missingNs);
		assertTrue("Elements with missing namespace in stylesheet " + xslName + ": " + missingNs, missingNs.isEmpty());

		assertTrue("No XHTML link elements found for stylesheet: " + xslName, htmlDoc.getElementsByTagNameNS(XHTML_NS, "link").getLength() > 0);
		assertTrue("No XHTML script elements found for stylesheet: " + xslName, htmlDoc.getElementsByTagNameNS(XHTML_NS, "script").getLength() > 0);
		assertTrue("No XHTML meta elements found for stylesheet: " + xslName, htmlDoc.getElementsByTagNameNS(XHTML_NS, "meta").getLength() > 0);
	}

	private Document parseNamespaceAware(final String xml) throws Exception
	{
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setValidating(false);
		dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		final DocumentBuilder builder = dbf.newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(xml)));
	}

	private void collectElementsMissingNamespace(final Element element, final List<String> missingNs)
	{
		if (element.getNamespaceURI() == null || element.getNamespaceURI().isEmpty())
		{
			missingNs.add(element.getTagName());
		}

		final NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			final Node child = children.item(i);
			if (child instanceof Element)
			{
				collectElementsMissingNamespace((Element) child, missingNs);
			}
		}
	}
}