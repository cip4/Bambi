<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template name="dateTime">
        <xsl:param name="val" />
        <xsl:if test="$val">
            <xsl:variable name="date" select="concat(substring($val,9,2),'/',substring($val,6,2),'/',substring($val,1,4))" />
            <xsl:value-of select="$date" />
            /
            <xsl:variable name="time" select="substring($val,12,8)" />
            <xsl:value-of select="$time" />
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>