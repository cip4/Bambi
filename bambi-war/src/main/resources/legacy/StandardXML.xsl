<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <!--   ///////////////////////////////////////////////// -->
  <xsl:template name="default">
    <xsl:param name="pre"/>
    <xsl:param name="printme" select="'y'"/>
    <xsl:if test="$printme">
      <h4>
        <xsl:if test="$pre">
          <xsl:value-of select="$pre"/>
          /
        </xsl:if>
        <xsl:value-of select="name()"/>
      </h4>
    </xsl:if>
    <xsl:variable name="pre2">
      <xsl:if test="$printme">
        <xsl:if test="$pre">
          <xsl:value-of select="$pre"/>
          /
        </xsl:if>
        <xsl:value-of select="name()"/>
      </xsl:if>
    </xsl:variable>
    <xsl:call-template name="printAttributelines">
      <xsl:with-param name="prefix" select="$pre2"/>
    </xsl:call-template>
    <xsl:apply-templates>
      <xsl:with-param name="pre" select="$pre2"/>
    </xsl:apply-templates>
  </xsl:template>
  <!--   ///////////////////////////////////////////////// -->

  <xsl:template name="defaultshort">
    <xsl:param name="pre"/>
    <xsl:param name="printme" select="'y'"/>
    <xsl:if test="$printme">
      <h4>
        <xsl:if test="$pre">
          <xsl:value-of select="$pre"/>
          /
        </xsl:if>
        <xsl:value-of select="name()"/>
      </h4>
    </xsl:if>
    <xsl:variable name="pre2">
      <xsl:if test="$printme">
        <xsl:if test="$pre">
          <xsl:value-of select="$pre"/>
          /
        </xsl:if>
        <xsl:value-of select="name()"/>
      </xsl:if>
    </xsl:variable>
    <xsl:call-template name="printAttributes">
      <xsl:with-param name="prefix" select="$pre2"/>
    </xsl:call-template>
    <xsl:apply-templates>
      <xsl:with-param name="pre" select="$pre2"/>
    </xsl:apply-templates>
  </xsl:template>
  <!--   ///////////////////////////////////////////////// -->


  <xsl:template name="printAttributelines">
    <xsl:param name="prefix" select="''"/>
    <xsl:param name="x1" select="''"/>
    <xsl:param name="x2" select="''"/>
    <xsl:param name="x3" select="''"/>
    <xsl:param name="x4" select="''"/>
    <xsl:param name="x5" select="''"/>
    <xsl:param name="x6" select="''"/>
    <xsl:param name="x7" select="''"/>
    <xsl:param name="x8" select="''"/>

    <table Border="0" cellspacing="0">
      <xsl:for-each select="@*">
        <xsl:choose>
          <xsl:when test="$x1 = name()"/>
          <xsl:when test="$x2 = name()"/>
          <xsl:when test="$x3 = name()"/>
          <xsl:when test="$x4 = name()"/>
          <xsl:when test="$x5 = name()"/>
          <xsl:when test="$x6 = name()"/>
          <xsl:when test="$x7 = name()"/>
          <xsl:when test="$x8 = name()"/>
          <xsl:when test=". = ''"/>
          <xsl:when test="'ID' = name()"/>
          <xsl:when test="string-length(name())>3 and string-length(name()) = 3 + string-length(substring-before(name(),'Ref'))">
            <tr valign="top">
              <xsl:call-template name="printRefs"/>
            </tr>
          </xsl:when>
          <xsl:when test="string-length(name())>4 and string-length(name()) = 4 + string-length(substring-before(name(),'Refs'))">
            <tr valign="top">
              <xsl:call-template name="printRefs"/>
            </tr>
          </xsl:when>
          <xsl:otherwise>
            <tr valign="top">
              <td nowrap="true">
                <xsl:if test="normalize-space($prefix) != ''">
                  <xsl:value-of select="$prefix"/>
                  /
                </xsl:if>
                <xsl:value-of select="name()"/>
              </td>
              <td>=</td>
              <td nowrap="true">
                <xsl:value-of select="."/>
              </td>
            </tr>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </table>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <xsl:template name="printAttributes">
    <xsl:param name="prefix" select="''"/>
    <xsl:param name="x1" select="''"/>
    <xsl:param name="x2" select="''"/>
    <xsl:param name="x3" select="''"/>
    <xsl:param name="x4" select="''"/>
    <xsl:param name="x5" select="''"/>
    <xsl:param name="x6" select="''"/>
    <xsl:param name="x7" select="''"/>
    <xsl:param name="x8" select="''"/>
    <table Border="0" cellspacing="0">
      <tr>
        <xsl:if test="normalize-space($prefix) != ''">
          <td nowrap="true">
            <b>
              <xsl:value-of select="$prefix"/>
            </b>
          </td>
        </xsl:if>

        <xsl:for-each select="@*">
          <xsl:choose>
            <xsl:when test="$x1 = name()"/>
            <xsl:when test="$x2 = name()"/>
            <xsl:when test="$x3 = name()"/>
            <xsl:when test="$x4 = name()"/>
            <xsl:when test="$x5 = name()"/>
            <xsl:when test="$x6 = name()"/>
            <xsl:when test="$x7 = name()"/>
            <xsl:when test="$x8 = name()"/>
            <xsl:when test=". = ''"/>
            <xsl:when test="'ID' = name()"/>
            <xsl:otherwise>
              <td nowrap="true">
                <em>
                  <xsl:value-of select="name()"/>
                </em>
              </td>
              <td>
                =
                <xsl:value-of select="."/>
              </td>
              <td width="2%"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </tr>
    </table>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->
  <xsl:template name="printRefs">
    <xsl:param name="val" select="."/>
    <xsl:param name="n" select="''"/>
    <xsl:if test="not(. = $val)">
      <td nowrap="true">
        <xsl:value-of select="substring-before(name(),'Ref')"/>
      </td>
      <td nowrap="true">
        =
	    </td>
    </xsl:if>

    <td nowrap="true" width="80">
      <a>
        <xsl:choose>
          <xsl:when test="string-length(substring-before($val,' '))=0">
            <xsl:attribute name="href">#<xsl:value-of select="$val"/></xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="href">#<xsl:value-of select="substring-before($val,' ')"/></xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:value-of select="substring-before(name(),'Ref')"/>
        :
        <xsl:value-of select="$val"/>
      </a>
    </td>
    <!-- remove string up to blank and recurse with remaining right string -->
    <xsl:if test="string-length(substring-after($val,' ')) != 0">
      <xsl:call-template name="printRefs">
        <xsl:with-param name="val">
          <xsl:value-of select="substring-after($val,' ')"/>
        </xsl:with-param>
        <xsl:with-param name="n">
          <xsl:value-of select="$n + 1"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="dateTime">
    <xsl:param name="val"/>
    <xsl:if test="$val">
      <xsl:variable name="date" select="concat(substring($val,6,2),'/',substring($val,9,2),'/',substring($val,1,4))"/>
      <xsl:variable name="time" select="substring($val,12,8)"/>
      <xsl:value-of select="$date"/>
      /
      <xsl:value-of select="$time"/>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>