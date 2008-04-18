<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
	xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi" >
  <xsl:output method="html"/>
  
  <xsl:template match="jdf:Queue">
    <html>
      <head> 
 						<link rel="stylesheet" type="text/css" href="../css/styles_pc.css"/>
     				<meta http-equiv="refresh">
							<xsl:attribute name="content">15; URL=../showQueue/<xsl:value-of select="@DeviceID"/>?refresh=true</xsl:attribute>
					 		</meta>

      </head>
      <body>
		<img src="../logo.gif" height="70"  alt="logo"/>
      <h1>Queue - DeviceID:<xsl:value-of select = "@DeviceID" /> - Status: <xsl:value-of select = "@Status" /></h1>
      <hr/>
      Show Device:<a><xsl:attribute name="href">../showDevice/<xsl:value-of select="@DeviceID"/></xsl:attribute>
      Device: <xsl:value-of select="@DeviceID"/> </a>
     
      <hr/>
        <table cellspacing="1" border="1" >
          <tr bgcolor="#bbbbbb">
            <th align="left" >QueueEntryID</th>
            <th align="left" >Submission Date / Time</th>
            <th align="left" >Start Date / Time</th>
            <th align="left" >End Date / Time</th>
            <th align="left" >JobID</th>
            <th align="left" >JobPartID</th>
            <th align="center" >Device</th>
            <th align="center" >Status</th>
          <th align="center" >Show JDF</th>
          </tr>
          <xsl:apply-templates/>
        </table>
        <hr/>
        <form ><xsl:attribute name="action">../showQueue/<xsl:value-of select="@DeviceID" /></xsl:attribute>
			     <input type="submit" value="refresh queue" /> 
				</form>

      </body>
    </html>
  </xsl:template>

  <xsl:template match="jdf:QueueEntry">
	<tr>
	<xsl:if test="@Status='Running'">
      <xsl:attribute name="bgcolor">#aaffaa</xsl:attribute>
  </xsl:if>
	<xsl:if test="@Status='Waiting'">
      <xsl:attribute name="bgcolor">#ddddd</xsl:attribute>
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
  <td align="left"><xsl:value-of select = "@QueueEntryID" /></td>
	  <td align="left"><xsl:value-of select = "@SubmissionTime" /></td>
	  <td align="left"><xsl:value-of select = "@StartTime" /></td>
	  <td align="left"><xsl:value-of select = "@EndTime" /></td>
	  <td align="left"><xsl:value-of select = "@JobID" /></td>
	  <td align="left"><xsl:value-of select = "@JobPartID" /></td>
      <td>
       <a><xsl:attribute name="href">../showDevice/<xsl:value-of select="@DeviceID"/></xsl:attribute>
      Device: <xsl:value-of select="@DeviceID"/> </a>     
       </td>
       
       <td nowrap="true">
      <!-- calls the optionList -->
<form>
<xsl:attribute name="action">../modifyQE/<xsl:value-of select="../@DeviceID" /></xsl:attribute>

         <xsl:apply-templates/>
  <input type="hidden" name="qeID">
  <xsl:attribute name="value"><xsl:value-of select="@QueueEntryID" /></xsl:attribute>
  </input>  
   <input type="submit" value="modify entry" /> 
			   </form>
         </td>
      <td nowrap="true">
     <a><xsl:attribute name="href">../showJDF/<xsl:value-of select="../@DeviceID"/>?qeID=<xsl:value-of select="@QueueEntryID"/></xsl:attribute>
       Show JDF</a>
         </td>
	</tr>

  </xsl:template> 
  
<xsl:include href="optionlist.xsl"/> 
 
</xsl:stylesheet>
