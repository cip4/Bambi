<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
  <link type="text/css" rel="stylesheet" href="<s:url value="/css/main.css" />" />
  <script type="text/javascript" src="<s:url value="/scripts/jquery/jquery-1.11.2.min.js" />"></script>
  <script type="text/javascript" src="<s:url value="/scripts/jquery/jquery-ui.min.js" />"></script>
  <script type="text/javascript" src="<s:url value="/scripts/jquery/jquery.tmpl.min.js" />"></script>

  <script type="text/javascript" src="<s:url value="/scripts/main.js" />"></script>
  <title><s:text name="home.title"/></title>
</head>

  <body>
    <h2><s:text name="home.title"/></h2>

    <script type="text/html" id="jobTemplate">
        <div class="view-level-1 hide jobid-\${param.jobid}">
            <div class="icon-level"></div>
            <div style="display:inline-block;" class="">Job ID: \${param.jobid}</div>
            <div class="queue-status-bar running"></div>
            <div>* Submission: \${param.submission}</div>
            <div class="view level-basic">
              <div>* Priority: xxx</div>
              <div>* Start: 2015-MAR-08 12:34:56</div>
              <div>* End: 2015-MAR-08 12:34:56</div>
            </div>
            <div class="view level-full">
              <div>* Attribute-01: xxx</div>
              <div>* Attribute-02: xxx</div>
            </div>
            <div>&nbsp;</div>
        </div>
    </script>

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
        <s:iterator status="devicesStatus" value="devices" var="devicesIterator">
        <tr class="device-<s:property value="deviceId"/>">
          <td class="left-panel device-<s:property value="deviceId"/>">
            <div><b>Device: <s:property value="deviceId"/></b>
              <span class="button details" title="Show details"></span>
            </div>

<div class="device-status-bar <s:property value="deviceStatus"/>"></div>
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
<div class="queue-status-bar <s:property value="queueStatus"/>"></div>
<div class="queue-stat">
  <span title="Waiting/Running/Completed/All">Queue stat:</span>
  <span class="queue-stat-value"><s:property value="queueWaiting"/>/<s:property value="queueRunning"/>/<s:property value="queueCompleted"/>/<s:property value="queueAll"/></span>
</div>

          </td>
          <td class="main-panel">
            <div class="queue-entries hide">
            <s:iterator status="jobsStatus" value="jobsQueue" var="jobsIterator">
              <div class="view-level-1 jobid-<s:property value="queueEntryID"/>">
                <div class="icon-level"></div>
                <div class="" style="display:inline-block;">Job ID: <s:property value="queueEntryID"/></div>
                <div class="queue-status-bar running"></div>
                <div>* Submission: <s:property value="submissionTime.dateTimeISO"/></div>
                <div class="view level-basic">
                  <div>* Priority: xxx</div>
                  <div>* Start: 2015-MAR-08 12:34:56</div>
                  <div>* End: 2015-MAR-08 12:34:56</div>
                </div>
                <div class="view level-full">
                  <div>* Attribute-01: xxx</div>
                  <div>* Attribute-02: xxx</div>
                </div>
                <div>&nbsp;</div>
              </div>
            </s:iterator>
            </div>
          </td>
        </tr>
        </s:iterator>
      </tbody>
    </table>

  </body>
</html>
