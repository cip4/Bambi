<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<!--  Copyright 2009-2014 CIP4 -->
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml"
	xmlns:bambi="www.cip4.org/Bambi" xmlns:xjdf="http://www.CIP4.org/JDFSchema_2_0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!--  (C) 2001 - 2014 CIP4  -->
	<xsl:strip-space elements="*" />
	<xsl:output method="html" />
	<xsl:template match="/SubscriptionList">
		<xsl:variable name="context" select="@Context" />
		<xsl:variable name="deviceID" select="@DeviceID" />
		<html>
			<head>
				<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
				<link rel="stylesheet" type="text/css" href="../css/styles_pc.css" />
				<link rel="icon" href="favicon.ico" type="image/x-icon" />
				<title>
					<xsl:value-of select="@DeviceID" />
					- Subscriptions
				</title>
			</head>
			<body>
				<img src="../logo.gif" height="70" alt="logo" />
				<h1>
					<xsl:value-of select="@DeviceID" />
					- Subscriptions
				</h1>
				<xsl:call-template name="links" />
				<xsl:if test="MsgSubscription">
					<h2>
						Subscriptions:
						<xsl:value-of select="count(MsgSubscription)" />
					</h2>
					<table cellspacing="2" border="1">
						<tr>
							<th align="left"> # </th>
							<th align="left"> Channel ID</th>
							<th align="left"> Device ID</th>
							<th align="left"> QueueEntry ID</th>
							<th align="left"> Signal Type</th>
							<th align="left"> Subscription URL</th>
							<th align="left"> Repeat Time</th>
							<th align="left"> Repeat Step</th>
							<th align="left"> Messages Queued</th>
							<th align="left"> Last time Queued</th>
							<th align="left"> Remove Subscription</th>
						</tr>
						<xsl:apply-templates select="MsgSubscription" />
					</table>

					<hr />
				</xsl:if>
				<h2>Message Sender Channels</h2>
				<table cellspacing="2" border="1">
					<tr>
						<th align="left" title="Base URL of the Sender Channel"> Base URL</th>
						<th align="left" title="Status of the Sender Channel"> Status</th>
						<th align="left" title="Number of unsent messages pending"> JMF Pending</th>
						<th align="left" title="Number of successfully sent messages"> JMF Sent</th>
						<th align="left"
							title="Number of message send attempts, including unsuccessfull "> JMF Queued</th>
						<th align="left" title="Number of heartbeat messages removed "> JMF Removed</th>
						<th align="left" title="Number of fire &amp; forget messages not sent "> Fire &amp; Forget Removed</th>
						<th align="left" title="Number of error messages not sent "> JMF Errors Removed</th>
						<th align="left" title="Time of last message sending"> Last time Sent</th>
						<th align="left" title="Time the last message was queued for sending"> Last time Queued</th>
						<th align="left" title="Time the message sender started up"> Active since</th>
						<th align="left"> Show Sent Messages</th>
						<th align="left" title="pause or resume sending of outqiong messages"> pause / resume</th>
						<th align="left"
							title="remove this sender - does NOT flush pending messages"> Remove Sender</th>
						<th align="left" title="Flush all queued but unsent messages irrevocibly"> Flush unsent Messages</th>
					</tr>
					<xsl:apply-templates select="MessageSender" />
				</table>
				<hr />
				<ul>
					<xsl:apply-templates select="RemovedChannel" />
				</ul>

				<xsl:if test="MessageSender/Message">
					<hr />
					<xsl:call-template name="cputimer" />
					<hr />

					<h2>Queued Messages</h2>
					<table cellspacing="2" border="1">
						<tr>
							<th align="left"> JMF ID</th>
							<th align="left"> Sent Time</th>
							<th align="left"> Processing Status</th>
							<th align="left"> Full URL</th>
						</tr>
						<xsl:apply-templates select="MessageSender/Message" />
					</table>

					<hr />
				</xsl:if>

				<hr />
				<xsl:apply-templates select="ProxySubscriptions" />
				<xsl:apply-templates select="PrivateSubscriptions" />
				<xsl:call-template name="links" />
			</body>
		</html>
	</xsl:template>

	<!-- end of template SubscriptionList -->
	<xsl:template match="MsgSubscription">
		<tr>
			<xsl:choose>
				<xsl:when test="@LastTime=' - '">
					<xsl:choose>
						<xsl:when test="@Sent='0'">
							<xsl:attribute name="bgcolor">#ffaaaa</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="bgcolor">#ffffaa</xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="bgcolor">#aaffaa</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<td align="left">
				<xsl:value-of select="position()" />
			</td>
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

	<xsl:template match="MessageSender">
		<tr>
			<xsl:choose>
				<!-- something is waiting -->
				<xsl:when test="@Size!='0'">
					<xsl:choose>
						<xsl:when test="@pause='true'">
							<xsl:attribute name="bgcolor">#ffccaa</xsl:attribute>
						</xsl:when>
						<xsl:when test="@Active='true'">
							<xsl:attribute name="bgcolor">#ffffaa</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="bgcolor">#ffaaaa</xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="@pause='true'">
					<xsl:attribute name="bgcolor">#ffccaa</xsl:attribute>
				</xsl:when>
				<xsl:when test="@Active='true'">
					<xsl:choose>
						<xsl:when test="@Problems='true'">
							<xsl:attribute name="bgcolor">#ffaaaa</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="bgcolor">#aaffaa</xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="bgcolor">#aaaaff</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<td align="left">
				<xsl:value-of select="@URL" />
			</td>
			<td align="left">
				<xsl:choose>
					<xsl:when test="@Active='false'">
						down
					</xsl:when>
					<xsl:when test="@pause='true'">
						paused
					</xsl:when>
					<xsl:when test="@Size!='0'">
						<xsl:choose>
							<xsl:when test="@idle!='0'">
								dispatch errors
							</xsl:when>
							<xsl:otherwise>
								back log
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						active
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td align="left">
				<xsl:value-of select="@Size" />
			</td>
			<td align="left">
				<xsl:value-of select="@NumSent" />
			</td>
			<td align="left">
				<xsl:value-of select="@NumTry" />
			</td>
			<td align="left">
				<xsl:value-of select="@NumRemoveJMF" />
			</td>
			<td align="left">
				<xsl:value-of select="@NumRemoveFireForget" />
			</td>
			<td align="left">
				<xsl:value-of select="@NumRemoveError" />
			</td>
			<td align="left">
				<xsl:value-of select="@LastSent" />
			</td>
			<td align="left">
				<xsl:value-of select="@LastQueued" />
			</td>
			<td align="left">
				<xsl:value-of select="@CreationDate" />
			</td>
			<td align="center">
				<form>
					<xsl:attribute name="action"><xsl:value-of
						select="../@Context" />/showSubscriptions/<xsl:value-of
						select="../@DeviceID" /></xsl:attribute>
					<input type="hidden" name="ListSenders" value="true" />
					<input type="hidden" name="URL">
						<xsl:attribute name="value"><xsl:value-of
							select="@URL" /></xsl:attribute>
					</input>
					<input type="submit" value="List Senders" />
				</form>
			</td>
			<td align="center">
				<form>
					<xsl:attribute name="action"><xsl:value-of
						select="../@Context" />/showSubscriptions/<xsl:value-of
						select="../@DeviceID" /></xsl:attribute>
					<input type="hidden" name="URL">
						<xsl:attribute name="value"><xsl:value-of
							select="@URL" /></xsl:attribute>
					</input>
					<xsl:choose>
						<xsl:when test="@pause='true'">
							<input type="hidden" name="pause" value="false" />
							<input type="submit" value="resume" />
						</xsl:when>
						<xsl:otherwise>
							<input type="hidden" name="pause" value="true" />
							<input type="submit" value="pause" />
						</xsl:otherwise>
					</xsl:choose>
				</form>
			</td>
			<td align="center">
				<form>
					<xsl:attribute name="action"><xsl:value-of
						select="../@Context" />/showSubscriptions/<xsl:value-of
						select="../@DeviceID" /></xsl:attribute>
					<input type="hidden" name="StopSender" value="true" />
					<input type="hidden" name="URL">
						<xsl:attribute name="value"><xsl:value-of
							select="@URL" /></xsl:attribute>
					</input>
					<input type="submit" value="remove" />
				</form>
			</td>
			<td align="center">
				<form>
					<xsl:attribute name="action"><xsl:value-of
						select="../@Context" />/showSubscriptions/<xsl:value-of
						select="../@DeviceID" /></xsl:attribute>
					<input type="hidden" name="FlushSender" value="true" />
					<input type="hidden" name="URL">
						<xsl:attribute name="value"><xsl:value-of
							select="@URL" /></xsl:attribute>
					</input>
					<input type="submit" value="flush" />
				</form>
			</td>
		</tr>
	</xsl:template>
	<!-- end of template MessageSender -->
	<xsl:template match="RemovedChannel">
		<li>
			Subscription
			<xsl:value-of select="@ChannelID" />
			to
			<xsl:value-of select="@URL" />
			has been removed.
		</li>
	</xsl:template>
	<!-- end of template RemovedChannel -->
	<xsl:template match="Message">
		<tr>
			<xsl:choose>
				<!-- something is waiting -->
				<xsl:when test="@Return='sent'">
					<xsl:attribute name="bgcolor">#aaffaa</xsl:attribute>
				</xsl:when>
				<xsl:when test="@Return='error'">
					<xsl:attribute name="bgcolor">#ffaaaa</xsl:attribute>
				</xsl:when>
				<xsl:when test="@Return='removed'">
					<xsl:attribute name="bgcolor">#ffaaaa</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="bgcolor">#aaaaaa</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<td>
				<xsl:value-of select="position()" />
				<form>
					<xsl:attribute name="action"><xsl:value-of
						select="/SubscriptionList/@Context" />/showSubscriptions/<xsl:value-of
						select="/SubscriptionList/@DeviceID" /></xsl:attribute>
					<input type="hidden" name="ListSenders" value="true" />
					<input type="hidden" name="URL">
						<xsl:attribute name="value"><xsl:value-of
							select="@URL" /></xsl:attribute>
					</input>
					<input type="hidden" name="pos">
						<xsl:attribute name="value"><xsl:value-of
							select="position()" /></xsl:attribute>
					</input>
					<input type="submit" value="Show Details" />
				</form>
			</td>
			<td>
				<xsl:choose>
					<xsl:when test="xjdf:JMF/@TimeStamp">
						<xsl:value-of select="xjdf:JMF/@TimeStamp" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="@TimeStamp" />
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td>
				<xsl:value-of select="@Return" />
			</td>
			<td>
				<xsl:value-of select="@URL" />
			</td>
		</tr>
	</xsl:template>
	<xsl:template name="links">
		<table>
			<tr>
				<td>
					<a>
						<xsl:attribute name="href"><xsl:value-of
							select="@Context" />/showSubscriptions/<xsl:value-of
							select="@DeviceID" />
            </xsl:attribute>
						refresh list
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
				<td>
					-
				</td>
				<td>
					<a>
						<xsl:attribute name="href"><xsl:value-of
							select="@Context" />/showQueue/<xsl:value-of select="@DeviceID" />
            </xsl:attribute>
						Back to Queue
					</a>
				</td>
			</tr>
		</table>
		<hr />
	</xsl:template>
	<xsl:include href="SubscriptionExtension.xsl" />
	<xsl:include href="CPUTimer.xsl" />

</xsl:stylesheet>