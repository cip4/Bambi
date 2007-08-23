<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@ page import="org.cip4.bambi.Device"%>
<%@ page import="org.cip4.bambi.QueueFacade"%>
<%@ page import="org.cip4.bambi.QueueFacade.BambiQueueEntry"%>
<%@ page import="java.util.Vector"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
		<title>Bambi - DeviceInfo</title>
	</head>
	
	<body>
		<h3>General Info:</h3>
		<% 
			Device dev = (Device) request.getAttribute("device");
			String bambiUrl = "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath() +"/";
		%>

		<p>
			<b>Device ID: </b> <%=dev.getDeviceID()%> <br/>
			<b>Device Type: </b> <%=dev.getDeviceType()%><br/>
			<b>Device URL: </b> <%= bambiUrl + dev.getDeviceID()%>
			<!-- TODO: show device status -->
		</p>

		<h3>Queue:</h3>
		<% 
			QueueFacade bqu = (QueueFacade)request.getAttribute("bqu");
			String qStat = bqu.getQueueStatus();
		%>
		<b>Queue Status: </b> <%=qStat %> 
		<%
			// show pause / resume link:
			if (qStat.equals("Running") )
			{
				//
			} else if (qStat.equals("Paused") )
			{
				//
			}
		%>
		
		
		<br/>
		
		<table>
	        <thead>
	        	<tr>
	                <th align="left" width="250px">QueueEntry ID</th>
	                <th align="left" width="60px">Status</th>
	                <th align="left">Priority</th>
	            </tr>
	        </thead>
	        <tbody>
			
			<%
				Vector bqEntries = bqu.getBambiQueueEntryVector();
				for (int i=0; i<bqEntries.size();i++)
				{
					QueueFacade.BambiQueueEntry bqe = (QueueFacade.BambiQueueEntry)bqEntries.get(i);
			%>
				<tr>
					<td> <%=bqe.QueueEntryID %> </td>                     
                    <td> <%=bqe.QueueStatus %> </td>
                    <td> <%=bqe.QueuePriority %>  </td>
                </tr>
			<%
				}
			%>
           
	        </tbody>
     	</table>


	</body>
</html>
