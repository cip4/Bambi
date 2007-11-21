<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="org.cip4.bambi.core.AbstractDevice" %>
<%@ page import="org.cip4.bambi.core.queues.QueueFacade" %>
<%@ page import="org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus" %>
<%@ page import="org.cip4.bambi.proxy.ProxyDevice" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<link rel="stylesheet" type="text/css" href="./css/styles_pc.css"/>
		<link rel="icon" href="favicon.ico" type="image/x-icon" />
		<title>Bambi - Proxy</title>
	</head>
	<body>
		<h1>Bambi Proxy - Overview</h1>
		
		<h2>Known Proxies:</h2>
		<% 
			HashMap<String, ProxyDevice> devices = (HashMap<String, ProxyDevice>)request.getAttribute("devices");
			if (devices==null) {
				return;
			}
			SortedSet<String> keys = new TreeSet<String>();
			keys.addAll( devices.keySet() );
			java.util.Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next().toString();
				ProxyDevice dev =devices.get(key);
	  	%>
				<h3><%=dev.getDeviceID()%></h3>
				<big>Device type: </big><%=dev.getDeviceType()%> <br/>
				<big>URL: </big><%=dev.getDeviceURL()%> <br/>
				<big>Queue:</big>
				<% QueueFacade qf=dev.getQueueFacade(); %>
				Completed: <%= qf.count(EnumQueueEntryStatus.Completed)%>, Aborted: <%= qf.count(EnumQueueEntryStatus.Aborted)%>, 
				Total: <%= qf.countAll()%> <br>
				<iframe src="overview?cmd=showQueue&devID=<%=dev.getDeviceID()%>" width="450" height="200">
			 		Oops! You should see a frame here. Please follow this link to see the queue of this device:			
			 		<a href="overview?cmd=showQueue&devID=<%=dev.getDeviceID()%> ">Link</a>
				</iframe>
		
		<br>
			
		<%
			}
		%>
		
		<br>

		<p>Build @build.number@, @build.timestamp@</p>
	</body>
</html>