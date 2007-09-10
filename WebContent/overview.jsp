<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="org.cip4.bambi.AbstractDevice" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Bambi</title>
	</head>
	<body>
		<h1>Bambi - A lightweight JDF Device</h1>
		
		use <strong>http://<%=request.getServerName() + ":" + request.getServerPort() + request.getContextPath()%>/BambiRootDevice </strong> to connect your MIS/Alces <br>
		
		<br>
		<h3>Known Devices:</h3>
		<table>
			<tr>
				<th align="left"> Device ID </th>
				<th align="left"> Device Type </th>
				<th align="left"> </th>
			</tr>
		
			<% 
				HashMap devices = (HashMap)request.getAttribute("devices");
				SortedSet keys = new TreeSet();
				keys.addAll( devices.keySet() );
				java.util.Iterator it = keys.iterator();
				while (it.hasNext()) {
					String key = it.next().toString();
					AbstractDevice dev = (AbstractDevice)devices.get(key);
		  	%>
				<tr>
					<td><%=dev.getDeviceID()%></td>
					<td><%=dev.getDeviceType()%></td>
					<td><a href="BambiRootDevice?cmd=showDevice&id=<%=dev.getDeviceID()%>">show</a></td>
				</tr>
			<%
				}
			%>
		</table>
		
		<h3>Queue:</h3>
		<iframe src="BambiRootDevice?cmd=showQueue" width="350" height="200">
			  <a href="BambiRootDevice?cmd=showQueue">show queue</a>
		</iframe>
		
		<br>
		<p>Build @build.number@, @build.timestamp@</p>
	</body>
</html>