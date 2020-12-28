<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi">
	<xsl:template name="cpu-timer">
		<div class="card-columns">
			<xsl:for-each select="//CPUTimer">
				<div class="card">
					<div class="card-body">
						<h4 class="card-title"><xsl:value-of select="@Name" /></h4>
						<table class="table table-borderless table-sm table-hover small">
							<tbody>
								<tr>
									<th>Start:</th>
									<td><xsl:value-of select="@CreationTime" /></td>
								</tr>
								<tr>
									<th>Innvocations:</th>
									<td><xsl:value-of select="@StartStop" /></td>
								</tr>
								<tr>
									<th>Real Time (sec.):</th>
									<td>
										<a href="#" data-toggle="tooltip" title="Current"><xsl:value-of select="format-number(@CurrentRealTime, '#0.00')" /></a> /
										<a href="#" data-toggle="tooltip" title="Total"><xsl:value-of select="format-number(@TotalRealTime, '#0.00')" /></a> /
										<a href="#" data-toggle="tooltip" title="Average"><xsl:value-of select="format-number(@AverageRealTime, '#0.00')" /></a>
									</td>
								</tr>
								<tr>
									<th>CPU Time (sec.):</th>
									<td>
										<a href="#" data-toggle="tooltip" title="Current"><xsl:value-of select="format-number(@CurrentCPUTime, '#0.00')" /></a> /
										<a href="#" data-toggle="tooltip" title="Total"><xsl:value-of select="format-number(@TotalCPUTime, '#0.00')" /></a> /
										<a href="#" data-toggle="tooltip" title="Average"><xsl:value-of select="format-number(@AverageCPUTime, '#0.00')" /></a>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</xsl:for-each>
		</div>
	</xsl:template>
</xsl:stylesheet>
