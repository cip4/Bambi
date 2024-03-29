/**
 * The CIP4 Software License, Version 1.0
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
package org.cip4.bambi.server;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.MultiDeviceProperties;
import org.cip4.jdflib.util.file.UserDir;
import org.cip4.jdfutility.logging.LogConfigurator;
import org.cip4.jdfutility.server.JettyServer;
import org.cip4.jdfutility.server.ui.JettyFrame;

/**
 * @author rainer prosi
 * @date Jan 11, 2011
 */
public class BambiFrame extends JettyFrame
{
	private static final long serialVersionUID = 1L;
	private JButton baseDirButton;
	private JButton extractXsltButton;
	private JTextField baseDirText;

	public BambiFrame(final JettyServer server)
	{
		super(server);
	}

	/**
	 *
	 */
	protected void browse()
	{
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setSelectedFile(getProp().getBaseDir());
		final int i = fc.showOpenDialog(this);
		if (i == JFileChooser.APPROVE_OPTION)
		{
			final File f = fc.getSelectedFile();
			getProp().setBaseDir(f);
			setBaseDirText(f);
			LogConfigurator.configureLog(FilenameUtils.concat(getProp().getBaseDir().getAbsolutePath(), "logs"), "bambi.log");
		}
	}

	private MultiDeviceProperties getProp()
	{
		return ((BambiServer) server).getProp();
	}

	/**
	 * @see org.cip4.jdfutility.server.ui.JettyFrame#getFrameName()
	 */
	@Override
	protected String getFrameName()
	{
		retrieveVersion();
		return "Bambi, version: " + RuntimeProperties.productVersion + ", date: " + RuntimeProperties.productBuildTimestamp;
	}

	/**
	 * Read applications version details.
	 */
	static void retrieveVersion()
	{
		final Properties propsVersion = new Properties();

		try
		{
			propsVersion.load(BambiFrame.class.getResourceAsStream("/bambi-buildtime.properties"));
		}
		catch (final Exception e)
		{
			LogFactory.getLog(BambiFrame.class).error("Error reading bambi-buildtime.properties: " + e.getMessage(), e);
			propsVersion.put("release.version", "BambiVersion");
			propsVersion.put("release.build.timestamp", "Time");
			propsVersion.put("release.build.number", "Build");
		}

		RuntimeProperties.setProductVersion(propsVersion.getProperty("release.version"));
		RuntimeProperties.setProductBuildTimestamp(propsVersion.getProperty("release.build.timestamp"));
		RuntimeProperties.setProductBuildNumber(propsVersion.getProperty("release.build.number"));
	}

	/**
	 * @see org.cip4.jdfutility.server.ui.JettyFrame#createPanel()
	 */
	@Override
	protected JPanel createPanel()
	{
		final JPanel panel = super.createPanel();

		extractXsltButton = new JButton("Extract all resources");
		extractXsltButton.setToolTipText("Bambi restart suggested");
		extractXsltButton.addActionListener(this);
		panel.add(extractXsltButton);

		baseDirButton = new JButton("Browse Base Directory");
		baseDirButton.addActionListener(this);
		panel.add(baseDirButton);

		baseDirText = new JTextField();
		panel.add(baseDirText);
		setBaseDirText(getProp().getBaseDir());

		return panel;
	}

	/**
	 * @param baseDir
	 */
	private void setBaseDirText(final File baseDir)
	{
		baseDirText.setText(baseDir.getAbsolutePath());
		baseDirText.setEditable(false);
	}

	@Override
	public void actionPerformed(final ActionEvent event)
	{
		if (baseDirButton.equals(event.getSource()))
		{
			browse();
		}
		else if (extractXsltButton.equals(event.getSource()))
		{
			extractResources();
		}
		else
		{
			super.actionPerformed(event);
		}
	}

	/**
	 * Extract all listed files, overwriting existing.
	 */
	private void extractResources()
	{
		final Class<? extends BambiFrame> myClass = getClass();
		final InputStream listStream = myClass.getResourceAsStream(BambiServer.RESOURCES_FILE);
		final UserDir userDir = new UserDir(BambiServer.BAMBI);
		final File toolDir = new File(userDir.getToolPath());
		BambiServer.unpackLines(myClass, toolDir, listStream);
		setTitle(getFrameName()); // set title as release info can be changed after extracting
	}

	/**
	 * @see org.cip4.jdfutility.server.ui.JettyFrame#started()
	 */
	@Override
	protected void started()
	{
		super.started();
		baseDirButton.setEnabled(false);
		extractXsltButton.setEnabled(false);
	}

	/**
	 * @see org.cip4.jdfutility.server.ui.JettyFrame#stopped()
	 */
	@Override
	protected void stopped()
	{
		super.stopped();
		baseDirButton.setEnabled(true);
		extractXsltButton.setEnabled(true);
	}
}
