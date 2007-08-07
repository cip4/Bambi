<html>
<!-- <LINK REL="stylesheet" HREF="http://www.cip4.org/css/styles_pc.css" TYPE="text/css"/> -->

<head><title>Bambi</title></head>
<body>
<h1>Bambi - A lightweight JDF Device</h1>

use <strong>http://<%=request.getServerName() + ":" + request.getServerPort() + request.getContextPath()%>/jmf </strong> to connect your MIS/Alces
<c:out value="${request.getServerName()}"/>
<p>Build @build.number@, @build.timestamp@</p>
</body>
</html>
