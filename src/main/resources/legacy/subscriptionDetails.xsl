<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:strip-space elements="*" />
	<xsl:output method="html" cdata-section-elements="jdf:JMF jdf:Query" />
	<xsl:template match="/SubscriptionList">
		<xsl:variable name="context" select="@Context" />
		<html>
			<head>
				<xsl:call-template name="head-content" />
			</head>

			<body>
				<!-- navigation -->
				<nav class="navbar navbar-expand-sm fixed-top">
					<a class="navbar-brand" href="#">
						<img class="nav-logo" src="http://assets.cip4.org/logo/cip4-organization.png" />
						<span class="cip">CIP4</span> Organization
					</a>

					<!-- left -->
					<ul class="navbar-nav mr-auto"></ul>

					<!-- right -->
					<ul class="navbar-nav"></ul>
				</nav>


				<div class="container">

					<!-- breadcrumb -->
					<div class="row pt-2">
						<div class="col-12">
							<ul class="breadcrumb">
								<li>
									<a><xsl:attribute name="href"><xsl:value-of select="$context" />/overview</xsl:attribute>Device List</a>
								</li>
								<li>
									<a><xsl:attribute name="href"><xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" /></xsl:attribute>Device: <xsl:value-of select="@DeviceID" /></a>
								</li>
								<li>
									<a><xsl:attribute name="href"><xsl:value-of select="@Context" />/showSubscriptions/<xsl:value-of select="@DeviceID" /></xsl:attribute>Subscriptions</a>
								</li>
								<li>
									Channel: <xsl:value-of select="MsgSubscription/@ChannelID" />
								</li>
							</ul>
						</div>
					</div>

					<!-- channel title -->
					<div class="row pt-2">
						<div class="col-12">
							<h1>Channel: <xsl:value-of select="MsgSubscription/@ChannelID" /></h1>
							<p>Channel details of the selected channel of device <xsl:value-of select="@DeviceID" />.</p>
						</div>
					</div>

					<!-- message subscription details -->
					<div class="row mt-4">
						<div class="col-12">
							<h2>Message Subscriptions</h2>
							<p>List of message subscriptions.</p>

							<xsl:call-template name="msg-subscriptions-table" />
						</div>
					</div>



					<xsl:if test="MsgSubscription/Sub">
						<div class="row mt-4">
							<div class="col-12">
								<h2>Subscription Details</h2>
								<xsl:for-each select="MsgSubscription/Sub">
									<xsl:apply-templates select="jdf:Query" />
								</xsl:for-each>
							</div>
						</div>
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

				</div>
			</body>
		</html>
	</xsl:template>







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

	<xsl:include href="xjdf.xsl" />
	<xsl:include href="modules/msg-subscriptions-table.module.xsl" />

</xsl:stylesheet>
