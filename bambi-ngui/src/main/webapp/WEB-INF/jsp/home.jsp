<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
  <%-- 3rd party resources --%>
  <link type="text/css" rel="stylesheet" href="<s:url value="/css/themes/vader/jquery-ui.css" />" />

  <script type="text/javascript" src="<s:url value="/scripts/jquery/jquery-1.11.2.min.js" />"></script>
  <script type="text/javascript" src="<s:url value="/scripts/jquery/jquery-ui.min.js" />"></script>
  <script type="text/javascript" src="<s:url value="/scripts/jquery/jquery.tmpl.min.js" />"></script>
  <script type="text/javascript" src="<s:url value="/scripts/jquery/jquery.ui-contextmenu.min.js" />"></script>

  <%-- Bambi resources --%>
  <link type="text/css" rel="stylesheet" href="<s:url value="/css/main.css" />" />
  <script type="text/javascript" src="<s:url value="/scripts/main.js" />"></script>

  <title><s:text name="home.title"/></title>
</head>

  <body>
    <h2><s:text name="home.title"/></h2>
    <a href="settings/view" target="_blank" style="float: right;">Settings</a>
    <br/><br/>

    <script type="text/html" id="jobTemplate">
        <div class="view-level-1 hide jobid-\${param.jobId}">
            <div class="icon-level"></div>
            <div style="display:inline-block;" class="">Job ID: \${param.jobId}</div>
            <div class="job-status-bar \${param.status}" deviceid="\${param.deviceId}" jobid="\${param.jobId}"></div>
            <div>* Submission: \${param.submission}</div>
            <div class="view level-basic">
              <div>* Priority: xxx</div>
              <div>*
                <span>Start:</span>
                <span class="job-started">\${param.start}</span>
              </div>
              <div>*
                <span>End:</span>
                <span class="job-ended">\${param.end}</span>
              </div>
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
        <tr id="<s:property value="deviceId"/>" class="device-<s:property value="deviceId"/>">
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
<div id="<s:property value="deviceId"/>" class="queue-status-bar <s:property value="queueStatus"/>"><s:property value="queueStatus"/>
</div>
<div class="queue-stat">
  <span title="Waiting/Running/Completed/All">Queue stat:</span>
  <span class="queue-stat-value"><s:property value="queueWaiting"/>/<s:property value="queueRunning"/>/<s:property value="queueCompleted"/>/<s:property value="queueAll"/></span>
</div>

          </td>
          <td class="main-panel">
            <div class="queue-entries hide">
            <s:iterator status="jobsStatus" value="jobsQueue" var="jobsIterator">
              <div class="view-level-1 jobid-<s:property value="jobId"/>">
                <div class="icon-level"></div>
                <div class="" style="display:inline-block;">Job ID: <s:property value="jobId"/></div>
                <div class="job-status-bar <s:property value="status"/>" deviceid="<s:property value="deviceId"/>" jobid="<s:property value="jobId"/>"></div>
                <div>* Submission: <s:property value="submitted"/></div>
                <div class="view level-basic">
                  <div>* Priority: <s:property value="priority"/></div>
                  <div>*
                    <span>Start:</span>
                    <span class="job-started"><s:property value="started"/></span>
                  </div>
                  <div>*
                    <span>End:</span>
                    <span class="job-ended"><s:property value="ended"/></span>
                  </div>
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
