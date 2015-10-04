<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
  <link type="text/css" rel="stylesheet" href="<s:url value="/css/main.css" />" />
  <script type="text/javascript" src="<s:url value="/scripts/jquery/jquery-1.11.2.min.js" />"></script>
  <script type="text/javascript" src="<s:url value="/scripts/jquery/jquery-ui.min.js" />"></script>
  <script type="text/javascript" src="<s:url value="/scripts/jquery/jquery.tmpl.min.js" />"></script>
  <script type="text/javascript" src="<s:url value="/scripts/settings.js" />"></script>

  <title><s:text name="page.title.settings"/></title>
</head>

  <body>
    <h2><s:text name="page.title.settings"/></h2>

    <form id="saveSettings" action="/abc">
    <div>
      <table>
        <tr>
          <%-- <td style="vertical-align: top;">Show date/time as:</td>
          <td style=""> --%>
            <%-- <input type="radio" name="dateTimeFormatter" value="US"/>US
            <br/>
            <input type="radio" name="dateTimeFormatter" value="EURO" checked="checked"/>EURO
            <br/>
            <input type="radio" name="dateTimeFormatter" value="ISO"/>ISO
            <br/> --%>
            
            <s:radio label="Show date/time as" name="dateTimeFormatter" list="formatters" value="currentFormatter" />
            
            <%-- <input type="radio" name="dateTimeFormatter" value="other"/>other:
            <input type="text" name="dateTimeFormatterPattern" value="DD-MM-YYYY, HH:MIN"/> --%>
          <%-- </td> --%>
        </tr>
        <%-- <tr>
          <td style=""></td>
          <td style="">
            <input type="radio" name="dateTimeFormatter" value="other"/>other:
            <input type="text" name="dateTimeFormatterPattern" value="DD-MM-YYYY, HH:MIN"/>
          </td>
        </tr> --%>
        <tr>
          <td>
            <input id="button-apply-settings" style="width: 100%;" type="button" value="Apply">
          </td>
          <td></td>
        </tr>
      </table>
    </div>
    </form>
    
  <br/>
  </body>
</html>
