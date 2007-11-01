<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" errorPage="exception.jsp"%>
<%@ page import="org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus"%>
<%@ page import="org.cip4.bambi.core.AbstractDevice"%>
<%@ page import="org.cip4.bambi.core.queues.QueueFacade"%>
<%@ page import="org.cip4.bambi.core.queues.QueueFacade.BambiQueueEntry"%>
<%@ page import="java.util.Vector"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
	<% AbstractDevice dev = (AbstractDevice) request.getAttribute("device"); %>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
		<link rel="stylesheet" type="text/css" href="http://www.cip4.org/css/styles_pc.css"/>
		<link rel="icon" href="favicon.ico" type="image/x-icon" />
		<title>SimWorker - Device "<%=dev.getDeviceID()%>"</title>
	</head>
	
	<body>
		<h1>SimWorker - Device "<%=dev.getDeviceID() %>"</h1>
		
		<p align="center">
		// <a href="overview">back to root device</a> //
		<a href="overview?cmd=showDevice&id=<%=dev.getDeviceID()%>">reload this page</a> //
		</p>
		
		<!-- -------- device info section ------------------------------------------------------------- -->


		<h3>Device</h3>
		<div style="margin-left: 20px">
			<b>ID: </b> <%= dev.getDeviceID() %> <br/>
			<b>Class: </b> <%= dev.getDeviceType() %><br/>
			<b>URL: </b> http://<%=request.getLocalAddr() + ":" + request.getServerPort() + request.getContextPath()+"/devices/"+dev.getDeviceID()%><br/>
			<b>Status: </b> <%= dev.getDeviceStatus().getName() %>
		</div>
		
		
		<!-- --------- queue info section ------------------------------------------------------------- -->
		

		<h3>Queue</h3>
		<% 
			QueueFacade bqu = (QueueFacade)request.getAttribute("bqu");
			String qStat = bqu.getQueueStatusString();
		%>
		<div style="margin-left: 20px">
			<b>Queue Status: </b> <%=qStat %><br/>
			
			<table>
		        <thead>
		        	<tr>
		                <th align="left" width="250px">QueueEntry ID</th>
		                <th align="left" width="70px">Status</th>
		                <th align="left">Priority</th>
		                <th></th> <!-- suspend/resume/hold -->
		                <th></th> <!-- abort -->
		                <th></th> <!-- remove -->
		            </tr>
		        </thead>
		        <tbody>
				
				<%
					Vector<BambiQueueEntry> bqEntries = bqu.getBambiQueueEntryVector();
					for (int i=0; i<bqEntries.size();i++)
					{
						QueueFacade.BambiQueueEntry bqe = (QueueFacade.BambiQueueEntry)bqEntries.get(i);
				%>
					<tr>
						<td> <%=bqe.queueEntryID %> </td>                     
	                    <td> <%=bqe.queueEntryStatus.getName() %> </td>
	                    <td> <%=bqe.queuePriority %>  </td>
	                    <td>
	            <%
	            			if ( bqe.queueEntryStatus.equals(EnumQueueEntryStatus.Running) )
	            			{
	            %>
	            				<a href="overview?cmd=suspendQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">suspend</a>
	            <%
	            			} else if ( bqe.queueEntryStatus.equals(EnumQueueEntryStatus.Suspended) || bqe.queueEntryStatus.equals(EnumQueueEntryStatus.Held) )
	            			{
	            %>
	            	           	<a href="overview?cmd=resumeQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">resume</a>				
	            <%
	            	        }else if ( bqe.queueEntryStatus.equals(EnumQueueEntryStatus.Waiting) )
	            			{
	            %>
	            	           	<a href="overview?cmd=holdQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">hold</a>				
	            <%
	            	        }
	            %>
					    </td>
					    <td>
	            <%
	            			if ( ! bqe.queueEntryStatus.equals(EnumQueueEntryStatus.Completed) && ! bqe.queueEntryStatus.equals(EnumQueueEntryStatus.Aborted) && ! bqe.queueEntryStatus.equals(EnumQueueEntryStatus.Removed) )
	            			{
	            %>
	            				<a href="overview?cmd=abortQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">abort</a>
	            <%
	            			}
	            %>
					    </td>
					    <td>
	            <%
	            			if ( ! bqe.queueEntryStatus.equals(EnumQueueEntryStatus.Running) && ! bqe.queueEntryStatus.equals(EnumQueueEntryStatus.Suspended)  && ! bqe.queueEntryStatus.equals(EnumQueueEntryStatus.Removed) )
	            			{
	            %>
	            				<a href="overview?cmd=removeQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">remove</a>
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
