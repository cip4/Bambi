<%@ page contentType="text/html;charset=windows-1252" isErrorPage="true" %>
<%@ page import="java.io.CharArrayWriter, java.io.PrintWriter, java.util.StringTokenizer" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<link rel="stylesheet" type="text/css" href="./css/styles_pc.css"/>
		<link rel="icon" href="favicon.ico" type="image/x-icon" />
		<title>Bambi - Exception</title>
	</head>
	<body>
		<h1>Bambi - Exception</h1>
		<h2>Oops! Bambi ran into an exception...</h2>
		<b>Query String: </b><%= request.getQueryString() %> <br>
		<b>Status Code: </b>${pageContext.errorData.statusCode} <br>
		<b>Exception: </b><%= exception.getClass() %> <br>
		<b>Message: </b><%= exception.getMessage() %> <br>
		<b>Stack Trace: </b> <br>
		<code style="margin-left: 40px">
			<% 
				CharArrayWriter charArrayWriter = new CharArrayWriter(); 
				PrintWriter printWriter = new PrintWriter(charArrayWriter, true); 
				exception.printStackTrace(printWriter);
				StringTokenizer st = new StringTokenizer(charArrayWriter.toString(),"");
				while(st.hasMoreTokens()){
			  		out.println(st.nextToken());
			  		out.println("\n");
				}
			%>
		</code>
	</body>
</html>