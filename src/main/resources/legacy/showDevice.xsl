<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:strip-space elements="*" />
	<xsl:output method="html" />
	<xsl:template match="/XMLDevice">
		<html>
			<xsl:variable name="deviceID" select="@DeviceID" />
			<xsl:variable name="deviceType" select="@DeviceType" />
			<xsl:variable name="context" select="@Context" />
			<xsl:variable name="mutable" select="@mutable" />

			<head>
				<xsl:call-template name="head-content" />

				<xsl:if test="@refresh='true'">
					<meta http-equiv="refresh">
						<xsl:attribute name="content">
							15; URL=<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="$deviceID" />?refresh=true
						</xsl:attribute>
					</meta>
				</xsl:if>
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
									Device <xsl:value-of select="$deviceID" />
								</li>
							</ul>
						</div>
					</div>

					<!-- device title -->
					<div class="row pt-2">
						<div class="col-12">
							<h1>Device <xsl:value-of select="$deviceID" /></h1>
							<p><xsl:value-of select="$deviceType" /></p>
						</div>
					</div>

					<!-- control buttons -->
					<div class="row">
						<div class="col-8">
							<ul class="list-inline mb-2 mt-2">
								<li class="list-inline-item">
									<form class="mb-0">
										<xsl:attribute name="action">
											<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" />
										</xsl:attribute>
										<input type="hidden" name="refresh" value="false" />
										<input type="hidden" name="setup" value="true" />
										<input type="submit" class="btn btn-outline-secondary" value="Refresh" />
									</form>
								</li>

								<xsl:if test="@login='true'">
									<li class="list-inline-item">
										<form class="mb-0">
											<xsl:attribute name="action">
												<xsl:value-of select="$context" />/login/<xsl:value-of select="@DeviceID" />
											</xsl:attribute>
											<input type="submit" class="btn btn-outline-secondary" value="Login" />
										</form>
									</li>
								</xsl:if>

								<li class="list-inline-item">
									<form class="mb-0">
										<xsl:attribute name="action">
											<xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" />
										</xsl:attribute>
										<input type="submit" class="btn btn-outline-secondary" value="Show Queue" />
									</form>
								</li>
								<li class="list-inline-item">
									<form class="mb-0">
										<xsl:attribute name="action">
											<xsl:value-of select="$context" />/showSubscriptions/<xsl:value-of select="@DeviceID" />
										</xsl:attribute>
										<input type="submit" class="btn btn-outline-secondary" value="Show Subscriptions" />
									</form>
								</li>
							</ul>
						</div>
						<div class="col d-flex justify-content-end">
							<ul class="list-inline mb-2 mt-2">
								<xsl:if test="$mutable='true'">
									<li class="list-inline-item">
										<form class="mb-0">
											<xsl:attribute name="action">
												<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" />
											</xsl:attribute>
											<input type="hidden" name="setup" value="true" />
											<input type="hidden" name="restart" value="true" />
											<input type="submit" class="btn btn-outline-danger" value="Restart" />
										</form>
									</li>
									<li class="list-inline-item">
										<form class="mb-0">
											<xsl:attribute name="action">
												<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" />
											</xsl:attribute>
											<input type="hidden" name="setup" value="true" />
											<input type="hidden" name="reset" value="true" />
											<input type="submit" class="btn btn-outline-danger" value="Reset" />
										</form>
									</li>
								</xsl:if>
							</ul>
						</div>
					</div>

					<!-- links buttons -->
					<div class="row">
						<div class="col-12">
							<ul class="list-inline">
								<xsl:choose>
									<xsl:when test="@refresh='true'">
										<li class="list-inline-item">
											<a class="btn btn-link pl-0" role="button">
												<xsl:attribute name="href">
													<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="$deviceID" />?refresh=false
												</xsl:attribute>
												Modify page
											</a>
										</li>
									</xsl:when>
									<xsl:otherwise>
										<li class="list-inline-item">
											<a class="btn btn-link pl-0" role="button">
												<xsl:attribute name="href">
													<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="$deviceID" />?refresh=false
												</xsl:attribute>
												Reload Once
											</a>
										</li>
										<li class="list-inline-item">
											<a class="btn btn-link" role="button">
												<xsl:attribute name="href">
													<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="$deviceID" />?refresh=true
												</xsl:attribute>
												Reload Continually
											</a>
										</li>
									</xsl:otherwise>
								</xsl:choose>
							</ul>
						</div>
					</div>

					<!-- device details -->
					<div class="row mt-4">
						<div class="col-12">
							<h2>Device Details</h2>
							<table class="table table-borderless table-hover">
								<tbody>
									<tr class="d-flex">
										<th class="col-2 pl-0">ID:</th>
										<td class="col-5"><xsl:value-of select="@DeviceID" /></td>
										<td class="col-5">Unique device Identifier.</td>
									</tr>
									<tr class="d-flex">
										<th class="col-2 pl-0">Device URL:</th>
										<td class="col-5"><xsl:value-of select="@DeviceURL" /></td>
										<td class="col-5">Base URL of the device.</td>
									</tr>
									<tr class="d-flex">
										<th class="col-2 pl-0">Device Type:</th>
										<td class="col-5"><xsl:value-of select="@DeviceType" /></td>
										<td class="col-5">Short, <b>machine</b>-readable description of the device.</td>
									</tr>
									<tr class="d-flex">
										<th class="col-2 pl-0">Descriptive Name:</th>
										<td class="col-5"><xsl:value-of select="@Description" /></td>
										<td class="col-5">Short, <b>human</b>-readable description of the device.</td>
									</tr>
									<tr class="d-flex">
										<th class="col-2 pl-0">Entries Processed:</th>
										<td class="col-5"><xsl:value-of select="@EntriesProcessed" /></td>
										<td class="col-5">Number of entries processed since startup.</td>
									</tr>
									<tr class="d-flex">
										<th class="col-2 pl-0">Watch URL:</th>
										<td class="col-5"><xsl:value-of select="@WatchURL" /></td>
										<td class="col-5">Single URL that receives messages (Status, Resource, Notification) from the device. If empty, no non-subscribed messages are sent.</td>
									</tr>
									<tr class="d-flex">
										<th class="col-2 pl-0">JDF Type Expression:</th>
										<td class="col-5"><xsl:value-of select="@TypeExpression" /></td>
										<td class="col-5">Regular expression of types that are accepted by this device.</td>
									</tr>
									<xsl:if test="@Dump">
										<tr class="d-flex">
											<th class="col-2 pl-0">Dump:</th>
											<td class="col-5"><xsl:value-of select="@Dump" /></td>
											<td class="col-5">Toggles the dump behavior of the entire application.</td>
										</tr>
									</xsl:if>
									<tr class="d-flex">
										<th class="col-2 pl-0">Input Folder:</th>
										<td class="col-5"><xsl:value-of select="@InputHF" /></td>
										<td class="col-5">Input hot folder for the proxy. Drop JDF Files in here. This is for testing only.</td>
									</tr>
									<tr class="d-flex">
										<th class="col-2 pl-0">Output Folder:</th>
										<td class="col-5"><xsl:value-of select="@OutputHF" /></td>
										<td class="col-5">Output folder for the proxy. Completed jdf files will be dropped here. This is for testing only.</td>
									</tr>
									<tr class="d-flex">
										<th class="col-2 pl-0">Error Folder:</th>
										<td class="col-5"><xsl:value-of select="@ErrorHF" /></td>
										<td class="col-5">Output error folder for the proxy. Aborted jdf files will be dropped here. This is for testing only.</td>
									</tr>
								</tbody>
							</table>
						</div>
					</div>

					<!-- employee details -->
					<div class="row mt-4">
						<div class="col-12">
							<h2>Employee Details</h2>
							<p>Employess currently logged into this device:</p>
							<table class="table table-borderless table-sm table-hover">
								<thead class="thead-light">
									<tr>
										<th>Employee ID</th>
										<th>Name</th>
										<th>Roles</th>
									</tr>
								</thead>
								<tbody>
									<xsl:for-each select="jdf:Employee">
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
									</xsl:for-each>
								</tbody>
							</table>

							<xsl:if test="not(jdf:Employee)">
								<p><i>No Employees are logged in into this device.</i></p>
							</xsl:if>

							<form class="mb-0">
								<xsl:attribute name="action">
									<xsl:value-of select="$context" />/login/<xsl:value-of select="@DeviceID" />
								</xsl:attribute>
								<input type="submit" class="btn btn-outline-secondary" value="Login / Logout Employee" />
							</form>
						</div>
					</div>

					<!-- metrics -->
					<div class="row mt-4">
						<div class="col-12">
							<h2>Metrics</h2>
							<xsl:call-template name="cpu-timer" />
						</div>
					</div>

					<!-- further stuff -->
					<div class="row mt-4">
						<div class="col-12">
							<xsl:apply-templates />
						</div>
					</div>

					<!-- version details -->
					<div class="row mt-4">
						<div class="col-12">
							<xsl:call-template name="version" />
						</div>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>

	<xsl:include href="modules/head-content.module.xsl" />
	<xsl:include href="modules/cpu-timer.module.xsl" />
	<xsl:include href="modules/version.module.xsl" />

	<!-- ============================================================ -->

	<xsl:include href="CPUTimer.xsl" />
	<xsl:include href="processor.xsl" />
	<xsl:include href="DeviceExtension.xsl" />
	<xsl:include href="optionlist.xsl" />
	<xsl:include href="DeviceDetails.xsl" />



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