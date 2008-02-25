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
		<title>Bambi - Manual Device :<xsl:value-of select="$deviceID"/></title>
	</head>
 	<body>
		<h1>Bambi - Manual Device :<xsl:value-of select="$deviceID"/></h1>		
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

<!--  modifiable phase -->
<xsl:template match="Phase">
<h2>Current Job Phase</h2> 


<form style="margin-left: 20px">
<xsl:attribute name="action">../processNextPhase/<xsl:value-of select="../@DeviceID"/></xsl:attribute> 

Device Status:   
    <xsl:apply-templates select="bambi:OptionList[@name='DeviceStatus']"/>
<br/>
Device StatusDetails: 
<input name="DeviceStatusDetails" type="text" size="30" maxlength="30">
<xsl:attribute name="value"><xsl:value-of select="@DeviceStatusDetails"/></xsl:attribute>
</input>
<br/>
Node Status:   
<xsl:apply-templates select="bambi:OptionList[@name='NodeStatus']"/>
<br/>
Node StatusDetails: 
<input name="NodeStatusDetails" type="text" size="30" maxlength="30">
<xsl:attribute name="value"><xsl:value-of select="@NodeStatusDetails"/></xsl:attribute>
</input>
<br/>
Seconds to go: <xsl:value-of select="@Duration"/>; new time to go:
<input name="Duration" type="text" size="30" maxlength="30">
<xsl:attribute name="value"></xsl:attribute>
</input>


<h3>Resource Amounts</h3>
<xsl:apply-templates select="ResourceAmount"/>

<input type="submit" value="update phase"/>
</form>
</xsl:template>

<xsl:include href="optionlist.xsl"/> 
 
<!-- resource amount setup -->
<xsl:template match="ResourceAmount">
<h4>
<xsl:value-of select="@ResourceName"/>
</h4>

<xsl:if test="@ResourceIndex='0'">
Waste Production: <input type="checkbox" name="Waste0" value="true">
<xsl:if test="@Waste0='true'">
<xsl:attribute name="checked">checked</xsl:attribute>
</xsl:if>
</input>

- Speed: 
<input name ="Speed0" type="text" size="10" maxlength="30">
<xsl:attribute name="value"><xsl:value-of select="@Speed0"/></xsl:attribute>
</input>
<br/>
</xsl:if>

<xsl:apply-templates/>
</xsl:template>



<!-- add more templates -->
<!-- the catchall -->
<xsl:template match="*">
<h3>Unhandled element: <xsl:value-of select="name()"/></h3>
    <xsl:apply-templates/>
</xsl:template>
</xsl:stylesheet>