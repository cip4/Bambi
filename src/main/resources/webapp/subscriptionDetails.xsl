<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<!-- (C) 2001-2017 CIP4 -->
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml"
	xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:strip-space elements="*" />
	<xsl:output method="html" cdata-section-elements="jdf:JMF jdf:Query" />

	<xsl:template match="/SubscriptionList">
		<xsl:variable name="context" select="@Context" />
		<html>
			<head>
				<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
				<link rel="stylesheet" type="text/css" href="/webapp/css/styles_pc.css" />
				<link rel="icon" href="/webapp/favicon.ico" type="image/x-icon" />
				<title>
					Device
					<xsl:value-of select="@DeviceID" />
					- Subscription
				</title>
			</head>
			<body>
				<img src="/webapp/logo.gif" height="70" alt="logo" />
				<h1>
					Device
					<xsl:value-of select="@DeviceID" />
					- Subscription ChannelID=
					<xsl:value-of select="MsgSubscription/@ChannelID" />
				</h1>
				<xsl:if test="MsgSubscription">
					<h2>Subscriptions</h2>
					<table cellspacing="2" border="1">
						<tr>
							<th align="left"> Channel ID</th>
							<th align="left"> Device ID</th>
							<th align="left"> QueueEntry ID</th>
							<th align="left"> Signal Type</th>
							<th align="left"> Subscription URL</th>
							<th align="left"> Channel Mode</th>
							<th align="left"> Repeat Time</th>
							<th align="left"> Repeat Step</th>
							<th align="left"> Messages Queued</th>
							<th align="left"> Last time Queued</th>
							<th align="left"> Remove Subscription</th>
						</tr>
						<xsl:apply-templates select="MsgSubscription" />
					</table>
				</xsl:if>
				<table>
					<tr>
						<td>
							<a>
								<xsl:attribute name="href"><xsl:value-of
									select="@Context" />/showSubscriptions/<xsl:value-of
									select="@DeviceID" />
          </xsl:attribute>
								Back to Subscription List
							</a>
						</td>
						<td>
							-
						</td>
						<td>
							<a>
								<xsl:attribute name="href"><xsl:value-of
									select="@Context" />/showDevice/<xsl:value-of
									select="@DeviceID" />
          </xsl:attribute>
								Back to Device
							</a>
						</td>
					</tr>
				</table>
				<hr />

				<xsl:if test="MsgSubscription/Sub">
					<xsl:apply-templates select="MsgSubscription/Sub" />
					<hr />
				</xsl:if>
				<xsl:apply-templates select="MessageSender/Message" />
				<xsl:if test="MsgSubscription/Message">
					<hr />
					<h2>Previously Queued Messages</h2>
					<table>
						<tr>
							<th>Position</th>
							<th>Time Sent</th>
							<th>Message</th>
						</tr>
						<xsl:apply-templates select="MsgSubscription/Message" />
					</table>
				</xsl:if>


			</body>
		</html>
	</xsl:template>

	<!-- end of template SubscriptionList -->
	<xsl:template match="MsgSubscription">
		<tr>
			<td align="left">
				<a>
					<xsl:attribute name="href"><xsl:value-of
						select="../@Context" />/showSubscriptions/<xsl:value-of
						select="../@DeviceID" />?DetailID=<xsl:value-of
						select="@ChannelID" />
                  </xsl:attribute>
					<xsl:value-of select="@ChannelID" />
				</a>
			</td>
			<td align="left">
				<xsl:value-of select="@DeviceID" />
			</td>
			<td align="left">
				<xsl:value-of select="@QueueEntryID" />
			</td>
			<td align="left">
				<xsl:value-of select="@Type" />
			</td>
			<td align="left">
				<xsl:value-of select="@URL" />
			</td>
			<td align="left">
				<xsl:value-of select="*/jdf:Query/jdf:Subscription/@ChannelMode" />
			</td>
			<td align="left">
				<xsl:value-of select="@RepeatTime" />
			</td>
			<td align="left">
				<xsl:value-of select="@RepeatStep" />
			</td>
			<td align="left">
				<xsl:value-of select="@Sent" />
			</td>
			<td align="left">
				<xsl:value-of select="@LastTime" />
			</td>
			<td align="center">
				<form>
					<xsl:attribute name="action"><xsl:value-of
						select="../@Context" />/showSubscriptions/<xsl:value-of
						select="../@DeviceID" /></xsl:attribute>
					<input type="hidden" name="StopChannel" value="true" />
					<input type="hidden" name="ChannelID">
						<xsl:attribute name="value"><xsl:value-of
							select="@ChannelID" /></xsl:attribute>
					</input>
					<input type="submit" value="remove" />
				</form>
			</td>
		</tr>
	</xsl:template>
	<!-- end of template MsgSubscription -->

	<xsl:include href="xjdf.xsl" />

	<!-- end of template MessageSender -->
	<xsl:template match="Sub">
		<h2>Subscription Details</h2>
		<xsl:apply-templates select="jdf:Query" />
	</xsl:template>
	<!-- end of template MessageSender -->

	


	<xsl:template match="MessageSender/Message">
		<h2>
			Last Queued Message Details - Status =
			<xsl:value-of select="@Return" />
		</h2>
		<xsl:apply-templates select="jdf:JMF" />
	</xsl:template>

	<xsl:template match="MessageSender">
		<h2>Queued Message Details</h2>
		<xsl:apply-templates select="jdf:JMF" />
	</xsl:template>

	<xsl:template match="Message">
		<xsl:variable name="pos" select="position()" />
		<tr valign="top">
			<td>
				<a>
					<xsl:attribute name="name">m<xsl:value-of
						select="position()" /></xsl:attribute>
				</a>
				<a>
					<xsl:attribute name="href"><xsl:value-of
						select="/SubscriptionList/@Context" />/showSubscriptions/<xsl:value-of
						select="/SubscriptionList/@DeviceID" />?pos=<xsl:value-of
						select="$pos" />&amp;DetailID=<xsl:value-of
						select="/SubscriptionList/MsgSubscription/@ChannelID" />#m<xsl:value-of
						select="$pos" /></xsl:attribute>
					JMF #
					<xsl:value-of select="$pos" />
				</a>
			</td>
			<td>
				<xsl:choose>
					<xsl:when test="jdf:JMF/@TimeStamp">
						<xsl:value-of select="jdf:JMF/@TimeStamp" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="@TimeStamp" />
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td>
				<xsl:apply-templates select="jdf:JMF" />
			</td>
		</tr>
	</xsl:template>
	<!-- end of template RemovedChannel -->
	<xsl:include href="SubscriptionExtension.xsl" />
	<xsl:include href="topnavigation.xsl" />
</xsl:stylesheet>
