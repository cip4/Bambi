<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi">

  <xsl:template match="/*">
    <xsl:variable name="context" select="@Context"/>
    <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <link rel="stylesheet" type="text/css" href="../css/styles_pc.css"/>
        <link rel="icon" href="favicon.ico" type="image/x-icon"/>
        <title>
          JDF Job Ticket Summary
          <xsl:value-of select="@JobID"/>
        </title>
      </head>
      <body>
        <img src="../logo.gif" height="70" alt="logo"/>
        <table>
          <tr>
            <td>
              <a>
                <xsl:attribute name="href"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                Back to Queue
              </a>
            </td>
            <td>
              <a>
                <xsl:attribute name="href"><xsl:value-of select="$context"/>/showDevice/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                Back to Device
              </a>
            </td>
            <td>
              <a>
                <xsl:attribute name="href"><xsl:value-of select="$context"/>/showJDF/<xsl:value-of select="@DeviceID"/>?raw=true&amp;qeID=<xsl:value-of
            select="@QueueEntryID"/></xsl:attribute>
          Show JDF XML Source
        </a>
        </td>
        </tr>
        </table>
        <h1>
          JDF Job Ticket
          <xsl:value-of select="@JobID"/>
        </h1>
        <table cellspacing="1" border="1">
          <tr bgcolor="#bbbbbb">
            <th align="left">JobPartID</th>
            <th align="left">Status</th>
            <th align="left">Description</th>
            <th align="left">Type</th>
            <th align="left">Types</th>
          </tr>
          <xsl:call-template name="jdf"/> <!-- add myself -->
          <xsl:apply-templates select="jdf:JDF"/>
        </table>

      </body>
    </html>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <!--  one / node -->
  <xsl:template match="jdf:JDF" name="jdf">
    <tr>
    <td>
        <a>
          <xsl:attribute name="href"><xsl:value-of select="/jdf:JDF/@Context"/>/showJDF/<xsl:value-of select="/jdf:JDF/@DeviceID"/>?qeID=<xsl:value-of
            select="/jdf:JDF/@QueueEntryID"/>&amp;JobPartID=<xsl:value-of select="@JobPartID"/></xsl:attribute>
    <xsl:value-of select="@JobPartID"/>
        </a>
    </td>
    <td>
    <xsl:value-of select="@Status"/>
    </td>
    <td>
    <xsl:value-of select="@DescriptiveName"/>
    </td>
    <td>
    <xsl:value-of select="@Type"/>
    </td>
    <td>
    <xsl:value-of select="@Types"/>
    </td>
        </tr>
        
    <xsl:apply-templates select="jdf:JDF"/>

  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

</xsl:stylesheet>