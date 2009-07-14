<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:bambi="www.cip4.org/Bambi" xmlns:xjdf="http://www.CIP4.org/JDFSchema_1_1"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  <xsl:strip-space elements="*"/>
  <xsl:output method="html"/>
  <xsl:template match="/SubscriptionList">
    <xsl:variable name="context" select="@Context"/>
    <xsl:variable name="deviceID" select="@DeviceID"/>
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
        <xsl:call-template name="links"/>
        <xsl:if test="MsgSubscription">
          <h2>
            Subscriptions:
            <xsl:value-of select="count(MsgSubscription)"/>
          </h2>
          <table cellspacing="2" border="1">
            <tr>
              <th align="left"> # </th>
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
        </xsl:if>
        <h2>Message Sender Channels</h2>
        <table cellspacing="2" border="1">
          <tr>
            <th align="left"> Base URL</th>
            <th align="left"> Status</th>
            <th align="left"> Messages Pending</th>
            <th align="left"> Messages Sent</th>
            <th align="left"> Last time Sent</th>
            <th align="left"> Last time Queued</th>
            <th align="left"> Active since</th>
            <th align="left"> Show Sent Messages</th>
            <th align="left"> pause / resume</th>
            <th align="left"> Remove Sender</th>
            <th align="left"> Flush unsent Messages</th>
          </tr>
          <xsl:apply-templates select="MessageSender"/>
        </table>
        <hr/>
        <ul>
          <xsl:apply-templates select="RemovedChannel"/>
        </ul>

        <xsl:if test="MessageSender/Message">
          <h2>Queued Messages</h2>
          <table cellspacing="2" border="1">
            <tr>
              <th align="left"> JMF ID</th>
              <th align="left"> Sent Time</th>
              <th align="left"> Full URL</th>
            </tr>
            <xsl:apply-templates select="MessageSender/Message"/>
          </table>

          <hr/>
        </xsl:if>

        <hr/>
        <xsl:call-template name="links"/>
      </body>
    </html>
  </xsl:template>

  <!--  end of template SubscriptionList  -->
  <xsl:template match="MsgSubscription">
    <tr>
      <xsl:choose>
        <xsl:when test="@LastTime=' - '">
          <xsl:choose>
            <xsl:when test="@Sent='0'">
              <xsl:attribute name="bgcolor">#ffaaaa</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <xsl:attribute name="bgcolor">#ffffaa</xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="bgcolor">#aaffaa</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <td align="left">
        <xsl:value-of select="position()"/>
      </td>
      <td align="left">
        <a>
          <xsl:attribute name="href"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/>?DetailID=<xsl:value-of
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

  <xsl:template match="MessageSender">
    <tr>
      <xsl:choose>
        <!-- something is waiting -->
        <xsl:when test="@Size!='0'">
          <xsl:choose>
            <xsl:when test="@pause='true'">
              <xsl:attribute name="bgcolor">#ffccaa</xsl:attribute>
            </xsl:when>
            <xsl:when test="@Active='true'">
              <xsl:attribute name="bgcolor">#ffffaa</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <xsl:attribute name="bgcolor">#ffaaaa</xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="@pause='true'">
          <xsl:attribute name="bgcolor">#ffccaa</xsl:attribute>
        </xsl:when>
        <xsl:when test="@Active='true'">
          <xsl:attribute name="bgcolor">#aaffaa</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="bgcolor">#aaaaff</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <td align="left">
        <xsl:value-of select="@URL"/>
      </td>
      <td align="left">
        <xsl:choose>
          <xsl:when test="@Active='false'">
            down
          </xsl:when>
          <xsl:when test="@pause='true'">
            paused
          </xsl:when>
          <xsl:when test="@Size!='0'">
            <xsl:choose>
              <xsl:when test="@idle!='0'">
                dispatch errors
              </xsl:when>
              <xsl:otherwise>
                back log
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            active
          </xsl:otherwise>
        </xsl:choose>
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
          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
          <input type="hidden" name="ListSenders" value="true"/>
          <input type="hidden" name="URL">
            <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
          </input>
          <input type="submit" value="List Senders"/>
        </form>
      </td>
      <td align="center">
        <form>
          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
          <input type="hidden" name="URL">
            <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
          </input>
          <xsl:choose>
            <xsl:when test="@pause='true'">
              <input type="hidden" name="pause" value="false"/>
              <input type="submit" value="resume"/>
            </xsl:when>
            <xsl:otherwise>
              <input type="hidden" name="pause" value="true"/>
              <input type="submit" value="pause"/>
            </xsl:otherwise>
          </xsl:choose>
        </form>
      </td>
      <td align="center">
        <form>
          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
          <input type="hidden" name="StopSender" value="true"/>
          <input type="hidden" name="URL">
            <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
          </input>
          <input type="submit" value="remove"/>
        </form>
      </td>
      <td align="center">
        <form>
          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
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
  <xsl:template match="Message">
    <tr>
      <td>
        <xsl:value-of select="position()"/>
        <form>
          <xsl:attribute name="action"><xsl:value-of select="/SubscriptionList/@Context"/>/showSubscriptions/<xsl:value-of select="/SubscriptionList/@DeviceID"/></xsl:attribute>
          <input type="hidden" name="ListSenders" value="true"/>
          <input type="hidden" name="URL">
            <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
          </input>
          <input type="hidden" name="pos">
            <xsl:attribute name="value"><xsl:value-of select="position()"/></xsl:attribute>
          </input>
          <input type="submit" value="Show Details"/>
        </form>
      </td>
      <td>
        <xsl:choose>
          <xsl:when test="xjdf:JMF/@TimeStamp">
            <xsl:value-of select="xjdf:JMF/@TimeStamp"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@TimeStamp"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td>
        <xsl:value-of select="@URL"/>
      </td>
    </tr>
  </xsl:template>
  <xsl:template name="links">
    <table>
      <tr>
        <td>
          <a>
            <xsl:attribute name="href"><xsl:value-of select="@Context"/>/showSubscriptions/<xsl:value-of select="@DeviceID"/>
            </xsl:attribute>
            refresh
          </a>
        </td>
        <td>
          -
          </td>
        <td>
          <a>
            <xsl:attribute name="href"><xsl:value-of select="@Context"/>/showDevice/<xsl:value-of select="@DeviceID"/>
            </xsl:attribute>
            Back to Device
          </a>
        </td>
        <td>
          -
          </td>
        <td>
          <a>
            <xsl:attribute name="href"><xsl:value-of select="@Context"/>/showQueue/<xsl:value-of select="@DeviceID"/>
            </xsl:attribute>
            Back to Queue
          </a>
        </td>
      </tr>
    </table>
    <hr/>
  </xsl:template>
</xsl:stylesheet>