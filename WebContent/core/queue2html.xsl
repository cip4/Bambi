<!--  Copyright 2009 CIP4 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi">
  <xsl:output method="html"/>
  <xsl:template match="jdf:Queue">
    <xsl:variable name="context" select="@Context"/>
    <html>
      <head>
        <link rel="stylesheet" type="text/css">
          <xsl:attribute name="href"><xsl:value-of select="$context"/>/css/styles_pc.css</xsl:attribute>
        </link>
        <xsl:if test="@refresh">
          <meta http-equiv="refresh">
            <xsl:attribute name="content">15; URL=<xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/>?refresh=true</xsl:attribute>
          </meta>
        </xsl:if>
      </head>
      <body>
        <img height="70" alt="logo">
          <xsl:attribute name="src"><xsl:value-of select="$context"/>/logo.gif</xsl:attribute>
        </img>
        <h1>
          Queue - DeviceID:
          <xsl:value-of select="@DeviceID"/>
          - Queue Status:
          <xsl:value-of select="@Status"/>
        </h1>
        <h2>
          <xsl:value-of select="@DescriptiveName"/>
        </h2>
        <hr/>
        <table cellspacing="22" border="0">
          <tr>
            <td>
              Show Device:
              <a>
                <xsl:attribute name="href"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                Device:
                <xsl:value-of select="@DeviceID"/>
              </a>
            </td>
            <td>
              Back to
              <a>
                <xsl:attribute name="href"><xsl:value-of select="$context"/>/overview</xsl:attribute>
                Device List
              </a>
            </td>
          </tr>
        </table>
        <hr/>

        <!--  queue summary table  -->
        <h2>Queue Summary</h2>
        <table cellspacing="2" border="1">
          <xsl:choose>
            <xsl:when test="@Status='Waiting'">
              <tr bgcolor="#aaffaa">
                <th align="left">Status</th>
                <th align="center">
                  <xsl:value-of select="@Status"/>
                </th>
              </tr>
            </xsl:when>
            <xsl:when test="@Status='Running'">
              <tr bgcolor="#aaaaff">
                <th align="left">Status</th>
                <th align="center">
                  <xsl:value-of select="@Status"/>
                </th>
              </tr>
            </xsl:when>
            <xsl:otherwise>
              <tr bgcolor="#ffaaaa">
                <td align="left">Status</td>
                <td align="center">
                  <xsl:value-of select="@Status"/>
                </td>
              </tr>
            </xsl:otherwise>
          </xsl:choose>
          <tr bgcolor="#bbbbbb">
            <th align="left">Queue Entry Status</th>
            <th align="center"># of Entries</th>
          </tr>
          <tr>
            <td align="left">All</td>
            <td align="center">
              <xsl:value-of select="count(jdf:QueueEntry)"/>
            </td>
          </tr>
          <xsl:if test="count(jdf:QueueEntry[@Status='Waiting'])>0">
            <tr bgcolor="#aaaaff">
              <td align="left">Waiting</td>
              <td align="center">
                <xsl:value-of select="count(jdf:QueueEntry[@Status='Waiting'])"/>
              </td>
            </tr>
          </xsl:if>
          <xsl:if test="count(jdf:QueueEntry[@Status='Running'])>0">
            <tr bgcolor="#aaffaa">
              <td align="left">Running</td>
              <td align="center">
                <xsl:value-of select="count(jdf:QueueEntry[@Status='Running'])"/>
              </td>
            </tr>
          </xsl:if>
          <xsl:if test="count(jdf:QueueEntry[@Status='Held'])>0">
            <tr bgcolor="#ffffaa">
              <td align="left">Held</td>
              <td align="center">
                <xsl:value-of select="count(jdf:QueueEntry[@Status='Held'])"/>
              </td>
            </tr>
          </xsl:if>
          <xsl:if test="count(jdf:QueueEntry[@Status='Suspended'])>0">
            <tr bgcolor="#ffffaa">
              <td align="left">Suspended</td>
              <td align="center">
                <xsl:value-of select="count(jdf:QueueEntry[@Status='Suspended'])"/>
              </td>
            </tr>
          </xsl:if>
          <xsl:if test="count(jdf:QueueEntry[@Status='Completed'])>0">
            <tr bgcolor="#dddddd">
              <td align="left">Completed</td>
              <td align="center">
                <xsl:value-of select="count(jdf:QueueEntry[@Status='Completed'])"/>
              </td>
            </tr>
          </xsl:if>
          <xsl:if test="count(jdf:QueueEntry[@Status='Aborted'])>0">
            <tr bgcolor="#ffaaaa">
              <td align="left">Aborted</td>
              <td align="center">
                <xsl:value-of select="count(jdf:QueueEntry[@Status='Aborted'])"/>
              </td>
            </tr>
          </xsl:if>
        </table>
        <br/>
        <hr/>
        <!--  global queue buttons -->
        <table cellspacing="2" border="0">
          <tr>
            <td align="center" colspan="2">Incoming Entries</td>
            <td/>
            <td align="center" colspan="2">Outgoing Entries</td>
            <td/>
            <td align="center" colspan="2">refresh Queue</td>
            <td align="center" colspan="2">
              Filter Queue
            </td>
            <td align="center" colspan="2">
              <em>Danger flushes entire Queue!</em>
            </td>
          </tr>
          <tr>
            <td align="left">
              <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="open" value="true"/>
                <input type="submit" value="open queue"/>
              </form>
            </td>
            <td align="left">
              <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="close" value="true"/>
                <input type="submit" value="close queue"/>
              </form>
            </td>
            <td width="5"/>
            <td align="left">
              <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="resume" value="true"/>
                <input type="submit" value="resume queue"/>
              </form>
            </td>
            <td align="left">
              <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="hold" value="true"/>
                <input type="submit" value="hold queue"/>
              </form>
            </td>
            <td width="10"/>
            <td align="center">
              <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="submit" value="refresh queue"/>
              </form>
            </td>
            <td width="10"/>

            <td align="center">
              <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="text" name="filter">
                  <xsl:attribute name="value"><xsl:value-of select="@filter"/></xsl:attribute>
                </input>
                <input type="submit" value="filter queue"/>
              </form>
            </td>
            <td width="10"/>
            <td align="center">
              <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="flush" value="true"/>
                <input type="submit" value="flush queue"/>
              </form>
            </td>
          </tr>
        </table>
        <h2>Queue Entry Details</h2>
        <!--  queueentry table description  -->
        <table cellspacing="1" border="1">
          <tr bgcolor="#bbbbbb">
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'QueueEntryID'"/>
              <xsl:with-param name="attName" select="'QueueEntryID'"/>
            </xsl:call-template>
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'Priority'"/>
              <xsl:with-param name="attName" select="'Priority'"/>
            </xsl:call-template>
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'Submission Date / Time'"/>
              <xsl:with-param name="attName" select="'SubmissionTime'"/>
            </xsl:call-template>
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'Start Date / Time'"/>
              <xsl:with-param name="attName" select="'StartTime'"/>
            </xsl:call-template>
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'End Date / Time'"/>
              <xsl:with-param name="attName" select="'EndTime'"/>
            </xsl:call-template>
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'Description'"/>
              <xsl:with-param name="attName" select="'DescriptiveName'"/>
            </xsl:call-template>
           <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'Job ID'"/>
              <xsl:with-param name="attName" select="'JobID'"/>
            </xsl:call-template>
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'JobPartID'"/>
              <xsl:with-param name="attName" select="'JobPartID'"/>
            </xsl:call-template>
            <xsl:if test="@bambi:SlaveURL='true'">
              <xsl:call-template name="qeHeader">
                <xsl:with-param name="display" select="'Slave QueueEntryID'"/>
                <xsl:with-param name="attName" select="'bambi:SlaveQueueEntryID'"/>
              </xsl:call-template>
            </xsl:if>
            <th align="center">Device</th>
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'Status'"/>
              <xsl:with-param name="attName" select="''"/>
            </xsl:call-template>
          </tr>
          <xsl:apply-templates/>
        </table>
        <hr/>
        <form>
          <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
          <input type="submit" value="refresh queue"/>
        </form>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="qeHeader">
    <xsl:param name="display"/>
    <xsl:param name="attName"/>
    <th align="center">
      <a>
        <xsl:attribute name="href">
                 <xsl:value-of select="@Context"/>/showQueue/<xsl:value-of select="@DeviceID"/>?SortBy=<xsl:value-of select="$attName"/><xsl:if test="@filter">&amp;filter=<xsl:value-of select="@filter"/></xsl:if>
        </xsl:attribute>
        <xsl:value-of select="$display"/>
      </a>
    </th>
  </xsl:template>
  <!--      QueueEntry template  -->
  <xsl:template match="jdf:QueueEntry">
    <xsl:variable name="context" select="../@Context"/>
    <tr>
      <xsl:if test="@Status='Running'">
        <xsl:attribute name="bgcolor">#aaffaa</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Waiting'">
        <xsl:attribute name="bgcolor">#aaaaff</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Suspended'">
        <xsl:attribute name="bgcolor">#ffffaa</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Held'">
        <xsl:attribute name="bgcolor">#ffffaa</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Aborted'">
        <xsl:attribute name="bgcolor">#ffaaaa</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Completed'">
        <xsl:attribute name="bgcolor">#dddddd</xsl:attribute>
      </xsl:if>
      <td align="left">
        <a>
          <xsl:attribute name="href"><xsl:value-of select="$context"/>/showJDF/<xsl:value-of select="../@DeviceID"/>?qeID=<xsl:value-of
            select="@QueueEntryID"/></xsl:attribute>
          <xsl:value-of select="@QueueEntryID"/>
        </a>
      </td>
      <td align="left">
        <xsl:value-of select="@Priority"/>
      </td>
      <td align="left">
           <xsl:call-template name="dateTime">
              <xsl:with-param name="val" select="@SubmissionTime"/>
            </xsl:call-template>
       </td>
      <td align="left">
          <xsl:call-template name="dateTime">
              <xsl:with-param name="val" select="@StartTime"/>
            </xsl:call-template>
      </td>
      <td align="left">
          <xsl:call-template name="dateTime">
              <xsl:with-param name="val" select="@EndTime"/>
            </xsl:call-template>
      </td>
      <td align="left">
        <xsl:value-of select="@DescriptiveName"/>
      </td>
      <td align="left">
        <xsl:value-of select="@JobID"/>
      </td>
      <td align="left">
        <xsl:value-of select="@JobPartID"/>
      </td>
      <xsl:if test="../@bambi:SlaveURL='true'">
        <td align="left">
          <xsl:value-of select="@bambi:SlaveQueueEntryID"/>
        </td>
      </xsl:if>
      <td>
        <a>
          <xsl:attribute name="href"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of select="@DeviceID"/></xsl:attribute>
          Device:
          <xsl:value-of select="@DeviceID"/>
        </a>
      </td>
      <td nowrap="true">
        <!-- calls the optionList -->
        <form>
          <xsl:attribute name="action"><xsl:value-of select="$context"/>/modifyQE/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
          <xsl:apply-templates/>
          <input type="hidden" name="qeID">
            <xsl:attribute name="value"><xsl:value-of select="@QueueEntryID"/></xsl:attribute>
          </input>
          <input type="submit" value="modify entry"/>
        </form>
      </td>
    </tr>
  </xsl:template>
  <xsl:include href="optionlist.xsl"/>
  <xsl:include href="StandardXML.xsl"/>
</xsl:stylesheet>