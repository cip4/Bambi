package org.cip4.bambi.server.mockImpl;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.cip4.bambi.core.BambiContainer;
import org.cip4.bambi.core.Observable;
import org.cip4.bambi.core.Observer;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.json.JSONObject;
import org.json.XML;

@WebSocket
public class MyServiceWebSocket implements Observer {
	private final static Logger log = Logger.getLogger(MyServiceWebSocket.class);
	
	private HttpSession httpSession;
    private Session session;
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    
//    private static DataGeneratorThread t;
    
    MyServiceWebSocket(HttpSession httpSession) {
    	this.httpSession = httpSession;
    	
    	System.out.println("StockServiceWebSocket, httpSession: " + httpSession);
    	System.out.println("StockServiceWebSocket, httpSession.getId: " + httpSession.getId());
    }

    // called when the socket connection with the browser is established
    @OnWebSocketConnect
    public void handleConnect(Session session) {
    	this.session = session;
    	System.out.println("handleConnect, session=" + session);
    	
//    	t = new DataGeneratorThread();
//    	t.setStockServiceWebSocket(this);
//    	t.start();
    	
    	BambiContainer.getInstance().addListener(this);
    }

    // called when the connection closed
    @OnWebSocketClose
    public void handleClose(int statusCode, String reason) {
    	log.debug("handleClose, statusCode=" + statusCode + ", reason=" + reason);
    	System.out.println("handleClose, statusCode=" + statusCode + ", reason=" + reason);
    	
    	BambiContainer.getInstance().removeListener(this);
    }

    // called when a message received from the browser
    @OnWebSocketMessage
    public void handleMessage(Session session, String message) {
    	System.out.println("handleMessage, httpSession.getId: " + httpSession.getId() + ", message: " + message);
    }

    // called in case of an error
    @OnWebSocketError
    public void handleError(Throwable error) {
    	error.printStackTrace();
    	
        log.error("Error", error);
        System.out.println("Error: " + error.getMessage());
    }
 
    // sends message to browser
    public void send(String message) {
        try {
            if (session.isOpen()) {
                session.getRemote().sendString(message);
            }
        } catch (IOException e) {
            log.error("Error", e);
        }
    }
 
    // closes the socket
    private void stop() {
    	System.out.println("stop");
    	
        try {
            session.disconnect();
        } catch (IOException e) {
            log.error("Error", e);
        }
    }

	@Override
	public void refreshData(final Observable observable, final String jsonObj) {
		System.out.println("---> UPDATE occurred in observable: " + observable);
		
		send(jsonObj);
	}

}
