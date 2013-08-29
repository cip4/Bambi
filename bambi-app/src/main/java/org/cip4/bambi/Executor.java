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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Starts bambi.war in a jetty web server environment.
 * @author smeissner
 * @date 07.04.2011
 */
public class Executor {

	private final static String UPDATE_URL = "http://apps.jdf4you.org/update.php?appId=bambi-app";
	
	private final static String RES_BAMBI_WAR = "/org/cip4/bambi/bambi.war";
	
	private final static String RES_VERSION = "/org/cip4/bambi/version.properties";

	private final static Configuration VERSION_CONFIG = initVersionConfig();

	private static String paramContext = "bambi";

	private static int paramPort = 8080;

	/**
	 * Initialize version configuration.
	 * @return
	 */
	private static Configuration initVersionConfig() {
		Configuration result = null;
		
		InputStream is = Executor.class.getResourceAsStream(RES_VERSION);
		

		try {
			File fileVersion = File.createTempFile("bambi-app-verison", ".properties");
			fileVersion.deleteOnExit();
			
			FileOutputStream fos = new FileOutputStream(fileVersion);
			IOUtils.copy(is, fos);
			fos.close();
			is.close();
			
			result = new PropertiesConfiguration(fileVersion);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Entry point application.
	 * @param args
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public static void main(String[] args) throws Exception {
		// print splash
		printSplashScreen();

		// check for updates
		checkForUpdates(UPDATE_URL);

		// configure application
		configApp();

		// start bambi app
		startBambiApp();
	}

	/**
	 * Configure Bambi Application
	 */
	private static void configApp() {
		String input = "";
		String inputConfirm;
		Scanner scanner = new Scanner(System.in);

		System.out.println("    ---------------------------------------------");
		System.out.println("    Setup Bambi Runtime Configuration:");
		System.out.println("");

		do {
			inputConfirm = "OK";

			// port
			System.out.print("        Port [" + paramPort + "]: ");
			try {
				paramPort = Integer.parseInt(scanner.nextLine());
			} catch (Exception ex) {
			}

			// context
			System.out.print("        Context Path [" + paramContext + "]: ");

			input = scanner.nextLine();

			if (!input.equals("")) {
				paramContext = input;
			}

			// Confirmation
			System.out.println("");
			System.out.println("        -----------------------------------------");
			System.out.println("");
			System.out.println("        Configured Bambi 2 URL:");
			System.out.println("        http://localhost:" + paramPort + "/" + paramContext);
			System.out.println("");
			System.out.print("        Right? [OK]:");

			input = scanner.nextLine();
			if (!input.equals("")) {
				inputConfirm = input;
			}
		} while (!"OK".equalsIgnoreCase(inputConfirm));
	}

	/**
	 * Starts Bambi in a jetty web server environment.
	 * @throws IOException
	 */
	private static void startBambiApp() throws IOException {
		// text output console
		System.out.println("");
		System.out.println("----------------------------------------------------------------------------");
		System.out.println("");
		System.out.println("Start Bambi....");
		System.out.println("");

		Server server = new Server();
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
		OutputStream os = new FileOutputStream(file);
		IOUtils.copy(is, os);

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
			System.in.read();
			server.stop();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
	}

	/**
	 * Print splash screen to console.
	 * @throws ConfigurationException
	 */
	private static void printSplashScreen() throws ConfigurationException {

		String version = VERSION_CONFIG.getString("version");
		String buildnumber = VERSION_CONFIG.getString("buildnumber");
		String release = VERSION_CONFIG.getString("release");

		System.out.println("");
		System.out.println("    Powered by CIP4 (http://www.cip4.org):");
		System.out.println("");
		System.out.println("        ______                 _     _  ___              ");
		System.out.println("        | ___ \\               | |   (_)/ _ \\             ");
		System.out.println("        | |_/ / __ _ _ __ ___ | |__  _/ /_\\ \\_ __  _ __  ");
		System.out.println("        | ___ \\/ _` | '_ ` _ \\| '_ \\| |  _  | '_ \\| '_ \\ ");
		System.out.println("        | |_/ / (_| | | | | | | |_) | | | | | |_) | |_) |");
		System.out.println("        \\____/ \\__,_|_| |_| |_|_.__/|_\\_| |_/ .__/| .__/ ");
		System.out.println("                                            | |   | |    ");
		System.out.println("                                            |_|   |_|    ");
		System.out.println("    Version: " + version + "." + buildnumber + " (" + release + ")");
		System.out.println("");
		System.out.println("    Authors: Dr. Rainer Prosi (Heidelberger Druckmaschinen AG)");
		System.out.println("             Stefan Meissner (flyeralarm GmbH)");
		System.out.println("             Niels Boeger");
		System.out.println("");
	}

	/**
	 * Checks the jdf4you.org server for updates.
	 */
	private static void checkForUpdates(String updateUrl) {

		int latestBuildNumber = -1;
		int currentBuildNumber;

		try {
			currentBuildNumber = VERSION_CONFIG.getInt("buildnumber");
		} catch (Exception e) {
			// new update is available
			System.out.println("");
			System.out.println("    --> !!! UNKNOWN VERSION NUMBER !!! <--");
			System.out.println("");

			// leave update checker
			return;
		}

		// write line
		System.out.println("");
		System.out.print("    Check for Updates.... ");

		// get latest build number from server
		try {
			URL url = new URL(updateUrl);
			URLConnection con;

			Proxy proxy = getProxy();

			if (proxy == null) {
				con = url.openConnection();
			} else {
				con = url.openConnection(proxy);
			}
			con.setConnectTimeout(1500);
			con.setReadTimeout(1500);

			File file = File.createTempFile(UUID.randomUUID().toString(), "tmp");
			FileOutputStream os = new FileOutputStream(file);
			IOUtils.copy(con.getInputStream(), os);
			con.getInputStream().close();

			Configuration config = new PropertiesConfiguration(file);
			file.delete();
			latestBuildNumber = config.getInt("latestBuildNumber");

			System.out.println("(OK)");
		} catch (Exception ex) {
			System.out.println("(FAILED)");
			// ex.printStackTrace();
		}

		// check for updates
		if (latestBuildNumber > currentBuildNumber) {
			// new update is available
			System.out.println("");
			System.out.println("    --> !!! NEW VERSION IS AVAILABLE FOR DOWNLOAD NOW !!! <--");
			System.out.println("");
		}
	}

	/**
	 * If necessary detect Proxy Server.
	 * @return ProxyServer
	 * @throws Exception
	 */
	private static Proxy getProxy() throws Exception {
		Proxy result = null;

		System.setProperty("java.net.useSystemProxies", "true");

		List<Proxy> lst = ProxySelector.getDefault().select(new URI(UPDATE_URL));

		if (lst != null) {
			for (Iterator iter = lst.iterator(); iter.hasNext() && result == null;)
				result = (java.net.Proxy) iter.next();
		}

		return result;
	}
}
