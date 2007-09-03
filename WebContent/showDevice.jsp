<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" errorPage="exception.jsp"%>
<%@ page import="org.cip4.bambi.AbstractDevice"%>
<%@ page import="org.cip4.bambi.QueueFacade"%>
<%@ page import="org.cip4.bambi.QueueFacade.BambiQueueEntry"%>
<%@ page import="org.cip4.bambi.servlets.DeviceServlet"%>
<%@ page import="java.util.Vector"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
	<% AbstractDevice dev = (AbstractDevice) request.getAttribute("device"); %>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
		<title>Bambi - Device "<%=dev.getDeviceID()%>"</title>
	</head>
	
	<body>
		<p align="center">
		// <a href="BambiRootDevice">back to root device</a> //
		<a href="BambiRootDevice?cmd=showDevice&id=<%=dev.getDeviceID()%>">reload this page</a> //
		</p>
		
		<!-- -------- device info section ------------------------------------------------------------- -->


		<h3>Device</h3>
		<div style="margin-left: 20px">
			<% String bambiUrl = "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath() +"/" + DeviceServlet.bambiRootDeviceID + "/"; %>
			<b>ID: </b> <%= dev.getDeviceID() %> <br/>
			<b>Class: </b> <%= dev.getDeviceType() %><br/>
			<b>URL: </b> <%= bambiUrl + dev.getDeviceID() %> <br/>
			<b>Status: </b> <%= dev.getDeviceStatus().getName() %>
		</div>
		
		
		<!-- --------- queue info section ------------------------------------------------------------- -->
		

		<h3>Queue</h3>
		<% 
			QueueFacade bqu = (QueueFacade)request.getAttribute("bqu");
			String qStat = bqu.getQueueStatus();
		%>
		<div style="margin-left: 20px">
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
	            			} else if ( bqe.queueStatus.equals("Suspended") )
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
     	</div>
	</body>
</html>
