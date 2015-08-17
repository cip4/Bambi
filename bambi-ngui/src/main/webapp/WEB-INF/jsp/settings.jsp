<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
  <link type="text/css" rel="stylesheet" href="<s:url value="/css/main.css" />" />
  <script type="text/javascript" src="<s:url value="/scripts/jquery/jquery-1.11.2.min.js" />"></script>
  <script type="text/javascript" src="<s:url value="/scripts/jquery/jquery-ui.min.js" />"></script>
  <script type="text/javascript" src="<s:url value="/scripts/jquery/jquery.tmpl.min.js" />"></script>

  <title><s:text name="page.title.settings"/></title>
</head>

  <body>
    <h2><s:text name="page.title.settings"/></h2>
    <div>
      Show date/time as: 
      us
      euro
      iso
      other: pattern
      
      What...
    </div>
    <hr/>
    
    BUTTONS: APPLY(NOT SAVED), SAVE

  </body>
</html>
