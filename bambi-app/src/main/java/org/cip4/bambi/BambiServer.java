package org.cip4.bambi;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.*;

/**
 * Created by stefanmeissner on 07.10.14.
 */
public class BambiServer {

    private final static String RES_BAMBI_WAR = "/org/cip4/bambi/bambi.war";

    private Server server;

    /**
     * Default constructor.
     */
    public BambiServer() {

    }

    /**
     * Start Jetty Server.
     */
    public void start(int port, String context) {

        // text output console
        System.out.println("Start Bambi....");
        System.out.println("");

        server = new Server();
        SocketConnector connector = new SocketConnector();

        // Set some timeout options to make debugging easier.
        connector.setMaxIdleTime(1000 * 60 * 60);
        connector.setSoLingerTime(-1);
        connector.setPort(port);
        server.setConnectors(new Connector[] { connector });

        // war path
        InputStream is = ExecutorForm.class.getResourceAsStream(RES_BAMBI_WAR);

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

        WebAppContext ctx = new WebAppContext();
        ctx.setContextPath("/" + context);
        ctx.setWar(warPath);
        ctx.getSecurityHandler().setLoginService(loginService);
        server.setHandler(ctx);

        try {
            server.start();
            System.out.println("System started...");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
    }

    /**
     * Stop Jetty Server
     */
    public void stop() throws Exception {
        server.stop();
    }
}
