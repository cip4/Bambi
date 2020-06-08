<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  <xsl:strip-space elements="*"/>
  <xsl:output method="html" cdata-section-elements="jdf:JMF jdf:Query"/>
  
  <!-- ######################################## -->
  
  <!-- Start of SubscriptionList -->
  <xsl:template match="/SubscriptionList">
    <xsl:variable name="context" select="@Context"/>
    <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <link rel="stylesheet" type="text/css" href="../css/styles_pc.css"/>
        <link rel="icon" href="favicon.ico" type="image/x-icon"/>
        <title>
          Device
          <xsl:value-of select="@DeviceID"/>
          - Subscription
        </title>
      </head>
      <body>
        <h1>
          Device
          <xsl:value-of select="@DeviceID"/>
          - Subscription ChannelID=
          <xsl:value-of select="MsgSubscription/@ChannelID"/>
        </h1>
        <xsl:if test="MsgSubscription">
          <h2>Subscriptions</h2>

            <xsl:apply-templates select="MsgSubscription"/>
          
        </xsl:if>

              <a>
                <xsl:attribute name="href"><xsl:value-of select="@Context"/>/showSubscriptions/<xsl:value-of select="@DeviceID"/>
          </xsl:attribute>
                Back to Subscription List
              </a>

              <a>
                <xsl:attribute name="href"><xsl:value-of select="@Context"/>/showDevice/<xsl:value-of select="@DeviceID"/>
          </xsl:attribute>
                Back to Device
              </a>



        <xsl:if test="MsgSubscription/Sub">
          <xsl:apply-templates select="MsgSubscription/Sub"/>
          <hr/>
        </xsl:if>
        <xsl:apply-templates select="MessageSender/Message"/>
        <xsl:if test="MsgSubscription/Message">
          <hr/>
          <h2>Previously Queued Messages</h2>
          <table>
            <tr>
              <th>Position</th>
              <th>Time Sent</th>
              <th>Message</th>
            </tr>
            <xsl:apply-templates select="MsgSubscription/Message"/>
          </table>
        </xsl:if>


      </body>
    </html>
  </xsl:template>
  <!--  end of template SubscriptionList  -->
  
  <!-- ######################################## -->
  
  <!-- Start of MsgSubscription -->
  <xsl:template match="MsgSubscription">
    <div class="box">
    <div class="subrow">
        Channel ID
        <a>
          <xsl:attribute name="href"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/>?DetailID=<xsl:value-of
            select="@ChannelID"/>
                  </xsl:attribute>
          <xsl:value-of select="@ChannelID"/>
        </a>
      </div>
      <div class="subrow">
        Device ID<xsl:value-of select="@DeviceID"/>
      </div>
      <div class="subrow">
        QueueEntry ID<xsl:value-of select="@QueueEntryID"/>
      </div>
      <div class="subrow">
        Signal Type<xsl:value-of select="@Type"/>
      </div>
      <div class="subrow">
        Subscription URL<xsl:value-of select="@URL"/>
      </div>
      <div class="subrow">
        Channel Mode<xsl:value-of select="*/jdf:Query/jdf:Subscription/@ChannelMode"/>
       </div>
      <div class="subrow">
        Repeat Time<xsl:value-of select="@RepeatTime"/>
      </div>
      <div class="subrow">
        Repeat Step<xsl:value-of select="@RepeatStep"/>
      </div>
      <div class="subrow">
        Messages Queued<xsl:value-of select="@Sent"/>
      </div>
      <div class="subrow">
        Last time Queued<xsl:value-of select="@LastTime"/>
      </div>
      <div class="subrow">
        Remove Subscription<form>
          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
          <input type="hidden" name="StopChannel" value="true"/>
          <input type="hidden" name="ChannelID">
            <xsl:attribute name="value"><xsl:value-of select="@ChannelID"/></xsl:attribute>
          </input>
          <input type="submit" value="remove"/>
        </form>
      </div>
    </div><!-- box -->
  </xsl:template>
  <!--  end of template MsgSubscription  -->
  
  <!-- ######################################## -->
  
  <!-- Start of MessageSender -->
  <xsl:include href="xjdf.xsl"/>
  <!--  end of template MessageSender  -->
  
  <!-- ######################################## -->
  
  <!-- Start of Sub -->
  <xsl:template match="Sub">
    <h2>Subscription Details</h2>
    <xsl:apply-templates select="jdf:Query"/>

  </xsl:template>
  <!--  end of template Sub  -->
  
  <!-- ######################################## -->
  
  <!-- Start of MessageSender Message -->
  <xsl:template match="MessageSender/Message">
    <h2>Last Queued Message Details - Status = <xsl:value-of select="@Return"/></h2>  
    <xsl:apply-templates select="jdf:JMF"/>
  </xsl:template>

  <xsl:template match="MessageSender">
    <h2>Queued Message Details</h2>
    <xsl:apply-templates select="jdf:JMF"/>
  </xsl:template>
  <!--  end of template MessageSender Message  -->
  
  <!-- ######################################## -->
  
  <!-- Start of RemovedChannel -->
  <xsl:template match="Message">
    <xsl:variable name="pos" select="position()"/>
    <tr valign="top">
      <td>
        <a>
          <xsl:attribute name="name">m<xsl:value-of select="position()"/></xsl:attribute>
        </a>
        <a>
          <xsl:attribute name="href"><xsl:value-of select="/SubscriptionList/@Context"/>/showSubscriptions/<xsl:value-of select="/SubscriptionList/@DeviceID"/>?pos=<xsl:value-of
            select="$pos"/>&amp;DetailID=<xsl:value-of select="/SubscriptionList/MsgSubscription/@ChannelID"/>#m<xsl:value-of select="$pos"/></xsl:attribute>
         JMF # <xsl:value-of select="$pos"/> 
           </a>
       </td>
       <td>
         <xsl:choose>
           <xsl:when test="jdf:JMF/@TimeStamp">
             <xsl:value-of select="jdf:JMF/@TimeStamp"/>
           </xsl:when>
           <xsl:otherwise>
             <xsl:value-of select="@TimeStamp"/>
           </xsl:otherwise>
         </xsl:choose>
       </td>
       <td>
           <xsl:apply-templates select="jdf:JMF"/>
       </td>
      </tr>
  </xsl:template>
  <!--  end of template RemovedChannel  -->
  
  <!-- ######################################## -->
  
  <xsl:include href="SubscriptionExtension.xsl"/>
  
</xsl:stylesheet>