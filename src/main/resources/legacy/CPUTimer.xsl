<!-- (C) 2001-2014 CIP4 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1"
	xmlns:bambi="www.cip4.org/Bambi">

	<xsl:template name="cputimer">
		<table border="4" cellpadding="10">
			<tr>
				<td>
					<table>
						<font size="-2">
							<tr>
								<td bgcolor="#dddddd">CPU Timer</td>
							</tr>
							<tr>
								<td bgcolor="#dddddd">Stats</td>
							</tr>
							<tr>
								<th bgcolor="#dddddd">type</th>
							</tr>
							<tr>
								<td bgcolor="#dddddd">Real Time (sec.)</td>
							</tr>
							<tr>
								<td bgcolor="#dddddd">CPU Time (sec.)</td>
							</tr>
						</font>
					</table>
				</td>
				<xsl:for-each select="//CPUTimer">
					<td>
						<xsl:call-template name="internalcputimer" />
					</td>
				</xsl:for-each>
			</tr>
		</table>
	</xsl:template>


	<xsl:template name="internalcputimer">
		<table>
			<font size="-2">
				<tr>
					<td colspan="3" align="center">
						<xsl:value-of select="@Name" />
					</td>
				</tr>
				<tr>
					<td colspan="2" align="left">
						Start:
						<xsl:value-of select="@CreationTime" />
					</td>
					<td align="left">
						Invocations:
						<xsl:value-of select="@StartStop" />
					</td>
				</tr>
				<tr>
					<th>Current </th>
					<th align="left">Total</th>
					<th align="left">Average</th>
				</tr>
				<tr>
					<td align="left">
						<xsl:value-of select="@CurrentRealTime" />
					</td>
					<td align="left">
						<xsl:value-of select="@TotalRealTime" />
					</td>
					<td align="left">
						<xsl:value-of select="@AverageRealTime" />
					</td>
				</tr>
				<tr>
					<td align="left">
						<xsl:value-of select="@CurrentCPUTime" />
					</td>
					<td align="left">
						<xsl:value-of select="@TotalCPUTime" />
					</td>
					<td align="left">
						<xsl:value-of select="@AverageCPUTime" />
					</td>
				</tr>
			</font>
		</table>

	</xsl:template>

	<xsl:template match="CPUTimer" />
</xsl:stylesheet>