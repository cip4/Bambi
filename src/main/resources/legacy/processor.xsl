<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi">

  <!--  device processor -->
  <xsl:template match="bambi:Processor">
    <h2>Processor Status</h2>
    Processor Status:
    <xsl:value-of select="@DeviceStatus"/>
    since
    <xsl:value-of select="@StartTime"/>
    <br/>
    <xsl:if test="@DeviceStatusDetails">
      Processor Status Details:
      <xsl:value-of select="@DeviceStatusDetails"/>
    </xsl:if>
    <xsl:if test="@NodeStatus">
      <h2>Node Status</h2>
      Node Status:
      <xsl:value-of select="@NodeStatus"/>
      <br/>
      <xsl:if test="@NodeStatusDetails">
        Node Status Details:
        <xsl:value-of select="@NodeStatusDetails"/>
        <br/>
      </xsl:if>
      Job ID:
      <xsl:value-of select="@JobID"/>
      <xsl:if test="@JobPartID">
        /
        <xsl:value-of select="@JobPartID"/>
      </xsl:if>
      <br/>
      <xsl:if test="@PartIDKeys">
        Node Partition Keys:
        <xsl:value-of select="@PartIDKeys"/>
        <br/>
      </xsl:if>
      QueueEntryID:
      <xsl:value-of select="@QueueEntryID"/>
      <br/>
      Node type:
      <xsl:value-of select="@Type"/>
      <br/>
      Description:
      <xsl:value-of select="@DescriptiveName"/>
      <br/>
      Start Time:
      <xsl:value-of select="@StartTime"/>
      <br/>
      <xsl:if test="@PercentCompleted">
        Percent Completed:
        <xsl:value-of select="@PercentCompleted"/>% <br/>
</xsl:if>
  Show JDF:<a><xsl:attribute name="href">../showJDF/<xsl:value-of select="../@DeviceID"/>?qeID=<xsl:value-of select="@QueueEntryID"/></xsl:attribute>
<xsl:value-of select="@QueueEntryID"/></a>


<h2>Resources:</h2>
<table cellspacing="1" border="1">
<tr bgcolor="#dddddd">
<th>Type</th>
<th>Planned Amount</th>
<th>Planned Waste</th>
<th>Phase Amount</th>
<th>Phase Waste</th>
<th>Total Amount</th>
<th>Total Waste</th>
</tr>

<xsl:apply-templates select="bambi:PhaseAmount"/>

</table>
</xsl:if>
</xsl:template>

<!--   ///////////////////////////////////////////////// -->

<xsl:template match="bambi:PhaseAmount">
<tr>
<td bgcolor="#dddddd"><xsl:value-of select="@ResourceName"/></td>
<td align="center"><xsl:value-of select="@PlannedAmount"/></td>
<td align="center"><xsl:value-of select="@PlannedWaste"/></td>
<td align="center"><xsl:value-of select="@PhaseAmount"/></td>
<td align="center"><xsl:value-of select="@PhaseWaste"/></td>
<td align="center"><xsl:value-of select="@TotalAmount"/></td>
<td align="center"><xsl:value-of select="@TotalWaste"/></td>
</tr>


</xsl:template>

</xsl:stylesheet>