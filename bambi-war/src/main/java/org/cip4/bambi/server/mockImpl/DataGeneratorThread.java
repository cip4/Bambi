package org.cip4.bambi.server.mockImpl;


public class DataGeneratorThread extends Thread {
	private MyServiceWebSocket service;
	private static int i;
	
	private static String sampleJson = "{\"employees\":"
		+ "["
			+ "{\"firstName\":\"John\", \"lastName\":\"Doe\"},"
			+ "{\"firstName\":\"Anna\", \"lastName\":\"Smith\"},"
			+ "{\"firstName\":\"Peter\", \"lastName\":\"Jones\"}"
		+ "]"
			+ "}";
	
	private static String templateDeviceQueueJson = 
			"{"
			+ "\"deviceId\":\"${deviceId}\","
			+ "\"queueAll\":\"${queueAll}\""
			+ "}";
	
	public void setStockServiceWebSocket(MyServiceWebSocket s) {
		service = s;
	}
	
	public void run() {
		while (true) {
			try {
				Thread.sleep(20000);
//				service.send(sampleJson);
				
				String s = templateDeviceQueueJson.replace("${deviceId}", "sim001").replace("${queueAll}", "" + i);
				service.send(s);
				
				i++;
			} catch (InterruptedException e) {
				e.printStackTrace(System.out);
			}
		}
	}
}
