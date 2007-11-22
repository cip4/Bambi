<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" errorPage="exception.jsp"%>
<%@ page import="org.cip4.bambi.workers.manual.ManualDevice"%>
<%@ page import="org.cip4.bambi.core.queues.QueueFacade"%>
<%@ page import="org.cip4.bambi.core.queues.QueueFacade.BambiQueueEntry"%>
<%@ page import="org.cip4.bambi.workers.core.AbstractBambiDeviceProcessor.JobPhase"%>
<%@ page import="org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus"%>
<%@ page import="org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus"%>
<%@ page import="org.cip4.jdflib.core.JDFElement.EnumNodeStatus"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.util.Iterator"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">


<html>
	<% ManualDevice dev = (ManualDevice) request.getAttribute("device"); %>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
		<link rel="stylesheet" type="text/css" href="http://www.cip4.org/css/styles_pc.css"/>
		<link rel="icon" href="favicon.ico" type="image/x-icon" />
		<title>Bambi - Device "<%=dev.getDeviceID()%>"</title>
	</head>
	
	<body>
		<h1>Bambi - Device "<%=dev.getDeviceID()%>"</h1>
		
		<p align="center">
			// <a href="overview">back to root device</a> //
			<a href="overview?cmd=showDevice&id=<%=dev.getDeviceID()%>">reload this page</a> //
		</p>

		
<!-- -------- device info section ------------------------------------------------------------- -->


		<h3>Device</h3>
		<div style="margin-left: 20px">
			<b>ID: </b> <%=dev.getDeviceID()%> <br/>
			<b>Class: </b> <%=dev.getDeviceType()%><br/>
			<b>URL: </b> <%=dev.getDeviceURL()%> <br/>
			<b>Status: </b> <%=dev.getDeviceStatus().getName()%>
		</div>


<!-- --------- queue info section ------------------------------------------------------------- -->


		<h3>Queue</h3>
		<% 
			QueueFacade bqu = (QueueFacade)request.getAttribute("bqu");
			String qStat = bqu.getQueueStatusString();
		%>		
		<div style="margin-left: 20px">
			<b>Queue Status: </b> <%=qStat%> <br/>
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
						QueueFacade.BambiQueueEntry bqe = bqEntries.get(i);
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
	            			} else if ( bqe.queueEntryStatus.equals(EnumQueueEntryStatus.Suspended) || bqe.queueEntryStatus.equals(EnumQueueEntryStatus.Held))
	            			{
	            %>
	            	           	<a href="overview?cmd=resumeQueueEntry&id=<%=dev.getDeviceID()%>&qeid=<%=bqe.queueEntryID%>&id=<%=dev.getDeviceID()%>&show=true">resume</a>				
	            <%
	            	        } else if ( bqe.queueEntryStatus.equals(EnumQueueEntryStatus.Waiting) )
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
	     	<%
				Vector<BambiQueueEntry> bqEntry = bqu.getBambiQueueEntryVector();
				if (bqEntry.size() == 0)
					return;
				
				QueueFacade.BambiQueueEntry currentBQE = bqEntry.get(0);
				if (currentBQE.queueEntryStatus!=EnumQueueEntryStatus.Running) {
					return;
				}
			%>
			<form action="overview">
				<input type="hidden" name="cmd"	value="finalizeCurrentQE" /> 
				<input type="hidden" name="id" value="<%=dev.getDeviceID() %>" /> 
				<input type="hidden" name="show" value="true" /> 
				<input type="hidden" name="qeid" value="<%=currentBQE.queueEntryID %>" /> 
				<input type="submit" value="finish processing current QueueEntry" />
			</form>
		</div>
		
<!-- --------- jop phase info section --------------------------------------------------------- -->
		
		
     	<br/>
     	<h3>Job Phase</h3>
		<form action="overview" style="margin-left: 20px">
 			<input type="hidden" name="cmd" value="processNextPhase" />
 			<input type="hidden" name="id" value="<%=dev.getDeviceID() %>" />
 			<input type="hidden" name="qeid" value="<%=currentBQE.queueEntryID %>" />
 			<input type="hidden" name="show" value="true" />
     	<%
     				JobPhase currentPhase = (JobPhase) request.getAttribute("currentPhase");
     		if (currentPhase == null) {
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
	     			<td><%=currentPhase.deviceStatus.getName()%></td>
	     			<td>
		     			<select name="DeviceStatus" size="1">
			 			<%
			 					Iterator<EnumDeviceStatus> devIter = EnumDeviceStatus.iterator();
			 					while (devIter.hasNext()) {
			 						String devStatus = devIter.next().getName();
			 						if (devStatus.equals(currentPhase.deviceStatus.getName())) {
			 			%>
			 					<option selected="selected"><%=devStatus%></option>
			 			<%
			 						} else {
			 			%>
			 					<option><%=devStatus%></option>
			 			<%
			 						}
			 					}
			 			%>
			 			</select>
	     			</td>
	     		</tr>
	     		<tr>
	     			<td><b>Device Status Details</b></td>
	     			<td><%=currentPhase.deviceStatusDetails%></td>
	     			<td>
	     				<input name="DeviceStatusDetails" type="text" size="30" maxlength="30" value="<%=currentPhase.deviceStatusDetails %>"/>
	     			</td>
	     		</tr>
	     		<tr>
	     			<td><b>Node Status</b></td>
	     			<td><%=currentPhase.nodeStatus.getName()%></td>
	     			<td>
	     				<select name="NodeStatus" size="1">
			 			<%
			 					Iterator<EnumNodeStatus> nodeIter = EnumNodeStatus.iterator();
			 					while (nodeIter.hasNext()) {
			 						String nodeStatus = nodeIter.next().getName();
			 						if (nodeStatus.equals(currentPhase.nodeStatus.getName())) {
			 			%>
			 					<option selected="selected"><%=nodeStatus%></option>
			 			<%
			 						} else {
			 			%>
			 					<option><%=nodeStatus%></option>
			 			<%
			 						}
			 					}
			 			%>
		 				</select>
	     			</td>
	     		</tr>
	     		<tr>
	     			<td><b>Node Status Details</b></td>
	     			<td><%=currentPhase.nodeStatusDetails%></td>
	     			<td>
	     				<input name="NodeStatusDetails" type="text" size="30" maxlength="30" value="<%=currentPhase.nodeStatusDetails %>"/>
	     			</td>
	     		</tr>
	     		<tr>
	     			<td><b>Good to be produced</b></td>
	     			<td><%=currentPhase.Output_Good%></td>
	     			<td>
	     				<input name="Good" type="text" size="10" maxlength="10" value="<%=currentPhase.Output_Good %>"/> 
	     			</td>
	     		</tr>
	     		<tr>
	     			<td><b>Waste to be produced</b></td>
	     			<td><%=currentPhase.Output_Waste%></td>
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
