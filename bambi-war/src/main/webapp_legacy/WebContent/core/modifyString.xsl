<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<!-- simple dispay / form swapper -->
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml"
	xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template name="modifyString">
		<xsl:param name="attLabel" />
		<xsl:param name="attName" />
		<xsl:param name="attVal" />
		<xsl:param name="modify" />
		<xsl:param name="desc" select="''" />
		<tr>
			<td>
				<b>
					<xsl:value-of select="$attLabel" />
				</b>
			</td>
			<td>
				<xsl:choose>
					<xsl:when test="$modify='true'">
						<input type="text" size="80" maxLength="255">
							<xsl:attribute name="value"><xsl:value-of
								select="$attVal" /></xsl:attribute>
							<xsl:attribute name="name"><xsl:value-of
								select="$attName" /></xsl:attribute>
							<xsl:if test="$attName='Dump'">
								<xsl:attribute name="type">checkbox</xsl:attribute>
								<xsl:if test="$attVal='true'">
									<xsl:attribute name="checked">true</xsl:attribute>
								</xsl:if>
							</xsl:if>
						</input>
						<xsl:if test="$attName='Dump'">
							<input type="hidden" name="UpdateDump" value="true" />
						</xsl:if>
					</xsl:when>
					<xsl:when test="contains($attName,'URL')">
						<a>
							<xsl:attribute name="href"><xsl:value-of
								select="$attVal" /></xsl:attribute>
							<xsl:value-of select="$attVal" />
						</a>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$attVal" />
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td>
				<xsl:if test="$desc">
					<xsl:value-of select="$desc" />
				</xsl:if>
			</td>
		</tr>
	</xsl:template>

</xsl:stylesheet>