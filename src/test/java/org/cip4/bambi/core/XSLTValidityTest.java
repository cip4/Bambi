/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2026 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.cip4.bambi.BambiTestCaseBase;
import org.junit.Before;
import org.junit.Test;

public class XSLTValidityTest extends BambiTestCaseBase
{
	private File legacyDir;
	private List<File> xslFiles;

	@Before
	public void initLegacyStylesheets() throws Exception
	{
		final URL u = getClass().getResource("/legacy");
		if (u == null)
		{
			fail("Could not resolve /legacy on test classpath");
		}

		legacyDir = new File(u.toURI());
		if (!legacyDir.isDirectory())
		{
			fail("Resolved /legacy is not a directory: " + legacyDir.getAbsolutePath());
		}

		xslFiles = discoverXslFiles(legacyDir.toPath());
	}

	@Test
	public void testAllStylesheetsWellFormed()
	{
		final List<String> failures = new ArrayList<>();
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);

		try
		{
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		}
		catch (final Exception x)
		{
			fail("Could not configure secure XML parser features: " + x.getMessage());
		}

		for (final File xslFile : xslFiles)
		{
			try (final InputStream in = new FileInputStream(xslFile))
			{
				final DocumentBuilder db = dbf.newDocumentBuilder();
				db.parse(in);
			}
			catch (final Exception x)
			{
				failures.add(toRelativePath(xslFile) + " -> " + x.getClass().getSimpleName() + ": " + x.getMessage());
			}
		}

		assertTrue(buildFailureMessage("Well-formedness failures", failures), failures.isEmpty());
	}

	@Test
	public void testAllStylesheetsCompile()
	{
		final List<String> failures = new ArrayList<>();

		for (final File xslFile : xslFiles)
		{
			try
			{
				final TransformerFactory factory = TransformerFactory.newInstance();
				factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
				// StreamSource(File) sets systemId so relative xsl:include resolution works.
				factory.newTransformer(new StreamSource(xslFile));
			}
			catch (final Exception x)
			{
				failures.add(toRelativePath(xslFile) + " -> " + x.getClass().getSimpleName() + ": " + x.getMessage());
			}
		}

		assertTrue(buildFailureMessage("XSLT compilation failures", failures), failures.isEmpty());
	}

	@Test
	public void testStylesheetCountSanity()
	{
		assertTrue("Expected at least 20 .xsl files under /legacy but found " + xslFiles.size(), xslFiles.size() >= 20);
	}

	private List<File> discoverXslFiles(final Path root) throws Exception
	{
		try (final Stream<Path> pathStream = Files.walk(root))
		{
			return pathStream.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".xsl"))
					.sorted()
					.map(Path::toFile)
					.collect(Collectors.toList());
		}
	}

	private String toRelativePath(final File file)
	{
		return legacyDir.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/');
	}

	private String buildFailureMessage(final String label, final List<String> failures)
	{
		if (failures.isEmpty())
		{
			return label;
		}
		return label + " (" + failures.size() + "):\n" + String.join("\n", failures);
	}
}
