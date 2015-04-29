<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
  <link type="text/css" rel="stylesheet" href="<s:url value="/css/main.css" />" />
  <script type="text/javascript" src="<s:url value="/scripts/jquery-1.11.2.min.js" />"></script>
  <script type="text/javascript" src="<s:url value="/scripts/main.js" />"></script>
  <title><s:text name="home.title"/></title>
</head>

  <body>
    <h2><s:text name="home.title"/></h2>

    <div>
    <s:iterator status="stat" value="devices">
      <div id="<s:property value="deviceId"/>" style="display: table-row;" class="device">
        <div id="deviceId" style="display: table-cell;" class="deviceId"><s:property value="deviceId"/></div>
        <div id="queueAll" style="display: table-cell;" class="queueAll"><s:property value="queueAll"/></div>
      </div>
    </s:iterator>
    </div>

  </body>
</html>
