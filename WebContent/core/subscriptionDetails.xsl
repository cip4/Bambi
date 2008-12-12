<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  <xsl:strip-space elements="*"/>
  <xsl:output method="html" cdata-section-elements="jdf:JMF jdf:Query"/>
  <xsl:template match="/SubscriptionList">
    <xsl:variable name="context" select="@Context"/>
    <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <link rel="stylesheet" type="text/css" href="../css/styles_pc.css"/>
        <link rel="icon" href="favicon.ico" type="image/x-icon"/>
        <title>
          <xsl:value-of select="@DeviceID"/>
          - Subscriptions
        </title>
      </head>
      <body>
        <img src="../logo.gif" height="70" alt="logo"/>
        <h1>
          <xsl:value-of select="@DeviceID"/>
          - Subscriptions
        </h1>
        <h2>Subscriptions</h2>
        <table cellspacing="2" border="1">
          <tr>
            <th align="left"> Channel ID</th>
            <th align="left"> Device ID</th>
            <th align="left"> QueueEntry ID</th>
            <th align="left"> Signal Type</th>
            <th align="left"> Subscription URL</th>
            <th align="left"> Repeat Time</th>
            <th align="left"> Repeat Step</th>
            <th align="left"> Messages Queued</th>
            <th align="left"> Last time Queued</th>
            <th align="left"> Remove Subscription</th>
          </tr>
          <xsl:apply-templates select="MsgSubscription"/>
        </table>

        <a>
          <xsl:attribute name="href"><xsl:value-of select="@Context"/>/showSubscriptions/<xsl:value-of select="@DeviceID"/>
                  </xsl:attribute>
          Back to Subscription List
        </a>
        <hr/>

        <xsl:apply-templates select="MsgSubscription/Sub"/>
        <hr/>
        <xsl:apply-templates select="MsgSubscription/Last"/>



      </body>
    </html>
  </xsl:template>

  <!--  end of template SubscriptionList  -->
  <xsl:template match="MsgSubscription">
    <tr>
      <td align="left">
        <a>
          <xsl:attribute name="href"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="@DeviceID"/>?DetailID=<xsl:value-of
            select="@ChannelID"/>
                  </xsl:attribute>
          <xsl:value-of select="@ChannelID"/>
        </a>
      </td>
      <td align="left">
        <xsl:value-of select="@DeviceID"/>
      </td>
      <td align="left">
        <xsl:value-of select="@QueueEntryID"/>
      </td>
      <td align="left">
        <xsl:value-of select="@Type"/>
      </td>
      <td align="left">
        <xsl:value-of select="@URL"/>
      </td>
      <td align="left">
        <xsl:value-of select="@RepeatTime"/>
      </td>
      <td align="left">
        <xsl:value-of select="@RepeatStep"/>
      </td>
      <td align="left">
        <xsl:value-of select="@Sent"/>
      </td>
      <td align="left">
        <xsl:value-of select="@LastTime"/>
      </td>
      <td align="center">
        <form>
          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
          <input type="hidden" name="StopChannel" value="true"/>
          <input type="hidden" name="ChannelID">
            <xsl:attribute name="value"><xsl:value-of select="@ChannelID"/></xsl:attribute>
          </input>
          <input type="submit" value="remove"/>
        </form>
      </td>
    </tr>
  </xsl:template>
  <!--  end of template MsgSubscription  -->

  <!--  end of template MessageSender  -->
  <xsl:template match="Sub">
    <h2>Subscription Details</h2>
    <code>
      <xsl:apply-templates select="jdf:Query"/>
    </code>

  </xsl:template>
  <xsl:template match="Last">
    <h2>Last Queued Message Details</h2>
    <code>
      <xsl:apply-templates select="jdf:JMF"/>
    </code>
  </xsl:template>
  <xsl:template match="jdf:*">
    <br/>
   &lt;<b><xsl:value-of select="name()" disable-output-escaping="yes" /></b>
<xsl:for-each select="@*">
<br/><xsl:text> </xsl:text>
<i><xsl:value-of select="name()"/></i>="<xsl:value-of select="."/>"
</xsl:for-each>&gt;
           <xsl:apply-templates select="jdf:*"/>
           <b>
<br/>&lt;/<xsl:value-of select="name()" disable-output-escaping="yes" />&gt;
</b>
</xsl:template>
 
  <!--  end of template RemovedChannel  -->
</xsl:stylesheet>