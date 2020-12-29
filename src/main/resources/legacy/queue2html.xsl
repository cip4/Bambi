<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi">
	<xsl:output method="html" />
	<xsl:template match="jdf:Queue">
		<xsl:variable name="context" select="@Context" />

		<html>
			<head>
				<xsl:call-template name="head-content" />

				<xsl:if test="@refresh">
					<meta http-equiv="refresh">
						<xsl:attribute name="content">15; URL=<xsl:value-of
							select="$context" />/showQueue/<xsl:value-of select="@DeviceID" />?refresh=true</xsl:attribute>
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
									<a><xsl:attribute name="href"><xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" /></xsl:attribute>Device: <xsl:value-of select="@DeviceID" /></a>
								</li>
								<li>
									Queue
								</li>
							</ul>
						</div>
					</div>

					<!-- queue title -->
					<div class="row pt-2">
						<div class="col-12">
							<h1>Queue Device <xsl:value-of select="@DeviceID" /></h1>
							<p>Visualization and management of jobs in the devices' queue.</p>
						</div>
					</div>

					<!-- control buttons -->
					<div class="row">
						<div class="col-9">
							<ul class="list-inline mb-2 mt-2">
								<li class="list-inline-item">
									<form class="mb-0">
										<xsl:attribute name="action">
											<xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" />
										</xsl:attribute>
										<input type="hidden" name="open" value="true" />
										<input type="submit" class="btn btn-outline-secondary" value="Open Queue" />
									</form>
								</li>
								<li class="list-inline-item">
									<form class="mb-0">
										<xsl:attribute name="action">
											<xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" />
										</xsl:attribute>
										<input type="hidden" name="close" value="true" />
										<input type="submit" class="btn btn-outline-secondary" value="Close Queue" />
									</form>
								</li>

								<li class="list-inline-item ml-4">
									<form class="mb-0">
										<xsl:attribute name="action">
											<xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" />
										</xsl:attribute>
										<input type="hidden" name="resume" value="true" />
										<input type="submit" class="btn btn-outline-secondary" value="Resume Queue" />
									</form>
								</li>
								<li class="list-inline-item">
									<form class="mb-0">
										<xsl:attribute name="action">
											<xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" />
										</xsl:attribute>
										<input type="hidden" name="hold" value="true" />
										<input type="submit" class="btn btn-outline-secondary" value="Hold Queue" />
									</form>
								</li>

								<li class="list-inline-item ml-4">
									<form class="mb-0">
										<xsl:attribute name="action">
											<xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" />
										</xsl:attribute>
										<input type="hidden" name="pos">
											<xsl:attribute name="value"><xsl:value-of select="number(@pos)" /></xsl:attribute>
										</input>
										<input type="hidden" name="filter">
											<xsl:attribute name="value"><xsl:value-of select="@filter" /></xsl:attribute>
										</input>
										<input type="hidden" name="SortBy">
											<xsl:attribute name="value"><xsl:value-of select="@sortby" /></xsl:attribute>
										</input>
										<input type="hidden" name="refresh" value="true" />
										<input type="submit" class="btn btn-outline-secondary" value="Refresh Queue" />
									</form>
								</li>
							</ul>
						</div>
						<div class="col d-flex justify-content-end">
							<ul class="list-inline mb-2 mt-2">
								<li class="list-inline-item">
									<form class="mb-0">
										<xsl:attribute name="action">
											<xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" />
										</xsl:attribute>
										<input type="hidden" name="flush" value="true" />
										<input type="submit" class="btn btn-outline-danger" value="Flush Queue" />
									</form>
								</li>
							</ul>
						</div>
					</div>

					<!-- queue summary -->
					<div class="row pt-4">
						<div class="col-4">
							<p><b>Status: <xsl:value-of select="@Status" /></b></p>
							<table class="table table-bordered table-sm">
								<thead class="thead-light">
									<tr>
										<th>Queue Entry Status</th>
										<th>Number of Entries</th>
									</tr>
								</thead>
								<tbody>
									<tr>
										<td title="Total Size of Queue">All</td>
										<td><xsl:value-of select="@TotalQueueSize" /></td>
									</tr>
									<tr>
										<td title="Number of Displayed Entries">Shown</td>
										<td><xsl:value-of select="count(jdf:QueueEntry)" /></td>
									</tr>
									<xsl:if test="count(jdf:QueueEntry[@Status='Waiting'])>0">
										<tr>
											<td>Waiting</td>
											<td><xsl:value-of select="count(jdf:QueueEntry[@Status='Waiting'])" /></td>
										</tr>
									</xsl:if>
									<xsl:if test="count(jdf:QueueEntry[@Status='Running'])>0">
										<tr>
											<td>Running</td>
											<td><xsl:value-of select="count(jdf:QueueEntry[@Status='Running'])" /></td>
										</tr>
									</xsl:if>
									<xsl:if test="count(jdf:QueueEntry[@Status='Held'])>0">
										<tr>
											<td>Held</td>
											<td><xsl:value-of select="count(jdf:QueueEntry[@Status='Held'])" /></td>
										</tr>
									</xsl:if>
									<xsl:if test="count(jdf:QueueEntry[@Status='Suspended'])>0">
										<tr>
											<td>Suspended</td>
											<td><xsl:value-of select="count(jdf:QueueEntry[@Status='Suspended'])" /></td>
										</tr>
									</xsl:if>
									<xsl:if test="count(jdf:QueueEntry[@Status='Completed'])>0">
										<tr>
											<td>Completed</td>
											<td><xsl:value-of select="count(jdf:QueueEntry[@Status='Completed'])" /></td>
										</tr>
									</xsl:if>
									<xsl:if test="count(jdf:QueueEntry[@Status='Aborted'])>0">
										<tr>
											<td>Aborted</td>
											<td><xsl:value-of select="count(jdf:QueueEntry[@Status='Aborted'])" /></td>
										</tr>
									</xsl:if>
								</tbody>
							</table>
						</div>
					</div>

					<!-- queue entries -->
					<div class="row mt-4">
						<div class="col-12">
							<h2>Queue Entries</h2>
						</div>
					</div>
					<div class="row">
						<div class="col-4">
							<ul class="list-inline mb-2 mt-2">
								<li class="list-inline-item">
									<form class="form-inline mb-0">
										<xsl:attribute name="action">
											<xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" />
										</xsl:attribute>
										<input type="text" class="form-control" name="filter" autocomplete="off">
											<xsl:attribute name="value"><xsl:value-of select="@filter" /></xsl:attribute>
										</input>
										<input type="submit" class="btn btn-outline-secondary ml-2" value="Filter" />
									</form>
								</li>
							</ul>
						</div>
						<div class="col d-flex justify-content-end">
							<ul class="list-inline mb-2 mt-2">
								<xsl:if test="@pos&gt;0">
									<li class="list-inline-item">
										<form class="mb-0">
											<xsl:attribute name="action">
												<xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" />
											</xsl:attribute>
											<input type="hidden" name="pos" value="0" />
											<input type="hidden" name="SortBy">
												<xsl:attribute name="value"><xsl:value-of select="@sortby" /></xsl:attribute>
											</input>
											<input type="hidden" name="filter">
												<xsl:attribute name="value"><xsl:value-of select="@filter" /></xsl:attribute>
											</input>
											<input type="submit" class="btn btn-outline-secondary" value="&lt;&lt; First" title="show first frame" />
										</form>
									</li>
									<li class="list-inline-item">
										<form class="mb-0">
											<xsl:attribute name="action">
												<xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" />
											</xsl:attribute>
											<input type="hidden" name="pos">
												<xsl:attribute name="value"><xsl:value-of select="number(@pos)-1" /></xsl:attribute>
											</input>
											<input type="hidden" name="filter">
												<xsl:attribute name="value"><xsl:value-of select="@filter" /></xsl:attribute>
											</input>
											<input type="hidden" name="SortBy">
												<xsl:attribute name="value"><xsl:value-of select="@sortby" /></xsl:attribute>
											</input>
											<input type="submit" class="btn btn-outline-secondary" value="&lt; Previous" title="show previous frame" />
										</form>
									</li>
								</xsl:if>

								<xsl:if test="@hasNext">
									<li class="list-inline-item">
										<form class="mb-0">
											<xsl:attribute name="action">
												<xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" />
											</xsl:attribute>
											<input type="hidden" name="pos">
												<xsl:attribute name="value"><xsl:value-of select="number(@pos)+1" /></xsl:attribute>
											</input>
											<input type="hidden" name="SortBy">
												<xsl:attribute name="value"><xsl:value-of select="@sortby" /></xsl:attribute>
											</input>
											<input type="submit" class="btn btn-outline-secondary" value="Next &gt;" title="show next frame" />
										</form>
									</li>
									<li class="list-inline-item">
										<form class="mb-0">
											<xsl:attribute name="action">
												<xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" />
											</xsl:attribute>
											<input type="hidden" name="pos" value="-1" />

											<input type="hidden" name="filter">
												<xsl:attribute name="value"><xsl:value-of select="@filter" /></xsl:attribute>
											</input>
											<input type="hidden" name="SortBy">
												<xsl:attribute name="value"><xsl:value-of select="@sortby" /></xsl:attribute>
											</input>
											<input type="submit" class="btn btn-outline-secondary" value="Last &gt;&gt;" title="Show last frame" />
										</form>
									</li>
								</xsl:if>
							</ul>
						</div>
					</div>
					<div class="row">
						<div class="col-12">
							<table class="table table-hover table-sm">
								<thead class="thead-light">
									<tr>
										<th>#</th>
										<th>
											<xsl:call-template name="columnTitle">
												<xsl:with-param name="display" select="'QueueEntryID'" />
												<xsl:with-param name="attName" select="'QueueEntryID'" />
											</xsl:call-template>
										</th>
										<th>
											<xsl:call-template name="columnTitle">
												<xsl:with-param name="display" select="'Priority'" />
												<xsl:with-param name="attName" select="'Priority'" />
											</xsl:call-template>
										</th>
										<th>
											<xsl:call-template name="columnTitle">
												<xsl:with-param name="display" select="'Submission'" />
												<xsl:with-param name="attName" select="'SubmissionTime'" />
											</xsl:call-template>
										</th>
										<th>
											<xsl:call-template name="columnTitle">
												<xsl:with-param name="display" select="'Start'" />
												<xsl:with-param name="attName" select="'StartTime'" />
											</xsl:call-template>
										</th>
										<th>
											<xsl:call-template name="columnTitle">
												<xsl:with-param name="display" select="'End'" />
												<xsl:with-param name="attName" select="'EndTime'" />
											</xsl:call-template>
										</th>
										<th></th>
									</tr>
								</thead>
								<tbody>
									<xsl:for-each select="jdf:QueueEntry">
										<tr>
											<td class="pt-2">
												<xsl:variable name="c1" select="500*number(../@pos)" />
												<xsl:variable name="c2"><xsl:number count="jdf:QueueEntry" /></xsl:variable>
												<xsl:value-of select="$c1 + $c2" />
												<!-- submission button for pulling jobs -->
												<xsl:if test="../@Pull='true'">
													<xsl:if test="@Status='Waiting'">
														<xsl:if test="count(../jdf:QueueEntry[@Status='Running'])=0">
															<form>
																<xsl:attribute name="action">
																	<xsl:value-of select="$context" />/modifyQE/<xsl:value-of select="../@DeviceID" />
																</xsl:attribute>
																<input type="hidden" name="qeID">
																	<xsl:attribute name="value"><xsl:value-of select="@QueueEntryID" /></xsl:attribute>
																</input>
																<input type="hidden" name="submit" value="true" />
																<input type="submit" value="submit" />
															</form>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</td>
											<td class="pt-2">
												<a data-toggle="tooltip" data-html="true">
													<xsl:attribute name="href">
														<xsl:value-of select="$context" />/showJDF/<xsl:value-of select="../@DeviceID" />?qeID=<xsl:value-of select="@bambi:QueueEntryURL" />
													</xsl:attribute>
													<xsl:attribute name="title">
														JobID: <xsl:value-of select="@JobID" />&lt;br/&gt;
														JobPartID: <xsl:value-of select="@JobPartID" />&lt;br/&gt;
														Description: <xsl:value-of select="@DescriptiveName" />&lt;br/&gt;
														Activation: <xsl:value-of select="@Activation" />&lt;br/&gt;
														StatusDetails: <xsl:value-of select="@StatusDetails" />&lt;br/&gt;
														Device: <xsl:value-of select="@DeviceID" />&lt;br/&gt;
													</xsl:attribute>
													<xsl:value-of select="@QueueEntryID" />
												</a>
											</td>
											<td class="pt-2">
												<xsl:value-of select="@Priority" />
											</td>
											<td class="pt-2">
												<xsl:call-template name="dateTime">
													<xsl:with-param name="val" select="@SubmissionTime" />
												</xsl:call-template>
											</td>
											<td class="pt-2">
												<xsl:call-template name="dateTime">
													<xsl:with-param name="val" select="@StartTime" />
												</xsl:call-template>
											</td>
											<td class="pt-2">
												<xsl:call-template name="dateTime">
													<xsl:with-param name="val" select="@EndTime" />
												</xsl:call-template>
											</td>
											<td class="p-0">
												<form class="form-inline m-0 float-right">
													<xsl:attribute name="action">
														<xsl:value-of select="$context" />/modifyQE/<xsl:value-of select="../@DeviceID" />
													</xsl:attribute>
													<xsl:apply-templates select="bambi:OptionList" />
													<input type="hidden" name="qeID">
														<xsl:attribute name="value"><xsl:value-of select="@QueueEntryID" /></xsl:attribute>
													</input>
													<input type="submit" class="btn btn-outline-secondary btn-sm ml-2" value="Update" />
												</form>
											</td>
										</tr>
									</xsl:for-each>
								</tbody>
							</table>
						</div>
					</div>

					<div class="row">
						<div class="col-4">
							<ul class="list-inline mb-2 mt-2">
								<li class="list-inline-item">
									<form class="form-inline mb-0">
										<xsl:attribute name="action">
											<xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" />
										</xsl:attribute>
										<input type="hidden" name="pos">
											<xsl:attribute name="value"><xsl:value-of select="number(@pos)" /></xsl:attribute>
										</input>
										<input type="hidden" name="filter">
											<xsl:attribute name="value"><xsl:value-of select="@filter" /></xsl:attribute>
										</input>
										<input type="hidden" name="SortBy">
											<xsl:attribute name="value"><xsl:value-of select="@sortby" /></xsl:attribute>
										</input>
										<input type="hidden" name="refresh" value="true" />
										<input type="submit" class="btn btn-outline-secondary" value="Refresh Queue" />
									</form>
								</li>
							</ul>
						</div>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>

	<!-- the tables column title -->
	<xsl:template name="columnTitle">
		<xsl:param name="display" />
		<xsl:param name="attName" />
		<a>
			<xsl:attribute name="href">
				<xsl:value-of select="@Context" />/showQueue/<xsl:value-of select="@DeviceID" />?SortBy=<xsl:value-of select="$attName" /><xsl:if test="@filter">&amp;filter=<xsl:value-of select="@filter" /></xsl:if>
			</xsl:attribute>
			<xsl:value-of select="$display" />
		</a>
	</xsl:template>

	<xsl:include href="modules/formatting.module.xsl" />
	<xsl:include href="modules/option-list.module.xsl" />
	<xsl:include href="modules/head-content.module.xsl" />
</xsl:stylesheet>
