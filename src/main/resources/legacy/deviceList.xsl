<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:strip-space elements="*" />
	<xsl:output method="html" />
	<xsl:template match="/DeviceList">
		<html>
			<xsl:variable name="context" select="@Context" />

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
									Device List
								</li>
							</ul>
						</div>
					</div>

					<!-- device list title -->
					<div class="row pt-2">
						<div class="col-12">
							<h1>Device List</h1>
							<p>List of all Devices and Controllers simulated by this CIP4 Bambi instance.</p>
						</div>
					</div>

					<!-- root controller -->
					<div class="row mt-4">
						<div class="col-12">
							<h2>Root Controller</h2>
							<table class="table table-bordered">
								<thead class="thead-light">
									<tr>
										<th>Controller ID</th>
										<th>Status</th>
										<th>URL / Base Directory</th>
										<th>Dump</th>
										<th>Controller Queue</th>
									</tr>
								</thead>
								<tbody>
									<xsl:apply-templates select="XMLDevice[@Root='true']" />
								</tbody>
							</table>
						</div>
					</div>


					<!-- known devices -->
					<div class="row mt-4">
						<div class="col-12">
							<h2>Known Devices</h2>
							<table class="table table-bordered">
								<thead class="thead-light">
									<tr>
										<th>Device ID</th>
										<th>Status</th>
										<th>URL</th>
										<th>Device Queue</th>
									</tr>
								</thead>
								<tbody>
									<xsl:apply-templates select="XMLDevice[@Root='false']" />
								</tbody>
							</table>
						</div>
					</div>


					<!-- device templates devices -->
	`				<xsl:if test="Template">
						<div class="row">
							<div class="col-12">
								<h2>Device Templates</h2>
								<table class="table table-bordered">
									<thead class="thead-light">
										<tr>
											<th>Device Type</th>
											<th>Device ID</th>
										</tr>
									</thead>
									<tbody>
										<xsl:apply-templates select="Template" />
									</tbody>
								</table>
							</div>
						</div>
					</xsl:if>

					<!-- Metrics -->
					<div class="row mt-4">
						<div class="col-12">
							<h2>Metrics</h2>
							<xsl:call-template name="cpu-timer" />
						</div>
					</div>
					<div class="row">
						<div class="col-4">
							<table class="table table-bordered table-sm table-hover">
								<tbody>
									<tr>
										<th>Requests Handled:</th>
										<td><xsl:value-of select="@NumRequests" /></td>
									</tr>
									<tr>
										<th>Free Memory:</th>
										<td><xsl:value-of select="@MemFree" /></td>
									</tr>
									<tr>
										<th>Currently used Memory:</th>
										<td><xsl:value-of select="@MemCurrent" /></td>
									</tr>
									<tr>
										<th>Total Allocated Memory:</th>
										<td><xsl:value-of select="@MemTotal" /></td>
									</tr>
								</tbody>
							</table>
						</div>
					</div>

					<!-- Version Details -->
					<div class="row mt-4">
						<div class="col-12">
							<xsl:call-template name="version" />
						</div>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>

	<!-- Table Body: Root Controller -->
	<xsl:template match="XMLDevice[@Root='true']">
		<xsl:variable name="context" select="../@Context" />
		<tr>
			<td>
				<p><xsl:value-of select="@DeviceID" /></p>
				<small><xsl:value-of select="@DeviceType" /></small>
			</td>
			<td>
				<b><xsl:value-of select="@DeviceStatus" /></b>
			</td>
			<td>
				<p><xsl:value-of select="@DeviceURL" /></p>
				<p><xsl:value-of select="../@BaseDir" /></p>
			</td>
			<td>
				<form>
					<div class="form-group form-check">
						<label class="form-check-label">
							<input type="checkbox" class="form-check-input" Name="Dump" value="true">
								<xsl:if test="@Dump='true'">
									<xsl:attribute name="checked">true</xsl:attribute>
								</xsl:if>
							</input>Is Enabled
						</label>
					</div>
					<input type="hidden" name="UpdateDump" value="true" />
					<input type="submit" value="Update" class="btn btn-outline-secondary" />
				</form>
			</td>
			<td>
				<p>
					<b><xsl:value-of select="@QueueStatus" /></b>
				</p>
				<p>
					Running: <xsl:value-of select="@QueueRunning" /> /
					Waiting: <xsl:value-of select="@QueueWaiting" /> /
					Completed: <xsl:value-of select="@QueueCompleted" />
				</p>
				<p>
					<small>
						<a>
							<xsl:attribute name="href"><xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" /></xsl:attribute>
							more details &gt;&gt;
						</a>
					</small>
				</p>
			</td>
		</tr>
	</xsl:template>

	<!-- Table Body: Device -->
	<xsl:template match="XMLDevice[@Root='false']">
		<xsl:variable name="context" select="../@Context" />
		<tr>
			<td>
				<p>
					<a>
						<xsl:attribute name="href">
							<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" />
						</xsl:attribute>
						<xsl:value-of select="@DeviceID" />
					</a>
				</p>
				<small><xsl:value-of select="@DeviceType" /></small>
			</td>
			<td>
				<b><xsl:value-of select="@DeviceStatus" /></b>
			</td>
			<td>
				<xsl:value-of select="@DeviceURL" />
			</td>
			<td>
				<p>
					<b><xsl:value-of select="@QueueStatus" /></b>
				</p>
				<p>
					Running: <xsl:value-of select="@QueueRunning" /> /
					Waiting: <xsl:value-of select="@QueueWaiting" /> /
					Completed: <xsl:value-of select="@QueueCompleted" />
				</p>
				<p>
					<small>
						<a>
							<xsl:attribute name="href"><xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" /></xsl:attribute>
							more details &gt;&gt;
						</a>
					</small>
				</p>
			</td>
		</tr>
	</xsl:template>


	<!-- Table Body: Template -->
	<xsl:template match="Template">
		<xsl:variable name="context" select="../@Context" />
		<tr>
			<td>
				<xsl:value-of select="@DeviceType" />
			</td>
			<td>
				<form class="form-inline mb-0">
					<xsl:attribute name="action">
						<xsl:value-of select="$context" />/addDevice/<xsl:value-of select="/DeviceList/XMLDevice[@Root='true']/@DeviceID" />
					</xsl:attribute>

					<input type="hidden" name="DeviceType">
						<xsl:attribute name="value"><xsl:value-of select="@DeviceType" /></xsl:attribute>
					</input>

					<input type="text" class="form-control mr-sm-2" placeholder="Device ID" name="DeviceID">
						<xsl:attribute name="value"><xsl:value-of select="@DeviceID" /></xsl:attribute>
					</input>

					<input type="submit" class="btn btn-outline-secondary" value="Add Device" />
				</form>
			</td>
		</tr>
	</xsl:template>

	<xsl:include href="modules/head-content.module.xsl" />
	<xsl:include href="modules/cpu-timer.module.xsl" />
	<xsl:include href="modules/version.module.xsl" />
</xsl:stylesheet>
