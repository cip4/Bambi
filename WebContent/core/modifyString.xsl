<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<!-- simple dispay / form swapper -->
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template name="modifyString">
    <xsl:param name="attLabel"/>
    <xsl:param name="attName"/>
    <xsl:param name="attVal"/>
    <xsl:param name="modify"/>
    <tr>
      <td>
        <b>
          <xsl:value-of select="$attLabel"/>
        </b>
      </td>
      <xsl:choose>
        <xsl:when test="$modify='true'">
          <td>
            <input type="text" size="80" maxLength="255">
              <xsl:attribute name="value"><xsl:value-of select="$attVal"/></xsl:attribute>
              <xsl:attribute name="name"><xsl:value-of select="$attName"/></xsl:attribute>
            </input>
          </td>
        </xsl:when>

        <xsl:otherwise>
          <td>
            <xsl:value-of select="$attVal"/>
          </td>
        </xsl:otherwise>
      </xsl:choose>
    </tr>
  </xsl:template>

</xsl:stylesheet>