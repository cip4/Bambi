<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi">
	<xsl:template name="cputimer">
		<div class="card-columns">
			<xsl:for-each select="//CPUTimer">
				<div class="card">
					<div class="card-body">
						<h4 class="card-title"><xsl:value-of select="@Name" /></h4>

						<small>
							<p><b>Start: </b> <xsl:value-of select="@CreationTime" /></p>
							<p><b>Innvocations: </b> <xsl:value-of select="@StartStop" /></p>

							<p>
								<b>Real Time (sec.): </b>
								<a href="#" data-toggle="tooltip" title="Current"><xsl:value-of select="@CurrentRealTime" /></a> /
								<a href="#" data-toggle="tooltip" title="Total"><xsl:value-of select="@TotalRealTime" /></a> /
								<a href="#" data-toggle="tooltip" title="Average"><xsl:value-of select="@AverageRealTime" /></a>
							</p>
							<p><b>CPU Time (sec.): </b>
								<a href="#" data-toggle="tooltip" title="Current"><xsl:value-of select="@CurrentCPUTime" /></a> /
								<a href="#" data-toggle="tooltip" title="Total"><xsl:value-of select="@TotalCPUTime" /></a> /
								<a href="#" data-toggle="tooltip" title="Average"><xsl:value-of select="@AverageCPUTime" /></a>
							</p>
						</small>
					</div>
				</div>
			</xsl:for-each>
		</div>
	</xsl:template>
</xsl:stylesheet>