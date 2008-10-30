<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:bambi="www.cip4.org/Bambi"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:strip-space elements="*"/>
  <xsl:output method="html"/>
  <xsl:template match="/XMLDevice">
    <html>
      <xsl:variable name="deviceID" select="@DeviceID"/>
      <xsl:variable name="deviceType" select="@DeviceType"/>
      <xsl:variable name="deviceURL" select="@DeviceURL"/>
      <xsl:variable name="deviceStatus" select="@DeviceStatus"/>
      <xsl:variable name="context" select="@Context"/>
      <head>
        <link rel="stylesheet" type="text/css">
          <xsl:attribute name="href"><xsl:value-of select="$context"/>/css/styles_pc.css</xsl:attribute>
        </link>
        <link rel="icon" type="image/x-icon">
          <xsl:attribute name="href"><xsl:value-of select="$context"/>/favicon.ico</xsl:attribute>
        </link>
        <title>
          <xsl:value-of select="$deviceType"/>
          Simulation Device :
          <xsl:value-of select="$deviceID"/>
        </title>
        <xsl:if test="@refresh='true'">
          <meta http-equiv="refresh">
            <xsl:attribute name="content">15; URL=<xsl:value-of select="$context"/>/showDevice/<xsl:value-of
              select="$deviceID"/>?refresh=true</xsl:attribute>
          </meta>
        </xsl:if>
      </head>
      <body>
        <img height="70" alt="logo">
          <xsl:attribute name="src"><xsl:value-of select="$context"/>/logo.gif</xsl:attribute>
        </img>
        <h1>
          <xsl:value-of select="$deviceType"/>
          - Simulation Device :
          <xsl:value-of select="$deviceID"/>
        </h1>
        <p align="center">
          <table>
            <tr valign="bottom">
              <xsl:choose>
                <xsl:when test="@refresh='true'">
                  <a>
                    <xsl:attribute name="href"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of
                      select="$deviceID"/>?refresh=false</xsl:attribute>
                    modify page
                  </a>
                </xsl:when>
                <xsl:otherwise>
                  <td>
                    <a>
                      <xsl:attribute name="href"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of
                        select="$deviceID"/>?refresh=false</xsl:attribute>
                      reload once
                    </a>
                  </td>
                  <td width="15"/>
                  <td>
                    <a>
                      <xsl:attribute name="href"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of
                        select="$deviceID"/>?refresh=true</xsl:attribute>
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
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of
                  select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="refresh" value="false"/>
                <input type="submit" value="refresh page"/>
              </form>
            </td>
            <td>
              <form style="margin-left: 20px">
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of
                  select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="shutdown" value="true"/>
                <input type="submit" value="shutdown"/>
              </form>
            </td>
            <td>
              <form style="margin-left: 20px">
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of
                  select="@DeviceID"/></xsl:attribute>
                <input type="submit" value="restart"/>
                <input type="hidden" name="restart" value="true"/>
              </form>
            </td>
          </tr>
        </table>
        <br/>
        <hr/>
        <br/>
        <div style="margin-left: 20px">
          <b>ID:</b>
          <xsl:value-of select="$deviceID"/>
          <br/>
          <b>Class:</b>
          <xsl:value-of select="$deviceType"/>
          <br/>
          <b>URL:</b>
          <xsl:value-of select="$deviceURL"/>
          <br/>
          <xsl:if test="@bambi:SlaveURL">
            <b>Slave URL:</b>
            <xsl:value-of select="@bambi:SlaveURL"/>
            <br/>
          </xsl:if>
        </div>
        <hr/>
        <!-- call queues and phases -->
        <h3>Queue and Subscriptions</h3>
        <table>
          <tr>
            <td>
              <form style="margin-left: 20px">
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of
                  select="@DeviceID"/></xsl:attribute>
                <input type="submit" value="show queue"/>
              </form>
            </td>
            <td>
              <form style="margin-left: 20px">
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showSubscriptions/<xsl:value-of
                  select="@DeviceID"/></xsl:attribute>
                <input type="submit" value="show subscriptions"/>
              </form>
            </td>
          </tr>
        </table>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>
  <xsl:include href="processor.xsl"/>

  <!--  modifiable phase -->
  <xsl:template match="Phase">
    <br/>
    <h2>Current Job Phase Setup</h2>
    <form style="margin-left: 20px">
      <xsl:attribute name="action">../processNextPhase/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
      Device Status:
      <xsl:apply-templates select="bambi:OptionList[@name='DeviceStatus']"/>
      <br/>
      Device StatusDetails:
      <input name="DeviceStatusDetails" type="text" size="30" maxlength="30">
        <xsl:attribute name="value"><xsl:value-of select="@DeviceStatusDetails"/></xsl:attribute>
      </input>
      <br/>
      Node Status:
      <xsl:apply-templates select="bambi:OptionList[@name='NodeStatus']"/>
      <br/>
      Node StatusDetails:
      <input name="NodeStatusDetails" type="text" size="30" maxlength="30">
        <xsl:attribute name="value"><xsl:value-of select="@NodeStatusDetails"/></xsl:attribute>
      </input>
      <br/>
      Seconds to go:
      <xsl:value-of select="@Duration"/>
      ; new time to go:
      <input name="Duration" type="text" size="30" maxlength="30">
        <xsl:attribute name="value"></xsl:attribute>
      </input>
      <hr/>
      <h3>Resource Simulation Speed Setup</h3>
      <input type="submit" value="update phase"/>
      <xsl:apply-templates select="ResourceAmount"/>
    </form>
  </xsl:template>
  <xsl:include href="optionlist.xsl"/>

  <!-- resource amount setup -->
  <xsl:template match="ResourceAmount">
    <h4>
      <xsl:value-of select="@ResourceName"/>
    </h4>
    <input type="hidden">
      <xsl:attribute name="name">Res<xsl:value-of select="@ResourceIndex"/></xsl:attribute>
      <xsl:attribute name="value"><xsl:value-of select="@ResourceName"/></xsl:attribute>
    </input>
    Waste Production:
    <input type="checkbox" value="true">
      <xsl:attribute name="name">Waste<xsl:value-of select="@ResourceIndex"/></xsl:attribute>
      <xsl:if test="@Waste='true'">
        <xsl:attribute name="checked">Waste</xsl:attribute>
      </xsl:if>
    </input>
    - Speed:
    <input type="text" size="10" maxlength="30">
      <xsl:attribute name="name">Speed<xsl:value-of select="@ResourceIndex"/></xsl:attribute>
      <xsl:attribute name="value"><xsl:value-of select="@Speed"/></xsl:attribute>
    </input>
    <br/>
    <xsl:apply-templates/>
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