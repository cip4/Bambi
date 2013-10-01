<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:bambi="www.cip4.org/Bambi" xmlns:xjdf="http://www.CIP4.org/JDFSchema_1_1"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  <xsl:strip-space elements="*"/>
  <xsl:output method="html"/>
  <xsl:template match="ProxySubscriptions">

        <xsl:if test="ProxySubscription">
        <hr/>
          <h2>
            Subscriptions to slave device:
            <xsl:value-of select="count(ProxySubscription)"/>
          </h2>
          <table cellspacing="2" border="1">
            <tr>
              <th align="left"> # </th>
              <th align="left"> Channel ID</th>
              <th align="left"> Signal Type</th>
              <th align="left"> Slave URL</th>
               <th align="left"> Messages Received</th>
              <th align="left"> Last time Received</th>
              <th align="left">Active since</th>
            </tr>
            <xsl:apply-templates select="ProxySubscription"/>
          </table>

          <hr/>
        </xsl:if>
 
        <hr/>
 
  </xsl:template>

  <!--  end of template SubscriptionList  -->
  <xsl:template match="ProxySubscription">
    <tr>
       <xsl:choose>
        <xsl:when test="@LastReceived=' - '">
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
        <xsl:value-of select="@ChannelID"/>
      </td>
       <td align="left">
        <xsl:value-of select="@Type"/>
      </td>
      <td align="left">
        <xsl:value-of select="xjdf:JMF/xjdf:Query/xjdf:Subscription/@URL"/>
      </td>
       <td align="left">
        <xsl:value-of select="@NumReceived"/>
      </td>
      <td align="left">
        <xsl:value-of select="@LastReceived"/>
      </td>
      <td align="left">
        <xsl:value-of select="@CreationDate"/>
      </td>
 
    </tr>
  </xsl:template>
  <!--  end of template MsgSubscription  -->


</xsl:stylesheet>