<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<!-- (C) 2001-2015 CIP4 -->
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml"
	xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1">
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
						select="$context" />/css/styles_pc.css</xsl:attribute>
				</link>
				<link rel="icon" type="image/x-icon">
					<xsl:attribute name="href"><xsl:value-of
						select="$context" />/favicon.ico</xsl:attribute>
				</link>
				<title>
					<xsl:value-of select="$deviceType" />
					- Operator Login for device:
					<xsl:value-of select="$deviceID" />
				</title>
			</head>

			<!-- Body only -->
			<body>
				<xsl:call-template name="topnavigation" />
				<h1>
					<xsl:value-of select="$deviceType" />
					- Operator Login for device:
					<xsl:value-of select="$deviceID" />
				</h1>

				<h2>Currently logged into this device</h2>
				<table>
					<tbody>
						<tr>
							<th>Employee ID</th>
							<th>Name</th>
							<th>Roles</th>
						</tr>
						<xsl:for-each select="jdf:Employee">
							<xsl:call-template name="modifyEmployee" />
						</xsl:for-each>
					</tbody>
				</table>

				<xsl:apply-templates select="KnownEmployees" />

				<hr />

				<h3>Device</h3>
				<table>
					<tr>
						<td>
							<form style="margin-left: 20px">
								<xsl:attribute name="action"><xsl:value-of
									select="$context" />/showDevice/<xsl:value-of
									select="@DeviceID" /></xsl:attribute>
								<input type="hidden" name="refresh" value="false" />
								<input type="submit" value="refresh page" />
							</form>
						</td>
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
						<td>
							<form style="margin-left: 20px">
								<xsl:attribute name="action"><xsl:value-of
									select="$context" />/showDevice/<xsl:value-of
									select="@DeviceID" /></xsl:attribute>
								<input type="hidden" name="setup" value="true" />
								<input type="submit" value="device setup" />
							</form>
						</td>
						<xsl:if test="@login='true'">
							<td>
								<form style="margin-left: 20px">
									<xsl:attribute name="action"><xsl:value-of
										select="$context" />/showDevice/<xsl:value-of
										select="@DeviceID" /></xsl:attribute>
									<input type="hidden" name="setup" value="false" />
									<input type="submit" value="show console" />
								</form>
							</td>
						</xsl:if>
					</tr>
				</table>

			</body>
		</html>
	</xsl:template>
	<!-- modifiable Employee -->
	<xsl:template match="KnownEmployees">
		<h2>Login Operator</h2>
		<form>
			<table>
				<tr>
					<td>
						<select name="PersonalID">
							<xsl:for-each select="jdf:Employee">
								<xsl:call-template name="loginEmployee" />
							</xsl:for-each>
						</select>
					</td>
					<td>
						<input type="submit" value="login" title="login operator" />
						<input type="hidden" name="inout" value="login" />
					</td>
				</tr>
			</table>
		</form>
	</xsl:template>

	<!-- modifiable Employee -->
	<xsl:template name="loginEmployee">
		<option>
			<xsl:value-of select="@ProductID" />
			-
			<xsl:value-of select="jdf:Person/@DescriptiveName" />
		</option>
	</xsl:template>

	<!-- modifiable Employee -->
	<xsl:template name="modifyEmployee">
		<xsl:variable name="context" select="/XMLDevice/@Context" />
		<xsl:variable name="deviceID" select="/XMLDevice/@DeviceID" />
		<tr>
			<td>
				<form>
					<xsl:attribute name="action"><xsl:value-of
						select="$context" />/login/<xsl:value-of select="$deviceID" /></xsl:attribute>
					<input type="hidden" name="PersonalID">
						<xsl:attribute name="value"><xsl:value-of
							select="@ProductID" /></xsl:attribute>
					</input>
					<input type="hidden" name="inout" value="logout" />
					<input type="submit" title="log off operator">
						<xsl:attribute name="value">log off <xsl:value-of
							select="@ProductID" /></xsl:attribute>
					</input>
				</form>
			</td>
			<td>
				<xsl:value-of select="jdf:Person/@DescriptiveName" />
			</td>
			<td>
				<xsl:value-of select="@Roles" />
			</td>
		</tr>
	</xsl:template>
	<xsl:include href="topnavigation.xsl" />
</xsl:stylesheet>