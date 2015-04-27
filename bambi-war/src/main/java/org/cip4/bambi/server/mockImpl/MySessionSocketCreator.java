package org.cip4.bambi.server.mockImpl;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

public class MySessionSocketCreator implements WebSocketCreator {
	private final static Logger log = Logger.getLogger(MySessionSocketCreator.class);
	
	@Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp)
    {
        HttpSession httpSession = req.getSession();
        System.out.println("createWebSocket, httpSession: " + httpSession); // this is 'null' always
        log.debug("createWebSocket, httpSession: " + httpSession); // this is 'null' always
        
        httpSession = req.getHttpServletRequest().getSession();
        System.out.println("createWebSocket, httpSession.getId: " + httpSession.getId());
        log.debug("createWebSocket, httpSession.getId: " + httpSession.getId());
        
        return new MyServiceWebSocket(httpSession);
    }
}
