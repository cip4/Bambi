<%@ page contentType="text/html;charset=windows-1252" isErrorPage="false"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
	<!-- This is not an JSP error page, just a simple means of displaying Bambi errors -->
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<link rel="stylesheet" type="text/css" href="./css/styles_pc.css"/>
		<link rel="icon" href="favicon.ico" type="image/x-icon" />
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