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
package org.cip4.bambi;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * TODO Please insert comment!
 * @author stefanmeissner
 * @date 29.08.2013
 */
public class ExecutorForm {

	private final static String RES_BAMBI_WAR = "/org/cip4/bambi/bambi.war";

	private final static String RES_VERSION = "/org/cip4/bambi/version.properties";

	private static String paramContext = "bambi";

	private static int paramPort = 8080;

	private JFrame frmCipBambiapp;

	private JTextArea textArea;

	private Server server;

	/**
	 * Entry point application.
	 * @param args
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ExecutorForm window = new ExecutorForm();
					window.frmCipBambiapp.setVisible(true);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ExecutorForm() {

		// init form
		initialize();

		// redirect output
		PrintStream printStream = new PrintStream(new JTextAreaOutputStream(textArea));
		System.setOut(printStream);
		System.setErr(printStream);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmCipBambiapp = new JFrame();
		frmCipBambiapp.setTitle("CIP4 BambiApp");
		frmCipBambiapp.setBounds(100, 100, 546, 436);
		frmCipBambiapp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JButton btnStart = new JButton("Start Bambi");
		btnStart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				startBambiApp();
			}
		});

		JLabel lblBambiapp = new JLabel("BambiApp");

		textArea = new JTextArea();

		JButton btnStop = new JButton("Stop Bambi");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopBambiApp();
			}
		});

		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openUrl();
			}
		});
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(textArea);

		GroupLayout groupLayout = new GroupLayout(frmCipBambiapp.getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
				groupLayout
						.createSequentialGroup()
						.addGroup(
								groupLayout
										.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE))
										.addGroup(
												groupLayout
														.createSequentialGroup()
														.addGap(147)
														.addGroup(
																groupLayout
																		.createParallelGroup(Alignment.LEADING)
																		.addComponent(lblBambiapp)
																		.addGroup(
																				groupLayout.createSequentialGroup().addComponent(btnStart).addPreferredGap(ComponentPlacement.RELATED)
																						.addComponent(btnStop).addPreferredGap(ComponentPlacement.RELATED).addComponent(btnOpen))).addGap(65)))
						.addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
				groupLayout.createSequentialGroup().addGap(49).addComponent(lblBambiapp).addGap(27)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(btnStart).addComponent(btnStop).addComponent(btnOpen)).addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE).addContainerGap()));
		frmCipBambiapp.getContentPane().setLayout(groupLayout);
	}

	/**
	 * Starts Bambi in a jetty web server environment.
	 * @throws IOException
	 */
	private void startBambiApp() {

		// text output console
		System.out.println("");
		System.out.println("----------------------------------------------------------------------------");
		System.out.println("");
		System.out.println("Start Bambi....");
		System.out.println("");

		server = new Server();
		SocketConnector connector = new SocketConnector();

		// Set some timeout options to make debugging easier.
		connector.setMaxIdleTime(1000 * 60 * 60);
		connector.setSoLingerTime(-1);
		connector.setPort(paramPort);
		server.setConnectors(new Connector[] { connector });

		// war path
		InputStream is = Executor.class.getResourceAsStream(RES_BAMBI_WAR);

		String warPath = FileUtils.getTempDirectoryPath() + "/bambi.tmp.war";
		File file = new File(warPath);
		if (file.exists())
			file.delete();
		OutputStream os;

		try {
			os = new FileOutputStream(file);
			IOUtils.copy(is, os);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		// login service
		HashLoginService loginService = new HashLoginService();
		loginService.setName("test");

		WebAppContext context = new WebAppContext();
		context.setContextPath("/" + paramContext);
		context.setWar(warPath);
		context.getSecurityHandler().setLoginService(loginService);
		server.setHandler(context);

		try {
			server.start();
			// System.in.read();
			// server.stop();
			// server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
	}

	/**
	 * Stop Bambi Appication.
	 */
	private void stopBambiApp() {
		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open bambi in browser.
	 */
	private void openUrl() {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI("http://localhost:8080/bambi"));
			} catch (Exception e) { /* TODO: error handling */
			}
		} else { /* TODO: error handling */
		}
	}
}
