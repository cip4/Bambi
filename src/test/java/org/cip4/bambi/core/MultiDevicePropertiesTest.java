/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2022 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.util.PlatformUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Enclosed.class)
public class MultiDevicePropertiesTest extends BambiTestCaseBase {
    @RunWith(Parameterized.class)
    public static class PathTests {

        private final String toolPath;
        private final String appDir;
        private final String configuredDir;
        private final String expectedPath;
        private MultiDeviceProperties properties;

        public PathTests(String toolPath, String appDir, String configuredDir, String expectedPath) {
            this.toolPath = toolPath;
            this.appDir = appDir;
            this.configuredDir = configuredDir;
            this.expectedPath = expectedPath;
        }

        @Parameterized.Parameters
        public static Collection<String[]> data() {
            if (PlatformUtil.isWindows()) {
                return Arrays.asList(
                        // AppDir, BaseDir, Expected path
                        new String[]{"C:\\t", "C:\\a", "", "C:\\t"},
                        new String[]{"C:\\t", "C:\\a", "C:\\b\\c", "C:\\b\\c"},
                        new String[]{"C:\\t", "C:\\a", "b\\c", "C:\\t\\b\\c"},
                        new String[]{"C:\\t", "C:\\a", "b/c", "C:\\t\\b\\c"},
                        new String[]{"C:\\t", "/a", "b/c", "C:\\t\\b\\c"},
                        new String[]{"C:\\t", "a", "b/c", "C:\\t\\b\\c"},
                        new String[]{"/t", "a", "b/c", "\\t\\b\\c"},
                        new String[]{"t", "C:\\a", "b\\c", "C:\\a\\t\\b\\c"}
                );
            } else {
                return Arrays.asList(
                        // AppDir, BaseDir, Expected path
                        new String[]{"/t", "/a", "", "/t"},
                        new String[]{"/t", "/a", "/b/c", "/b/c"},
                        new String[]{"/t", "/a", "b/c", "/a/b/c"},
                        new String[]{"/t", "/a", "b/c", "/a/b/c"}
                );
            }
        }

        @Before
        public void init()
        {
            final XMLDoc doc = new XMLDoc("application", null);
            doc.setOriginalFileName("foo");
            final KElement root = doc.getRoot();
            root.setAttribute("BaseDir", configuredDir);
            root.setAttribute("AppDir", appDir);
            root.setAttribute("InputHF", configuredDir);
            root.setAttribute("OutputHF", configuredDir);
            root.setAttribute("ErrorHF", configuredDir);
            properties = new MultiDeviceProperties(doc, Paths.get(this.toolPath));
        }

        @Test
        public void getBaseDir() {
            assertEquals(new File(expectedPath), properties.getBaseDir());
        }

        @Test
        public void getErrorHF() {
            assertEquals(new File(expectedPath), properties.createDeviceProps(null).getErrorHF());
        }

        @Test
        public void getOutputHF() {
            assertEquals(new File(expectedPath), properties.createDeviceProps(null).getOutputHF());
        }

        @Test
        public void getInputHF() {
            assertEquals(new File(expectedPath), properties.createDeviceProps(null).getInputHF());
        }
    }

    public static class NormalTests
    {

        @Test
        public void testContextURL() {
            final XMLDoc d = new XMLDoc("application", null);
            d.setOriginalFileName("foo");
            final MultiDeviceProperties p = new MultiDeviceProperties(d);
            System.setProperty("CIP4_BAMBI_BASE_URL", "foo");
            assertEquals("foo/null", p.getContextURL());
            System.getProperties().remove("CIP4_BAMBI_BASE_URL");
            assertNotEquals("Foo/null", p.getContextURL());

        }

        @Test
        public void testPersist() {
            final XMLDoc d = new XMLDoc("application", null);
            d.setOriginalFileName(sm_dirTestDataTemp + "foo.ini");
            final MultiDeviceProperties p = new MultiDeviceProperties(d);
            p.persist();
            p.persist();
            assertTrue(new File(sm_dirTestDataTemp + "foo.1.ini").exists());
        }

        @Test
        public void testIsCompatible() {
            final XMLDoc d = new XMLDoc("application", null);
            d.setOriginalFileName(sm_dirTestDataTemp + "foo.ini");
            final MultiDeviceProperties p = new MultiDeviceProperties(d);
            assertTrue(p.isCompatible(p));
        }

        @Test
        public void testIsCompatibleNot() {
            final XMLDoc d = new XMLDoc("application", null);
            d.setOriginalFileName(sm_dirTestDataTemp + "foo.ini");
            final MultiDeviceProperties p = new MultiDeviceProperties(d);
            final XMLDoc d2 = d.clone();
            d2.getRoot().setAttribute(MultiDeviceProperties.CONFIG_VERSION, "v1");
            final MultiDeviceProperties p2 = new MultiDeviceProperties(d2);
            assertFalse(p.isCompatible(p2));
            d.getRoot().setAttribute(MultiDeviceProperties.CONFIG_VERSION, "v2");
            assertFalse(p.isCompatible(p2));
            d.getRoot().setAttribute(MultiDeviceProperties.CONFIG_VERSION, "v1");
            assertTrue(p.isCompatible(p2));
        }
    }
}
