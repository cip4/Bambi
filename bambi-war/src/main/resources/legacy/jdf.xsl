<!-- Copyright 2009-2015 CIP4 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1"
	xmlns:bambi="www.cip4.org/Bambi">
	<xsl:template match="/*">
		<xsl:variable name="context" select="@Context" />
		<html>
			<head>
				<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
				<link rel="stylesheet" type="text/css" href="/legacy/css/styles_pc.css" />
				<link rel="icon" href="/legacy/favicon.ico" type="image/x-icon" />
				<title>
					JDF Job Ticket Summary
					<xsl:value-of select="@JobID" />
				</title>
			</head>
			<body>
				<xsl:call-template name="topnavigation" />
				<table cellspacing="5" border="2" bgcolor="#dddddd">
					<tr>
						<td>
							<a>
								<xsl:attribute name="href"><xsl:value-of
									select="$context" />/showQueue/<xsl:value-of
									select="@DeviceID" /></xsl:attribute>
								Back to Queue
							</a>
						</td>
						<td>
							<a>
								<xsl:attribute name="href"><xsl:value-of
									select="$context" />/showDevice/<xsl:value-of
									select="@DeviceID" /></xsl:attribute>
								Back to Device
							</a>
						</td>
						<td>
							<a>
								<xsl:attribute name="href"><xsl:value-of
									select="$context" />/showJDF/<xsl:value-of select="@DeviceID" />?qeID=<xsl:value-of
									select="@QueueEntryID" /></xsl:attribute>
								Show internal JDF
							</a>
						</td>
						<td>
							<a>
								<xsl:attribute name="href"><xsl:value-of
									select="$context" />/showJDF/<xsl:value-of select="@DeviceID" />?fix=true&amp;qeID=<xsl:value-of
									select="@QueueEntryID" /></xsl:attribute>
								Show refined JDF
							</a>
						</td>
						<td>
							<a>
								<xsl:attribute name="href"><xsl:value-of
									select="$context" />/showJDF/<xsl:value-of select="@DeviceID" />?raw=true&amp;qeID=<xsl:value-of
									select="@QueueEntryID" /></xsl:attribute>
								Show JDF XML Source
							</a>
						</td>
						<!-- todo handle cleanup <td> <a title="remove all outstanding spawns"> 
							<xsl:attribute name="href"><xsl:value-of select="$context"/>/showJDF/<xsl:value-of 
							select="@DeviceID"/>?repair=true&amp;qeID=<xsl:value-of select="@QueueEntryID"/></xsl:attribute> 
							Repair JDF </a> </td> -->
					</tr>
				</table>
				<h1>
					JDF Job Ticket
					<xsl:value-of select="@JobID" />
				</h1>
				<table cellspacing="1" border="1">
					<tr bgcolor="#bbbbbb">
						<th align="left">JobPartID</th>
						<th align="left">Status</th>
						<th align="left">Description</th>
						<th align="left">Type</th>
						<th align="left">Types</th>
					</tr>
					<xsl:call-template name="jdf" /> <!-- add myself -->
				</table>

			</body>
		</html>
	</xsl:template>

	<!-- ///////////////////////////////////////////////// -->

	<!-- one / node -->
	<xsl:template match="jdf:JDF" name="jdf">
		<tr>
			<xsl:if test="@Status='InProgress'">
				<xsl:attribute name="bgcolor">#aaffaa</xsl:attribute>
			</xsl:if>
			<xsl:if test="@Status='Waiting'">
				<xsl:attribute name="bgcolor">#aaaaff</xsl:attribute>
			</xsl:if>
			<xsl:if test="@Status='Spawned'">
				<xsl:attribute name="bgcolor">#aaffaa</xsl:attribute>
			</xsl:if>
			<xsl:if test="@Status='Part'">
				<xsl:attribute name="bgcolor">#ffffaa</xsl:attribute>
			</xsl:if>
			<xsl:if test="@Status='Aborted'">
				<xsl:attribute name="bgcolor">#ffaaaa</xsl:attribute>
			</xsl:if>
			<xsl:if test="@Status='Completed'">
				<xsl:attribute name="bgcolor">#dddddd</xsl:attribute>
			</xsl:if>
			<td>
				<a>
					<xsl:attribute name="href"><xsl:value-of
						select="/jdf:JDF/@Context" />/showJDF/<xsl:value-of
						select="/jdf:JDF/@DeviceID" />?qeID=<xsl:value-of
						select="/jdf:JDF/@QueueEntryID" />&amp;JobPartID=<xsl:value-of
						select="@JobPartID" /></xsl:attribute>
					<xsl:value-of select="@JobPartID" />
				</a>
			</td>
			<td>
				<xsl:value-of select="@Status" />
			</td>
			<td>
				<xsl:value-of select="@DescriptiveName" />
			</td>
			<td>
				<xsl:value-of select="@Type" />
			</td>
			<td>
				<xsl:value-of select="@Types" />
			</td>
		</tr>

		<xsl:apply-templates select="jdf:JDF" />

	</xsl:template>

	<xsl:include href="topnavigation.xsl" />

</xsl:stylesheet>