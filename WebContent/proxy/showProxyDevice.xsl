<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet exclude-result-prefixes="tbl" version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:strip-space elements="*"/>
  <xsl:output method="html"/>

<xsl:template match="/XMLDevice">

<html>
   <xsl:variable name="deviceID" select="@DeviceID"/>
   <xsl:variable name="deviceType" select="@DeviceType"/>
   <xsl:variable name="deviceURL" select="@DeviceURL"/>
   <xsl:variable name="deviceStatus" select="@DeviceStatus"/>

	<head>
		<link rel="stylesheet" type="text/css" href="../css/styles_pc.css"/>
		<link rel="icon" href="../favicon.ico" type="image/x-icon" />
		<title>Bambi - Proxy Device :<xsl:value-of select="$deviceID"/></title>
		<h1>Bambi - Proxy Device :<xsl:value-of select="$deviceID"/></h1>
	</head>
 	<body>
		
		<p align="center">
 			<a><xsl:attribute name="href">
 			../showDevice/<xsl:value-of select="$deviceID"/>
 			</xsl:attribute>reload this page</a> 
		</p>

<!--  device info section   -->
		<hr/>
		<h3>Device</h3>
		<div style="margin-left: 20px">
			<b>ID: </b> <xsl:value-of select="$deviceID"/><br/>
			<b>Class: </b><xsl:value-of select="$deviceType"/><br/>
			<b>URL: </b><xsl:value-of select="$deviceURL"/><br/>
		</div>
		<hr/>
<!-- call queues and phases -->
		<h3>Queue</h3>
				<form style="margin-left: 20px">
 			<xsl:attribute name="action">../showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute> 
 			<input type="submit" value="show queue"/>
 			</form>

    <xsl:apply-templates/>

	</body>	
</html>
</xsl:template>

<xsl:include href="processor.xsl"/> 

<!-- add more templates -->
<!-- the catchall -->
<xsl:template match="*">
<h3>Unhandled element: <xsl:value-of select="name()"/></h3>
    <xsl:apply-templates/>
</xsl:template>
</xsl:stylesheet>