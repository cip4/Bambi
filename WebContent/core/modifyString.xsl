<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
  <!-- simple dispay / form swapper -->
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template name="modifyString">
    <xsl:param name="attLabel"/>
    <xsl:param name="attName"/>
    <xsl:param name="attVal"/>
    <xsl:param name="modify"/>
    <xsl:param name="desc" select="''"/>
    <tr>
      <td>
        <b>
          <xsl:value-of select="$attLabel"/>
        </b>
      </td>
      <td>
        <xsl:choose>
          <xsl:when test="$modify='true'">
            <input type="text" size="80" maxLength="255">
              <xsl:attribute name="value"><xsl:value-of select="$attVal"/></xsl:attribute>
              <xsl:attribute name="name"><xsl:value-of select="$attName"/></xsl:attribute>
            </input>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$attVal"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td>
          <xsl:if test="$desc">
           <xsl:value-of select="$desc"/>
          </xsl:if>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>