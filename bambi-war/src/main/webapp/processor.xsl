<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi">

  <!--  device processor -->
  <xsl:template match="bambi:Processor">
    
    <div class="row">
    <h2>Processor Status</h2>
    <em>
    <xsl:value-of select="@DeviceStatus"/>
    since
    <xsl:value-of select="@StartTime"/>
    </em>
    </div>
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
  
  
  Show JDF:
  <a><xsl:attribute name="href">../showJDF/<xsl:value-of select="../@DeviceID"/>?qeID=<xsl:value-of select="@QueueEntryID"/></xsl:attribute>
<xsl:value-of select="@QueueEntryID"/></a>

<div class="overlay-background processor">
<div class="overlay-content">
<h2>Resources:</h2>


<xsl:apply-templates select="bambi:PhaseAmount"/>
</div> <!-- overlay-content -->
</div> <!-- overlay-background -->
</xsl:if>
</xsl:template>

<!--   ///////////////////////////////////////////////// -->

<xsl:template match="bambi:PhaseAmount">
Type:<xsl:value-of select="@ResourceName"/>
Planned Amount: <xsl:value-of select="@PlannedAmount"/>
Planned Waste: <xsl:value-of select="@PlannedWaste"/>
Phase Amount: <xsl:value-of select="@PhaseAmount"/>
Phase Waste: <xsl:value-of select="@PhaseWaste"/>
Total Amount: <xsl:value-of select="@TotalAmount"/>
Total Waste: <xsl:value-of select="@TotalWaste"/>



</xsl:template>

</xsl:stylesheet>