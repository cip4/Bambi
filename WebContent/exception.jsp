<%@ page contentType="text/html;charset=windows-1252" isErrorPage="true" %>
<%@ page import="java.io.CharArrayWriter, java.io.PrintWriter, java.util.StringTokenizer" %>

<html>
	<head>
		<title>Bambi - Error</title>
	</head>
	<body>
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