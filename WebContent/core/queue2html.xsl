<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
	xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi" >
  <xsl:output method="html"/>
  
  <xsl:template match="jdf:Queue">
    <html>
      <head> </head>
      <body>
      <h1>Queue - DeviceID=<xsl:value-of select = "@DeviceID" /></h1>
      <hr/>
      Show: 
      <a><xsl:attribute name="href">../showDevice/<xsl:value-of select="@DeviceID"/></xsl:attribute>
      Device: <xsl:value-of select="@DeviceID"/> </a>
     
      <hr/>
        <table cellspacing="5">
          <tr>
            <th align="left" >QueueEntryID</th>
            <th align="center" >Device</th>
            <th align="center" >Status</th>
           <th align="right" >Modify</th>
          <th align="right" >Show JDF</th>
          </tr>
          <xsl:apply-templates/>
        </table>
        <hr/>
        <form >
	<xsl:attribute name="action">../showQueue/<xsl:value-of select="@DeviceID" /></xsl:attribute>
			   <input type="submit" value="refresh queue" /> 
							   </form>

      </body>
    </html>
  </xsl:template>

  <xsl:template match="jdf:QueueEntry">
	<tr>
	  <td>
		<xsl:value-of select = "@QueueEntryID" />  
      </td>
      <td>
      <a><xsl:attribute name="href">../showDevice/<xsl:value-of select="@DeviceID"/></xsl:attribute>
      Device: <xsl:value-of select="@DeviceID"/> </a>
     
       </td>
       <td colspan="2" nowrap="true">
      <!-- calls the optionList -->
<form>
<xsl:attribute name="action">../modifyQE/<xsl:value-of select="@DeviceID" /></xsl:attribute>

         <xsl:apply-templates/>
  <input type="hidden" name="qeID">
  <xsl:attribute name="value"><xsl:value-of select="@QueueEntryID" /></xsl:attribute>
  </input>  
   <input type="submit" value="modify entry" /> 
			   </form>
         </td>
      <td nowrap="true">
     <a><xsl:attribute name="href">../showJDF/<xsl:value-of select="@DeviceID"/>?qeID=<xsl:value-of select="@QueueEntryID"/></xsl:attribute>
       Show JDF: <xsl:value-of select="@QueueEntryID"/> </a>
         </td>
	</tr>

  </xsl:template> 
  
<xsl:include href="optionlist.xsl"/> 
 
</xsl:stylesheet>
