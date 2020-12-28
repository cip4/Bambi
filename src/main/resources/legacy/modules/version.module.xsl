<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi">
    <xsl:template name="version">
        <small><b>Bambi <xsl:value-of select="@ReleaseVersionString" /></b> build <xsl:value-of select="@ReleaseBuildNumberString" /> (<xsl:value-of select="@ReleaseTimestampString" />) <i>based on JDFLibJ <xsl:value-of select="@JdfLibVersion" /></i></small>
    </xsl:template>
</xsl:stylesheet>