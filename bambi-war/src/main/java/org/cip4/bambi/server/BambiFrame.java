/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2014 The International Cooperation for the Integration of 
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
package org.cip4.bambi.server;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cip4.bambi.core.BambiContainer;
import org.cip4.bambi.core.MultiDeviceProperties;
import org.cip4.jdflib.util.logging.LogConfigurator;
import org.cip4.jdfutility.server.JettyServer;
import org.cip4.jdfutility.server.ui.JettyFrame;

/**
 * @author rainer prosi
 * @date Jan 11, 2011
 */
public class BambiFrame extends JettyFrame
{
	JButton baseDirButton;
	JTextField baseDirText;
	JCheckBox cbLegacy;

	/**
	 * @param server
	 */
	public BambiFrame(JettyServer server)
	{
		super(server);
	}

	/**
	 * 
	 */
	protected void browse()
	{
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setSelectedFile(getProp().getBaseDir());
		int i = fc.showOpenDialog(this);
		if (i == JFileChooser.APPROVE_OPTION)
		{
			File f = fc.getSelectedFile();
			getProp().setBaseDir(f);
			setBaseDirText(f);
			LogConfigurator.configureLog(getProp().getBaseDir().getAbsolutePath(), "bambi.log");
		}
	}

	/**
	 * 
	 * @return
	 */
	MultiDeviceProperties getProp()
	{
		return ((BambiServer) server).getProp();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @see org.cip4.jdfutility.server.ui.JettyFrame#getFrameName()
	 */
	@Override
	protected String getFrameName()
	{
		return "Bambi";
	}

	/**
	 * 
	 * @see org.cip4.jdfutility.server.ui.JettyFrame#createPanel()
	 */
	@Override
	protected JPanel createPanel()
	{
		JPanel panel = super.createPanel();
		baseDirButton = new JButton("Browse Base Directory");
		baseDirButton.addActionListener(this);
		panel.add(baseDirButton);
		baseDirText = new JTextField();
		panel.add(baseDirText);
		setBaseDirText(getProp().getBaseDir());
		cbLegacy = new JCheckBox("Legacy style");
		cbLegacy.setSelected("/legacy".equals(getProp().getCSS()));
		cbLegacy.addActionListener(this);
		panel.add(cbLegacy);
		return panel;
	}

	/**
	 * 
	 * @param baseDir
	 */
	private void setBaseDirText(File baseDir)
	{
		baseDirText.setText(baseDir.getAbsolutePath());
		baseDirText.setEditable(false);
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (baseDirButton.equals(arg0.getSource()))
		{
			browse();
		}
		else if (cbLegacy.equals(arg0.getSource()))
		{
			getProp().setCSS(cbLegacy.isSelected() ? "/legacy" : "/webapp");
			getProp().serialize();
			if (BambiContainer.getInstance() != null)
			{
				BambiContainer.getInstance().getProps().setCSS(cbLegacy.isSelected() ? "/legacy" : "/webapp");
				BambiContainer.getInstance().getProps().serialize();
			}

		}
		else
		{
			super.actionPerformed(arg0);
		}
	}

	/**
	 * 
	 * @see org.cip4.jdfutility.server.ui.JettyFrame#started()
	 */
	@Override
	protected void started()
	{
		super.started();
		baseDirButton.setEnabled(false);
	}

	/**
	 * 
	 * @see org.cip4.jdfutility.server.ui.JettyFrame#stopped()
	 */
	@Override
	protected void stopped()
	{
		super.stopped();
		baseDirButton.setEnabled(true);
	}
}
