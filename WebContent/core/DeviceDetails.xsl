<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml"
	xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:strip-space elements="*" />
	<xsl:output method="html" />



	<xsl:template name="devicedetails">
		<xsl:variable name="deviceID" select="@DeviceID" />
		<xsl:variable name="deviceType" select="@DeviceType" />
		<xsl:variable name="deviceURL" select="@DeviceURL" />
		<xsl:variable name="deviceStatus" select="@DeviceStatus" />
		<xsl:variable name="context" select="@Context" />
		<xsl:variable name="modify" select="@modify" />

		<div style="margin-left: 20px">
			<form style="margin-left: 20px">
				<table>
					<xsl:attribute name="action">.</xsl:attribute>
					<tr>
						<td colspan="3">
							<h3>Details of the Bambi device</h3>
						</td>
					</tr>

					<xsl:call-template name="modifyString">
						<xsl:with-param name="attLabel" select="'ID: '" />
						<xsl:with-param name="attName" select="'unused'" />
						<xsl:with-param name="attVal" select="$deviceID" />
						<xsl:with-param name="modify" select="'false'" />
						<xsl:with-param name="desc" select="'Unique device Identifier'" />
					</xsl:call-template>

					<xsl:call-template name="modifyString">
						<xsl:with-param name="attLabel" select="'URL: '" />
						<xsl:with-param name="attName" select="'DeviceURL'" />
						<xsl:with-param name="attVal" select="$deviceURL" />
						<xsl:with-param name="modify" select="'false'" />
						<xsl:with-param name="desc"
							select="'Base (root) URL of the device'" />
					</xsl:call-template>

					<xsl:call-template name="modifyString">
						<xsl:with-param name="attLabel" select="'Device Class: '" />
						<xsl:with-param name="attName" select="'DeviceType'" />
						<xsl:with-param name="attVal" select="$deviceType" />
						<xsl:with-param name="modify" select="$modify" />
						<xsl:with-param name="desc"
							select="'Short, human readable description of the device'" />
					</xsl:call-template>

				<xsl:call-template name="modifyString">
						<xsl:with-param name="attLabel" select="'Entries Processed '" />
						<xsl:with-param name="attName" select="'EntriesProcessed'" />
						<xsl:with-param name="attVal" select="@EntriesProcessed" />
						<xsl:with-param name="modify" select="'false'" />
						<xsl:with-param name="desc"
							select="'Number of entries processed since startup'" />
					</xsl:call-template>

					<xsl:call-template name="modifyString">
						<xsl:with-param name="attLabel" select="'Watch URL: '" />
						<xsl:with-param name="attName" select="'WatchURL'" />
						<xsl:with-param name="attVal" select="@WatchURL" />
						<xsl:with-param name="modify" select="$modify" />
						<xsl:with-param name="desc"
							select="'Single URL that receives messages (Status, Resource, Notification) from the device. If empty, no non-subscribed messages are sent.'" />
					</xsl:call-template>

					<xsl:call-template name="modifyString">
						<xsl:with-param name="attLabel" select="'JDF Type expression: '" />
						<xsl:with-param name="attName" select="'TypeExpression'" />
						<xsl:with-param name="attVal" select="@TypeExpression" />
						<xsl:with-param name="modify" select="$modify" />
						<xsl:with-param name="desc"
							select="'Regular expression of types that are accepted by this device.'" />
					</xsl:call-template>

					<xsl:if test="@Dump">
						<xsl:call-template name="modifyString">
							<xsl:with-param name="attLabel"
								select="'Enable Dump of HTTP requests '" />
							<xsl:with-param name="attName" select="'Dump'" />
							<xsl:with-param name="attVal" select="@Dump" />
							<xsl:with-param name="modify" select="$modify" />
							<xsl:with-param name="desc"
								select="'Toggles the dump behavior of the entire application'" />
						</xsl:call-template>
					</xsl:if>

					<xsl:call-template name="modifyString">
						<xsl:with-param name="attLabel" select="'Input Hot Folder: '" />
						<xsl:with-param name="attName" select="'InputHF'" />
						<xsl:with-param name="attVal" select="@InputHF" />
						<xsl:with-param name="modify" select="$modify" />
						<xsl:with-param name="desc"
							select="'Input hot folder for the proxy. Drop JDF Files in here. This is for testing only.'" />
					</xsl:call-template>
					<xsl:call-template name="modifyString">
						<xsl:with-param name="attLabel" select="'Output Hot Folder: '" />
						<xsl:with-param name="attName" select="'OutputHF'" />
						<xsl:with-param name="attVal" select="@OutputHF" />
						<xsl:with-param name="modify" select="$modify" />
						<xsl:with-param name="desc"
							select="'Output folder for the proxy. Completed jdf files will be dropped here. This is for testing only.'" />
					</xsl:call-template>
					<xsl:call-template name="modifyString">
						<xsl:with-param name="attLabel" select="'Error Output Hot Folder: '" />
						<xsl:with-param name="attName" select="'ErrorHF'" />
						<xsl:with-param name="attVal" select="@ErrorHF" />
						<xsl:with-param name="modify" select="$modify" />
						<xsl:with-param name="desc"
							select="'Output error folder for the proxy. Aborted jdf files will be dropped here. This is for testing only.'" />
					</xsl:call-template>

					<xsl:if test="@SlaveURL">
						<tr>
							<td colspan="3">
								<h3>Details of the Slave(3rd Party) device</h3>
							</td>
						</tr>
						<xsl:call-template name="modifyString">
							<xsl:with-param name="attLabel" select="'Proxy URL for Slave: '" />
							<xsl:with-param name="attName" select="'DeviceURLForSlave'" />
							<xsl:with-param name="attVal" select="@DeviceURLForSlave" />
							<xsl:with-param name="modify" select="'false'" />
							<xsl:with-param name="desc"
								select="'URL of the proxy device for the slave (3rd party device).'" />
						</xsl:call-template>
						<xsl:call-template name="modifyString">
							<xsl:with-param name="attLabel" select="'Slave Entries: '" />
							<xsl:with-param name="attName" select="'MaxPush'" />
							<xsl:with-param name="attVal" select="@MaxPush" />
							<xsl:with-param name="modify" select="$modify" />
							<xsl:with-param name="desc"
								select="'Maximum number of concurrent entries to actively send to the device. If 0, device will pull entries.'" />
						</xsl:call-template>
						<xsl:call-template name="modifyString">
							<xsl:with-param name="attLabel" select="'Slave Input Hot Folder: '" />
							<xsl:with-param name="attName" select="'SlaveInputHF'" />
							<xsl:with-param name="attVal" select="@SlaveInputHF" />
							<xsl:with-param name="modify" select="$modify" />
							<xsl:with-param name="desc"
								select="'Input folder for the slave (3rd party) device. The proxy will forward to this folder. If empty jobs are submitted to the slave URL via http.'" />
						</xsl:call-template>
						<xsl:call-template name="modifyString">
							<xsl:with-param name="attLabel" select="'Slave Output Hot Folder: '" />
							<xsl:with-param name="attName" select="'SlaveOutputHF'" />
							<xsl:with-param name="attVal" select="@SlaveOutputHF" />
							<xsl:with-param name="modify" select="$modify" />
							<xsl:with-param name="desc"
								select="'Output folder for the slave (3rd party) device. The proxy will watch this folder. If empty jobs must be returned to the proxy slave URL via http.'" />
						</xsl:call-template>
						<xsl:call-template name="modifyString">
							<xsl:with-param name="attLabel"
								select="'Slave Error Output Hot Folder: '" />
							<xsl:with-param name="attName" select="'SlaveErrorHF'" />
							<xsl:with-param name="attVal" select="@SlaveErrorHF" />
							<xsl:with-param name="modify" select="$modify" />
							<xsl:with-param name="desc"
								select="'Error output folder for the slave (3rd party) device. The proxy will watch this folder. If empty jobs must be returned to the proxy slave URL via http.'" />
						</xsl:call-template>
						<xsl:call-template name="modifyString">
							<xsl:with-param name="attLabel" select="'Slave URL: '" />
							<xsl:with-param name="attName" select="'SlaveURL'" />
							<xsl:with-param name="attVal" select="@SlaveURL" />
							<xsl:with-param name="modify" select="$modify" />
							<xsl:with-param name="desc"
								select="'URL of the slave (3rd party) device.'" />
						</xsl:call-template>
						<xsl:call-template name="modifyString">
							<xsl:with-param name="attLabel" select="'Slave DeviceID: '" />
							<xsl:with-param name="attName" select="'SlaveDeviceID'" />
							<xsl:with-param name="attVal" select="@SlaveDeviceID" />
							<xsl:with-param name="modify" select="$modify" />
							<xsl:with-param name="desc"
								select="'Device ID of the slave (3rd party) device.'" />
						</xsl:call-template>
					</xsl:if>
					<xsl:if test="$modify='true'">
						<tr>
							<input type="submit" value="Modify" />
						</tr>
					</xsl:if>
				</table>
			</form>
		</div>
	</xsl:template>

</xsl:stylesheet>