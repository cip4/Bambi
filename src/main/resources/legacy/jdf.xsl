<!-- Copyright 2009-2015 CIP4 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi">
	<xsl:template match="/*">
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
									<a><xsl:attribute name="href"><xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" /></xsl:attribute>Queue</a>
								</li>
								<li>
									Job: <xsl:value-of select="@JobID" />
								</li>
							</ul>
						</div>
					</div>

					<!-- job title -->
					<div class="row pt-2">
						<div class="col-12">
							<h1>Job <xsl:value-of select="@JobID" /></h1>
							<p>Visualization of the Jobs JDF.</p>
						</div>
					</div>

					<!-- control buttons -->
					<div class="row">
						<div class="col-12">
							<ul class="list-inline">
								<li class="list-inline-item">
									<a class="btn btn-outline-secondary">
										<xsl:attribute name="href">
											<xsl:value-of select="$context" />/showJDF/<xsl:value-of select="@DeviceID" />?qeID=<xsl:value-of select="@QueueEntryID" />
										</xsl:attribute>
										Show Internal JDF
									</a>
								</li>
								<li class="list-inline-item">
									<a class="btn btn-outline-secondary">
										<xsl:attribute name="href">
											<xsl:value-of select="$context" />/showJDF/<xsl:value-of select="@DeviceID" />?fix=true&amp;qeID=<xsl:value-of select="@QueueEntryID" />
										</xsl:attribute>
										Show Refined JDF
									</a>
								</li>
								<li class="list-inline-item">
									<a class="btn btn-outline-secondary">
										<xsl:attribute name="href">
											<xsl:value-of select="$context" />/showJDF/<xsl:value-of select="@DeviceID" />?raw=true&amp;qeID=<xsl:value-of select="@QueueEntryID" />
										</xsl:attribute>
										Show JDF XML Source
									</a>
								</li>
							</ul>
						</div>
					</div>

					<!-- queue summary -->
					<div class="row pt-4">
						<div class="col-12">
							<table class="table">
								<thead class="thead-light">
									<tr>
										<th>JobPartID</th>
										<th>Status</th>
										<th>Description</th>
										<th>Type</th>
										<th>Types</th>
									</tr>
								</thead>
								<tbody>
									<xsl:for-each select="/jdf:JDF">
										<tr>
											<xsl:choose>
												<xsl:when test="@Status='InProgress'">
													<xsl:attribute name="class">table-success</xsl:attribute>
												</xsl:when>
												<xsl:when test="@Status='Waiting'">
													<xsl:attribute name="class"></xsl:attribute>
												</xsl:when>
												<xsl:when test="@Status='Spawned'">
													<xsl:attribute name="class">table-warning</xsl:attribute>
												</xsl:when>
												<xsl:when test="@Status='Part'">
													<xsl:attribute name="class">table-warning</xsl:attribute>
												</xsl:when>
												<xsl:when test="@Status='Aborted'">
													<xsl:attribute name="class">table-danger</xsl:attribute>
												</xsl:when>
												<xsl:when test="@Status='Completed'">
													<xsl:attribute name="class">table-light text-muted</xsl:attribute>
												</xsl:when>
											</xsl:choose>
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
									</xsl:for-each>
								</tbody>
							</table>
						</div>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>

	<xsl:include href="modules/head-content.module.xsl" />

</xsl:stylesheet>