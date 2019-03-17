<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<!-- (C) 2001-2019 CIP4 -->
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml"
	xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:strip-space elements="*" />
	<xsl:output method="html" />
	<xsl:template match="/DeviceList">
		<html>
			<xsl:variable name="context" select="@Context" />
			<head>
				<!-- <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/> -->
				<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
				<link rel="stylesheet" type="text/css">
					<xsl:attribute name="href"><xsl:value-of
						select="$context" />/legacy/css/styles_pc.css</xsl:attribute>
				</link>
				<link rel="icon" type="image/x-icon">
					<xsl:attribute name="href"><xsl:value-of
						select="$context" />/legacy/favicon.ico</xsl:attribute>
				</link>
				<title>
					<xsl:value-of select="@DeviceType" />
					- Overview
				</title>
			</head>
			<body>
				<xsl:call-template name="topnavigation" />
				<h1>
					<xsl:value-of select="@DeviceType" />
					- Overview
				</h1>
				<h3>Root Controller:</h3>
				<table cellspacing="2" border="1">
					<tr bgcolor="#bbbbbb">
						<th align="left"> Controller ID</th>
						<th align="left"> Controller Type</th>
						<th align="left"> Controller Status</th>
						<th align="left"> Controller URL</th>
						<th align="left"> Controller Base Directory</th>
						<th align="left"> Dump Enabled</th>
						<th align="left"> Controller Queue</th>
						<th align="left"> Queue Status</th>
						<th align="left"> # Running</th>
						<th align="left"> # Waiting/Suspended </th>
						<th align="left"> # Completed</th>
						<th align="left"> # All</th>
					</tr>
					<xsl:apply-templates select="XMLDevice[@Root='true']" />
				</table>
				<hr />
				<h3>Known Devices:</h3>
				<table cellspacing="2" border="1">
					<tr bgcolor="#bbbbbb">
						<th align="left"> Device ID</th>
						<th align="left"> Device Type</th>
						<th align="left"> Device Status</th>
						<th align="left"> Device URL</th>
						<th align="left"> Device Queue</th>
						<th align="left"> Queue Status</th>
						<th align="left"> # Running</th>
						<th align="left"> # Waiting/Suspended</th>
						<th align="left"> # Completed</th>
						<th align="left"> # All</th>
					</tr>
					<xsl:apply-templates select="XMLDevice[@Root='false']" />
				</table>
				
				<xsl:if test="Template">
				<hr />
				<h3>Known DeviceTemplates:</h3>
				<table cellspacing="2" border="1">
					<tr bgcolor="#bbbbbb">
						<th align="left"> Device Type</th>
						<th align="left"> Device ID</th>
					</tr>
					<xsl:apply-templates select="Template" />
				</table>
				</xsl:if>
				
				<hr />
				<xsl:call-template name="cputimer" />

				<hr />
				<font size="-1" color="#b0c4de">
					<table>
						<tr>
							<td>
								requests handled:
							</td>
							<td>
								<xsl:value-of select="@NumRequests" />
							</td>
						</tr>
						<tr>
							<td>
								Free Memory:
							</td>
							<td>
								<xsl:value-of select="@MemFree" />
							</td>
						</tr>
						<tr>
							<td>
								Currently used Memory:
							</td>
							<td>
								<xsl:value-of select="@MemCurrent" />
							</td>
						</tr>
						<tr>
							<td>
								Total Allocated Memory:
							</td>
							<td>
								<xsl:value-of select="@MemTotal" />
							</td>
						</tr>
						<xsl:if test="@ReleaseVersionString">
							<tr>
								<td>Bambi Product Version:</td>
								<td>
									<xsl:value-of select="@ReleaseVersionString" />
								</td>
							</tr>
						</xsl:if>
						<xsl:if test="@ReleaseTimestampString">
							<tr>
								<td>Bambi Release date/time:</td>
								<td><xsl:value-of select="@ReleaseTimestampString" /></td>
							</tr>
						</xsl:if>
						<tr>
							<td>JDFLib Build:</td>
							<td><xsl:value-of select="@JdfLibVersion" />
							</td>
						</tr>
					</table>
				</font>
			</body>
		</html>
	</xsl:template>

	<!-- root controller spec -->
	<xsl:template match="XMLDevice[@Root='true']">
		<xsl:variable name="context" select="../@Context" />
		<tr>
			<td align="left">
				<xsl:value-of select="@DeviceID" />
			</td>
			<td align="left">
				<xsl:value-of select="@DeviceType" />
			</td>
			<td align="left">
				<xsl:value-of select="@DeviceStatus" />
			</td>
			<td align="left">
				<xsl:value-of select="@DeviceURL" />
			</td>
		<td align="left">
				<xsl:value-of select="../@BaseDir" />
			</td>
			<td>
				<form>
					<input type="checkbox" Name="Dump" value="true">
						<xsl:if test="@Dump='true'">
							<xsl:attribute name="checked">true</xsl:attribute>
						</xsl:if>
					</input>
					<input type="hidden" name="UpdateDump" value="true" />
					<input type="submit" value="modify" />
				</form>
			</td>
			<td align="left">
				<a>
					<xsl:attribute name="href"><xsl:value-of
						select="$context" />/showQueue/<xsl:value-of select="@DeviceID" /></xsl:attribute>
					Queue for
					<xsl:value-of select="@DeviceID" />
				</a>
			</td>
			<td align="left">
				<xsl:value-of select="@QueueStatus" />
			</td>
			<td align="left">
				<xsl:value-of select="@QueueRunning" />
			</td>
			<td align="left">
				<xsl:value-of select="@QueueWaiting" />
			</td>
			<td align="left">
				<xsl:value-of select="@QueueCompleted" />
			</td>
			<td align="left">
				<xsl:value-of select="@QueueAll" />
			</td>
		</tr>
	</xsl:template>

	<!-- device spec -->
	<xsl:template match="XMLDevice[@Root='false']">
		<xsl:variable name="context" select="../@Context" />
		<tr>
			<td align="left">
				<a>
					<xsl:attribute name="href"><xsl:value-of
						select="$context" />/showDevice/<xsl:value-of select="@DeviceID" /></xsl:attribute>
					<xsl:value-of select="@DeviceID" />
				</a>
			</td>
			<td align="left">
				<xsl:value-of select="@DeviceType" />
			</td>
			<td align="left">
				<xsl:value-of select="@DeviceStatus" />
			</td>
			<td align="left">
				<xsl:value-of select="@DeviceURL" />
			</td>
			<td align="left">
				<a>
					<xsl:attribute name="href">./showQueue/<xsl:value-of
						select="@DeviceID" /></xsl:attribute>
					Queue for
					<xsl:value-of select="@DeviceID" />
				</a>
			</td>
			<td align="left">
				<xsl:value-of select="@QueueStatus" />
			</td>
			<td align="left">
				<xsl:value-of select="@QueueRunning" />
			</td>
			<td align="left">
				<xsl:value-of select="@QueueWaiting" />
			</td>
			<td align="left">
				<xsl:value-of select="@QueueCompleted" />
			</td>
			<td align="left">
				<xsl:value-of select="@QueueAll" />
			</td>
		</tr>
	</xsl:template>


	<!-- -->
	<xsl:template match="Template">
		<xsl:variable name="context" select="../@Context" />
		<tr>
			<td align="left">
				<xsl:value-of select="@DeviceType" />
			</td>
			<td align="left">
				<form>
					<xsl:attribute name="action"><xsl:value-of
						select="$context" />/addDevice/<xsl:value-of select="/DeviceList/XMLDevice[@Root='true']/@DeviceID" /></xsl:attribute>

					<input type="text" name="DeviceID">
							<xsl:attribute name="value"><xsl:value-of
							select="@DeviceID" /></xsl:attribute>
					</input>
					<input type="hidden" name="DeviceType">
							<xsl:attribute name="value"><xsl:value-of
							select="@DeviceType" /></xsl:attribute>
					</input>
					<input type="submit" value="add Device" />
				</form>
			</td>
		</tr>
	</xsl:template>

	<xsl:include href="topnavigation.xsl" />
	<xsl:include href="CPUTimer.xsl" />

</xsl:stylesheet>