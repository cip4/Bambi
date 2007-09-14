<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
	xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi" >
  <xsl:output method="html"/>
  
  <xsl:template match="jdf:Queue">
    <html>
      <head> </head>
      <body>
        <table>
          <tr>
            <th align="left" width="210px">QueueEntryID</th>
            <th align="left" width="90">Status</th>
            <th align="left" width="90">Device</th>
          </tr>
          <xsl:apply-templates/>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="jdf:QueueEntry">
	<tr>
	  <td>
		<xsl:value-of select = "@QueueEntryID" />  
      </td>
      <td>
	    <xsl:value-of select = "@Status" />  
      </td>
      <td>
        <xsl:value-of select = "@bambi:DeviceID" />
      </td>
	</tr>
  </xsl:template> 

</xsl:stylesheet>