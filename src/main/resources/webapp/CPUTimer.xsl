<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi">

<xsl:template name="cputimer">

<xsl:for-each select="//CPUTimer">

  <xsl:call-template name="internalcputimer"/>

</xsl:for-each>

</xsl:template>


<xsl:template name="internalcputimer">



<div class="cputimer row clear noheight">
    <div class="subrow">
    <h3>CPU Timer:</h3>
    <em><xsl:value-of select="@Name"/></em>
    </div>
    
    
    <!-- Stats -->
    <div class="subrow">
    <h3>Stats: </h3>
    <em>Start:<br/>
    <xsl:value-of select="@CreationTime"/><br/>
    Invocations:<br/>
    <xsl:value-of select="@StartStop"/><br/>
    </em>
    </div>
    
    
    
    
    <!-- Timers -->
    
    <div class="subrow clear noheight">
    <h3>Current:</h3>
    <em>
    Real Time: <xsl:value-of select="@CurrentRealTime"/> sec<br/>
    CPU Time: <xsl:value-of select="@CurrentCPUTime"/> sec<br/>
    </em>
    </div>
    
    <div class="subrow">
    <h3>Total:</h3>
    <em>
    Real Time: <xsl:value-of select="@TotalRealTime"/> sec<br/>
    CPU Time: <xsl:value-of select="@TotalCPUTime"/> sec<br/>
    </em>
    </div>
    
    <div class="subrow">
    <h3>Average:</h3>
    <em>
    Real Time: <xsl:value-of select="@AverageRealTime"/> sec<br/>
    CPU Time: <xsl:value-of select="@AverageCPUTime"/> sec<br/>
    </em>
    </div>
    <div class="clear"></div>
</div><!-- box clear cputimer -->
</xsl:template>

<xsl:template match="CPUTimer"/>
</xsl:stylesheet>