<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:bambi="www.cip4.org/Bambi" >
<!--  Option Box -->
<xsl:template match="bambi:OptionList">
<select size="1"> 
<xsl:attribute name="name">
<xsl:value-of select="@name"/>
</xsl:attribute>
<xsl:apply-templates/>
</select>
</xsl:template>

<xsl:template match="bambi:Option">
<option>
<xsl:if test="@selected='selected'">
<xsl:attribute name="selected">
<xsl:value-of select="@selected"/>
</xsl:attribute>
</xsl:if>
<xsl:value-of select="@name"/>
</option> 
<xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet>