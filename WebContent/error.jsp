<%@ page contentType="text/html;charset=windows-1252" isErrorPage="true"  
	import="java.io.CharArrayWriter, java.io.PrintWriter, java.util.StringTokenizer"%>
<html>
	<head>
		<title>Bambi - Error</title>
	</head>
	<body>
	<h2>Error Page</h2>
		Oops! Bambi ran into an exception...<br>
		<b>Class: </b><%= exception.getClass() %> <br>
		<b>Message: </b><%= exception.getMessage() %> <br>
		<b>Stack Trace: </b> <br>
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
	</body>
</html>