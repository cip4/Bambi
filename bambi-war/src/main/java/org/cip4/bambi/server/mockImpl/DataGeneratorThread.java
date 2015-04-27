package org.cip4.bambi.server.mockImpl;


public class DataGeneratorThread extends Thread {
	private MyServiceWebSocket service;
	private static int i;
	
	public void setStockServiceWebSocket(MyServiceWebSocket s) {
		service = s;
	}
	
	public void run() {
		while (true) {
			try {
				Thread.sleep(20000);
				service.send("Это я - такой ответ! i: " + i);
				i++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
