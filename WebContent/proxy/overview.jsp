<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="org.cip4.bambi.core.AbstractDevice" %>
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
		<img src="logo.gif" height="70" alt="BambiPic"/>
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
				<big>Queue:</big><a href="showQueue/<%=dev.getDeviceID()%> ">Queue for <%=dev.getDeviceID()%></a>

		
		<br>
			
		<%
			}
		%>
		
		<br>

		<p>Build @build.number@, @build.timestamp@</p>
	</body>
</html>