<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:bambi="www.cip4.org/Bambi"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:strip-space elements="*"/>
  <xsl:output method="html"/>
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
        
        <hr/>
        
        <h2>Message Sender Channels</h2>
        <table cellspacing="2" border="1">
          <tr>
            <th align="left"> Base URL</th>
            <th align="left"> Currently active</th>
            <th align="left"> Messages Pending</th>
            <th align="left"> Messages Sent</th>
            <th align="left"> Last time Sent</th>
            <th align="left"> Last time Queued</th>
            <th align="left"> Active since</th>
            <th align="left"> Remove Sender</th>
            <th align="left"> Flush unsent Messages</th>
          </tr>
          <xsl:apply-templates select="MessageSender"/>
        </table>
        <hr/>
        <ul>
          <xsl:apply-templates select="RemovedChannel"/>
        </ul>
        <hr/>
        <a>
          <xsl:attribute name="href">../showDevice/<xsl:value-of select="@DeviceID"/></xsl:attribute>
          back to device
          <xsl:value-of select="@DeviceID"/>
        </a>
      </body>
    </html>
  </xsl:template>  

<!--  end of template SubscriptionList  -->
  <xsl:template match="MsgSubscription">
    <tr>
      <td align="left">
        <xsl:value-of select="@ChannelID"/>
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
          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of
            select="../@DeviceID"/></xsl:attribute>
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
  <xsl:template match="MessageSender">
    <tr>
      <td align="left">
        <xsl:value-of select="@URL"/>
      </td>
      <td align="left">
        <xsl:value-of select="@Active"/>
      </td>
      <td align="left">
        <xsl:value-of select="@Size"/>
      </td>
      <td align="left">
        <xsl:value-of select="@NumSent"/>
      </td>
      <td align="left">
        <xsl:value-of select="@LastSent"/>
      </td>
      <td align="left">
        <xsl:value-of select="@LastQueued"/>
      </td>
      <td align="left">
        <xsl:value-of select="@CreationDate"/>
      </td>
      <td align="center">
        <form>
          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of
            select="../@DeviceID"/></xsl:attribute>
          <input type="hidden" name="StopSender" value="true"/>
          <input type="hidden" name="URL">
            <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
          </input>
          <input type="submit" value="remove"/>
        </form>
      </td>
      <td align="center">
        <form>
          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of
            select="../@DeviceID"/></xsl:attribute>
          <input type="hidden" name="FlushSender" value="true"/>
          <input type="hidden" name="URL">
            <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
          </input>
          <input type="submit" value="flush"/>
        </form>
      </td>
    </tr>
  </xsl:template>  
<!--  end of template MessageSender  -->
  <xsl:template match="RemovedChannel">
    <li>
      Subscription
      <xsl:value-of select="@ChannelID"/>
      to
      <xsl:value-of select="@URL"/>
      has been removed.
    </li>
  </xsl:template>  
<!--  end of template RemovedChannel  -->
</xsl:stylesheet>