<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:strip-space elements="*"/>
  <xsl:output method="html"/>
  <xsl:template match="/XMLDevice">
    <html>
      <xsl:variable name="deviceID" select="@DeviceID"/>
      <xsl:variable name="deviceType" select="@DeviceType"/>
      <xsl:variable name="deviceURL" select="@DeviceURL"/>
      <xsl:variable name="deviceStatus" select="@DeviceStatus"/>
      <xsl:variable name="context" select="@Context"/>
      <xsl:variable name="modify" select="@modify"/>
      <head>
        <link rel="stylesheet" type="text/css">
          <xsl:attribute name="href"><xsl:value-of select="$context"/>/css/styles_pc.css</xsl:attribute>
        </link>
        <link rel="icon" type="image/x-icon">
          <xsl:attribute name="href"><xsl:value-of select="$context"/>/favicon.ico</xsl:attribute>
        </link>
        <title>
          <xsl:value-of select="$deviceType"/>
          Console Device :
          <xsl:value-of select="$deviceID"/>
        </title>
        <xsl:if test="@refresh='true'">
          <meta http-equiv="refresh">
            <xsl:attribute name="content">15; URL=<xsl:value-of select="$context"/>/showDevice/<xsl:value-of select="$deviceID"/>?refresh=true</xsl:attribute>
          </meta>
        </xsl:if>
      </head>

      <!-- Body only  -->
      <body>
        <img height="70" alt="logo">
          <xsl:attribute name="src"><xsl:value-of select="$context"/>/logo.gif</xsl:attribute>
        </img>
        <h1>
          <xsl:value-of select="$deviceType"/>
          - Device :
          <xsl:value-of select="$deviceID"/>
        </h1>
        <p align="center">
          <table>
            <tr valign="bottom">
              <xsl:choose>
                <xsl:when test="@refresh='true'">
                  <a>
                    <xsl:attribute name="href"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of select="$deviceID"/>?refresh=false</xsl:attribute>
                    modify page
                  </a>
                </xsl:when>
                <xsl:otherwise>
                  <td>
                    <a>
                      <xsl:attribute name="href"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of select="$deviceID"/>?refresh=false</xsl:attribute>
                      reload once
                    </a>
                  </td>
                  <td width="15"/>
                  <td>
                    <a>
                      <xsl:attribute name="href"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of select="$deviceID"/>?refresh=true</xsl:attribute>
                      reload continually
                    </a>
                  </td>
                </xsl:otherwise>
              </xsl:choose>
              <td>
                <img height="70" hspace="10" alt="logo">
                  <xsl:attribute name="src"><xsl:value-of select="$context"/>/logo.gif</xsl:attribute>
                </img>
              </td>
              <td>
                Go to
                <a>
                  <xsl:attribute name="href"><xsl:value-of select="$context"/>/overview</xsl:attribute>
                  DeviceList
                </a>
              </td>
            </tr>
          </table>
        </p>

        <!--  device info section   -->
        <hr/>
        <h3>Device</h3>
        <table>
          <tr>
            <td>
              <form style="margin-left: 20px">
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="refresh" value="false"/>
                <input type="submit" value="refresh page"/>
              </form>
            </td>
            <td>
              <form style="margin-left: 20px">
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="submit" value="show queue"/>
              </form>
            </td>
            <td>
              <form style="margin-left: 20px">
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showSubscriptions/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="submit" value="show subscriptions"/>
              </form>
            </td>
            <td>
              <form style="margin-left: 20px">
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="setup" value="true"/>
                <input type="submit" value="device setup"/>
              </form>
            </td>
           <xsl:if test="@login='true'">
              <td>
                <form style="margin-left: 20px">
                  <xsl:attribute name="action"><xsl:value-of select="$context"/>/login/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                  <input type="submit" value="login"/>
                 </form>
              </td>
            </xsl:if>
          </tr>
        </table>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:include href="processor.xsl"/>
 <xsl:template match="jdf:Employee">
  <!--  nop here -->
  </xsl:template>


 <xsl:template match="KnownEmployees">
  <!--  nop her -->
  </xsl:template>

  <!-- add more templates -->
  <!-- the catchall -->
  <xsl:template match="*">
    <h3>
      Unhandled element:
      <xsl:value-of select="name()"/>
    </h3>
    <xsl:apply-templates/>
  </xsl:template>
</xsl:stylesheet>