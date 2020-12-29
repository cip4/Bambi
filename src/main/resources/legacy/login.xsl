<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1">
	<xsl:strip-space elements="*" />
	<xsl:output method="html" />
	<xsl:template match="/XMLDevice">
		<html>
			<xsl:variable name="deviceID" select="@DeviceID" />
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
									<a>
										<xsl:attribute name="href"><xsl:value-of select="$context" />/overview</xsl:attribute>
										DeviceList
									</a>
								</li>
								<li>
									<a>
										<xsl:attribute name="href"><xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" /></xsl:attribute>
										Device: <xsl:value-of select="$deviceID" />
									</a>
								</li>
								<li>
									Login / Logout
								</li>
							</ul>
						</div>
					</div>

					<!-- login title -->
					<div class="row pt-5">
						<div class="col-12">
							<h1>Operator Login <xsl:value-of select="$deviceID" /></h1>
						</div>
					</div>

					<!-- control buttons -->
					<div class="row">
						<div class="col-12">
							<ul class="list-inline mb-2 mt-2">
								<li class="list-inline-item">
									<form class="mb-0">
										<xsl:attribute name="action">
											<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" />
										</xsl:attribute>
										<input type="hidden" name="refresh" value="false" />
										<input type="submit" class="btn btn-outline-secondary" value="Refresh Page" />
									</form>
								</li>
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
								<li class="list-inline-item">
									<form class="mb-0">
										<xsl:attribute name="action">
											<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" />
										</xsl:attribute>
										<input type="hidden" name="setup" value="true" />
										<input type="submit" class="btn btn-outline-secondary" value="Device Setup" />
									</form>
								</li>

								<xsl:if test="@login='true'">
									<li class="list-inline-item">
										<form class="mb-0">
											<xsl:attribute name="action">
												<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" />
											</xsl:attribute>
											<input type="hidden" name="setup" value="false" />
											<input type="submit" class="btn btn-outline-secondary" value="Show Console" />
										</form>
									</li>
								</xsl:if>
							</ul>
						</div>
					</div>

					<!-- logged in operators -->
					<div class="row mt-5">
						<div class="col-8">
							<h2>Logged in Operators</h2>
							<table class="table table-hover">
								<thead class="thead-light">
									<tr class="d-flex">
										<th class="col-3">Employee ID</th>
										<th class="col-3">Name</th>
										<th class="col-3">Roles</th>
										<th class="col-3"></th>
									</tr>
								</thead>
								<tbody>
									<xsl:for-each select="jdf:Employee">
										<tr class="d-flex">
											<td class="col-3 pt-3">
												<xsl:value-of select="@ProductID" />
											</td>
											<td class="col-3 pt-3">
												<xsl:value-of select="jdf:Person/@DescriptiveName" />
											</td>
											<td class="col-3 pt-3">
												<xsl:value-of select="@Roles" />
											</td>
											<td class="col-3">
												<form class="mb-0">
													<xsl:attribute name="action">
														<xsl:value-of select="$context" />/login/<xsl:value-of select="$deviceID" />
													</xsl:attribute>
													<input type="hidden" name="PersonalID">
														<xsl:attribute name="value"><xsl:value-of select="@ProductID" /></xsl:attribute>
													</input>
													<input type="hidden" name="inout" value="logout" />
													<input type="submit" class="btn btn-outline-secondary form-control" value="Log Out" />
												</form>
											</td>
										</tr>
									</xsl:for-each>
								</tbody>
							</table>
							<xsl:if test="not(jdf:Employee)">
								<p><i>No operators are logged in into this device.</i></p>
							</xsl:if>
						</div>
					</div>

					<!-- login operators -->
					<div class="row mt-5">
						<div class="col-12">
							<h2>Login Operator</h2>
							<xsl:apply-templates select="KnownEmployees" />
						</div>
					</div>

					<!-- version details -->
					<div class="row mt-5">
						<div class="col-12">
							<xsl:call-template name="version" />
						</div>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>


	<!-- known employees -->
	<xsl:template match="KnownEmployees">
		<form class="form-inline">
			<select name="PersonalID" class="form-control mr-2">
				<xsl:for-each select="jdf:Employee">
					<option>
						<xsl:value-of select="@ProductID" /> - <xsl:value-of select="jdf:Person/@DescriptiveName" />
					</option>
				</xsl:for-each>
			</select>
			<input type="hidden" name="inout" value="login" />
			<input type="submit" class="btn btn-outline-secondary form-control" value="Login" />
		</form>
	</xsl:template>

	<xsl:include href="modules/head-content.module.xsl" />
	<xsl:include href="modules/version.module.xsl" />
</xsl:stylesheet>