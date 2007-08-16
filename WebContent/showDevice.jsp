<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@ page import="org.cip4.bambi.Device"%>
<%@ page import="org.cip4.jdflib.jmf.JDFQueue"%>
<%@page import="org.cip4.jdflib.jmf.JDFQueueEntry"%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Bambi - DeviceInfo</title>
	</head>
	
	<body>
		<h3>General Info:</h3>
		<% 
			Device dev = (Device) request.getAttribute("device");
			String bambiUrl = "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath() +"/";
		%>
		<p>
			<b>Device ID: </b> <%=dev.getDeviceID()%> <br>
			<b>Device Type: </b> <%=dev.getDeviceType()%><br>
			<b>Device URL: </b> <%= bambiUrl + dev.getDeviceID()%>
			<!-- TODO: show device status -->
		</p>

		<h3>Queue:</h3>
		<% JDFQueue qu = (JDFQueue)request.getAttribute("qu"); %>
		<b>Queue Status: </b> ${qu.queueStatus.name} <br>
		<br>
		
		<table>
	        <thead>
	        	<tr>
	                <th>Job ID</th>
	                <th>Status</th>
	                <th>Priority</th>
	                <th>Submission Time</th>
	            </tr>
	        </thead>
	        <tbody>
			
			<%
				org.cip4.jdflib.core.VElement qev = qu.getQueueEntryVector();
				for (int i=0; i<qev.size();i++)
				{
					JDFQueueEntry qe = (JDFQueueEntry)qev.get(i);
			%>
				<tr>
					<td>${qe.jobID} (${qe.jobPartID})</td>                     
                    <td>${qe.queueEntryStatus.name}</td>
                    <td>${qe.priority}</td>
                    <td>${qe.submissionTime.dateTimeISO}</td>
                </tr>
			<%
				}
			%>
           
	        </tbody>
     	</table>


	</body>
</html>
