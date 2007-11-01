<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="org.cip4.bambi.core.AbstractDevice" %>
<%@ page import="org.cip4.bambi.core.queues.QueueFacade" %>
<%@ page import="org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus" %>

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
		
		use <strong>http://<%=request.getLocalAddr() + ":" + request.getServerPort() + request.getContextPath()%>/devices</strong> to connect your MIS/Alces <br>
		
		<br>
		
		<h3>Queue:</h3>
		<% QueueFacade qf=(QueueFacade)request.getAttribute("qf"); %>
		Waiting: <%= qf.count(EnumQueueEntryStatus.Waiting)%>, Running: <%= qf.count(EnumQueueEntryStatus.Running)%>, 
		Completed: <%= qf.count(EnumQueueEntryStatus.Completed)%>, Aborted: <%= qf.count(EnumQueueEntryStatus.Aborted)%>, 
		Total: <%= qf.countAll()%> <br>
		<iframe src="overview?cmd=showQueue" width="450" height="200">
			  <a href="overview?cmd=showQueue">show queue</a>
		</iframe>
		
		<br>
		<p>Build @build.number@, @build.timestamp@</p>
	</body>
</html>