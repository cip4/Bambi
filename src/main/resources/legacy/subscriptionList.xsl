<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:bambi="www.cip4.org/Bambi" xmlns:xjdf="http://www.CIP4.org/JDFSchema_2_0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:strip-space elements="*" />
	<xsl:output method="html" />
	<xsl:template match="/SubscriptionList">
		<xsl:variable name="context" select="@Context" />
		<xsl:variable name="deviceID" select="@DeviceID" />
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
									<a><xsl:attribute name="href"><xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" /></xsl:attribute>Device <xsl:value-of select="@DeviceID" /></a>
								</li>
								<li>
									Subscriptions
								</li>
							</ul>
						</div>
					</div>

					<!-- device title -->
					<div class="row pt-2">
						<div class="col-12">
							<h1>Subscriptions Device <xsl:value-of select="$deviceID" /></h1>
							<p>Visualization and management of the devices subscriptions.</p>
						</div>
					</div>

					<!-- message subscriptions -->
					<div class="row mt-4">
						<div class="col-12">
							<h2>Message Subscriptions</h2>
							<p>Number of Message Subscriptions: <xsl:value-of select="count(MsgSubscription)" /></p>

							<table class="table">
								<thead class="thead-light">
									<tr>
										<th>Channel ID</th>
										<th>Signal Type</th>
										<th>Subscription Details</th>
										<th></th>
									</tr>
								</thead>
								<tbody>
									<xsl:for-each select="MsgSubscription">
										<tr>
											<xsl:choose>
												<xsl:when test="@LastTime=' - '">
													<xsl:choose>
														<xsl:when test="@Sent='0'">
															<xsl:attribute name="class">table-danger</xsl:attribute>
														</xsl:when>
														<xsl:otherwise>
															<xsl:attribute name="class">table-warning</xsl:attribute>
														</xsl:otherwise>
													</xsl:choose>
												</xsl:when>
												<xsl:otherwise>
													<xsl:attribute name="class">table-success</xsl:attribute>
												</xsl:otherwise>
											</xsl:choose>
											<td>
												<a>
													<xsl:attribute name="href">
														<xsl:value-of select="../@Context" />/showSubscriptions/<xsl:value-of select="../@DeviceID" />?DetailID=<xsl:value-of select="@ChannelID" />
													</xsl:attribute>
													<xsl:value-of select="@ChannelID" />
												</a>

												<table class="table table-borderless table-sm small mt-2">
													<tbody>
														<tr>
															<th class="pl-0">Device ID:</th>
															<td><xsl:value-of select="@DeviceID" /></td>
														</tr>
														<tr>
															<th class="pl-0">QueueEntry ID:</th>
															<td><xsl:value-of select="@QueueEntryID" /></td>
														</tr>
													</tbody>
												</table>
											</td>
											<td>
												<xsl:value-of select="@Type" />
											</td>
											<td>
												<table class="table table-borderless table-sm table-hover small">
													<tbody>
														<tr>
															<th>Subscription Url:</th>
															<td><xsl:value-of select="@URL" /></td>
														</tr>
														<tr>
															<th>Repeat-Time:</th>
															<td><xsl:value-of select="@RepeatTime" /></td>
														</tr>
														<tr>
															<th>Repeat-Step:</th>
															<td><xsl:value-of select="@RepeatStep" /></td>
														</tr>
														<tr>
															<th>Messages Queued:</th>
															<td><xsl:value-of select="@Sent" /></td>
														</tr>
														<tr>
															<th>Last time Queued:</th>
															<td><xsl:value-of select="@LastTime" /></td>
														</tr>
													</tbody>
												</table>
											</td>
											<td class="text-right">
												<form class="mb-0">
													<xsl:attribute name="action">
														<xsl:value-of select="../@Context" />/showSubscriptions/<xsl:value-of select="../@DeviceID" />
													</xsl:attribute>
													<input type="hidden" name="StopChannel" value="true" />
													<input type="hidden" name="ChannelID">
														<xsl:attribute name="value"><xsl:value-of select="@ChannelID" /></xsl:attribute>
													</input>
													<input type="submit" class="btn btn-outline-danger" value="Remove" />
												</form>
											</td>
										</tr>
									</xsl:for-each>
								</tbody>
							</table>
							<xsl:if test="not(MsgSubscription)">
								<p><i>No subscriptions are set up for this device.</i></p>
							</xsl:if>
						</div>
					</div>

					<!-- message sender channels -->
					<div class="row mt-4">
						<div class="col-12">
							<h2>Message Sender Channels</h2>
							<p>List of Message Sender Channels.</p>

							<table class="table">
								<thead class="thead-light">
									<tr>
										<th>Base Url</th>
										<th>JMF Metrics</th>
										<th>Time Metrics</th>
										<th></th>
									</tr>
								</thead>
								<tbody>
									<xsl:for-each select="MessageSender">
										<tr>
											<xsl:choose>
												<xsl:when test="@Size!='0'">
													<xsl:choose>
														<xsl:when test="@pause='true'">
															<xsl:attribute name="class">table-warning</xsl:attribute>
														</xsl:when>
														<xsl:when test="@Active='true'">
															<xsl:attribute name="class">table-warning</xsl:attribute>
														</xsl:when>
														<xsl:otherwise>
															<xsl:attribute name="class">table-danger</xsl:attribute>
														</xsl:otherwise>
													</xsl:choose>
												</xsl:when>
												<xsl:when test="@pause='true'">
													<xsl:attribute name="class">table-warning</xsl:attribute>
												</xsl:when>
												<xsl:when test="@Active='true'">
													<xsl:choose>
														<xsl:when test="@Problems='true'">
															<xsl:attribute name="class">table-danger</xsl:attribute>
														</xsl:when>
														<xsl:otherwise>
															<xsl:attribute name="class">table-success</xsl:attribute>
														</xsl:otherwise>
													</xsl:choose>
												</xsl:when>
												<xsl:otherwise>
													<xsl:attribute name="class">table-success</xsl:attribute>
												</xsl:otherwise>
											</xsl:choose>
											<td>
												<p>
													<xsl:value-of select="@URL" />
												</p>
												<p>
													<b>Status:</b>
													<xsl:choose>
														<xsl:when test="@Active='false'">
															Down
														</xsl:when>
														<xsl:when test="@pause='true'">
															Paused
														</xsl:when>
														<xsl:when test="@Size!='0'">
															<xsl:choose>
																<xsl:when test="@idle!='0'">
																	Dispatch Errors
																</xsl:when>
																<xsl:otherwise>
																	Processing Backlog
																</xsl:otherwise>
															</xsl:choose>
														</xsl:when>
														<xsl:otherwise>
															Active
														</xsl:otherwise>
													</xsl:choose>
												</p>
											</td>
											<td>
												<table class="table table-borderless table-sm table-hover small">
													<tbody>
														<tr>
															<th>JMF Pending:</th>
															<td><xsl:value-of select="@Size" /></td>
														</tr>
														<tr>
															<th>JMF Sent:</th>
															<td><xsl:value-of select="@NumSent" /></td>
														</tr>
														<tr>
															<th>JMF Queued:</th>
															<td><xsl:value-of select="@NumTry" /></td>
														</tr>
														<tr>
															<th>JMF Removed:</th>
															<td><xsl:value-of select="@NumRemoveJMF" /></td>
														</tr>
														<tr>
															<th>Fire &amp; Forget Removed:</th>
															<td><xsl:value-of select="@NumRemoveFireForget" /></td>
														</tr>
														<tr>
															<th>JMF Errors Removed:</th>
															<td><xsl:value-of select="@NumRemoveError" /></td>
														</tr>
													</tbody>
												</table>
											</td>
											<td>
												<table class="table table-borderless table-sm table-hover small">
													<tbody>
														<tr>
															<th>Average Real Time:</th>
															<td><xsl:value-of select="CPUTimer/@AverageRealTime" /></td>
														</tr>
														<tr>
															<th>Total Real Time:</th>
															<td><xsl:value-of select="CPUTimer/@TotalRealTime" /></td>
														</tr>
														<tr>
															<th>Last Time Sent:</th>
															<td><xsl:value-of select="@LastSent" /></td>
														</tr>
														<tr>
															<th>Last Time Queued:</th>
															<td><xsl:value-of select="@LastQueued" /></td>
														</tr>
														<tr>
															<th>Active Since:</th>
															<td><xsl:value-of select="@CreationDate" /></td>
														</tr>
													</tbody>
												</table>
											</td>
											<td>
												<p class="small font-weight-bold mb-2">Messages:</p>
												<ul class="list-inline">
													<li class="list-inline-item">
														<form class="mb-0">
															<xsl:attribute name="action">
																<xsl:value-of select="../@Context" />/showSubscriptions/<xsl:value-of select="../@DeviceID" />
															</xsl:attribute>
															<input type="hidden" name="ListSenders" value="true" />
															<input type="hidden" name="URL">
																<xsl:attribute name="value"><xsl:value-of select="@URL" /></xsl:attribute>
															</input>
															<input type="submit" class="btn btn-outline-secondary" value="List" />
														</form>
													</li>
													<li class="list-inline-item">
														<form class="mb-0">
															<xsl:attribute name="action">
																<xsl:value-of select="../@Context" />/showSubscriptions/<xsl:value-of select="../@DeviceID" />
															</xsl:attribute>
															<input type="hidden" name="URL">
																<xsl:attribute name="value"><xsl:value-of select="@URL" /></xsl:attribute>
															</input>
															<input type="hidden" name="ZappFirst" value="true" />
															<input type="submit" class="btn btn-outline-danger" value="Remove First" />
														</form>
													</li>
													<li class="list-inline-item">
														<form class="mb-0">
															<xsl:attribute name="action">
																<xsl:value-of select="../@Context" />/showSubscriptions/<xsl:value-of select="../@DeviceID" />
															</xsl:attribute>
															<input type="hidden" name="FlushSender" value="true" />
															<input type="hidden" name="URL">
																<xsl:attribute name="value"><xsl:value-of select="@URL" /></xsl:attribute>
															</input>
															<input type="submit" class="btn btn-outline-danger" value="Flush" />
														</form>
													</li>
												</ul>

												<p class="small font-weight-bold mb-2">Sender:</p>
												<ul class="list-inline">
													<li class="list-inline-item">
														<form class="mb-0">
															<xsl:attribute name="action">
																<xsl:value-of select="../@Context" />/showSubscriptions/<xsl:value-of select="../@DeviceID" />
															</xsl:attribute>
															<input type="hidden" name="URL">
																<xsl:attribute name="value"><xsl:value-of select="@URL" /></xsl:attribute>
															</input>
															<xsl:choose>
																<xsl:when test="@pause='true'">
																	<input type="hidden" name="pause" value="false" />
																	<input type="submit" class="btn btn-outline-secondary" value="Resume" />
																</xsl:when>
																<xsl:otherwise>
																	<input type="hidden" name="pause" value="true" />
																	<input type="submit" class="btn btn-outline-secondary" value="Pause" />
																</xsl:otherwise>
															</xsl:choose>
														</form>
													</li>
													<li class="list-inline-item">
														<form class="mb-0">
															<xsl:attribute name="action">
																<xsl:value-of select="../@Context" />/showSubscriptions/<xsl:value-of select="../@DeviceID" />
															</xsl:attribute>
															<input type="hidden" name="StopSender" value="true" />
															<input type="hidden" name="URL">
																<xsl:attribute name="value"><xsl:value-of select="@URL" /></xsl:attribute>
															</input>
															<input type="submit" class="btn btn-outline-danger" value="Remove" />
														</form>
													</li>
												</ul>
											</td>
										</tr>
									</xsl:for-each>
								</tbody>
							</table>
						</div>
					</div>

					<xsl:if test="MessageSender/Message">
						<!-- metrics -->
						<div class="row mt-4">
							<div class="col-12">
								<h2>Queued Messages</h2>
								<table class="table table-hover table-sm">
									<thead class="thead-light">
										<tr>
											<th>#</th>
											<th>Sent Time</th>
											<th>Processing Status</th>
											<th>Full URL</th>
											<th></th>
										</tr>
									</thead>
									<tbody>
										<xsl:for-each select="MessageSender/Message">
											<tr>
												<xsl:choose>
													<!-- something is waiting -->
													<xsl:when test="@Return='sent'">
														<xsl:attribute name="class">table-success</xsl:attribute>
													</xsl:when>
													<xsl:when test="@Return='error'">
														<xsl:attribute name="class">table-danger</xsl:attribute>
													</xsl:when>
													<xsl:when test="@Return='removed'">
														<xsl:attribute name="class">table-danger</xsl:attribute>
													</xsl:when>
												</xsl:choose>
												<td class="pt-2">
													<xsl:value-of select="position()" />
												</td>
												<td class="pt-2">
													<xsl:choose>
														<xsl:when test="xjdf:JMF/@TimeStamp">
															<xsl:value-of select="xjdf:JMF/@TimeStamp" />
														</xsl:when>
														<xsl:otherwise>
															<xsl:value-of select="@TimeStamp" />
														</xsl:otherwise>
													</xsl:choose>
												</td>
												<td class="pt-2">
													<xsl:value-of select="@Return" />
												</td>
												<td class="pt-2">
													<xsl:value-of select="@URL" />
												</td>
												<td class="pt-1">
													<form class="form-inline m-0 float-right">
														<xsl:attribute name="action">
															<xsl:value-of select="/SubscriptionList/@Context" />/showSubscriptions/<xsl:value-of select="/SubscriptionList/@DeviceID" />
														</xsl:attribute>
														<input type="hidden" name="ListSenders" value="true" />
														<input type="hidden" name="URL">
															<xsl:attribute name="value"><xsl:value-of select="@URL" /></xsl:attribute>
														</input>
														<input type="hidden" name="pos">
															<xsl:attribute name="value"><xsl:value-of select="position()" /></xsl:attribute>
														</input>
														<input type="submit" class="btn btn-outline-secondary btn-sm" value="Show Details" />
													</form>
												</td>
											</tr>
										</xsl:for-each>
									</tbody>
								</table>
							</div>
						</div>

						<!-- metrics -->
						<div class="row mt-4">
							<div class="col-12">
								<h2>Metrics</h2>
								<xsl:call-template name="cpu-timer" />
							</div>
						</div>
					</xsl:if>
				</div>
			</body>
		</html>
	</xsl:template>

	<xsl:include href="modules/head-content.module.xsl" />
	<xsl:include href="modules/cpu-timer.module.xsl" />

</xsl:stylesheet>