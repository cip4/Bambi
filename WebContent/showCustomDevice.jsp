<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="org.cip4.bambi.CustomDevice"%>
<%@ page import="org.cip4.bambi.QueueFacade"%>
<%@ page import="org.cip4.bambi.QueueFacade.BambiQueueEntry"%>
<%@ page import="org.cip4.bambi.servlets.DeviceServlet"%>
<%@ page import="org.cip4.bambi.AbstractDeviceProcessor.JobPhase"%>
<%@ page import="org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus"%>
<%@ page import="org.cip4.jdflib.core.JDFElement.EnumNodeStatus"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.util.Iterator"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
		<title>Bambi - DeviceInfo</title>
	</head>
	
	<body>
		<% CustomDevice dev = (CustomDevice) request.getAttribute("device"); %>
		<p align="center">
			// <a href="BambiRootDevice">back to root device</a> //
			<a href="BambiRootDevice?cmd=showDevice&id=<%=dev.getDeviceID()%>">reload this page</a> //
		</p>
		
		<h3>General Info:</h3>
		<% String bambiUrl = "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath() +"/" + DeviceServlet.bambiRootDeviceID + "/"; %>

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

		<%
			Vector bqEntry = bqu.getBambiQueueEntryVector();
			if (bqEntry.size() == 0)
				return;
			
			QueueFacade.BambiQueueEntry bqe = (QueueFacade.BambiQueueEntry)bqEntry.get(0);
		%>
		<b>QueueEntry Details:</b> <br />
		<div style="margin-left: 20px">
			ID: <%=bqe.queueEntryID %> <br/>                     
        	Status: <%=bqe.queueStatus %> <br/>
        	Priority: <%=bqe.queuePriority %>  <br/>
		</div>
			
		<b>Queue Control: </b>
		    <%
            			if ( bqe.queueStatus.equals("Running") )
            			{
            %>
            				<a href="BambiRootDevice?cmd=suspendQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">suspend  </a>
            <%
            			} else if ( bqe.queueStatus.equals("Suspended") )
            			{
            %>
            	           	<a href="BambiRootDevice?cmd=resumeQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">resume  </a>
            <%
            	        }

            			if ( !bqe.queueStatus.equals("Completed") && !bqe.queueStatus.equals("Aborted") && !bqe.queueEntryID.equals("Removed") )
            			{
            %>
            				<a href="BambiRootDevice?cmd=abortQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">abort  </a>
            <%
            			}

            			if ( !bqe.queueStatus.equals("Running") && !bqe.queueStatus.equals("Suspended")  && !bqe.queueStatus.equals("Removed") )
            			{
            %>
            				<a href="BambiRootDevice?cmd=removeQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">remove  </a>
            <%
            			}
            %>
     	
     	<br/>
     	<h3>Job Phase</h3>
     	<div style="margin-left: 20px">
     		<b>Current Job Phase:</b>
     	<% 
     		JobPhase currentPhase = (JobPhase)request.getAttribute("currentPhase");
     		if (currentPhase == null)
     		{
 		%>
 				current job phase is null
 		<%
     		} else {
 		%>
 				Device Status: <%=currentPhase.deviceStatus %> <br/>
 				Device Status Details: <%=currentPhase.deviceStatusDetails %> <br/>
 				Node Status: <%=currentPhase.nodeStatus %> <br/>
 				Node Status Details: <%=currentPhase.nodeStatusDetails %> <br/>
 				Good to be produced: <%=currentPhase.Output_Good %> <br/>
 				Waste to be produced: <%=currentPhase.Output_Waste %> <br/>
 		<%
     		}
 		%>
 		</div>
 		
 		<br/>
 		<b>Next Job Phase:</b>
 		<form action="BambiRootDevice">
 			<input type="hidden" name="cmd" value="processNextPhase" />
 			<input type="hidden" name="id" value="<%=dev.getDeviceID() %>" />
 			<input type="hidden" name="qeid" value="<%=bqe.queueEntryID %>" />
 		
 			Device Status: <br/>
	 		<p style="margin-left: 20px">
	 			<%
	 				Iterator devIter = EnumDeviceStatus.iterator();
	 				while (devIter.hasNext())
	 				{
	 					String devStatus = ((EnumDeviceStatus)devIter.next()).getName();
	 			%>
	 					<input type="radio" name="DeviceStatus" value="<%=devStatus %>"/> <%=devStatus %> <br/>
	 			<%
	 				}
	 			%>
	 		
	 			Device Status Details:  <input name="DeviceStatusDetails" type="text" size="30" maxlength="30" value="Waiting"/><br/>
	 		</p>

			Node Status: <br/>
			<p style="margin-left: 20px">
	 			<%
	 				Iterator nodeIter = EnumNodeStatus.iterator();
	 				while (nodeIter.hasNext())
	 				{
	 					String nodeStatus = ((EnumNodeStatus)nodeIter.next()).getName();
	 			%>
	 					<input type="radio" name="NodeStatus" value="<%=nodeStatus %>"/> <%=nodeStatus %> <br/>
	 			<%
	 				}
	 			%>
	 			Node Status Details: <input name="NodeStatusDetails" type="text" size="30" maxlength="30" value="Waiting"/>
	 		</p>

	 		Good: <input name="Good" type="text" size="10" maxlength="10" value="0"/> <br/>
	 		Waste: <input name="Waste" type="text" size="10" maxlength="10" value="0"/> <br/>
			<input type="submit" value="process new phase"/>

			
 		</form>

	</body>
</html>
