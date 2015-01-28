<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<!-- Copyright 2009-2014 CIP4 -->
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml"
	xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:strip-space elements="*" />
	<xsl:output method="html" />
	<xsl:template match="/XMLDevice">
		<html>
			<xsl:variable name="deviceID" select="@DeviceID" />
			<xsl:variable name="deviceType" select="@DeviceType" />
			<xsl:variable name="deviceURL" select="@DeviceURL" />
			<xsl:variable name="deviceStatus" select="@DeviceStatus" />
			<xsl:variable name="context" select="@Context" />
			<xsl:variable name="modify" select="@modify" />
			<head>
				<link rel="stylesheet" type="text/css">
					<xsl:attribute name="href"><xsl:value-of
						select="$context" />/legacy/css/styles_pc.css</xsl:attribute>
				</link>
				<link rel="icon" type="image/x-icon">
					<xsl:attribute name="href"><xsl:value-of
						select="$context" />/legacy/favicon.ico</xsl:attribute>
				</link>
				<title>
					<xsl:value-of select="$deviceType" />
					:
					<xsl:value-of select="$deviceID" />
				</title>
				<xsl:if test="@refresh='true'">
					<meta http-equiv="refresh">
						<xsl:attribute name="content">15; URL=<xsl:value-of
							select="$context" />/showDevice/<xsl:value-of
							select="$deviceID" />?refresh=true</xsl:attribute>
					</meta>
				</xsl:if>
			</head>

			<!-- Body only -->
			<body>
				<xsl:call-template name="topnavigation" />
				<h1>
					<xsl:value-of select="$deviceType" />
					- Device :
					<xsl:value-of select="$deviceID" />
				</h1>
				<p align="center">
					<table>
						<tr valign="bottom">
							<xsl:choose>
								<xsl:when test="@refresh='true'">
									<a>
										<xsl:attribute name="href"><xsl:value-of
											select="$context" />/showDevice/<xsl:value-of
											select="$deviceID" />?refresh=false</xsl:attribute>
										modify page
									</a>
								</xsl:when>
								<xsl:otherwise>
									<td>
										<a>
											<xsl:attribute name="href"><xsl:value-of
												select="$context" />/showDevice/<xsl:value-of
												select="$deviceID" />?refresh=false</xsl:attribute>
											reload once
										</a>
									</td>
									<td width="15" />
									<td>
										<a>
											<xsl:attribute name="href"><xsl:value-of
												select="$context" />/showDevice/<xsl:value-of
												select="$deviceID" />?refresh=true</xsl:attribute>
											reload continually
										</a>
									</td>
								</xsl:otherwise>
							</xsl:choose>
							<td>
								<img height="70" hspace="10" alt="logo">
									<xsl:attribute name="src"><xsl:value-of
										select="$context" />/logo.gif</xsl:attribute>
								</img>
							</td>
							<td>
								Go to
								<a>
									<xsl:attribute name="href"><xsl:value-of
										select="$context" />/overview</xsl:attribute>
									DeviceList
								</a>
							</td>
						</tr>
					</table>
				</p>

				<!-- device info section -->
				<hr />
				<table>
					<tr>
						<td>
							<form style="margin-left: 20px">
								<xsl:attribute name="action"><xsl:value-of
									select="$context" />/showDevice/<xsl:value-of
									select="@DeviceID" /></xsl:attribute>
								<input type="hidden" name="refresh" value="false" />
								<input type="hidden" name="setup" value="true" />
								<input type="submit" value="refresh page" />
							</form>
						</td>
						<td>
							<form style="margin-left: 20px">
								<xsl:attribute name="action"><xsl:value-of
									select="$context" />/showDevice/<xsl:value-of
									select="@DeviceID" /></xsl:attribute>
								<input type="hidden" name="shutdown" value="true" />
								<input type="hidden" name="setup" value="true" />
								<input type="submit" value="shutdown"
									title="attention this removes the device - adding a new device is not yet implemented!" />
							</form>
						</td>
						<td>
							<form style="margin-left: 20px">
								<xsl:attribute name="action"><xsl:value-of
									select="$context" />/showDevice/<xsl:value-of
									select="@DeviceID" /></xsl:attribute>
								<input type="submit" value="restart" />
								<input type="hidden" name="setup" value="true" />
								<input type="hidden" name="restart" value="true" />
							</form>
						</td>
						<td>
							<form style="margin-left: 20px">
								<xsl:attribute name="action"><xsl:value-of
									select="$context" />/showDevice/<xsl:value-of
									select="@DeviceID" /></xsl:attribute>
								<input type="submit" value="reset"
									title="attention this is a hard reset!" />
								<input type="hidden" name="setup" value="true" />
								<input type="hidden" name="reset" value="true" />
							</form>
						</td>
						<xsl:if test="$modify!='true'">
							<td>
								<form style="margin-left: 20px">
									<xsl:attribute name="action"><xsl:value-of
										select="$context" />/showDevice/<xsl:value-of
										select="@DeviceID" /></xsl:attribute>
									<input type="submit" value="modify"
										title="update / review device details" />
									<input type="hidden" name="modify" value="true" />
									<input type="hidden" name="setup" value="true" />
									<input type="hidden" name="refresh" value="false" />
								</form>
							</td>
						</xsl:if>
						<xsl:if test="@login='true'">
							<td>
								<form style="margin-left: 20px">
									<xsl:attribute name="action"><xsl:value-of
										select="$context" />/login/<xsl:value-of select="@DeviceID" /></xsl:attribute>
									<input type="submit" value="login" title="open operator login screen" />
								</form>
							</td>
						</xsl:if>
						<td>
							<form style="margin-left: 20px">
								<xsl:attribute name="action"><xsl:value-of
									select="$context" />/showQueue/<xsl:value-of
									select="@DeviceID" /></xsl:attribute>
								<input type="submit" value="show queue" />
							</form>
						</td>
						<td>
							<form style="margin-left: 20px">
								<xsl:attribute name="action"><xsl:value-of
									select="$context" />/showSubscriptions/<xsl:value-of
									select="@DeviceID" /></xsl:attribute>
								<input type="submit" value="show subscriptions" />
							</form>
						</td>
					</tr>
				</table>
				<hr />
				<xsl:if test="$modify='true'">
					<xsl:call-template name="devicedetails" />
					<hr />
				</xsl:if>
				<xsl:if test="$modify!='true'">
					<xsl:call-template name="devicedetails" />
					<hr />
				</xsl:if>

				<xsl:if test="jdf:Employee">
					<h3>Employess currently logged into this device</h3>
					<table>
						<tbody>
							<tr>
								<th>Employee ID</th>
								<th>Name</th>
								<th>Roles</th>
							</tr>
							<xsl:for-each select="jdf:Employee">
								<xsl:call-template name="showEmployee" />
							</xsl:for-each>
						</tbody>
					</table>
					<hr />
				</xsl:if>
				<xsl:apply-templates />
				<hr />
				<xsl:call-template name="cputimer" />

				<hr />

				<font size="-1" color="#b0c4de">
					<table>
						<xsl:if test="@VersionString">
							<tr>
								<td>Bambi Product Version:</td>
								<td>
									<xsl:value-of select="@VersionString" />
								</td>
							</tr>
						</xsl:if>
						<tr>
							<td>Bambi Internal Build: </td>
							<td>@build.number@ at @build.timestamp@</td>
						</tr>
						<tr>
							<td>JDFLib Build:</td>
							<td>@build.minorversion@, JDF Schema Version:
								@build.majorversion@
							</td>
						</tr>
					</table>
				</font>
			</body>
		</html>
	</xsl:template>

	<!-- ============================================================ -->

	<xsl:include href="processor.xsl" />
	<xsl:include href="DeviceExtension.xsl" />
	<xsl:include href="CPUTimer.xsl" />

	<!-- modifiable Employee -->
	<xsl:template name="showEmployee">
		<tr>
			<td>
				<xsl:value-of select="@ProductID" />
			</td>
			<td>
				<xsl:value-of select="jdf:Person/@DescriptiveName" />
			</td>
			<td>
				<xsl:value-of select="@Roles" />
			</td>
		</tr>
	</xsl:template>


	<!-- modifiable phase -->
	<xsl:template match="Phase">
		<br />
		<h2>Current Job Phase Setup</h2>

		<form style="margin-left: 20px">
			<xsl:attribute name="action">../processNextPhase/<xsl:value-of
				select="../@DeviceID" /></xsl:attribute>
			Device Status:
			<xsl:apply-templates select="bambi:OptionList[@name='DeviceStatus']" />
			<br />
			Device StatusDetails:
			<input name="DeviceStatusDetails" type="text" size="30"
				maxlength="30">
				<xsl:attribute name="value"><xsl:value-of
					select="@DeviceStatusDetails" /></xsl:attribute>
			</input>
			<br />
			Node Status:
			<xsl:apply-templates select="bambi:OptionList[@name='NodeStatus']" />
			<br />
			Node StatusDetails:
			<input name="NodeStatusDetails" type="text" size="30"
				maxlength="30">
				<xsl:attribute name="value"><xsl:value-of select="@NodeStatusDetails" /></xsl:attribute>
			</input>
			<br />
			Seconds to go:
			<xsl:value-of select="@Duration" />
			; new time to go:
			<input name="Duration" type="text" size="30" maxlength="30">
				<xsl:attribute name="value"></xsl:attribute>
			</input>
			<hr />
			<h3>Resource Simulation Speed Setup</h3>
			<table>
				<tr>
					<td>
						<input type="submit" value="update phase" />
					</td>
					<td>
						<form style="margin-left: 20px">
							<xsl:attribute name="action">../showDevice/<xsl:value-of
								select="@DeviceID" /></xsl:attribute>
							<input type="hidden" name="refresh" value="false" />
							<input type="hidden" name="setup" value="true" />
							<input type="submit" value="refresh page" />
						</form>
					</td>
				</tr>
			</table>
			<xsl:apply-templates select="ResourceAmount" />
		</form>
	</xsl:template>
	<xsl:include href="optionlist.xsl" />
	<xsl:include href="DeviceDetails.xsl" />

	<!-- resource amount setup -->
	<xsl:template match="ResourceAmount">
		<h4>
			<xsl:value-of select="@ResourceName" />
		</h4>
		<input type="hidden">
			<xsl:attribute name="name">Res<xsl:value-of
				select="@ResourceIndex" /></xsl:attribute>
			<xsl:attribute name="value"><xsl:value-of select="@ResourceName" /></xsl:attribute>
		</input>
		Waste Production:
		<input type="checkbox" value="true">
			<xsl:attribute name="name">Waste<xsl:value-of
				select="@ResourceIndex" /></xsl:attribute>
			<xsl:if test="@Waste='true'">
				<xsl:attribute name="checked">Waste</xsl:attribute>
			</xsl:if>
		</input>
		- Speed:
		<input type="text" size="10" maxlength="30">
			<xsl:attribute name="name">Speed<xsl:value-of
				select="@ResourceIndex" /></xsl:attribute>
			<xsl:attribute name="value"><xsl:value-of select="@Speed" /></xsl:attribute>
		</input>
		<br />
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="KnownEmployees">
		<!-- nop here -->
	</xsl:template>
	<xsl:template match="jdf:Employee">
		<!-- nop here -->
	</xsl:template>

	<xsl:include href="topnavigation.xsl" />

	<!-- add more templates -->
	<!-- the catchall -->
	<xsl:template match="*">
		<h3>
			Unhandled element:
			<xsl:value-of select="name()" />
		</h3>
		<xsl:apply-templates />
	</xsl:template>
</xsl:stylesheet>