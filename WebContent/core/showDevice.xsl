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
          Simulation Device :
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
                <input type="hidden" name="setup" value="true"/>
                <input type="submit" value="refresh page"/>
              </form>
            </td>
            <td>
              <form style="margin-left: 20px">
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="shutdown" value="true"/>
                <input type="hidden" name="setup" value="true"/>
                <input type="submit" value="shutdown"/>
              </form>
            </td>
            <td>
              <form style="margin-left: 20px">
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="submit" value="restart"/>
                <input type="hidden" name="setup" value="true"/>
                <input type="hidden" name="restart" value="true"/>
              </form>
            </td>
            <xsl:if test="$modify!='true'">
              <td>
                <form style="margin-left: 20px">
                  <xsl:attribute name="action"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                  <input type="submit" value="modify"/>
                  <input type="hidden" name="modify" value="true"/>
                  <input type="hidden" name="setup" value="true"/>
                  <input type="hidden" name="refresh" value="false"/>
                </form>
              </td>
            </xsl:if>
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
        <br/>
        <hr/>
        <br/>
        <div style="margin-left: 20px">
          <form style="margin-left: 20px">
            <table>
              <xsl:attribute name="action">.</xsl:attribute>
              <tr>
                <td colspan="3">
                  <h3>Details of the Bambi device</h3>
                </td>
              </tr>

              <xsl:call-template name="modifyString">
                <xsl:with-param name="attLabel" select="'ID: '"/>
                <xsl:with-param name="attName" select="'unused'"/>
                <xsl:with-param name="attVal" select="$deviceID"/>
                <xsl:with-param name="modify" select="'false'"/>
                <xsl:with-param name="desc" select="'Unique device Identifier'"/>
              </xsl:call-template>

              <xsl:call-template name="modifyString">
                <xsl:with-param name="attLabel" select="'URL: '"/>
                <xsl:with-param name="attName" select="'DeviceURL'"/>
                <xsl:with-param name="attVal" select="$deviceURL"/>
                <xsl:with-param name="modify" select="'false'"/>
                <xsl:with-param name="desc" select="'Base (root) URL of the device'"/>
              </xsl:call-template>

              <xsl:call-template name="modifyString">
                <xsl:with-param name="attLabel" select="'Device Class: '"/>
                <xsl:with-param name="attName" select="'DeviceType'"/>
                <xsl:with-param name="attVal" select="$deviceType"/>
                <xsl:with-param name="modify" select="$modify"/>
                <xsl:with-param name="desc" select="'Short, human readable description of the device'"/>
              </xsl:call-template>

              <xsl:call-template name="modifyString">
                <xsl:with-param name="attLabel" select="'Watch URL: '"/>
                <xsl:with-param name="attName" select="'WatchURL'"/>
                <xsl:with-param name="attVal" select="@WatchURL"/>
                <xsl:with-param name="modify" select="$modify"/>
                <xsl:with-param name="desc"
                  select="'Single URL that receives messages (Status, Resource, Notification) from the device. If empty, no non-subscribed messages are sent.'"/>
              </xsl:call-template>

              <xsl:call-template name="modifyString">
                <xsl:with-param name="attLabel" select="'JDF Type expression: '"/>
                <xsl:with-param name="attName" select="'TypeExpression'"/>
                <xsl:with-param name="attVal" select="@TypeExpression"/>
                <xsl:with-param name="modify" select="$modify"/>
                <xsl:with-param name="desc" select="'Regular expression of types that are accepted by this device.'"/>
              </xsl:call-template>

              <xsl:call-template name="modifyString">
                <xsl:with-param name="attLabel" select="'Input Hot Folder: '"/>
                <xsl:with-param name="attName" select="'InputHF'"/>
                <xsl:with-param name="attVal" select="@InputHF"/>
                <xsl:with-param name="modify" select="$modify"/>
                <xsl:with-param name="desc" select="'Input hot folder for the proxy. Drop JDF Files in here. This is for testing only.'"/>
              </xsl:call-template>
              <xsl:call-template name="modifyString">
                <xsl:with-param name="attLabel" select="'Output Hot Folder: '"/>
                <xsl:with-param name="attName" select="'OutputHF'"/>
                <xsl:with-param name="attVal" select="@OutputHF"/>
                <xsl:with-param name="modify" select="$modify"/>
                <xsl:with-param name="desc" select="'Output folder for the proxy. Completed jdf files will be dropped here. This is for testing only.'"/>
              </xsl:call-template>
              <xsl:call-template name="modifyString">
                <xsl:with-param name="attLabel" select="'Error Output Hot Folder: '"/>
                <xsl:with-param name="attName" select="'ErrorHF'"/>
                <xsl:with-param name="attVal" select="@ErrorHF"/>
                <xsl:with-param name="modify" select="$modify"/>
                <xsl:with-param name="desc" select="'Output error folder for the proxy. Aborted jdf files will be dropped here. This is for testing only.'"/>
              </xsl:call-template>

              <xsl:if test="@SlaveURL">
                <tr>
                  <td colspan="3">
                    <h3>Details of the Slave(3rd Party) device</h3>
                  </td>
                </tr>
                <xsl:call-template name="modifyString">
                  <xsl:with-param name="attLabel" select="'Proxy URL for Slave: '"/>
                  <xsl:with-param name="attName" select="'DeviceURLForSlave'"/>
                  <xsl:with-param name="attVal" select="@DeviceURLForSlave"/>
                  <xsl:with-param name="modify" select="'false'"/>
                  <xsl:with-param name="desc" select="'URL of the proxy device for the slave (3rd party device).'"/>
                </xsl:call-template>
                <xsl:call-template name="modifyString">
                  <xsl:with-param name="attLabel" select="'Slave Entries: '"/>
                  <xsl:with-param name="attName" select="'MaxPush'"/>
                  <xsl:with-param name="attVal" select="@MaxPush"/>
                  <xsl:with-param name="modify" select="$modify"/>
                  <xsl:with-param name="desc" select="'Maximum number of concurrent entries to actively send to the device. If 0, device will pull entries.'"/>
                </xsl:call-template>
                <xsl:call-template name="modifyString">
                  <xsl:with-param name="attLabel" select="'Slave Input Hot Folder: '"/>
                  <xsl:with-param name="attName" select="'SlaveInputHF'"/>
                  <xsl:with-param name="attVal" select="@SlaveInputHF"/>
                  <xsl:with-param name="modify" select="$modify"/>
                  <xsl:with-param name="desc"
                    select="'Input folder for the slave (3rd party) device. The proxy will forward to this folder. If empty jobs are submitted to the slave URL via http.'"/>
                </xsl:call-template>
                <xsl:call-template name="modifyString">
                  <xsl:with-param name="attLabel" select="'Slave Output Hot Folder: '"/>
                  <xsl:with-param name="attName" select="'SlaveOutputHF'"/>
                  <xsl:with-param name="attVal" select="@SlaveOutputHF"/>
                  <xsl:with-param name="modify" select="$modify"/>
                  <xsl:with-param name="desc"
                    select="'Output folder for the slave (3rd party) device. The proxy will watch this folder. If empty jobs must be returned to the proxy slave URL via http.'"/>
                </xsl:call-template>
                <xsl:call-template name="modifyString">
                  <xsl:with-param name="attLabel" select="'Slave Error Output Hot Folder: '"/>
                  <xsl:with-param name="attName" select="'SlaveErrorHF'"/>
                  <xsl:with-param name="attVal" select="@SlaveErrorHF"/>
                  <xsl:with-param name="modify" select="$modify"/>
                  <xsl:with-param name="desc"
                    select="'Error output folder for the slave (3rd party) device. The proxy will watch this folder. If empty jobs must be returned to the proxy slave URL via http.'"/>
                </xsl:call-template>
                <xsl:call-template name="modifyString">
                  <xsl:with-param name="attLabel" select="'Slave URL: '"/>
                  <xsl:with-param name="attName" select="'SlaveURL'"/>
                  <xsl:with-param name="attVal" select="@SlaveURL"/>
                  <xsl:with-param name="modify" select="$modify"/>
                  <xsl:with-param name="desc" select="'URL of the slave (3rd party) device.'"/>
                </xsl:call-template>
                <xsl:call-template name="modifyString">
                  <xsl:with-param name="attLabel" select="'Slave DeviceID: '"/>
                  <xsl:with-param name="attName" select="'SlaveDeviceID'"/>
                  <xsl:with-param name="attVal" select="@SlaveDeviceID"/>
                  <xsl:with-param name="modify" select="$modify"/>
                  <xsl:with-param name="desc" select="'Device ID of the slave (3rd party) device.'"/>
                </xsl:call-template>
              </xsl:if>
              <xsl:if test="$modify='true'">
                <tr>
                  <input type="submit" value="Modify"/>
                </tr>
              </xsl:if>
            </table>
          </form>
        </div>
        <hr/>
        <!-- call queues and phases -->
        <h3>Queue and Subscriptions</h3>
        <table>
          <tr>
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
          </tr>
        </table>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:include href="processor.xsl"/>
  <xsl:include href="modifyString.xsl"/>
  <xsl:include href="DeviceExtension.xsl"/>


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

  <xsl:template match="KnownEmployees">
  <!--  nop here -->
  </xsl:template>
  <xsl:template match="jdf:Employee">
  <!--  nop here -->
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