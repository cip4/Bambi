<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="org.cip4.bambi.core.AbstractDevice" %>
<%@ page import="org.cip4.bambi.core.IDevice" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<link rel="stylesheet" type="text/css" href="./css/styles_pc.css"/>
		<link rel="icon" href="favicon.ico" type="image/x-icon" />
		<title>SimWorker - Overview</title>
	</head>
	<body>
		<img src="logo.gif" height="70" alt="logo"/>
		<h1>SimWorker - Overview</h1>
		
		use <b>http://<%=request.getLocalAddr() + ":" + request.getServerPort() + request.getContextPath()%>/devices</b> to connect your MIS/Alces<br>
		
		<br>
		<h3>Known Devices:</h3>
		<table>
			<tr>
				<th align="left"> Device ID </th>
				<th align="left"> Device Type </th>
				<th align="left"> </th>
			</tr>
		
			<% 
				HashMap<String, IDevice> devices = (HashMap<String, IDevice>)request.getAttribute("devices");
				if (devices==null) {
					return;
				}
				SortedSet<String> keys = new TreeSet<String>();
				keys.addAll( devices.keySet() );
				java.util.Iterator<String> it = keys.iterator();
				while (it.hasNext()) {
					String key = it.next().toString();
					AbstractDevice dev = (AbstractDevice)devices.get(key);
		  	%>
				<tr>
					<td><%=dev.getDeviceID()%></td>
					<td><%=dev.getDeviceType()%></td>
					<td><a href="showDevice/<%=dev.getDeviceID()%>">show</a></td>
				</tr>
			<%
				}
			%>
		</table>
		
		<p>Build @build.number@, @build.timestamp@</p>
	</body>
</html>