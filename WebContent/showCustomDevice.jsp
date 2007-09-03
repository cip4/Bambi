<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" errorPage="exception.jsp"%>
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
	<% CustomDevice dev = (CustomDevice) request.getAttribute("device"); %>
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
		<div style="margin-left: 20px">
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
				ID: <%= bqe.queueEntryID %> <br/>                     
	        	Status: <%= bqe.queueStatus %> <br/>
	        	Priority: <%= bqe.queuePriority %>  <br/>
			</div>
				
			<b>Queue Control: </b>
			    <%
	            			if ( bqe.queueStatus.equals("Running") )
	            			{
	            %>
	            				<a href="BambiRootDevice?cmd=suspendQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">suspend</a> &nbsp; 
	            <%
	            			} else if ( bqe.queueStatus.equals("Suspended") )
	            			{
	            %>
	            	           	<a href="BambiRootDevice?cmd=resumeQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">resume</a> &nbsp;
	            <%
	            	        }
	
	            			if ( !bqe.queueStatus.equals("Completed") && !bqe.queueStatus.equals("Aborted") && !bqe.queueEntryID.equals("Removed") )
	            			{
	            %>
	            				<a href="BambiRootDevice?cmd=abortQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">abort</a> &nbsp;
	            <%
	            			}
	
	            			if ( !bqe.queueStatus.equals("Running") && !bqe.queueStatus.equals("Suspended")  && !bqe.queueStatus.equals("Removed") )
	            			{
	            %>
	            				<a href="BambiRootDevice?cmd=removeQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">remove</a>  &nbsp;
	            <%
	            			}
	            			
	          				if ( bqe.queueStatus.equals("Running") ) {
	     		%>
						     	<form action="BambiRootDevice">
						 			<input type="hidden" name="cmd" value="finalizeCurrentQE" />
						 			<input type="hidden" name="id" value="<%=dev.getDeviceID() %>" />
						 			<input type="hidden" name="qeid" value="<%=bqe.queueEntryID %>" />
									<input type="submit" value="finish processing current QueueEntry"/>
								</form>
				<%				
	          				}
				%>
		</div>
		
		
		<%
			if  (bqe.queueStatus.equals("Completed")) {
				return;
			}
		%>
		
		
<!-- --------- jop phase info section --------------------------------------------------------- -->
		
		
     	<br/>
     	<h3>Job Phase</h3>
		<form action="BambiRootDevice" style="margin-left: 20px">
 			<input type="hidden" name="cmd" value="processNextPhase" />
 			<input type="hidden" name="id" value="<%=dev.getDeviceID() %>" />
 			<input type="hidden" name="qeid" value="<%=bqe.queueEntryID %>" />
     	<% 
     		JobPhase currentPhase = (JobPhase)request.getAttribute("currentPhase");
     		if (currentPhase == null)
     		{
 		%>
 				current job phase is null
 		<%
     		} else {
 		%>
     	
	     	<table>
	     		<tr>
	     			<th></th> <!-- description -->
	     			<th align="left">Current Job Phase</th>
	     			<th align="left">Next Job Phase</th>
	     		</tr>
	     		<tr>
	     			<td><b>Device Status</b></td>
	     			<td><%=currentPhase.deviceStatus.getName() %></td>
	     			<td>
		     			<select name="DeviceStatus" size="1">
			 			<%
			 				Iterator devIter = EnumDeviceStatus.iterator();
			 				while (devIter.hasNext())
			 				{
			 					String devStatus = ((EnumDeviceStatus)devIter.next()).getName();
			 					if (devStatus.equals(currentPhase.deviceStatus.getName())) {
			 			%>
			 					<option selected="selected"><%=devStatus %></option>
			 			<%
			 					} else {
			 			%>
			 					<option><%=devStatus %></option>
			 			<%
			 					}
			 				}
			 			%>
			 			</select>
	     			</td>
	     		</tr>
	     		<tr>
	     			<td><b>Device Status Details</b></td>
	     			<td><%=currentPhase.deviceStatusDetails %></td>
	     			<td>
	     				<input name="DeviceStatusDetails" type="text" size="30" maxlength="30" value="<%=currentPhase.deviceStatusDetails %>"/>
	     			</td>
	     		</tr>
	     		<tr>
	     			<td><b>Node Status</b></td>
	     			<td><%=currentPhase.nodeStatus.getName() %></td>
	     			<td>
	     				<select name="NodeStatus" size="1">
			 			<%
			 				Iterator nodeIter = EnumNodeStatus.iterator();
			 				while (nodeIter.hasNext())
			 				{
			 					String nodeStatus = ((EnumNodeStatus)nodeIter.next()).getName();
			 					if (nodeStatus.equals(currentPhase.nodeStatus.getName())) {
			 			%>
			 					<option selected="selected"><%=nodeStatus %></option>
			 			<%
			 					} else {
			 			%>
			 					<option><%=nodeStatus %></option>
			 			<%
			 					}
			 				}
			 			%>
		 				</select>
	     			</td>
	     		</tr>
	     		<tr>
	     			<td><b>Node Status Details</b></td>
	     			<td><%=currentPhase.nodeStatusDetails %></td>
	     			<td>
	     				<input name="NodeStatusDetails" type="text" size="30" maxlength="30" value="<%=currentPhase.nodeStatusDetails %>"/>
	     			</td>
	     		</tr>
	     		<tr>
	     			<td><b>Good to be produced</b></td>
	     			<td><%=currentPhase.Output_Good %></td>
	     			<td>
	     				<input name="Good" type="text" size="10" maxlength="10" value="<%=currentPhase.Output_Good %>"/> 
	     			</td>
	     		</tr>
	     		<tr>
	     			<td><b>Waste to be produced</b></td>
	     			<td><%=currentPhase.Output_Waste %></td>
	     			<td>
	     				<input name="Waste" type="text" size="10" maxlength="10" value="<%=currentPhase.Output_Waste %>"/> 
	     			</td>
	     		</tr>
	     	</table>
     	
     	<%
     		}
 		%>
 			<input type="submit" value="process next phase phase"/>
 		</form>
 		
	</body>
</html>
