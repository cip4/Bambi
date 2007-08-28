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
		<% Device dev = (Device) request.getAttribute("device"); %>
		// <a href="BambiRootDevice<%=dev.getDeviceID()%>">back to root device</a> //
		<a href="BambiRootDevice?cmd=showDevice&id=<%=dev.getDeviceID()%>">reload this page</a> //
		
		<h3>General Info:</h3>
		<% String bambiUrl = "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath() +"/"; %>

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
		<b>Queue Status: </b> <%=qStat %><br/>
		
		<table>
	        <thead>
	        	<tr>
	                <th align="left" width="250px">QueueEntry ID</th>
	                <th align="left" width="70px">Status</th>
	                <th align="left">Priority</th>
	                <th></th> <!-- suspend/resume -->
	                <th></th> <!-- abort -->
	                <th></th> <!-- remove -->
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
					<td> <%=bqe.queueEntryID %> </td>                     
                    <td> <%=bqe.queueStatus %> </td>
                    <td> <%=bqe.queuePriority %>  </td>
                    <td>
            <%
            			if ( bqe.queueStatus.equals("Running") )
            			{
            %>
            				<a href="BambiRootDevice?cmd=suspendQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">suspend</a>
            <%
            			} else if ( bqe.queueEntryID.equals("Suspended") )
            			{
            %>
            	           	<a href="BambiRootDevice?cmd=resumeQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">resume</a>				
            <%
            	        }
            %>
				    </td>
				    <td>
            <%
            			if ( !bqe.queueStatus.equals("Completed") && !bqe.queueStatus.equals("Aborted") && !bqe.queueEntryID.equals("Removed") )
            			{
            %>
            				<a href="BambiRootDevice?cmd=abortQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">abort</a>
            <%
            			}
            %>
				    </td>
				    <td>
            <%
            			if ( !bqe.queueStatus.equals("Running") && !bqe.queueStatus.equals("Suspended")  && !bqe.queueStatus.equals("Removed") )
            			{
            %>
            				<a href="BambiRootDevice?cmd=removeQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">remove</a>
            <%
            			}
            %>
				    </td>
                </tr>
			<%
				}
			%>
           
	        </tbody>
     	</table>
	</body>
</html>
