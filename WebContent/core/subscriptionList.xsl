<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
	<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
		<xsl:strip-space elements="*"/>
		<xsl:output method="html"/>

	<xsl:template match="/SubscriptionList">  
	<html>
		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
			<link rel="stylesheet" type="text/css" href="../css/styles_pc.css"/>
			<link rel="icon" href="favicon.ico" type="image/x-icon" />
			<title><xsl:value-of select="@DeviceID"/> - Subscriptions</title>
		</head>
		<body>
			<img src="../logo.gif" height="70" alt="logo"/>
			<h1><xsl:value-of select="@DeviceID"/> - Subscriptions</h1>
			
			<table cellspacing="2" border="1">
				<tr>
					<th align="left"> Channel ID </th>
					<th align="left"> QueueEntry ID  </th>
					<th align="left"> Signal Type </th>
					<th align="left"> Subscription URL </th>
					<th align="left"> Repeat Time </th>
					<th align="left"> Repeat Step </th>
					</tr>

			<xsl:apply-templates select="MsgSubscription"/>
			</table>
<hr/>
				<a><xsl:attribute name="href">../showDevice/<xsl:value-of select="@DeviceID"/></xsl:attribute>back to device <xsl:value-of select="@DeviceID"/></a>

		</body>
	</html>
	</xsl:template>  

	<xsl:template match="MsgSubscription">  
				<tr>
					<td align="left"><xsl:value-of select="@ChannelID"/></td>
					<td align="left"><xsl:value-of select="@QueueEntryID"/></td>
					<td align="left"><xsl:value-of select="@Type"/></td>
					<td align="left"><xsl:value-of select="@URL"/></td>
					<td align="left"><xsl:value-of select="@RepeatTime"/></td>
					<td align="left"><xsl:value-of select="@RepeatStep"/></td>
				</tr>

	</xsl:template>  


	</xsl:stylesheet>  
