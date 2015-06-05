<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
  <link type="text/css" rel="stylesheet" href="<s:url value="/css/main.css" />" />
  <script type="text/javascript" src="<s:url value="/scripts/jquery-1.11.2.min.js" />"></script>
  <script type="text/javascript" src="<s:url value="/scripts/jquery/jquery-ui.min.js" />"></script>

  <script type="text/javascript" src="<s:url value="/scripts/main.js" />"></script>
  <title><s:text name="home.title"/></title>
</head>

  <body>
    <h2><s:text name="home.title"/></h2>

    <%-- <div>
    <s:iterator status="stat" value="devices">
      <div id="<s:property value="deviceId"/>" style="display: table-row;" class="device">
        <div id="deviceId" style="display: table-cell;" class="deviceId"><s:property value="deviceId"/></div>
        <div id="queueAll" style="display: table-cell;" class="queueAll"><s:property value="queueAll"/></div>
      </div>
    </s:iterator>
    </div> --%>

    <table class="pane">
      <tbody>
        <s:iterator status="devicesStatus" value="devices" var="it">
        <tr>
          <td class="left-panel device-<s:property value="deviceId"/>">
            <div><b>Device: <s:property value="deviceId"/></b>
              <span class="button details" title="Show details"></span>
            </div>
            <%-- <s:include value="deviceProperties.jsp">
              <s:param name="param1" value="<s:property value="queueAll"/>" />
            </s:include> --%>

<div class="device-status-bar running"></div>
<div class="device-details hide">
  <div>* ID: xxx</div>
  <div>* URL: xxx</div>
  <div>* Class: xxx</div>
  <div>* Input Hot Folder: xxx</div>
  <div>* Output Hot Folder: xxx</div>
  <div>* Error Hot Folder: xxx</div>
</div>
<div>&nbsp;</div>

<div class="queue-header">Queue
  <span class="button entries" title="Show entries"></span>
</div>
<div class="queue-status-bar running"></div>
<div class="queue-stat">
  <span title="Waiting/Running/Completed/All">Queue stat:</span>
  <span class="queue-stat-value"><s:property value="queueWaiting"/>/<s:property value="queueRunning"/>/<s:property value="queueCompleted"/>/<s:property value="queueAll"/></span>
</div>

          </td>
          <td class="main-panel">
            <div class="queue-entries hide">
            </div>
          </td>
        </tr>
        </s:iterator>
      </tbody>
    </table>

  </body>
</html>
