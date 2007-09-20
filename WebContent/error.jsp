<%@ page contentType="text/html;charset=windows-1252" isErrorPage="false"%>

<html>
	<!-- This is not an JSP error page, just a simple means of displaying Bambi errors -->
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<link rel="stylesheet" type="text/css" href="./css/styles_pc.css"/>
		<title>Bambi - Error</title>
	</head>
	<body>
		<h1>Bambi - Error</h1>
		<h2>Oops! Bambi is unable to process your request...</h2>
		<p>
			<b>Origin: </b><%= request.getAttribute("errorOrigin") %> <br>
			<b>Query String: </b><%= request.getQueryString() %> <br>
			<b>Message: </b><%= request.getAttribute("errorMsg") %> <br>
			<b>Details: </b> <br>
			<code>
				<%= request.getAttribute("errorDetails") %>
			</code>
		</p>
	</body>
</html>